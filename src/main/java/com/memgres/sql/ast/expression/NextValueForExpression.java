package com.memgres.sql.ast.expression;

import com.memgres.sql.ast.AstVisitor;

/**
 * Represents a NEXT VALUE FOR sequence_name expression.
 * This is H2's SQL standard way to get the next value from a sequence.
 */
public class NextValueForExpression extends Expression {
    
    private final String sequenceName;
    
    public NextValueForExpression(String sequenceName) {
        this.sequenceName = sequenceName;
    }
    
    public String getSequenceName() {
        return sequenceName;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitNextValueForExpression(this, context);
    }
    
    @Override
    public String toString() {
        return "NEXT VALUE FOR " + sequenceName;
    }
}