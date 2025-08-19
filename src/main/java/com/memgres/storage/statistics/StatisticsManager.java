package com.memgres.storage.statistics;

import com.memgres.storage.Table;
import com.memgres.types.Column;
import com.memgres.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages statistics collection and maintenance for database tables.
 * Provides cost-based query planning with up-to-date statistics.
 */
public class StatisticsManager {
    
    private static final Logger logger = LoggerFactory.getLogger(StatisticsManager.class);
    
    private final Map<String, TableStatistics> tableStats;
    private final ScheduledExecutorService statsUpdateExecutor;
    private final int statsRefreshIntervalMinutes;
    private volatile boolean autoUpdateEnabled;
    
    public StatisticsManager() {
        this(30); // Default: refresh every 30 minutes
    }
    
    public StatisticsManager(int refreshIntervalMinutes) {
        this.tableStats = new ConcurrentHashMap<>();
        this.statsRefreshIntervalMinutes = refreshIntervalMinutes;
        this.autoUpdateEnabled = true;
        this.statsUpdateExecutor = Executors.newScheduledThreadPool(2, r -> {
            Thread thread = new Thread(r, "StatisticsUpdater");
            thread.setDaemon(true);
            return thread;
        });
        
        // Schedule automatic statistics refresh
        if (refreshIntervalMinutes > 0) {
            statsUpdateExecutor.scheduleAtFixedRate(
                this::refreshStaleStatistics,
                refreshIntervalMinutes,
                refreshIntervalMinutes,
                TimeUnit.MINUTES
            );
        }
    }
    
    /**
     * Get or create table statistics.
     */
    public TableStatistics getTableStatistics(String tableName) {
        return tableStats.computeIfAbsent(tableName, TableStatistics::new);
    }
    
