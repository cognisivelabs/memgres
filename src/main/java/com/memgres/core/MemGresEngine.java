package com.memgres.core;

import com.memgres.storage.Schema;
import com.memgres.storage.Sequence;
import com.memgres.storage.Table;
import com.memgres.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Main engine for MemGres database operations.
 * Manages schemas, tables, and provides the primary interface for database operations.
 */
public class MemGresEngine {
    private static final Logger logger = LoggerFactory.getLogger(MemGresEngine.class);
    
    private final ConcurrentMap<String, Schema> schemas;
    private final TransactionManager transactionManager;
    private final ReadWriteLock engineLock;
    private volatile boolean initialized;
    
    public MemGresEngine() {
        this.schemas = new ConcurrentHashMap<>();
        this.transactionManager = new TransactionManager();
        this.engineLock = new ReentrantReadWriteLock();
        this.initialized = false;
        logger.info("MemGres Engine created");
    }
    
    /**
     * Initialize the database engine with default schema
     */
    public void initialize() {
        engineLock.writeLock().lock();
        try {
            if (initialized) {
                logger.warn("Engine already initialized");
                return;
            }
            
            // Create default 'public' schema
            Schema publicSchema = new Schema("public");
            schemas.put("public", publicSchema);
            
            initialized = true;
            logger.info("MemGres Engine initialized successfully");
        } finally {
            engineLock.writeLock().unlock();
        }
    }
    
    /**
     * Shutdown the database engine and clean up resources
     */
    public void shutdown() {
        engineLock.writeLock().lock();
        try {
            if (!initialized) {
                logger.warn("Engine already shutdown or not initialized");
                return;
            }
            
            transactionManager.shutdown();
            schemas.clear();
            initialized = false;
            logger.info("MemGres Engine shutdown successfully");
        } finally {
            engineLock.writeLock().unlock();
        }
    }
    
    /**
     * Create a new schema
     * @param schemaName the name of the schema to create
     * @return true if schema was created, false if it already exists
     */
    public boolean createSchema(String schemaName) {
        validateInitialized();
        engineLock.readLock().lock();
        try {
            Schema existingSchema = schemas.get(schemaName);
            if (existingSchema != null) {
                return false;
            }
            
            Schema newSchema = new Schema(schemaName);
            schemas.put(schemaName, newSchema);
            logger.debug("Created schema: {}", schemaName);
            return true;
        } finally {
            engineLock.readLock().unlock();
        }
    }
    
    /**
     * Drop a schema
     * @param schemaName the name of the schema to drop
     * @param cascade if true, drop all tables in the schema
     * @return true if schema was dropped, false if it didn't exist
     */
    public boolean dropSchema(String schemaName, boolean cascade) {
        validateInitialized();
        if ("public".equals(schemaName)) {
            throw new IllegalArgumentException("Cannot drop public schema");
        }
        
        engineLock.writeLock().lock();
        try {
            Schema schema = schemas.get(schemaName);
            if (schema == null) {
                return false;
            }
            
            if (!cascade && schema.hasTable()) {
                throw new IllegalStateException("Schema contains tables. Use CASCADE to force drop.");
            }
            
            schemas.remove(schemaName);
            logger.debug("Dropped schema: {}", schemaName);
            return true;
        } finally {
            engineLock.writeLock().unlock();
        }
    }
    
    /**
     * Get a schema by name
     * @param schemaName the schema name
     * @return the schema or null if not found
     */
    public Schema getSchema(String schemaName) {
        validateInitialized();
        engineLock.readLock().lock();
        try {
            return schemas.get(schemaName);
        } finally {
            engineLock.readLock().unlock();
        }
    }
    
    /**
     * Create a table in the specified schema
     * @param schemaName the schema name
     * @param table the table to create
     * @return true if table was created, false if it already exists
     */
    public boolean createTable(String schemaName, Table table) {
        validateInitialized();
        Schema schema = getSchema(schemaName);
        if (schema == null) {
            throw new IllegalArgumentException("Schema does not exist: " + schemaName);
        }
        
        return schema.createTable(table);
    }
    
    /**
     * Get a table from the specified schema
     * @param schemaName the schema name
     * @param tableName the table name
     * @return the table or null if not found
     */
    public Table getTable(String schemaName, String tableName) {
        validateInitialized();
        Schema schema = getSchema(schemaName);
        if (schema == null) {
            return null;
        }
        
        return schema.getTable(tableName);
    }
    
    // ===== SEQUENCE MANAGEMENT METHODS =====
    
    /**
     * Create a sequence in the specified schema
     * @param schemaName the schema name
     * @param sequence the sequence to create
     * @return true if sequence was created, false if it already exists
     */
    public boolean createSequence(String schemaName, Sequence sequence) {
        validateInitialized();
        Schema schema = getSchema(schemaName);
        if (schema == null) {
            throw new IllegalArgumentException("Schema does not exist: " + schemaName);
        }
        
        return schema.createSequence(sequence);
    }
    
    /**
     * Drop a sequence from the specified schema
     * @param schemaName the schema name
     * @param sequenceName the sequence name
     * @return true if sequence was dropped, false if it didn't exist
     */
    public boolean dropSequence(String schemaName, String sequenceName) {
        validateInitialized();
        Schema schema = getSchema(schemaName);
        if (schema == null) {
            return false;
        }
        
        return schema.dropSequence(sequenceName);
    }
    
    /**
     * Get a sequence from the specified schema
     * @param schemaName the schema name
     * @param sequenceName the sequence name
     * @return the sequence or null if not found
     */
    public Sequence getSequence(String schemaName, String sequenceName) {
        validateInitialized();
        Schema schema = getSchema(schemaName);
        if (schema == null) {
            return null;
        }
        
        return schema.getSequence(sequenceName);
    }
    
    /**
     * Get the transaction manager
     * @return the transaction manager
     */
    public TransactionManager getTransactionManager() {
        validateInitialized();
        return transactionManager;
    }
    
    /**
     * Check if the engine is initialized
     * @return true if initialized
     */
    public boolean isInitialized() {
        engineLock.readLock().lock();
        try {
            return initialized;
        } finally {
            engineLock.readLock().unlock();
        }
    }
    
    private void validateInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Engine not initialized. Call initialize() first.");
        }
    }
}