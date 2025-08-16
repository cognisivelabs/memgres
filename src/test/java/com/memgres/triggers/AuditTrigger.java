package com.memgres.triggers;

import com.memgres.api.Trigger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

/**
 * Example audit trigger that logs data changes to an audit table.
 * Demonstrates H2-compatible trigger implementation in MemGres.
 */
public class AuditTrigger implements Trigger {
    
    private String triggerName;
    private String tableName;
    private boolean before;
    private int type;
    
    @Override
    public void init(Connection conn, String schemaName, String triggerName, 
                    String tableName, boolean before, int type) throws SQLException {
        this.triggerName = triggerName;
        this.tableName = tableName;
        this.before = before;
        this.type = type;
        
        // Create audit table if it doesn't exist
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS audit_log (" +
                "id INTEGER PRIMARY KEY, " +
                "table_name VARCHAR(100), " +
                "operation VARCHAR(10), " +
                "old_values TEXT, " +
                "new_values TEXT, " +
                "changed_at TIMESTAMP" +
                ")"
            );
        }
    }
    
    @Override
    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
        String operation = getOperationName(type);
        String oldValues = oldRow != null ? arrayToString(oldRow) : null;
        String newValues = newRow != null ? arrayToString(newRow) : null;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(String.format(
                "INSERT INTO audit_log (table_name, operation, old_values, new_values, changed_at) " +
                "VALUES ('%s', '%s', %s, %s, '%s')",
                tableName,
                operation,
                oldValues != null ? "'" + oldValues + "'" : "NULL",
                newValues != null ? "'" + newValues + "'" : "NULL",
                LocalDateTime.now().toString()
            ));
        }
    }
    
    private String getOperationName(int type) {
        switch (type) {
            case Trigger.INSERT: return "INSERT";
            case Trigger.UPDATE: return "UPDATE";
            case Trigger.DELETE: return "DELETE";
            case Trigger.SELECT: return "SELECT";
            default: return "UNKNOWN";
        }
    }
    
    private String arrayToString(Object[] array) {
        if (array == null) return null;
        
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(array[i] != null ? array[i].toString() : "NULL");
        }
        sb.append("]");
        return sb.toString();
    }
}