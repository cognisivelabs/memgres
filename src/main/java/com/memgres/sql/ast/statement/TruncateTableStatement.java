package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstVisitor;

/**
 * AST node representing a TRUNCATE TABLE statement.
 * 
 * Syntax: TRUNCATE TABLE tableName [CONTINUE IDENTITY | RESTART IDENTITY]
 */
public class TruncateTableStatement extends Statement {
    private final String tableName;
    private final IdentityOption identityOption;
    
    public TruncateTableStatement(String tableName, IdentityOption identityOption) {
        this.tableName = tableName;
        this.identityOption = identityOption;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public IdentityOption getIdentityOption() {
        return identityOption;
    }
    
    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) throws Exception {
        return visitor.visitTruncateTableStatement(this, context);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TRUNCATE TABLE ").append(tableName);
        if (identityOption != null) {
            sb.append(" ").append(identityOption.toString());
        }
        return sb.toString();
    }
    
    /**
     * Identity option for TRUNCATE TABLE statement.
     */
    public enum IdentityOption {
        CONTINUE_IDENTITY("CONTINUE IDENTITY"),
        RESTART_IDENTITY("RESTART IDENTITY");
        
        private final String syntax;
        
        IdentityOption(String syntax) {
            this.syntax = syntax;
        }
        
        @Override
        public String toString() {
            return syntax;
        }
    }
}