package com.memgres.sql.ast.expression;

import com.memgres.sql.ast.AstVisitor;

/**
 * Represents a CURRENT VALUE FOR sequence_name expression.
 * This is H2's SQL standard way to get the current value from a sequence.
 */
public class CurrentValueForExpression extends Expression {
    
    private final String sequenceName;
    
    public CurrentValueForExpression(String sequenceName) {
        this.sequenceName = sequenceName;
    }
    
    public String getSequenceName() {
        return sequenceName;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitCurrentValueForExpression(this, context);
    }
    
    @Override
    public String toString() {
        return "CURRENT VALUE FOR " + sequenceName;
    }
}