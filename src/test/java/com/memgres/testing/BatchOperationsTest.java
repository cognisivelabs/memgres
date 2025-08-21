package com.memgres.testing;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for JDBC batch operations support in MemGres.
 */
public class BatchOperationsTest {
    
    private MemGresEngine engine;
    private SqlExecutionEngine sqlEngine;
    private MemGresTestConnection connection;
    
    @BeforeEach
    void setUp() throws SQLException {
        engine = new MemGresEngine();
        engine.initialize();
        
        sqlEngine = new SqlExecutionEngine(engine);
        connection = new MemGresTestConnection(engine, sqlEngine);
        
        // Create test table
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE batch_test (id INTEGER, name TEXT, amount INTEGER)");
        }
    }
    
    @AfterEach
    void tearDown() {
        if (engine != null) {
            engine.shutdown();
        }
    }
    
    @Test
    void testStatementBatchOperations() throws SQLException {
        Statement stmt = connection.createStatement();
        
        // Test addBatch
        stmt.addBatch("INSERT INTO batch_test (id, name, amount) VALUES (1, 'first', 100)");
        stmt.addBatch("INSERT INTO batch_test (id, name, amount) VALUES (2, 'second', 200)");
        stmt.addBatch("INSERT INTO batch_test (id, name, amount) VALUES (3, 'third', 300)");
        
        // Test executeBatch
        int[] results = stmt.executeBatch();
        
        assertEquals(3, results.length);
        assertEquals(1, results[0]); // 1 row inserted
        assertEquals(1, results[1]); // 1 row inserted 
        assertEquals(1, results[2]); // 1 row inserted
        
        // Verify data was inserted
        try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM batch_test")) {
            assertTrue(rs.next());
            assertEquals(3, rs.getInt(1));
        }
        
        stmt.close();
    }
    
    @Test
    void testStatementBatchClear() throws SQLException {
        Statement stmt = connection.createStatement();
        
        // Add batch items
        stmt.addBatch("INSERT INTO batch_test (id, name, amount) VALUES (4, 'fourth', 400)");
        stmt.addBatch("INSERT INTO batch_test (id, name, amount) VALUES (5, 'fifth', 500)");
        
        // Clear batch
        stmt.clearBatch();
        
        // Execute empty batch
        int[] results = stmt.executeBatch();
        assertEquals(0, results.length);
        
        // Verify no data was inserted
        try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM batch_test")) {
            assertTrue(rs.next());
            assertEquals(0, rs.getInt(1));
        }
        
        stmt.close();
    }
    
    @Test
    void testPreparedStatementBatchOperations() throws SQLException {
        String sql = "INSERT INTO batch_test (id, name, amount) VALUES (?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        
        // First batch entry
        pstmt.setInt(1, 10);
        pstmt.setString(2, "ten");
        pstmt.setInt(3, 1000);
        pstmt.addBatch();
        
        // Second batch entry
        pstmt.setInt(1, 20);
        pstmt.setString(2, "twenty");
        pstmt.setInt(3, 2000);
        pstmt.addBatch();
        
        // Third batch entry
        pstmt.setInt(1, 30);
        pstmt.setString(2, "thirty");
        pstmt.setInt(3, 3000);
        pstmt.addBatch();
        
        // Execute batch
        int[] results = pstmt.executeBatch();
        
        assertEquals(3, results.length);
        assertEquals(1, results[0]); // 1 row inserted
        assertEquals(1, results[1]); // 1 row inserted
        assertEquals(1, results[2]); // 1 row inserted
        
        // Verify data was inserted with correct amounts
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, amount FROM batch_test ORDER BY id")) {
            
            assertTrue(rs.next());
            assertEquals(10, rs.getInt("id"));
            assertEquals("ten", rs.getString("name"));
            assertEquals(1000, rs.getInt("amount"));
            
            assertTrue(rs.next());
            assertEquals(20, rs.getInt("id"));
            assertEquals("twenty", rs.getString("name"));
            assertEquals(2000, rs.getInt("amount"));
            
            assertTrue(rs.next());
            assertEquals(30, rs.getInt("id"));
            assertEquals("thirty", rs.getString("name"));
            assertEquals(3000, rs.getInt("amount"));
            
            assertFalse(rs.next());
        }
        
        pstmt.close();
    }
    
    @Test
    void testPreparedStatementBatchClear() throws SQLException {
        String sql = "INSERT INTO batch_test (id, name, amount) VALUES (?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        
        // Add batch entries
        pstmt.setInt(1, 40);
        pstmt.setString(2, "forty");
        pstmt.setInt(3, 4000);
        pstmt.addBatch();
        
        pstmt.setInt(1, 50);
        pstmt.setString(2, "fifty");
        pstmt.setInt(3, 5000);
        pstmt.addBatch();
        
        // Clear batch
        pstmt.clearBatch();
        
        // Execute empty batch
        int[] results = pstmt.executeBatch();
        assertEquals(0, results.length);
        
        // Verify no data was inserted
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM batch_test")) {
            assertTrue(rs.next());
            assertEquals(0, rs.getInt(1));
        }
        
        pstmt.close();
    }
    
    @Test
    void testBatchWithMixedOperations() throws SQLException {
        // Insert initial data
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO batch_test (id, name, amount) VALUES (1, 'initial', 100)");
        }
        
        Statement stmt = connection.createStatement();
        
        // Mix INSERT, UPDATE, DELETE in batch
        stmt.addBatch("INSERT INTO batch_test (id, name, amount) VALUES (2, 'inserted', 200)");
        stmt.addBatch("UPDATE batch_test SET amount = 150 WHERE id = 1");
        stmt.addBatch("INSERT INTO batch_test (id, name, amount) VALUES (3, 'another', 300)");
        
        int[] results = stmt.executeBatch();
        
        assertEquals(3, results.length);
        assertEquals(1, results[0]); // INSERT affected 1 row
        assertEquals(1, results[1]); // UPDATE affected 1 row
        assertEquals(1, results[2]); // INSERT affected 1 row
        
        // Verify final state
        try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM batch_test")) {
            assertTrue(rs.next());
            assertEquals(3, rs.getInt(1)); // Should have 3 rows total
        }
        
        stmt.close();
    }
    
    @Test
    void testBatchEmptySQL() throws SQLException {
        Statement stmt = connection.createStatement();
        
        // Test adding null/empty SQL
        assertThrows(SQLException.class, () -> stmt.addBatch(null));
        assertThrows(SQLException.class, () -> stmt.addBatch(""));
        assertThrows(SQLException.class, () -> stmt.addBatch("   "));
        
        stmt.close();
    }
}