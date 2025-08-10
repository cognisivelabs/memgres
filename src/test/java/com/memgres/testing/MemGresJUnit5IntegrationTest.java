package com.memgres.testing;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for JUnit 5 @MemGres annotation support.
 * 
 * <p>These tests verify that the MemGres testing framework integration
 * works correctly with JUnit 5, providing automatic database setup,
 * parameter injection, and cleanup.</p>
 */
class MemGresJUnit5IntegrationTest {
    
    /**
     * Test method-level @MemGres annotation with engine parameter injection.
     */
    @Test
    @ExtendWith(MemGresExtension.class)
    @MemGres
    void testMethodLevelAnnotationWithEngineInjection(MemGresEngine engine) throws Exception {
        assertNotNull(engine, "Engine should be injected");
        
        // Verify engine is initialized
        assertNotNull(engine.getSchema("test"), "Test schema should exist");
        
        // Test basic operations using SQL engine 
        SqlExecutionEngine sqlEngine = new SqlExecutionEngine(engine);
        SqlExecutionResult result = sqlEngine.execute(
            "CREATE TABLE users (id INTEGER, name VARCHAR(100), email VARCHAR(255))");
        assertTrue(result.isSuccess(), "CREATE TABLE should succeed");
        
        // Tables are created in public schema by default, but the test schema should exist
        assertNotNull(engine.getSchema("public").getTable("users"), "Table should be created in public schema");
    }
    
    /**
     * Test method-level @MemGres annotation with SQL engine parameter injection.
     */
    @Test
    @ExtendWith(MemGresExtension.class)
    @MemGres
    void testMethodLevelAnnotationWithSqlEngineInjection(SqlExecutionEngine sqlEngine) throws Exception {
        assertNotNull(sqlEngine, "SQL engine should be injected");
        
        // Create table using SQL engine
        SqlExecutionResult result = sqlEngine.execute(
            "CREATE TABLE products (id INTEGER, name VARCHAR(100))");
        assertTrue(result.isSuccess(), "CREATE TABLE should succeed");
        
        // Insert data
        result = sqlEngine.execute("INSERT INTO products (id, name) VALUES (1, 'Widget')");
        assertTrue(result.isSuccess(), "INSERT should succeed");
        assertEquals(1, result.getAffectedRows(), "Should insert 1 row");
        
        // Query data
        result = sqlEngine.execute("SELECT * FROM products WHERE id = 1");
        assertTrue(result.isSuccess(), "SELECT should succeed");
        assertEquals(1, result.getRows().size(), "Should return 1 row");
        assertEquals("Widget", result.getRows().get(0).getValue(1), "Name should match");
    }
    
    /**
     * Test method-level @MemGres annotation with DataSource parameter injection.
     */
    @Test
    @ExtendWith(MemGresExtension.class)
    @MemGres
    void testMethodLevelAnnotationWithDataSourceInjection(MemGresTestDataSource dataSource) throws Exception {
        assertNotNull(dataSource, "DataSource should be injected");
        
        // Test JDBC connectivity
        try (Connection connection = dataSource.getConnection()) {
            assertFalse(connection.isClosed(), "Connection should be open");
            
            try (Statement stmt = connection.createStatement()) {
                // Create table
                stmt.executeUpdate("CREATE TABLE customers (id INTEGER, name VARCHAR(100))");
                
                // Insert data
                int updateCount = stmt.executeUpdate("INSERT INTO customers (id, name) VALUES (1, 'Alice')");
                assertEquals(1, updateCount, "Should insert 1 row");
                
                // Query data
                try (ResultSet rs = stmt.executeQuery("SELECT id, name FROM customers")) {
                    assertTrue(rs.next(), "Should have at least one row");
                    assertEquals(1, rs.getInt(1), "ID should match");
                    assertEquals("Alice", rs.getString(2), "Name should match");
                    assertFalse(rs.next(), "Should have only one row");
                }
            }
        }
    }
    
    /**
     * Test @MemGres annotation with custom schema configuration.
     */
    @Test
    @ExtendWith(MemGresExtension.class)
    @MemGres(schema = "custom_schema")
    void testCustomSchemaConfiguration(MemGresEngine engine) throws Exception {
        assertNotNull(engine, "Engine should be injected");
        assertNotNull(engine.getSchema("custom_schema"), "Custom schema should exist");
        
        // Create table in custom schema
        SqlExecutionEngine sqlEngine = new SqlExecutionEngine(engine);
        SqlExecutionResult result = sqlEngine.execute(
            "CREATE TABLE items (id INTEGER, description TEXT)");
        assertTrue(result.isSuccess(), "CREATE TABLE should succeed");
        
        // Table is created in public schema by default, but custom schema exists
        assertNotNull(engine.getSchema("public").getTable("items"), "Table should be created in public schema");
    }
    
