package com.memgres.sql.execution;

/**
 * Exception thrown when SQL execution fails.
 * Enhanced with H2-compatible error codes and SQL states.
 */
public class SqlExecutionException extends Exception {
    
    private final SqlErrorCode errorCode;
    private final String sqlState;
    private final int errorCodeValue;
    
    public SqlExecutionException(String message) {
        super(message);
        this.errorCode = SqlErrorCode.GENERAL_ERROR;
        this.sqlState = errorCode.getSqlState();
        this.errorCodeValue = errorCode.getCode();
    }
    
    public SqlExecutionException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = SqlErrorCode.GENERAL_ERROR;
        this.sqlState = errorCode.getSqlState();
        this.errorCodeValue = errorCode.getCode();
    }
    
    public SqlExecutionException(SqlErrorCode errorCode, Object... args) {
        super(errorCode.formatMessage(args));
        this.errorCode = errorCode;
        this.sqlState = errorCode.getSqlState();
        this.errorCodeValue = errorCode.getCode();
    }
    
    public SqlExecutionException(SqlErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode.formatMessage(args), cause);
        this.errorCode = errorCode;
        this.sqlState = errorCode.getSqlState();
        this.errorCodeValue = errorCode.getCode();
    }
    
    /**
     * Get the H2-compatible error code.
     */
    public int getErrorCode() {
        return errorCodeValue;
    }
    
    /**
     * Get the SQL state (SQLSTATE).
     */
    public String getSqlState() {
        return sqlState;
    }
    
    /**
     * Get the error code enum.
     */
    public SqlErrorCode getErrorCodeEnum() {
        return errorCode;
    }
    
    @Override
    public String toString() {
        return String.format("SqlExecutionException [%s:%d] %s", sqlState, errorCodeValue, getMessage());
    }
}