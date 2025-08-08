package com.memgres.sql.ast.expression;

import com.memgres.sql.ast.AstVisitor;
import com.memgres.sql.ast.statement.SelectStatement;

/**
 * Represents a subquery expression that returns a single scalar value.
 * Used in contexts like SELECT lists and WHERE clauses where a single value is expected.
 */
public class SubqueryExpression extends Expression {
    
    private final SelectStatement selectStatement;
    
    public SubqueryExpression(SelectStatement selectStatement) {
        this.selectStatement = selectStatement;
    }
    
    public SelectStatement getSelectStatement() {
        return selectStatement;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitSubqueryExpression(this, context);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SubqueryExpression that = (SubqueryExpression) obj;
        return selectStatement.equals(that.selectStatement);
    }
    
    @Override
    public int hashCode() {
        return selectStatement.hashCode();
    }
    
    @Override
    public String toString() {
        return "SubqueryExpression{" +
                "selectStatement=" + selectStatement +
                '}';
    }
}