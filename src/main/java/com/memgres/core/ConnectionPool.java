package com.memgres.core;

import com.memgres.testing.MemGresTestConnection;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.monitoring.PerformanceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Connection pool implementation for MemGres database.
 * Provides efficient connection reuse and management for high-throughput applications.
 */
public class ConnectionPool {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
    
    private final MemGresEngine engine;
    private final BlockingQueue<PooledConnection> availableConnections;
    private final AtomicInteger activeConnections;
    private final AtomicInteger totalConnections;
    private final AtomicLong totalConnectionsCreated;
    private final AtomicLong totalConnectionsDestroyed;
    private final AtomicLong totalConnectionsAcquired;
    private final AtomicLong totalConnectionsReleased;
    private final PerformanceMonitor performanceMonitor;
    
    // Pool configuration
    private final int minPoolSize;
    private final int maxPoolSize;
    private final long connectionTimeoutMs;
    private final long maxConnectionIdleMs;
    private final boolean testOnBorrow;
    private final boolean testOnReturn;
    
    private volatile boolean initialized;
    private volatile boolean shutdown;
    
    /**
     * Create a connection pool with default configuration.
     */
    public ConnectionPool(MemGresEngine engine) {
        this(engine, new PoolConfiguration());
    }
    
    /**
     * Create a connection pool with custom configuration.
     */
    public ConnectionPool(MemGresEngine engine, PoolConfiguration config) {
        this.engine = engine;
        this.minPoolSize = config.getMinPoolSize();
        this.maxPoolSize = config.getMaxPoolSize();
        this.connectionTimeoutMs = config.getConnectionTimeoutMs();
        this.maxConnectionIdleMs = config.getMaxConnectionIdleMs();
        this.testOnBorrow = config.isTestOnBorrow();
        this.testOnReturn = config.isTestOnReturn();
        
        this.availableConnections = new ArrayBlockingQueue<>(maxPoolSize);
        this.activeConnections = new AtomicInteger(0);
        this.totalConnections = new AtomicInteger(0);
        this.totalConnectionsCreated = new AtomicLong(0);
        this.totalConnectionsDestroyed = new AtomicLong(0);
        this.totalConnectionsAcquired = new AtomicLong(0);
        this.totalConnectionsReleased = new AtomicLong(0);
        this.performanceMonitor = PerformanceMonitor.getInstance();
        
        logger.info("Created connection pool: min={}, max={}, timeout={}ms", 
                minPoolSize, maxPoolSize, connectionTimeoutMs);
    }
    
    /**
     * Initialize the connection pool by creating minimum connections.
     */
    public synchronized void initialize() throws SQLException {
        if (initialized) {
            return;
        }
        
        logger.info("Initializing connection pool with {} minimum connections", minPoolSize);
        
        for (int i = 0; i < minPoolSize; i++) {
            PooledConnection connection = createPooledConnection();
            availableConnections.offer(connection);
        }
        
        initialized = true;
        logger.info("Connection pool initialized with {} connections", availableConnections.size());
    }
    
    /**
     * Get a connection from the pool.
     */
    public Connection getConnection() throws SQLException {
        return getConnection(connectionTimeoutMs);
    }
    
    /**
     * Get a connection from the pool with timeout.
     */
    public Connection getConnection(long timeoutMs) throws SQLException {
        if (shutdown) {
            throw new SQLException("Connection pool has been shutdown");
        }
        
        if (!initialized) {
            initialize();
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Try to get an existing connection first
            PooledConnection pooledConnection = availableConnections.poll(timeoutMs, TimeUnit.MILLISECONDS);
            
            // If no connection available, try to create a new one
            if (pooledConnection == null && totalConnections.get() < maxPoolSize) {
                pooledConnection = createPooledConnection();
                logger.debug("Created new connection, total: {}", totalConnections.get());
            }
            
            // If still no connection, wait for one to become available
            if (pooledConnection == null) {
                long remainingTime = timeoutMs - (System.currentTimeMillis() - startTime);
                if (remainingTime > 0) {
                    pooledConnection = availableConnections.poll(remainingTime, TimeUnit.MILLISECONDS);
                }
            }
            
            if (pooledConnection == null) {
                throw new SQLException("Connection timeout: unable to get connection within " + timeoutMs + "ms");
            }
            
            // Test connection if required
            if (testOnBorrow && !isConnectionValid(pooledConnection)) {
                logger.debug("Connection failed validation test, creating new one");
                destroyPooledConnection(pooledConnection);
                return getConnection(timeoutMs - (System.currentTimeMillis() - startTime));
            }
            
            // Mark connection as active
            activeConnections.incrementAndGet();
            totalConnectionsAcquired.incrementAndGet();
            pooledConnection.markAsAcquired();
            
            logger.debug("Connection acquired: active={}, available={}", 
                    activeConnections.get(), availableConnections.size());
            
            return pooledConnection;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for connection", e);
        }
    }
    
