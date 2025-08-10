package com.memgres.testing.testng;

import com.memgres.core.MemGresEngine;
import com.memgres.testing.MemGres;
import com.memgres.testing.MemGresTestHelper;
import com.memgres.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.*;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * TestNG listener for MemGres database integration.
 * 
 * <p>This listener automatically manages the lifecycle of MemGres database instances
 * for tests annotated with {@link MemGres}. It provides:</p>
 * <ul>
 *   <li>Automatic database startup and shutdown</li>
 *   <li>Schema creation and initialization</li>
 *   <li>SQL script execution</li>
 *   <li>Transaction management with rollback support</li>
 *   <li>Test isolation</li>
 * </ul>
 * 
 * <p>To use this listener, add it to your TestNG configuration:</p>
 * <pre>{@code
 * @Listeners(MemGresTestNGListener.class)
 * public class MyTest {
 *     @MemGres
 *     @Test
 *     public void testWithMemGres() {
 *         // Test implementation
 *     }
 * }
 * }</pre>
 * 
 * @since 1.0.0
 */
public class MemGresTestNGListener implements ITestListener, IInvokedMethodListener {
    
    private static final Logger logger = LoggerFactory.getLogger(MemGresTestNGListener.class);
    
    private final ConcurrentMap<String, MemGresEngine> classEngines = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, MemGresTestHelper> classHelpers = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, MemGresTestHelper.MemGresConfig> classConfigs = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Transaction> methodTransactions = new ConcurrentHashMap<>();
    
    @Override
    public void onStart(ITestContext context) {
        logger.debug("Starting TestNG test context: {}", context.getName());
    }
    
    @Override
    public void onFinish(ITestContext context) {
        logger.debug("Finishing TestNG test context: {}", context.getName());
        // Clean up any remaining class-level engines
        classEngines.forEach((className, engine) -> {
            MemGresTestHelper helper = classHelpers.get(className);
            if (helper != null && engine != null) {
                try {
                    helper.shutdownEngine(engine);
                    logger.debug("Shutdown class-level MemGres database for: {}", className);
                } catch (Exception e) {
                    logger.error("Error shutting down class-level MemGres database for: " + className, e);
                }
            }
        });
        classEngines.clear();
        classHelpers.clear();
        classConfigs.clear();
    }
    
    @Override
    public void onTestStart(ITestResult result) {
        Method method = result.getMethod().getConstructorOrMethod().getMethod();
        Class<?> testClass = result.getTestClass().getRealClass();
        
        MemGres annotation = findMemGresAnnotation(method, testClass);
        if (annotation == null) {
            return;
        }
        
        try {
            MemGresEngine engine = getOrCreateEngine(testClass, annotation);
            MemGresTestHelper helper = getOrCreateHelper(testClass);
            MemGresTestHelper.MemGresConfig config = getOrCreateConfig(testClass, annotation);
            
            // Start transaction for ALL tests to maintain data consistency within a test method
            // For non-transactional tests, this ensures INSERT/SELECT operations see each other's data
            // For transactional tests, this will be rolled back after the test
            Transaction transaction = engine.getTransactionManager()
                    .beginTransaction(com.memgres.transaction.TransactionIsolationLevel.READ_COMMITTED);
            
            String testKey = getTestKey(result);
            methodTransactions.put(testKey, transaction);
            
            // Set the transaction in the thread-local context so SqlExecutionEngine can use it
            engine.getTransactionManager().setCurrentTransaction(transaction);
            
            // Store engine reference for parameter injection (legacy)
            result.setAttribute("memgres.engine", engine);
            result.setAttribute("memgres.helper", helper);
            result.setAttribute("memgres.config", config);
            
            // Set thread-local components for parameter-free access
            MemGresTestNGConfigurationProvider.setCurrentTestComponents(engine, helper, config);
            
            logger.debug("Started transaction for TestNG test method: {} (transactional={})", 
                        method.getName(), config.isTransactional());
            
        } catch (Exception e) {
            logger.error("Error setting up MemGres for test: " + method.getName(), e);
            throw new RuntimeException("Failed to setup MemGres database", e);
        }
    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
        handleTestFinish(result);
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        handleTestFinish(result);
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        handleTestFinish(result);
    }
    
