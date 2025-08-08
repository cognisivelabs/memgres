package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstNode;
import com.memgres.sql.ast.AstVisitor;
import com.memgres.sql.ast.expression.Expression;

/**
 * Represents a HAVING clause in a SELECT statement.
 */
public class HavingClause extends AstNode {
    
    private final Expression condition;
    
    public HavingClause(Expression condition) {
        this.condition = condition;
    }
    
    public Expression getCondition() {
        return condition;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitHavingClause(this, context);
    }
}