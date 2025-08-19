package com.memgres.storage;

import com.memgres.types.Column;
import com.memgres.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Composite index implementation supporting multiple columns.
 * Uses a hierarchical structure for efficient multi-column lookups.
 */
public class CompositeIndex {
    private static final Logger logger = LoggerFactory.getLogger(CompositeIndex.class);
    
    private final String name;
    private final List<Column> indexedColumns;
    private final Table table;
    private final List<Integer> columnIndexes;
    private final ConcurrentNavigableMap<CompositeKey, Set<Long>> indexMap;
    private final ReadWriteLock indexLock;
    private final boolean unique;
    
    public CompositeIndex(String name, List<Column> indexedColumns, Table table) {
        this(name, indexedColumns, table, false);
    }
    
    public CompositeIndex(String name, List<Column> indexedColumns, Table table, boolean unique) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Index name cannot be null or empty");
        }
        if (indexedColumns == null || indexedColumns.isEmpty()) {
            throw new IllegalArgumentException("Indexed columns cannot be null or empty");
        }
        if (table == null) {
            throw new IllegalArgumentException("Table cannot be null");
        }
        
        this.name = name.toLowerCase();
        this.indexedColumns = new ArrayList<>(indexedColumns);
        this.table = table;
        this.unique = unique;
        this.indexMap = new ConcurrentSkipListMap<>();
        this.indexLock = new ReentrantReadWriteLock();
        this.columnIndexes = new ArrayList<>();
        
        // Find column indexes in table
        List<Column> tableColumns = table.getColumns();
        for (Column indexColumn : indexedColumns) {
            int foundIndex = -1;
            for (int i = 0; i < tableColumns.size(); i++) {
                if (tableColumns.get(i).getName().equalsIgnoreCase(indexColumn.getName())) {
                    foundIndex = i;
                    break;
                }
            }
            if (foundIndex == -1) {
                throw new IllegalArgumentException("Column not found in table: " + indexColumn.getName());
            }
            columnIndexes.add(foundIndex);
        }
        
        // Build index from existing table data
        buildIndex();
        
        logger.debug("Created composite index {} on columns {} for table {}",
                name, indexedColumns.stream().map(Column::getName).toArray(), table.getName());
    }
    
    /**
     * Build the index from existing table data.
     */
    private void buildIndex() {
        List<Row> rows = table.getAllRows();
        for (Row row : rows) {
            insert(row);
        }
        logger.debug("Built composite index {} with {} entries", name, indexMap.size());
    }
    
    /**
     * Insert a row into the index.
     */
    public void insert(Row row) {
        CompositeKey key = createCompositeKey(row);
        if (key == null) return; // Skip rows with null values in key columns
        
        indexLock.writeLock().lock();
        try {
            Set<Long> rowIds = indexMap.computeIfAbsent(key, k -> new HashSet<>());
            
            // Check uniqueness constraint if enabled
            if (unique && !rowIds.isEmpty()) {
                throw new IllegalStateException(
                    String.format("Duplicate key violation for unique index %s: %s", name, key)
                );
            }
            
            rowIds.add(row.getId());
        } finally {
            indexLock.writeLock().unlock();
        }
    }
    
    /**
     * Update a row in the index.
     */
    public void update(Row oldRow, Row newRow) {
        CompositeKey oldKey = createCompositeKey(oldRow);
        CompositeKey newKey = createCompositeKey(newRow);
        
        indexLock.writeLock().lock();
        try {
            // Remove old entry
            if (oldKey != null) {
                Set<Long> oldRowIds = indexMap.get(oldKey);
                if (oldRowIds != null) {
                    oldRowIds.remove(oldRow.getId());
                    if (oldRowIds.isEmpty()) {
                        indexMap.remove(oldKey);
                    }
                }
            }
            
            // Add new entry
            if (newKey != null) {
                Set<Long> newRowIds = indexMap.computeIfAbsent(newKey, k -> new HashSet<>());
                
                // Check uniqueness constraint if enabled
                if (unique && !newRowIds.isEmpty() && !newRowIds.contains(newRow.getId())) {
                    // Rollback the removal
                    if (oldKey != null) {
                        indexMap.computeIfAbsent(oldKey, k -> new HashSet<>()).add(oldRow.getId());
                    }
                    throw new IllegalStateException(
                        String.format("Duplicate key violation for unique index %s: %s", name, newKey)
                    );
                }
                
                newRowIds.add(newRow.getId());
            }
        } finally {
            indexLock.writeLock().unlock();
        }
    }
    
    /**
     * Delete a row from the index.
     */
    public void delete(Row row) {
        CompositeKey key = createCompositeKey(row);
        if (key == null) return;
        
        indexLock.writeLock().lock();
        try {
            Set<Long> rowIds = indexMap.get(key);
            if (rowIds != null) {
                rowIds.remove(row.getId());
                if (rowIds.isEmpty()) {
                    indexMap.remove(key);
                }
            }
        } finally {
            indexLock.writeLock().unlock();
        }
    }
    
    /**
     * Find rows with exact match on all indexed columns.
     */
    public Set<Long> findExact(Object... values) {
        if (values.length != indexedColumns.size()) {
            throw new IllegalArgumentException("Number of values must match number of indexed columns");
        }
        
        CompositeKey key = new CompositeKey(Arrays.asList(values));
        
        indexLock.readLock().lock();
        try {
            Set<Long> rowIds = indexMap.get(key);
            return rowIds != null ? new HashSet<>(rowIds) : new HashSet<>();
        } finally {
            indexLock.readLock().unlock();
        }
    }
    
    /**
     * Find rows with prefix match (useful for range queries on first few columns).
     */
    public Set<Long> findPrefix(Object... prefixValues) {
        if (prefixValues.length == 0 || prefixValues.length > indexedColumns.size()) {
            throw new IllegalArgumentException("Invalid prefix length");
        }
        
        Set<Long> result = new HashSet<>();
        CompositeKey startKey = new CompositeKey(Arrays.asList(prefixValues));
        
        indexLock.readLock().lock();
        try {
            for (Map.Entry<CompositeKey, Set<Long>> entry : indexMap.tailMap(startKey).entrySet()) {
                CompositeKey entryKey = entry.getKey();
                
                // Check if this key starts with our prefix
                boolean matches = true;
                for (int i = 0; i < prefixValues.length; i++) {
                    Object prefixValue = prefixValues[i];
                    Object entryValue = entryKey.values.get(i);
                    
                    if (!Objects.equals(prefixValue, entryValue)) {
                        matches = false;
                        break;
                    }
                }
                
                if (matches) {
                    result.addAll(entry.getValue());
                } else {
                    // Since the map is sorted, once we find a non-match, we're done
                    break;
                }
            }
        } finally {
            indexLock.readLock().unlock();
        }
        
        return result;
    }
    
    /**
     * Get all row IDs in the index (full scan).
     */
    public Set<Long> getAllRowIds() {
        indexLock.readLock().lock();
        try {
            Set<Long> allRowIds = new HashSet<>();
            for (Set<Long> rowIds : indexMap.values()) {
                allRowIds.addAll(rowIds);
            }
            return allRowIds;
        } finally {
            indexLock.readLock().unlock();
        }
    }
    
    /**
     * Create a composite key from a row's data.
     */
    private CompositeKey createCompositeKey(Row row) {
        List<Object> keyValues = new ArrayList<>();
        Object[] rowData = row.getData();
        
        for (int columnIndex : columnIndexes) {
            if (columnIndex >= rowData.length) {
                return null; // Invalid row structure
            }
            Object value = rowData[columnIndex];
            
            // For now, skip entries with null values in key columns
            // This can be made configurable later
            if (value == null) {
                return null;
            }
            
            keyValues.add(value);
        }
        
        return new CompositeKey(keyValues);
    }
    
    // Getters
    public String getName() { return name; }
    public List<Column> getIndexedColumns() { return new ArrayList<>(indexedColumns); }
    public Table getTable() { return table; }
    public boolean isUnique() { return unique; }
    
    /**
     * Get the number of unique keys in the index.
     */
    public int getKeyCount() {
        return indexMap.size();
    }
    
    /**
     * Get the total number of row references in the index.
     */
    public int getTotalRowCount() {
        indexLock.readLock().lock();
        try {
            return indexMap.values().stream().mapToInt(Set::size).sum();
        } finally {
            indexLock.readLock().unlock();
        }
    }
    
    @Override
    public String toString() {
        return String.format("CompositeIndex{name='%s', columns=%s, unique=%s, keyCount=%d}",
                name,
                indexedColumns.stream().map(Column::getName).toArray(),
                unique,
                getKeyCount()
        );
    }
    
    /**
     * Represents a composite key made up of multiple values.
     */
    public static class CompositeKey implements Comparable<CompositeKey> {
        private final List<Object> values;
        private final int hashCode;
        
        public CompositeKey(List<Object> values) {
            this.values = new ArrayList<>(values);
            this.hashCode = this.values.hashCode();
        }
        
        @Override
        public int compareTo(CompositeKey other) {
            if (other == null) return 1;
            
            int minSize = Math.min(values.size(), other.values.size());
            
            for (int i = 0; i < minSize; i++) {
                Object thisValue = values.get(i);
                Object otherValue = other.values.get(i);
                
                int result = compareValues(thisValue, otherValue);
                if (result != 0) {
                    return result;
                }
            }
            
            // If all compared values are equal, compare by size
            return Integer.compare(values.size(), other.values.size());
        }
        
        @SuppressWarnings("unchecked")
        private int compareValues(Object a, Object b) {
            if (a == null && b == null) return 0;
            if (a == null) return -1;
            if (b == null) return 1;
            
            // Both are non-null, try to compare
            if (a instanceof Comparable && b instanceof Comparable && 
                a.getClass().equals(b.getClass())) {
                return ((Comparable<Object>) a).compareTo(b);
            }
            
            // Fall back to string comparison
            return a.toString().compareTo(b.toString());
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof CompositeKey)) return false;
            CompositeKey other = (CompositeKey) obj;
            return Objects.equals(values, other.values);
        }
        
        @Override
        public int hashCode() {
            return hashCode;
        }
        
        @Override
        public String toString() {
            return values.toString();
        }
        
        public List<Object> getValues() {
            return new ArrayList<>(values);
        }
    }
}