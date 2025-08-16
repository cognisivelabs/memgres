package com.memgres.sql;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionResult;
import com.memgres.storage.MaterializedView;
import com.memgres.types.Row;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for H2-compatible materialized view functionality in MemGres.
 */
class MaterializedViewIntegrationTest {
    
    private MemGresEngine engine;
    private SqlExecutionEngine sqlEngine;
    
    @BeforeEach
    void setUp() throws Exception {
        engine = new MemGresEngine();
        engine.initialize();
        sqlEngine = new SqlExecutionEngine(engine);
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (engine != null) {
            engine.shutdown();
        }
    }
    
    @Test
    void testCreateMaterializedView() throws Exception {
        // Create a test table
        sqlEngine.execute("CREATE TABLE employees (id INTEGER, name VARCHAR(100), department VARCHAR(50), salary DECIMAL)");
        
        // Insert test data
        sqlEngine.execute("INSERT INTO employees VALUES (1, 'Alice', 'Engineering', 75000)");
        sqlEngine.execute("INSERT INTO employees VALUES (2, 'Bob', 'Sales', 65000)");
        sqlEngine.execute("INSERT INTO employees VALUES (3, 'Carol', 'Engineering', 80000)");
        
        // Create a materialized view
        SqlExecutionResult result = sqlEngine.execute(
            "CREATE MATERIALIZED VIEW engineering_employees AS " +
            "SELECT id, name, salary FROM employees WHERE department = 'Engineering'"
        );
        
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("created"));
        
        // Verify materialized view exists in schema
        assertTrue(engine.getSchema("public").materializedViewExists("engineering_employees"));
        
