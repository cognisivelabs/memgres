package com.memgres.sql.ast.expression;

import com.memgres.sql.ast.AstVisitor;

import java.util.Objects;

/**
 * Represents an aggregate function call (COUNT, SUM, AVG, MIN, MAX).
 */
public class AggregateFunction extends Expression {
    
    public enum AggregateType {
        COUNT, SUM, AVG, MIN, MAX, COUNT_DISTINCT
    }
    
    private final AggregateType aggregateType;
    private final Expression expression; // null for COUNT(*)
    private final boolean distinct;
    
    public AggregateFunction(AggregateType aggregateType, Expression expression) {
        this.aggregateType = aggregateType;
        this.expression = expression;
        this.distinct = false;
    }
    
    public AggregateFunction(AggregateType aggregateType, Expression expression, boolean distinct) {
        this.aggregateType = aggregateType;
        this.expression = expression;
        this.distinct = distinct;
    }
    
    public AggregateType getAggregateType() {
        return aggregateType;
    }
    
    public Expression getExpression() {
        return expression;
    }
    
    public boolean isDistinct() {
        return distinct;
    }
    
    public boolean isCountStar() {
        return aggregateType == AggregateType.COUNT && expression == null;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitAggregateFunction(this, context);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AggregateFunction that = (AggregateFunction) obj;
        return distinct == that.distinct &&
               aggregateType == that.aggregateType &&
               Objects.equals(expression, that.expression);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(aggregateType, expression, distinct);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(aggregateType.name()).append("(");
        if (distinct) {
            sb.append("DISTINCT ");
        }
        if (expression == null && aggregateType == AggregateType.COUNT) {
            sb.append("*");
        } else if (expression != null) {
            sb.append(expression);
        }
        sb.append(")");
        return sb.toString();
    }
}