package com.memgres.storage;

import com.memgres.types.Column;
import com.memgres.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents a database table with columns, rows, and indexes.
 * Provides thread-safe operations for data manipulation.
 */
public class Table {
    private static final Logger logger = LoggerFactory.getLogger(Table.class);
    
    private final String name;
    private final List<Column> columns;
    private final Map<String, Column> columnMap;
    private final List<Row> rows;
    private final ConcurrentMap<String, Index> indexes;
    private final ReadWriteLock tableLock;
    private final AtomicLong rowIdGenerator;
    
    public Table(String name, List<Column> columns) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("Table must have at least one column");
        }
        
        this.name = name.toLowerCase();
        this.columns = new ArrayList<>(columns);
        this.columnMap = new HashMap<>();
        this.rows = new ArrayList<>();
        this.indexes = new ConcurrentHashMap<>();
        this.tableLock = new ReentrantReadWriteLock();
        this.rowIdGenerator = new AtomicLong(0);
        
        // Build column map for fast lookup
        for (Column column : columns) {
            columnMap.put(column.getName().toLowerCase(), column);
        }
        
        logger.debug("Created table: {} with {} columns", this.name, columns.size());
    }
    
    /**
     * Get the table name
     * @return the table name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get all columns in the table
     * @return unmodifiable list of columns
     */
    public List<Column> getColumns() {
        return Collections.unmodifiableList(columns);
    }
    
    /**
     * Get a column by name
     * @param columnName the column name
     * @return the column or null if not found
     */
    public Column getColumn(String columnName) {
        if (columnName == null || columnName.trim().isEmpty()) {
            return null;
        }
        return columnMap.get(columnName.toLowerCase());
    }
    
    /**
     * Check if a column exists
     * @param columnName the column name
     * @return true if the column exists
     */
    public boolean hasColumn(String columnName) {
        return getColumn(columnName) != null;
    }
    
    /**
     * Insert a new row into the table
     * @param rowData the data for the new row (values for each column)
     * @return the row ID of the inserted row
     */
    public long insertRow(Object[] rowData) {
        if (rowData == null) {
            throw new IllegalArgumentException("Row data cannot be null");
        }
        if (rowData.length != columns.size()) {
            throw new IllegalArgumentException("Row data length must match column count");
        }
        
        tableLock.writeLock().lock();
        try {
            long rowId = rowIdGenerator.incrementAndGet();
            Row row = new Row(rowId, rowData);
            
            // Validate data types
            validateRowData(row);
            
            rows.add(row);
            
            // Update indexes
            updateIndexesForInsert(row);
            
            logger.debug("Inserted row {} into table {}", rowId, name);
            return rowId;
        } finally {
            tableLock.writeLock().unlock();
        }
    }
    
    /**
     * Update a row in the table
     * @param rowId the ID of the row to update
     * @param newData the new data for the row
     * @return true if the row was updated, false if not found
     */
    public boolean updateRow(long rowId, Object[] newData) {
        if (newData == null) {
            throw new IllegalArgumentException("New data cannot be null");
        }
        if (newData.length != columns.size()) {
            throw new IllegalArgumentException("New data length must match column count");
        }
        
        tableLock.writeLock().lock();
        try {
            for (int i = 0; i < rows.size(); i++) {
                Row row = rows.get(i);
                if (row.getId() == rowId) {
                    Row oldRow = new Row(row.getId(), row.getData().clone());
                    Row newRow = new Row(rowId, newData);
                    
                    // Validate new data types
                    validateRowData(newRow);
                    
                    // Update the row
                    rows.set(i, newRow);
                    
                    // Update indexes
                    updateIndexesForUpdate(oldRow, newRow);
                    
                    logger.debug("Updated row {} in table {}", rowId, name);
                    return true;
                }
            }
            
            logger.warn("Row {} not found in table {}", rowId, name);
            return false;
        } finally {
            tableLock.writeLock().unlock();
        }
    }
    
    /**
     * Delete a row from the table
     * @param rowId the ID of the row to delete
     * @return true if the row was deleted, false if not found
     */
    public boolean deleteRow(long rowId) {
        tableLock.writeLock().lock();
        try {
            Iterator<Row> iterator = rows.iterator();
            while (iterator.hasNext()) {
                Row row = iterator.next();
                if (row.getId() == rowId) {
                    iterator.remove();
                    
                    // Update indexes
                    updateIndexesForDelete(row);
                    
                    logger.debug("Deleted row {} from table {}", rowId, name);
                    return true;
                }
            }
            
            logger.warn("Row {} not found in table {}", rowId, name);
            return false;
        } finally {
            tableLock.writeLock().unlock();
        }
    }
    
    /**
     * Get a row by ID
     * @param rowId the row ID
     * @return the row or null if not found
     */
    public Row getRow(long rowId) {
        tableLock.readLock().lock();
        try {
            for (Row row : rows) {
                if (row.getId() == rowId) {
                    return new Row(row.getId(), row.getData().clone()); // Return copy
                }
            }
            return null;
        } finally {
            tableLock.readLock().unlock();
        }
    }
    
    /**
     * Get all rows in the table
     * @return list of all rows (copies)
     */
    public List<Row> getAllRows() {
        tableLock.readLock().lock();
        try {
            List<Row> result = new ArrayList<>(rows.size());
            for (Row row : rows) {
                result.add(new Row(row.getId(), row.getData().clone()));
            }
            return result;
        } finally {
            tableLock.readLock().unlock();
        }
    }
    
    /**
     * Get the number of rows in the table
     * @return row count
     */
    public int getRowCount() {
        tableLock.readLock().lock();
        try {
            return rows.size();
        } finally {
            tableLock.readLock().unlock();
        }
    }
    
    /**
     * Create an index on a column
     * @param columnName the column to index
     * @param indexName the name of the index
     * @return true if index was created, false if it already exists
     */
    public boolean createIndex(String columnName, String indexName) {
        Column column = getColumn(columnName);
        if (column == null) {
            throw new IllegalArgumentException("Column does not exist: " + columnName);
        }
        
        tableLock.writeLock().lock();
        try {
            if (indexes.containsKey(indexName)) {
                return false;
            }
            
            Index index = new Index(indexName, column, this);
            indexes.put(indexName, index);
            
            logger.debug("Created index {} on column {} for table {}", indexName, columnName, name);
            return true;
        } finally {
            tableLock.writeLock().unlock();
        }
    }
    
    /**
     * Get an index by name
     * @param indexName the index name
     * @return the index or null if not found
     */
    public Index getIndex(String indexName) {
        return indexes.get(indexName);
    }
    
    private void validateRowData(Row row) {
        Object[] data = row.getData();
        for (int i = 0; i < data.length; i++) {
            Column column = columns.get(i);
            Object value = data[i];
            
            if (value == null && !column.isNullable()) {
                throw new IllegalArgumentException("Column " + column.getName() + " cannot be null");
            }
            
            if (value != null && !column.getDataType().isValidValue(value)) {
                throw new IllegalArgumentException("Invalid value for column " + column.getName() + 
                    ": " + value + " (type: " + column.getDataType() + ")");
            }
        }
    }
    
    private void updateIndexesForInsert(Row row) {
        for (Index index : indexes.values()) {
            index.insert(row);
        }
    }
    
    private void updateIndexesForUpdate(Row oldRow, Row newRow) {
        for (Index index : indexes.values()) {
            index.update(oldRow, newRow);
        }
    }
    
    private void updateIndexesForDelete(Row row) {
        for (Index index : indexes.values()) {
            index.delete(row);
        }
    }
    
    /**
     * Insert a row with a specific ID (used for transaction rollback)
     * @param rowId the specific row ID to use
     * @param rowData the row data
     */
    public void insertRowWithId(long rowId, Object[] rowData) {
        if (rowData == null) {
            throw new IllegalArgumentException("Row data cannot be null");
        }
        if (rowData.length != columns.size()) {
            throw new IllegalArgumentException("Row data length must match column count");
        }
        
        tableLock.writeLock().lock();
        try {
            Row row = new Row(rowId, rowData);
            
            // Validate data types
            validateRowData(row);
            
            rows.add(row);
            
            // Update indexes
            updateIndexesForInsert(row);
            
            // Update row ID generator to ensure we don't reuse this ID
            if (rowId >= rowIdGenerator.get()) {
                rowIdGenerator.set(rowId);
            }
            
            logger.debug("Inserted row {} with specific ID into table {}", rowId, name);
        } finally {
            tableLock.writeLock().unlock();
        }
    }
    
    @Override
    public String toString() {
        return "Table{" +
                "name='" + name + '\'' +
                ", columns=" + columns.size() +
                ", rows=" + getRowCount() +
                ", indexes=" + indexes.size() +
                '}';
    }
}