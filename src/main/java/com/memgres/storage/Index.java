package com.memgres.storage;

import com.memgres.types.Column;
import com.memgres.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Basic index implementation using ConcurrentSkipListMap for efficient range queries.
 * This is a simplified version that will be replaced by B+ tree implementation.
 */
public class Index {
    private static final Logger logger = LoggerFactory.getLogger(Index.class);
    
    private final String name;
    private final Column indexedColumn;
    private final Table table;
    private final int columnIndex;
    private final ConcurrentNavigableMap<Comparable<?>, Set<Long>> indexMap;
    private final ReadWriteLock indexLock;
    
    public Index(String name, Column indexedColumn, Table table) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Index name cannot be null or empty");
        }
        if (indexedColumn == null) {
            throw new IllegalArgumentException("Indexed column cannot be null");
        }
        if (table == null) {
            throw new IllegalArgumentException("Table cannot be null");
        }
        
        this.name = name.toLowerCase();
        this.indexedColumn = indexedColumn;
        this.table = table;
        this.indexMap = new ConcurrentSkipListMap<>();
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
        
        logger.debug("Created index {} on column {} for table {}", 
                    name, indexedColumn.getName(), table.getName());
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
        Comparable<Object> comparableValue = (Comparable<Object>) value;
        
        indexLock.writeLock().lock();
        try {
            Set<Long> rowIds = indexMap.computeIfAbsent(comparableValue, k -> ConcurrentHashMap.newKeySet());
            rowIds.add(row.getId());
            
            logger.trace("Inserted row {} with value {} into index {}", 
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
        Comparable<Object> comparableValue = (Comparable<Object>) value;
        
        indexLock.writeLock().lock();
        try {
            Set<Long> rowIds = indexMap.get(comparableValue);
            if (rowIds != null) {
                rowIds.remove(row.getId());
                if (rowIds.isEmpty()) {
                    indexMap.remove(comparableValue);
                }
                
                logger.trace("Deleted row {} with value {} from index {}", 
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
        Comparable<Object> comparableValue = (Comparable<Object>) value;
        
        indexLock.readLock().lock();
        try {
            Set<Long> rowIds = indexMap.get(comparableValue);
            return rowIds != null ? new HashSet<>(rowIds) : Collections.emptySet();
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
            Set<Long> result = new HashSet<>();
            ConcurrentNavigableMap<Comparable<?>, Set<Long>> subMap = 
                indexMap.subMap(minComparable, true, maxComparable, true);
            
            for (Set<Long> rowIds : subMap.values()) {
                result.addAll(rowIds);
            }
            
            return result;
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
        Comparable<Object> comparableValue = (Comparable<Object>) value;
        
        indexLock.readLock().lock();
        try {
            Set<Long> result = new HashSet<>();
            ConcurrentNavigableMap<Comparable<?>, Set<Long>> headMap = 
                indexMap.headMap(comparableValue, false);
            
            for (Set<Long> rowIds : headMap.values()) {
                result.addAll(rowIds);
            }
            
            return result;
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
        Comparable<Object> comparableValue = (Comparable<Object>) value;
        
        indexLock.readLock().lock();
        try {
            Set<Long> result = new HashSet<>();
            ConcurrentNavigableMap<Comparable<?>, Set<Long>> tailMap = 
                indexMap.tailMap(comparableValue, false);
            
            for (Set<Long> rowIds : tailMap.values()) {
                result.addAll(rowIds);
            }
            
            return result;
        } finally {
            indexLock.readLock().unlock();
        }
    }
    
    /**
     * Get the total number of indexed entries
     * @return the number of distinct values in the index
     */
    public int getEntryCount() {
        indexLock.readLock().lock();
        try {
            return indexMap.size();
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
            return indexMap.values().stream()
                    .mapToLong(Set::size)
                    .sum();
        } finally {
            indexLock.readLock().unlock();
        }
    }
    
    private void buildInitialIndex() {
        logger.debug("Building initial index for {} on column {}", name, indexedColumn.getName());
        
        List<Row> allRows = table.getAllRows();
        for (Row row : allRows) {
            insert(row);
        }
        
        logger.debug("Initial index built with {} entries and {} total row references", 
                    getEntryCount(), getTotalRowCount());
    }
    
    @Override
    public String toString() {
        return "Index{" +
                "name='" + name + '\'' +
                ", column='" + indexedColumn.getName() + '\'' +
                ", table='" + table.getName() + '\'' +
                ", entries=" + getEntryCount() +
                ", totalRows=" + getTotalRowCount() +
                '}';
    }
    
    /**
     * Clear all entries from the index.
     * Used during TRUNCATE TABLE operations.
     */
    public void clear() {
        indexLock.writeLock().lock();
        try {
            indexMap.clear();
            logger.debug("Cleared index {} on column {}", name, indexedColumn.getName());
        } finally {
            indexLock.writeLock().unlock();
        }
    }
}