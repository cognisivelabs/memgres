package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstVisitor;

/**
 * AST node representing a SET statement for configuration.
 */
public class SetStatement extends Statement {
    private final String configurationKey;
    private final Object configurationValue;
    
    public SetStatement(String configurationKey, Object configurationValue) {
        this.configurationKey = configurationKey;
        this.configurationValue = configurationValue;
    }
    
    public String getConfigurationKey() {
        return configurationKey;
    }
    
    public Object getConfigurationValue() {
        return configurationValue;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitSetStatement(this, context);
    }
    
    @Override
    public String toString() {
        return "SET " + configurationKey + " = " + configurationValue;
    }
}