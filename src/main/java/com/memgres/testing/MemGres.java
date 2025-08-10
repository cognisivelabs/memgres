package com.memgres.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JUnit 5 annotation for automatic MemGres database setup and teardown.
 * 
 * <p>This annotation can be applied to test classes or methods to automatically
 * create and configure an in-memory MemGres database instance for testing.
 * The database is initialized before the test runs and cleaned up afterward.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * @Test
 * @MemGres
 * public void testUserRepository() {
 *     // Automatic in-memory database setup
 *     UserRepository repository = new UserRepository(dataSource);
 *     // Test implementation
 * }
 * }</pre>
 * 
 * <p>For class-level annotation:</p>
 * <pre>{@code
 * @MemGres
 * class UserRepositoryTest {
 *     @Test
 *     void testSaveUser() {
 *         // Database available for all test methods
 *     }
 * }
 * }</pre>
 * 
 * @see MemGresExtension
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MemGres {
    
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
     * @return array of SQL script resource paths
     */
    String[] initScripts() default {};
    
    /**
     * Whether to enable transaction rollback after each test method.
     * When true, each test method runs in its own transaction that is rolled back.
     * @return true to enable transaction rollback
     */
    boolean transactional() default false;
    
    /**
     * Database startup timeout in milliseconds.
     * @return timeout value
     */
    long startupTimeoutMs() default 5000L;
}