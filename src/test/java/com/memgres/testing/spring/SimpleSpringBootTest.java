package com.memgres.testing.spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple Spring test demonstrating MemGres integration.
 * 
 * <p>This test shows how to use MemGres in a Spring application
 * with basic JDBC operations.</p>
 * 
 * @since 1.0.0
 */
@SpringJUnitConfig(classes = {MemGresTestConfiguration.class})
@DataMemGres(
    schema = "app_test",
    transactional = false
)
class SimpleSpringBootTest {
    
    @Autowired
    private DataSource dataSource;
    
    @Test
    void testJdbcIntegration() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create table
            stmt.execute("CREATE TABLE customers (id INTEGER PRIMARY KEY, name TEXT, email TEXT)");
            
            // Insert data
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO customers (id, name, email) VALUES (?, ?, ?)")) {
                ps.setInt(1, 1);
                ps.setString(2, "John Smith");
                ps.setString(3, "john@example.com");
                ps.executeUpdate();
                
                ps.setInt(1, 2);
                ps.setString(2, "Jane Doe");
                ps.setString(3, "jane@example.com");
                ps.executeUpdate();
            }
            
            // Query data
            List<Map<String, Object>> customers = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM customers ORDER BY id")) {
                while (rs.next()) {
                    Map<String, Object> customer = new HashMap<>();
                    customer.put("id", rs.getInt("id"));
                    customer.put("name", rs.getString("name"));
                    customer.put("email", rs.getString("email"));
                    customers.add(customer);
                }
            }
            
            assertEquals(2, customers.size());
            
            Map<String, Object> customer1 = customers.get(0);
            assertEquals(1, customer1.get("id"));
            assertEquals("John Smith", customer1.get("name"));
            assertEquals("john@example.com", customer1.get("email"));
            
            Map<String, Object> customer2 = customers.get(1);
            assertEquals(2, customer2.get("id"));
            assertEquals("Jane Doe", customer2.get("name"));
            assertEquals("jane@example.com", customer2.get("email"));
            
            // Test count
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM customers")) {
                assertTrue(rs.next());
                assertEquals(2, rs.getInt(1));
            }
            
            // Test single result
            try (PreparedStatement ps = conn.prepareStatement("SELECT email FROM customers WHERE name = ?")) {
                ps.setString(1, "John Smith");
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals("john@example.com", rs.getString("email"));
                }
            }
        }
    }
    
    @Test
    void testDataSourceProperties() {
        assertNotNull(dataSource);
        
        // Verify we can get connections
        assertDoesNotThrow(() -> {
            try (var connection = dataSource.getConnection()) {
                assertNotNull(connection);
                assertFalse(connection.isClosed());
            }
        });
    }
    
    @Test
    void testComplexDataOperations() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create tables for a blog-like structure
            stmt.execute("CREATE TABLE authors (id INTEGER PRIMARY KEY, name TEXT NOT NULL, email TEXT UNIQUE NOT NULL)");
            stmt.execute("CREATE TABLE posts (id INTEGER PRIMARY KEY, title TEXT NOT NULL, content TEXT, author_id INTEGER)");
            
            // Insert test data
            stmt.execute("INSERT INTO authors (id, name, email) VALUES (1, 'Alice Johnson', 'alice@blog.com')");
            stmt.execute("INSERT INTO authors (id, name, email) VALUES (2, 'Bob Wilson', 'bob@blog.com')");
            
            stmt.execute("INSERT INTO posts (id, title, content, author_id) VALUES (1, 'Getting Started with Java', 'Java is a powerful programming language...', 1)");
            stmt.execute("INSERT INTO posts (id, title, content, author_id) VALUES (2, 'Database Design Principles', 'Good database design is crucial...', 2)");
            stmt.execute("INSERT INTO posts (id, title, content, author_id) VALUES (3, 'Advanced SQL Techniques', 'Advanced SQL features...', 1)");
            
            // Test complex queries
            String query = "SELECT p.title, a.name as author_name FROM posts p JOIN authors a ON p.author_id = a.id ORDER BY p.id";
            
            List<Map<String, Object>> results = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("title", rs.getString("title"));
                    result.put("author_name", rs.getString("author_name"));
                    results.add(result);
                }
            }
            
            assertEquals(3, results.size());
            
            // Verify first post
            Map<String, Object> post1 = results.get(0);
            assertEquals("Getting Started with Java", post1.get("title"));
            assertEquals("Alice Johnson", post1.get("author_name"));
            
            // Test author statistics
            String statsQuery = "SELECT a.name, COUNT(p.id) as post_count FROM authors a LEFT JOIN posts p ON a.id = p.author_id GROUP BY a.id, a.name ORDER BY post_count DESC";
            
            List<Map<String, Object>> stats = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery(statsQuery)) {
                while (rs.next()) {
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("name", rs.getString("name"));
                    stat.put("post_count", rs.getInt("post_count"));
                    stats.add(stat);
                }
            }
            
            assertEquals(2, stats.size());
            
            Map<String, Object> alice = stats.get(0);
            assertEquals("Alice Johnson", alice.get("name"));
            assertEquals(2, alice.get("post_count"));
            
            Map<String, Object> bob = stats.get(1);
            assertEquals("Bob Wilson", bob.get("name"));
            assertEquals(1, bob.get("post_count"));
        }
    }
}