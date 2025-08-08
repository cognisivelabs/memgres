package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstNode;
import com.memgres.sql.ast.AstVisitor;
import com.memgres.sql.ast.expression.Expression;

import java.util.List;

/**
 * Represents an ORDER BY clause in a SELECT statement.
 */
public class OrderByClause extends AstNode {
    
    public static class OrderItem {
        private final Expression expression;
        private final boolean ascending;
        
        public OrderItem(Expression expression, boolean ascending) {
            this.expression = expression;
            this.ascending = ascending;
        }
        
        public Expression getExpression() {
            return expression;
        }
        
        public boolean isAscending() {
            return ascending;
        }
    }
    
    private final List<OrderItem> orderItems;
    
    public OrderByClause(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
    
    public List<OrderItem> getOrderItems() {
        return orderItems;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitOrderByClause(this, context);
    }
}