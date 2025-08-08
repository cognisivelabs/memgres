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
 * Simple subquery test to debug parsing and execution issues.
 */
class SimpleSubqueryTest {
    
    private static final Logger logger = LoggerFactory.getLogger(SimpleSubqueryTest.class);
    
    private MemGresEngine engine;
    private SqlExecutionEngine sqlEngine;
    
    @BeforeEach
    void setUp() {
        engine = new MemGresEngine();
        engine.initialize();
        sqlEngine = new SqlExecutionEngine(engine);
        logger.info("Simple subquery test setup complete");
    }
    
    @AfterEach
    void tearDown() {
        if (engine != null) {
            engine.shutdown();
        }
        logger.info("Simple subquery test teardown complete");
    }
    
    @Test
    void testBasicSelectFirst() throws Exception {
        logger.info("Testing basic SELECT without subqueries");
        
        // First test basic functionality 
        String createTableSql = "CREATE TABLE test (id INTEGER, name VARCHAR(255))";
        sqlEngine.execute(createTableSql, TransactionIsolationLevel.READ_COMMITTED);
        
        String insertSql = "INSERT INTO test VALUES (1, 'Alice')";
        sqlEngine.execute(insertSql, TransactionIsolationLevel.READ_COMMITTED);
        
        String selectSql = "SELECT * FROM test";
        SqlExecutionResult result = sqlEngine.execute(selectSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        assertEquals(1, result.getRows().size());
        
        logger.info("Basic SELECT test passed - {} rows returned", result.getRows().size());
    }
    
    @Test
    void testSimplestSubquery() throws Exception {
        logger.info("Testing simplest possible subquery");
        
        String createTableSql = "CREATE TABLE test (id INTEGER)";
        sqlEngine.execute(createTableSql, TransactionIsolationLevel.READ_COMMITTED);
        
        String insertSql = "INSERT INTO test VALUES (42)";
        sqlEngine.execute(insertSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Test the simplest scalar subquery
        String subquerySql = "SELECT (SELECT 123) as result";
        SqlExecutionResult result = sqlEngine.execute(subquerySql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        assertEquals(1, result.getRows().size());
        assertEquals(123L, result.getRows().get(0).getData()[0]);
        
        logger.info("Simplest subquery test passed");
    }
}