    /**
     * Return a connection to the pool.
     */
    public void returnConnection(Connection connection) {
        if (connection == null || shutdown) {
            return;
        }
        
        if (!(connection instanceof PooledConnection)) {
            logger.warn("Attempting to return non-pooled connection");
            return;
        }
        
        PooledConnection pooledConnection = (PooledConnection) connection;
        
        if (!pooledConnection.isFromThisPool(this)) {
            logger.warn("Attempting to return connection from different pool");
            return;
        }
        
        try {
            // Test connection if required
            if (testOnReturn && !isConnectionValid(pooledConnection)) {
                logger.debug("Connection failed return validation test, destroying");
                destroyPooledConnection(pooledConnection);
                return;
            }
            
            // Check if connection is too old
            if (maxConnectionIdleMs > 0 && pooledConnection.getIdleTime() > maxConnectionIdleMs) {
                logger.debug("Connection exceeded max idle time, destroying");
                destroyPooledConnection(pooledConnection);
                return;
            }
            
            // Return to pool
            pooledConnection.markAsReturned();
            activeConnections.decrementAndGet();
            totalConnectionsReleased.incrementAndGet();
            
            if (!availableConnections.offer(pooledConnection)) {
                logger.warn("Unable to return connection to pool, destroying");
                destroyPooledConnection(pooledConnection);
            } else {
                logger.debug("Connection returned: active={}, available={}", 
                        activeConnections.get(), availableConnections.size());
            }
            
        } catch (Exception e) {
            logger.error("Error returning connection to pool", e);
            try {
                destroyPooledConnection(pooledConnection);
            } catch (Exception destroyError) {
                logger.error("Error destroying connection during return failure", destroyError);
            }
        }
    }
    
    /**
     * Create a new pooled connection.
     */
    private PooledConnection createPooledConnection() throws SQLException {
        try {
            SqlExecutionEngine sqlEngine = new SqlExecutionEngine(engine);
            Connection rawConnection = new MemGresTestConnection(engine, sqlEngine);
            PooledConnection pooledConnection = new PooledConnection(rawConnection, this);
            
            totalConnections.incrementAndGet();
            totalConnectionsCreated.incrementAndGet();
            
            // Record connection creation
            performanceMonitor.recordConnectionEvent(
                PerformanceMonitor.ConnectionEvent.CREATED,
                "Pool connection " + pooledConnection.getConnectionId());
            
            return pooledConnection;
        } catch (Exception e) {
            throw new SQLException("Failed to create pooled connection", e);
        }
    }
    
    /**
     * Destroy a pooled connection.
     */
    private void destroyPooledConnection(PooledConnection connection) {
        try {
            connection.reallyClose();
            totalConnections.decrementAndGet();
            totalConnectionsDestroyed.incrementAndGet();
            // Only decrement active connections if the connection was active
            if (connection.isFromThisPool(this)) {
                // Connection is being destroyed, so it's no longer active
            }
            
            logger.debug("Connection destroyed: total={}", totalConnections.get());
            
            // Record connection destruction
            performanceMonitor.recordConnectionEvent(
                PerformanceMonitor.ConnectionEvent.CLOSED,
                "Pool connection " + connection.getConnectionId());
        } catch (Exception e) {
            logger.error("Error destroying pooled connection", e);
        }
    }
    
    /**
     * Test if a connection is valid.
     */
    private boolean isConnectionValid(PooledConnection connection) {
        try {
            return connection.isValid(1); // 1 second timeout
        } catch (Exception e) {
            logger.debug("Connection validation failed", e);
            return false;
        }
    }
    
    /**
     * Shutdown the connection pool.
     */
    public synchronized void shutdown() {
        if (shutdown) {
            return;
        }
        
        shutdown = true;
        logger.info("Shutting down connection pool");
        
        // Close all available connections
        PooledConnection connection;
        while ((connection = availableConnections.poll()) != null) {
            destroyPooledConnection(connection);
        }
        
        logger.info("Connection pool shutdown complete. Statistics: created={}, destroyed={}, acquired={}, released={}", 
                totalConnectionsCreated.get(), totalConnectionsDestroyed.get(),
                totalConnectionsAcquired.get(), totalConnectionsReleased.get());
    }
    
