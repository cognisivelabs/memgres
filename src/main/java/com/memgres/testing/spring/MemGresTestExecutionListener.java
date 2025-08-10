package com.memgres.testing.spring;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.testing.MemGresTestDataSource;
import com.memgres.testing.MemGresTestHelper;
import com.memgres.transaction.Transaction;
import com.memgres.transaction.TransactionIsolationLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import javax.sql.DataSource;

/**
 * Spring TestExecutionListener for MemGres database integration.
 * 
 * <p>This listener automatically manages the lifecycle of MemGres database instances
 * for tests annotated with {@link DataMemGres}. It provides:</p>
 * <ul>
 *   <li>Automatic database startup and shutdown</li>
 *   <li>Spring ApplicationContext integration</li>
 *   <li>DataSource bean registration and injection</li>
 *   <li>Schema creation and initialization</li>
 *   <li>SQL script execution</li>
 *   <li>Transaction management with rollback support</li>
 *   <li>Test isolation</li>
 * </ul>
 * 
 * <p>The listener runs with a higher precedence than the default 
 * {@link DependencyInjectionTestExecutionListener} to ensure the MemGres 
 * database is available for dependency injection.</p>
 * 
 * @since 1.0.0
 */
public class MemGresTestExecutionListener implements TestExecutionListener, Ordered {
    
    private static final Logger logger = LoggerFactory.getLogger(MemGresTestExecutionListener.class);
    
    private static final String ENGINE_ATTR = "memgres.engine";
    private static final String TRANSACTION_ATTR = "memgres.transaction";
    private static final String CONFIG_ATTR = "memgres.config";
    private static final String HELPER_ATTR = "memgres.helper";
    private static final String DATASOURCE_ATTR = "memgres.datasource";
    
    // Run before DependencyInjectionTestExecutionListener to ensure DataSource is available
    private static final int ORDER = DependencyInjectionTestExecutionListener.class
            .getAnnotation(org.springframework.core.annotation.Order.class) != null ?
            DependencyInjectionTestExecutionListener.class.getAnnotation(org.springframework.core.annotation.Order.class).value() - 100 :
            2000;
    
    @Override
    public int getOrder() {
        return ORDER;
    }
    
    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        DataMemGres annotation = findDataMemGresAnnotation(testContext);
        if (annotation == null) {
            return;
        }
        
        logger.debug("Setting up MemGres database for test class: {}", testContext.getTestClass().getName());
        
        MemGresTestHelper helper = new MemGresTestHelper();
        MemGresTestHelper.MemGresConfig config = createConfig(annotation);
        MemGresEngine engine = helper.createEngine(config);
        
        // Create DataSource
        MemGresTestDataSource dataSource = new MemGresTestDataSource(engine);
        
        // Store in test context
        testContext.setAttribute(ENGINE_ATTR, engine);
        testContext.setAttribute(CONFIG_ATTR, config);
        testContext.setAttribute(HELPER_ATTR, helper);
        testContext.setAttribute(DATASOURCE_ATTR, dataSource);
        
        // Register DataSource in Spring ApplicationContext
        registerDataSourceInContext(testContext, annotation, dataSource);
        
