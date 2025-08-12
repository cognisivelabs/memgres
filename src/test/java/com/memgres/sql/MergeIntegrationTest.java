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
 * Integration tests for MERGE statements.
 * Tests both simple and advanced H2-compatible MERGE syntax parsing and execution.
 */
class MergeIntegrationTest {
    
    private static final Logger logger = LoggerFactory.getLogger(MergeIntegrationTest.class);
    
    private MemGresEngine engine;
    private SqlExecutionEngine sqlEngine;
    
    @BeforeEach
    void setUp() {
        engine = new MemGresEngine();
        engine.initialize();
        sqlEngine = new SqlExecutionEngine(engine);
        
        // Create test tables for MERGE operations
        try {
            String createTargetTableSql = "CREATE TABLE target_table (id INTEGER, name VARCHAR, data_value INTEGER)";
            sqlEngine.execute(createTargetTableSql, TransactionIsolationLevel.READ_COMMITTED);
            
            String createSourceTableSql = "CREATE TABLE source_table (id INTEGER, name VARCHAR, data_value INTEGER)";
            sqlEngine.execute(createSourceTableSql, TransactionIsolationLevel.READ_COMMITTED);
            
            // Insert test data into source_table for subquery tests
            sqlEngine.execute("INSERT INTO source_table VALUES (1, 'Alice', 75)", TransactionIsolationLevel.READ_COMMITTED);
            sqlEngine.execute("INSERT INTO source_table VALUES (2, 'Bob', 25)", TransactionIsolationLevel.READ_COMMITTED);
            sqlEngine.execute("INSERT INTO source_table VALUES (3, 'Charlie', 100)", TransactionIsolationLevel.READ_COMMITTED);
            
            logger.info("Test tables created successfully");
        } catch (Exception e) {
            logger.error("Failed to create test tables", e);
            throw new RuntimeException("Test setup failed", e);
        }
    }
    
    @AfterEach
    void tearDown() {
        if (engine != null) {
            engine.shutdown();
        }
        logger.info("Test teardown complete");
    }
    
    @Test
    void testSimpleMergeStatement() throws Exception {
        logger.info("Testing simple MERGE statement");
        
        String simpleMergeSql = "MERGE INTO target_table KEY(id) VALUES (1, 'Alice', 100)";
        SqlExecutionResult result = sqlEngine.execute(simpleMergeSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.MERGE, result.getType());
        assertTrue(result.isSuccess());
        assertNotNull(result.getMessage());
        assertTrue(result.getMessage().contains("MERGE completed successfully"));
        logger.info("Simple MERGE test passed: {}", result.getMessage());
    }
    
    @Test
    void testSimpleMergeWithMultipleValues() throws Exception {
        logger.info("Testing simple MERGE with multiple values");
        
        String simpleMergeSql = "MERGE INTO target_table KEY(id) VALUES (1, 'Alice', 100), (2, 'Bob', 200)";
        SqlExecutionResult result = sqlEngine.execute(simpleMergeSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.MERGE, result.getType());
        assertTrue(result.isSuccess());
        logger.info("Simple MERGE with multiple values test passed: {}", result.getMessage());
    }
    
    @Test
    void testSimpleMergeWithMultipleKeyColumns() throws Exception {
        logger.info("Testing simple MERGE with multiple key columns");
        
        String simpleMergeSql = "MERGE INTO target_table KEY(id, name) VALUES (1, 'Alice', 100)";
        SqlExecutionResult result = sqlEngine.execute(simpleMergeSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.MERGE, result.getType());
        assertTrue(result.isSuccess());
        logger.info("Simple MERGE with multiple key columns test passed: {}", result.getMessage());
    }
    
    @Test
    void testAdvancedMergeWithTableSource() throws Exception {
        logger.info("Testing advanced MERGE with table source");
        
        String advancedMergeSql = "MERGE INTO target_table USING source_table " +
                                 "ON target_table.id = source_table.id " +
                                 "WHEN MATCHED THEN UPDATE SET data_value = source_table.data_value " +
                                 "WHEN NOT MATCHED THEN INSERT VALUES (source_table.id, source_table.name, source_table.data_value)";
        SqlExecutionResult result = sqlEngine.execute(advancedMergeSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.MERGE, result.getType());
        assertTrue(result.isSuccess());
        logger.info("Advanced MERGE with table source test passed: {}", result.getMessage());
    }
    
