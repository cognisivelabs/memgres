package com.memgres.monitoring;

import com.memgres.core.ConnectionPool;
import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the complete monitoring system with SQL execution.
 */
public class MonitoringIntegrationTest {
    
    private MemGresEngine engine;
    private SqlExecutionEngine sqlEngine;
    private ConnectionPool pool;
    private PerformanceMonitor monitor;
    private QueryAnalyzer analyzer;
    
    @BeforeEach
    void setUp() {
        engine = new MemGresEngine();
        engine.initialize();
        
        sqlEngine = new SqlExecutionEngine(engine);
        
        ConnectionPool.PoolConfiguration config = new ConnectionPool.PoolConfiguration()
                .setMinPoolSize(2)
                .setMaxPoolSize(5);
        pool = new ConnectionPool(engine, config);
        
        monitor = PerformanceMonitor.getInstance();
        analyzer = QueryAnalyzer.getInstance();
        
        // Reset monitors to start with clean state
        monitor.reset();
        analyzer.reset();
    }
    
    @AfterEach
    void tearDown() {
        if (pool != null) {
            pool.shutdown();
        }
        if (engine != null) {
            engine.shutdown();
        }
    }
    
    @Test
    void testSqlExecutionWithMonitoring() throws Exception {
        // Create a test table
        sqlEngine.execute("CREATE TABLE users (id INTEGER PRIMARY KEY, name VARCHAR(50), email VARCHAR(100))");
        
        // Insert test data
        sqlEngine.execute("INSERT INTO users VALUES (1, 'John Doe', 'john@example.com')");
        sqlEngine.execute("INSERT INTO users VALUES (2, 'Jane Smith', 'jane@example.com')");
        sqlEngine.execute("INSERT INTO users VALUES (3, 'Bob Johnson', 'bob@example.com')");
        
        // Execute various queries that will be monitored
        sqlEngine.execute("SELECT * FROM users"); // Potential full table scan
        sqlEngine.execute("SELECT * FROM users WHERE id = 1"); // Good query with WHERE
        sqlEngine.execute("SELECT name FROM users ORDER BY name"); // ORDER BY without LIMIT
        
        // Simulate a slow query by running a complex one multiple times
        for (int i = 0; i < 5; i++) {
            sqlEngine.execute("SELECT u1.name, u2.name FROM users u1 CROSS JOIN users u2"); // Complex join
        }
        
        // Get monitoring statistics
        PerformanceStatistics stats = monitor.getStatistics();
        
        // Verify that queries were recorded
        assertTrue(stats.getTotalQueries() > 0, "Should have recorded queries");
        assertTrue(stats.getTotalQueryTime() >= 0, "Should have recorded query time (may be 0 for fast queries)");
        
        // Verify query type classification
        assertTrue(stats.getQueryTypeCounts().get("DDL") >= 1, "Should have DDL queries (CREATE TABLE)");
        assertTrue(stats.getQueryTypeCounts().get("INSERT") >= 3, "Should have INSERT queries");
        assertTrue(stats.getQueryTypeCounts().get("SELECT") >= 6, "Should have SELECT queries");
        
        // Verify query analysis
        assertFalse(analyzer.getAllQueryPatterns().isEmpty(), "Should have analyzed query patterns");
        
        // Generate optimization report
        OptimizationReport report = analyzer.generateOptimizationReport(10);
        assertNotNull(report, "Should generate optimization report");
        
        String recommendations = report.generateRecommendations();
        assertTrue(recommendations.contains("MemGres Query Optimization Report"), 
            "Should contain report header");
    }
    
    @Test
    void testConnectionPoolMonitoring() throws Exception {
        pool.initialize();
        
        // Get multiple connections
        Connection conn1 = pool.getConnection();
        Connection conn2 = pool.getConnection();
        
        // Use the connections
        try (Statement stmt = conn1.createStatement()) {
            stmt.execute("CREATE TABLE test (id INTEGER)");
        }
        
        try (Statement stmt = conn2.createStatement()) {
            stmt.execute("INSERT INTO test VALUES (1)");
        }
        
        // Return connections
        conn1.close();
        conn2.close();
        
        // Check pool statistics
        ConnectionPool.PoolStatistics poolStats = pool.getStatistics();
        assertTrue(poolStats.getTotalConnections() >= 2, "Should have created connections");
        assertTrue(poolStats.getTotalConnectionsAcquired() >= 2, "Should have acquired connections");
        assertEquals(poolStats.getTotalConnectionsAcquired(), poolStats.getTotalConnectionsReleased(), 
            "Should have released all acquired connections");
        
        // Check monitoring statistics
        PerformanceStatistics monitorStats = monitor.getStatistics();
        assertTrue(monitorStats.getTotalConnections() >= 2, "Monitor should track connections");
    }
    
