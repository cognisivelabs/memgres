package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstNode;
import com.memgres.sql.ast.AstVisitor;

import java.util.List;

/**
 * AST node representing a CREATE INDEX statement.
 * Supports H2-compatible CREATE INDEX syntax with all options.
 */
public class CreateIndexStatement extends Statement {
    
    private final boolean unique;
    private final boolean nullsDistinct;
    private final boolean spatial;
    private final boolean ifNotExists;
    private final String indexName;
    private final String tableName;
    private final List<IndexColumn> indexColumns;
    private final List<IndexColumn> includeColumns;
    
    public CreateIndexStatement(boolean unique, boolean nullsDistinct, boolean spatial,
                               boolean ifNotExists, String indexName, String tableName,
                               List<IndexColumn> indexColumns, List<IndexColumn> includeColumns) {
        this.unique = unique;
        this.nullsDistinct = nullsDistinct;
        this.spatial = spatial;
        this.ifNotExists = ifNotExists;
        this.indexName = indexName;
        this.tableName = tableName;
        this.indexColumns = indexColumns;
        this.includeColumns = includeColumns;
    }
    
    public boolean isUnique() {
        return unique;
    }
    
    public boolean isNullsDistinct() {
        return nullsDistinct;
    }
    
    public boolean isSpatial() {
        return spatial;
    }
    
    public boolean isIfNotExists() {
        return ifNotExists;
    }
    
    public String getIndexName() {
        return indexName;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public List<IndexColumn> getIndexColumns() {
        return indexColumns;
    }
    
    public List<IndexColumn> getIncludeColumns() {
        return includeColumns;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitCreateIndexStatement(this, context);
    }
    
    
    /**
     * Represents a single column in an index definition.
     */
    public static class IndexColumn {
        private final String columnName;
        private final SortOrder sortOrder;
        private final NullsOrdering nullsOrdering;
        
        public IndexColumn(String columnName, SortOrder sortOrder, NullsOrdering nullsOrdering) {
            this.columnName = columnName;
            this.sortOrder = sortOrder != null ? sortOrder : SortOrder.ASC;
            this.nullsOrdering = nullsOrdering;
        }
        
        public String getColumnName() {
            return columnName;
        }
        
        public SortOrder getSortOrder() {
            return sortOrder;
        }
        
        public NullsOrdering getNullsOrdering() {
            return nullsOrdering;
        }
    }
    
    public enum SortOrder {
        ASC, DESC
    }
    
    public enum NullsOrdering {
        FIRST, LAST
    }
}