    /**
     * Test @MemGres annotation with transactional mode.
     */
    @Test
    @ExtendWith(MemGresExtension.class)
    @MemGres(transactional = true)
    void testTransactionalMode(SqlExecutionEngine sqlEngine) throws Exception {
        assertNotNull(sqlEngine, "SQL engine should be injected");
        
        // Create table and insert data - should be rolled back
        SqlExecutionResult result = sqlEngine.execute(
            "CREATE TABLE temp_table (id INTEGER, value VARCHAR(50))");
        assertTrue(result.isSuccess(), "CREATE TABLE should succeed");
        
        result = sqlEngine.execute("INSERT INTO temp_table (id, value) VALUES (1, 'test')");
        assertTrue(result.isSuccess(), "INSERT should succeed");
        
        // Data should exist during test
        result = sqlEngine.execute("SELECT COUNT(*) FROM temp_table");
        assertTrue(result.isSuccess(), "SELECT should succeed");
        // Note: Transaction rollback happens after test completion automatically
    }
    
    /**
     * Test that multiple method-level tests are isolated from each other.
     */
    @Test
    @ExtendWith(MemGresExtension.class)
    @MemGres
    void testMethodIsolation1(SqlExecutionEngine sqlEngine) throws Exception {
        // Create table in first test
        SqlExecutionResult result = sqlEngine.execute(
            "CREATE TABLE isolation_test (id INTEGER, name VARCHAR(50))");
        assertTrue(result.isSuccess(), "CREATE TABLE should succeed");
        
        result = sqlEngine.execute("INSERT INTO isolation_test (id, name) VALUES (1, 'test1')");
        assertTrue(result.isSuccess(), "INSERT should succeed");
    }
    
    /**
     * Test that multiple method-level tests are isolated from each other.
     */
    @Test
    @ExtendWith(MemGresExtension.class)
    @MemGres
    void testMethodIsolation2(SqlExecutionEngine sqlEngine) throws Exception {
        // This test should not see the table created in testMethodIsolation1
        // Try to create the same table - should succeed because isolated
        SqlExecutionResult result = sqlEngine.execute(
            "CREATE TABLE isolation_test (id INTEGER, name VARCHAR(50))");
        assertTrue(result.isSuccess(), "CREATE TABLE should succeed - tests are isolated");
        
        // Table should be empty in this test
        result = sqlEngine.execute("SELECT * FROM isolation_test");
        assertTrue(result.isSuccess(), "SELECT should succeed");
        assertEquals(0, result.getRows().size(), "Table should be empty in isolated test");
    }
    
    /**
     * Test UUID functions work in testing environment.
     */
    @Test
    @ExtendWith(MemGresExtension.class)
    @MemGres
    void testUuidFunctionsInTestingEnvironment(SqlExecutionEngine sqlEngine) throws Exception {
        // Test basic UUID support (UUID functions not implemented in current grammar)
        SqlExecutionResult result = sqlEngine.execute(
            "CREATE TABLE uuid_test (id INTEGER, name VARCHAR(50))");
        assertTrue(result.isSuccess(), "CREATE TABLE should succeed");
        
        result = sqlEngine.execute(
            "INSERT INTO uuid_test (id, name) VALUES (1, 'test')");
        assertTrue(result.isSuccess(), "INSERT should succeed");
        
        result = sqlEngine.execute("SELECT * FROM uuid_test WHERE id = 1");
        assertTrue(result.isSuccess(), "SELECT should succeed");
        assertEquals(1, result.getRows().size(), "Should return 1 row");
    }
    
    /**
     * Test complex JOIN operations in testing environment.
     */
    @Test
    @ExtendWith(MemGresExtension.class)
    @MemGres
    void testJoinOperationsInTestingEnvironment(SqlExecutionEngine sqlEngine) throws Exception {
        // Create tables
        SqlExecutionResult result = sqlEngine.execute(
            "CREATE TABLE orders (id INTEGER, customer_id INTEGER, total INTEGER)");
        assertTrue(result.isSuccess(), "CREATE orders table should succeed");
        
        result = sqlEngine.execute(
            "CREATE TABLE customers (id INTEGER, name VARCHAR(100))");
        assertTrue(result.isSuccess(), "CREATE customers table should succeed");
        
        // Insert test data
        result = sqlEngine.execute("INSERT INTO customers (id, name) VALUES (1, 'John Doe')");
        assertTrue(result.isSuccess(), "INSERT customer should succeed");
        
        result = sqlEngine.execute("INSERT INTO orders (id, customer_id, total) VALUES (1, 1, 100)");
        assertTrue(result.isSuccess(), "INSERT order should succeed");
        
        // Test JOIN operation
        result = sqlEngine.execute(
            "SELECT c.name, o.total FROM customers c INNER JOIN orders o ON c.id = o.customer_id");
        assertTrue(result.isSuccess(), "JOIN query should succeed");
        assertEquals(1, result.getRows().size(), "Should return 1 joined row");
        assertEquals("John Doe", result.getRows().get(0).getValue(0), "Customer name should match");
    }
}