    private void handleTestFinish(ITestResult result) {
        Method method = result.getMethod().getConstructorOrMethod().getMethod();
        Class<?> testClass = result.getTestClass().getRealClass();
        
        MemGres annotation = findMemGresAnnotation(method, testClass);
        if (annotation == null) {
            return;
        }
        
        try {
            MemGresEngine engine = (MemGresEngine) result.getAttribute("memgres.engine");
            MemGresTestHelper helper = (MemGresTestHelper) result.getAttribute("memgres.helper");
            MemGresTestHelper.MemGresConfig config = (MemGresTestHelper.MemGresConfig) result.getAttribute("memgres.config");
            
            String testKey = getTestKey(result);
            Transaction transaction = methodTransactions.remove(testKey);
            
            // Handle transaction cleanup
            if (helper != null && config != null && transaction != null && engine != null) {
                if (config.isTransactional()) {
                    // For transactional tests, rollback to undo changes
                    helper.rollbackTransactionIfNeeded(engine, transaction, config);
                    logger.debug("Rolled back transaction for transactional TestNG test method: {}", method.getName());
                } else {
                    // For non-transactional tests, commit the changes
                    engine.getTransactionManager().commitTransaction(transaction);
                    logger.debug("Committed transaction for non-transactional TestNG test method: {}", method.getName());
                }
                
                // Clear the transaction context
                engine.getTransactionManager().setCurrentTransaction(null);
                
                // Clear thread-local components
                MemGresTestNGConfigurationProvider.clearCurrentTestComponents();
            }
            
            // Clean up method-level engines
            if (!isClassLevelAnnotation(testClass)) {
                if (engine != null && helper != null) {
                    helper.shutdownEngine(engine);
                    logger.debug("Shutdown method-level MemGres database for TestNG: {}", method.getName());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error cleaning up MemGres for test: " + method.getName(), e);
        }
    }
    
    private MemGres findMemGresAnnotation(Method method, Class<?> testClass) {
        // Check method-level annotation first
        MemGres methodAnnotation = method.getAnnotation(MemGres.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        
        // Check class-level annotation
        return testClass.getAnnotation(MemGres.class);
    }
    
    private boolean isClassLevelAnnotation(Class<?> testClass) {
        return testClass.isAnnotationPresent(MemGres.class);
    }
    
    private MemGresEngine getOrCreateEngine(Class<?> testClass, MemGres annotation) throws Exception {
        // Check if class-level engine exists
        String className = testClass.getName();
        MemGresEngine engine = classEngines.get(className);
        
        if (engine != null) {
            return engine;
        }
        
        // Create new engine
        MemGresTestHelper helper = getOrCreateHelper(testClass);
        MemGresTestHelper.MemGresConfig config = MemGresTestHelper.MemGresConfig.fromAnnotation(annotation);
        engine = helper.createEngine(config);
        
        if (isClassLevelAnnotation(testClass)) {
            // Store class-level engine for reuse
            classEngines.put(className, engine);
        }
        
        logger.debug("Created MemGres database for TestNG class: {}", className);
        
        return engine;
    }
    
    private MemGresTestHelper getOrCreateHelper(Class<?> testClass) {
        String className = testClass.getName();
        return classHelpers.computeIfAbsent(className, k -> new MemGresTestHelper());
    }
    
    private MemGresTestHelper.MemGresConfig getOrCreateConfig(Class<?> testClass, MemGres annotation) {
        String className = testClass.getName();
        return classConfigs.computeIfAbsent(className, k -> MemGresTestHelper.MemGresConfig.fromAnnotation(annotation));
    }
    
    private String getTestKey(ITestResult result) {
        return result.getTestClass().getName() + "." + result.getMethod().getMethodName() + 
               "[" + java.util.Arrays.toString(result.getParameters()) + "]";
    }
}