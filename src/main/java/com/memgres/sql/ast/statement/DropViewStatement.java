package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstVisitor;

/**
 * AST node representing a DROP VIEW statement.
 * 
 * Syntax: DROP VIEW [IF EXISTS] viewName [RESTRICT | CASCADE]
 */
public class DropViewStatement extends Statement {
    private final boolean ifExists;
    private final String viewName;
    private final DropOption dropOption;
    
    public DropViewStatement(boolean ifExists, String viewName, DropOption dropOption) {
        this.ifExists = ifExists;
        this.viewName = viewName;
        this.dropOption = dropOption;
    }
    
    public boolean isIfExists() {
        return ifExists;
    }
    
    public String getViewName() {
        return viewName;
    }
    
    public DropOption getDropOption() {
        return dropOption;
    }
    
    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) throws Exception {
        return visitor.visitDropViewStatement(this, context);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DROP VIEW");
        if (ifExists) {
            sb.append(" IF EXISTS");
        }
        sb.append(" ").append(viewName);
        if (dropOption != null) {
            sb.append(" ").append(dropOption.toString());
        }
        return sb.toString();
    }
    
    /**
     * Drop option for CASCADE/RESTRICT behavior.
     */
    public enum DropOption {
        RESTRICT("RESTRICT"),
        CASCADE("CASCADE");
        
        private final String syntax;
        
        DropOption(String syntax) {
            this.syntax = syntax;
        }
        
        @Override
        public String toString() {
            return syntax;
        }
    }
}