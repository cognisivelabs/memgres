package com.memgres.sql.ast.expression;

import com.memgres.sql.ast.AstVisitor;

/**
 * Represents a literal value in SQL (string, number, boolean, null, etc.).
 */
public class LiteralExpression extends Expression {
    
    private final Object value;
    private final LiteralType type;
    
    public enum LiteralType {
        STRING, INTEGER, DECIMAL, BOOLEAN, NULL
    }
    
    public LiteralExpression(Object value, LiteralType type) {
        this.value = value;
        this.type = type;
    }
    
    public Object getValue() {
        return value;
    }
    
    public LiteralType getType() {
        return type;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitLiteralExpression(this, context);
    }
    
    @Override
    public String toString() {
        return "Literal(" + value + ", " + type + ")";
    }
}