package com.memgres.storage.btree;

import com.memgres.storage.Table;
import com.memgres.types.Column;
import com.memgres.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * B+ Tree-based index implementation that provides a drop-in replacement
 * for the basic ConcurrentSkipListMap-based Index class.
 * Offers better performance for range queries and large datasets.
 */
public class BPlusTreeIndex {
    private static final Logger logger = LoggerFactory.getLogger(BPlusTreeIndex.class);
    
    private final String name;
    private final Column indexedColumn;
    private final Table table;
    private final int columnIndex;
    private final BPlusTree<Comparable, Long> btree;
    private final ReadWriteLock indexLock;
    
    /**
     * Creates a new B+ tree index
     * @param name the index name
     * @param indexedColumn the column to index
     * @param table the table this index belongs to
     */
    public BPlusTreeIndex(String name, Column indexedColumn, Table table) {
        this(name, indexedColumn, table, 64); // Default B+ tree order
    }
    
    /**
     * Creates a new B+ tree index with specified order
     * @param name the index name
     * @param indexedColumn the column to index
     * @param table the table this index belongs to
     * @param order the B+ tree order (branching factor)
     */
    public BPlusTreeIndex(String name, Column indexedColumn, Table table, int order) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Index name cannot be null or empty");
        }
        if (indexedColumn == null) {
            throw new IllegalArgumentException("Indexed column cannot be null");
        }
        if (table == null) {
            throw new IllegalArgumentException("Table cannot be null");
        }
        if (order < 3) {
            throw new IllegalArgumentException("B+ tree order must be at least 3");
        }
        
        this.name = name.toLowerCase();
        this.indexedColumn = indexedColumn;
        this.table = table;
        this.btree = new BPlusTree<>(order);
        this.indexLock = new ReentrantReadWriteLock();
        
        // Find column index in table
        List<Column> columns = table.getColumns();
        int foundIndex = -1;
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).equals(indexedColumn)) {
                foundIndex = i;
                break;
            }
        }
        
        if (foundIndex == -1) {
            throw new IllegalArgumentException("Column not found in table: " + indexedColumn.getName());
        }
        
        this.columnIndex = foundIndex;
        
        // Build initial index from existing table data
        buildInitialIndex();
        
        logger.debug("Created B+ tree index {} on column {} for table {} with order {}", 
                    name, indexedColumn.getName(), table.getName(), order);
    }
    
    /**
     * Get the index name
     * @return the index name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the indexed column
     * @return the indexed column
     */
    public Column getIndexedColumn() {
        return indexedColumn;
    }
    
    /**
     * Get the table this index belongs to
     * @return the table
     */
    public Table getTable() {
        return table;
    }
    
    /**
     * Get the underlying B+ tree
     * @return the B+ tree
     */
    public BPlusTree<Comparable, Long> getBTree() {
        return btree;
    }
    
    /**
     * Insert a row into the index
     * @param row the row to insert
     */
    public void insert(Row row) {
        Object value = row.getValue(columnIndex);
        if (value == null) {
            return; // Don't index null values
        }
        
        if (!(value instanceof Comparable)) {
            logger.warn("Cannot index non-comparable value: {} for column {}", 
                       value, indexedColumn.getName());
            return;
        }
        
        @SuppressWarnings("unchecked")
        Comparable comparableValue = (Comparable) value;
        
        indexLock.writeLock().lock();
        try {
            btree.insert(comparableValue, row.getId());
            logger.trace("Inserted row {} with value {} into B+ tree index {}", 
                        row.getId(), value, name);
        } finally {
            indexLock.writeLock().unlock();
        }
    }
    
    /**
     * Update a row in the index
     * @param oldRow the old row data
     * @param newRow the new row data
     */
    public void update(Row oldRow, Row newRow) {
        delete(oldRow);
        insert(newRow);
    }
    
    /**
     * Delete a row from the index
     * @param row the row to delete
     */
    public void delete(Row row) {
        Object value = row.getValue(columnIndex);
        if (value == null) {
            return; // Null values are not indexed
        }
        
        if (!(value instanceof Comparable)) {
            return; // Non-comparable values are not indexed
        }
        
        @SuppressWarnings("unchecked")
        Comparable comparableValue = (Comparable) value;
        
        indexLock.writeLock().lock();
        try {
            boolean removed = btree.remove(comparableValue, row.getId());
            if (removed) {
                logger.trace("Deleted row {} with value {} from B+ tree index {}", 
                           row.getId(), value, name);
            }
        } finally {
            indexLock.writeLock().unlock();
        }
    }
    
    /**
     * Find rows with the exact value
     * @param value the value to search for
     * @return set of row IDs matching the value
     */
    public Set<Long> findEqual(Object value) {
        if (value == null || !(value instanceof Comparable)) {
            return Collections.emptySet();
        }
        
        @SuppressWarnings("unchecked")
        Comparable comparableValue = (Comparable) value;
        
        indexLock.readLock().lock();
        try {
            return btree.find(comparableValue);
        } finally {
            indexLock.readLock().unlock();
        }
    }
    
    /**
     * Find rows with values in the specified range (inclusive)
     * @param minValue the minimum value (inclusive)
     * @param maxValue the maximum value (inclusive)
     * @return set of row IDs in the range
     */
    public Set<Long> findRange(Object minValue, Object maxValue) {
        if (minValue == null || maxValue == null || 
            !(minValue instanceof Comparable) || !(maxValue instanceof Comparable)) {
            return Collections.emptySet();
        }
        
        @SuppressWarnings("unchecked")
        Comparable<Object> minComparable = (Comparable<Object>) minValue;
        @SuppressWarnings("unchecked")
        Comparable<Object> maxComparable = (Comparable<Object>) maxValue;
        
        indexLock.readLock().lock();
        try {
            return btree.findRange(minComparable, maxComparable);
        } finally {
            indexLock.readLock().unlock();
        }
    }
    
    /**
     * Find rows with values less than the specified value
     * @param value the upper bound (exclusive)
     * @return set of row IDs less than the value
     */
    public Set<Long> findLessThan(Object value) {
        if (value == null || !(value instanceof Comparable)) {
            return Collections.emptySet();
        }
        
        @SuppressWarnings("unchecked")
        Comparable comparableValue = (Comparable) value;
        
        indexLock.readLock().lock();
        try {
            return btree.findLessThan(comparableValue);
        } finally {
            indexLock.readLock().unlock();
        }
    }
    
    /**
     * Find rows with values greater than the specified value
     * @param value the lower bound (exclusive)
     * @return set of row IDs greater than the value
     */
    public Set<Long> findGreaterThan(Object value) {
        if (value == null || !(value instanceof Comparable)) {
            return Collections.emptySet();
        }
        
        @SuppressWarnings("unchecked")
        Comparable comparableValue = (Comparable) value;
        
        indexLock.readLock().lock();
        try {
            return btree.findGreaterThan(comparableValue);
        } finally {
            indexLock.readLock().unlock();
        }
    }
    
    /**
     * Find rows with values less than or equal to the specified value
     * @param value the upper bound (inclusive)
     * @return set of row IDs less than or equal to the value
     */
    public Set<Long> findLessThanOrEqual(Object value) {
        if (value == null || !(value instanceof Comparable)) {
            return Collections.emptySet();
        }
        
        Set<Long> result = new HashSet<>();
        result.addAll(findLessThan(value));
        result.addAll(findEqual(value));
        return result;
    }
    
    /**
     * Find rows with values greater than or equal to the specified value
     * @param value the lower bound (inclusive)
     * @return set of row IDs greater than or equal to the value
     */
    public Set<Long> findGreaterThanOrEqual(Object value) {
        if (value == null || !(value instanceof Comparable)) {
            return Collections.emptySet();
        }
        
        Set<Long> result = new HashSet<>();
        result.addAll(findGreaterThan(value));
        result.addAll(findEqual(value));
        return result;
    }
    
    /**
     * Get the total number of indexed entries (distinct values)
     * @return the number of distinct values in the index
     */
    public int getEntryCount() {
        indexLock.readLock().lock();
        try {
            return btree.getAllKeys().size();
        } finally {
            indexLock.readLock().unlock();
        }
    }
    
    /**
     * Get the total number of indexed row references
     * @return the total number of row references in the index
     */
    public long getTotalRowCount() {
        indexLock.readLock().lock();
        try {
            return btree.totalValues();
        } finally {
            indexLock.readLock().unlock();
        }
    }
    
    /**
     * Get all keys in the index in sorted order
     * @return list of all keys
     */
    public List<Comparable> getAllKeys() {
        indexLock.readLock().lock();
        try {
            return btree.getAllKeys();
        } finally {
            indexLock.readLock().unlock();
        }
    }
    
    /**
     * Clear all entries from the index
     */
    public void clear() {
        indexLock.writeLock().lock();
        try {
            btree.clear();
            logger.debug("Cleared B+ tree index {}", name);
        } finally {
            indexLock.writeLock().unlock();
        }
    }
    
    /**
     * Get detailed statistics about the index
     * @return map of statistics including B+ tree metrics
     */
    public Map<String, Object> getStatistics() {
        indexLock.readLock().lock();
        try {
            Map<String, Object> stats = new HashMap<>(btree.getStatistics());
            stats.put("name", name);
            stats.put("columnName", indexedColumn.getName());
            stats.put("tableName", table.getName());
            stats.put("columnIndex", columnIndex);
            stats.put("type", "BPlusTreeIndex");
            return stats;
        } finally {
            indexLock.readLock().unlock();
        }
    }
    
    /**
     * Rebuild the entire index from scratch
     */
    public void rebuild() {
        indexLock.writeLock().lock();
        try {
            logger.debug("Rebuilding B+ tree index {}", name);
            btree.clear();
            buildInitialIndex();
            logger.debug("Rebuilt B+ tree index {} with {} entries and {} total row references", 
                        name, getEntryCount(), getTotalRowCount());
        } finally {
            indexLock.writeLock().unlock();
        }
    }
    
    private void buildInitialIndex() {
        logger.debug("Building initial B+ tree index for {} on column {}", name, indexedColumn.getName());
        
        List<Row> allRows = table.getAllRows();
        for (Row row : allRows) {
            insert(row);
        }
        
        logger.debug("Initial B+ tree index built with {} entries and {} total row references", 
                    getEntryCount(), getTotalRowCount());
    }
    
    @Override
    public String toString() {
        indexLock.readLock().lock();
        try {
            return "BPlusTreeIndex{" +
                   "name='" + name + '\'' +
                   ", column='" + indexedColumn.getName() + '\'' +
                   ", table='" + table.getName() + '\'' +
                   ", entries=" + getEntryCount() +
                   ", totalRows=" + getTotalRowCount() +
                   ", btreeStats=" + btree.toString() +
                   '}';
        } finally {
            indexLock.readLock().unlock();
        }
    }
}