package com.memgres.sql.ast.expression;

import com.memgres.sql.ast.AstVisitor;

/**
 * Represents a binary expression (e.g., left + right, left = right, left AND right).
 */
public class BinaryExpression extends Expression {
    
    public enum Operator {
        // Arithmetic
        ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, POWER,
        // Comparison
        EQUALS, NOT_EQUALS, LESS_THAN, LESS_THAN_EQUALS, GREATER_THAN, GREATER_THAN_EQUALS,
        // Logical
        AND, OR,
        // String
        CONCAT, LIKE,
        // JSONB
        JSONB_CONTAINS, JSONB_CONTAINED, JSONB_EXISTS,
        JSONB_EXTRACT, JSONB_EXTRACT_TEXT,
        JSONB_PATH_EXTRACT, JSONB_PATH_EXTRACT_TEXT
    }
    
    private final Expression left;
    private final Operator operator;
    private final Expression right;
    
    public BinaryExpression(Expression left, Operator operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }
    
    public Expression getLeft() {
        return left;
    }
    
    public Operator getOperator() {
        return operator;
    }
    
    public Expression getRight() {
        return right;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitBinaryExpression(this, context);
    }
    
    @Override
    public String toString() {
        return "(" + left + " " + operator + " " + right + ")";
    }
}