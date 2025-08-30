package com.memgres.sql;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionResult;
import com.memgres.sql.execution.SqlExecutionException;
import com.memgres.sql.procedure.ProcedureRegistry;
import com.memgres.sql.procedure.StoredProcedure;
import com.memgres.sql.procedure.ProcedureMetadata;
import com.memgres.testing.MemGresTestConnection;
import com.memgres.testing.MemGres;
import com.memgres.testing.MemGresExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.*;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for CREATE PROCEDURE and DROP PROCEDURE DDL statements.
 * Tests the complete lifecycle of stored procedure management via SQL DDL.
 */
@ExtendWith(MemGresExtension.class)
public class ProcedureDDLTest {
    
    /**
     * Test stored procedure implementation for DDL testing.
     */
    public static class TestProcedure implements StoredProcedure {
        @Override
        public Map<String, Object> execute(Map<String, Object> parameters) throws SQLException {
            Map<String, Object> results = new HashMap<>();
            results.put("message", "Hello from DDL procedure");
            results.put("input_count", parameters.size());
            return results;
        }
        
        @Override
        public String getName() {
            return "ddl_test_proc";
        }
        
        @Override
        public ProcedureMetadata getMetadata() {
            return new ProcedureMetadata("ddl_test_proc", java.util.List.of());
        }
    }
    
    @Test
    @MemGres
    void testCreateProcedureBasic(MemGresEngine engine, SqlExecutionEngine sqlEngine) throws SQLException, SqlExecutionException {
        // Test basic CREATE PROCEDURE statement
        SqlExecutionResult result = sqlEngine.execute(
            "CREATE PROCEDURE test_ddl_proc AS 'com.memgres.sql.ProcedureDDLTest$TestProcedure'"
        );
        
        assertNotNull(result, "CREATE PROCEDURE should return a result");
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType(), "Should be DDL result type");
        assertTrue(result.isSuccess(), "CREATE PROCEDURE should succeed");
        
