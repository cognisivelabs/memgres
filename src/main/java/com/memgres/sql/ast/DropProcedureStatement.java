package com.memgres.sql.ast;

import com.memgres.sql.ast.statement.Statement;
import com.memgres.sql.ast.AstVisitor;

/**
 * AST node for DROP PROCEDURE statements.
 */
public class DropProcedureStatement extends Statement {
    
    private final String procedureName;
    private final boolean ifExists;
    
    public DropProcedureStatement(String procedureName, boolean ifExists) {
        this.procedureName = procedureName;
        this.ifExists = ifExists;
    }
    
    public String getProcedureName() {
        return procedureName;
    }
    
    public boolean isIfExists() {
        return ifExists;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        if (visitor instanceof com.memgres.sql.execution.StatementExecutor) {
            // Cast to the concrete visitor type that has visitDropProcedureStatement
            com.memgres.sql.execution.StatementExecutor executor = (com.memgres.sql.execution.StatementExecutor) visitor;
            return (T) executor.visitDropProcedureStatement(this, (com.memgres.sql.execution.ExecutionContext) context);
        } else {
            // Fallback for other visitors or simplified implementation
            return (T) toString();
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DROP PROCEDURE ");
        if (ifExists) {
            sb.append("IF EXISTS ");
        }
        sb.append(procedureName);
        return sb.toString();
    }
}