        MaterializedView materializedView = engine.getSchema("public").getMaterializedView("engineering_employees");
        assertNotNull(materializedView);
        assertEquals("engineering_employees", materializedView.getName());
        assertFalse(materializedView.hasData()); // Not refreshed yet
    }
    
    @Test
    void testCreateMaterializedViewIfNotExists() throws Exception {
        // Create a test table
        sqlEngine.execute("CREATE TABLE test_table (id INTEGER, name VARCHAR(100))");
        
        // Create a materialized view
        sqlEngine.execute("CREATE MATERIALIZED VIEW test_view AS SELECT * FROM test_table");
        
        // Try to create the same materialized view again without IF NOT EXISTS - should fail
        assertThrows(Exception.class, () -> {
            sqlEngine.execute("CREATE MATERIALIZED VIEW test_view AS SELECT * FROM test_table");
        });
        
        // Try to create the same materialized view again with IF NOT EXISTS - should succeed
        SqlExecutionResult result = sqlEngine.execute(
            "CREATE MATERIALIZED VIEW IF NOT EXISTS test_view AS SELECT * FROM test_table"
        );
        
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        assertTrue(result.isSuccess());
    }
    
    @Test
    void testCreateOrReplaceMaterializedView() throws Exception {
        // Create a test table
        sqlEngine.execute("CREATE TABLE test_table (id INTEGER, name VARCHAR(100))");
        
        // Create a materialized view
        sqlEngine.execute("CREATE MATERIALIZED VIEW test_view AS SELECT id FROM test_table");
        
        // Replace with a different query
        SqlExecutionResult result = sqlEngine.execute(
            "CREATE OR REPLACE MATERIALIZED VIEW test_view AS SELECT id, name FROM test_table"
        );
        
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        assertTrue(result.isSuccess());
        
        // Verify the materialized view was replaced
        MaterializedView materializedView = engine.getSchema("public").getMaterializedView("test_view");
        assertNotNull(materializedView);
        assertEquals("test_view", materializedView.getName());
    }
    
    @Test
    void testDropMaterializedView() throws Exception {
        // Create a test table
        sqlEngine.execute("CREATE TABLE test_table (id INTEGER, name VARCHAR(100))");
        
        // Create a materialized view
        sqlEngine.execute("CREATE MATERIALIZED VIEW test_view AS SELECT * FROM test_table");
        
        // Verify it exists
        assertTrue(engine.getSchema("public").materializedViewExists("test_view"));
        
        // Drop the materialized view
        SqlExecutionResult result = sqlEngine.execute("DROP MATERIALIZED VIEW test_view");
        
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("dropped"));
        
        // Verify materialized view no longer exists
        assertFalse(engine.getSchema("public").materializedViewExists("test_view"));
    }
    
    @Test
    void testDropMaterializedViewIfExists() throws Exception {
        // Try to drop a non-existent materialized view without IF EXISTS - should fail
        assertThrows(Exception.class, () -> {
            sqlEngine.execute("DROP MATERIALIZED VIEW non_existent_view");
        });
        
        // Try to drop a non-existent materialized view with IF EXISTS - should succeed
        SqlExecutionResult result = sqlEngine.execute("DROP MATERIALIZED VIEW IF EXISTS non_existent_view");
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        assertTrue(result.isSuccess());
    }
    
    @Test
    void testRefreshMaterializedView() throws Exception {
        // Create a test table
        sqlEngine.execute("CREATE TABLE products (id INTEGER, name VARCHAR(100), price DECIMAL)");
        
        // Insert initial data
        sqlEngine.execute("INSERT INTO products VALUES (1, 'Widget A', 10.00)");
        sqlEngine.execute("INSERT INTO products VALUES (2, 'Widget B', 15.00)");
        
        // Create a materialized view
        sqlEngine.execute("CREATE MATERIALIZED VIEW expensive_products AS " +
                         "SELECT id, name, price FROM products WHERE price > 12.00");
        
        // Initially, materialized view has no data
        MaterializedView materializedView = engine.getSchema("public").getMaterializedView("expensive_products");
        assertFalse(materializedView.hasData());
        assertEquals(0, materializedView.getRowCount());
        
        // Refresh the materialized view
        SqlExecutionResult result = sqlEngine.execute("REFRESH MATERIALIZED VIEW expensive_products");
        
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("refreshed"));
        assertTrue(result.getMessage().contains("1 rows")); // Should have 1 row (Widget B)
        
        // Verify data was loaded
        assertTrue(materializedView.hasData());
        assertEquals(1, materializedView.getRowCount());
        assertNotNull(materializedView.getLastRefreshTime());
        
        // Add more data to the base table
        sqlEngine.execute("INSERT INTO products VALUES (3, 'Widget C', 20.00)");
        
        // Materialized view should still have old data
        assertEquals(1, materializedView.getRowCount());
        
        // Refresh again
        result = sqlEngine.execute("REFRESH MATERIALIZED VIEW expensive_products");
        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("2 rows")); // Should now have 2 rows
        
        // Verify updated data
        assertEquals(2, materializedView.getRowCount());
    }
    
    @Test
    void testRefreshNonExistentMaterializedView() throws Exception {
        // Try to refresh a non-existent materialized view - should fail
        assertThrows(Exception.class, () -> {
            sqlEngine.execute("REFRESH MATERIALIZED VIEW non_existent_view");
        });
    }
    
    @Test
    void testMaterializedViewWithExplicitColumns() throws Exception {
        // Create a test table
        sqlEngine.execute("CREATE TABLE test_table (id INTEGER, full_name VARCHAR(100))");
        
        // Create a materialized view with explicit column names
        SqlExecutionResult result = sqlEngine.execute(
            "CREATE MATERIALIZED VIEW test_view (employee_id, employee_name) AS " +
            "SELECT id, full_name FROM test_table"
        );
        
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        assertTrue(result.isSuccess());
        
        // Verify the materialized view was created with explicit columns
        MaterializedView materializedView = engine.getSchema("public").getMaterializedView("test_view");
        assertNotNull(materializedView);
        assertTrue(materializedView.hasExplicitColumns());
        assertEquals(2, materializedView.getColumnNames().size());
        assertEquals("employee_id", materializedView.getColumnNames().get(0));
        assertEquals("employee_name", materializedView.getColumnNames().get(1));
    }
    
    @Test
    void testMaterializedViewConcurrency() throws Exception {
        // Create a test table
        sqlEngine.execute("CREATE TABLE test_table (id INTEGER, data VARCHAR(100))");
        sqlEngine.execute("INSERT INTO test_table VALUES (1, 'test data')");
        
        // Create a materialized view
        sqlEngine.execute("CREATE MATERIALIZED VIEW test_view AS SELECT * FROM test_table");
        
        MaterializedView materializedView = engine.getSchema("public").getMaterializedView("test_view");
        
        // Test concurrent refresh operations
        Thread refreshThread1 = new Thread(() -> {
            try {
                sqlEngine.execute("REFRESH MATERIALIZED VIEW test_view");
            } catch (Exception e) {
                fail("Refresh should not fail: " + e.getMessage());
            }
        });
        
        Thread refreshThread2 = new Thread(() -> {
            try {
                sqlEngine.execute("REFRESH MATERIALIZED VIEW test_view");
            } catch (Exception e) {
                fail("Refresh should not fail: " + e.getMessage());
            }
        });
        
        refreshThread1.start();
        refreshThread2.start();
        
        refreshThread1.join();
        refreshThread2.join();
        
        // Both refreshes should complete successfully
        assertTrue(materializedView.hasData());
        assertEquals(1, materializedView.getRowCount());
    }
}