    @Test
    void testAdvancedMergeWithAliases() throws Exception {
        logger.info("Testing advanced MERGE with table aliases");
        
        String advancedMergeSql = "MERGE INTO target_table t USING source_table s " +
                                 "ON t.id = s.id " +
                                 "WHEN MATCHED THEN UPDATE SET data_value = s.data_value " +
                                 "WHEN NOT MATCHED THEN INSERT VALUES (s.id, s.name, s.data_value)";
        SqlExecutionResult result = sqlEngine.execute(advancedMergeSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.MERGE, result.getType());
        assertTrue(result.isSuccess());
        logger.info("Advanced MERGE with aliases test passed: {}", result.getMessage());
    }
    
    @Test
    void testAdvancedMergeWithSubquerySource() throws Exception {
        logger.info("Testing advanced MERGE with subquery source");
        
        String advancedMergeSql = "MERGE INTO target_table " +
                                 "USING (SELECT id, name, data_value FROM source_table WHERE data_value > 50) s " +
                                 "ON target_table.id = s.id " +
                                 "WHEN MATCHED THEN UPDATE SET data_value = s.data_value " +
                                 "WHEN NOT MATCHED THEN INSERT VALUES (s.id, s.name, s.data_value)";
        SqlExecutionResult result = sqlEngine.execute(advancedMergeSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.MERGE, result.getType());
        assertTrue(result.isSuccess());
        logger.info("Advanced MERGE with subquery source test passed: {}", result.getMessage());
    }
    
    @Test
    void testAdvancedMergeWithDeleteAction() throws Exception {
        logger.info("Testing advanced MERGE with DELETE action");
        
        String advancedMergeSql = "MERGE INTO target_table USING source_table " +
                                 "ON target_table.id = source_table.id " +
                                 "WHEN MATCHED AND source_table.data_value < 0 THEN DELETE " +
                                 "WHEN MATCHED THEN UPDATE SET data_value = source_table.data_value " +
                                 "WHEN NOT MATCHED THEN INSERT VALUES (source_table.id, source_table.name, source_table.data_value)";
        SqlExecutionResult result = sqlEngine.execute(advancedMergeSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.MERGE, result.getType());
        assertTrue(result.isSuccess());
        logger.info("Advanced MERGE with DELETE action test passed: {}", result.getMessage());
    }
    
    @Test
    void testAdvancedMergeWithConditionalInsert() throws Exception {
        logger.info("Testing advanced MERGE with conditional INSERT");
        
        String advancedMergeSql = "MERGE INTO target_table USING source_table " +
                                 "ON target_table.id = source_table.id " +
                                 "WHEN MATCHED THEN UPDATE SET data_value = source_table.data_value " +
                                 "WHEN NOT MATCHED AND source_table.data_value > 0 THEN INSERT VALUES (source_table.id, source_table.name, source_table.data_value)";
        SqlExecutionResult result = sqlEngine.execute(advancedMergeSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.MERGE, result.getType());
        assertTrue(result.isSuccess());
        logger.info("Advanced MERGE with conditional INSERT test passed: {}", result.getMessage());
    }
    
    @Test
    void testAdvancedMergeWithSpecificColumns() throws Exception {
        logger.info("Testing advanced MERGE with specific column list");
        
        String advancedMergeSql = "MERGE INTO target_table USING source_table " +
                                 "ON target_table.id = source_table.id " +
                                 "WHEN MATCHED THEN UPDATE SET name = source_table.name, data_value = source_table.data_value " +
                                 "WHEN NOT MATCHED THEN INSERT (id, name, data_value) VALUES (source_table.id, source_table.name, source_table.data_value)";
        SqlExecutionResult result = sqlEngine.execute(advancedMergeSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.MERGE, result.getType());
        assertTrue(result.isSuccess());
        logger.info("Advanced MERGE with specific columns test passed: {}", result.getMessage());
    }
    
    @Test
    void testMergeWithComplexConditions() throws Exception {
        logger.info("Testing MERGE with complex conditions");
        
        String complexMergeSql = "MERGE INTO target_table t USING source_table s " +
                                "ON t.id = s.id " +
                                "WHEN MATCHED AND t.data_value < s.data_value THEN UPDATE SET data_value = s.data_value " +
                                "WHEN MATCHED AND t.data_value >= s.data_value THEN DELETE " +
                                "WHEN NOT MATCHED AND s.data_value > 100 THEN INSERT VALUES (s.id, s.name, s.data_value)";
        SqlExecutionResult result = sqlEngine.execute(complexMergeSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.MERGE, result.getType());
        assertTrue(result.isSuccess());
        logger.info("MERGE with complex conditions test passed: {}", result.getMessage());
    }
    
