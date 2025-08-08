package com.memgres.sql.execution;

/**
 * Exception thrown when SQL execution fails.
 */
public class SqlExecutionException extends Exception {
    
    public SqlExecutionException(String message) {
        super(message);
    }
    
    public SqlExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}