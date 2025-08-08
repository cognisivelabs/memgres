package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstVisitor;

import java.util.Optional;

/**
 * Represents a DELETE statement in SQL.
 */
public class DeleteStatement extends Statement {
    
    private final String tableName;
    private final Optional<WhereClause> whereClause;
    
    public DeleteStatement(String tableName, Optional<WhereClause> whereClause) {
        this.tableName = tableName;
        this.whereClause = whereClause;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public Optional<WhereClause> getWhereClause() {
        return whereClause;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitDeleteStatement(this, context);
    }
}