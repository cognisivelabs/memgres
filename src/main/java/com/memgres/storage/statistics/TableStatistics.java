package com.memgres.storage.statistics;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Collects and maintains statistics for a database table.
 * Used by the cost-based query planner for optimization decisions.
 */
public class TableStatistics {
    
    private final String tableName;
    private final AtomicLong rowCount;
    private final AtomicLong totalSize; // Estimated size in bytes
    private final Map<String, ColumnStatistics> columnStats;
    private volatile LocalDateTime lastUpdated;
    private final AtomicLong accessCount; // Number of times table was accessed
    private final AtomicLong scanCount;   // Number of full table scans
    
    public TableStatistics(String tableName) {
        this.tableName = tableName;
        this.rowCount = new AtomicLong(0);
        this.totalSize = new AtomicLong(0);
        this.columnStats = new ConcurrentHashMap<>();
        this.lastUpdated = LocalDateTime.now();
        this.accessCount = new AtomicLong(0);
        this.scanCount = new AtomicLong(0);
    }
    
    /**
     * Update statistics after data modification.
     */
    public void updateAfterInsert(int insertedRows, long estimatedSizeIncrease) {
        rowCount.addAndGet(insertedRows);
        totalSize.addAndGet(estimatedSizeIncrease);
        lastUpdated = LocalDateTime.now();
    }
    
    /**
     * Update statistics after data deletion.
     */
    public void updateAfterDelete(int deletedRows, long estimatedSizeDecrease) {
        rowCount.addAndGet(-deletedRows);
        totalSize.addAndGet(-estimatedSizeDecrease);
        lastUpdated = LocalDateTime.now();
    }
    
    /**
     * Record table access for query planning.
     */
    public void recordAccess() {
        accessCount.incrementAndGet();
    }
    
    /**
     * Record full table scan.
     */
    public void recordScan() {
        scanCount.incrementAndGet();
        recordAccess();
    }
    
    /**
     * Add or update column statistics.
     */
    public void updateColumnStatistics(String columnName, ColumnStatistics stats) {
        columnStats.put(columnName, stats);
        lastUpdated = LocalDateTime.now();
    }
    
    /**
     * Get statistics for a specific column.
     */
    public ColumnStatistics getColumnStatistics(String columnName) {
        return columnStats.get(columnName);
    }
    
    /**
     * Estimate selectivity for equality predicates.
     * Returns the fraction of rows that would match a condition.
     */
    public double estimateSelectivity(String columnName, Object value) {
        ColumnStatistics colStats = columnStats.get(columnName);
        if (colStats == null) {
            return 0.1; // Default selectivity when no statistics available
        }
        return colStats.estimateEqualitySelectivity(value);
    }
    
    /**
     * Estimate selectivity for range predicates (>, <, BETWEEN).
     */
    public double estimateRangeSelectivity(String columnName, Object minValue, Object maxValue) {
        ColumnStatistics colStats = columnStats.get(columnName);
        if (colStats == null) {
            return 0.3; // Default range selectivity
        }
        return colStats.estimateRangeSelectivity(minValue, maxValue);
    }
    
    // Getters
    public String getTableName() { return tableName; }
    public long getRowCount() { return rowCount.get(); }
    public long getTotalSize() { return totalSize.get(); }
    public long getAccessCount() { return accessCount.get(); }
    public long getScanCount() { return scanCount.get(); }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    
    /**
     * Get average row size estimate.
     */
    public double getAverageRowSize() {
        long rows = rowCount.get();
        return rows > 0 ? (double) totalSize.get() / rows : 0.0;
    }
    
    /**
     * Check if statistics are stale and need refresh.
     */
    public boolean isStale(int maxAgeMinutes) {
        return lastUpdated.isBefore(LocalDateTime.now().minusMinutes(maxAgeMinutes));
    }
    
    @Override
    public String toString() {
        return String.format("TableStatistics{table='%s', rows=%d, size=%d bytes, columns=%d, updated=%s}",
                tableName, rowCount.get(), totalSize.get(), columnStats.size(), lastUpdated);
    }
}