    /**
     * Get pool statistics.
     */
    public PoolStatistics getStatistics() {
        return new PoolStatistics(
                activeConnections.get(),
                availableConnections.size(),
                totalConnections.get(),
                totalConnectionsCreated.get(),
                totalConnectionsDestroyed.get(),
                totalConnectionsAcquired.get(),
                totalConnectionsReleased.get(),
                maxPoolSize,
                minPoolSize
        );
    }
    
    /**
     * Pool configuration class.
     */
    public static class PoolConfiguration {
        private int minPoolSize = 5;
        private int maxPoolSize = 20;
        private long connectionTimeoutMs = 5000; // 5 seconds
        private long maxConnectionIdleMs = 300000; // 5 minutes
        private boolean testOnBorrow = true;
        private boolean testOnReturn = true;
        
        public int getMinPoolSize() { return minPoolSize; }
        public PoolConfiguration setMinPoolSize(int minPoolSize) { 
            this.minPoolSize = minPoolSize; return this; 
        }
        
        public int getMaxPoolSize() { return maxPoolSize; }
        public PoolConfiguration setMaxPoolSize(int maxPoolSize) { 
            this.maxPoolSize = maxPoolSize; return this; 
        }
        
        public long getConnectionTimeoutMs() { return connectionTimeoutMs; }
        public PoolConfiguration setConnectionTimeoutMs(long connectionTimeoutMs) { 
            this.connectionTimeoutMs = connectionTimeoutMs; return this; 
        }
        
        public long getMaxConnectionIdleMs() { return maxConnectionIdleMs; }
        public PoolConfiguration setMaxConnectionIdleMs(long maxConnectionIdleMs) { 
            this.maxConnectionIdleMs = maxConnectionIdleMs; return this; 
        }
        
        public boolean isTestOnBorrow() { return testOnBorrow; }
        public PoolConfiguration setTestOnBorrow(boolean testOnBorrow) { 
            this.testOnBorrow = testOnBorrow; return this; 
        }
        
        public boolean isTestOnReturn() { return testOnReturn; }
        public PoolConfiguration setTestOnReturn(boolean testOnReturn) { 
            this.testOnReturn = testOnReturn; return this; 
        }
    }
    
    /**
     * Pool statistics class.
     */
    public static class PoolStatistics {
        private final int activeConnections;
        private final int availableConnections;
        private final int totalConnections;
        private final long totalConnectionsCreated;
        private final long totalConnectionsDestroyed;
        private final long totalConnectionsAcquired;
        private final long totalConnectionsReleased;
        private final int maxPoolSize;
        private final int minPoolSize;
        
        public PoolStatistics(int activeConnections, int availableConnections, int totalConnections,
                            long totalConnectionsCreated, long totalConnectionsDestroyed,
                            long totalConnectionsAcquired, long totalConnectionsReleased,
                            int maxPoolSize, int minPoolSize) {
            this.activeConnections = activeConnections;
            this.availableConnections = availableConnections;
            this.totalConnections = totalConnections;
            this.totalConnectionsCreated = totalConnectionsCreated;
            this.totalConnectionsDestroyed = totalConnectionsDestroyed;
            this.totalConnectionsAcquired = totalConnectionsAcquired;
            this.totalConnectionsReleased = totalConnectionsReleased;
            this.maxPoolSize = maxPoolSize;
            this.minPoolSize = minPoolSize;
        }
        
        // Getters
        public int getActiveConnections() { return activeConnections; }
        public int getAvailableConnections() { return availableConnections; }
        public int getTotalConnections() { return totalConnections; }
        public long getTotalConnectionsCreated() { return totalConnectionsCreated; }
        public long getTotalConnectionsDestroyed() { return totalConnectionsDestroyed; }
        public long getTotalConnectionsAcquired() { return totalConnectionsAcquired; }
        public long getTotalConnectionsReleased() { return totalConnectionsReleased; }
        public int getMaxPoolSize() { return maxPoolSize; }
        public int getMinPoolSize() { return minPoolSize; }
        
        @Override
        public String toString() {
            return String.format(
                "PoolStatistics{active=%d, available=%d, total=%d, " +
                "created=%d, destroyed=%d, acquired=%d, released=%d, " +
                "maxSize=%d, minSize=%d}",
                activeConnections, availableConnections, totalConnections,
                totalConnectionsCreated, totalConnectionsDestroyed,
                totalConnectionsAcquired, totalConnectionsReleased,
                maxPoolSize, minPoolSize
            );
        }
    }
}