package com.memgres.core;

import com.memgres.api.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Manages database triggers in MemGres.
 * Handles trigger registration, execution, and lifecycle management.
 */
public class TriggerManager {
    
    private static final Logger logger = LoggerFactory.getLogger(TriggerManager.class);
    
    // Map of schema -> trigger name -> trigger definition
    private final Map<String, Map<String, TriggerDefinition>> triggers = new ConcurrentHashMap<>();
    
    // Map of schema.table -> list of triggers for that table
    private final Map<String, List<TriggerDefinition>> tableTriggers = new ConcurrentHashMap<>();
    
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    /**
     * Register a new trigger.
     */
    public void createTrigger(TriggerDefinition triggerDef, Connection connection) throws SQLException {
        lock.writeLock().lock();
        try {
            String schemaName = triggerDef.getSchemaName().toLowerCase();
            String triggerName = triggerDef.getName().toLowerCase();
            String tableKey = schemaName + "." + triggerDef.getTableName().toLowerCase();
            
            // Check if trigger already exists
            Map<String, TriggerDefinition> schemaTriggers = triggers.computeIfAbsent(schemaName, k -> new ConcurrentHashMap<>());
            if (schemaTriggers.containsKey(triggerName)) {
                if (triggerDef.isIfNotExists()) {
                    logger.debug("Trigger {} already exists, IF NOT EXISTS specified", triggerName);
                    return;
                } else {
                    throw new SQLException("Trigger already exists: " + triggerName);
                }
            }
            
            // Initialize the trigger
            try {
                Trigger trigger = triggerDef.getTriggerInstance();
                trigger.init(connection, schemaName, triggerName, triggerDef.getTableName(),
                           triggerDef.getTiming() == TriggerDefinition.Timing.BEFORE,
                           triggerDef.getEvent().getValue());
                
                // Register the trigger
                schemaTriggers.put(triggerName, triggerDef);
                tableTriggers.computeIfAbsent(tableKey, k -> new ArrayList<>()).add(triggerDef);
                
                logger.info("Created trigger {} on table {}.{}", triggerName, schemaName, triggerDef.getTableName());
                
            } catch (Exception e) {
                throw new SQLException("Failed to initialize trigger " + triggerName + ": " + e.getMessage(), e);
            }
            
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Drop an existing trigger.
     */
    public void dropTrigger(String schemaName, String triggerName, boolean ifExists) throws SQLException {
        lock.writeLock().lock();
        try {
            schemaName = schemaName.toLowerCase();
            triggerName = triggerName.toLowerCase();
            
            Map<String, TriggerDefinition> schemaTriggers = triggers.get(schemaName);
            if (schemaTriggers == null || !schemaTriggers.containsKey(triggerName)) {
                if (ifExists) {
                    logger.debug("Trigger {} does not exist, IF EXISTS specified", triggerName);
                    return;
                } else {
                    throw new SQLException("Trigger not found: " + triggerName);
                }
            }
            
            TriggerDefinition triggerDef = schemaTriggers.remove(triggerName);
            String tableKey = schemaName + "." + triggerDef.getTableName().toLowerCase();
            
            // Remove from table triggers
            List<TriggerDefinition> triggers = tableTriggers.get(tableKey);
            if (triggers != null) {
                triggers.remove(triggerDef);
                if (triggers.isEmpty()) {
                    tableTriggers.remove(tableKey);
                }
            }
            
            // Clean up the trigger
            try {
                Trigger trigger = triggerDef.getTriggerInstance();
                trigger.remove();
            } catch (Exception e) {
                logger.warn("Error cleaning up trigger {}: {}", triggerName, e.getMessage());
            }
            
            logger.info("Dropped trigger {} from table {}.{}", triggerName, schemaName, triggerDef.getTableName());
            
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Fire BEFORE triggers for a table operation.
     */
    public void fireBefore(String schemaName, String tableName, TriggerDefinition.Event event,
                          Object[] oldRow, Object[] newRow, Connection connection) throws SQLException {
        fireTriggers(schemaName, tableName, event, TriggerDefinition.Timing.BEFORE, oldRow, newRow, connection);
    }
    
    /**
     * Fire AFTER triggers for a table operation.
     */
    public void fireAfter(String schemaName, String tableName, TriggerDefinition.Event event,
                         Object[] oldRow, Object[] newRow, Connection connection) throws SQLException {
        fireTriggers(schemaName, tableName, event, TriggerDefinition.Timing.AFTER, oldRow, newRow, connection);
    }
    
    /**
     * Fire triggers for a specific timing, event, and table.
     */
    private void fireTriggers(String schemaName, String tableName, TriggerDefinition.Event event,
                             TriggerDefinition.Timing timing, Object[] oldRow, Object[] newRow,
                             Connection connection) throws SQLException {
        lock.readLock().lock();
        try {
            String tableKey = schemaName.toLowerCase() + "." + tableName.toLowerCase();
            List<TriggerDefinition> triggers = tableTriggers.get(tableKey);
            
            if (triggers == null || triggers.isEmpty()) {
                return; // No triggers for this table
            }
            
            for (TriggerDefinition triggerDef : triggers) {
                if (triggerDef.getTiming() == timing && triggerDef.getEvent() == event) {
                    try {
                        Trigger trigger = triggerDef.getTriggerInstance();
                        trigger.fire(connection, oldRow, newRow);
                        logger.debug("Fired {} {} trigger {} on {}.{}", timing, event, 
                                   triggerDef.getName(), schemaName, tableName);
                    } catch (Exception e) {
                        throw new SQLException("Error firing trigger " + triggerDef.getName() + 
                                             ": " + e.getMessage(), e);
                    }
                }
            }
            
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Get all triggers for a specific schema.
     */
    public List<TriggerDefinition> getTriggers(String schemaName) {
        lock.readLock().lock();
        try {
            Map<String, TriggerDefinition> schemaTriggers = triggers.get(schemaName.toLowerCase());
            if (schemaTriggers == null) {
                return new ArrayList<>();
            }
            return new ArrayList<>(schemaTriggers.values());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Get all triggers for a specific table.
     */
    public List<TriggerDefinition> getTableTriggers(String schemaName, String tableName) {
        lock.readLock().lock();
        try {
            String tableKey = schemaName.toLowerCase() + "." + tableName.toLowerCase();
            List<TriggerDefinition> triggers = tableTriggers.get(tableKey);
            return triggers != null ? new ArrayList<>(triggers) : new ArrayList<>();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Check if a trigger exists.
     */
    public boolean triggerExists(String schemaName, String triggerName) {
        lock.readLock().lock();
        try {
            Map<String, TriggerDefinition> schemaTriggers = triggers.get(schemaName.toLowerCase());
            return schemaTriggers != null && schemaTriggers.containsKey(triggerName.toLowerCase());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Cleanup all triggers when shutting down.
     */
    public void shutdown() {
        lock.writeLock().lock();
        try {
            for (Map<String, TriggerDefinition> schemaTriggers : triggers.values()) {
                for (TriggerDefinition triggerDef : schemaTriggers.values()) {
                    try {
                        Trigger trigger = triggerDef.getTriggerInstance();
                        trigger.close();
                    } catch (Exception e) {
                        logger.warn("Error closing trigger {}: {}", triggerDef.getName(), e.getMessage());
                    }
                }
            }
            triggers.clear();
            tableTriggers.clear();
            logger.info("Trigger manager shutdown complete");
        } finally {
            lock.writeLock().unlock();
        }
    }
}