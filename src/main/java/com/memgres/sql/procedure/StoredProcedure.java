package com.memgres.sql.procedure;

import java.sql.SQLException;
import java.util.Map;

/**
 * Interface for H2-compatible stored procedures.
 * 
 * <p>Stored procedures in MemGres follow H2's Java-based approach where
 * procedures are implemented as Java classes and registered with the database.</p>
 */
public interface StoredProcedure {
    
    /**
     * Execute the stored procedure with the given parameters.
     * 
     * @param parameters Map of parameter names to values (for IN/INOUT parameters)
     * @return Map of parameter names to values (for OUT/INOUT parameters)
     * @throws SQLException if execution fails
     */
    Map<String, Object> execute(Map<String, Object> parameters) throws SQLException;
    
    /**
     * Get the procedure name.
     * 
     * @return The procedure name
     */
    String getName();
    
    /**
     * Get parameter information for this procedure.
     * 
     * @return Parameter metadata
     */
    ProcedureMetadata getMetadata();
}