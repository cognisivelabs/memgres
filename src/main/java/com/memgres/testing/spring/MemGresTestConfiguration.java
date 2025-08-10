package com.memgres.testing.spring;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.testing.MemGresTestDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Spring Boot auto-configuration for MemGres testing support.
 * 
 * <p>This configuration class provides default beans for MemGres components
 * when they are not already defined in the application context. It ensures
 * that MemGres DataSource can be automatically injected into Spring components
 * during testing.</p>
 * 
 * <p>This configuration is automatically activated when MemGres classes are
 * present on the classpath and is typically used in conjunction with the
 * {@link DataMemGres} annotation.</p>
 * 
 * <p>Example usage in a Spring Boot test:</p>
 * <pre>{@code
 * @SpringBootTest
 * @DataMemGres
 * class UserServiceTest {
 *     
 *     @Autowired
 *     private DataSource dataSource; // MemGres DataSource injected
 *     
 *     @Autowired
 *     private UserService userService;
 *     
 *     @Test
 *     void testCreateUser() {
 *         // Test using MemGres database
 *     }
 * }
 * }</pre>
 * 
 * @since 1.0.0
 */
@Configuration
public class MemGresTestConfiguration {
    
    // Note: Beans are now registered dynamically by MemGresTestExecutionListener
    // to avoid circular dependency issues during Spring context initialization.
    // The MemGresEngine, DataSource, and SqlExecutionEngine beans are created
    // and registered in the beforeTestClass() method of MemGresTestExecutionListener.
}