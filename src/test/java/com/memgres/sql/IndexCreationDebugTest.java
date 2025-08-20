package com.memgres.sql;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionResult;
import com.memgres.transaction.TransactionIsolationLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Debug test to isolate the CREATE INDEX issue.
 */
public class IndexCreationDebugTest {
    private static final Logger logger = LoggerFactory.getLogger(IndexCreationDebugTest.class);
    
    private MemGresEngine engine;
    private SqlExecutionEngine sqlEngine;
    
    @BeforeEach
    void setUp() {
        engine = new MemGresEngine();
        engine.initialize();
        sqlEngine = new SqlExecutionEngine(engine);
        
        // Create test table
        try {
            String createTableSql = "CREATE TABLE debug_table (id INTEGER, name VARCHAR, age INTEGER)";
            sqlEngine.execute(createTableSql, TransactionIsolationLevel.READ_COMMITTED);
            logger.info("Debug table created successfully");
        } catch (Exception e) {
            logger.error("Failed to create debug table", e);
            throw new RuntimeException("Test setup failed", e);
        }
    }
    
    @AfterEach
    void tearDown() {
        if (engine != null) {
            engine.shutdown();
        }
    }
    
    @Test
    void testBasicIndexCreation() throws Exception {
        logger.info("Testing basic index creation with explicit name");
        
        String createIndexSql = "CREATE INDEX idx_test_name ON debug_table (name)";
        SqlExecutionResult result = sqlEngine.execute(createIndexSql, TransactionIsolationLevel.READ_COMMITTED);
        
        logger.info("Result: success={}, message={}", result.isSuccess(), result.getMessage());
        
        if (!result.isSuccess()) {
            logger.error("Index creation failed: {}", result.getMessage());
        }
    }
    
    @Test
    void testIndexCreationWithoutName() throws Exception {
        logger.info("Testing index creation without explicit name - this should fail");
        
        String createIndexSql = "CREATE INDEX ON debug_table (age)";
        SqlExecutionResult result = sqlEngine.execute(createIndexSql, TransactionIsolationLevel.READ_COMMITTED);
        
        logger.info("Result: success={}, message={}", result.isSuccess(), result.getMessage());
        
        if (!result.isSuccess()) {
            logger.error("Index creation failed: {}", result.getMessage());
        }
    }
    
    @Test
    void testDirectTableIndexCreation() throws Exception {
        logger.info("Testing direct table index creation (bypassing SQL parsing)");
        
        // Get the table directly
        var table = engine.getTable("public", "debug_table");
        if (table == null) {
            logger.error("Table not found!");
            return;
        }
        
        try {
            // Try to create an index directly on the table with null name
            boolean created = table.createIndex(null, java.util.List.of("age"), false, false);
            logger.info("Direct index creation result: {}", created);
        } catch (Exception e) {
            logger.error("Direct index creation failed", e);
        }
    }
}