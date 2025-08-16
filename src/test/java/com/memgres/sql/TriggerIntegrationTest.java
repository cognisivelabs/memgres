package com.memgres.sql;

import com.memgres.api.Trigger;
import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionResult;
import com.memgres.triggers.TestTrigger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for H2-compatible trigger functionality in MemGres.
 */
class TriggerIntegrationTest {
    
    private MemGresEngine engine;
    private SqlExecutionEngine sqlEngine;
    
    @BeforeEach
    void setUp() throws Exception {
        engine = new MemGresEngine();
        engine.initialize();
        sqlEngine = new SqlExecutionEngine(engine);
        
        // Clear any previous trigger events
        TestTrigger.clearEvents();
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (engine != null) {
            engine.shutdown();
        }
        TestTrigger.clearEvents();
    }
    
    @Test
    void testCreateAndDropTrigger() throws Exception {
        // Create a test table
        sqlEngine.execute("CREATE TABLE test_table (id INTEGER, name VARCHAR(100))");
        
        // Create a trigger
        SqlExecutionResult result = sqlEngine.execute(
            "CREATE TRIGGER test_trigger BEFORE INSERT ON test_table " +
            "CALL 'com.memgres.triggers.TestTrigger'"
        );
        
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        assertTrue(result.isSuccess());
        assertEquals("Trigger test_trigger created", result.getMessage());
        
        // Verify trigger exists
        assertTrue(engine.getTriggerManager().triggerExists("public", "test_trigger"));
        
        // Drop the trigger
        SqlExecutionResult dropResult = sqlEngine.execute("DROP TRIGGER test_trigger");
        assertEquals(SqlExecutionResult.ResultType.DDL, dropResult.getType());
        assertTrue(dropResult.isSuccess());
        assertEquals("Trigger test_trigger dropped", dropResult.getMessage());
        
        // Verify trigger no longer exists
        assertFalse(engine.getTriggerManager().triggerExists("public", "test_trigger"));
    }
    
    @Test
    void testCreateTriggerIfNotExists() throws Exception {
        // Create a test table
        sqlEngine.execute("CREATE TABLE test_table (id INTEGER, name VARCHAR(100))");
        
        // Create a trigger
        sqlEngine.execute(
            "CREATE TRIGGER test_trigger BEFORE INSERT ON test_table " +
            "CALL 'com.memgres.triggers.TestTrigger'"
        );
        
        // Try to create the same trigger again without IF NOT EXISTS - should fail
        assertThrows(Exception.class, () -> {
            sqlEngine.execute(
                "CREATE TRIGGER test_trigger BEFORE INSERT ON test_table " +
                "CALL 'com.memgres.triggers.TestTrigger'"
            );
        });
        
        // Try to create the same trigger again with IF NOT EXISTS - should succeed
        SqlExecutionResult result = sqlEngine.execute(
            "CREATE TRIGGER IF NOT EXISTS test_trigger BEFORE INSERT ON test_table " +
            "CALL 'com.memgres.triggers.TestTrigger'"
        );
        
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        assertTrue(result.isSuccess());
    }
    
    @Test
    void testDropTriggerIfExists() throws Exception {
        // Try to drop a non-existent trigger without IF EXISTS - should fail
        assertThrows(Exception.class, () -> {
            sqlEngine.execute("DROP TRIGGER non_existent_trigger");
        });
        
        // Try to drop a non-existent trigger with IF EXISTS - should succeed
        SqlExecutionResult result = sqlEngine.execute("DROP TRIGGER IF EXISTS non_existent_trigger");
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        assertTrue(result.isSuccess());
    }
    
