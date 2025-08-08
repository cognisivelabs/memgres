package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstNode;
import com.memgres.sql.ast.AstVisitor;
import com.memgres.sql.ast.expression.Expression;

import java.util.Optional;

/**
 * Represents a LIMIT clause in a SELECT statement.
 */
public class LimitClause extends AstNode {
    
    private final Expression limit;
    private final Optional<Expression> offset;
    
    public LimitClause(Expression limit, Optional<Expression> offset) {
        this.limit = limit;
        this.offset = offset;
    }
    
    public Expression getLimit() {
        return limit;
    }
    
    public Optional<Expression> getOffset() {
        return offset;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitLimitClause(this, context);
    }
}