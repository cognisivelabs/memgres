package com.memgres.fulltext;

import java.util.Objects;

/**
 * Represents a full-text search result with query and relevance score.
 * Compatible with H2's FT_SEARCH result format.
 */
public class FullTextSearchResult {
    
    private final String query;
    private final float score;
    private final String schemaName;
    private final String tableName;
    
    public FullTextSearchResult(String query, float score, String schemaName, String tableName) {
        this.query = query;
        this.score = score;
        this.schemaName = schemaName;
        this.tableName = tableName;
    }
    
    public String getQuery() {
        return query;
    }
    
    public float getScore() {
        return score;
    }
    
    public String getSchemaName() {
        return schemaName;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        FullTextSearchResult that = (FullTextSearchResult) obj;
        return Float.compare(that.score, score) == 0 &&
               Objects.equals(query, that.query) &&
               Objects.equals(schemaName, that.schemaName) &&
               Objects.equals(tableName, that.tableName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(query, score, schemaName, tableName);
    }
    
    @Override
    public String toString() {
        return "FullTextSearchResult{" +
               "query='" + query + '\'' +
               ", score=" + score +
               ", table=" + schemaName + "." + tableName +
               '}';
    }
}