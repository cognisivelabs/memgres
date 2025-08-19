package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstVisitor;

/**
 * AST node representing a CREATE SCHEMA statement.
 */
public class CreateSchemaStatement extends Statement {
    private final String schemaName;
    private final boolean ifNotExists;
    
    public CreateSchemaStatement(String schemaName, boolean ifNotExists) {
        this.schemaName = schemaName;
        this.ifNotExists = ifNotExists;
    }
    
    public String getSchemaName() {
        return schemaName;
    }
    
    public boolean isIfNotExists() {
        return ifNotExists;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitCreateSchemaStatement(this, context);
    }
    
    @Override
    public String toString() {
        return "CREATE SCHEMA " + (ifNotExists ? "IF NOT EXISTS " : "") + schemaName;
    }
}