package com.memgres.core;

import com.memgres.memory.MemoryManager;
import com.memgres.memory.MemoryOptimizer;
import com.memgres.storage.Schema;
import com.memgres.storage.Sequence;
import com.memgres.storage.Table;
import com.memgres.transaction.TransactionManager;
import com.memgres.wal.WalTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
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
    private final TriggerManager triggerManager;
    private final MemoryManager memoryManager;
    private final MemoryOptimizer memoryOptimizer;
    private final ReadWriteLock engineLock;
    private volatile boolean initialized;
    
    public MemGresEngine() {
        this.schemas = new ConcurrentHashMap<>();
        this.transactionManager = new TransactionManager();
        this.triggerManager = new TriggerManager();
        this.memoryManager = MemoryManager.getInstance();
        this.memoryOptimizer = new MemoryOptimizer(this);
        this.engineLock = new ReentrantReadWriteLock();
        this.initialized = false;
        logger.info("MemGres Engine created");
    }
    
    /**
     * Constructor with WAL support.
     * @param walDirectory directory for WAL files
     */
    public MemGresEngine(String walDirectory) {
        this.schemas = new ConcurrentHashMap<>();
        try {
            this.transactionManager = new WalTransactionManager(walDirectory);
            logger.info("MemGres Engine created with WAL enabled: {}", walDirectory);
        } catch (Exception e) {
            logger.error("Failed to initialize WAL, falling back to regular transaction manager", e);
            throw new RuntimeException("Failed to initialize WAL transaction manager", e);
        }
        this.triggerManager = new TriggerManager();
        this.memoryManager = MemoryManager.getInstance();
        this.memoryOptimizer = new MemoryOptimizer(this);
        this.engineLock = new ReentrantReadWriteLock();
        this.initialized = false;
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
            
            // Perform WAL recovery if enabled
            if (transactionManager instanceof WalTransactionManager) {
                WalTransactionManager walTxnMgr = (WalTransactionManager) transactionManager;
                try {
                    walTxnMgr.performRecovery();
                    logger.info("WAL recovery completed successfully");
                } catch (Exception e) {
                    logger.error("WAL recovery failed", e);
                    throw new RuntimeException("WAL recovery failed during initialization", e);
                }
            }
            
            // Create default 'public' schema
            Schema publicSchema = new Schema("public");
            schemas.put("public", publicSchema);
            
            // Register memory alert handler
            memoryManager.registerAlertHandler(alert -> {
                logger.warn("Memory alert: {} - {}", alert.getLevel(), alert.getMessage());
                if (alert.getLevel() == MemoryManager.MemoryAlert.Level.CRITICAL) {
                    // Trigger immediate optimization on critical memory pressure
                    memoryOptimizer.optimize();
                }
            });
            
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
            
            memoryOptimizer.shutdown();
            transactionManager.shutdown();
            triggerManager.shutdown();
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
            throw new IllegalArgumentException("Cannot drop schema \"public\"");
        }
        
        engineLock.writeLock().lock();
        try {
            Schema schema = schemas.get(schemaName);
            if (schema == null) {
                return false;
            }
            
            if (!cascade && schema.hasTable()) {
                throw new IllegalStateException("Cannot drop schema \"" + schemaName + "\" because it contains objects; use CASCADE");
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
     * Get the trigger manager for this engine.
     */
    public TriggerManager getTriggerManager() {
        validateInitialized();
        return triggerManager;
    }
    
    /**
     * Get the memory manager for this engine.
     */
    public MemoryManager getMemoryManager() {
        validateInitialized();
        return memoryManager;
    }
    
    /**
     * Get the memory optimizer for this engine.
     */
    public MemoryOptimizer getMemoryOptimizer() {
        validateInitialized();
        return memoryOptimizer;
    }
    
    /**
     * Check if WAL (Write-Ahead Logging) is enabled.
     */
    public boolean isWalEnabled() {
        return transactionManager instanceof WalTransactionManager;
    }
    
    /**
     * Get the WAL transaction manager if WAL is enabled.
     * @return WalTransactionManager or null if WAL is not enabled
     */
    public WalTransactionManager getWalTransactionManager() {
        if (transactionManager instanceof WalTransactionManager) {
            return (WalTransactionManager) transactionManager;
        }
        return null;
    }
    
    /**
     * Get all schemas in the engine.
     */
    public Collection<Schema> getAllSchemas() {
        validateInitialized();
        engineLock.readLock().lock();
        try {
            return schemas.values();
        } finally {
            engineLock.readLock().unlock();
        }
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