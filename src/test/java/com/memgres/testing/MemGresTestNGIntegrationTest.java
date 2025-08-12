package com.memgres.testing;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionResult;
import com.memgres.testing.testng.MemGresTestNGConfigurationProvider;
import com.memgres.testing.testng.MemGresTestNGListener;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Integration tests for TestNG @MemGres annotation support.
 * 
 * <p>These tests verify that the MemGres testing framework integration
 * works correctly with TestNG, providing automatic database setup,
 * parameter injection, and cleanup.</p>
 */
@Listeners(MemGresTestNGListener.class)
public class MemGresTestNGIntegrationTest {
    
    /**
     * Test method-level @MemGres annotation with manual engine retrieval.
     */
    @Test
    @MemGres
    public void testMethodLevelAnnotationWithManualEngineRetrieval() throws Exception {
        MemGresEngine engine = MemGresTestNGConfigurationProvider.getEngine();
        Assert.assertNotNull(engine, "Engine should be available");
        
        // Verify engine is initialized
        Assert.assertNotNull(engine.getSchema("test"), "Test schema should exist");
        
        // Test basic operations using SQL engine 
        SqlExecutionEngine sqlEngine = MemGresTestNGConfigurationProvider.getSqlEngine();
        SqlExecutionResult executionResult = sqlEngine.execute(
            "CREATE TABLE testng_users (id INTEGER, name VARCHAR(100), email VARCHAR(255))");
        Assert.assertTrue(executionResult.isSuccess(), "CREATE TABLE should succeed");
        
        // Tables are created in public schema by default, but the test schema should exist
        Assert.assertNotNull(engine.getSchema("public").getTable("testng_users"), "Table should be created in public schema");
    }
    
    /**
     * Test method-level @MemGres annotation with SQL engine operations.
     */
    @Test
    @MemGres
    public void testMethodLevelAnnotationWithSqlEngineOperations() throws Exception {
        SqlExecutionEngine sqlEngine = MemGresTestNGConfigurationProvider.getSqlEngine();
        Assert.assertNotNull(sqlEngine, "SQL engine should be available");
        
        // Create table using SQL engine
        SqlExecutionResult executionResult = sqlEngine.execute(
            "CREATE TABLE testng_products (id INTEGER, name VARCHAR(100))");
        Assert.assertTrue(executionResult.isSuccess(), "CREATE TABLE should succeed");
        
        // Insert data
        executionResult = sqlEngine.execute("INSERT INTO testng_products (id, name) VALUES (1, 'TestNG Widget')");
        Assert.assertTrue(executionResult.isSuccess(), "INSERT should succeed");
        Assert.assertEquals(executionResult.getAffectedRows(), 1, "Should insert 1 row");
        
        // Query data
        executionResult = sqlEngine.execute("SELECT * FROM testng_products WHERE id = 1");
        Assert.assertTrue(executionResult.isSuccess(), "SELECT should succeed");
        Assert.assertEquals(executionResult.getRows().size(), 1, "Should return 1 row");
        Assert.assertEquals(executionResult.getRows().get(0).getValue(1), "TestNG Widget", "Name should match");
    }
    
    /**
     * Test method-level @MemGres annotation with DataSource operations.
     */
    @Test
    @MemGres
    public void testMethodLevelAnnotationWithDataSourceOperations() throws Exception {
        MemGresTestDataSource dataSource = MemGresTestNGConfigurationProvider.getDataSource();
        Assert.assertNotNull(dataSource, "DataSource should be available");
        
        // Test JDBC connectivity
        try (Connection connection = dataSource.getConnection()) {
            Assert.assertFalse(connection.isClosed(), "Connection should be open");
            
            try (Statement stmt = connection.createStatement()) {
                // Create table
                stmt.executeUpdate("CREATE TABLE testng_customers (id INTEGER, name VARCHAR(100))");
                
                // Insert data
                int updateCount = stmt.executeUpdate("INSERT INTO testng_customers (id, name) VALUES (1, 'TestNG Alice')");
                Assert.assertEquals(updateCount, 1, "Should insert 1 row");
                
                // Query data
                try (ResultSet rs = stmt.executeQuery("SELECT id, name FROM testng_customers")) {
                    Assert.assertTrue(rs.next(), "Should have at least one row");
                    Assert.assertEquals(rs.getInt(1), 1, "ID should match");
                    Assert.assertEquals(rs.getString(2), "TestNG Alice", "Name should match");
                    Assert.assertFalse(rs.next(), "Should have only one row");
                }
            }
        }
    }
    
    /**
     * Test @MemGres annotation with custom schema configuration.
     */
    @Test
    @MemGres(schema = "testng_custom_schema")
    public void testCustomSchemaConfiguration() throws Exception {
        MemGresEngine engine = MemGresTestNGConfigurationProvider.getEngine();
        Assert.assertNotNull(engine, "Engine should be available");
        Assert.assertNotNull(engine.getSchema("testng_custom_schema"), "Custom schema should exist");
        
        // Create table in custom schema
        SqlExecutionEngine sqlEngine = MemGresTestNGConfigurationProvider.getSqlEngine();
        SqlExecutionResult executionResult = sqlEngine.execute(
            "CREATE TABLE testng_items (id INTEGER, description TEXT)");
        Assert.assertTrue(executionResult.isSuccess(), "CREATE TABLE should succeed");
        
        // Table is created in public schema by default, but custom schema exists
        Assert.assertNotNull(engine.getSchema("public").getTable("testng_items"), "Table should be created in public schema");
    }
    
