package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstNode;
import com.memgres.sql.ast.AstVisitor;

import java.util.List;

/**
 * AST node representing a DROP INDEX statement.
 * Supports H2-compatible DROP INDEX syntax.
 */
public class DropIndexStatement extends Statement {
    
    private final boolean ifExists;
    private final String indexName;
    
    public DropIndexStatement(boolean ifExists, String indexName) {
        this.ifExists = ifExists;
        this.indexName = indexName;
    }
    
    public boolean isIfExists() {
        return ifExists;
    }
    
    public String getIndexName() {
        return indexName;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitDropIndexStatement(this, context);
    }
    
}