package com.memgres.sql.ast.expression;

import com.memgres.sql.ast.AstNode;
import com.memgres.sql.ast.AstVisitor;
import com.memgres.sql.ast.statement.OrderByClause;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents an OVER clause for window functions.
 * Syntax: OVER (PARTITION BY expr1, expr2 ORDER BY expr3, expr4)
 */
public class OverClause extends AstNode {
    
    private final Optional<List<Expression>> partitionByExpressions;
    private final Optional<List<OrderByClause.OrderItem>> orderByItems;
    
    public OverClause(Optional<List<Expression>> partitionByExpressions, 
                      Optional<List<OrderByClause.OrderItem>> orderByItems) {
        this.partitionByExpressions = partitionByExpressions;
        this.orderByItems = orderByItems;
    }
    
    public Optional<List<Expression>> getPartitionByExpressions() {
        return partitionByExpressions;
    }
    
    public Optional<List<OrderByClause.OrderItem>> getOrderByItems() {
        return orderByItems;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitOverClause(this, context);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OverClause that = (OverClause) obj;
        return Objects.equals(partitionByExpressions, that.partitionByExpressions) &&
               Objects.equals(orderByItems, that.orderByItems);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(partitionByExpressions, orderByItems);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(");
        
        if (partitionByExpressions.isPresent() && !partitionByExpressions.get().isEmpty()) {
            sb.append("PARTITION BY ");
            for (int i = 0; i < partitionByExpressions.get().size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(partitionByExpressions.get().get(i));
            }
        }
        
        if (orderByItems.isPresent() && !orderByItems.get().isEmpty()) {
            if (partitionByExpressions.isPresent() && !partitionByExpressions.get().isEmpty()) {
                sb.append(" ");
            }
            sb.append("ORDER BY ");
            for (int i = 0; i < orderByItems.get().size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(orderByItems.get().get(i));
            }
        }
        
        sb.append(")");
        return sb.toString();
    }
}