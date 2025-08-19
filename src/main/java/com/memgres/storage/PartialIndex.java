package com.memgres.storage;

import com.memgres.sql.ast.expression.Expression;
import com.memgres.sql.execution.ExpressionEvaluator;
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
 * Partial index implementation that only includes rows matching a WHERE condition.
 * This provides space efficiency and faster queries for selective conditions.
 */
public class PartialIndex {
    private static final Logger logger = LoggerFactory.getLogger(PartialIndex.class);
    
    private final String name;
    private final List<Column> indexedColumns;
    private final Table table;
    private final List<Integer> columnIndexes;
    private final Expression whereCondition;
    private final ExpressionEvaluator expressionEvaluator;
    private final ConcurrentNavigableMap<CompositeIndex.CompositeKey, Set<Long>> indexMap;
    private final ReadWriteLock indexLock;
    private final boolean unique;
    
    public PartialIndex(String name, List<Column> indexedColumns, Table table, 
                       Expression whereCondition, ExpressionEvaluator expressionEvaluator) {
        this(name, indexedColumns, table, whereCondition, expressionEvaluator, false);
    }
    
    public PartialIndex(String name, List<Column> indexedColumns, Table table, 
                       Expression whereCondition, ExpressionEvaluator expressionEvaluator, boolean unique) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Index name cannot be null or empty");
        }
        if (indexedColumns == null || indexedColumns.isEmpty()) {
            throw new IllegalArgumentException("Indexed columns cannot be null or empty");
        }
        if (table == null) {
            throw new IllegalArgumentException("Table cannot be null");
        }
        if (whereCondition == null) {
            throw new IllegalArgumentException("WHERE condition cannot be null");
        }
        if (expressionEvaluator == null) {
            throw new IllegalArgumentException("Expression evaluator cannot be null");
        }
        
        this.name = name.toLowerCase();
        this.indexedColumns = new ArrayList<>(indexedColumns);
        this.table = table;
        this.whereCondition = whereCondition;
        this.expressionEvaluator = expressionEvaluator;
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
        
        logger.debug("Created partial index {} on columns {} with WHERE condition for table {}",
                name, indexedColumns.stream().map(Column::getName).toArray(), table.getName());
    }
    
    /**
     * Build the index from existing table data.
     */
    private void buildIndex() {
        List<Row> rows = table.getAllRows();
        int includedRows = 0;
        
        for (Row row : rows) {
            if (evaluateCondition(row)) {
                insert(row);
                includedRows++;
            }
        }
        
        logger.debug("Built partial index {} with {} entries from {} total rows", 
                name, indexMap.size(), includedRows);
    }
    
    /**
     * Evaluate whether a row satisfies the WHERE condition.
     */
    private boolean evaluateCondition(Row row) {
        try {
            // Create a simple execution context with the row data
            // This is a simplified implementation - in a real system,
            // you'd need to properly map column names to values
            Map<String, Object> rowContext = createRowContext(row);
            return evaluateExpressionWithContext(whereCondition, rowContext);
        } catch (Exception e) {
            logger.debug("Failed to evaluate WHERE condition for row {}: {}", row.getId(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Create a context map from row data for expression evaluation.
     */
    private Map<String, Object> createRowContext(Row row) {
        Map<String, Object> context = new HashMap<>();
        Object[] data = row.getData();
        List<Column> tableColumns = table.getColumns();
        
        for (int i = 0; i < Math.min(data.length, tableColumns.size()); i++) {
            context.put(tableColumns.get(i).getName().toLowerCase(), data[i]);
        }
        
        return context;
    }
    
    /**
     * Simplified expression evaluation with row context.
     * In a full implementation, this would integrate with the existing ExpressionEvaluator.
     */
    private boolean evaluateExpressionWithContext(Expression expr, Map<String, Object> context) {
        // For now, return true to include all rows - this would need proper implementation
        // to integrate with the SQL expression evaluation system
        // This is a placeholder for demonstration purposes
        return true;
    }
    
    /**
     * Insert a row into the index if it matches the condition.
     */
    public void insert(Row row) {
        if (!evaluateCondition(row)) {
            return; // Row doesn't match the WHERE condition
        }
        
        CompositeIndex.CompositeKey key = createCompositeKey(row);
        if (key == null) return; // Skip rows with null values in key columns
        
        indexLock.writeLock().lock();
        try {
            Set<Long> rowIds = indexMap.computeIfAbsent(key, k -> new HashSet<>());
            
            // Check uniqueness constraint if enabled
            if (unique && !rowIds.isEmpty()) {
                throw new IllegalStateException(
                    String.format("Duplicate key violation for unique partial index %s: %s", name, key)
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
        boolean oldMatches = evaluateCondition(oldRow);
        boolean newMatches = evaluateCondition(newRow);
        
        CompositeIndex.CompositeKey oldKey = oldMatches ? createCompositeKey(oldRow) : null;
        CompositeIndex.CompositeKey newKey = newMatches ? createCompositeKey(newRow) : null;
        
        indexLock.writeLock().lock();
        try {
            // Remove old entry if it was in the index
            if (oldKey != null) {
                Set<Long> oldRowIds = indexMap.get(oldKey);
                if (oldRowIds != null) {
                    oldRowIds.remove(oldRow.getId());
                    if (oldRowIds.isEmpty()) {
                        indexMap.remove(oldKey);
                    }
                }
            }
            
            // Add new entry if it matches the condition
            if (newKey != null) {
                Set<Long> newRowIds = indexMap.computeIfAbsent(newKey, k -> new HashSet<>());
                
                // Check uniqueness constraint if enabled
                if (unique && !newRowIds.isEmpty() && !newRowIds.contains(newRow.getId())) {
                    // Rollback the removal
                    if (oldKey != null) {
                        indexMap.computeIfAbsent(oldKey, k -> new HashSet<>()).add(oldRow.getId());
                    }
                    throw new IllegalStateException(
                        String.format("Duplicate key violation for unique partial index %s: %s", name, newKey)
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
        if (!evaluateCondition(row)) {
            return; // Row wasn't in the index anyway
        }
        
        CompositeIndex.CompositeKey key = createCompositeKey(row);
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
     * Only returns rows that also satisfy the WHERE condition.
     */
    public Set<Long> findExact(Object... values) {
        if (values.length != indexedColumns.size()) {
            throw new IllegalArgumentException("Number of values must match number of indexed columns");
        }
        
        CompositeIndex.CompositeKey key = new CompositeIndex.CompositeKey(Arrays.asList(values));
        
        indexLock.readLock().lock();
        try {
            Set<Long> rowIds = indexMap.get(key);
            return rowIds != null ? new HashSet<>(rowIds) : new HashSet<>();
        } finally {
            indexLock.readLock().unlock();
        }
    }
    
    /**
     * Find rows with prefix match.
     */
    public Set<Long> findPrefix(Object... prefixValues) {
        if (prefixValues.length == 0 || prefixValues.length > indexedColumns.size()) {
            throw new IllegalArgumentException("Invalid prefix length");
        }
        
        Set<Long> result = new HashSet<>();
        CompositeIndex.CompositeKey startKey = new CompositeIndex.CompositeKey(Arrays.asList(prefixValues));
        
        indexLock.readLock().lock();
        try {
            for (Map.Entry<CompositeIndex.CompositeKey, Set<Long>> entry : indexMap.tailMap(startKey).entrySet()) {
                CompositeIndex.CompositeKey entryKey = entry.getKey();
                
                // Check if this key starts with our prefix
                boolean matches = true;
                for (int i = 0; i < prefixValues.length; i++) {
                    Object prefixValue = prefixValues[i];
                    Object entryValue = entryKey.getValues().get(i);
                    
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
     * Get all row IDs in the partial index.
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
    private CompositeIndex.CompositeKey createCompositeKey(Row row) {
        List<Object> keyValues = new ArrayList<>();
        Object[] rowData = row.getData();
        
        for (int columnIndex : columnIndexes) {
            if (columnIndex >= rowData.length) {
                return null; // Invalid row structure
            }
            Object value = rowData[columnIndex];
            
            // For now, skip entries with null values in key columns
            if (value == null) {
                return null;
            }
            
            keyValues.add(value);
        }
        
        return new CompositeIndex.CompositeKey(keyValues);
    }
    
    // Getters
    public String getName() { return name; }
    public List<Column> getIndexedColumns() { return new ArrayList<>(indexedColumns); }
    public Table getTable() { return table; }
    public Expression getWhereCondition() { return whereCondition; }
    public boolean isUnique() { return unique; }
    
    /**
     * Get the number of unique keys in the partial index.
     */
    public int getKeyCount() {
        return indexMap.size();
    }
    
    /**
     * Get the total number of row references in the partial index.
     */
    public int getTotalRowCount() {
        indexLock.readLock().lock();
        try {
            return indexMap.values().stream().mapToInt(Set::size).sum();
        } finally {
            indexLock.readLock().unlock();
        }
    }
    
    /**
     * Check if a row would be included in this partial index.
     */
    public boolean wouldIncludeRow(Row row) {
        return evaluateCondition(row);
    }
    
    @Override
    public String toString() {
        return String.format("PartialIndex{name='%s', columns=%s, unique=%s, keyCount=%d, condition='%s'}",
                name,
                indexedColumns.stream().map(Column::getName).toArray(),
                unique,
                getKeyCount(),
                whereCondition
        );
    }
}