    @Test
    void testBeforeInsertTrigger() throws Exception {
        // Create a test table
        sqlEngine.execute("CREATE TABLE test_table (id INTEGER, name VARCHAR(100))");
        
        // Create a BEFORE INSERT trigger
        sqlEngine.execute(
            "CREATE TRIGGER before_insert_trigger BEFORE INSERT ON test_table " +
            "CALL 'com.memgres.triggers.TestTrigger'"
        );
        
        // Insert a row - this should fire the trigger
        sqlEngine.execute("INSERT INTO test_table VALUES (1, 'Test Name')");
        
        // Verify the trigger was fired
        List<TestTrigger.TriggerEvent> events = TestTrigger.getEvents();
        assertEquals(1, events.size());
        
        TestTrigger.TriggerEvent event = events.get(0);
        assertEquals("before_insert_trigger", event.triggerName);
        assertEquals("test_table", event.tableName);
        assertTrue(event.before);
        assertEquals(Trigger.INSERT, event.type);
        assertNull(event.oldRow); // No old row for INSERT
        assertNotNull(event.newRow); // Should have new row data
        assertEquals(1, ((Number) event.newRow[0]).intValue());
        assertEquals("Test Name", event.newRow[1]);
    }
    
    @Test
    void testAfterInsertTrigger() throws Exception {
        // Create a test table
        sqlEngine.execute("CREATE TABLE test_table (id INTEGER, name VARCHAR(100))");
        
        // Create an AFTER INSERT trigger
        sqlEngine.execute(
            "CREATE TRIGGER after_insert_trigger AFTER INSERT ON test_table " +
            "CALL 'com.memgres.triggers.TestTrigger'"
        );
        
        // Insert a row - this should fire the trigger
        sqlEngine.execute("INSERT INTO test_table VALUES (2, 'Another Test')");
        
        // Verify the trigger was fired
        List<TestTrigger.TriggerEvent> events = TestTrigger.getEvents();
        assertEquals(1, events.size());
        
        TestTrigger.TriggerEvent event = events.get(0);
        assertEquals("after_insert_trigger", event.triggerName);
        assertEquals("test_table", event.tableName);
        assertFalse(event.before);
        assertEquals(Trigger.INSERT, event.type);
        assertNull(event.oldRow); // No old row for INSERT
        assertNotNull(event.newRow);
        assertEquals(2, ((Number) event.newRow[0]).intValue());
        assertEquals("Another Test", event.newRow[1]);
    }
    
    @Test
    void testBeforeUpdateTrigger() throws Exception {
        // Create a test table and insert initial data
        sqlEngine.execute("CREATE TABLE test_table (id INTEGER, name VARCHAR(100))");
        sqlEngine.execute("INSERT INTO test_table VALUES (1, 'Original Name')");
        
        // Clear any insert trigger events
        TestTrigger.clearEvents();
        
        // Create a BEFORE UPDATE trigger
        sqlEngine.execute(
            "CREATE TRIGGER before_update_trigger BEFORE UPDATE ON test_table " +
            "CALL 'com.memgres.triggers.TestTrigger'"
        );
        
        // Update the row - this should fire the trigger
        sqlEngine.execute("UPDATE test_table SET name = 'Updated Name' WHERE id = 1");
        
        // Verify the trigger was fired
        List<TestTrigger.TriggerEvent> events = TestTrigger.getEvents();
        assertEquals(1, events.size());
        
        TestTrigger.TriggerEvent event = events.get(0);
        assertEquals("before_update_trigger", event.triggerName);
        assertEquals("test_table", event.tableName);
        assertTrue(event.before);
        assertEquals(Trigger.UPDATE, event.type);
        assertNotNull(event.oldRow); // Should have old row data
        assertNotNull(event.newRow); // Should have new row data
        assertEquals("Original Name", event.oldRow[1]);
        assertEquals("Updated Name", event.newRow[1]);
    }
    
    @Test
    void testBeforeDeleteTrigger() throws Exception {
        // Create a test table and insert initial data
        sqlEngine.execute("CREATE TABLE test_table (id INTEGER, name VARCHAR(100))");
        sqlEngine.execute("INSERT INTO test_table VALUES (1, 'To Be Deleted')");
        
        // Clear any insert trigger events
        TestTrigger.clearEvents();
        
        // Create a BEFORE DELETE trigger
        sqlEngine.execute(
            "CREATE TRIGGER before_delete_trigger BEFORE DELETE ON test_table " +
            "CALL 'com.memgres.triggers.TestTrigger'"
        );
        
        // Delete the row - this should fire the trigger
        sqlEngine.execute("DELETE FROM test_table WHERE id = 1");
        
        // Verify the trigger was fired
        List<TestTrigger.TriggerEvent> events = TestTrigger.getEvents();
        assertEquals(1, events.size());
        
        TestTrigger.TriggerEvent event = events.get(0);
        assertEquals("before_delete_trigger", event.triggerName);
        assertEquals("test_table", event.tableName);
        assertTrue(event.before);
        assertEquals(Trigger.DELETE, event.type);
        assertNotNull(event.oldRow); // Should have old row data
        assertNull(event.newRow); // No new row for DELETE
        assertEquals(1, ((Number) event.oldRow[0]).intValue());
        assertEquals("To Be Deleted", event.oldRow[1]);
    }
    
