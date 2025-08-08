package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstNode;
import com.memgres.sql.ast.AstVisitor;
import com.memgres.sql.ast.expression.Expression;

/**
 * Represents a WHERE clause in a SQL statement.
 */
public class WhereClause extends AstNode {
    
    private final Expression condition;
    
    public WhereClause(Expression condition) {
        this.condition = condition;
    }
    
    public Expression getCondition() {
        return condition;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitWhereClause(this, context);
    }
}