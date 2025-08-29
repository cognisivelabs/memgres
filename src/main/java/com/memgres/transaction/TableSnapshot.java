package com.memgres.transaction;

import com.memgres.types.Row;

import java.util.List;
import java.util.Map;

/**
 * Represents a snapshot of table state at a specific point in time.
 * Used for savepoint rollback operations.
 * 
 * <p>This is a simplified implementation that captures the complete table state.
 * In a full implementation, this would use more efficient incremental snapshots
 * or transaction logs.</p>
 */
public class TableSnapshot {
    
    private final String schemaName;
    private final String tableName;
    private final List<Row> rows;
    private final long snapshotTime;
    
    /**
     * Create a snapshot of a table's current state.
     * 
     * @param schemaName the schema name
     * @param tableName the table name
     * @param rows the current rows in the table (will be copied)
     */
    public TableSnapshot(String schemaName, String tableName, List<Row> rows) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.rows = List.copyOf(rows); // Immutable copy
        this.snapshotTime = System.currentTimeMillis();
    }
    
    /**
     * Get the schema name.
     * 
     * @return the schema name
     */
    public String getSchemaName() {
        return schemaName;
    }
    
    /**
     * Get the table name.
     * 
     * @return the table name
     */
    public String getTableName() {
        return tableName;
    }
    
    /**
     * Get the rows that were in the table at snapshot time.
     * 
     * @return immutable list of rows
     */
    public List<Row> getRows() {
        return rows;
    }
    
    /**
     * Get the snapshot timestamp.
     * 
     * @return the time when this snapshot was created
     */
    public long getSnapshotTime() {
        return snapshotTime;
    }
    
    /**
     * Get the table key for this snapshot.
     * 
     * @return schema.table identifier
     */
    public String getTableKey() {
        return schemaName + "." + tableName;
    }
    
    @Override
    public String toString() {
        return String.format("TableSnapshot{%s.%s, %d rows, time=%d}", 
            schemaName, tableName, rows.size(), snapshotTime);
    }
}