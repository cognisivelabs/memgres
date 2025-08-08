package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstVisitor;
import com.memgres.sql.ast.expression.Expression;

import java.util.List;
import java.util.Optional;

/**
 * Represents an INSERT statement in SQL.
 */
public class InsertStatement extends Statement {
    
    private final String tableName;
    private final Optional<List<String>> columns;
    private final List<List<Expression>> valuesList;
    
    public InsertStatement(String tableName, Optional<List<String>> columns, List<List<Expression>> valuesList) {
        this.tableName = tableName;
        this.columns = columns;
        this.valuesList = valuesList;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public Optional<List<String>> getColumns() {
        return columns;
    }
    
    public List<List<Expression>> getValuesList() {
        return valuesList;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitInsertStatement(this, context);
    }
}