package com.memgres.sql;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.testing.MemGresTestConnection;
import com.memgres.testing.MemGresTestPreparedStatement;
import com.memgres.testing.MemGresTestStatement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for Generated Keys functionality.
 * Tests auto-increment columns and getGeneratedKeys() support.
 */
public class GeneratedKeysTest {
    
    private MemGresEngine engine;
    private SqlExecutionEngine sqlEngine;
    private MemGresTestConnection connection;
    
    @BeforeEach
    void setUp() throws Exception {
        engine = new MemGresEngine();
        engine.initialize();
        sqlEngine = new SqlExecutionEngine(engine);
        connection = new MemGresTestConnection(engine, sqlEngine);
        
        // Create test schema
        engine.createSchema("public");
        connection.setAutoCommit(true);
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
    void testAutoIncrementColumn() throws Exception {
        // Create table with auto-increment primary key
        String createTableSql = "CREATE TABLE users (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR(100) NOT NULL" +
                ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createTableSql);
            
            // Insert records without specifying ID
            stmt.executeUpdate("INSERT INTO users (name) VALUES ('Alice')");
            stmt.executeUpdate("INSERT INTO users (name) VALUES ('Bob')");
            
            // Verify auto-increment values were generated
            ResultSet rs = stmt.executeQuery("SELECT id, name FROM users ORDER BY id");
            
            assertTrue(rs.next());
            assertEquals(1L, rs.getLong("id"));
            assertEquals("Alice", rs.getString("name"));
            
            assertTrue(rs.next());
            assertEquals(2L, rs.getLong("id"));
            assertEquals("Bob", rs.getString("name"));
            
            assertFalse(rs.next());
        }
    }
    
    @Test
    void testGetGeneratedKeysStatement() throws Exception {
        // Create table with auto-increment column
        String createTableSql = "CREATE TABLE products (" +
                "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR(50) NOT NULL, " +
                "price DECIMAL(10,2)" +
                ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createTableSql);
            
            // Insert with generated keys capture
            int affectedRows = stmt.executeUpdate("INSERT INTO products (name, price) VALUES ('Laptop', 999.99)");
            assertEquals(1, affectedRows);
            
            // Get generated keys
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            assertNotNull(generatedKeys);
            
            assertTrue(generatedKeys.next());
            long generatedId = generatedKeys.getLong(1);
            assertTrue(generatedId > 0, "Generated key should be positive");
            
            assertFalse(generatedKeys.next(), "Should only have one generated key");
            
            // Verify the record was inserted with the generated key
            ResultSet rs = stmt.executeQuery("SELECT id, name, price FROM products WHERE id = " + generatedId);
            assertTrue(rs.next());
            assertEquals(generatedId, rs.getLong("id"));
            assertEquals("Laptop", rs.getString("name"));
            assertEquals(999.99, rs.getDouble("price"), 0.01);
        }
    }
    
    @Test
    void testGetGeneratedKeysPreparedStatement() throws Exception {
        // Create table with auto-increment column
        String createTableSql = "CREATE TABLE orders (" +
                "order_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "customer_name VARCHAR(100), " +
                "total DECIMAL(10,2)" +
                ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createTableSql);
        }
        
        // Use PreparedStatement for insertion
        String insertSql = "INSERT INTO orders (customer_name, total) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
            
            // Insert first order
            pstmt.setString(1, "John Doe");
            pstmt.setDouble(2, 150.50);
            int affectedRows = pstmt.executeUpdate();
            assertEquals(1, affectedRows);
            
            // Get generated keys
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            assertNotNull(generatedKeys);
            
            assertTrue(generatedKeys.next());
            long firstOrderId = generatedKeys.getLong(1);
            assertTrue(firstOrderId > 0);
            
            // Insert second order
            pstmt.setString(1, "Jane Smith");
            pstmt.setDouble(2, 75.25);
            pstmt.executeUpdate();
            
            // Get generated keys for second insert
            generatedKeys = pstmt.getGeneratedKeys();
            assertTrue(generatedKeys.next());
            long secondOrderId = generatedKeys.getLong(1);
            assertTrue(secondOrderId > firstOrderId);
        }
    }
    
    @Test
    void testMultipleInsertsAutoIncrement() throws Exception {
        // Create table
        String createTableSql = "CREATE TABLE items (" +
                "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                "description VARCHAR(200)" +
                ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createTableSql);
            
            // Insert multiple records and verify sequential IDs
            for (int i = 1; i <= 5; i++) {
                stmt.executeUpdate("INSERT INTO items (description) VALUES ('Item " + i + "')");
                
                ResultSet keys = stmt.getGeneratedKeys();
                assertTrue(keys.next());
                assertEquals(i, keys.getLong(1));
            }
            
            // Verify all records are present with correct IDs
            ResultSet rs = stmt.executeQuery("SELECT id, description FROM items ORDER BY id");
            for (int i = 1; i <= 5; i++) {
                assertTrue(rs.next());
                assertEquals(i, rs.getInt("id"));
                assertEquals("Item " + i, rs.getString("description"));
            }
        }
    }
    
    @Test
    void testExplicitIdOverridesAutoIncrement() throws Exception {
        // Create table with auto-increment
        String createTableSql = "CREATE TABLE manual_ids (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "data VARCHAR(50)" +
                ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createTableSql);
            
            // Insert with explicit ID
            stmt.executeUpdate("INSERT INTO manual_ids (id, data) VALUES (100, 'Explicit')");
            
            // Insert without ID (should auto-increment from current max + 1)
            stmt.executeUpdate("INSERT INTO manual_ids (data) VALUES ('Auto')");
            
            ResultSet keys = stmt.getGeneratedKeys();
            assertTrue(keys.next());
            long generatedId = keys.getLong(1);
            assertTrue(generatedId > 100, "Auto-increment should continue from highest existing ID");
            
            // Verify both records
            ResultSet rs = stmt.executeQuery("SELECT id, data FROM manual_ids ORDER BY id");
            
            assertTrue(rs.next());
            assertEquals(100L, rs.getLong("id"));
            assertEquals("Explicit", rs.getString("data"));
            
            assertTrue(rs.next());
            assertEquals(generatedId, rs.getLong("id"));
            assertEquals("Auto", rs.getString("data"));
        }
    }
    
    @Test
    void testGeneratedKeysEmptyForNonInsert() throws Exception {
        // Create table
        String createTableSql = "CREATE TABLE test_table (" +
                "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                "data VARCHAR(100)" +
                ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createTableSql);
            stmt.executeUpdate("INSERT INTO test_table (data) VALUES ('test')");
            
            // UPDATE should not generate keys
            stmt.executeUpdate("UPDATE test_table SET data = 'updated' WHERE id = 1");
            ResultSet keys = stmt.getGeneratedKeys();
            assertFalse(keys.next(), "UPDATE should not generate keys");
            
            // DELETE should not generate keys
            stmt.executeUpdate("DELETE FROM test_table WHERE id = 1");
            keys = stmt.getGeneratedKeys();
            assertFalse(keys.next(), "DELETE should not generate keys");
        }
    }
    
    @Test
    void testGeneratedKeysWithBatchInsert() throws Exception {
        // Create table
        String createTableSql = "CREATE TABLE batch_test (" +
                "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR(50)" +
                ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createTableSql);
            
            // Add batch of INSERT statements
            stmt.addBatch("INSERT INTO batch_test (name) VALUES ('Batch1')");
            stmt.addBatch("INSERT INTO batch_test (name) VALUES ('Batch2')");
            stmt.addBatch("INSERT INTO batch_test (name) VALUES ('Batch3')");
            
            // Execute batch
            int[] results = stmt.executeBatch();
            assertEquals(3, results.length);
            
            // Note: Generated keys behavior for batch operations may vary
            // This test ensures batch operations don't break the functionality
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM batch_test");
            assertTrue(rs.next());
            assertEquals(3, rs.getInt(1));
        }
    }
}