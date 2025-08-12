package com.memgres.sql;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionResult;
import com.memgres.transaction.TransactionIsolationLevel;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Debug test to isolate SEQUENCE parsing issues.
 */
class SequenceDebugTest {
    
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
    void testWorkingCreateTable() throws Exception {
        // Test a known working statement
        String createTableSql = "CREATE TABLE test_table (id INTEGER)";
        SqlExecutionResult result = sqlEngine.execute(createTableSql, TransactionIsolationLevel.READ_COMMITTED);
        
        System.out.println("CREATE TABLE result type: " + result.getType());
        System.out.println("CREATE TABLE affected rows: " + result.getAffectedRows());
        System.out.println("CREATE TABLE success: " + result.isSuccess());
        
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
    }
    
    @Test
    void testCreateSequenceParsing() throws Exception {
        // Test the CREATE SEQUENCE statement
        String createSeqSql = "CREATE SEQUENCE test_seq";
        
        try {
            SqlExecutionResult result = sqlEngine.execute(createSeqSql, TransactionIsolationLevel.READ_COMMITTED);
            
            System.out.println("CREATE SEQUENCE result type: " + result.getType());
            System.out.println("CREATE SEQUENCE affected rows: " + result.getAffectedRows());
            System.out.println("CREATE SEQUENCE success: " + result.isSuccess());
            
            // This is what we expect to see
            assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        } catch (Exception e) {
            System.out.println("CREATE SEQUENCE parsing failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}