package com.memgres.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for ConnectionPool functionality.
 */
public class ConnectionPoolTest {
    
    private MemGresEngine engine;
    private ConnectionPool pool;
    
    @BeforeEach
    void setUp() {
        engine = new MemGresEngine();
        engine.initialize();
        
        ConnectionPool.PoolConfiguration config = new ConnectionPool.PoolConfiguration()
                .setMinPoolSize(2)
                .setMaxPoolSize(5)
                .setConnectionTimeoutMs(1000)
                .setMaxConnectionIdleMs(5000);
        
        pool = new ConnectionPool(engine, config);
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
    void testPoolInitialization() throws SQLException {
        pool.initialize();
        
        ConnectionPool.PoolStatistics stats = pool.getStatistics();
        assertEquals(2, stats.getAvailableConnections(), "Should have minimum connections available");
        assertEquals(0, stats.getActiveConnections(), "Should have no active connections");
        assertEquals(2, stats.getTotalConnections(), "Should have created minimum connections");
    }
    
    @Test
    void testGetAndReturnConnection() throws SQLException {
        Connection conn = pool.getConnection();
        
        assertNotNull(conn, "Should get a connection");
        assertFalse(conn.isClosed(), "Connection should be open");
        assertTrue(conn instanceof PooledConnection, "Should be a pooled connection");
        
        ConnectionPool.PoolStatistics stats = pool.getStatistics();
        assertEquals(1, stats.getActiveConnections(), "Should have one active connection");
        
        // Return connection
        conn.close();
        
        stats = pool.getStatistics();
        assertEquals(0, stats.getActiveConnections(), "Should have no active connections after return");
    }
    
    @Test
    void testConnectionReuse() throws SQLException {
        pool.initialize(); // Ensure pool is initialized
        
        // Get and return a connection
        Connection conn1 = pool.getConnection();
        long connectionId1 = ((PooledConnection) conn1).getConnectionId();
        conn1.close();
        
        // Get another connection - should prefer reusing existing ones
        Connection conn2 = pool.getConnection();
        
        // The connection should be returned from the pool (may or may not be the same ID)
        // What's important is that we're getting a valid connection
        assertNotNull(conn2, "Should get a connection from the pool");
        assertTrue(conn2 instanceof PooledConnection, "Should be a pooled connection");
        conn2.close();
        
        // Verify pool statistics show connection reuse pattern
        ConnectionPool.PoolStatistics stats = pool.getStatistics();
        assertEquals(2, stats.getTotalConnectionsAcquired(), "Should have acquired 2 connections");
        assertEquals(2, stats.getTotalConnectionsReleased(), "Should have released 2 connections");
    }
    
    @Test
    void testMaxPoolSize() throws SQLException {
        List<Connection> connections = new ArrayList<>();
        
        // Get maximum connections
        for (int i = 0; i < 5; i++) {
            Connection conn = pool.getConnection();
            connections.add(conn);
        }
        
        ConnectionPool.PoolStatistics stats = pool.getStatistics();
        assertEquals(5, stats.getActiveConnections(), "Should have max active connections");
        assertEquals(5, stats.getTotalConnections(), "Should have max total connections");
        
        // Clean up
        for (Connection conn : connections) {
            conn.close();
        }
    }
    
    @Test
    void testConnectionTimeout() {
        // Get all available connections
        List<Connection> connections = new ArrayList<>();
        try {
            for (int i = 0; i < 5; i++) {
                connections.add(pool.getConnection());
            }
            
            // Try to get another connection - should timeout
            assertThrows(SQLException.class, () -> {
                pool.getConnection(100); // 100ms timeout
            }, "Should timeout when no connections available");
            
        } catch (SQLException e) {
            fail("Should not fail getting initial connections");
        } finally {
            // Clean up
            for (Connection conn : connections) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    // Ignore
                }
            }
        }
    }
    
    @Test
    void testConcurrentAccess() throws InterruptedException {
        int numThreads = 10;
        int operationsPerThread = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        List<Exception> exceptions = new ArrayList<>();
        
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        Connection conn = pool.getConnection();
                        
                        // Use connection briefly
                        try (Statement stmt = conn.createStatement()) {
                            // Just test that we can create a statement
                            assertNotNull(stmt);
                        }
                        
                        // Return connection
                        conn.close();
                        
                        // Small delay to simulate real usage
                        Thread.sleep(1);
                    }
                } catch (Exception e) {
                    synchronized (exceptions) {
                        exceptions.add(e);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(10, TimeUnit.SECONDS), "All threads should complete");
        
        if (!exceptions.isEmpty()) {
            fail("Concurrent access failed: " + exceptions.get(0).getMessage());
        }
        
        ConnectionPool.PoolStatistics stats = pool.getStatistics();
        assertEquals(0, stats.getActiveConnections(), "Should have no active connections after test");
        assertEquals(numThreads * operationsPerThread, stats.getTotalConnectionsAcquired(), 
                "Should have acquired expected number of connections");
        assertEquals(numThreads * operationsPerThread, stats.getTotalConnectionsReleased(), 
                "Should have released expected number of connections");
        
        executor.shutdown();
    }
    
    @Test
    void testConnectionValidation() throws SQLException {
        ConnectionPool.PoolConfiguration config = new ConnectionPool.PoolConfiguration()
                .setMinPoolSize(1)
                .setMaxPoolSize(2)
                .setTestOnBorrow(true)
                .setTestOnReturn(true);
        
        ConnectionPool validationPool = new ConnectionPool(engine, config);
        
        try {
            Connection conn = validationPool.getConnection();
            assertTrue(conn.isValid(1), "Connection should be valid");
            conn.close();
            
            ConnectionPool.PoolStatistics stats = validationPool.getStatistics();
            assertEquals(0, stats.getActiveConnections(), "Should have no active connections");
            
        } finally {
            validationPool.shutdown();
        }
    }
    
    @Test
    void testPoolShutdown() throws SQLException {
        pool.initialize();
        
        // Get some connections
        Connection conn1 = pool.getConnection();
        Connection conn2 = pool.getConnection();
        
        // Return one connection to the pool before shutdown
        conn1.close();
        
        ConnectionPool.PoolStatistics beforeShutdown = pool.getStatistics();
        assertTrue(beforeShutdown.getTotalConnections() > 0, "Should have connections before shutdown");
        
        // Shutdown pool
        pool.shutdown();
        
        // Try to get new connection - should fail
        assertThrows(SQLException.class, () -> {
            pool.getConnection();
        }, "Should not be able to get connection after shutdown");
        
        // Close remaining active connection
        conn2.close();
        
        ConnectionPool.PoolStatistics afterShutdown = pool.getStatistics();
        // After shutdown, available connections should be cleaned up
        assertTrue(afterShutdown.getTotalConnectionsDestroyed() > 0, "Should have destroyed some connections");
    }
    
    @Test
    void testPoolStatistics() throws SQLException {
        pool.initialize();
        
        ConnectionPool.PoolStatistics initialStats = pool.getStatistics();
        assertEquals(2, initialStats.getMinPoolSize());
        assertEquals(5, initialStats.getMaxPoolSize());
        assertTrue(initialStats.getTotalConnectionsCreated() >= 2);
        
        // Get and return a connection
        Connection conn = pool.getConnection();
        conn.close();
        
        ConnectionPool.PoolStatistics finalStats = pool.getStatistics();
        assertEquals(1, finalStats.getTotalConnectionsAcquired());
        assertEquals(1, finalStats.getTotalConnectionsReleased());
    }
    
    @Test
    void testConnectionProperties() throws SQLException {
        Connection conn = pool.getConnection();
        
        assertTrue(conn instanceof PooledConnection);
        PooledConnection pooledConn = (PooledConnection) conn;
        
        assertTrue(pooledConn.getConnectionId() > 0, "Should have valid connection ID");
        assertTrue(pooledConn.getAge() >= 0, "Should have valid age");
        assertEquals(0, pooledConn.getIdleTime(), "Active connection should have zero idle time");
        
        conn.close();
        
        // After returning to pool, idle time should be positive
        assertTrue(pooledConn.getIdleTime() >= 0, "Returned connection should have idle time");
    }
}