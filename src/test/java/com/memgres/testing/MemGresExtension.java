package com.memgres.testing;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.transaction.Transaction;
import org.junit.jupiter.api.extension.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * JUnit 5 extension for MemGres database integration.
 * 
 * <p>This extension automatically manages the lifecycle of MemGres database instances
 * for tests annotated with {@link MemGres}. It provides:</p>
 * <ul>
 *   <li>Automatic database startup and shutdown</li>
 *   <li>Schema creation and initialization</li>
 *   <li>SQL script execution</li>
 *   <li>Transaction management with rollback support</li>
 *   <li>Test isolation</li>
 * </ul>
 * 
 * @since 1.0.0
 */
public class MemGresExtension implements 
        BeforeAllCallback, AfterAllCallback,
        BeforeEachCallback, AfterEachCallback,
        ParameterResolver {
    
    private static final Logger logger = LoggerFactory.getLogger(MemGresExtension.class);
    
    private static final String ENGINE_KEY = "memgres.engine";
    private static final String TRANSACTION_KEY = "memgres.transaction";
    private static final String CONFIG_KEY = "memgres.config";
    private static final String HELPER_KEY = "memgres.helper";
    
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        MemGres annotation = findMemGresAnnotation(context);
        if (annotation == null) {
            return; // Only method-level annotation
        }
        
        logger.debug("Setting up MemGres database for test class: {}", context.getTestClass());
        
        MemGresTestHelper helper = new MemGresTestHelper();
        MemGresTestHelper.MemGresConfig config = MemGresTestHelper.MemGresConfig.fromAnnotation(annotation);
        MemGresEngine engine = helper.createEngine(config);
        
        context.getStore(ExtensionContext.Namespace.GLOBAL).put(ENGINE_KEY, engine);
        context.getStore(ExtensionContext.Namespace.GLOBAL).put(CONFIG_KEY, config);
        context.getStore(ExtensionContext.Namespace.GLOBAL).put(HELPER_KEY, helper);
        
        logger.info("MemGres database ready for test class: {}", context.getTestClass());
    }
    
    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        MemGresEngine engine = context.getStore(ExtensionContext.Namespace.GLOBAL)
                .get(ENGINE_KEY, MemGresEngine.class);
        MemGresTestHelper helper = context.getStore(ExtensionContext.Namespace.GLOBAL)
                .get(HELPER_KEY, MemGresTestHelper.class);
        
        if (engine != null && helper != null) {
            logger.debug("Shutting down MemGres database for test class: {}", context.getTestClass());
            helper.shutdownEngine(engine);
            logger.info("MemGres database shutdown complete for test class: {}", context.getTestClass());
        }
    }
    
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        MemGres annotation = findMemGresAnnotation(context);
        if (annotation == null) {
            return;
        }
        
        MemGresEngine engine = getOrCreateEngine(context, annotation);
        MemGresTestHelper helper = context.getStore(ExtensionContext.Namespace.GLOBAL)
                .get(HELPER_KEY, MemGresTestHelper.class);
        MemGresTestHelper.MemGresConfig config = context.getStore(ExtensionContext.Namespace.GLOBAL)
                .get(CONFIG_KEY, MemGresTestHelper.MemGresConfig.class);
        
        if (helper == null) {
            helper = new MemGresTestHelper();
            context.getStore(ExtensionContext.Namespace.GLOBAL).put(HELPER_KEY, helper);
        }
        
        if (config == null) {
            config = MemGresTestHelper.MemGresConfig.fromAnnotation(annotation);
            context.getStore(ExtensionContext.Namespace.GLOBAL).put(CONFIG_KEY, config);
        }
        
        // Start transaction for ALL tests to maintain data consistency within a test method
        // For non-transactional tests, this ensures INSERT/SELECT operations see each other's data
        // For transactional tests, this will be rolled back after the test
        Transaction transaction = engine.getTransactionManager()
                .beginTransaction(com.memgres.transaction.TransactionIsolationLevel.READ_COMMITTED);
        context.getStore(ExtensionContext.Namespace.GLOBAL).put(TRANSACTION_KEY, transaction);
        
        // Set the transaction in the thread-local context so SqlExecutionEngine can use it
        engine.getTransactionManager().setCurrentTransaction(transaction);
        
        logger.debug("Started transaction for test method: {} (transactional={})", 
                    context.getDisplayName(), config.isTransactional());
    }
    
    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        MemGres annotation = findMemGresAnnotation(context);
        if (annotation == null) {
            return;
        }
        
        MemGresEngine engine = context.getStore(ExtensionContext.Namespace.GLOBAL)
                .get(ENGINE_KEY, MemGresEngine.class);
        MemGresTestHelper helper = context.getStore(ExtensionContext.Namespace.GLOBAL)
                .get(HELPER_KEY, MemGresTestHelper.class);
        MemGresTestHelper.MemGresConfig config = context.getStore(ExtensionContext.Namespace.GLOBAL)
                .get(CONFIG_KEY, MemGresTestHelper.MemGresConfig.class);
        Transaction transaction = context.getStore(ExtensionContext.Namespace.GLOBAL)
                .get(TRANSACTION_KEY, Transaction.class);
        
        // Handle transaction cleanup
        if (helper != null && config != null && transaction != null) {
            if (config.isTransactional()) {
                // For transactional tests, rollback to undo changes
                helper.rollbackTransactionIfNeeded(engine, transaction, config);
                logger.debug("Rolled back transaction for transactional test method: {}", context.getDisplayName());
            } else {
                // For non-transactional tests, commit the changes
                engine.getTransactionManager().commitTransaction(transaction);
                logger.debug("Committed transaction for non-transactional test method: {}", context.getDisplayName());
            }
            
            // Clear the transaction context
            engine.getTransactionManager().setCurrentTransaction(null);
        }
        
        // Clean up method-level engines
        if (!isClassLevelAnnotation(context)) {
            if (engine != null && helper != null) {
                helper.shutdownEngine(engine);
                logger.debug("Shutdown method-level MemGres database: {}", context.getDisplayName());
            }
        }
    }
    
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> parameterType = parameterContext.getParameter().getType();
        return parameterType == MemGresEngine.class || 
               parameterType == SqlExecutionEngine.class ||
               parameterType == MemGresTestDataSource.class;
    }
    
    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> parameterType = parameterContext.getParameter().getType();
        
        MemGresEngine engine = extensionContext.getStore(ExtensionContext.Namespace.GLOBAL)
                .get(ENGINE_KEY, MemGresEngine.class);
        MemGresTestHelper helper = extensionContext.getStore(ExtensionContext.Namespace.GLOBAL)
                .get(HELPER_KEY, MemGresTestHelper.class);
        
        if (engine == null) {
            throw new ExtensionConfigurationException("MemGres engine not initialized. Ensure @MemGres annotation is present.");
        }
        
        if (helper == null) {
            helper = new MemGresTestHelper();
        }
        
        return helper.createParameterInstance(parameterType, engine);
    }
    
    private MemGres findMemGresAnnotation(ExtensionContext context) {
        // Check method-level annotation first
        if (context.getTestMethod().isPresent()) {
            Method method = context.getTestMethod().get();
            MemGres methodAnnotation = method.getAnnotation(MemGres.class);
            if (methodAnnotation != null) {
                return methodAnnotation;
            }
        }
        
        // Check class-level annotation
        if (context.getTestClass().isPresent()) {
            Class<?> testClass = context.getTestClass().get();
            return testClass.getAnnotation(MemGres.class);
        }
        
        return null;
    }
    
    private boolean isClassLevelAnnotation(ExtensionContext context) {
        return context.getTestClass().isPresent() && 
               context.getTestClass().get().isAnnotationPresent(MemGres.class);
    }
    
    private MemGresEngine getOrCreateEngine(ExtensionContext context, MemGres annotation) throws Exception {
        // Check if class-level engine exists
        MemGresEngine engine = context.getStore(ExtensionContext.Namespace.GLOBAL)
                .get(ENGINE_KEY, MemGresEngine.class);
        
        if (engine != null) {
            return engine;
        }
        
        // Create method-level engine
        MemGresTestHelper helper = new MemGresTestHelper();
        MemGresTestHelper.MemGresConfig config = MemGresTestHelper.MemGresConfig.fromAnnotation(annotation);
        engine = helper.createEngine(config);
        
        context.getStore(ExtensionContext.Namespace.GLOBAL).put(ENGINE_KEY, engine);
        context.getStore(ExtensionContext.Namespace.GLOBAL).put(HELPER_KEY, helper);
        context.getStore(ExtensionContext.Namespace.GLOBAL).put(CONFIG_KEY, config);
        
        logger.debug("Created method-level MemGres database: {}", context.getDisplayName());
        
        return engine;
    }
}