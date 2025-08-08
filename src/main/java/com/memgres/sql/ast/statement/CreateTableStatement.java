package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstVisitor;

import java.util.List;

/**
 * Represents a CREATE TABLE statement in SQL.
 */
public class CreateTableStatement extends Statement {
    
    private final String tableName;
    private final List<ColumnDefinition> columnDefinitions;
    
    public CreateTableStatement(String tableName, List<ColumnDefinition> columnDefinitions) {
        this.tableName = tableName;
        this.columnDefinitions = columnDefinitions;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public List<ColumnDefinition> getColumnDefinitions() {
        return columnDefinitions;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitCreateTableStatement(this, context);
    }
}