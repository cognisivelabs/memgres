package com.memgres.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.Map;

/**
 * Performance monitoring and metrics collection system for MemGres.
 * Provides real-time performance metrics, query analysis, and system monitoring.
 */
public class PerformanceMonitor {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitor.class);
    private static final PerformanceMonitor INSTANCE = new PerformanceMonitor();
    
    // Query Performance Metrics
    private final LongAdder totalQueries = new LongAdder();
    private final LongAdder totalQueryTime = new LongAdder();
    private final LongAdder slowQueries = new LongAdder();
    private final AtomicLong longestQueryTime = new AtomicLong(0);
    
    // Connection Metrics
    private final LongAdder totalConnections = new LongAdder();
    private final LongAdder activeConnections = new LongAdder();
    private final LongAdder connectionErrors = new LongAdder();
    
    // Transaction Metrics
    private final LongAdder totalTransactions = new LongAdder();
    private final LongAdder committedTransactions = new LongAdder();
    private final LongAdder rolledBackTransactions = new LongAdder();
    
    // Memory Metrics
    private final LongAdder memoryAllocations = new LongAdder();
    private final LongAdder memoryDeallocations = new LongAdder();
    
    // Query Type Counters
    private final Map<String, LongAdder> queryTypeCounters = new ConcurrentHashMap<>();
    
    // Slow Query Threshold (milliseconds)
    private volatile long slowQueryThreshold = 1000; // 1 second
    
    private PerformanceMonitor() {
        // Initialize query type counters
        queryTypeCounters.put("SELECT", new LongAdder());
        queryTypeCounters.put("INSERT", new LongAdder());
        queryTypeCounters.put("UPDATE", new LongAdder());
        queryTypeCounters.put("DELETE", new LongAdder());
        queryTypeCounters.put("DDL", new LongAdder());
        queryTypeCounters.put("OTHER", new LongAdder());
        
        logger.info("Performance monitor initialized with slow query threshold: {}ms", slowQueryThreshold);
    }
    
    public static PerformanceMonitor getInstance() {
        return INSTANCE;
    }
    
    /**
     * Record a query execution with timing and analysis.
     */
    public void recordQuery(String sql, long executionTimeMs, boolean success) {
        try {
            // Set MDC for structured logging
            MDC.put("queryTime", String.valueOf(executionTimeMs));
            MDC.put("querySuccess", String.valueOf(success));
            
            totalQueries.increment();
            totalQueryTime.add(executionTimeMs);
            
            // Update longest query time
            long currentLongest = longestQueryTime.get();
            if (executionTimeMs > currentLongest) {
                longestQueryTime.compareAndSet(currentLongest, executionTimeMs);
            }
            
            // Classify and count query type
            String queryType = classifyQuery(sql);
            queryTypeCounters.get(queryType).increment();
            MDC.put("queryType", queryType);
            
            // Check if this is a slow query
            if (executionTimeMs >= slowQueryThreshold) {
                slowQueries.increment();
                logger.warn("Slow query detected: {} | Time: {}ms | SQL: {}", 
                    queryType, executionTimeMs, truncateSql(sql));
            } else {
                logger.debug("Query executed: {} | Time: {}ms | Success: {}", 
                    queryType, executionTimeMs, success);
            }
            
            // Log error details for failed queries
            if (!success) {
                logger.error("Query failed: {} | Time: {}ms | SQL: {}", 
                    queryType, executionTimeMs, truncateSql(sql));
            }
            
        } finally {
            // Clean up MDC
            MDC.remove("queryTime");
            MDC.remove("querySuccess");
            MDC.remove("queryType");
        }
    }
    
    /**
     * Record connection lifecycle events.
     */
    public void recordConnectionEvent(ConnectionEvent event, String details) {
        switch (event) {
            case CREATED:
                totalConnections.increment();
                activeConnections.increment();
                logger.debug("Connection created: {}", details);
                break;
            case CLOSED:
                activeConnections.decrement();
                logger.debug("Connection closed: {}", details);
                break;
            case ERROR:
                connectionErrors.increment();
                logger.error("Connection error: {}", details);
                break;
        }
    }
    
    /**
     * Record transaction lifecycle events.
     */
    public void recordTransactionEvent(TransactionEvent event, long transactionId, long durationMs) {
        try {
            MDC.put("transactionId", String.valueOf(transactionId));
            MDC.put("transactionDuration", String.valueOf(durationMs));
            
            switch (event) {
                case STARTED:
                    totalTransactions.increment();
                    logger.debug("Transaction started: {}", transactionId);
                    break;
                case COMMITTED:
                    committedTransactions.increment();
                    logger.debug("Transaction committed: {} | Duration: {}ms", transactionId, durationMs);
                    break;
                case ROLLED_BACK:
                    rolledBackTransactions.increment();
                    logger.debug("Transaction rolled back: {} | Duration: {}ms", transactionId, durationMs);
                    break;
            }
        } finally {
            MDC.remove("transactionId");
            MDC.remove("transactionDuration");
        }
    }
    
    /**
     * Record memory allocation events.
     */
    public void recordMemoryEvent(MemoryEvent event, long sizeBytes, String description) {
        try {
            MDC.put("memorySize", String.valueOf(sizeBytes));
            MDC.put("memoryDescription", description);
            
            switch (event) {
                case ALLOCATED:
                    memoryAllocations.increment();
                    logger.debug("Memory allocated: {} bytes | {}", sizeBytes, description);
                    break;
                case DEALLOCATED:
                    memoryDeallocations.increment();
                    logger.debug("Memory deallocated: {} bytes | {}", sizeBytes, description);
                    break;
            }
        } finally {
            MDC.remove("memorySize");
            MDC.remove("memoryDescription");
        }
    }
    
    /**
     * Get comprehensive performance statistics.
     */
    public PerformanceStatistics getStatistics() {
        return new PerformanceStatistics(
            totalQueries.sum(),
            totalQueryTime.sum(),
            slowQueries.sum(),
            longestQueryTime.get(),
            totalConnections.sum(),
            activeConnections.sum(),
            connectionErrors.sum(),
            totalTransactions.sum(),
            committedTransactions.sum(),
            rolledBackTransactions.sum(),
            memoryAllocations.sum(),
            memoryDeallocations.sum(),
            new ConcurrentHashMap<>(queryTypeCounters).entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().sum()
                ))
        );
    }
    
    /**
     * Log current system health metrics.
     */
    public void logSystemHealth() {
        PerformanceStatistics stats = getStatistics();
        Runtime runtime = Runtime.getRuntime();
        
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        logger.info("System Health Check:");
        logger.info("  Queries: {} total, {} slow, avg: {}ms, longest: {}ms",
            stats.getTotalQueries(),
            stats.getSlowQueries(),
            stats.getTotalQueries() > 0 ? stats.getTotalQueryTime() / stats.getTotalQueries() : 0,
            stats.getLongestQueryTime());
        
        logger.info("  Connections: {} total, {} active, {} errors",
            stats.getTotalConnections(),
            stats.getActiveConnections(),
            stats.getConnectionErrors());
        
        logger.info("  Transactions: {} total, {} committed, {} rolled back",
            stats.getTotalTransactions(),
            stats.getCommittedTransactions(),
            stats.getRolledBackTransactions());
        
        logger.info("  Memory: {}MB used, {}MB free, {}MB max",
            usedMemory / (1024 * 1024),
            freeMemory / (1024 * 1024),
            maxMemory / (1024 * 1024));
        
        logger.info("  Query Types: {}", stats.getQueryTypeCounts());
    }
    
    /**
     * Set the slow query threshold in milliseconds.
     */
    public void setSlowQueryThreshold(long thresholdMs) {
        this.slowQueryThreshold = thresholdMs;
        logger.info("Slow query threshold updated to: {}ms", thresholdMs);
    }
    
    /**
     * Reset all performance counters.
     */
    public void reset() {
        totalQueries.reset();
        totalQueryTime.reset();
        slowQueries.reset();
        longestQueryTime.set(0);
        totalConnections.reset();
        activeConnections.reset();
        connectionErrors.reset();
        totalTransactions.reset();
        committedTransactions.reset();
        rolledBackTransactions.reset();
        memoryAllocations.reset();
        memoryDeallocations.reset();
        
        queryTypeCounters.values().forEach(LongAdder::reset);
        
        logger.info("Performance monitor counters reset");
    }
    
    private String classifyQuery(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return "OTHER";
        }
        
        String trimmedUpper = sql.trim().toUpperCase();
        if (trimmedUpper.startsWith("SELECT")) {
            return "SELECT";
        } else if (trimmedUpper.startsWith("INSERT")) {
            return "INSERT";
        } else if (trimmedUpper.startsWith("UPDATE")) {
            return "UPDATE";
        } else if (trimmedUpper.startsWith("DELETE")) {
            return "DELETE";
        } else if (trimmedUpper.startsWith("CREATE") || 
                   trimmedUpper.startsWith("DROP") || 
                   trimmedUpper.startsWith("ALTER") ||
                   trimmedUpper.startsWith("TRUNCATE")) {
            return "DDL";
        } else {
            return "OTHER";
        }
    }
    
    private String truncateSql(String sql) {
        if (sql == null) return "null";
        if (sql.length() <= 100) return sql;
        return sql.substring(0, 100) + "...";
    }
    
    // Event enums
    public enum ConnectionEvent {
        CREATED, CLOSED, ERROR
    }
    
    public enum TransactionEvent {
        STARTED, COMMITTED, ROLLED_BACK
    }
    
    public enum MemoryEvent {
        ALLOCATED, DEALLOCATED
    }
}