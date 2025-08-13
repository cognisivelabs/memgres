package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstNode;
import com.memgres.sql.ast.AstVisitor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a single Common Table Expression (CTE) within a WITH clause.
 * 
 * Syntax: cte_name [(column1, column2, ...)] AS (SELECT ...)
 */
public class CommonTableExpression extends AstNode {
    
    private final String name;
    private final Optional<List<String>> columnNames;
    private final SelectStatement selectStatement;
    
    public CommonTableExpression(String name, Optional<List<String>> columnNames, SelectStatement selectStatement) {
        this.name = name;
        this.columnNames = columnNames;
        this.selectStatement = selectStatement;
    }
    
    public String getName() {
        return name;
    }
    
    public Optional<List<String>> getColumnNames() {
        return columnNames;
    }
    
    public SelectStatement getSelectStatement() {
        return selectStatement;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitCommonTableExpression(this, context);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CommonTableExpression that = (CommonTableExpression) obj;
        return Objects.equals(name, that.name) &&
               Objects.equals(columnNames, that.columnNames) &&
               Objects.equals(selectStatement, that.selectStatement);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, columnNames, selectStatement);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        
        if (columnNames.isPresent()) {
            sb.append(" (");
            List<String> cols = columnNames.get();
            for (int i = 0; i < cols.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(cols.get(i));
            }
            sb.append(")");
        }
        
        sb.append(" AS (").append(selectStatement).append(")");
        return sb.toString();
    }
}