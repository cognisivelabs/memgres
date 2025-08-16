package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstVisitor;

/**
 * AST node representing a REFRESH MATERIALIZED VIEW statement.
 * 
 * Syntax: REFRESH MATERIALIZED VIEW viewName
 */
public class RefreshMaterializedViewStatement extends Statement {
    private final String viewName;
    
    public RefreshMaterializedViewStatement(String viewName) {
        this.viewName = viewName;
    }
    
    public String getViewName() {
        return viewName;
    }
    
    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) throws Exception {
        return visitor.visitRefreshMaterializedViewStatement(this, context);
    }
    
    @Override
    public String toString() {
        return "REFRESH MATERIALIZED VIEW " + viewName;
    }
}