    @Test
    void testMergeCaseInsensitivity() throws Exception {
        logger.info("Testing MERGE case insensitivity");
        
        String[] caseVariations = {
            "merge into target_table key(id) VALUES (1, 'test', 100)",
            "MERGE INTO target_table KEY(id) VALUES (2, 'TEST', 200)",
            "Merge Into target_table Key(id) Values (3, 'Test', 300)",
            "MERGE INTO target_table USING source_table ON target_table.id = source_table.id WHEN MATCHED THEN UPDATE SET data_value = source_table.data_value WHEN NOT MATCHED THEN INSERT VALUES (source_table.id, source_table.name, source_table.data_value)"
        };
        
        for (int i = 0; i < caseVariations.length; i++) {
            String sql = caseVariations[i];
            SqlExecutionResult result = sqlEngine.execute(sql, TransactionIsolationLevel.READ_COMMITTED);
            
            assertEquals(SqlExecutionResult.ResultType.MERGE, result.getType());
            assertTrue(result.isSuccess());
            logger.info("Case variation {} passed: {}", i + 1, sql.substring(0, Math.min(50, sql.length())) + "...");
        }
        
        logger.info("MERGE case insensitivity test passed");
    }
    
    @Test
    void testMergeErrorHandling() throws Exception {
        logger.info("Testing MERGE error handling");
        
        // Test MERGE on non-existent table
        assertThrows(Exception.class, () -> {
            sqlEngine.execute("MERGE INTO nonexistent_table KEY(id) VALUES (1, 'test', 100)", 
                             TransactionIsolationLevel.READ_COMMITTED);
        });
        
        // Test invalid SQL that should fail during parsing
        assertThrows(Exception.class, () -> {
            sqlEngine.execute("MERGE INTO INVALID SYNTAX", 
                             TransactionIsolationLevel.READ_COMMITTED);
        });
        
        logger.info("MERGE error handling tests passed - expected errors were caught");
    }
    
    @Test
    void testMergeParsingValidation() throws Exception {
        logger.info("Testing MERGE parsing validation");
        
        // Test various valid MERGE statement patterns to ensure parsing works correctly
        String[] validMergeStatements = {
            // Simple MERGE variations
            "MERGE INTO target_table KEY(id) VALUES (1, 'test', 100)",
            "MERGE INTO target_table KEY(id, name) VALUES (1, 'test', 100)",
            "MERGE INTO target_table KEY(id) VALUES (1, 'test', 100), (2, 'test2', 200)",
            
            // Advanced MERGE variations
            "MERGE INTO target_table USING source_table ON target_table.id = source_table.id WHEN MATCHED THEN UPDATE SET data_value = source_table.data_value WHEN NOT MATCHED THEN INSERT VALUES (source_table.id, source_table.name, source_table.data_value)",
            "MERGE INTO target_table t USING source_table s ON t.id = s.id WHEN MATCHED THEN DELETE WHEN NOT MATCHED THEN INSERT VALUES (s.id, s.name, s.data_value)",
            "MERGE INTO target_table USING (SELECT * FROM source_table) s ON target_table.id = s.id WHEN MATCHED THEN UPDATE SET data_value = s.data_value WHEN NOT MATCHED THEN INSERT VALUES (s.id, s.name, s.data_value)",
            "MERGE INTO target_table USING source_table ON target_table.id = source_table.id WHEN MATCHED AND target_table.data_value < source_table.data_value THEN UPDATE SET data_value = source_table.data_value WHEN NOT MATCHED AND source_table.data_value > 0 THEN INSERT VALUES (source_table.id, source_table.name, source_table.data_value)"
        };
        
        for (String sql : validMergeStatements) {
            SqlExecutionResult result = sqlEngine.execute(sql, TransactionIsolationLevel.READ_COMMITTED);
            assertEquals(SqlExecutionResult.ResultType.MERGE, result.getType());
            assertTrue(result.isSuccess());
            logger.info("Parsed successfully: {}", sql.substring(0, Math.min(80, sql.length())) + "...");
        }
        
        logger.info("MERGE parsing validation test completed successfully");
    }
    
    @Test
    void testMergeWithExpressions() throws Exception {
        logger.info("Testing MERGE with complex expressions");
        
        String expressionMergeSql = "MERGE INTO target_table USING source_table " +
                                   "ON target_table.id = source_table.id " +
                                   "WHEN MATCHED THEN UPDATE SET data_value = source_table.data_value + 10 " +
                                   "WHEN NOT MATCHED THEN INSERT VALUES (source_table.id, 'prefix_' || source_table.name, source_table.data_value * 2)";
        SqlExecutionResult result = sqlEngine.execute(expressionMergeSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.MERGE, result.getType());
        assertTrue(result.isSuccess());
        logger.info("MERGE with expressions test passed: {}", result.getMessage());
    }
}