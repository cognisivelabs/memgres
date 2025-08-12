package com.memgres.sql;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionResult;
import com.memgres.transaction.TransactionIsolationLevel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic ALTER TABLE tests focusing on core functionality.
 */
class AlterTableBasicTest {
    
    private static final Logger logger = LoggerFactory.getLogger(AlterTableBasicTest.class);
    
    private MemGresEngine engine;
    private SqlExecutionEngine sqlEngine;
    
    @BeforeEach
    void setUp() {
        engine = new MemGresEngine();
        engine.initialize();
        sqlEngine = new SqlExecutionEngine(engine);
    }
    
    @AfterEach
    void tearDown() {
        if (engine != null) {
            engine.shutdown();
        }
    }
    
    @Test
    void testSimpleAddColumn() throws Exception {
        logger.info("Testing simple ADD COLUMN without positioning");
        
        // Create test table
        String createTableSql = "CREATE TABLE test_table (id INTEGER)";
        sqlEngine.execute(createTableSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Test simple ADD COLUMN (should work)
        String alterSql = "ALTER TABLE test_table ADD name VARCHAR";
        SqlExecutionResult result = sqlEngine.execute(alterSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertTrue(result.isSuccess());
        logger.info("Simple ADD COLUMN test passed");
    }
    
    @Test  
    void testDropColumn() throws Exception {
        logger.info("Testing DROP COLUMN");
        
        // Create test table with multiple columns
        String createTableSql = "CREATE TABLE test_table (id INTEGER, name VARCHAR, email VARCHAR)";
        sqlEngine.execute(createTableSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Drop column
        String alterSql = "ALTER TABLE test_table DROP name";
        SqlExecutionResult result = sqlEngine.execute(alterSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertTrue(result.isSuccess());
        logger.info("DROP COLUMN test passed");
    }
    
    @Test
    void testRenameColumn() throws Exception {
        logger.info("Testing RENAME COLUMN");
        
        // Create test table
        String createTableSql = "CREATE TABLE test_table (id INTEGER, old_name VARCHAR)";
        sqlEngine.execute(createTableSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Rename column
        String alterSql = "ALTER TABLE test_table ALTER COLUMN old_name RENAME TO new_name";
        SqlExecutionResult result = sqlEngine.execute(alterSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertTrue(result.isSuccess());
        logger.info("RENAME COLUMN test passed");
    }
}