package com.memgres.sql.procedure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for stored procedures in MemGres.
 * 
 * <p>Manages procedure registration, lookup, and execution similar to H2's approach.</p>
 */
public class ProcedureRegistry {
    
    private static final Logger logger = LoggerFactory.getLogger(ProcedureRegistry.class);
    
    private final Map<String, StoredProcedure> procedures = new ConcurrentHashMap<>();
    private final Map<String, String> procedureClasses = new ConcurrentHashMap<>();
    
    /**
     * Register a stored procedure by Java class name.
     * 
     * @param procedureName The procedure name (case-insensitive)
     * @param javaClassName The Java class implementing the procedure
     * @throws SQLException if registration fails
     */
    public void registerProcedure(String procedureName, String javaClassName) throws SQLException {
        logger.info("Registering procedure {} with class {}", procedureName, javaClassName);
        
        try {
            // Load and instantiate the procedure class
            Class<?> clazz = Class.forName(javaClassName);
            if (!StoredProcedure.class.isAssignableFrom(clazz)) {
                throw new SQLException("Class " + javaClassName + " does not implement StoredProcedure interface");
            }
            
            StoredProcedure procedure = (StoredProcedure) clazz.getDeclaredConstructor().newInstance();
            
            // Store both the instance and class name
            String normalizedName = procedureName.toLowerCase();
            procedures.put(normalizedName, procedure);
            procedureClasses.put(normalizedName, javaClassName);
            
            logger.info("Successfully registered procedure: {}", procedureName);
            
        } catch (Exception e) {
            String message = "Failed to register procedure " + procedureName + " with class " + javaClassName;
            logger.error(message, e);
            throw new SQLException(message, e);
        }
    }
    
    /**
     * Unregister a stored procedure.
     * 
     * @param procedureName The procedure name to remove
     * @return true if the procedure was removed, false if it didn't exist
     */
    public boolean unregisterProcedure(String procedureName) {
        String normalizedName = procedureName.toLowerCase();
        StoredProcedure removed = procedures.remove(normalizedName);
        procedureClasses.remove(normalizedName);
        
        if (removed != null) {
            logger.info("Unregistered procedure: {}", procedureName);
            return true;
        }
        
        logger.warn("Attempt to unregister non-existent procedure: {}", procedureName);
        return false;
    }
    
    /**
     * Get a stored procedure by name.
     * 
     * @param procedureName The procedure name (case-insensitive)
     * @return The stored procedure instance, or null if not found
     */
    public StoredProcedure getProcedure(String procedureName) {
        return procedures.get(procedureName.toLowerCase());
    }
    
    /**
     * Check if a procedure exists.
     * 
     * @param procedureName The procedure name (case-insensitive)
     * @return true if the procedure exists
     */
    public boolean exists(String procedureName) {
        return procedures.containsKey(procedureName.toLowerCase());
    }
    
    /**
     * Execute a stored procedure.
     * 
     * @param procedureName The procedure name
     * @param parameters The input parameters
     * @return The output parameters
     * @throws SQLException if execution fails or procedure doesn't exist
     */
    public Map<String, Object> executeProcedure(String procedureName, Map<String, Object> parameters) throws SQLException {
        StoredProcedure procedure = getProcedure(procedureName);
        if (procedure == null) {
            throw new SQLException("Procedure not found: " + procedureName);
        }
        
        logger.debug("Executing procedure {} with parameters: {}", procedureName, parameters);
        
        try {
            Map<String, Object> result = procedure.execute(parameters);
            logger.debug("Procedure {} completed with results: {}", procedureName, result);
            return result;
        } catch (Exception e) {
            String message = "Error executing procedure " + procedureName;
            logger.error(message, e);
            throw new SQLException(message, e);
        }
    }
    
    /**
     * Get all registered procedure names.
     * 
     * @return Array of procedure names
     */
    public String[] getProcedureNames() {
        return procedures.keySet().toArray(new String[0]);
    }
    
    /**
     * Get the Java class name for a procedure.
     * 
     * @param procedureName The procedure name
     * @return The Java class name, or null if procedure doesn't exist
     */
    public String getProcedureClassName(String procedureName) {
        return procedureClasses.get(procedureName.toLowerCase());
    }
    
    /**
     * Clear all registered procedures.
     */
    public void clearAll() {
        logger.info("Clearing all registered procedures");
        procedures.clear();
        procedureClasses.clear();
    }
    
    /**
     * Get the number of registered procedures.
     * 
     * @return The procedure count
     */
    public int getCount() {
        return procedures.size();
    }
}