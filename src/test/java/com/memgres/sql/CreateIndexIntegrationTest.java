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
 * Integration tests for CREATE INDEX and DROP INDEX statements.
 * Tests H2-compatible index DDL parsing and execution.
 */
class CreateIndexIntegrationTest {
    
    private static final Logger logger = LoggerFactory.getLogger(CreateIndexIntegrationTest.class);
    
    private MemGresEngine engine;
    private SqlExecutionEngine sqlEngine;
    
    @BeforeEach
    void setUp() {
        engine = new MemGresEngine();
        engine.initialize();
        sqlEngine = new SqlExecutionEngine(engine);
        
        // Create test table for all index tests
        try {
            String createTableSql = "CREATE TABLE test_table (id INTEGER, name VARCHAR, email VARCHAR, age INTEGER, score DECIMAL)";
            sqlEngine.execute(createTableSql, TransactionIsolationLevel.READ_COMMITTED);
            logger.info("Test table created successfully");
        } catch (Exception e) {
            logger.error("Failed to create test table", e);
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
    void testBasicCreateIndex() throws Exception {
        logger.info("Testing basic CREATE INDEX statement");
        
        String createIndexSql = "CREATE INDEX idx_name ON test_table (name)";
        SqlExecutionResult result = sqlEngine.execute(createIndexSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        assertTrue(result.isSuccess());
        assertNotNull(result.getMessage());
        logger.info("Basic CREATE INDEX test passed: {}", result.getMessage());
    }
    
    @Test
    void testCreateUniqueIndex() throws Exception {
        logger.info("Testing CREATE UNIQUE INDEX statement");
        
        String createIndexSql = "CREATE UNIQUE INDEX idx_unique_email ON test_table (email)";
        SqlExecutionResult result = sqlEngine.execute(createIndexSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        assertTrue(result.isSuccess());
        logger.info("CREATE UNIQUE INDEX test passed: {}", result.getMessage());
    }
    
    @Test
    void testCreateIndexWithIfNotExists() throws Exception {
        logger.info("Testing CREATE INDEX IF NOT EXISTS statement");
        
        String createIndexSql = "CREATE INDEX IF NOT EXISTS idx_conditional ON test_table (age)";
        SqlExecutionResult result = sqlEngine.execute(createIndexSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        assertTrue(result.isSuccess());
        logger.info("CREATE INDEX IF NOT EXISTS test passed: {}", result.getMessage());
    }
    
    @Test
    void testCreateSpatialIndex() throws Exception {
        logger.info("Testing CREATE SPATIAL INDEX statement");
        
        String createIndexSql = "CREATE SPATIAL INDEX idx_spatial ON test_table (name)";
        SqlExecutionResult result = sqlEngine.execute(createIndexSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        assertTrue(result.isSuccess());
        logger.info("CREATE SPATIAL INDEX test passed: {}", result.getMessage());
    }
    
    @Test
    void testCreateUniqueNullsDistinctIndex() throws Exception {
        logger.info("Testing CREATE UNIQUE NULLS DISTINCT INDEX statement");
        
        String createIndexSql = "CREATE UNIQUE NULLS DISTINCT INDEX idx_nulls_distinct ON test_table (email)";
        SqlExecutionResult result = sqlEngine.execute(createIndexSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        assertTrue(result.isSuccess());
        logger.info("CREATE UNIQUE NULLS DISTINCT INDEX test passed: {}", result.getMessage());
    }
    
    @Test
    void testCreateIndexWithMultipleColumns() throws Exception {
        logger.info("Testing CREATE INDEX with multiple columns");
        
        String createIndexSql = "CREATE INDEX idx_multi ON test_table (name, age)";
        SqlExecutionResult result = sqlEngine.execute(createIndexSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        assertTrue(result.isSuccess());
        logger.info("Multi-column CREATE INDEX test passed: {}", result.getMessage());
    }
    
    @Test
    void testCreateIndexWithSortOrder() throws Exception {
        logger.info("Testing CREATE INDEX with sort order specifications");
        
        String createIndexSql = "CREATE INDEX idx_sorted ON test_table (name ASC, age DESC)";
        SqlExecutionResult result = sqlEngine.execute(createIndexSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        assertTrue(result.isSuccess());
        logger.info("CREATE INDEX with sort order test passed: {}", result.getMessage());
    }
    
    @Test
    void testCreateIndexWithNullsOrdering() throws Exception {
        logger.info("Testing CREATE INDEX with NULLS FIRST/LAST");
        
        String createIndexSql = "CREATE INDEX idx_nulls ON test_table (name ASC NULLS FIRST, age DESC NULLS LAST)";
        SqlExecutionResult result = sqlEngine.execute(createIndexSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        assertTrue(result.isSuccess());
        logger.info("CREATE INDEX with NULLS ordering test passed: {}", result.getMessage());
    }
    
    @Test
    void testCreateIndexWithIncludeColumns() throws Exception {
        logger.info("Testing CREATE INDEX with INCLUDE columns");
        
        String createIndexSql = "CREATE INDEX idx_include ON test_table (name) INCLUDE (email, score)";
        SqlExecutionResult result = sqlEngine.execute(createIndexSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        assertTrue(result.isSuccess());
        logger.info("CREATE INDEX with INCLUDE columns test passed: {}", result.getMessage());
    }
    
    @Test
    void testCreateIndexWithoutName() throws Exception {
        logger.info("Testing CREATE INDEX without explicit name");
        
        String createIndexSql = "CREATE INDEX ON test_table (age)";
        SqlExecutionResult result = sqlEngine.execute(createIndexSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        assertTrue(result.isSuccess());
        logger.info("CREATE INDEX without name test passed: {}", result.getMessage());
    }
    
    @Test
    void testComplexCreateIndexStatement() throws Exception {
        logger.info("Testing complex CREATE INDEX statement with all options");
        
        String createIndexSql = "CREATE UNIQUE NULLS DISTINCT INDEX IF NOT EXISTS idx_complex ON test_table (name ASC NULLS FIRST, age DESC NULLS LAST) INCLUDE (email, score)";
        SqlExecutionResult result = sqlEngine.execute(createIndexSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        assertTrue(result.isSuccess());
        logger.info("Complex CREATE INDEX test passed: {}", result.getMessage());
    }
    
    @Test
    void testBasicDropIndex() throws Exception {
        logger.info("Testing basic DROP INDEX statement");
        
        // Create an index first
        sqlEngine.execute("CREATE INDEX idx_to_drop ON test_table (name)", TransactionIsolationLevel.READ_COMMITTED);
        
        String dropIndexSql = "DROP INDEX idx_to_drop";
        SqlExecutionResult result = sqlEngine.execute(dropIndexSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        assertTrue(result.isSuccess());
        logger.info("Basic DROP INDEX test passed: {}", result.getMessage());
    }
    
    @Test
    void testDropIndexIfExists() throws Exception {
        logger.info("Testing DROP INDEX IF EXISTS statement");
        
        String dropIndexSql = "DROP INDEX IF EXISTS idx_nonexistent";
        SqlExecutionResult result = sqlEngine.execute(dropIndexSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        assertTrue(result.isSuccess());
        logger.info("DROP INDEX IF EXISTS test passed: {}", result.getMessage());
    }
    
    @Test
    void testIndexErrorHandling() throws Exception {
        logger.info("Testing index-related error handling");
        
        // Test real validation with actual implementation
        
        // Test CREATE INDEX on non-existent table (should now fail with real implementation)
        assertThrows(Exception.class, () -> {
            sqlEngine.execute("CREATE INDEX idx_bad ON nonexistent_table (id)", 
                             TransactionIsolationLevel.READ_COMMITTED);
        });
        
        // Test CREATE INDEX on non-existent column (should return failed result)
        SqlExecutionResult result2 = sqlEngine.execute("CREATE INDEX idx_bad ON test_table (nonexistent_column)", 
                                                       TransactionIsolationLevel.READ_COMMITTED);
        assertEquals(SqlExecutionResult.ResultType.DDL, result2.getType());
        assertFalse(result2.isSuccess());
        assertTrue(result2.getMessage().contains("Column does not exist"));
        
        // Test invalid SQL that should fail during parsing
        assertThrows(Exception.class, () -> {
            sqlEngine.execute("CREATE INDEX INVALID SYNTAX", 
                             TransactionIsolationLevel.READ_COMMITTED);
        });
        
        logger.info("Index error handling tests passed - real validation works correctly");
    }
    
    @Test
    void testCreateIndexCaseInsensitivity() throws Exception {
        logger.info("Testing CREATE INDEX case insensitivity");
        
        String[] caseVariations = {
            "create index idx_lower on test_table (name)",
            "CREATE INDEX idx_upper ON test_table (email)", 
            "Create Index idx_mixed On test_table (age)",
            "CREATE unique INDEX idx_unique_case ON test_table (score)"
        };
        
        for (int i = 0; i < caseVariations.length; i++) {
            String sql = caseVariations[i];
            SqlExecutionResult result = sqlEngine.execute(sql, TransactionIsolationLevel.READ_COMMITTED);
            
            assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
            assertTrue(result.isSuccess());
            logger.info("Case variation {} passed: {}", i + 1, sql);
        }
        
        logger.info("CREATE INDEX case insensitivity test passed");
    }
    
    @Test
    void testIndexWithFunctionalParsing() throws Exception {
        logger.info("Testing index parsing validation");
        
        // This test ensures the parser correctly handles various edge cases
        // Even though implementation is placeholder, the parsing should work
        
        String[] validIndexStatements = {
            "CREATE INDEX ON test_table (name)",
            "CREATE UNIQUE INDEX idx1 ON test_table (email ASC)",
            "CREATE INDEX idx2 ON test_table (name DESC NULLS FIRST)",
            "CREATE SPATIAL INDEX idx3 ON test_table (name, age)",
            "CREATE UNIQUE NULLS DISTINCT INDEX IF NOT EXISTS idx4 ON test_table (score) INCLUDE (name)"
        };
        
        for (String sql : validIndexStatements) {
            SqlExecutionResult result = sqlEngine.execute(sql, TransactionIsolationLevel.READ_COMMITTED);
            assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
            assertTrue(result.isSuccess());
            logger.info("Parsed successfully: {}", sql);
        }
        
        logger.info("Index parsing validation test completed successfully");
    }
}