    @Test
    void testMultipleTriggers() throws Exception {
        // Create a test table
        sqlEngine.execute("CREATE TABLE test_table (id INTEGER, name VARCHAR(100))");
        
        // Create multiple triggers
        sqlEngine.execute(
            "CREATE TRIGGER before_insert_trigger BEFORE INSERT ON test_table " +
            "CALL 'com.memgres.triggers.TestTrigger'"
        );
        sqlEngine.execute(
            "CREATE TRIGGER after_insert_trigger AFTER INSERT ON test_table " +
            "CALL 'com.memgres.triggers.TestTrigger'"
        );
        
        // Insert a row - both triggers should fire
        sqlEngine.execute("INSERT INTO test_table VALUES (1, 'Test Multiple')");
        
        // Verify both triggers were fired
        List<TestTrigger.TriggerEvent> events = TestTrigger.getEvents();
        assertEquals(2, events.size());
        
        // Check BEFORE trigger (should fire first)
        TestTrigger.TriggerEvent beforeEvent = events.get(0);
        assertEquals("before_insert_trigger", beforeEvent.triggerName);
        assertTrue(beforeEvent.before);
        assertEquals(Trigger.INSERT, beforeEvent.type);
        
        // Check AFTER trigger (should fire second)
        TestTrigger.TriggerEvent afterEvent = events.get(1);
        assertEquals("after_insert_trigger", afterEvent.triggerName);
        assertFalse(afterEvent.before);
        assertEquals(Trigger.INSERT, afterEvent.type);
    }
    
    @Test
    void testTriggerWithInvalidClass() throws Exception {
        // Create a test table
        sqlEngine.execute("CREATE TABLE test_table (id INTEGER, name VARCHAR(100))");
        
        // Try to create a trigger with a non-existent class - should fail
        assertThrows(Exception.class, () -> {
            sqlEngine.execute(
                "CREATE TRIGGER invalid_trigger BEFORE INSERT ON test_table " +
                "CALL 'com.memgres.NonExistentTriggerClass'"
            );
        });
    }
    
    @Test
    void testTriggerForEachRowVsStatement() throws Exception {
        // Create a test table
        sqlEngine.execute("CREATE TABLE test_table (id INTEGER, name VARCHAR(100))");
        
        // Test FOR EACH ROW (default)
        sqlEngine.execute(
            "CREATE TRIGGER row_trigger BEFORE INSERT ON test_table FOR EACH ROW " +
            "CALL 'com.memgres.triggers.TestTrigger'"
        );
        
        // Test FOR EACH STATEMENT
        sqlEngine.execute(
            "CREATE TRIGGER stmt_trigger AFTER INSERT ON test_table FOR EACH STATEMENT " +
            "CALL 'com.memgres.triggers.TestTrigger'"
        );
        
        // Both should exist
        assertTrue(engine.getTriggerManager().triggerExists("public", "row_trigger"));
        assertTrue(engine.getTriggerManager().triggerExists("public", "stmt_trigger"));
    }
    
