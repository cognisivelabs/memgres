package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstVisitor;

/**
 * AST node representing a DROP MATERIALIZED VIEW statement.
 * 
 * Syntax: DROP MATERIALIZED VIEW [IF EXISTS] viewName [RESTRICT | CASCADE]
 */
public class DropMaterializedViewStatement extends Statement {
    private final boolean ifExists;
    private final String viewName;
    private final RestrictOrCascade restrictOrCascade;
    
    public DropMaterializedViewStatement(boolean ifExists, String viewName, RestrictOrCascade restrictOrCascade) {
        this.ifExists = ifExists;
        this.viewName = viewName;
        this.restrictOrCascade = restrictOrCascade;
    }
    
    public boolean isIfExists() {
        return ifExists;
    }
    
    public String getViewName() {
        return viewName;
    }
    
    public RestrictOrCascade getRestrictOrCascade() {
        return restrictOrCascade;
    }
    
    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) throws Exception {
        return visitor.visitDropMaterializedViewStatement(this, context);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DROP MATERIALIZED VIEW");
        if (ifExists) {
            sb.append(" IF EXISTS");
        }
        sb.append(" ").append(viewName);
        if (restrictOrCascade != null) {
            sb.append(" ").append(restrictOrCascade);
        }
        return sb.toString();
    }
    
    public enum RestrictOrCascade {
        RESTRICT, CASCADE
    }
}