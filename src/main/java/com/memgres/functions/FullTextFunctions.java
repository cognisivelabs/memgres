package com.memgres.functions;

import com.memgres.core.MemGresEngine;
import com.memgres.fulltext.FullTextEngine;
import com.memgres.fulltext.FullTextSearchResult;
import com.memgres.sql.execution.SqlExecutionResult;
import com.memgres.types.Column;
import com.memgres.types.DataType;
import com.memgres.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * H2-compatible Full-Text Search functions for MemGres.
 * Provides FT_INIT, FT_CREATE_INDEX, FT_SEARCH, and related functionality.
 */
public class FullTextFunctions {
    
    private static final Logger logger = LoggerFactory.getLogger(FullTextFunctions.class);
    
    private static FullTextEngine fullTextEngine;
    
    /**
     * Initializes full-text search functionality.
     * H2 compatible: FT_INIT()
     */
    public static void ftInit(MemGresEngine engine) throws Exception {
        if (fullTextEngine == null) {
            fullTextEngine = new FullTextEngine(engine);
            logger.info("Full-text search engine initialized");
        }
    }
    
    /**
     * Creates a full-text index for a table.
     * H2 compatible: FT_CREATE_INDEX(schema, table, columnList)
     */
    public static void ftCreateIndex(String schema, String table, String columnList) throws Exception {
        if (fullTextEngine == null) {
            throw new IllegalStateException("Full-text search not initialized. Call FT_INIT() first.");
        }
        
        fullTextEngine.createIndex(schema, table, columnList);
    }
    
    /**
     * Drops the full-text index for a table.
     * H2 compatible: FT_DROP_INDEX(schema, table)
     */
    public static void ftDropIndex(String schema, String table) {
        if (fullTextEngine != null) {
            fullTextEngine.dropIndex(schema, table);
        }
    }
    
    /**
     * Rebuilds all full-text indexes.
     * H2 compatible: FT_REINDEX()
     */
    public static void ftReindex() throws Exception {
        if (fullTextEngine != null) {
            fullTextEngine.reindex();
        }
    }
    
    /**
     * Drops all full-text indexes.
     * H2 compatible: FT_DROP_ALL()
     */
    public static void ftDropAll() {
        if (fullTextEngine != null) {
            fullTextEngine.dropAll();
        }
    }
    
    /**
     * Searches full-text indexes and returns results.
     * H2 compatible: FT_SEARCH(text, limit, offset)
     */
    public static SqlExecutionResult ftSearch(String text, int limit, int offset) {
        if (fullTextEngine == null) {
            throw new IllegalStateException("Full-text search not initialized. Call FT_INIT() first.");
        }
        
        List<FullTextSearchResult> results = fullTextEngine.search(text, limit, offset);
        
        // Create result set compatible with H2 format
        List<Column> columns = List.of(
            new Column.Builder().name("QUERY").dataType(DataType.VARCHAR).nullable(false).build(),
            new Column.Builder().name("SCORE").dataType(DataType.REAL).nullable(false).build()
        );
        
        List<Row> rows = results.stream()
                .map(result -> new Row(0L, new Object[]{result.getQuery(), result.getScore()}))
                .toList();
        
        return new SqlExecutionResult(columns, rows);
    }
    
    /**
     * Sets the list of words to ignore during indexing.
     * H2 compatible: FT_SET_IGNORE_LIST(commaSeparatedList)
     */
    public static void ftSetIgnoreList(String commaSeparatedList) {
        if (fullTextEngine != null) {
            fullTextEngine.setIgnoreList(commaSeparatedList);
        }
    }
    
    /**
     * Sets the characters used to separate words during indexing.
     * H2 compatible: FT_SET_WHITESPACE_CHARS(whitespaceChars)
     */
    public static void ftSetWhitespaceChars(String whitespaceChars) {
        if (fullTextEngine != null) {
            fullTextEngine.setWhitespaceChars(whitespaceChars);
        }
    }
    
    /**
     * Updates full-text index when a row is inserted.
     */
    public static void onRowInserted(String schema, String table, Row row, List<Column> columns) {
        if (fullTextEngine != null) {
            fullTextEngine.indexRow(schema, table, row, columns);
        }
    }
    
    /**
     * Updates full-text index when a row is updated.
     */
    public static void onRowUpdated(String schema, String table, Row oldRow, Row newRow, List<Column> columns) {
        if (fullTextEngine != null) {
            fullTextEngine.updateRow(schema, table, oldRow, newRow, columns);
        }
    }
    
    /**
     * Updates full-text index when a row is deleted.
     */
    public static void onRowDeleted(String schema, String table, Row row, List<Column> columns) {
        if (fullTextEngine != null) {
            fullTextEngine.removeRow(schema, table, row, columns);
        }
    }
    
    /**
     * Resets the full-text engine (for testing purposes).
     */
    public static void reset() {
        fullTextEngine = null;
    }
}