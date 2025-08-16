package com.memgres.sql;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionResult;
import com.memgres.types.Row;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Demonstrates real-world trigger usage in MemGres with H2 compatibility.
 * Shows how to implement audit logging using triggers.
 */
class TriggerUsageExampleTest {
    
    private MemGresEngine engine;
    private SqlExecutionEngine sqlEngine;
    
    @BeforeEach
    void setUp() throws Exception {
        engine = new MemGresEngine();
        engine.initialize();
        sqlEngine = new SqlExecutionEngine(engine);
        
        // Clear any previous trigger events
        com.memgres.triggers.TestTrigger.clearEvents();
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (engine != null) {
            engine.shutdown();
        }
    }
    
    @Test
    void testAuditTriggerExample() throws Exception {
        // Create a users table
        sqlEngine.execute(
            "CREATE TABLE users (" +
            "id INTEGER PRIMARY KEY, " +
            "username VARCHAR(50), " +
            "email VARCHAR(100), " +
            "status VARCHAR(20)" +
            ")"
        );
        
        // Note: AuditTrigger would create the audit_log table automatically,
        // but we need to create it manually since MemGres doesn't support 
        // cross-table operations in triggers yet (Phase 2 feature)
        sqlEngine.execute(
            "CREATE TABLE audit_log (" +
            "id INTEGER PRIMARY KEY, " +
            "table_name VARCHAR(100), " +
            "operation VARCHAR(10), " +
            "old_values TEXT, " +
            "new_values TEXT, " +
            "changed_at VARCHAR(50)" +
            ")"
        );
        
        // Create audit triggers for the users table
        sqlEngine.execute(
            "CREATE TRIGGER users_insert_audit AFTER INSERT ON users " +
            "CALL 'com.memgres.triggers.TestTrigger'"
        );
        
        sqlEngine.execute(
            "CREATE TRIGGER users_update_audit AFTER UPDATE ON users " +
            "CALL 'com.memgres.triggers.TestTrigger'"
        );
        
        sqlEngine.execute(
            "CREATE TRIGGER users_delete_audit AFTER DELETE ON users " +
            "CALL 'com.memgres.triggers.TestTrigger'"
        );
        
        // Test INSERT operations
        sqlEngine.execute("INSERT INTO users VALUES (1, 'john_doe', 'john@example.com', 'active')");
        sqlEngine.execute("INSERT INTO users VALUES (2, 'jane_smith', 'jane@example.com', 'active')");
        
        // Test UPDATE operations  
        sqlEngine.execute("UPDATE users SET status = 'inactive' WHERE id = 1");
        sqlEngine.execute("UPDATE users SET email = 'john.doe@newdomain.com' WHERE id = 1");
        
        // Test DELETE operations
        sqlEngine.execute("DELETE FROM users WHERE id = 2");
        
        // Verify all users operations worked
        SqlExecutionResult usersResult = sqlEngine.execute("SELECT * FROM users");
        List<Row> users = usersResult.getRows();
        assertEquals(1, users.size()); // Only user 1 should remain
        
        Object[] userData = users.get(0).getData();
        assertEquals(1, ((Number) userData[0]).intValue());
        assertEquals("john_doe", userData[1]);
        assertEquals("john.doe@newdomain.com", userData[2]);
        assertEquals("inactive", userData[3]);
        
        // Verify triggers fired correctly
        List<com.memgres.triggers.TestTrigger.TriggerEvent> events = 
            com.memgres.triggers.TestTrigger.getEvents();
        
        // Should have 5 events: 2 INSERTs, 2 UPDATEs, 1 DELETE
        assertEquals(5, events.size());
        
        // Check INSERT events
        long insertEvents = events.stream()
            .filter(e -> e.type == com.memgres.api.Trigger.INSERT)
            .count();
        assertEquals(2, insertEvents);
        
        // Check UPDATE events
        long updateEvents = events.stream()
            .filter(e -> e.type == com.memgres.api.Trigger.UPDATE)
            .count();
        assertEquals(2, updateEvents);
        
        // Check DELETE events
        long deleteEvents = events.stream()
            .filter(e -> e.type == com.memgres.api.Trigger.DELETE)
            .count();
        assertEquals(1, deleteEvents);
        
        // All events should be AFTER triggers
        assertTrue(events.stream().allMatch(e -> !e.before));
        
        // All events should be on the users table
        assertTrue(events.stream().allMatch(e -> "users".equals(e.tableName)));
    }
    
    @Test
    void testTriggerH2Syntax() throws Exception {
        // Test H2-compatible syntax variations
        sqlEngine.execute("CREATE TABLE products (id INTEGER, name VARCHAR(100), price DECIMAL)");
        
        // Test BEFORE INSERT with FOR EACH ROW (H2 default)
        sqlEngine.execute(
            "CREATE TRIGGER product_validation BEFORE INSERT ON products FOR EACH ROW " +
            "CALL 'com.memgres.triggers.TestTrigger'"
        );
        
        // Test AFTER UPDATE with FOR EACH STATEMENT
        sqlEngine.execute(
            "CREATE TRIGGER product_update_log AFTER UPDATE ON products FOR EACH STATEMENT " +
            "CALL 'com.memgres.triggers.TestTrigger'"
        );
        
        // Test IF NOT EXISTS
        sqlEngine.execute(
            "CREATE TRIGGER IF NOT EXISTS product_validation BEFORE INSERT ON products " +
            "CALL 'com.memgres.triggers.TestTrigger'"
        );
        
        // Verify triggers exist
        assertTrue(engine.getTriggerManager().triggerExists("public", "product_validation"));
        assertTrue(engine.getTriggerManager().triggerExists("public", "product_update_log"));
        
        // Test operations to trigger events
        sqlEngine.execute("INSERT INTO products VALUES (1, 'Widget', 9.99)");
        sqlEngine.execute("UPDATE products SET price = 10.99 WHERE id = 1");
        
        // Verify triggers fired
        List<com.memgres.triggers.TestTrigger.TriggerEvent> events = 
            com.memgres.triggers.TestTrigger.getEvents();
        
        assertTrue(events.size() >= 2);
        
        // Should have at least one INSERT and one UPDATE event
        assertTrue(events.stream().anyMatch(e -> e.type == com.memgres.api.Trigger.INSERT));
        assertTrue(events.stream().anyMatch(e -> e.type == com.memgres.api.Trigger.UPDATE));
        
        // Test DROP TRIGGER
        sqlEngine.execute("DROP TRIGGER product_validation");
        assertFalse(engine.getTriggerManager().triggerExists("public", "product_validation"));
        
        // Test DROP TRIGGER IF EXISTS
        sqlEngine.execute("DROP TRIGGER IF EXISTS non_existent_trigger");
    }
}