        // Verify procedure was registered
        ProcedureRegistry registry = engine.getProcedureRegistry();
        assertTrue(registry.exists("test_ddl_proc"), "Procedure should be registered");
        assertEquals(1, registry.getCount(), "Should have one procedure registered");
    }
    
    @Test
    @MemGres
    void testCreateProcedureWithParameters(MemGresEngine engine, SqlExecutionEngine sqlEngine) throws SQLException, SqlExecutionException {
        // Test CREATE PROCEDURE with parameter syntax (even though parameters are not fully processed yet)
        SqlExecutionResult result = sqlEngine.execute(
            "CREATE PROCEDURE complex_proc(IN p1 INTEGER, OUT p2 VARCHAR) AS 'com.memgres.sql.ProcedureDDLTest$TestProcedure'"
        );
        
        assertNotNull(result, "CREATE PROCEDURE with parameters should return a result");
        assertTrue(result.isSuccess(), "CREATE PROCEDURE should succeed");
        
        // Verify procedure was registered
        ProcedureRegistry registry = engine.getProcedureRegistry();
        assertTrue(registry.exists("complex_proc"), "Procedure should be registered");
    }
    
    @Test
    @MemGres
    void testDropProcedureBasic(MemGresEngine engine, SqlExecutionEngine sqlEngine) throws SQLException, SqlExecutionException {
        // First create a procedure
        sqlEngine.execute("CREATE PROCEDURE drop_test_proc AS 'com.memgres.sql.ProcedureDDLTest$TestProcedure'");
        
        ProcedureRegistry registry = engine.getProcedureRegistry();
        assertTrue(registry.exists("drop_test_proc"), "Procedure should exist before drop");
        
        // Now drop it
        SqlExecutionResult result = sqlEngine.execute("DROP PROCEDURE drop_test_proc");
        
        assertNotNull(result, "DROP PROCEDURE should return a result");
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType(), "Should be DDL result type");
        assertTrue(result.isSuccess(), "DROP PROCEDURE should succeed");
        
        // Verify procedure was unregistered
        assertFalse(registry.exists("drop_test_proc"), "Procedure should no longer exist");
        assertEquals(0, registry.getCount(), "Should have zero procedures after drop");
    }
    
    @Test
    @MemGres
    void testDropProcedureIfExists(MemGresEngine engine, SqlExecutionEngine sqlEngine) throws SQLException, SqlExecutionException {
        // Test DROP PROCEDURE IF EXISTS with non-existent procedure
        SqlExecutionResult result = sqlEngine.execute("DROP PROCEDURE IF EXISTS nonexistent_proc");
        
        assertNotNull(result, "DROP PROCEDURE IF EXISTS should return a result");
        assertTrue(result.isSuccess(), "DROP PROCEDURE IF EXISTS should succeed even for non-existent procedure");
        
        // Create and then drop with IF EXISTS
        sqlEngine.execute("CREATE PROCEDURE exists_test_proc AS 'com.memgres.sql.ProcedureDDLTest$TestProcedure'");
        
        ProcedureRegistry registry = engine.getProcedureRegistry();
        assertTrue(registry.exists("exists_test_proc"), "Procedure should exist");
        
        result = sqlEngine.execute("DROP PROCEDURE IF EXISTS exists_test_proc");
        assertTrue(result.isSuccess(), "DROP PROCEDURE IF EXISTS should succeed for existing procedure");
        assertFalse(registry.exists("exists_test_proc"), "Procedure should be dropped");
    }
    
    @Test
    @MemGres
    void testDropNonexistentProcedure(MemGresEngine engine, SqlExecutionEngine sqlEngine) throws SQLException {
        // Test DROP PROCEDURE without IF EXISTS for non-existent procedure - should fail
        SqlExecutionException exception = assertThrows(SqlExecutionException.class, () -> {
            sqlEngine.execute("DROP PROCEDURE nonexistent_proc");
        }, "DROP PROCEDURE should fail for non-existent procedure");
        
        assertTrue(exception.getMessage().contains("does not exist"), 
            "Error message should mention procedure doesn't exist");
    }
    
    @Test
    @MemGres
    void testCreateProcedureDuplicateName(MemGresEngine engine, SqlExecutionEngine sqlEngine) throws SQLException, SqlExecutionException {
        // First procedure creation should succeed
        SqlExecutionResult result1 = sqlEngine.execute(
            "CREATE PROCEDURE dup_proc AS 'com.memgres.sql.ProcedureDDLTest$TestProcedure'"
        );
        assertTrue(result1.isSuccess(), "First CREATE PROCEDURE should succeed");
        
        // Second procedure with same name should fail or overwrite (depending on implementation)
        try {
            SqlExecutionResult result2 = sqlEngine.execute(
                "CREATE PROCEDURE dup_proc AS 'com.memgres.sql.ProcedureDDLTest$TestProcedure'"
            );
            // If we reach here, the implementation allows overwriting
            assertTrue(result2.isSuccess(), "Duplicate CREATE PROCEDURE should succeed if overwriting is allowed");
        } catch (SqlExecutionException e) {
            // If exception is thrown, the implementation prevents duplicates
            assertTrue(e.getMessage().contains("already exists") || e.getMessage().contains("duplicate"), 
                "Error message should mention duplicate or already exists");
        }
    }
    
    @Test
    @MemGres
    void testCreateProcedureInvalidClass(MemGresEngine engine, SqlExecutionEngine sqlEngine) throws SQLException {
        // Test CREATE PROCEDURE with invalid Java class
        SqlExecutionException exception = assertThrows(SqlExecutionException.class, () -> {
            sqlEngine.execute("CREATE PROCEDURE invalid_proc AS 'com.nonexistent.InvalidClass'");
        }, "CREATE PROCEDURE should fail for invalid Java class");
        
        assertTrue(exception.getMessage().contains("ClassNotFoundException") || 
                  exception.getMessage().contains("class not found") ||
                  exception.getMessage().contains("Failed to create procedure"), 
            "Error message should mention class loading issue: " + exception.getMessage());
    }
    
    @Test
    @MemGres
    void testProcedureDDLLifecycle(MemGresEngine engine, SqlExecutionEngine sqlEngine) throws SQLException, SqlExecutionException {
        ProcedureRegistry registry = engine.getProcedureRegistry();
        assertEquals(0, registry.getCount(), "Should start with no procedures");
        
        // Create multiple procedures
        sqlEngine.execute("CREATE PROCEDURE proc1 AS 'com.memgres.sql.ProcedureDDLTest$TestProcedure'");
        sqlEngine.execute("CREATE PROCEDURE proc2 AS 'com.memgres.sql.ProcedureDDLTest$TestProcedure'");
        sqlEngine.execute("CREATE PROCEDURE proc3 AS 'com.memgres.sql.ProcedureDDLTest$TestProcedure'");
        
        assertEquals(3, registry.getCount(), "Should have 3 procedures");
        assertTrue(registry.exists("proc1"), "proc1 should exist");
        assertTrue(registry.exists("proc2"), "proc2 should exist");
        assertTrue(registry.exists("proc3"), "proc3 should exist");
        
        // Drop some procedures
        sqlEngine.execute("DROP PROCEDURE proc2");
        assertEquals(2, registry.getCount(), "Should have 2 procedures after dropping proc2");
        assertFalse(registry.exists("proc2"), "proc2 should not exist");
        assertTrue(registry.exists("proc1"), "proc1 should still exist");
        assertTrue(registry.exists("proc3"), "proc3 should still exist");
        
        // Drop remaining procedures
        sqlEngine.execute("DROP PROCEDURE IF EXISTS proc1");
        sqlEngine.execute("DROP PROCEDURE IF EXISTS proc3");
        sqlEngine.execute("DROP PROCEDURE IF EXISTS nonexistent"); // Should not fail
        
        assertEquals(0, registry.getCount(), "Should have no procedures at the end");
    }
    
    @Test
    @MemGres
    void testProcedureDDLWithJDBCConnection(MemGresEngine engine, SqlExecutionEngine sqlEngine) throws SQLException, SqlExecutionException {
        // Test DDL operations through JDBC Connection interface
        try (MemGresTestConnection conn = new MemGresTestConnection(engine, sqlEngine)) {
            conn.setAutoCommit(false);
            
            try (Statement stmt = conn.createStatement()) {
                // Create procedure via JDBC
                int result = stmt.executeUpdate("CREATE PROCEDURE jdbc_proc AS 'com.memgres.sql.ProcedureDDLTest$TestProcedure'");
                assertEquals(0, result, "DDL statements typically return 0 for update count");
                
                // Verify procedure exists
                ProcedureRegistry registry = engine.getProcedureRegistry();
                assertTrue(registry.exists("jdbc_proc"), "Procedure should exist after JDBC creation");
                
                // Drop procedure via JDBC
                result = stmt.executeUpdate("DROP PROCEDURE jdbc_proc");
                assertEquals(0, result, "DDL statements typically return 0 for update count");
                
                // Verify procedure is gone
                assertFalse(registry.exists("jdbc_proc"), "Procedure should not exist after JDBC drop");
                
                conn.commit();
            }
        }
    }
}