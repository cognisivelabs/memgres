package com.memgres.sql.ast.expression;

import com.memgres.sql.ast.AstVisitor;

import java.util.List;
import java.util.Optional;

/**
 * Represents a CASE expression in SQL.
 */
public class CaseExpression extends Expression {
    
    public static class WhenClause {
        private final Expression condition;
        private final Expression result;
        
        public WhenClause(Expression condition, Expression result) {
            this.condition = condition;
            this.result = result;
        }
        
        public Expression getCondition() {
            return condition;
        }
        
        public Expression getResult() {
            return result;
        }
    }
    
    private final List<WhenClause> whenClauses;
    private final Optional<Expression> elseExpression;
    
    public CaseExpression(List<WhenClause> whenClauses, Optional<Expression> elseExpression) {
        this.whenClauses = whenClauses;
        this.elseExpression = elseExpression;
    }
    
    public List<WhenClause> getWhenClauses() {
        return whenClauses;
    }
    
    public Optional<Expression> getElseExpression() {
        return elseExpression;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitCaseExpression(this, context);
    }
}