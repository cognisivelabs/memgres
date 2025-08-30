package com.memgres.sql.ast.expression;

import com.memgres.sql.ast.AstVisitor;

import java.util.List;
import java.util.Objects;

/**
 * Represents a window function call (ROW_NUMBER, RANK, DENSE_RANK, etc.) with OVER clause.
 */
public class WindowFunction extends Expression {
    
    public enum WindowFunctionType {
        // Ranking functions
        ROW_NUMBER, RANK, DENSE_RANK, PERCENT_RANK, CUME_DIST,
        // Value functions  
        FIRST_VALUE, LAST_VALUE, NTH_VALUE,
        // Offset functions
        LAG, LEAD,
        // Distribution functions
        NTILE
    }
    
    private final WindowFunctionType windowFunctionType;
    private final List<Expression> arguments;
    private final OverClause overClause;
    
    public WindowFunction(WindowFunctionType windowFunctionType, OverClause overClause) {
        this.windowFunctionType = windowFunctionType;
        this.arguments = List.of();
        this.overClause = overClause;
    }
    
    public WindowFunction(WindowFunctionType windowFunctionType, List<Expression> arguments, OverClause overClause) {
        this.windowFunctionType = windowFunctionType;
        this.arguments = List.copyOf(arguments);
        this.overClause = overClause;
    }
    
    public WindowFunctionType getWindowFunctionType() {
        return windowFunctionType;
    }
    
    public List<Expression> getArguments() {
        return arguments;
    }
    
    public OverClause getOverClause() {
        return overClause;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitWindowFunction(this, context);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        WindowFunction that = (WindowFunction) obj;
        return windowFunctionType == that.windowFunctionType &&
               Objects.equals(arguments, that.arguments) &&
               Objects.equals(overClause, that.overClause);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(windowFunctionType, arguments, overClause);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(windowFunctionType.name()).append("(");
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(arguments.get(i));
        }
        sb.append(") OVER ").append(overClause);
        return sb.toString();
    }
}