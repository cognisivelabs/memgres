package com.memgres.sql.ast.expression;

import com.memgres.sql.ast.AstVisitor;

import java.util.List;

/**
 * Represents a function call in SQL (e.g., COUNT(*), gen_random_uuid(), UPPER(name)).
 */
public class FunctionCall extends Expression {
    
    private final String functionName;
    private final List<Expression> arguments;
    
    public FunctionCall(String functionName, List<Expression> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
    }
    
    public String getFunctionName() {
        return functionName;
    }
    
    public List<Expression> getArguments() {
        return arguments;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitFunctionCall(this, context);
    }
    
    @Override
    public String toString() {
        return functionName + "(" + arguments + ")";
    }
}