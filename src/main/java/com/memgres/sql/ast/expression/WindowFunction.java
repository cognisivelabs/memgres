package com.memgres.sql.ast.expression;

import com.memgres.sql.ast.AstVisitor;

import java.util.Objects;

/**
 * Represents a window function call (ROW_NUMBER, RANK, DENSE_RANK, etc.) with OVER clause.
 */
public class WindowFunction extends Expression {
    
    public enum WindowFunctionType {
        ROW_NUMBER, RANK, DENSE_RANK, PERCENT_RANK, CUME_DIST
    }
    
    private final WindowFunctionType windowFunctionType;
    private final OverClause overClause;
    
    public WindowFunction(WindowFunctionType windowFunctionType, OverClause overClause) {
        this.windowFunctionType = windowFunctionType;
        this.overClause = overClause;
    }
    
    public WindowFunctionType getWindowFunctionType() {
        return windowFunctionType;
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
               Objects.equals(overClause, that.overClause);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(windowFunctionType, overClause);
    }
    
    @Override
    public String toString() {
        return windowFunctionType.name() + "() OVER " + overClause;
    }
}