package com.memgres.sql.ast.expression;

import com.memgres.sql.ast.AstVisitor;
import com.memgres.sql.ast.statement.SelectStatement;

/**
 * Represents an IN expression with a subquery (e.g., "col IN (SELECT ...)").
 * Tests whether the left expression value exists in the result set of the subquery.
 */
public class InSubqueryExpression extends Expression {
    
    private final Expression expression;
    private final SelectStatement subquery;
    private final boolean negated;
    
    public InSubqueryExpression(Expression expression, SelectStatement subquery, boolean negated) {
        this.expression = expression;
        this.subquery = subquery;
        this.negated = negated;
    }
    
    public Expression getExpression() {
        return expression;
    }
    
    public SelectStatement getSubquery() {
        return subquery;
    }
    
    public boolean isNegated() {
        return negated;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitInSubqueryExpression(this, context);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        InSubqueryExpression that = (InSubqueryExpression) obj;
        return negated == that.negated &&
               expression.equals(that.expression) &&
               subquery.equals(that.subquery);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(expression, subquery, negated);
    }
    
    @Override
    public String toString() {
        return "InSubqueryExpression{" +
                "expression=" + expression +
                ", subquery=" + subquery +
                ", negated=" + negated +
                '}';
    }
}