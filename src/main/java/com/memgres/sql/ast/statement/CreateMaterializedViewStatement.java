package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstVisitor;

import java.util.List;

/**
 * AST node representing a CREATE MATERIALIZED VIEW statement.
 * 
 * Syntax: CREATE [OR REPLACE] MATERIALIZED VIEW [IF NOT EXISTS] viewName [(columnList)] AS selectStatement
 */
public class CreateMaterializedViewStatement extends Statement {
    private final boolean orReplace;
    private final boolean ifNotExists;
    private final String viewName;
    private final List<String> columnNames;
    private final SelectStatement selectStatement;
    
    public CreateMaterializedViewStatement(boolean orReplace, boolean ifNotExists, 
                                         String viewName, List<String> columnNames, SelectStatement selectStatement) {
        this.orReplace = orReplace;
        this.ifNotExists = ifNotExists;
        this.viewName = viewName;
        this.columnNames = columnNames;
        this.selectStatement = selectStatement;
    }
    
    public boolean isOrReplace() {
        return orReplace;
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
        return visitor.visitCreateMaterializedViewStatement(this, context);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE");
        if (orReplace) {
            sb.append(" OR REPLACE");
        }
        sb.append(" MATERIALIZED VIEW");
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