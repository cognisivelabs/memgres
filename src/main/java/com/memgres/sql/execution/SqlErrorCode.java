package com.memgres.sql.execution;

/**
 * H2-compatible SQL error codes for MemGres.
 * Based on H2 Database error code conventions.
 */
public enum SqlErrorCode {
    
    // Schema-related errors (42000-42999)
    SCHEMA_ALREADY_EXISTS(42501, "Schema \"%s\" already exists"),
    SCHEMA_NOT_FOUND(42502, "Schema \"%s\" not found"),
    CANNOT_DROP_SCHEMA_PUBLIC(42503, "Cannot drop schema \"public\""),
    SCHEMA_NOT_EMPTY(42504, "Cannot drop schema \"%s\" because it contains objects; use CASCADE"),
    
    // Table-related errors (42100-42199)
    TABLE_ALREADY_EXISTS(42101, "Table \"%s\" already exists"),
    TABLE_NOT_FOUND(42102, "Table \"%s\" not found"),
    COLUMN_NOT_FOUND(42103, "Column \"%s\" not found"),
    COLUMN_ALREADY_EXISTS(42104, "Column \"%s\" already exists"),
    
    // Data type errors (42200-42299)
    DATA_TYPE_CONVERSION_ERROR(42201, "Cannot convert \"%s\" to %s"),
    INVALID_DATA_TYPE(42202, "Unknown data type: %s"),
    VALUE_TOO_LONG(42203, "Value too long for column \"%s\""),
    
    // Constraint violations (23000-23999)
    NOT_NULL_VIOLATION(23502, "NULL not allowed for column \"%s\""),
    UNIQUE_CONSTRAINT_VIOLATION(23505, "Unique constraint violation for column \"%s\""),
    CHECK_CONSTRAINT_VIOLATION(23513, "Check constraint violation: %s"),
    
    // SQL syntax errors (42600-42699)
    SYNTAX_ERROR(42601, "Syntax error: %s"),
    INVALID_PARAMETER_COUNT(42602, "Invalid parameter count for function %s: expected %d, got %d"),
    UNKNOWN_FUNCTION(42603, "Unknown function: %s"),
    
    // Configuration errors (42800-42899)
    INVALID_CONFIGURATION_KEY(42801, "Invalid configuration key: %s"),
    INVALID_CONFIGURATION_VALUE(42802, "Invalid configuration value for %s: %s"),
    
    // General execution errors (50000-59999)
    GENERAL_ERROR(50000, "General error: %s"),
    FEATURE_NOT_SUPPORTED(50001, "Feature not supported: %s"),
    INTERNAL_ERROR(50002, "Internal error: %s"),
    
    // Transaction errors (40000-40999)
    TRANSACTION_ROLLBACK(40001, "Transaction was rolled back: %s"),
    DEADLOCK_DETECTED(40002, "Deadlock detected"),
    ISOLATION_LEVEL_CONFLICT(40003, "Isolation level conflict: %s");
    
    private final int code;
    private final String messageTemplate;
    
    SqlErrorCode(int code, String messageTemplate) {
        this.code = code;
        this.messageTemplate = messageTemplate;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessageTemplate() {
        return messageTemplate;
    }
    
    /**
     * Format the error message with the provided arguments.
     */
    public String formatMessage(Object... args) {
        try {
            return String.format(messageTemplate, args);
        } catch (Exception e) {
            return messageTemplate + " (formatting error with args: " + java.util.Arrays.toString(args) + ")";
        }
    }
    
    /**
     * Get the SQL state based on the error code.
     */
    public String getSqlState() {
        int majorCategory = code / 1000;
        int minorCategory = (code % 1000) / 100;
        
        switch (majorCategory) {
            case 23: return "23000"; // Constraint violation
            case 40: return "40000"; // Transaction rollback
            case 42: return "42000"; // Syntax error or access rule violation
            case 50: return "50000"; // System error
            default: return "HY000"; // General error
        }
    }
}