package com.memgres.transaction;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.testing.MemGresTestConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Savepoint;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for Savepoint functionality.
 * Tests nested transaction rollback points and JDBC savepoint compliance.
 */
public class SavepointsTest {
    
    private MemGresEngine engine;
    private SqlExecutionEngine sqlEngine;
    private MemGresTestConnection connection;
    
    @BeforeEach
    void setUp() throws Exception {
        engine = new MemGresEngine();
        engine.initialize();
        sqlEngine = new SqlExecutionEngine(engine);
        connection = new MemGresTestConnection(engine, sqlEngine);
        
        // Create test schema and table
        engine.createSchema("public");
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE test_savepoints (" +
                "id INTEGER PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "amount INTEGER" +
                ")");
        }
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (connection != null) {
            connection.close();
        }
        if (engine != null) {
            engine.shutdown();
        }
    }
    
    @Test
    void testSavepointInAutoCommitMode() throws Exception {
        // Should throw exception when trying to create savepoint in auto-commit mode
        assertTrue(connection.getAutoCommit());
        
        SQLException ex = assertThrows(SQLException.class, () -> connection.setSavepoint());
        assertTrue(ex.getMessage().contains("auto-commit"));
        
        SQLException ex2 = assertThrows(SQLException.class, () -> connection.setSavepoint("test"));
        assertTrue(ex2.getMessage().contains("auto-commit"));
    }
    
    @Test
    void testCreateUnnamedSavepoint() throws Exception {
        connection.setAutoCommit(false);
        
        try (Statement stmt = connection.createStatement()) {
            // Insert initial data
            stmt.executeUpdate("INSERT INTO test_savepoints (id, name, amount) VALUES (1, 'Initial', 100)");
            
            // Create unnamed savepoint
            Savepoint sp = connection.setSavepoint();
            assertNotNull(sp);
            
            // Should be able to get savepoint ID for unnamed savepoint
            int spId = sp.getSavepointId();
            assertTrue(spId > 0);
            
            // Should throw exception when trying to get name for unnamed savepoint
            SQLException ex = assertThrows(SQLException.class, sp::getSavepointName);
            assertTrue(ex.getMessage().contains("unnamed"));
            
            connection.commit();
        }
    }
    
    @Test
    void testCreateNamedSavepoint() throws Exception {
        connection.setAutoCommit(false);
        
        try (Statement stmt = connection.createStatement()) {
            // Insert initial data
            stmt.executeUpdate("INSERT INTO test_savepoints (id, name, amount) VALUES (1, 'Initial', 100)");
            
            // Create named savepoint
            Savepoint sp = connection.setSavepoint("test_savepoint");
            assertNotNull(sp);
            
            // Should be able to get savepoint name
            assertEquals("test_savepoint", sp.getSavepointName());
            
            // Should throw exception when trying to get ID for named savepoint
            SQLException ex = assertThrows(SQLException.class, sp::getSavepointId);
            assertTrue(ex.getMessage().contains("named"));
            
            connection.commit();
        }
    }
    
    @Test
    void testDuplicateNamedSavepoint() throws Exception {
        connection.setAutoCommit(false);
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("INSERT INTO test_savepoints (id, name, amount) VALUES (1, 'Initial', 100)");
            
            // Create first savepoint
            Savepoint sp1 = connection.setSavepoint("duplicate_name");
            assertNotNull(sp1);
            
            // Should throw exception for duplicate name
            SQLException ex = assertThrows(SQLException.class, 
                () -> connection.setSavepoint("duplicate_name"));
            assertTrue(ex.getMessage().contains("already exists"));
            
            connection.commit();
        }
    }
    
    @Test
    void testRollbackToUnnamedSavepoint() throws Exception {
        connection.setAutoCommit(false);
        
        try (Statement stmt = connection.createStatement()) {
            // Insert initial data
            stmt.executeUpdate("INSERT INTO test_savepoints (id, name, amount) VALUES (1, 'Initial', 100)");
            
            // Create savepoint
            Savepoint sp = connection.setSavepoint();
            
            // Insert more data after savepoint
            stmt.executeUpdate("INSERT INTO test_savepoints (id, name, amount) VALUES (2, 'After SP', 200)");
            stmt.executeUpdate("UPDATE test_savepoints SET amount = 150 WHERE id = 1");
            
            // Verify data exists
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM test_savepoints");
            assertTrue(rs.next());
            assertEquals(2, rs.getInt(1));
            
            rs = stmt.executeQuery("SELECT amount FROM test_savepoints WHERE id = 1");
            assertTrue(rs.next());
            assertEquals(150, rs.getInt(1));
            
            // Rollback to savepoint
            connection.rollback(sp);
            
            // Verify rollback worked - should only have initial data
            rs = stmt.executeQuery("SELECT COUNT(*) FROM test_savepoints");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1)); // Only initial record should remain
            
            rs = stmt.executeQuery("SELECT amount FROM test_savepoints WHERE id = 1");
            assertTrue(rs.next());
            assertEquals(100, rs.getInt(1)); // Original amount should be restored
            
            connection.commit();
        }
    }
    
    @Test
    void testRollbackToNamedSavepoint() throws Exception {
        connection.setAutoCommit(false);
        
        try (Statement stmt = connection.createStatement()) {
            // Insert initial data
            stmt.executeUpdate("INSERT INTO test_savepoints (id, name, amount) VALUES (1, 'Initial', 100)");
            
            // Create named savepoint
            Savepoint sp = connection.setSavepoint("rollback_test");
            
            // Insert and modify data after savepoint
            stmt.executeUpdate("INSERT INTO test_savepoints (id, name, amount) VALUES (2, 'After SP', 200)");
            stmt.executeUpdate("INSERT INTO test_savepoints (id, name, amount) VALUES (3, 'Also After', 300)");
            
            // Verify data
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM test_savepoints");
            assertTrue(rs.next());
            assertEquals(3, rs.getInt(1));
            
            // Rollback to named savepoint
            connection.rollback(sp);
            
            // Verify rollback
            rs = stmt.executeQuery("SELECT COUNT(*) FROM test_savepoints");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1)); // Only initial record
            
            connection.commit();
        }
    }
    
    @Test
    void testMultipleSavepoints() throws Exception {
        connection.setAutoCommit(false);
        
        try (Statement stmt = connection.createStatement()) {
            // Insert initial data
            stmt.executeUpdate("INSERT INTO test_savepoints (id, name, amount) VALUES (1, 'Initial', 100)");
            
            // Create first savepoint
            Savepoint sp1 = connection.setSavepoint("first");
            stmt.executeUpdate("INSERT INTO test_savepoints (id, name, amount) VALUES (2, 'After First', 200)");
            
            // Create second savepoint
            Savepoint sp2 = connection.setSavepoint("second");
            stmt.executeUpdate("INSERT INTO test_savepoints (id, name, amount) VALUES (3, 'After Second', 300)");
            
            // Verify all data exists
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM test_savepoints");
            assertTrue(rs.next());
            assertEquals(3, rs.getInt(1));
            
            // Rollback to first savepoint (should invalidate second savepoint)
            connection.rollback(sp1);
            
            // Verify rollback to first savepoint
            rs = stmt.executeQuery("SELECT COUNT(*) FROM test_savepoints");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
            
            // Second savepoint should be invalidated
            SQLException ex = assertThrows(SQLException.class, () -> connection.rollback(sp2));
            assertTrue(ex.getMessage().contains("released") || ex.getMessage().contains("invalid"));
            
            connection.commit();
        }
    }
    
    @Test
    void testReleaseSavepoint() throws Exception {
        connection.setAutoCommit(false);
        
        try (Statement stmt = connection.createStatement()) {
            // Insert initial data
            stmt.executeUpdate("INSERT INTO test_savepoints (id, name, amount) VALUES (1, 'Initial', 100)");
            
            // Create savepoint
            Savepoint sp = connection.setSavepoint("release_test");
            stmt.executeUpdate("INSERT INTO test_savepoints (id, name, amount) VALUES (2, 'After SP', 200)");
            
            // Release savepoint
            connection.releaseSavepoint(sp);
            
            // Should not be able to rollback to released savepoint
            SQLException ex = assertThrows(SQLException.class, () -> connection.rollback(sp));
            assertTrue(ex.getMessage().contains("released"));
            
            // Data should still be there (release doesn't rollback)
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM test_savepoints");
            assertTrue(rs.next());
            assertEquals(2, rs.getInt(1));
            
            connection.commit();
        }
    }
    
    @Test
    void testSavepointWithoutTransaction() throws Exception {
        connection.setAutoCommit(false);
        
        // Commit any existing transaction
        connection.commit();
        
        // Should throw exception when no active transaction
        SQLException ex = assertThrows(SQLException.class, () -> connection.setSavepoint());
        assertTrue(ex.getMessage().contains("No active transaction"));
        
        ex = assertThrows(SQLException.class, () -> connection.setSavepoint("test"));
        assertTrue(ex.getMessage().contains("No active transaction"));
    }
    
    @Test
    void testInvalidSavepointOperations() throws Exception {
        connection.setAutoCommit(false);
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("INSERT INTO test_savepoints (id, name, amount) VALUES (1, 'Initial', 100)");
            
            // Test null savepoint
            SQLException ex = assertThrows(SQLException.class, () -> connection.rollback((Savepoint) null));
            assertTrue(ex.getMessage().contains("null"));
            
            ex = assertThrows(SQLException.class, () -> connection.releaseSavepoint(null));
            assertTrue(ex.getMessage().contains("null"));
            
            // Test empty savepoint name
            ex = assertThrows(SQLException.class, () -> connection.setSavepoint(""));
            assertTrue(ex.getMessage().contains("empty"));
            
            ex = assertThrows(SQLException.class, () -> connection.setSavepoint("   "));
            assertTrue(ex.getMessage().contains("empty"));
            
            connection.commit();
        }
    }
    
    @Test
    void testSavepointAfterCommit() throws Exception {
        connection.setAutoCommit(false);
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("INSERT INTO test_savepoints (id, name, amount) VALUES (1, 'Initial', 100)");
            
            Savepoint sp = connection.setSavepoint("before_commit");
            stmt.executeUpdate("INSERT INTO test_savepoints (id, name, amount) VALUES (2, 'Before Commit', 200)");
            
            connection.commit();
            
            // After commit, savepoint should be invalid
            SQLException ex = assertThrows(SQLException.class, () -> connection.rollback(sp));
            assertTrue(ex.getMessage().contains("active"));
        }
    }
    
    @Test
    void testNestedSavepointScenario() throws Exception {
        connection.setAutoCommit(false);
        
        try (Statement stmt = connection.createStatement()) {
            // Step 1: Insert initial data
            stmt.executeUpdate("INSERT INTO test_savepoints (id, name, amount) VALUES (1, 'Base', 100)");
            
            // Step 2: Create outer savepoint
            Savepoint outerSp = connection.setSavepoint("outer");
            stmt.executeUpdate("INSERT INTO test_savepoints (id, name, amount) VALUES (2, 'Outer', 200)");
            stmt.executeUpdate("UPDATE test_savepoints SET amount = 110 WHERE id = 1");
            
            // Step 3: Create inner savepoint
            Savepoint innerSp = connection.setSavepoint("inner");
            stmt.executeUpdate("INSERT INTO test_savepoints (id, name, amount) VALUES (3, 'Inner', 300)");
            stmt.executeUpdate("UPDATE test_savepoints SET amount = 120 WHERE id = 1");
            
            // Step 4: Verify all changes
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM test_savepoints");
            assertTrue(rs.next());
            assertEquals(3, rs.getInt(1));
            
            rs = stmt.executeQuery("SELECT amount FROM test_savepoints WHERE id = 1");
            assertTrue(rs.next());
            assertEquals(120, rs.getInt(1));
            
            // Step 5: Rollback to inner savepoint (should restore to step 3)
            connection.rollback(innerSp);
            
            rs = stmt.executeQuery("SELECT COUNT(*) FROM test_savepoints");
            assertTrue(rs.next());
            assertEquals(2, rs.getInt(1)); // Records 1 and 2
            
            rs = stmt.executeQuery("SELECT amount FROM test_savepoints WHERE id = 1");
            assertTrue(rs.next());
            assertEquals(110, rs.getInt(1)); // Value from outer savepoint
            
            // Step 6: Rollback to outer savepoint (should restore to step 2)
            connection.rollback(outerSp);
            
            rs = stmt.executeQuery("SELECT COUNT(*) FROM test_savepoints");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1)); // Only initial record
            
            rs = stmt.executeQuery("SELECT amount FROM test_savepoints WHERE id = 1");
            assertTrue(rs.next());
            assertEquals(100, rs.getInt(1)); // Original amount
            
            connection.commit();
        }
    }
}