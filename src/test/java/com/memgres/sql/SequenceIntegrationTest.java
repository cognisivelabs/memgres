package com.memgres.sql;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionResult;
import com.memgres.transaction.TransactionIsolationLevel;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive integration tests for SEQUENCE support.
 * Tests CREATE SEQUENCE, DROP SEQUENCE, NEXT VALUE FOR, and CURRENT VALUE FOR functionality.
 */
class SequenceIntegrationTest {
    
    private static final Logger logger = LoggerFactory.getLogger(SequenceIntegrationTest.class);
    private MemGresEngine engine;
    private SqlExecutionEngine sqlEngine;
    
    @BeforeEach
    void setUp() {
        engine = new MemGresEngine();
        engine.initialize();
        sqlEngine = new SqlExecutionEngine(engine);
        logger.info("Test setup completed");
    }
    
    @AfterEach
    void tearDown() {
        if (engine != null) {
            engine.shutdown();
            logger.info("Test cleanup completed");
        }
    }
    
    // ===== BASIC CREATE/DROP SEQUENCE TESTS =====
    
    @Test
    void testCreateBasicSequence() throws Exception {
        logger.info("Testing basic CREATE SEQUENCE");
        
        String createSql = "CREATE SEQUENCE test_seq";
        SqlExecutionResult result = sqlEngine.execute(createSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        assertTrue(result.isSuccess());
        
        // Verify sequence exists
        assertNotNull(engine.getSequence("public", "test_seq"));
        logger.info("Basic CREATE SEQUENCE test passed");
    }
    
    @Test
    void testCreateSequenceWithOptions() throws Exception {
        logger.info("Testing CREATE SEQUENCE with options");
        
        String createSql = "CREATE SEQUENCE test_seq AS BIGINT START WITH 100 INCREMENT BY 5 MINVALUE 1 MAXVALUE 1000 CACHE 50";
        SqlExecutionResult result = sqlEngine.execute(createSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        assertTrue(result.isSuccess());
        
        // Verify sequence with options
        var sequence = engine.getSequence("public", "test_seq");
        assertNotNull(sequence);
        assertEquals(100L, sequence.getStartWith());
        assertEquals(5L, sequence.getIncrementBy());
        assertEquals(1L, sequence.getMinValue());
        assertEquals(1000L, sequence.getMaxValue());
        assertEquals(50L, sequence.getCacheSize());
        
        logger.info("CREATE SEQUENCE with options test passed");
    }
    
    @Test
    void testCreateSequenceIfNotExists() throws Exception {
        logger.info("Testing CREATE SEQUENCE IF NOT EXISTS");
        
        // Create sequence first time
        String createSql = "CREATE SEQUENCE IF NOT EXISTS test_seq";
        SqlExecutionResult result1 = sqlEngine.execute(createSql, TransactionIsolationLevel.READ_COMMITTED);
        assertTrue(result1.isSuccess());
        
        // Create same sequence again with IF NOT EXISTS
        SqlExecutionResult result2 = sqlEngine.execute(createSql, TransactionIsolationLevel.READ_COMMITTED);
        assertEquals(0, result2.getAffectedRows()); // Should not create duplicate
        assertTrue(result2.isSuccess());
        
        logger.info("CREATE SEQUENCE IF NOT EXISTS test passed");
    }
    
    @Test
    void testCreateSequenceDuplicateError() throws Exception {
        logger.info("Testing CREATE SEQUENCE duplicate error");
        
        String createSql = "CREATE SEQUENCE test_seq";
        sqlEngine.execute(createSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Try to create duplicate without IF NOT EXISTS
        assertThrows(Exception.class, () -> {
            sqlEngine.execute(createSql, TransactionIsolationLevel.READ_COMMITTED);
        }, "Should throw exception for duplicate sequence");
        
        logger.info("CREATE SEQUENCE duplicate error test passed");
    }
    
    @Test
    void testDropSequence() throws Exception {
        logger.info("Testing DROP SEQUENCE");
        
        // Create sequence first
        sqlEngine.execute("CREATE SEQUENCE test_seq", TransactionIsolationLevel.READ_COMMITTED);
        assertNotNull(engine.getSequence("public", "test_seq"));
        
        // Drop sequence
        String dropSql = "DROP SEQUENCE test_seq";
        SqlExecutionResult result = sqlEngine.execute(dropSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        assertTrue(result.isSuccess());
        
        // Verify sequence is gone
        assertNull(engine.getSequence("public", "test_seq"));
        logger.info("DROP SEQUENCE test passed");
    }
    
    @Test
    void testDropSequenceIfExists() throws Exception {
        logger.info("Testing DROP SEQUENCE IF EXISTS");
        
        // Drop non-existent sequence with IF EXISTS
        String dropSql = "DROP SEQUENCE IF EXISTS non_existent_seq";
        SqlExecutionResult result = sqlEngine.execute(dropSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(0, result.getAffectedRows()); // Should not affect anything
        assertTrue(result.isSuccess());
        
        logger.info("DROP SEQUENCE IF EXISTS test passed");
    }
    
    @Test
    void testDropSequenceNotFoundError() throws Exception {
        logger.info("Testing DROP SEQUENCE not found error");
        
        // Try to drop non-existent sequence without IF EXISTS
        String dropSql = "DROP SEQUENCE non_existent_seq";
        assertThrows(Exception.class, () -> {
            sqlEngine.execute(dropSql, TransactionIsolationLevel.READ_COMMITTED);
        }, "Should throw exception for non-existent sequence");
        
        logger.info("DROP SEQUENCE not found error test passed");
    }
    
    // ===== SEQUENCE VALUE GENERATION TESTS =====
    
    @Test
    void testNextValueFor() throws Exception {
        logger.info("Testing NEXT VALUE FOR");
        
        // Create sequence
        sqlEngine.execute("CREATE SEQUENCE test_seq START WITH 10 INCREMENT BY 2", 
                         TransactionIsolationLevel.READ_COMMITTED);
        
        // Test NEXT VALUE FOR in SELECT
        String selectSql = "SELECT NEXT VALUE FOR test_seq as next_val";
        SqlExecutionResult result = sqlEngine.execute(selectSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        assertEquals(1, result.getRows().size());
        
        // First call should return start value (10)
        Object firstValue = result.getRows().get(0).getData()[0];
        assertEquals(10L, firstValue);
        
        // Second call should return 12
        result = sqlEngine.execute(selectSql, TransactionIsolationLevel.READ_COMMITTED);
        Object secondValue = result.getRows().get(0).getData()[0];
        assertEquals(12L, secondValue);
        
        logger.info("NEXT VALUE FOR test passed: {} -> {}", firstValue, secondValue);
    }
    
    @Test
    void testCurrentValueFor() throws Exception {
        logger.info("Testing CURRENT VALUE FOR");
        
        // Create sequence
        sqlEngine.execute("CREATE SEQUENCE test_seq START WITH 5", 
                         TransactionIsolationLevel.READ_COMMITTED);
        
        // Call NEXT VALUE FOR first to initialize sequence
        sqlEngine.execute("SELECT NEXT VALUE FOR test_seq", TransactionIsolationLevel.READ_COMMITTED);
        
        // Test CURRENT VALUE FOR
        String selectSql = "SELECT CURRENT VALUE FOR test_seq as current_val";
        SqlExecutionResult result = sqlEngine.execute(selectSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        assertEquals(1, result.getRows().size());
        
        Object currentValue = result.getRows().get(0).getData()[0];
        assertEquals(5L, currentValue); // Should return start value
        
        logger.info("CURRENT VALUE FOR test passed: {}", currentValue);
    }
    
    @Test
    void testCurrentValueForUnaccessed() throws Exception {
        logger.info("Testing CURRENT VALUE FOR on unaccessed sequence");
        
        // Create sequence but don't access it
        sqlEngine.execute("CREATE SEQUENCE test_seq", TransactionIsolationLevel.READ_COMMITTED);
        
        // Try to get current value before any access
        String selectSql = "SELECT CURRENT VALUE FOR test_seq";
        assertThrows(Exception.class, () -> {
            sqlEngine.execute(selectSql, TransactionIsolationLevel.READ_COMMITTED);
        }, "Should throw exception for unaccessed sequence");
        
        logger.info("CURRENT VALUE FOR unaccessed sequence test passed");
    }
    
    // ===== ADVANCED SEQUENCE TESTS =====
    
    @Test
    void testSequenceInInsertStatement() throws Exception {
        logger.info("Testing SEQUENCE in INSERT statement");
        
        // Create table and sequence
        sqlEngine.execute("CREATE TABLE test_table (id INTEGER, name TEXT)", 
                         TransactionIsolationLevel.READ_COMMITTED);
        sqlEngine.execute("CREATE SEQUENCE id_seq START WITH 1", 
                         TransactionIsolationLevel.READ_COMMITTED);
        
        // Insert using sequence
        String insertSql = "INSERT INTO test_table (id, name) VALUES (NEXT VALUE FOR id_seq, 'Alice')";
        SqlExecutionResult result = sqlEngine.execute(insertSql, TransactionIsolationLevel.READ_COMMITTED);
        assertEquals(1, result.getAffectedRows());
        
        // Insert another row
        insertSql = "INSERT INTO test_table (id, name) VALUES (NEXT VALUE FOR id_seq, 'Bob')";
        result = sqlEngine.execute(insertSql, TransactionIsolationLevel.READ_COMMITTED);
        assertEquals(1, result.getAffectedRows());
        
        // Verify data
        String selectSql = "SELECT id, name FROM test_table ORDER BY id";
        result = sqlEngine.execute(selectSql, TransactionIsolationLevel.READ_COMMITTED);
        assertEquals(2, result.getRows().size());
        assertEquals(1, result.getRows().get(0).getData()[0]);
        assertEquals("Alice", result.getRows().get(0).getData()[1]);
        assertEquals(2, result.getRows().get(1).getData()[0]);
        assertEquals("Bob", result.getRows().get(1).getData()[1]);
        
        logger.info("SEQUENCE in INSERT statement test passed");
    }
    
    @Test
    void testSequenceWithDifferentDataTypes() throws Exception {
        logger.info("Testing SEQUENCE with different data types");
        
        // Test INTEGER sequence
        sqlEngine.execute("CREATE SEQUENCE int_seq AS INTEGER START WITH 1", 
                         TransactionIsolationLevel.READ_COMMITTED);
        
        // Test SMALLINT sequence  
        sqlEngine.execute("CREATE SEQUENCE small_seq AS SMALLINT START WITH 100", 
                         TransactionIsolationLevel.READ_COMMITTED);
        
        // Test BIGINT sequence
        sqlEngine.execute("CREATE SEQUENCE big_seq AS BIGINT START WITH 1000000", 
                         TransactionIsolationLevel.READ_COMMITTED);
        
        // Test each sequence
        SqlExecutionResult result;
        
        result = sqlEngine.execute("SELECT NEXT VALUE FOR int_seq", TransactionIsolationLevel.READ_COMMITTED);
        assertEquals(1L, result.getRows().get(0).getData()[0]);
        
        result = sqlEngine.execute("SELECT NEXT VALUE FOR small_seq", TransactionIsolationLevel.READ_COMMITTED);
        assertEquals(100L, result.getRows().get(0).getData()[0]);
        
        result = sqlEngine.execute("SELECT NEXT VALUE FOR big_seq", TransactionIsolationLevel.READ_COMMITTED);
        assertEquals(1000000L, result.getRows().get(0).getData()[0]);
        
        logger.info("SEQUENCE with different data types test passed");
    }
    
    @Test
    void testSequenceWithNegativeIncrement() throws Exception {
        logger.info("Testing SEQUENCE with negative increment");
        
        sqlEngine.execute("CREATE SEQUENCE desc_seq START WITH 100 INCREMENT BY -5", 
                         TransactionIsolationLevel.READ_COMMITTED);
        
        // Test descending sequence
        SqlExecutionResult result;
        
        result = sqlEngine.execute("SELECT NEXT VALUE FOR desc_seq", TransactionIsolationLevel.READ_COMMITTED);
        assertEquals(100L, result.getRows().get(0).getData()[0]);
        
        result = sqlEngine.execute("SELECT NEXT VALUE FOR desc_seq", TransactionIsolationLevel.READ_COMMITTED);
        assertEquals(95L, result.getRows().get(0).getData()[0]);
        
        result = sqlEngine.execute("SELECT NEXT VALUE FOR desc_seq", TransactionIsolationLevel.READ_COMMITTED);
        assertEquals(90L, result.getRows().get(0).getData()[0]);
        
        logger.info("SEQUENCE with negative increment test passed");
    }
    
    @Test
    void testSequenceWithMinMaxValues() throws Exception {
        logger.info("Testing SEQUENCE with MIN/MAX values");
        
        sqlEngine.execute("CREATE SEQUENCE bounded_seq START WITH 8 INCREMENT BY 1 MINVALUE 5 MAXVALUE 10", 
                         TransactionIsolationLevel.READ_COMMITTED);
        
        SqlExecutionResult result;
        
        // Should start at 8
        result = sqlEngine.execute("SELECT NEXT VALUE FOR bounded_seq", TransactionIsolationLevel.READ_COMMITTED);
        assertEquals(8L, result.getRows().get(0).getData()[0]);
        
        // Should go to 9
        result = sqlEngine.execute("SELECT NEXT VALUE FOR bounded_seq", TransactionIsolationLevel.READ_COMMITTED);
        assertEquals(9L, result.getRows().get(0).getData()[0]);
        
        // Should go to 10 (max value)
        result = sqlEngine.execute("SELECT NEXT VALUE FOR bounded_seq", TransactionIsolationLevel.READ_COMMITTED);
        assertEquals(10L, result.getRows().get(0).getData()[0]);
        
        // Next call should throw exception (reached max, no cycle)
        assertThrows(Exception.class, () -> {
            sqlEngine.execute("SELECT NEXT VALUE FOR bounded_seq", TransactionIsolationLevel.READ_COMMITTED);
        }, "Should throw exception when reaching max value without cycle");
        
        logger.info("SEQUENCE with MIN/MAX values test passed");
    }
    
    @Test
    void testSequenceNonExistentError() throws Exception {
        logger.info("Testing SEQUENCE non-existent error");
        
        // Try to use non-existent sequence
        assertThrows(Exception.class, () -> {
            sqlEngine.execute("SELECT NEXT VALUE FOR non_existent_seq", TransactionIsolationLevel.READ_COMMITTED);
        }, "Should throw exception for non-existent sequence");
        
        assertThrows(Exception.class, () -> {
            sqlEngine.execute("SELECT CURRENT VALUE FOR non_existent_seq", TransactionIsolationLevel.READ_COMMITTED);
        }, "Should throw exception for non-existent sequence");
        
        logger.info("SEQUENCE non-existent error test passed");
    }
    
    @Test
    void testSequenceCaseInsensitive() throws Exception {
        logger.info("Testing SEQUENCE case insensitive names");
        
        // Create sequence with mixed case
        sqlEngine.execute("CREATE SEQUENCE MyTestSeq START WITH 1", 
                         TransactionIsolationLevel.READ_COMMITTED);
        
        // Access with different cases
        SqlExecutionResult result;
        
        result = sqlEngine.execute("SELECT NEXT VALUE FOR MyTestSeq", TransactionIsolationLevel.READ_COMMITTED);
        assertEquals(1L, result.getRows().get(0).getData()[0]);
        
        result = sqlEngine.execute("SELECT NEXT VALUE FOR mytestseq", TransactionIsolationLevel.READ_COMMITTED);
        assertEquals(2L, result.getRows().get(0).getData()[0]);
        
        result = sqlEngine.execute("SELECT NEXT VALUE FOR MYTESTSEQ", TransactionIsolationLevel.READ_COMMITTED);
        assertEquals(3L, result.getRows().get(0).getData()[0]);
        
        logger.info("SEQUENCE case insensitive test passed");
    }
}