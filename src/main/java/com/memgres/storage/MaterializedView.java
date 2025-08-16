package com.memgres.storage;

import com.memgres.sql.ast.statement.SelectStatement;
import com.memgres.types.Row;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents a materialized view that stores computed results as physical data.
 * Unlike regular views, materialized views cache query results and require explicit refresh.
 */
public class MaterializedView {
    private final String name;
    private final List<String> columnNames;
    private final SelectStatement selectStatement;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    // Materialized data storage
    private volatile ConcurrentHashMap<Long, Row> rows = new ConcurrentHashMap<>();
    private volatile long nextRowId = 1;
    private volatile LocalDateTime lastRefreshTime;
    private volatile boolean isRefreshing = false;
    
    /**
     * Create a new materialized view
     * @param name the materialized view name
     * @param columnNames optional explicit column names (null if not specified)
     * @param selectStatement the SELECT statement that defines the view
     */
    public MaterializedView(String name, List<String> columnNames, SelectStatement selectStatement) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Materialized view name cannot be null or empty");
        }
        if (selectStatement == null) {
            throw new IllegalArgumentException("SELECT statement cannot be null");
        }
        
        this.name = name.toLowerCase(); // H2 converts view names to lowercase
        this.columnNames = columnNames;
        this.selectStatement = selectStatement;
        this.lastRefreshTime = null; // Not refreshed yet
    }
    
    /**
     * Get the materialized view name
     * @return the view name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the explicit column names if specified
     * @return list of column names or null if not specified
     */
    public List<String> getColumnNames() {
        return columnNames;
    }
    
    /**
     * Get the SELECT statement that defines this materialized view
     * @return the SELECT statement
     */
    public SelectStatement getSelectStatement() {
        return selectStatement;
    }
    
    /**
     * Get the last refresh time
     * @return last refresh time or null if never refreshed
     */
    public LocalDateTime getLastRefreshTime() {
        return lastRefreshTime;
    }
    
    /**
     * Check if the materialized view is currently being refreshed
     * @return true if refresh is in progress
     */
    public boolean isRefreshing() {
        return isRefreshing;
    }
    
    /**
     * Check if this materialized view has explicit column names defined
     * @return true if column names are explicitly defined
     */
    public boolean hasExplicitColumns() {
        return columnNames != null && !columnNames.isEmpty();
    }
    
    /**
     * Get all rows from the materialized view
     * @return list of rows
     */
    public List<Row> getAllRows() {
        lock.readLock().lock();
        try {
            return List.copyOf(rows.values());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Get the number of rows in the materialized view
     * @return row count
     */
    public int getRowCount() {
        return rows.size();
    }
    
    /**
     * Refresh the materialized view with new data
     * This method is thread-safe and will block concurrent refreshes
     * @param newRows the new rows to store
     */
    public void refresh(List<Row> newRows) {
        lock.writeLock().lock();
        try {
            isRefreshing = true;
            
            // Clear existing data
            ConcurrentHashMap<Long, Row> newRowMap = new ConcurrentHashMap<>();
            long rowId = 1;
            
            // Add new rows
            for (Row row : newRows) {
                newRowMap.put(rowId++, row);
            }
            
            // Atomically replace the data
            this.rows = newRowMap;
            this.nextRowId = rowId;
            this.lastRefreshTime = LocalDateTime.now();
            
        } finally {
            isRefreshing = false;
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Clear all data from the materialized view
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            rows.clear();
            nextRowId = 1;
            lastRefreshTime = null;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Check if the materialized view has been refreshed
     * @return true if data has been loaded
     */
    public boolean hasData() {
        return lastRefreshTime != null && !rows.isEmpty();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MATERIALIZED VIEW ").append(name);
        if (hasExplicitColumns()) {
            sb.append(" (").append(String.join(", ", columnNames)).append(")");
        }
        sb.append(" AS ").append(selectStatement);
        if (lastRefreshTime != null) {
            sb.append(" [Last refresh: ").append(lastRefreshTime).append("]");
        }
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        MaterializedView that = (MaterializedView) o;
        return Objects.equals(name, that.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}