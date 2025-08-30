package com.memgres.sql.ast;

import com.memgres.sql.ast.statement.Statement;
import com.memgres.sql.ast.AstVisitor;
import com.memgres.sql.procedure.ProcedureMetadata;

import java.util.List;

/**
 * AST node for CREATE PROCEDURE statements.
 */
public class CreateProcedureStatement extends Statement {
    
    private final String procedureName;
    private final List<ProcedureMetadata.Parameter> parameters;
    private final String javaClassName;
    
    public CreateProcedureStatement(String procedureName, List<ProcedureMetadata.Parameter> parameters, String javaClassName) {
        this.procedureName = procedureName;
        this.parameters = List.copyOf(parameters); // Immutable copy
        this.javaClassName = javaClassName;
    }
    
    public String getProcedureName() {
        return procedureName;
    }
    
    public List<ProcedureMetadata.Parameter> getParameters() {
        return parameters;
    }
    
    public String getJavaClassName() {
        return javaClassName;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        // Simplified implementation - return string representation
        return (T) toString();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE PROCEDURE ").append(procedureName).append("(");
        
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(parameters.get(i).toString());
        }
        
        sb.append(") AS '").append(javaClassName).append("'");
        return sb.toString();
    }
}