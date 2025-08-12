package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstVisitor;

/**
 * AST node representing a DROP SEQUENCE statement.
 * Supports H2-compatible DROP SEQUENCE syntax.
 * 
 * H2 Syntax:
 * DROP SEQUENCE [IF EXISTS] sequenceName
 */
public class DropSequenceStatement extends Statement {
    
    private final boolean ifExists;
    private final String sequenceName;
    
    public DropSequenceStatement(boolean ifExists, String sequenceName) {
        this.ifExists = ifExists;
        this.sequenceName = sequenceName;
    }
    
    public boolean isIfExists() {
        return ifExists;
    }
    
    public String getSequenceName() {
        return sequenceName;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitDropSequenceStatement(this, context);
    }
}