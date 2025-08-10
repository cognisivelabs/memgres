package com.memgres.testing.spring;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionResult;
import com.memgres.testing.MemGresTestDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Spring Test integration with MemGres.
 * 
 * <p>This test class demonstrates and validates the Spring Test integration
 * features provided by the {@link DataMemGres} annotation and related components.</p>
 * 
 * @since 1.0.0
 */
@SpringJUnitConfig
@DataMemGres(
    schema = "spring_test",
    initScripts = {},
    transactional = false
)
@TestExecutionListeners(
    listeners = MemGresTestExecutionListener.class,
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
class SpringMemGresIntegrationTest {
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private MemGresEngine engine;
    
    @Autowired
    private SqlExecutionEngine sqlExecutionEngine;
    
    @Test
    void testDataSourceInjection() {
        assertNotNull(dataSource, "DataSource should be injected");
        assertInstanceOf(MemGresTestDataSource.class, dataSource, "DataSource should be MemGres instance");
    }
    
    @Test
    void testEngineInjection() {
        assertNotNull(engine, "MemGresEngine should be injected");
        assertNotNull(engine.getSchema("spring_test"), "Should have configured schema");
    }
    
    @Test
    void testSqlExecutionEngineInjection() throws Exception {
        assertNotNull(sqlExecutionEngine, "SqlExecutionEngine should be injected");
        
        // Test direct SQL execution
        SqlExecutionResult result = sqlExecutionEngine.execute(
            "CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT NOT NULL)"
        );
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        assertTrue(result.isSuccess());
        
        result = sqlExecutionEngine.execute(
            "INSERT INTO users (id, name) VALUES (1, 'John Doe')"
        );
        assertEquals(SqlExecutionResult.ResultType.INSERT, result.getType());
        assertTrue(result.isSuccess());
        
        result = sqlExecutionEngine.execute("SELECT * FROM users");
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        assertNotNull(result.getRows());
        assertEquals(1, result.getRows().size());
    }
    
    @Test
    void testJDBCOperations() throws Exception {
        // Create table
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("CREATE TABLE products (id INTEGER PRIMARY KEY, name TEXT, price DECIMAL(10,2))");
            
            // Insert data
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO products (id, name, price) VALUES (?, ?, ?)")) {
                ps.setInt(1, 1);
                ps.setString(2, "Laptop");
                ps.setBigDecimal(3, new java.math.BigDecimal("999.99"));
                int rows = ps.executeUpdate();
                assertEquals(1, rows);
            }
            
            // Query data
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM products WHERE id = ?")) {
                ps.setInt(1, 1);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(1, rs.getInt("id"));
                    assertEquals("Laptop", rs.getString("name"));
                    assertEquals(new java.math.BigDecimal("999.99"), rs.getBigDecimal("price"));
                    assertFalse(rs.next());
                }
            }
        }
    }
    
    @Test
    void testDatabaseIsolation() throws Exception {
        // This test should not see data from other tests
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Try to query users table - should not exist from previous test
            assertThrows(Exception.class, () -> {
                stmt.executeQuery("SELECT * FROM users");
            }, "Users table should not exist in new test (database isolation)");
            
            // Create and use a different table
            stmt.execute("CREATE TABLE orders (id INTEGER PRIMARY KEY, customer TEXT)");
            stmt.execute("INSERT INTO orders (id, customer) VALUES (1, 'Alice')");
            
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM orders")) {
                assertTrue(rs.next());
                assertEquals("Alice", rs.getString("customer"));
            }
        }
    }
    
    @Test
    void testComplexQueries() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create related tables
            stmt.execute("CREATE TABLE departments (id INTEGER PRIMARY KEY, name TEXT)");
            stmt.execute("CREATE TABLE employees (id INTEGER PRIMARY KEY, name TEXT, dept_id INTEGER, salary DECIMAL(10,2))");
            
            // Insert test data
            stmt.execute("INSERT INTO departments (id, name) VALUES (1, 'Engineering'), (2, 'Sales')");
            stmt.execute("INSERT INTO employees (id, name, dept_id, salary) VALUES " +
                        "(1, 'John', 1, 75000.00), (2, 'Jane', 1, 80000.00), (3, 'Bob', 2, 60000.00)");
            
            // Test JOIN query
            String joinQuery = "SELECT e.name, d.name as dept_name, e.salary " +
                             "FROM employees e JOIN departments d ON e.dept_id = d.id " +
                             "ORDER BY e.salary DESC";
            
            try (ResultSet rs = stmt.executeQuery(joinQuery)) {
                assertTrue(rs.next());
                assertEquals("Jane", rs.getString("name"));
                assertEquals("Engineering", rs.getString("dept_name"));
                assertEquals(new java.math.BigDecimal("80000.00"), rs.getBigDecimal("salary"));
                
                assertTrue(rs.next());
                assertEquals("John", rs.getString("name"));
                assertEquals("Engineering", rs.getString("dept_name"));
                
                assertTrue(rs.next());
                assertEquals("Bob", rs.getString("name"));
                assertEquals("Sales", rs.getString("dept_name"));
                
                assertFalse(rs.next());
            }
            
            // Test aggregation query
            String aggQuery = "SELECT d.name, COUNT(*) as emp_count, AVG(e.salary) as avg_salary " +
                            "FROM departments d LEFT JOIN employees e ON d.id = e.dept_id " +
                            "GROUP BY d.id, d.name ORDER BY d.id";
            
            try (ResultSet rs = stmt.executeQuery(aggQuery)) {
                assertTrue(rs.next());
                assertEquals("Engineering", rs.getString("name"));
                assertEquals(2, rs.getInt("emp_count"));
                assertEquals(77500.0, rs.getDouble("avg_salary"), 0.01);
                
                assertTrue(rs.next());
                assertEquals("Sales", rs.getString("name"));
                assertEquals(1, rs.getInt("emp_count"));
                assertEquals(60000.0, rs.getDouble("avg_salary"), 0.01);
                
                assertFalse(rs.next());
            }
        }
    }
    
    @Test
    void testTransactionBehavior() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE accounts (id INTEGER PRIMARY KEY, balance DECIMAL(10,2))");
                stmt.execute("INSERT INTO accounts (id, balance) VALUES (1, 1000.00), (2, 500.00)");
                
                // Transfer money between accounts
                stmt.execute("UPDATE accounts SET balance = balance - 100 WHERE id = 1");
                stmt.execute("UPDATE accounts SET balance = balance + 100 WHERE id = 2");
                
                // Verify changes before commit
                try (ResultSet rs = stmt.executeQuery("SELECT balance FROM accounts WHERE id = 1")) {
                    assertTrue(rs.next());
                    assertEquals(new java.math.BigDecimal("900.00"), rs.getBigDecimal("balance"));
                }
                
                // Rollback transaction
                conn.rollback();
                
                // Verify rollback
                try (ResultSet rs = stmt.executeQuery("SELECT balance FROM accounts WHERE id = 1")) {
                    assertTrue(rs.next());
                    assertEquals(new java.math.BigDecimal("1000.00"), rs.getBigDecimal("balance"));
                }
                
                // Now commit the changes
                stmt.execute("UPDATE accounts SET balance = balance - 50 WHERE id = 1");
                stmt.execute("UPDATE accounts SET balance = balance + 50 WHERE id = 2");
                conn.commit();
                
                // Verify commit persisted
                try (ResultSet rs = stmt.executeQuery("SELECT balance FROM accounts ORDER BY id")) {
                    assertTrue(rs.next());
                    assertEquals(new java.math.BigDecimal("950.00"), rs.getBigDecimal("balance"));
                    assertTrue(rs.next());
                    assertEquals(new java.math.BigDecimal("550.00"), rs.getBigDecimal("balance"));
                }
            }
        }
    }
}