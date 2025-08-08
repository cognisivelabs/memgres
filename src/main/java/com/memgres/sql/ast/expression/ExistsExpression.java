package com.memgres.sql.ast.expression;

import com.memgres.sql.ast.AstVisitor;
import com.memgres.sql.ast.statement.SelectStatement;

/**
 * Represents an EXISTS expression that tests for the presence of rows in a subquery.
 * Returns true if the subquery returns at least one row, false otherwise.
 */
public class ExistsExpression extends Expression {
    
    private final SelectStatement subquery;
    
    public ExistsExpression(SelectStatement subquery) {
        this.subquery = subquery;
    }
    
    public SelectStatement getSubquery() {
        return subquery;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitExistsExpression(this, context);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ExistsExpression that = (ExistsExpression) obj;
        return subquery.equals(that.subquery);
    }
    
    @Override
    public int hashCode() {
        return subquery.hashCode();
    }
    
    @Override
    public String toString() {
        return "ExistsExpression{" +
                "subquery=" + subquery +
                '}';
    }
}