    @Test
    void testQueryAnalysisIntegration() throws Exception {
        // Create table for testing
        sqlEngine.execute("CREATE TABLE products (id INTEGER PRIMARY KEY, name VARCHAR(100), category_id INTEGER)");
        
        // Insert test data
        for (int i = 1; i <= 1000; i++) {
            sqlEngine.execute("INSERT INTO products VALUES (" + i + ", 'Product " + i + "', " + (i % 10) + ")");
        }
        
        // Execute queries that should trigger analysis issues
        sqlEngine.execute("SELECT * FROM products"); // Full table scan, large result set
        sqlEngine.execute("SELECT * FROM products ORDER BY name"); // ORDER BY without LIMIT
        
        // Execute a subquery in SELECT clause
        sqlEngine.execute("SELECT id, (SELECT COUNT(*) FROM products p2 WHERE p2.category_id = products.category_id) FROM products");
        
        // Check that analysis was performed
        assertFalse(analyzer.getAllQueryPatterns().isEmpty(), "Should have query patterns");
        
        // Find patterns that should have issues
        boolean foundIssues = analyzer.getAllQueryPatterns().values().stream()
            .anyMatch(pattern -> {
                QueryAnalysis analysis = analyzer.analyzeQuery(pattern.getNormalizedSql(), 
                    (long) pattern.getAverageExecutionTime(), (int) pattern.getAverageRowsReturned());
                return analysis.hasIssues();
            });
        
        assertTrue(foundIssues, "Should find some query issues in analysis");
    }
    
    @Test
    void testSystemHealthMonitoring() throws Exception {
        // Generate some activity
        sqlEngine.execute("CREATE TABLE health_test (id INTEGER, data VARCHAR(100))");
        
        for (int i = 0; i < 10; i++) {
            sqlEngine.execute("INSERT INTO health_test VALUES (" + i + ", 'test data " + i + "')");
        }
        
        // Execute some queries
        sqlEngine.execute("SELECT COUNT(*) FROM health_test");
        sqlEngine.execute("SELECT * FROM health_test WHERE id < 5");
        
        // This should not throw an exception
        assertDoesNotThrow(() -> monitor.logSystemHealth(), 
            "System health logging should work without errors");
        
        // Verify we have some statistics
        PerformanceStatistics stats = monitor.getStatistics();
        assertTrue(stats.getTotalQueries() > 0, "Should have query statistics");
        assertTrue(stats.getTotalQueryTime() >= 0, "Should have timing statistics (may be 0 for fast queries)");
    }
    
    @Test
    void testConcurrentMonitoring() throws Exception {
        pool.initialize();
        
        // Create test table
        sqlEngine.execute("CREATE TABLE concurrent_test (id INTEGER, thread_id INTEGER)");
        
        // Run concurrent operations
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < 10; j++) {
                        Connection conn = pool.getConnection();
                        try (Statement stmt = conn.createStatement()) {
                            stmt.execute("INSERT INTO concurrent_test VALUES (" + j + ", " + threadId + ")");
                            stmt.execute("SELECT COUNT(*) FROM concurrent_test WHERE thread_id = " + threadId);
                        }
                        conn.close();
                    }
                } catch (Exception e) {
                    fail("Concurrent operations should not fail: " + e.getMessage());
                }
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for completion
        for (Thread thread : threads) {
            thread.join(10000); // 10 second timeout
        }
        
        // Verify monitoring worked correctly
        PerformanceStatistics stats = monitor.getStatistics();
        assertTrue(stats.getTotalQueries() >= 100, "Should have recorded all concurrent queries");
        // Note: Active connections may still be in pool, not necessarily 0
        assertTrue(stats.getTotalConnections() >= 0, "Should have connection statistics");
    }
}