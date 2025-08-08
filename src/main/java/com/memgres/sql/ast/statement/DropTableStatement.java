package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstVisitor;

/**
 * Represents a DROP TABLE statement in SQL.
 */
public class DropTableStatement extends Statement {
    
    private final String tableName;
    
    public DropTableStatement(String tableName) {
        this.tableName = tableName;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitDropTableStatement(this, context);
    }
}