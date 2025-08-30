package com.memgres.sql.ast.expression;

import com.memgres.sql.ast.AstVisitor;

/**
 * Expression representing a parameter placeholder (?) in prepared statements or callable statements.
 */
public class ParameterExpression extends Expression {
    
    private final int position;
    
    public ParameterExpression(int position) {
        this.position = position;
    }
    
    public int getPosition() {
        return position;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        // For now, just return a simple string representation
        // In a full implementation, this would dispatch to visitor.visitParameterExpression
        return (T) toString();
    }
    
    @Override
    public String toString() {
        return "?" + position;
    }
}