    /**
     * Test @MemGres annotation with transactional mode.
     */
    @Test
    @MemGres(transactional = true)
    public void testTransactionalMode() throws Exception {
        SqlExecutionEngine sqlEngine = MemGresTestNGConfigurationProvider.getSqlEngine();
        Assert.assertNotNull(sqlEngine, "SQL engine should be available");
        
        // Create table and insert data - should be rolled back
        SqlExecutionResult executionResult = sqlEngine.execute(
            "CREATE TABLE testng_temp_table (id INTEGER, data_value VARCHAR(50))");
        Assert.assertTrue(executionResult.isSuccess(), "CREATE TABLE should succeed");
        
        executionResult = sqlEngine.execute("INSERT INTO testng_temp_table (id, data_value) VALUES (1, 'testng_test')");
        Assert.assertTrue(executionResult.isSuccess(), "INSERT should succeed");
        
        // Data should exist during test
        executionResult = sqlEngine.execute("SELECT COUNT(*) FROM testng_temp_table");
        Assert.assertTrue(executionResult.isSuccess(), "SELECT should succeed");
        // Note: Transaction rollback happens after test completion automatically
    }
    
    /**
     * Test that multiple method-level tests are isolated from each other.
     */
    @Test
    @MemGres
    public void testMethodIsolation1() throws Exception {
        // Create table in first test
        SqlExecutionEngine sqlEngine = MemGresTestNGConfigurationProvider.getSqlEngine();
        SqlExecutionResult executionResult = sqlEngine.execute(
            "CREATE TABLE testng_isolation_test (id INTEGER, name VARCHAR(50))");
        Assert.assertTrue(executionResult.isSuccess(), "CREATE TABLE should succeed");
        
        executionResult = sqlEngine.execute("INSERT INTO testng_isolation_test (id, name) VALUES (1, 'testng_test1')");
        Assert.assertTrue(executionResult.isSuccess(), "INSERT should succeed");
    }
    
    /**
     * Test that multiple method-level tests are isolated from each other.
     */
    @Test
    @MemGres
    public void testMethodIsolation2() throws Exception {
        // This test should not see the table created in testMethodIsolation1
        // Try to create the same table - should succeed because isolated
        SqlExecutionEngine sqlEngine = MemGresTestNGConfigurationProvider.getSqlEngine();
        SqlExecutionResult executionResult = sqlEngine.execute(
            "CREATE TABLE testng_isolation_test (id INTEGER, name VARCHAR(50))");
        Assert.assertTrue(executionResult.isSuccess(), "CREATE TABLE should succeed - tests are isolated");
        
        // Table should be empty in this test
        executionResult = sqlEngine.execute("SELECT * FROM testng_isolation_test");
        Assert.assertTrue(executionResult.isSuccess(), "SELECT should succeed");
        Assert.assertEquals(executionResult.getRows().size(), 0, "Table should be empty in isolated test");
    }
    
    /**
     * Test complex JOIN operations in TestNG environment.
     */
    @Test
    @MemGres
    public void testJoinOperationsInTestNGEnvironment() throws Exception {
        SqlExecutionEngine sqlEngine = MemGresTestNGConfigurationProvider.getSqlEngine();
        
        // Create tables
        SqlExecutionResult executionResult = sqlEngine.execute(
            "CREATE TABLE testng_orders (id INTEGER, customer_id INTEGER, total INTEGER)");
        Assert.assertTrue(executionResult.isSuccess(), "CREATE orders table should succeed");
        
        executionResult = sqlEngine.execute(
            "CREATE TABLE testng_customers_join (id INTEGER, name VARCHAR(100))");
        Assert.assertTrue(executionResult.isSuccess(), "CREATE customers table should succeed");
        
        // Insert test data
        executionResult = sqlEngine.execute("INSERT INTO testng_customers_join (id, name) VALUES (1, 'TestNG John Doe')");
        Assert.assertTrue(executionResult.isSuccess(), "INSERT customer should succeed");
        
        executionResult = sqlEngine.execute("INSERT INTO testng_orders (id, customer_id, total) VALUES (1, 1, 200)");
        Assert.assertTrue(executionResult.isSuccess(), "INSERT order should succeed");
        
        // Test JOIN operation
        executionResult = sqlEngine.execute(
            "SELECT c.name, o.total FROM testng_customers_join c INNER JOIN testng_orders o ON c.id = o.customer_id");
        Assert.assertTrue(executionResult.isSuccess(), "JOIN query should succeed");
        Assert.assertEquals(executionResult.getRows().size(), 1, "Should return 1 joined row");
        Assert.assertEquals(executionResult.getRows().get(0).getValue(0), "TestNG John Doe", "Customer name should match");
    }
    
    /**
     * Test configuration provider availability check.
     */
    @Test
    @MemGres
    public void testConfigurationProviderAvailability() throws Exception {
        Assert.assertTrue(MemGresTestNGConfigurationProvider.isMemGresAvailable(), 
                         "MemGres should be available for annotated test");
        
        MemGresTestHelper.MemGresConfig config = MemGresTestNGConfigurationProvider.getConfig();
        Assert.assertNotNull(config, "Config should be available");
        Assert.assertEquals(config.getSchema(), "test", "Default schema should be 'test'");
        Assert.assertFalse(config.isTransactional(), "Default transactional should be false");
    }
}