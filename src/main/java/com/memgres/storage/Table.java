package com.memgres.storage;

import com.memgres.types.Column;
import com.memgres.types.Row;
import com.memgres.storage.statistics.StatisticsManager;
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
    private final ConcurrentMap<String, CompositeIndex> compositeIndexes;
    private final ReadWriteLock tableLock;
    private final AtomicLong rowIdGenerator;
    private volatile StatisticsManager statisticsManager;
    
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
        this.compositeIndexes = new ConcurrentHashMap<>();
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
            updateCompositeIndexesForInsert(row);
            
            // Update statistics if available
            if (statisticsManager != null) {
                statisticsManager.updateTableStatistics(name, this);
            }
            
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
                    updateCompositeIndexesForUpdate(oldRow, newRow);
                    
                    // Update statistics if available
                    if (statisticsManager != null) {
                        statisticsManager.updateTableStatistics(name, this);
                    }
                    
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
                    updateCompositeIndexesForDelete(row);
                    
                    // Update statistics if available
                    if (statisticsManager != null) {
                        statisticsManager.updateTableStatistics(name, this);
                    }
                    
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
     * Get an index by name (single column index only)
     * @param indexName the index name
     * @return the index or null if not found
     */
    public Index getIndex(String indexName) {
        return indexes.get(indexName);
    }
    
    /**
     * Check if an index exists (checks both single and composite indexes)
     * @param indexName the index name
     * @return true if the index exists
     */
    public boolean hasIndex(String indexName) {
        return indexes.containsKey(indexName) || compositeIndexes.containsKey(indexName);
    }
    
    /**
     * Get all index names (both single and composite indexes)
     * @return set of all index names
     */
    public Set<String> getIndexNames() {
        Set<String> allIndexNames = new HashSet<>();
        allIndexNames.addAll(indexes.keySet());
        allIndexNames.addAll(compositeIndexes.keySet());
        return allIndexNames;
    }
    
    /**
     * Create an index with multiple columns and H2-compatible options
     * @param indexName the name of the index (can be null for auto-generated name)
     * @param columnNames the columns to index
     * @param unique whether the index should enforce uniqueness
     * @param ifNotExists whether to skip creation if index already exists
     * @return true if index was created, false if it already exists and ifNotExists is true
     * @throws IllegalArgumentException if column doesn't exist or index already exists (and ifNotExists is false)
     */
    public boolean createIndex(String indexName, List<String> columnNames, boolean unique, boolean ifNotExists) {
        if (columnNames == null || columnNames.isEmpty()) {
            throw new IllegalArgumentException("Index must have at least one column");
        }
        
        // Validate all columns exist
        List<Column> indexColumns = new ArrayList<>();
        for (String columnName : columnNames) {
            Column column = getColumn(columnName);
            if (column == null) {
                throw new IllegalArgumentException("Column does not exist: " + columnName);
            }
            indexColumns.add(column);
        }
        
        // Generate index name if not provided
        if (indexName == null || indexName.trim().isEmpty()) {
            indexName = generateIndexName(columnNames);
            logger.debug("Generated index name: {} for columns: {}", indexName, columnNames);
        }
        
        tableLock.writeLock().lock();
        try {
            // Check if index already exists in either collection
            if (indexes.containsKey(indexName) || compositeIndexes.containsKey(indexName)) {
                if (ifNotExists) {
                    logger.debug("Index {} already exists, skipping creation due to IF NOT EXISTS", indexName);
                    return false;
                } else {
                    throw new IllegalArgumentException("Index already exists: " + indexName);
                }
            }
            
            // Create appropriate index type based on number of columns
            if (indexColumns.size() == 1) {
                // Single column index - use simple Index
                Column column = indexColumns.get(0);
                Index index = new Index(indexName, column, this);
                indexes.put(indexName, index);
            } else {
                // Multi-column index - use CompositeIndex
                CompositeIndex compositeIndex = new CompositeIndex(indexName, indexColumns, this, unique);
                compositeIndexes.put(indexName, compositeIndex);
            }
            
            logger.debug("Created{} index {} on columns {} for table {}", 
                         unique ? " unique" : "", indexName, columnNames, name);
            return true;
        } finally {
            tableLock.writeLock().unlock();
        }
    }
    
    /**
     * Generate a default index name based on table and column names
     */
    private String generateIndexName(List<String> columnNames) {
        StringBuilder nameBuilder = new StringBuilder("idx_");
        nameBuilder.append(name);
        for (String columnName : columnNames) {
            nameBuilder.append("_").append(columnName);
        }
        return nameBuilder.toString();
    }
    
    /**
     * Drop an index by name
     * @param indexName the name of the index to drop
     * @return true if index was dropped, false if it didn't exist
     */
    public boolean dropIndex(String indexName) {
        tableLock.writeLock().lock();
        try {
            // Try to remove from single-column indexes first
            Index removedIndex = indexes.remove(indexName);
            if (removedIndex != null) {
                logger.debug("Dropped index {} from table {}", indexName, name);
                return true;
            }
            
            // Try to remove from composite indexes
            CompositeIndex removedCompositeIndex = compositeIndexes.remove(indexName);
            if (removedCompositeIndex != null) {
                logger.debug("Dropped composite index {} from table {}", indexName, name);
                return true;
            }
            
            return false;
        } finally {
            tableLock.writeLock().unlock();
        }
    }
    
    /**
     * Drop an index by name with IF EXISTS option
     * @param indexName the name of the index to drop
     * @param ifExists whether to skip error if index doesn't exist
     * @return true if index was dropped, false if it didn't exist and ifExists is true
     * @throws IllegalArgumentException if index doesn't exist and ifExists is false
     */
    public boolean dropIndex(String indexName, boolean ifExists) {
        tableLock.writeLock().lock();
        try {
            // Try to remove from single-column indexes first
            Index removedIndex = indexes.remove(indexName);
            if (removedIndex != null) {
                logger.debug("Dropped index {} from table {}", indexName, name);
                return true;
            }
            
            // Try to remove from composite indexes
            CompositeIndex removedCompositeIndex = compositeIndexes.remove(indexName);
            if (removedCompositeIndex != null) {
                logger.debug("Dropped composite index {} from table {}", indexName, name);
                return true;
            }
            
            // Index not found in either collection
            if (ifExists) {
                logger.debug("Index {} does not exist, skipping drop due to IF EXISTS", indexName);
                return false;
            } else {
                throw new IllegalArgumentException("Index does not exist: " + indexName);
            }
        } finally {
            tableLock.writeLock().unlock();
        }
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
            updateCompositeIndexesForInsert(row);
            
            // Update row ID generator to ensure we don't reuse this ID
            if (rowId >= rowIdGenerator.get()) {
                rowIdGenerator.set(rowId);
            }
            
            logger.debug("Inserted row {} with specific ID into table {}", rowId, name);
        } finally {
            tableLock.writeLock().unlock();
        }
    }
    
    /**
     * Add a new column to the table.
     * All existing rows will be extended with NULL value for the new column.
     * 
     * @param column the column to add
     * @param position the position to insert the column (null for end)
     * @param referenceColumnName column name to insert before/after (null if position is FIRST or DEFAULT)
     * @return true if column was added successfully
     */
    public boolean addColumn(Column column, String position, String referenceColumnName) {
        if (column == null) {
            throw new IllegalArgumentException("Column cannot be null");
        }
        
        String columnName = column.getName().toLowerCase();
        
        tableLock.writeLock().lock();
        try {
            // Check if column already exists
            if (columnMap.containsKey(columnName)) {
                throw new IllegalArgumentException("Column already exists: " + columnName);
            }
            
            int insertIndex = columns.size(); // Default: add at end
            
            // Determine insertion position
            if (position != null) {
                switch (position.toUpperCase()) {
                    case "FIRST":
                        insertIndex = 0;
                        break;
                    case "BEFORE":
                        if (referenceColumnName != null) {
                            insertIndex = findColumnIndex(referenceColumnName);
                            if (insertIndex == -1) {
                                throw new IllegalArgumentException("Reference column not found: " + referenceColumnName);
                            }
                        }
                        break;
                    case "AFTER":
                        if (referenceColumnName != null) {
                            insertIndex = findColumnIndex(referenceColumnName);
                            if (insertIndex == -1) {
                                throw new IllegalArgumentException("Reference column not found: " + referenceColumnName);
                            }
                            insertIndex++; // Insert after the reference column
                        }
                        break;
                }
            }
            
            // Add column to the appropriate position
            columns.add(insertIndex, column);
            columnMap.put(columnName, column);
            
            // Update all existing rows with NULL value for the new column
            for (int i = 0; i < rows.size(); i++) {
                Row row = rows.get(i);
                Object[] oldData = row.getData();
                Object[] newData = new Object[oldData.length + 1];
                
                // Copy data up to insertion point
                System.arraycopy(oldData, 0, newData, 0, insertIndex);
                
                // Insert NULL value for new column
                newData[insertIndex] = null;
                
                // Copy remaining data after insertion point
                if (insertIndex < oldData.length) {
                    System.arraycopy(oldData, insertIndex, newData, insertIndex + 1, oldData.length - insertIndex);
                }
                
                // Replace the row with updated data
                rows.set(i, new Row(row.getId(), newData));
            }
            
            logger.info("Added column {} to table {} at position {}", columnName, name, insertIndex);
            return true;
            
        } finally {
            tableLock.writeLock().unlock();
        }
    }
    
    /**
     * Remove a column from the table.
     * All existing rows will have the column data removed.
     * 
     * @param columnName the name of the column to remove
     * @return true if column was removed successfully
     */
    public boolean removeColumn(String columnName) {
        if (columnName == null || columnName.trim().isEmpty()) {
            throw new IllegalArgumentException("Column name cannot be null or empty");
        }
        
        String normalizedColumnName = columnName.toLowerCase();
        
        tableLock.writeLock().lock();
        try {
            // Check if column exists
            if (!columnMap.containsKey(normalizedColumnName)) {
                throw new IllegalArgumentException("Column does not exist: " + columnName);
            }
            
            // Cannot drop all columns - table must have at least one column
            if (columns.size() <= 1) {
                throw new IllegalArgumentException("Cannot drop the last column from table");
            }
            
            // Find column index
            int columnIndex = findColumnIndex(columnName);
            if (columnIndex == -1) {
                throw new IllegalArgumentException("Column not found: " + columnName);
            }
            
            // Remove column from list and map
            columns.remove(columnIndex);
            columnMap.remove(normalizedColumnName);
            
            // Update all existing rows to remove the column data
            for (int i = 0; i < rows.size(); i++) {
                Row row = rows.get(i);
                Object[] oldData = row.getData();
                Object[] newData = new Object[oldData.length - 1];
                
                // Copy data before the removed column
                System.arraycopy(oldData, 0, newData, 0, columnIndex);
                
                // Copy data after the removed column
                if (columnIndex < oldData.length - 1) {
                    System.arraycopy(oldData, columnIndex + 1, newData, columnIndex, oldData.length - columnIndex - 1);
                }
                
                // Replace the row with updated data
                rows.set(i, new Row(row.getId(), newData));
            }
            
            // Remove any indexes on this column
            indexes.entrySet().removeIf(entry -> {
                Index index = entry.getValue();
                if (index.getIndexedColumn().getName().equalsIgnoreCase(columnName)) {
                    logger.debug("Removed index {} because it was on dropped column {}", entry.getKey(), columnName);
                    return true;
                }
                return false;
            });
            
            logger.info("Removed column {} from table {}", columnName, name);
            return true;
            
        } finally {
            tableLock.writeLock().unlock();
        }
    }
    
    /**
     * Rename a column in the table.
     * 
     * @param oldColumnName current name of the column
     * @param newColumnName new name for the column
     * @return true if column was renamed successfully
     */
    public boolean renameColumn(String oldColumnName, String newColumnName) {
        if (oldColumnName == null || oldColumnName.trim().isEmpty()) {
            throw new IllegalArgumentException("Old column name cannot be null or empty");
        }
        if (newColumnName == null || newColumnName.trim().isEmpty()) {
            throw new IllegalArgumentException("New column name cannot be null or empty");
        }
        
        String normalizedOldName = oldColumnName.toLowerCase();
        String normalizedNewName = newColumnName.toLowerCase();
        
        tableLock.writeLock().lock();
        try {
            // Check if old column exists
            Column column = columnMap.get(normalizedOldName);
            if (column == null) {
                throw new IllegalArgumentException("Column does not exist: " + oldColumnName);
            }
            
            // Check if new column name already exists
            if (columnMap.containsKey(normalizedNewName)) {
                throw new IllegalArgumentException("Column already exists: " + newColumnName);
            }
            
            // Create new column with updated name
            Column renamedColumn = new Column.Builder()
                .name(newColumnName)
                .dataType(column.getDataType())
                .nullable(column.isNullable())
                .primaryKey(column.isPrimaryKey())
                .unique(column.isUnique())
                .build();
            
            // Update column in list
            int columnIndex = findColumnIndex(oldColumnName);
            columns.set(columnIndex, renamedColumn);
            
            // Update column map
            columnMap.remove(normalizedOldName);
            columnMap.put(normalizedNewName, renamedColumn);
            
            logger.info("Renamed column {} to {} in table {}", oldColumnName, newColumnName, name);
            return true;
            
        } finally {
            tableLock.writeLock().unlock();
        }
    }
    
    /**
     * Find the index of a column by name.
     * 
     * @param columnName the column name to find
     * @return the index of the column, or -1 if not found
     */
    private int findColumnIndex(String columnName) {
        String normalizedName = columnName.toLowerCase();
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).getName().toLowerCase().equals(normalizedName)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Truncate the table by removing all rows.
     * This operation cannot be rolled back and is faster than DELETE.
     * 
     * @param restartIdentity if true, reset identity sequences to their start value
     */
    public void truncate(boolean restartIdentity) {
        tableLock.writeLock().lock();
        try {
            int rowCount = rows.size();
            
            // Clear all data
            rows.clear();
            
            // Clear all indexes by removing and recreating them
            // This is a simple approach since Index doesn't have a clear method
            Map<String, Index> indexesCopy = new HashMap<>(indexes);
            indexes.clear();
            for (Map.Entry<String, Index> entry : indexesCopy.entrySet()) {
                Index oldIndex = entry.getValue();
                Index newIndex = new Index(oldIndex.getName(), oldIndex.getIndexedColumn(), this);
                indexes.put(entry.getKey(), newIndex);
            }
            
            // Reset identity/sequence columns if requested
            if (restartIdentity) {
                // Reset row ID generator
                rowIdGenerator.set(0);
                logger.info("RESTART IDENTITY applied for table {} - row ID generator reset", name);
            } else {
                logger.info("CONTINUE IDENTITY applied for table {} - row ID generator preserved", name);
            }
            
            // Update statistics to reflect empty table
            if (statisticsManager != null) {
                statisticsManager.updateTableStatistics(name, this);
            }
            
            logger.info("Truncated table {} (restart identity: {}): {} rows removed", name, restartIdentity, rowCount);
            
        } finally {
            tableLock.writeLock().unlock();
        }
    }
    
    /**
     * Truncate the table by removing all rows
     * @return the number of rows that were removed
     */
    public int truncate() {
        tableLock.writeLock().lock();
        try {
            int rowCount = rows.size();
            
            // Clear all rows
            rows.clear();
            
            // Clear all indexes by removing and recreating them
            Map<String, Index> indexesCopy = new HashMap<>(indexes);
            indexes.clear();
            for (Map.Entry<String, Index> entry : indexesCopy.entrySet()) {
                Index oldIndex = entry.getValue();
                Index newIndex = new Index(oldIndex.getName(), oldIndex.getIndexedColumn(), this);
                indexes.put(entry.getKey(), newIndex);
            }
            
            // Reset row ID generator
            rowIdGenerator.set(0);
            
            // Update statistics to reflect empty table
            if (statisticsManager != null) {
                statisticsManager.updateTableStatistics(name, this);
            }
            
            logger.info("Truncated table {}: {} rows removed", name, rowCount);
            return rowCount;
        } finally {
            tableLock.writeLock().unlock();
        }
    }
    
    /**
     * Set the statistics manager for this table.
     */
    public void setStatisticsManager(StatisticsManager statisticsManager) {
        this.statisticsManager = statisticsManager;
    }
    
    /**
     * Get the statistics manager for this table.
     */
    public StatisticsManager getStatisticsManager() {
        return statisticsManager;
    }
    
    /**
     * Trigger statistics update for this table.
     */
    public void updateStatistics() {
        if (statisticsManager != null) {
            statisticsManager.updateTableStatistics(name, this);
        }
    }
    
    /**
     * Force immediate statistics update.
     */
    public void forceUpdateStatistics() {
        if (statisticsManager != null) {
            statisticsManager.forceUpdateStatistics(name, this);
        }
    }
    
    /**
     * Create a composite index on multiple columns.
     */
    public boolean createCompositeIndex(String indexName, List<String> columnNames, boolean unique, boolean ifNotExists) {
        if (columnNames == null || columnNames.isEmpty()) {
            throw new IllegalArgumentException("Composite index must have at least one column");
        }
        
        // Validate all columns exist
        List<Column> indexColumns = new ArrayList<>();
        for (String columnName : columnNames) {
            Column column = getColumn(columnName);
            if (column == null) {
                throw new IllegalArgumentException("Column does not exist: " + columnName);
            }
            indexColumns.add(column);
        }
        
        // Generate index name if not provided
        if (indexName == null || indexName.trim().isEmpty()) {
            indexName = "idx_composite_" + name + "_" + String.join("_", columnNames);
        }
        
        tableLock.writeLock().lock();
        try {
            if (compositeIndexes.containsKey(indexName)) {
                if (ifNotExists) {
                    logger.debug("Composite index {} already exists, skipping creation due to IF NOT EXISTS", indexName);
                    return false;
                } else {
                    throw new IllegalArgumentException("Composite index already exists: " + indexName);
                }
            }
            
            CompositeIndex compositeIndex = new CompositeIndex(indexName, indexColumns, this, unique);
            compositeIndexes.put(indexName, compositeIndex);
            
            logger.debug("Created{} composite index {} on columns {} for table {}",
                         unique ? " unique" : "", indexName, columnNames, name);
            return true;
        } finally {
            tableLock.writeLock().unlock();
        }
    }
    
    /**
     * Get a composite index by name.
     */
    public CompositeIndex getCompositeIndex(String indexName) {
        return compositeIndexes.get(indexName);
    }
    
    /**
     * Drop a composite index by name.
     */
    public boolean dropCompositeIndex(String indexName, boolean ifExists) {
        tableLock.writeLock().lock();
        try {
            CompositeIndex removed = compositeIndexes.remove(indexName);
            if (removed != null) {
                logger.debug("Dropped composite index {} from table {}", indexName, name);
                return true;
            } else if (ifExists) {
                logger.debug("Composite index {} does not exist, skipping drop due to IF EXISTS", indexName);
                return false;
            } else {
                throw new IllegalArgumentException("Composite index does not exist: " + indexName);
            }
        } finally {
            tableLock.writeLock().unlock();
        }
    }
    
    /**
     * Get all composite indexes.
     */
    public Map<String, CompositeIndex> getAllCompositeIndexes() {
        return new HashMap<>(compositeIndexes);
    }
    
    private void updateCompositeIndexesForInsert(Row row) {
        for (CompositeIndex compositeIndex : compositeIndexes.values()) {
            compositeIndex.insert(row);
        }
    }
    
    private void updateCompositeIndexesForUpdate(Row oldRow, Row newRow) {
        for (CompositeIndex compositeIndex : compositeIndexes.values()) {
            compositeIndex.update(oldRow, newRow);
        }
    }
    
    private void updateCompositeIndexesForDelete(Row row) {
        for (CompositeIndex compositeIndex : compositeIndexes.values()) {
            compositeIndex.delete(row);
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