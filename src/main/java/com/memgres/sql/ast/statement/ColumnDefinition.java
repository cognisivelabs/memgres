package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstNode;
import com.memgres.sql.ast.AstVisitor;

import java.util.List;

/**
 * Represents a column definition in a CREATE TABLE statement.
 */
public class ColumnDefinition extends AstNode {
    
    public enum Constraint {
        NOT_NULL, NULL
    }
    
    private final String columnName;
    private final DataTypeNode dataType;
    private final List<Constraint> constraints;
    
    public ColumnDefinition(String columnName, DataTypeNode dataType, List<Constraint> constraints) {
        this.columnName = columnName;
        this.dataType = dataType;
        this.constraints = constraints;
    }
    
    public String getColumnName() {
        return columnName;
    }
    
    public DataTypeNode getDataType() {
        return dataType;
    }
    
    public List<Constraint> getConstraints() {
        return constraints;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitColumnDefinition(this, context);
    }
}