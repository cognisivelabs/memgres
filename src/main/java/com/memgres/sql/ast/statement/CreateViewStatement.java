package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstVisitor;

import java.util.List;

/**
 * AST node representing a CREATE VIEW statement.
 * 
 * Syntax: CREATE [OR REPLACE] [FORCE] VIEW [IF NOT EXISTS] viewName [(columnList)] AS selectStatement
 */
public class CreateViewStatement extends Statement {
    private final boolean orReplace;
    private final boolean force;
    private final boolean ifNotExists;
    private final String viewName;
    private final List<String> columnNames;
    private final SelectStatement selectStatement;
    
    public CreateViewStatement(boolean orReplace, boolean force, boolean ifNotExists, 
                              String viewName, List<String> columnNames, SelectStatement selectStatement) {
        this.orReplace = orReplace;
        this.force = force;
        this.ifNotExists = ifNotExists;
        this.viewName = viewName;
        this.columnNames = columnNames;
        this.selectStatement = selectStatement;
    }
    
    public boolean isOrReplace() {
        return orReplace;
    }
    
    public boolean isForce() {
        return force;
    }
    
    public boolean isIfNotExists() {
        return ifNotExists;
    }
    
    public String getViewName() {
        return viewName;
    }
    
    public List<String> getColumnNames() {
        return columnNames;
    }
    
    public SelectStatement getSelectStatement() {
        return selectStatement;
    }
    
    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) throws Exception {
        return visitor.visitCreateViewStatement(this, context);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE");
        if (orReplace) {
            sb.append(" OR REPLACE");
        }
        if (force) {
            sb.append(" FORCE");
        }
        sb.append(" VIEW");
        if (ifNotExists) {
            sb.append(" IF NOT EXISTS");
        }
        sb.append(" ").append(viewName);
        if (columnNames != null && !columnNames.isEmpty()) {
            sb.append(" (").append(String.join(", ", columnNames)).append(")");
        }
        sb.append(" AS ").append(selectStatement);
        return sb.toString();
    }
}