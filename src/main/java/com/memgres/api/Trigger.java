package com.memgres.api;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * H2-compatible Trigger interface for MemGres.
 * 
 * A trigger is fired when a table is modified. The trigger can be 
 * called before or after the modification (BEFORE or AFTER).
 * 
 * This interface is compatible with H2's org.h2.api.Trigger interface.
 */
public interface Trigger {
    
    /**
     * Trigger type constants matching H2's implementation.
     */
    int INSERT = 1;
    int UPDATE = 2;
    int DELETE = 4;
    int SELECT = 8;
    int ROLLBACK = 16;
    
    /**
     * Initialize the trigger. This method is called when the trigger is created,
     * as well as when the database is opened.
     *
     * @param conn the connection to the database
     * @param schemaName the schema name of the table
     * @param triggerName the name of the trigger
     * @param tableName the name of the table
     * @param before whether this is a BEFORE trigger
     * @param type the operation type: INSERT, UPDATE, DELETE, SELECT, or ROLLBACK
     * @throws SQLException on SQL exception
     */
    void init(Connection conn, String schemaName, String triggerName, 
              String tableName, boolean before, int type) throws SQLException;
    
    /**
     * Fire the trigger. This method is called for each triggered action.
     *
     * @param conn the connection to the database
     * @param oldRow the old row, or null if no old row is available (for INSERT)
     * @param newRow the new row, or null if no new row is available (for DELETE)
     * @throws SQLException on SQL exception
     */
    void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException;
    
    /**
     * Close the trigger. This method is called when the database is closed.
     * The default implementation does nothing.
     *
     * @throws SQLException on SQL exception
     */
    default void close() throws SQLException {
        // Default implementation does nothing
    }
    
    /**
     * Remove the trigger. This method is called when the trigger is dropped.
     * The default implementation does nothing.
     *
     * @throws SQLException on SQL exception
     */
    default void remove() throws SQLException {
        // Default implementation does nothing
    }
}