    @Test 
    void testAfterUpdateTrigger() throws Exception {
        // Create a test table and insert initial data
        sqlEngine.execute("CREATE TABLE test_table (id INTEGER, name VARCHAR(100))");
        sqlEngine.execute("INSERT INTO test_table VALUES (1, 'Original Name')");
        
        // Clear any insert trigger events
        TestTrigger.clearEvents();
        
        // Create an AFTER UPDATE trigger
        sqlEngine.execute(
            "CREATE TRIGGER after_update_trigger AFTER UPDATE ON test_table " +
            "CALL 'com.memgres.triggers.TestTrigger'"
        );
        
        // Update the row - this should fire the trigger
        sqlEngine.execute("UPDATE test_table SET name = 'Updated Name' WHERE id = 1");
        
        // Verify the trigger was fired
        List<TestTrigger.TriggerEvent> events = TestTrigger.getEvents();
        assertEquals(1, events.size());
        
        TestTrigger.TriggerEvent event = events.get(0);
        assertEquals("after_update_trigger", event.triggerName);
        assertEquals("test_table", event.tableName);
        assertFalse(event.before); // AFTER trigger
        assertEquals(Trigger.UPDATE, event.type);
        assertNotNull(event.oldRow);
        assertNotNull(event.newRow);
        assertEquals("Original Name", event.oldRow[1]);
        assertEquals("Updated Name", event.newRow[1]);
    }
    
    @Test
    void testAfterDeleteTrigger() throws Exception {
        // Create a test table and insert initial data
        sqlEngine.execute("CREATE TABLE test_table (id INTEGER, name VARCHAR(100))");
        sqlEngine.execute("INSERT INTO test_table VALUES (1, 'To Be Deleted')");
        
        // Clear any insert trigger events
        TestTrigger.clearEvents();
        
        // Create an AFTER DELETE trigger
        sqlEngine.execute(
            "CREATE TRIGGER after_delete_trigger AFTER DELETE ON test_table " +
            "CALL 'com.memgres.triggers.TestTrigger'"
        );
        
        // Delete the row - this should fire the trigger
        sqlEngine.execute("DELETE FROM test_table WHERE id = 1");
        
        // Verify the trigger was fired
        List<TestTrigger.TriggerEvent> events = TestTrigger.getEvents();
        assertEquals(1, events.size());
        
        TestTrigger.TriggerEvent event = events.get(0);
        assertEquals("after_delete_trigger", event.triggerName);
        assertEquals("test_table", event.tableName);
        assertFalse(event.before); // AFTER trigger
        assertEquals(Trigger.DELETE, event.type);
        assertNotNull(event.oldRow);
        assertNull(event.newRow); // No new row for DELETE
        assertEquals(1, ((Number) event.oldRow[0]).intValue());
        assertEquals("To Be Deleted", event.oldRow[1]);
    }
    
    @Test
    void testTriggersOnDifferentTables() throws Exception {
        // Create two test tables
        sqlEngine.execute("CREATE TABLE users (id INTEGER, name VARCHAR(100))");
        sqlEngine.execute("CREATE TABLE products (id INTEGER, name VARCHAR(100))");
        
        // Create triggers on different tables
        sqlEngine.execute(
            "CREATE TRIGGER user_trigger BEFORE INSERT ON users " +
            "CALL 'com.memgres.triggers.TestTrigger'"
        );
        sqlEngine.execute(
            "CREATE TRIGGER product_trigger BEFORE INSERT ON products " +
            "CALL 'com.memgres.triggers.TestTrigger'"
        );
        
        // Insert into both tables
        sqlEngine.execute("INSERT INTO users VALUES (1, 'John Doe')");
        sqlEngine.execute("INSERT INTO products VALUES (1, 'Widget')");
        
        // Verify both triggers fired
        List<TestTrigger.TriggerEvent> events = TestTrigger.getEvents();
        assertEquals(2, events.size());
        
        // Check user trigger
        TestTrigger.TriggerEvent userEvent = events.stream()
            .filter(e -> e.triggerName.equals("user_trigger"))
            .findFirst().orElse(null);
        assertNotNull(userEvent);
        assertEquals("users", userEvent.tableName);
        assertEquals("John Doe", userEvent.newRow[1]);
        
        // Check product trigger  
        TestTrigger.TriggerEvent productEvent = events.stream()
            .filter(e -> e.triggerName.equals("product_trigger"))
            .findFirst().orElse(null);
        assertNotNull(productEvent);
        assertEquals("products", productEvent.tableName);
        assertEquals("Widget", productEvent.newRow[1]);
    }
}