    /**
     * Update statistics for a table based on current data.
     */
    public void updateTableStatistics(String tableName, Table table) {
        if (!autoUpdateEnabled) {
            return;
        }
        
        try {
            TableStatistics stats = getTableStatistics(tableName);
            
            // Get current table data
            List<Row> rows = table.getAllRows();
            List<Column> columns = table.getColumns();
            
            // Update row count and estimated size
            long estimatedSize = calculateEstimatedSize(rows, columns);
            stats.updateAfterInsert(rows.size() - (int) stats.getRowCount(), 
                                   estimatedSize - stats.getTotalSize());
            
            // Update column statistics
            for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
                Column column = columns.get(colIndex);
                List<Object> columnValues = extractColumnValues(rows, colIndex);
                
                ColumnStatistics colStats = new ColumnStatistics(column.getName());
                colStats.updateFromData(columnValues);
                stats.updateColumnStatistics(column.getName(), colStats);
            }
            
            logger.debug("Updated statistics for table {}: {}", tableName, stats);
            
        } catch (Exception e) {
            logger.error("Failed to update statistics for table {}: {}", tableName, e.getMessage(), e);
        }
    }
    
    /**
     * Force immediate statistics update for a table.
     */
    public void forceUpdateStatistics(String tableName, Table table) {
        boolean wasAutoUpdate = autoUpdateEnabled;
        autoUpdateEnabled = true;
        try {
            updateTableStatistics(tableName, table);
        } finally {
            autoUpdateEnabled = wasAutoUpdate;
        }
    }
    
    /**
     * Record table access for query planning metrics.
     */
    public void recordTableAccess(String tableName) {
        TableStatistics stats = tableStats.get(tableName);
        if (stats != null) {
            stats.recordAccess();
        }
    }
    
    /**
     * Record full table scan for optimization decisions.
     */
    public void recordTableScan(String tableName) {
        TableStatistics stats = tableStats.get(tableName);
        if (stats != null) {
            stats.recordScan();
        }
    }
    
    /**
     * Estimate the cost of a table scan.
     */
    public double estimateScanCost(String tableName) {
        TableStatistics stats = tableStats.get(tableName);
        if (stats == null) {
            return 1000.0; // Default high cost for unknown tables
        }
        
        // Cost is based on row count and average row size
        long rowCount = stats.getRowCount();
        double avgRowSize = stats.getAverageRowSize();
        
        // Base cost formula: rows * (1 + size_factor)
        double sizeFactor = Math.log(Math.max(avgRowSize, 1.0)) / 100.0;
        return rowCount * (1.0 + sizeFactor);
    }
    
    /**
     * Estimate the cost of an index lookup.
     */
    public double estimateIndexCost(String tableName, String columnName, double selectivity) {
        TableStatistics stats = tableStats.get(tableName);
        if (stats == null) {
            return 100.0; // Default moderate cost
        }
        
        long rowCount = stats.getRowCount();
        
        // Index cost is logarithmic lookup + linear scan of matching rows
        double indexLookupCost = Math.log(Math.max(rowCount, 1.0)) / Math.log(2.0);
        double scanCost = rowCount * selectivity * 0.1; // Index scan is faster
        
        return indexLookupCost + scanCost;
    }
    
    /**
     * Get selectivity estimate for equality predicate.
     */
    public double getSelectivity(String tableName, String columnName, Object value) {
        TableStatistics stats = tableStats.get(tableName);
        if (stats == null) {
            return 0.1; // Default selectivity
        }
        return stats.estimateSelectivity(columnName, value);
    }
    
    /**
     * Get selectivity estimate for range predicate.
     */
    public double getRangeSelectivity(String tableName, String columnName, Object minValue, Object maxValue) {
        TableStatistics stats = tableStats.get(tableName);
        if (stats == null) {
            return 0.3; // Default range selectivity
        }
        return stats.estimateRangeSelectivity(columnName, minValue, maxValue);
    }
    
    /**
     * Refresh statistics that are older than the configured interval.
     */
    private void refreshStaleStatistics() {
        if (!autoUpdateEnabled) {
            return;
        }
        
        logger.debug("Checking for stale statistics...");
        int refreshed = 0;
        
        for (TableStatistics stats : tableStats.values()) {
            if (stats.isStale(statsRefreshIntervalMinutes)) {
                logger.debug("Statistics for table {} are stale, scheduling refresh", stats.getTableName());
                // Note: Would need access to Table objects to refresh
                // For now, just log - this would be enhanced with table registry
                refreshed++;
            }
        }
        
        if (refreshed > 0) {
            logger.info("Found {} tables with stale statistics", refreshed);
        }
    }
    
    /**
     * Extract column values from rows for statistics calculation.
     */
    private List<Object> extractColumnValues(List<Row> rows, int columnIndex) {
        List<Object> values = new ArrayList<>(rows.size());
        for (Row row : rows) {
            if (columnIndex < row.getData().length) {
                values.add(row.getValue(columnIndex));
            }
        }
        return values;
    }
    
    /**
     * Calculate estimated size of rows in bytes.
     */
    private long calculateEstimatedSize(List<Row> rows, List<Column> columns) {
        if (rows.isEmpty()) {
            return 0;
        }
        
        long totalSize = 0;
        for (Row row : rows) {
            totalSize += estimateRowSize(row, columns);
        }
        
        return totalSize;
    }
    
    /**
     * Estimate size of a single row in bytes.
     */
    private long estimateRowSize(Row row, List<Column> columns) {
        long size = 8; // Row overhead
        
        Object[] values = row.getData();
        for (int i = 0; i < values.length && i < columns.size(); i++) {
            Object value = values[i];
            if (value == null) {
                size += 1; // Null marker
            } else if (value instanceof String) {
                size += ((String) value).length() * 2; // UTF-16 estimate
            } else if (value instanceof Integer) {
                size += 4;
            } else if (value instanceof Long) {
                size += 8;
            } else if (value instanceof Double) {
                size += 8;
            } else if (value instanceof Boolean) {
                size += 1;
            } else {
                size += 16; // Default object overhead
            }
        }
        
        return size;
    }
    
    /**
     * Enable or disable automatic statistics updates.
     */
    public void setAutoUpdateEnabled(boolean enabled) {
        this.autoUpdateEnabled = enabled;
        logger.info("Automatic statistics updates {}", enabled ? "enabled" : "disabled");
    }
    
    /**
     * Get all table statistics for monitoring.
     */
    public Map<String, TableStatistics> getAllStatistics() {
        return new ConcurrentHashMap<>(tableStats);
    }
    
    /**
     * Clear statistics for a table (e.g., after table drop).
     */
    public void clearStatistics(String tableName) {
        tableStats.remove(tableName);
        logger.debug("Cleared statistics for table {}", tableName);
    }
    
    /**
     * Shutdown the statistics manager.
     */
    public void shutdown() {
        autoUpdateEnabled = false;
        if (statsUpdateExecutor != null && !statsUpdateExecutor.isShutdown()) {
            statsUpdateExecutor.shutdown();
            try {
                if (!statsUpdateExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    statsUpdateExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                statsUpdateExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        logger.info("Statistics manager shutdown complete");
    }
}