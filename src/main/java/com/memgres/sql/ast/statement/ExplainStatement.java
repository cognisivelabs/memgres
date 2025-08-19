package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstVisitor;

/**
 * AST node representing an EXPLAIN statement for query plans.
 */
public class ExplainStatement extends Statement {
    private final Statement targetStatement;
    
    public ExplainStatement(Statement targetStatement) {
        this.targetStatement = targetStatement;
    }
    
    public Statement getTargetStatement() {
        return targetStatement;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitExplainStatement(this, context);
    }
    
    @Override
    public String toString() {
        return "EXPLAIN " + targetStatement.toString();
    }
}