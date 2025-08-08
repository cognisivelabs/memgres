package com.memgres.sql.ast.expression;

import com.memgres.sql.ast.AstVisitor;

import java.util.Optional;

/**
 * Represents a column reference in SQL (e.g., column_name or table.column_name).
 */
public class ColumnReference extends Expression {
    
    private final Optional<String> tableName;
    private final String columnName;
    
    public ColumnReference(String columnName) {
        this.tableName = Optional.empty();
        this.columnName = columnName;
    }
    
    public ColumnReference(String tableName, String columnName) {
        this.tableName = Optional.of(tableName);
        this.columnName = columnName;
    }
    
    public Optional<String> getTableName() {
        return tableName;
    }
    
    public String getColumnName() {
        return columnName;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitColumnReference(this, context);
    }
    
    @Override
    public String toString() {
        return tableName.map(t -> t + ".").orElse("") + columnName;
    }
}