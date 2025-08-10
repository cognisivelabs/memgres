package com.memgres.testing.spring;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.testing.MemGresTestDataSource;
import com.memgres.testing.MemGresTestHelper;
import org.springframework.test.context.TestContext;

import javax.sql.DataSource;

/**
 * Utility class for Spring Test integration with MemGres.
 * 
 * <p>This class provides convenient static methods for accessing MemGres
 * components from within Spring test methods. It works in conjunction with
 * the {@link DataMemGres} annotation and {@link MemGresTestExecutionListener}.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * @SpringBootTest
 * @DataMemGres
 * class UserRepositoryTest {
 *     
 *     @Test
 *     void testDirectDatabaseAccess(@Autowired TestContext testContext) {
 *         // Get MemGres engine directly
 *         MemGresEngine engine = SpringMemGresTestHelper.getEngine(testContext);
 *         
 *         // Execute SQL directly
 *         SqlExecutionEngine sqlEngine = SpringMemGresTestHelper.getSqlExecutionEngine(testContext);
 *         
 *         // Get DataSource for JDBC operations
 *         DataSource dataSource = SpringMemGresTestHelper.getDataSource(testContext);
 *     }
 * }
 * }</pre>
 * 
 * @since 1.0.0
 */
public final class SpringMemGresTestHelper {
    
    private static final String ENGINE_ATTR = "memgres.engine";
    private static final String DATASOURCE_ATTR = "memgres.datasource";
    private static final String HELPER_ATTR = "memgres.helper";
    private static final String CONFIG_ATTR = "memgres.config";
    
    private SpringMemGresTestHelper() {
        // Utility class
    }
    
    /**
     * Retrieves the MemGres engine instance from the test context.
     * 
     * @param testContext the Spring test context
     * @return the MemGres engine instance
     * @throws IllegalStateException if the engine is not available
     */
    public static MemGresEngine getEngine(TestContext testContext) {
        MemGresEngine engine = (MemGresEngine) testContext.getAttribute(ENGINE_ATTR);
        if (engine == null) {
            throw new IllegalStateException("MemGres engine not available. Ensure @DataMemGres annotation is present.");
        }
        return engine;
    }
    
    /**
     * Retrieves the SQL execution engine from the test context.
     * 
     * @param testContext the Spring test context
     * @return the SQL execution engine
     * @throws IllegalStateException if the engine is not available
     */
    public static SqlExecutionEngine getSqlExecutionEngine(TestContext testContext) {
        return new SqlExecutionEngine(getEngine(testContext));
    }
    
    /**
     * Retrieves the MemGres DataSource from the test context.
     * 
     * @param testContext the Spring test context
     * @return the MemGres DataSource
     * @throws IllegalStateException if the DataSource is not available
     */
    public static DataSource getDataSource(TestContext testContext) {
        MemGresTestDataSource dataSource = (MemGresTestDataSource) testContext.getAttribute(DATASOURCE_ATTR);
        if (dataSource == null) {
            throw new IllegalStateException("MemGres DataSource not available. Ensure @DataMemGres annotation is present.");
        }
        return dataSource;
    }
    
    /**
     * Retrieves the MemGres test helper from the test context.
     * 
     * @param testContext the Spring test context
     * @return the test helper instance
     * @throws IllegalStateException if the helper is not available
     */
    public static MemGresTestHelper getTestHelper(TestContext testContext) {
        MemGresTestHelper helper = (MemGresTestHelper) testContext.getAttribute(HELPER_ATTR);
        if (helper == null) {
            throw new IllegalStateException("MemGres test helper not available. Ensure @DataMemGres annotation is present.");
        }
        return helper;
    }
    
    /**
     * Retrieves the MemGres configuration from the test context.
     * 
     * @param testContext the Spring test context
     * @return the configuration instance
     * @throws IllegalStateException if the configuration is not available
     */
    public static MemGresTestHelper.MemGresConfig getConfig(TestContext testContext) {
        MemGresTestHelper.MemGresConfig config = (MemGresTestHelper.MemGresConfig) testContext.getAttribute(CONFIG_ATTR);
        if (config == null) {
            throw new IllegalStateException("MemGres configuration not available. Ensure @DataMemGres annotation is present.");
        }
        return config;
    }
    
    /**
     * Checks if a MemGres database is available in the current test context.
     * 
     * @param testContext the Spring test context
     * @return true if MemGres is available, false otherwise
     */
    public static boolean isMemGresAvailable(TestContext testContext) {
        return testContext.getAttribute(ENGINE_ATTR) != null;
    }
    
    /**
     * Executes a SQL statement directly against the MemGres database.
     * This is a convenience method for quick SQL execution in tests.
     * 
     * @param testContext the Spring test context
     * @param sql the SQL statement to execute
     * @return the result of the SQL execution
     * @throws IllegalStateException if MemGres is not available
     */
    public static com.memgres.sql.execution.SqlExecutionResult executeSQL(TestContext testContext, String sql) 
            throws com.memgres.sql.execution.SqlExecutionException {
        SqlExecutionEngine sqlEngine = getSqlExecutionEngine(testContext);
        return sqlEngine.execute(sql);
    }
}