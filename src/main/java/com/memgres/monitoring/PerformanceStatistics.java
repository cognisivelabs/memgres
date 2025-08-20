package com.memgres.monitoring;

import java.util.Map;

/**
 * Immutable snapshot of performance statistics from PerformanceMonitor.
 */
public class PerformanceStatistics {
    private final long totalQueries;
    private final long totalQueryTime;
    private final long slowQueries;
    private final long longestQueryTime;
    private final long totalConnections;
    private final long activeConnections;
    private final long connectionErrors;
    private final long totalTransactions;
    private final long committedTransactions;
    private final long rolledBackTransactions;
    private final long memoryAllocations;
    private final long memoryDeallocations;
    private final Map<String, Long> queryTypeCounts;
    
    public PerformanceStatistics(long totalQueries, long totalQueryTime, long slowQueries,
                               long longestQueryTime, long totalConnections, long activeConnections,
                               long connectionErrors, long totalTransactions, long committedTransactions,
                               long rolledBackTransactions, long memoryAllocations, long memoryDeallocations,
                               Map<String, Long> queryTypeCounts) {
        this.totalQueries = totalQueries;
        this.totalQueryTime = totalQueryTime;
        this.slowQueries = slowQueries;
        this.longestQueryTime = longestQueryTime;
        this.totalConnections = totalConnections;
        this.activeConnections = activeConnections;
        this.connectionErrors = connectionErrors;
        this.totalTransactions = totalTransactions;
        this.committedTransactions = committedTransactions;
        this.rolledBackTransactions = rolledBackTransactions;
        this.memoryAllocations = memoryAllocations;
        this.memoryDeallocations = memoryDeallocations;
        this.queryTypeCounts = Map.copyOf(queryTypeCounts);
    }
    
    // Getters
    public long getTotalQueries() { return totalQueries; }
    public long getTotalQueryTime() { return totalQueryTime; }
    public long getSlowQueries() { return slowQueries; }
    public long getLongestQueryTime() { return longestQueryTime; }
    public long getTotalConnections() { return totalConnections; }
    public long getActiveConnections() { return activeConnections; }
    public long getConnectionErrors() { return connectionErrors; }
    public long getTotalTransactions() { return totalTransactions; }
    public long getCommittedTransactions() { return committedTransactions; }
    public long getRolledBackTransactions() { return rolledBackTransactions; }
    public long getMemoryAllocations() { return memoryAllocations; }
    public long getMemoryDeallocations() { return memoryDeallocations; }
    public Map<String, Long> getQueryTypeCounts() { return queryTypeCounts; }
    
    // Calculated metrics
    public double getAverageQueryTime() {
        return totalQueries > 0 ? (double) totalQueryTime / totalQueries : 0.0;
    }
    
    public double getSlowQueryPercentage() {
        return totalQueries > 0 ? (double) slowQueries / totalQueries * 100.0 : 0.0;
    }
    
    public double getTransactionSuccessRate() {
        long totalCompleted = committedTransactions + rolledBackTransactions;
        return totalCompleted > 0 ? (double) committedTransactions / totalCompleted * 100.0 : 0.0;
    }
    
    public double getConnectionErrorRate() {
        return totalConnections > 0 ? (double) connectionErrors / totalConnections * 100.0 : 0.0;
    }
    
    @Override
    public String toString() {
        return String.format(
            "PerformanceStatistics{totalQueries=%d, avgQueryTime=%.2fms, slowQueries=%d, " +
            "totalConnections=%d, activeConnections=%d, connectionErrors=%d, " +
            "totalTransactions=%d, committedTransactions=%d, rolledBackTransactions=%d, " +
            "memoryAllocations=%d, memoryDeallocations=%d}",
            totalQueries, getAverageQueryTime(), slowQueries,
            totalConnections, activeConnections, connectionErrors,
            totalTransactions, committedTransactions, rolledBackTransactions,
            memoryAllocations, memoryDeallocations
        );
    }
}