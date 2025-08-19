package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstVisitor;

/**
 * AST node representing a DROP SCHEMA statement.
 */
public class DropSchemaStatement extends Statement {
    private final String schemaName;
    private final boolean ifExists;
    private final boolean cascade;
    
    public DropSchemaStatement(String schemaName, boolean ifExists, boolean cascade) {
        this.schemaName = schemaName;
        this.ifExists = ifExists;
        this.cascade = cascade;
    }
    
    public String getSchemaName() {
        return schemaName;
    }
    
    public boolean isIfExists() {
        return ifExists;
    }
    
    public boolean isCascade() {
        return cascade;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitDropSchemaStatement(this, context);
    }
    
    @Override
    public String toString() {
        return "DROP SCHEMA " + (ifExists ? "IF EXISTS " : "") + schemaName + 
               (cascade ? " CASCADE" : "");
    }
}