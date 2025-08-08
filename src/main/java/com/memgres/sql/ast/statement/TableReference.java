package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstNode;
import com.memgres.sql.ast.AstVisitor;

import java.util.Optional;

/**
 * Represents a table reference in a FROM clause.
 */
public class TableReference extends AstNode {
    
    private final String tableName;
    private final Optional<String> alias;
    
    public TableReference(String tableName, Optional<String> alias) {
        this.tableName = tableName;
        this.alias = alias;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public Optional<String> getAlias() {
        return alias;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitTableReference(this, context);
    }
}