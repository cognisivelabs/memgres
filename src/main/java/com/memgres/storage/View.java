package com.memgres.storage;

import com.memgres.sql.ast.statement.SelectStatement;
import com.memgres.types.Column;

import java.util.List;
import java.util.Objects;

/**
 * Represents a database view containing a SELECT statement and metadata.
 */
public class View {
    private final String name;
    private final List<String> columnNames;
    private final SelectStatement selectStatement;
    private final boolean force;
    
    /**
     * Create a new view
     * @param name the view name
     * @param columnNames optional explicit column names (null if not specified)
     * @param selectStatement the SELECT statement that defines the view
     * @param force whether this view was created with FORCE option
     */
    public View(String name, List<String> columnNames, SelectStatement selectStatement, boolean force) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("View name cannot be null or empty");
        }
        if (selectStatement == null) {
            throw new IllegalArgumentException("SELECT statement cannot be null");
        }
        
        this.name = name.toLowerCase(); // PostgreSQL converts view names to lowercase
        this.columnNames = columnNames;
        this.selectStatement = selectStatement;
        this.force = force;
    }
    
    /**
     * Get the view name
     * @return the view name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the explicit column names if specified
     * @return list of column names or null if not specified
     */
    public List<String> getColumnNames() {
        return columnNames;
    }
    
    /**
     * Get the SELECT statement that defines this view
     * @return the SELECT statement
     */
    public SelectStatement getSelectStatement() {
        return selectStatement;
    }
    
    /**
     * Check if this view was created with FORCE option
     * @return true if created with FORCE
     */
    public boolean isForce() {
        return force;
    }
    
    /**
     * Check if this view has explicit column names defined
     * @return true if column names are explicitly defined
     */
    public boolean hasExplicitColumns() {
        return columnNames != null && !columnNames.isEmpty();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("VIEW ").append(name);
        if (hasExplicitColumns()) {
            sb.append(" (").append(String.join(", ", columnNames)).append(")");
        }
        sb.append(" AS ").append(selectStatement);
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        View view = (View) o;
        return Objects.equals(name, view.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}