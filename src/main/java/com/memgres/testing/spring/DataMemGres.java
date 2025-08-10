package com.memgres.testing.spring;

import org.springframework.test.context.TestExecutionListeners;

import java.lang.annotation.*;

/**
 * Spring Test annotation for automatic MemGres database setup and teardown.
 * 
 * <p>This annotation can be applied to Spring test classes to automatically
 * create and configure an in-memory MemGres database instance for testing.
 * The database is initialized before the test runs and cleaned up afterward.</p>
 * 
 * <p>This annotation automatically registers the {@link MemGresTestExecutionListener}
 * to handle the database lifecycle. It also provides configuration options for
 * database initialization and transaction management.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * @SpringBootTest
 * @DataMemGres
 * class UserRepositoryTest {
 *     
 *     @Autowired
 *     private DataSource dataSource;
 *     
 *     @Autowired
 *     private UserRepository userRepository;
 *     
 *     @Test
 *     void testSaveUser() {
 *         // MemGres database automatically available via DataSource
 *         User user = new User("John Doe");
 *         userRepository.save(user);
 *         // Test implementation
 *     }
 * }
 * }</pre>
 * 
 * <p>With custom configuration:</p>
 * <pre>{@code
 * @SpringBootTest
 * @DataMemGres(
 *     schema = "integration_test",
 *     initScripts = {"/sql/schema.sql", "/sql/test-data.sql"},
 *     transactional = true
 * )
 * class IntegrationTest {
 *     // Database with custom schema and initialization scripts
 * }
 * }</pre>
 * 
 * @see MemGresTestExecutionListener
 * @see MemGresTestConfiguration
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@TestExecutionListeners(
    listeners = MemGresTestExecutionListener.class,
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public @interface DataMemGres {
    
    /**
     * The database schema name to use. Defaults to "test".
     * @return the schema name
     */
    String schema() default "test";
    
    /**
     * Whether to automatically create tables from SQL scripts.
     * When true, looks for SQL files in test/resources matching the test class name.
     * @return true to enable automatic table creation
     */
    boolean autoCreateTables() default false;
    
    /**
     * SQL script paths to execute for database setup.
     * Scripts are executed in the order specified.
     * Resource paths should be relative to the classpath (e.g., "/sql/schema.sql").
     * @return array of SQL script resource paths
     */
    String[] initScripts() default {};
    
    /**
     * Whether to enable transaction rollback after each test method.
     * When true, each test method runs in its own transaction that is rolled back.
     * This works with Spring's @Transactional annotation and TestTransaction.
     * @return true to enable transaction rollback
     */
    boolean transactional() default false;
    
    /**
     * Database startup timeout in milliseconds.
     * @return timeout value
     */
    long startupTimeoutMs() default 5000L;
    
    /**
     * The name of the DataSource bean to register in the application context.
     * This allows the MemGres DataSource to be injected into your test components.
     * @return the DataSource bean name
     */
    String dataSourceBeanName() default "memgresDataSource";
    
    /**
     * Whether to replace any existing DataSource bean with the MemGres instance.
     * When true, any existing DataSource bean will be replaced with the MemGres DataSource.
     * This is useful for integration tests that need to override production DataSources.
     * @return true to replace existing DataSource beans
     */
    boolean replaceDataSource() default true;
}