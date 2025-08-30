package com.memgres.sql.ast;

import com.memgres.sql.ast.statement.Statement;
import com.memgres.sql.ast.expression.Expression;
import com.memgres.sql.ast.AstVisitor;

import java.util.List;

/**
 * AST node for CALL statements (stored procedure calls).
 */
public class CallStatement extends Statement {
    
    private final String procedureName;
    private final List<Expression> parameters;
    
    public CallStatement(String procedureName, List<Expression> parameters) {
        this.procedureName = procedureName;
        this.parameters = parameters != null ? List.copyOf(parameters) : List.of();
    }
    
    public String getProcedureName() {
        return procedureName;
    }
    
    public List<Expression> getParameters() {
        return parameters;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        if (visitor instanceof com.memgres.sql.execution.StatementExecutor) {
            // Cast to the concrete visitor type that has visitCallStatement
            com.memgres.sql.execution.StatementExecutor executor = (com.memgres.sql.execution.StatementExecutor) visitor;
            return (T) executor.visitCallStatement(this, (com.memgres.sql.execution.ExecutionContext) context);
        } else {
            // Fallback for other visitors or simplified implementation
            return (T) toString();
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CALL ").append(procedureName).append("(");
        
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(parameters.get(i).toString());
        }
        
        sb.append(")");
        return sb.toString();
    }
}