package com.memgres.testing;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionResult;
import com.memgres.transaction.Transaction;
import com.memgres.transaction.TransactionIsolationLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Helper class for MemGres testing framework integration.
 * 
 * <p>This class provides the core functionality for managing MemGres database instances
 * in testing environments. It can be used directly or through framework-specific
 * extensions like the JUnit 5 extension.</p>
 * 
 * @since 1.0.0
 */
public class MemGresTestHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(MemGresTestHelper.class);
    
    private final ConcurrentMap<String, MemGresEngine> sharedEngines = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Object> testContext = new ConcurrentHashMap<>();
    
    /**
     * Creates and initializes a new MemGres engine for testing.
     * 
     * @param config the MemGres configuration
     * @return the initialized engine
     * @throws Exception if engine creation fails
     */
    public MemGresEngine createEngine(MemGresConfig config) throws Exception {
        logger.debug("Setting up MemGres database with schema: {}", config.getSchema());
        
        MemGresEngine engine = new MemGresEngine();
        engine.initialize();
        
        // Create schema if specified
        if (!config.getSchema().equals("test") || engine.getSchema(config.getSchema()) == null) {
            engine.createSchema(config.getSchema());
        }
        
        // Initialize database
        initializeDatabase(engine, config);
        
        logger.info("MemGres database ready with schema: {}", config.getSchema());
        return engine;
    }
    
    /**
     * Starts a transaction for the given configuration.
     * 
     * @param engine the MemGres engine
     * @param config the configuration
     * @return the started transaction, or null if not transactional
     */
    public Transaction startTransactionIfNeeded(MemGresEngine engine, MemGresConfig config) {
        if (config.isTransactional()) {
            Transaction transaction = engine.getTransactionManager()
                    .beginTransaction(TransactionIsolationLevel.READ_COMMITTED);
            logger.debug("Started transaction for transactional test");
            return transaction;
        }
        return null;
    }
    
    /**
     * Rolls back a transaction if it exists.
     * 
     * @param engine the MemGres engine
     * @param transaction the transaction to rollback
     * @param config the configuration
     */
    public void rollbackTransactionIfNeeded(MemGresEngine engine, Transaction transaction, MemGresConfig config) {
        if (config.isTransactional() && transaction != null) {
            engine.getTransactionManager().rollbackTransaction(transaction);
            logger.debug("Rolled back transaction for transactional test");
        }
    }
    
    /**
     * Shuts down an engine.
     * 
     * @param engine the engine to shutdown
     */
    public void shutdownEngine(MemGresEngine engine) {
        if (engine != null) {
            logger.debug("Shutting down MemGres database");
            engine.shutdown();
            logger.info("MemGres database shutdown complete");
        }
    }
    
    /**
     * Creates parameter instances for injection.
     * 
     * @param parameterType the parameter type
     * @param engine the MemGres engine
     * @return the parameter instance
     */
    public Object createParameterInstance(Class<?> parameterType, MemGresEngine engine) {
        if (parameterType == MemGresEngine.class) {
            return engine;
        } else if (parameterType == SqlExecutionEngine.class) {
            return new SqlExecutionEngine(engine);
        } else if (parameterType == MemGresTestDataSource.class) {
            return new MemGresTestDataSource(engine);
        }
        
        throw new IllegalArgumentException("Unsupported parameter type: " + parameterType);
    }
    
    private void initializeDatabase(MemGresEngine engine, MemGresConfig config) throws Exception {
        SqlExecutionEngine sqlEngine = new SqlExecutionEngine(engine);
        
        // Execute initialization scripts
        for (String scriptPath : config.getInitScripts()) {
            executeScript(sqlEngine, scriptPath);
        }
    }
    
    private void executeScript(SqlExecutionEngine sqlEngine, String scriptPath) throws Exception {
        logger.debug("Executing SQL script: {}", scriptPath);
        
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(scriptPath)) {
            if (inputStream == null) {
                throw new RuntimeException("SQL script not found: " + scriptPath);
            }
            
            String sql = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            
            // Split by semicolon and execute each statement
            String[] statements = sql.split(";");
            for (String statement : statements) {
                statement = statement.trim();
                if (!statement.isEmpty()) {
                    try {
                        SqlExecutionResult result = sqlEngine.execute(statement);
                        if (!result.isSuccess()) {
                            throw new RuntimeException(
                                    "Failed to execute SQL script: " + scriptPath + 
                                    ", Error: " + result.getMessage());
                        }
                    } catch (com.memgres.sql.execution.SqlExecutionException e) {
                        throw new RuntimeException(
                                "Failed to execute SQL script: " + scriptPath + 
                                ", SQL Error: " + e.getMessage(), e);
                    }
                }
            }
            
            logger.debug("Successfully executed SQL script: {}", scriptPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read SQL script: " + scriptPath, e);
        }
    }
    
    /**
     * Configuration class for MemGres testing.
     */
    public static class MemGresConfig {
        private final String schema;
        private final boolean autoCreateTables;
        private final String[] initScripts;
        private final boolean transactional;
        private final long startupTimeoutMs;
        
        public MemGresConfig(String schema, boolean autoCreateTables, String[] initScripts, 
                            boolean transactional, long startupTimeoutMs) {
            this.schema = schema;
            this.autoCreateTables = autoCreateTables;
            this.initScripts = initScripts;
            this.transactional = transactional;
            this.startupTimeoutMs = startupTimeoutMs;
        }
        
        public static MemGresConfig fromAnnotation(MemGres annotation) {
            return new MemGresConfig(
                annotation.schema(),
                annotation.autoCreateTables(),
                annotation.initScripts(),
                annotation.transactional(),
                annotation.startupTimeoutMs()
            );
        }
        
        public static MemGresConfig defaultConfig() {
            return new MemGresConfig("test", false, new String[0], false, 5000L);
        }
        
        public String getSchema() { return schema; }
        public boolean isAutoCreateTables() { return autoCreateTables; }
        public String[] getInitScripts() { return initScripts; }
        public boolean isTransactional() { return transactional; }
        public long getStartupTimeoutMs() { return startupTimeoutMs; }
    }
}