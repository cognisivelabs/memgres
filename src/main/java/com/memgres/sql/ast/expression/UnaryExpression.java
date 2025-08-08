package com.memgres.sql.ast.expression;

import com.memgres.sql.ast.AstVisitor;

/**
 * Represents a unary expression (e.g., NOT expression, -expression).
 */
public class UnaryExpression extends Expression {
    
    public enum Operator {
        NOT, MINUS, PLUS
    }
    
    private final Operator operator;
    private final Expression operand;
    
    public UnaryExpression(Operator operator, Expression operand) {
        this.operator = operator;
        this.operand = operand;
    }
    
    public Operator getOperator() {
        return operator;
    }
    
    public Expression getOperand() {
        return operand;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitUnaryExpression(this, context);
    }
    
    @Override
    public String toString() {
        return operator + "(" + operand + ")";
    }
}