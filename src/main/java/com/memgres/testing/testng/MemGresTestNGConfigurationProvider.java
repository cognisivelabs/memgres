package com.memgres.testing.testng;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.testing.MemGresTestDataSource;
import com.memgres.testing.MemGresTestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

/**
 * TestNG configuration provider for MemGres components.
 * 
 * <p>This class provides utility methods for TestNG tests to access MemGres components
 * that were set up by the {@link MemGresTestNGListener}. It uses thread-local storage
 * to provide access to MemGres components without requiring parameter injection.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * public class MyTest {
 *     @Test
 *     @MemGres
 *     public void testWithMemGres() {
 *         MemGresEngine engine = MemGresTestNGConfigurationProvider.getEngine();
 *         SqlExecutionEngine sqlEngine = MemGresTestNGConfigurationProvider.getSqlEngine();
 *         // Test implementation
 *     }
 * }
 * }</pre>
 * 
 * @since 1.0.0
 */
public class MemGresTestNGConfigurationProvider extends TestListenerAdapter {
    
    // Thread-local storage for MemGres components
    private static final ThreadLocal<MemGresEngine> threadLocalEngine = new ThreadLocal<>();
    private static final ThreadLocal<MemGresTestHelper> threadLocalHelper = new ThreadLocal<>();
    private static final ThreadLocal<MemGresTestHelper.MemGresConfig> threadLocalConfig = new ThreadLocal<>();
    
    private static final Logger logger = LoggerFactory.getLogger(MemGresTestNGConfigurationProvider.class);
    
    /**
     * Sets the thread-local MemGres components for the current test thread.
     * This is called by MemGresTestNGListener.
     */
    static void setCurrentTestComponents(MemGresEngine engine, MemGresTestHelper helper, MemGresTestHelper.MemGresConfig config) {
        threadLocalEngine.set(engine);
        threadLocalHelper.set(helper);
        threadLocalConfig.set(config);
    }
    
    /**
     * Clears the thread-local MemGres components for the current test thread.
     * This is called by MemGresTestNGListener.
     */
    static void clearCurrentTestComponents() {
        threadLocalEngine.remove();
        threadLocalHelper.remove();
        threadLocalConfig.remove();
    }
    
    /**
     * Gets the MemGresEngine instance for the current test.
     * 
     * @return the MemGresEngine instance
     * @throws IllegalStateException if the engine is not available
     */
    public static MemGresEngine getEngine() {
        MemGresEngine engine = threadLocalEngine.get();
        if (engine == null) {
            throw new IllegalStateException("MemGres engine not available. Ensure @MemGres annotation is present and MemGresTestNGListener is configured.");
        }
        return engine;
    }
    
    /**
     * Gets the MemGresEngine instance for the current test (legacy method for backward compatibility).
     * 
     * @param result the current test result (ignored in new implementation)
     * @return the MemGresEngine instance
     * @throws IllegalStateException if the engine is not available
     * @deprecated Use {@link #getEngine()} instead
     */
    @Deprecated
    public static MemGresEngine getEngine(ITestResult result) {
        return getEngine();
    }
    
    /**
     * Gets the SqlExecutionEngine instance for the current test.
     * 
     * @return the SqlExecutionEngine instance
     * @throws IllegalStateException if the engine is not available
     */
    public static SqlExecutionEngine getSqlEngine() {
        MemGresEngine engine = getEngine();
        return new SqlExecutionEngine(engine);
    }
    
    /**
     * Gets the SqlExecutionEngine instance for the current test (legacy method for backward compatibility).
     * 
     * @param result the current test result (ignored in new implementation)
     * @return the SqlExecutionEngine instance
     * @throws IllegalStateException if the engine is not available
     * @deprecated Use {@link #getSqlEngine()} instead
     */
    @Deprecated
    public static SqlExecutionEngine getSqlEngine(ITestResult result) {
        return getSqlEngine();
    }
    
    /**
     * Gets the MemGresTestDataSource instance for the current test.
     * 
     * @return the MemGresTestDataSource instance
     * @throws IllegalStateException if the engine is not available
     */
    public static MemGresTestDataSource getDataSource() {
        MemGresEngine engine = getEngine();
        return new MemGresTestDataSource(engine);
    }
    
    /**
     * Gets the MemGresTestDataSource instance for the current test (legacy method for backward compatibility).
     * 
     * @param result the current test result (ignored in new implementation)
     * @return the MemGresTestDataSource instance
     * @throws IllegalStateException if the engine is not available
     * @deprecated Use {@link #getDataSource()} instead
     */
    @Deprecated
    public static MemGresTestDataSource getDataSource(ITestResult result) {
        return getDataSource();
    }
    
    /**
     * Gets the MemGresTestHelper instance for the current test.
     * 
     * @return the MemGresTestHelper instance
     * @throws IllegalStateException if the helper is not available
     */
    public static MemGresTestHelper getHelper() {
        MemGresTestHelper helper = threadLocalHelper.get();
        if (helper == null) {
            throw new IllegalStateException("MemGres helper not available. Ensure @MemGres annotation is present and MemGresTestNGListener is configured.");
        }
        return helper;
    }
    
    /**
     * Gets the MemGresTestHelper instance for the current test (legacy method for backward compatibility).
     * 
     * @param result the current test result (ignored in new implementation)
     * @return the MemGresTestHelper instance
     * @throws IllegalStateException if the helper is not available
     * @deprecated Use {@link #getHelper()} instead
     */
    @Deprecated
    public static MemGresTestHelper getHelper(ITestResult result) {
        return getHelper();
    }
    
    /**
     * Gets the MemGresConfig instance for the current test.
     * 
     * @return the MemGresConfig instance
     * @throws IllegalStateException if the config is not available
     */
    public static MemGresTestHelper.MemGresConfig getConfig() {
        MemGresTestHelper.MemGresConfig config = threadLocalConfig.get();
        if (config == null) {
            throw new IllegalStateException("MemGres config not available. Ensure @MemGres annotation is present and MemGresTestNGListener is configured.");
        }
        return config;
    }
    
    /**
     * Gets the MemGresConfig instance for the current test (legacy method for backward compatibility).
     * 
     * @param result the current test result (ignored in new implementation)
     * @return the MemGresConfig instance
     * @throws IllegalStateException if the config is not available
     * @deprecated Use {@link #getConfig()} instead
     */
    @Deprecated
    public static MemGresTestHelper.MemGresConfig getConfig(ITestResult result) {
        return getConfig();
    }
    
    /**
     * Checks if MemGres is available for the current test.
     * 
     * @return true if MemGres is available, false otherwise
     */
    public static boolean isMemGresAvailable() {
        return threadLocalEngine.get() != null;
    }
    
    /**
     * Checks if MemGres is available for the current test (legacy method for backward compatibility).
     * 
     * @param result the current test result (ignored in new implementation)
     * @return true if MemGres is available, false otherwise
     * @deprecated Use {@link #isMemGresAvailable()} instead
     */
    @Deprecated
    public static boolean isMemGresAvailable(ITestResult result) {
        return isMemGresAvailable();
    }
}