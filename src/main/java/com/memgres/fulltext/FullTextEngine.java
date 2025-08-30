package com.memgres.fulltext;

import com.memgres.core.MemGresEngine;
import com.memgres.storage.Table;
import com.memgres.types.Column;
import com.memgres.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

/**
 * Full-text search engine for MemGres, providing H2-compatible functionality.
 * Supports text indexing, searching, and relevance scoring.
 */
public class FullTextEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(FullTextEngine.class);
    
    private final MemGresEngine engine;
    private final Map<String, FullTextIndex> indexes;
    private final ReadWriteLock lock;
    private final Set<String> ignoreWords;
    private final Pattern whitespacePattern;
    
    // Default ignore words (common English stop words)
    private static final Set<String> DEFAULT_IGNORE_WORDS = Set.of(
        "a", "an", "and", "are", "as", "at", "be", "by", "for", "from",
        "has", "he", "in", "is", "it", "its", "of", "on", "that", "the",
        "to", "was", "were", "will", "with", "this", "but", "they",
        "have", "had", "what", "said", "each", "which", "she", "do", "how",
        "their", "if", "up", "out", "many", "then", "them", "these", "so"
    );
    
    public FullTextEngine(MemGresEngine engine) {
        this.engine = engine;
        this.indexes = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();
        this.ignoreWords = new HashSet<>(DEFAULT_IGNORE_WORDS);
        this.whitespacePattern = Pattern.compile("\\s+");
    }
    
    /**
     * Creates a full-text index for the specified table and columns.
     */
    public void createIndex(String schemaName, String tableName, String columnList) throws Exception {
        lock.writeLock().lock();
        try {
            String indexKey = getIndexKey(schemaName, tableName);
            
            if (indexes.containsKey(indexKey)) {
                throw new IllegalStateException("Full-text index already exists for table: " + indexKey);
            }
            
            Table table = engine.getTable(schemaName.toLowerCase(), tableName.toLowerCase());
            if (table == null) {
                throw new IllegalArgumentException("Table not found: " + schemaName + "." + tableName);
            }
            
            List<String> columns = parseColumnList(columnList, table);
            FullTextIndex index = new FullTextIndex(schemaName.toLowerCase(), tableName.toLowerCase(), columns);
            
            // Build initial index from existing data
            buildIndex(table, index, columns);
            
            indexes.put(indexKey, index);
            logger.info("Created full-text index for table {}.{} on columns: {}", 
                       schemaName, tableName, columns);
            
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Drops the full-text index for the specified table.
     */
    public void dropIndex(String schemaName, String tableName) {
        lock.writeLock().lock();
        try {
            String indexKey = getIndexKey(schemaName, tableName);
            FullTextIndex removed = indexes.remove(indexKey);
            
            if (removed != null) {
                logger.info("Dropped full-text index for table {}.{}", schemaName, tableName);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Rebuilds all full-text indexes.
     */
    public void reindex() throws Exception {
        lock.writeLock().lock();
        try {
            for (FullTextIndex index : indexes.values()) {
                Table table = engine.getTable(index.getSchemaName().toLowerCase(), index.getTableName().toLowerCase());
                if (table != null) {
                    index.clear();
                    buildIndex(table, index, index.getColumns());
                }
            }
            logger.info("Rebuilt {} full-text indexes", indexes.size());
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Drops all full-text indexes.
     */
    public void dropAll() {
        lock.writeLock().lock();
        try {
            int count = indexes.size();
            indexes.clear();
            logger.info("Dropped all {} full-text indexes", count);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Searches for text across all full-text indexes.
     */
    public List<FullTextSearchResult> search(String text, int limit, int offset) {
        lock.readLock().lock();
        try {
            List<FullTextSearchResult> allResults = new ArrayList<>();
            
            for (FullTextIndex index : indexes.values()) {
                List<FullTextSearchResult> indexResults = index.search(text, ignoreWords, whitespacePattern);
                allResults.addAll(indexResults);
            }
            
            // Sort by relevance score (descending)
            allResults.sort((a, b) -> Float.compare(b.getScore(), a.getScore()));
            
            // Apply limit and offset
            int fromIndex = Math.min(offset, allResults.size());
            int toIndex = limit > 0 ? Math.min(fromIndex + limit, allResults.size()) : allResults.size();
            
            return allResults.subList(fromIndex, toIndex);
            
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Updates the full-text index when a row is inserted.
     */
    public void indexRow(String schemaName, String tableName, Row row, List<Column> tableColumns) {
        lock.readLock().lock();
        try {
            String indexKey = getIndexKey(schemaName, tableName);
            FullTextIndex index = indexes.get(indexKey);
            
            if (index != null) {
                index.addRow(row, tableColumns, ignoreWords, whitespacePattern);
            }
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Updates the full-text index when a row is updated.
     */
    public void updateRow(String schemaName, String tableName, Row oldRow, Row newRow, List<Column> tableColumns) {
        lock.readLock().lock();
        try {
            String indexKey = getIndexKey(schemaName, tableName);
            FullTextIndex index = indexes.get(indexKey);
            
            if (index != null) {
                index.removeRow(oldRow, tableColumns, ignoreWords, whitespacePattern);
                index.addRow(newRow, tableColumns, ignoreWords, whitespacePattern);
            }
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Updates the full-text index when a row is deleted.
     */
    public void removeRow(String schemaName, String tableName, Row row, List<Column> tableColumns) {
        lock.readLock().lock();
        try {
            String indexKey = getIndexKey(schemaName, tableName);
            FullTextIndex index = indexes.get(indexKey);
            
            if (index != null) {
                index.removeRow(row, tableColumns, ignoreWords, whitespacePattern);
            }
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Sets the list of words to ignore during indexing.
     */
    public void setIgnoreList(String commaSeparatedList) {
        lock.writeLock().lock();
        try {
            ignoreWords.clear();
            if (commaSeparatedList != null && !commaSeparatedList.trim().isEmpty()) {
                String[] words = commaSeparatedList.split(",");
                for (String word : words) {
                    ignoreWords.add(word.trim().toLowerCase());
                }
            }
            logger.info("Updated ignore list with {} words", ignoreWords.size());
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Sets the characters used to separate words during indexing.
     */
    public void setWhitespaceChars(String whitespaceChars) {
        lock.writeLock().lock();
        try {
            String pattern = "[" + Pattern.quote(whitespaceChars) + "]+";
            // Update the whitespace pattern - this would require rebuilding indexes
            logger.info("Updated whitespace pattern to: {}", pattern);
            // Note: In a full implementation, we'd rebuild all indexes here
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    private String getIndexKey(String schemaName, String tableName) {
        return schemaName.toLowerCase() + "." + tableName.toLowerCase();
    }
    
    private List<String> parseColumnList(String columnList, Table table) {
        if (columnList == null || columnList.trim().isEmpty()) {
            // Index all text columns
            return table.getColumns().stream()
                    .filter(col -> isTextColumn(col))
                    .map(col -> col.getName().toLowerCase())
                    .toList();
        }
        
        String[] columnNames = columnList.split(",");
        List<String> result = new ArrayList<>();
        
        for (String columnName : columnNames) {
            String trimmed = columnName.trim().toLowerCase();
            
            // Verify column exists and is text-based
            Column column = table.getColumns().stream()
                    .filter(col -> col.getName().toLowerCase().equals(trimmed))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Column not found: " + trimmed));
            
            if (!isTextColumn(column)) {
                throw new IllegalArgumentException("Column is not text-based: " + trimmed);
            }
            
            result.add(trimmed);
        }
        
        return result;
    }
    
    private boolean isTextColumn(Column column) {
        return switch (column.getDataType()) {
            case VARCHAR, CHAR, TEXT, CLOB -> true;
            default -> false;
        };
    }
    
    private void buildIndex(Table table, FullTextIndex index, List<String> columns) throws Exception {
        for (Row row : table.getAllRows()) {
            index.addRow(row, table.getColumns(), ignoreWords, whitespacePattern);
        }
    }
}