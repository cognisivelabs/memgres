package com.memgres.sql;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionResult;
import com.memgres.storage.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DROP TABLE statement execution.
 */
public class DropTableTest {
    
    private MemGresEngine engine;
    private SqlExecutionEngine sqlEngine;
    
    @BeforeEach
    void setUp() {
        engine = new MemGresEngine();
        engine.initialize();
        sqlEngine = new SqlExecutionEngine(engine);
    }
    
    @AfterEach
    void tearDown() {
        if (engine != null) {
            engine.shutdown();
        }
    }
    
    @Test
    void testDropExistingTable() throws Exception {
        // Create a table first
        String createSql = "CREATE TABLE test_table (id INTEGER, name VARCHAR)";
        SqlExecutionResult createResult = sqlEngine.execute(createSql);
        assertTrue(createResult.isSuccess());
        
        // Verify table exists
        Table table = engine.getTable("public", "test_table");
        assertNotNull(table);
        
        // Drop the table
        String dropSql = "DROP TABLE test_table";
        SqlExecutionResult dropResult = sqlEngine.execute(dropSql);
        
        // Verify drop was successful
        assertTrue(dropResult.isSuccess());
        assertEquals(SqlExecutionResult.ResultType.DDL, dropResult.getType());
        assertTrue(dropResult.getMessage().contains("dropped successfully"));
        
        // Verify table no longer exists
        Table droppedTable = engine.getTable("public", "test_table");
        assertNull(droppedTable);
    }
    
    @Test
    void testDropNonExistentTable() throws Exception {
        // Try to drop a table that doesn't exist
        String dropSql = "DROP TABLE non_existent_table";
        SqlExecutionResult dropResult = sqlEngine.execute(dropSql);
        
        // Should fail with appropriate message
        assertFalse(dropResult.isSuccess());
        assertEquals(SqlExecutionResult.ResultType.DDL, dropResult.getType());
        assertTrue(dropResult.getMessage().contains("does not exist"));
    }
    
    @Test
    void testDropTableWithData() throws Exception {
        // Create a table with data
        sqlEngine.execute("CREATE TABLE users (id INTEGER, name VARCHAR)");
        sqlEngine.execute("INSERT INTO users VALUES (1, 'Alice')");
        sqlEngine.execute("INSERT INTO users VALUES (2, 'Bob')");
        
        // Verify data exists
        SqlExecutionResult selectResult = sqlEngine.execute("SELECT * FROM users");
        assertEquals(2, selectResult.getRows().size());
        
        // Drop the table
        SqlExecutionResult dropResult = sqlEngine.execute("DROP TABLE users");
        assertTrue(dropResult.isSuccess());
        
        // Verify table is gone
        Table droppedTable = engine.getTable("public", "users");
        assertNull(droppedTable);
        
        // Try to select from dropped table - should fail
        try {
            sqlEngine.execute("SELECT * FROM users");
            fail("Should have thrown exception for non-existent table");
        } catch (Exception e) {
            // Expected
            assertTrue(e.getMessage().contains("users") || e.getMessage().contains("not exist"));
        }
    }
    
    @Test
    void testDropAndRecreateTable() throws Exception {
        // Create a table
        sqlEngine.execute("CREATE TABLE temp_table (id INTEGER)");
        sqlEngine.execute("INSERT INTO temp_table VALUES (1)");
        
        // Drop it
        SqlExecutionResult dropResult = sqlEngine.execute("DROP TABLE temp_table");
        assertTrue(dropResult.isSuccess());
        
        // Recreate with different schema
        sqlEngine.execute("CREATE TABLE temp_table (name VARCHAR, age INTEGER)");
        
        // Verify new schema
        Table newTable = engine.getTable("public", "temp_table");
        assertNotNull(newTable);
        assertEquals(2, newTable.getColumns().size());
        assertTrue(newTable.getColumns().stream().anyMatch(col -> col.getName().equals("name")));
        assertTrue(newTable.getColumns().stream().anyMatch(col -> col.getName().equals("age")));
        
        // Old data should not exist
        SqlExecutionResult selectResult = sqlEngine.execute("SELECT * FROM temp_table");
        assertEquals(0, selectResult.getRows().size());
    }
}