package com.memgres.testing;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionResult;
import com.memgres.testing.testng.MemGresTestNGListener;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Simple TestNG test to verify MemGres integration works.
 */
@Listeners(MemGresTestNGListener.class)
public class SimpleTestNGTest {
    
    @Test
    @MemGres
    public void testBasicTestNGIntegration() throws Exception {
        // This test verifies that the TestNG listener can set up the database
        // We'll use the TestNG Reporter to access test context
        ITestResult result = Reporter.getCurrentTestResult();
        
        // Get engine from test attributes set by listener
        Object engineAttr = result.getAttribute("memgres.engine");
        Assert.assertNotNull(engineAttr, "MemGres engine should be set by listener");
        Assert.assertTrue(engineAttr instanceof MemGresEngine, "Should be MemGresEngine instance");
        
        MemGresEngine engine = (MemGresEngine) engineAttr;
        
        // Verify basic functionality
        Assert.assertNotNull(engine.getSchema("test"), "Test schema should exist");
        Assert.assertNotNull(engine.getSchema("public"), "Public schema should exist");
        
        // Test SQL operations
        SqlExecutionEngine sqlEngine = new SqlExecutionEngine(engine);
        SqlExecutionResult sqlResult = sqlEngine.execute(
            "CREATE TABLE testng_simple (id INTEGER, name VARCHAR(50))");
        Assert.assertTrue(sqlResult.isSuccess(), "CREATE TABLE should succeed");
        
        sqlResult = sqlEngine.execute("INSERT INTO testng_simple (id, name) VALUES (1, 'TestNG Works')");
        Assert.assertTrue(sqlResult.isSuccess(), "INSERT should succeed");
        
        sqlResult = sqlEngine.execute("SELECT * FROM testng_simple");
        Assert.assertTrue(sqlResult.isSuccess(), "SELECT should succeed");
        Assert.assertEquals(sqlResult.getRows().size(), 1, "Should return 1 row");
        Assert.assertEquals(sqlResult.getRows().get(0).getValue(1), "TestNG Works", "Name should match");
    }
    
    @Test
    @MemGres(transactional = true)
    public void testTransactionalTestNGIntegration() throws Exception {
        ITestResult result = Reporter.getCurrentTestResult();
        MemGresEngine engine = (MemGresEngine) result.getAttribute("memgres.engine");
        Assert.assertNotNull(engine, "MemGres engine should be available");
        
        SqlExecutionEngine sqlEngine = new SqlExecutionEngine(engine);
        SqlExecutionResult sqlResult = sqlEngine.execute(
            "CREATE TABLE testng_transactional (id INTEGER, value VARCHAR(50))");
        Assert.assertTrue(sqlResult.isSuccess(), "CREATE TABLE should succeed");
        
        sqlResult = sqlEngine.execute("INSERT INTO testng_transactional (id, value) VALUES (1, 'will rollback')");
        Assert.assertTrue(sqlResult.isSuccess(), "INSERT should succeed");
        
        // Verify data exists during test
        sqlResult = sqlEngine.execute("SELECT COUNT(*) FROM testng_transactional");
        Assert.assertTrue(sqlResult.isSuccess(), "SELECT COUNT should succeed");
        // Data should be rolled back after test automatically
    }
}