        logger.info("MemGres database ready for test class: {}", testContext.getTestClass().getName());
    }
    
    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        MemGresEngine engine = (MemGresEngine) testContext.getAttribute(ENGINE_ATTR);
        MemGresTestHelper helper = (MemGresTestHelper) testContext.getAttribute(HELPER_ATTR);
        
        if (engine != null && helper != null) {
            logger.debug("Shutting down MemGres database for test class: {}", testContext.getTestClass().getName());
            helper.shutdownEngine(engine);
            logger.info("MemGres database shutdown complete for test class: {}", testContext.getTestClass().getName());
        }
        
        // Clean up attributes
        testContext.removeAttribute(ENGINE_ATTR);
        testContext.removeAttribute(CONFIG_ATTR);
        testContext.removeAttribute(HELPER_ATTR);
        testContext.removeAttribute(DATASOURCE_ATTR);
        testContext.removeAttribute(TRANSACTION_ATTR);
    }
    
    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        DataMemGres annotation = findDataMemGresAnnotation(testContext);
        if (annotation == null) {
            return;
        }
        
        MemGresEngine engine = (MemGresEngine) testContext.getAttribute(ENGINE_ATTR);
        MemGresTestHelper.MemGresConfig config = (MemGresTestHelper.MemGresConfig) testContext.getAttribute(CONFIG_ATTR);
        
        if (engine == null || config == null) {
            throw new IllegalStateException("MemGres engine not initialized. Ensure @DataMemGres annotation is present at class level.");
        }
        
        // Start transaction for all test methods to maintain data consistency
        // For non-transactional tests, this ensures INSERT/SELECT operations see each other's data
        // For transactional tests, this will be rolled back after the test
        Transaction transaction = engine.getTransactionManager()
                .beginTransaction(TransactionIsolationLevel.READ_COMMITTED);
        testContext.setAttribute(TRANSACTION_ATTR, transaction);
        
        // Set the transaction in the thread-local context so SqlExecutionEngine can use it
        engine.getTransactionManager().setCurrentTransaction(transaction);
        
        logger.debug("Started transaction for test method: {} (transactional={})", 
                    testContext.getTestMethod().getName(), config.isTransactional());
    }
    
    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        DataMemGres annotation = findDataMemGresAnnotation(testContext);
        if (annotation == null) {
            return;
        }
        
        MemGresEngine engine = (MemGresEngine) testContext.getAttribute(ENGINE_ATTR);
        MemGresTestHelper helper = (MemGresTestHelper) testContext.getAttribute(HELPER_ATTR);
        MemGresTestHelper.MemGresConfig config = (MemGresTestHelper.MemGresConfig) testContext.getAttribute(CONFIG_ATTR);
        Transaction transaction = (Transaction) testContext.getAttribute(TRANSACTION_ATTR);
        
        // Handle transaction cleanup
        if (helper != null && config != null && transaction != null && engine != null) {
            if (config.isTransactional()) {
                // For transactional tests, rollback to undo changes
                helper.rollbackTransactionIfNeeded(engine, transaction, config);
                logger.debug("Rolled back transaction for transactional test method: {}", testContext.getTestMethod().getName());
            } else {
                // For non-transactional tests, commit the changes
                engine.getTransactionManager().commitTransaction(transaction);
                logger.debug("Committed transaction for non-transactional test method: {}", testContext.getTestMethod().getName());
            }
            
            // Clear the transaction context
            engine.getTransactionManager().setCurrentTransaction(null);
            testContext.removeAttribute(TRANSACTION_ATTR);
        }
    }
    
    private DataMemGres findDataMemGresAnnotation(TestContext testContext) {
        return testContext.getTestClass().getAnnotation(DataMemGres.class);
    }
    
    private MemGresTestHelper.MemGresConfig createConfig(DataMemGres annotation) {
        return new MemGresTestHelper.MemGresConfig(
            annotation.schema(),
            annotation.autoCreateTables(),
            annotation.initScripts(),
            annotation.transactional(),
            annotation.startupTimeoutMs()
        );
    }
    
    private void registerDataSourceInContext(TestContext testContext, DataMemGres annotation, DataSource dataSource) {
        // Register the DataSource in the ApplicationContext using Spring's test context framework
        if (testContext.getApplicationContext() instanceof 
            org.springframework.context.support.GenericApplicationContext) {
            
            org.springframework.context.support.GenericApplicationContext context = 
                (org.springframework.context.support.GenericApplicationContext) testContext.getApplicationContext();
            
            String beanName = annotation.dataSourceBeanName();
            
            // Check if DataSource should replace existing bean
            if (annotation.replaceDataSource() && context.containsBean(beanName)) {
                // Remove existing bean definition
                context.removeBeanDefinition(beanName);
                logger.debug("Replaced existing DataSource bean: {}", beanName);
            }
            
            // Register MemGres DataSource
            if (!context.containsBean(beanName)) {
                context.registerBean(beanName, DataSource.class, () -> dataSource);
                logger.debug("Registered MemGres DataSource bean: {}", beanName);
            }
            
            // Also register under standard DataSource name if requested
            if (annotation.replaceDataSource() && !beanName.equals("dataSource") && context.containsBean("dataSource")) {
                context.removeBeanDefinition("dataSource");
                context.registerBean("dataSource", DataSource.class, () -> dataSource);
                logger.debug("Replaced primary DataSource bean with MemGres DataSource");
            } else if (!beanName.equals("dataSource") && !context.containsBean("dataSource")) {
                context.registerBean("dataSource", DataSource.class, () -> dataSource);
                logger.debug("Registered MemGres DataSource as primary DataSource bean");
            }
            
            // Register additional MemGres components for injection
            context.registerBean("memgresEngine", MemGresEngine.class, 
                () -> (MemGresEngine) testContext.getAttribute(ENGINE_ATTR));
            context.registerBean("sqlExecutionEngine", SqlExecutionEngine.class,
                () -> new SqlExecutionEngine((MemGresEngine) testContext.getAttribute(ENGINE_ATTR)));
            
            logger.debug("Registered MemGres components in Spring ApplicationContext");
        } else {
            logger.warn("ApplicationContext is not GenericApplicationContext, cannot register DataSource bean dynamically");
        }
    }
}