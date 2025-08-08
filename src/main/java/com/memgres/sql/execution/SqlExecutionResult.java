package com.memgres.sql.execution;

import com.memgres.types.Column;
import com.memgres.types.Row;

import java.util.Collections;
import java.util.List;

/**
 * Represents the result of executing a SQL statement.
 */
public class SqlExecutionResult {
    
    public enum ResultType {
        SELECT,    // Query that returns rows
        INSERT,    // INSERT statement - returns affected rows count
        UPDATE,    // UPDATE statement - returns affected rows count  
        DELETE,    // DELETE statement - returns affected rows count
        DDL,       // CREATE/DROP statement - returns success/failure
        EMPTY      // No result
    }
    
    private final ResultType type;
    private final List<Column> columns;
    private final List<Row> rows;
    private final int affectedRows;
    private final boolean success;
    private final String message;
    
    // Constructor for SELECT results
    public SqlExecutionResult(List<Column> columns, List<Row> rows) {
        this.type = ResultType.SELECT;
        this.columns = columns;
        this.rows = rows;
        this.affectedRows = rows.size();
        this.success = true;
        this.message = "SELECT completed successfully";
    }
    
    // Constructor for INSERT/UPDATE/DELETE results
    public SqlExecutionResult(ResultType type, int affectedRows) {
        if (type == ResultType.SELECT || type == ResultType.DDL) {
            throw new IllegalArgumentException("Use appropriate constructor for " + type);
        }
        this.type = type;
        this.columns = Collections.emptyList();
        this.rows = Collections.emptyList();
        this.affectedRows = affectedRows;
        this.success = true;
        this.message = type + " completed successfully";
    }
    
    // Constructor for DDL results
    public SqlExecutionResult(ResultType type, boolean success, String message) {
        if (type != ResultType.DDL) {
            throw new IllegalArgumentException("Use this constructor only for DDL operations");
        }
        this.type = type;
        this.columns = Collections.emptyList();
        this.rows = Collections.emptyList();
        this.affectedRows = 0;
        this.success = success;
        this.message = message;
    }
    
    // Constructor for empty results
    private SqlExecutionResult() {
        this.type = ResultType.EMPTY;
        this.columns = Collections.emptyList();
        this.rows = Collections.emptyList();
        this.affectedRows = 0;
        this.success = true;
        this.message = "No operation performed";
    }
    
    public static SqlExecutionResult empty() {
        return new SqlExecutionResult();
    }
    
    public ResultType getType() {
        return type;
    }
    
    public List<Column> getColumns() {
        return columns;
    }
    
    public List<Row> getRows() {
        return rows;
    }
    
    public int getAffectedRows() {
        return affectedRows;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        switch (type) {
            case SELECT:
                return String.format("SELECT: %d rows returned", rows.size());
            case INSERT:
            case UPDATE:
            case DELETE:
                return String.format("%s: %d rows affected", type, affectedRows);
            case DDL:
                return String.format("DDL: %s - %s", success ? "SUCCESS" : "FAILED", message);
            case EMPTY:
                return "EMPTY RESULT";
            default:
                return "UNKNOWN RESULT";
        }
    }
}