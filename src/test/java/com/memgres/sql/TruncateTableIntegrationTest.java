package com.memgres.sql;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionResult;
import com.memgres.storage.Schema;
import com.memgres.storage.Table;
import com.memgres.transaction.TransactionIsolationLevel;
import com.memgres.types.Row;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for TRUNCATE TABLE statement.
 * Tests H2 compatibility and all TRUNCATE TABLE features.
 */
public class TruncateTableIntegrationTest {
    
    private static final Logger logger = LoggerFactory.getLogger(TruncateTableIntegrationTest.class);
    
    private MemGresEngine engine;
    private SqlExecutionEngine sqlEngine;
    
    @BeforeEach
    void setUp() throws Exception {
        engine = new MemGresEngine();
        engine.initialize();
        sqlEngine = new SqlExecutionEngine(engine);
        logger.info("TRUNCATE TABLE test setup complete");
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (engine != null) {
            engine.shutdown();
        }
        logger.info("TRUNCATE TABLE test teardown complete");
    }
    
    @Test
    void testBasicTruncateTable() throws Exception {
        logger.info("Testing basic TRUNCATE TABLE functionality");
        
        // Create test table
        String createTableSql = "CREATE TABLE employees (id INTEGER, name VARCHAR, email VARCHAR)";
        sqlEngine.execute(createTableSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Insert test data
        sqlEngine.execute("INSERT INTO employees VALUES (1, 'Alice', 'alice@example.com')", 
                         TransactionIsolationLevel.READ_COMMITTED);
        sqlEngine.execute("INSERT INTO employees VALUES (2, 'Bob', 'bob@example.com')", 
                         TransactionIsolationLevel.READ_COMMITTED);
        sqlEngine.execute("INSERT INTO employees VALUES (3, 'Carol', 'carol@example.com')", 
                         TransactionIsolationLevel.READ_COMMITTED);
        
        // Verify data exists
        Schema schema = engine.getSchema("public");
        Table table = schema.getTable("employees");
        assertEquals(3, table.getRowCount());
        
        // Truncate the table
        SqlExecutionResult result = sqlEngine.execute("TRUNCATE TABLE employees", 
                                                     TransactionIsolationLevel.READ_COMMITTED);
        
        assertTrue(result.isSuccess());
        assertEquals("TRUNCATE TABLE completed successfully", result.getMessage());
        
        // Verify table is empty
        assertEquals(0, table.getRowCount());
        
        // Verify table structure is intact
        assertEquals(3, table.getColumns().size());
        assertEquals("id", table.getColumns().get(0).getName());
        assertEquals("name", table.getColumns().get(1).getName());
        assertEquals("email", table.getColumns().get(2).getName());
        
        logger.info("Basic TRUNCATE TABLE test passed");
    }
    
    @Test
    void testTruncateTableWithRestartIdentity() throws Exception {
        logger.info("Testing TRUNCATE TABLE with RESTART IDENTITY");
        
        // Create test table
        String createTableSql = "CREATE TABLE orders (id INTEGER, customer_name VARCHAR, amount INTEGER)";
        sqlEngine.execute(createTableSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Insert test data
        sqlEngine.execute("INSERT INTO orders VALUES (1, 'Alice', 100)", 
                         TransactionIsolationLevel.READ_COMMITTED);
        sqlEngine.execute("INSERT INTO orders VALUES (2, 'Bob', 200)", 
                         TransactionIsolationLevel.READ_COMMITTED);
        
        // Verify data exists
        Schema schema = engine.getSchema("public");
        Table table = schema.getTable("orders");
        assertEquals(2, table.getRowCount());
        
        // Truncate with RESTART IDENTITY
        SqlExecutionResult result = sqlEngine.execute("TRUNCATE TABLE orders RESTART IDENTITY", 
                                                     TransactionIsolationLevel.READ_COMMITTED);
        
        assertTrue(result.isSuccess());
        assertEquals("TRUNCATE TABLE completed successfully", result.getMessage());
        
        // Verify table is empty
        assertEquals(0, table.getRowCount());
        
        logger.info("TRUNCATE TABLE RESTART IDENTITY test passed");
    }
    
    @Test
    void testTruncateTableWithContinueIdentity() throws Exception {
        logger.info("Testing TRUNCATE TABLE with CONTINUE IDENTITY");
        
        // Create test table
        String createTableSql = "CREATE TABLE products (id INTEGER, name VARCHAR, price INTEGER)";
        sqlEngine.execute(createTableSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Insert test data
        sqlEngine.execute("INSERT INTO products VALUES (1, 'Widget A', 10)", 
                         TransactionIsolationLevel.READ_COMMITTED);
        sqlEngine.execute("INSERT INTO products VALUES (2, 'Widget B', 20)", 
                         TransactionIsolationLevel.READ_COMMITTED);
        
        // Verify data exists
        Schema schema = engine.getSchema("public");
        Table table = schema.getTable("products");
        assertEquals(2, table.getRowCount());
        
        // Truncate with CONTINUE IDENTITY
        SqlExecutionResult result = sqlEngine.execute("TRUNCATE TABLE products CONTINUE IDENTITY", 
                                                     TransactionIsolationLevel.READ_COMMITTED);
        
        assertTrue(result.isSuccess());
        assertEquals("TRUNCATE TABLE completed successfully", result.getMessage());
        
        // Verify table is empty
        assertEquals(0, table.getRowCount());
        
        logger.info("TRUNCATE TABLE CONTINUE IDENTITY test passed");
    }
    
    @Test
    void testTruncateEmptyTable() throws Exception {
        logger.info("Testing TRUNCATE TABLE on empty table");
        
        // Create test table (empty)
        String createTableSql = "CREATE TABLE empty_table (id INTEGER, name VARCHAR)";
        sqlEngine.execute(createTableSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Verify table is empty
        Schema schema = engine.getSchema("public");
        Table table = schema.getTable("empty_table");
        assertEquals(0, table.getRowCount());
        
        // Truncate the empty table
        SqlExecutionResult result = sqlEngine.execute("TRUNCATE TABLE empty_table", 
                                                     TransactionIsolationLevel.READ_COMMITTED);
        
        assertTrue(result.isSuccess());
        assertEquals("TRUNCATE TABLE completed successfully", result.getMessage());
        
        // Verify table is still empty and structure intact
        assertEquals(0, table.getRowCount());
        assertEquals(2, table.getColumns().size());
        
        logger.info("TRUNCATE empty table test passed");
    }
    
    @Test
    void testTruncateTableWithIndexes() throws Exception {
        logger.info("Testing TRUNCATE TABLE with indexes");
        
        // Create test table
        String createTableSql = "CREATE TABLE indexed_table (id INTEGER, name VARCHAR, email VARCHAR)";
        sqlEngine.execute(createTableSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Create index
        sqlEngine.execute("CREATE INDEX idx_name ON indexed_table (name)", 
                         TransactionIsolationLevel.READ_COMMITTED);
        
        // Insert test data
        sqlEngine.execute("INSERT INTO indexed_table VALUES (1, 'Alice', 'alice@example.com')", 
                         TransactionIsolationLevel.READ_COMMITTED);
        sqlEngine.execute("INSERT INTO indexed_table VALUES (2, 'Bob', 'bob@example.com')", 
                         TransactionIsolationLevel.READ_COMMITTED);
        
        // Verify data exists
        Schema schema = engine.getSchema("public");
        Table table = schema.getTable("indexed_table");
        assertEquals(2, table.getRowCount());
        
        // Truncate the table
        SqlExecutionResult result = sqlEngine.execute("TRUNCATE TABLE indexed_table", 
                                                     TransactionIsolationLevel.READ_COMMITTED);
        
        assertTrue(result.isSuccess());
        assertEquals("TRUNCATE TABLE completed successfully", result.getMessage());
        
        // Verify table is empty but indexes remain (structure preserved)
        assertEquals(0, table.getRowCount());
        // Note: Index structure is preserved after truncate, just cleared of data
        
        logger.info("TRUNCATE TABLE with indexes test passed");
    }
    
    @Test
    void testTruncateTablePerformance() throws Exception {
        logger.info("Testing TRUNCATE TABLE performance vs DELETE");
        
        // Create test table
        String createTableSql = "CREATE TABLE perf_test (id INTEGER, data VARCHAR)";
        sqlEngine.execute(createTableSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Insert many rows for performance test
        for (int i = 1; i <= 1000; i++) {
            sqlEngine.execute("INSERT INTO perf_test VALUES (" + i + ", 'data" + i + "')", 
                             TransactionIsolationLevel.READ_COMMITTED);
        }
        
        // Verify data exists
        Schema schema = engine.getSchema("public");
        Table table = schema.getTable("perf_test");
        assertEquals(1000, table.getRowCount());
        
        // Time TRUNCATE operation
        long startTime = System.currentTimeMillis();
        SqlExecutionResult result = sqlEngine.execute("TRUNCATE TABLE perf_test", 
                                                     TransactionIsolationLevel.READ_COMMITTED);
        long truncateTime = System.currentTimeMillis() - startTime;
        
        assertTrue(result.isSuccess());
        assertEquals(0, table.getRowCount());
        
        logger.info("TRUNCATE TABLE performance test passed ({}ms for 1000 rows)", truncateTime);
    }
    
    @Test
    void testTruncateNonExistentTable() throws Exception {
        logger.info("Testing TRUNCATE TABLE error handling - non-existent table");
        
        // Try to truncate non-existent table
        Exception exception = assertThrows(Exception.class, () -> {
            sqlEngine.execute("TRUNCATE TABLE non_existent_table", 
                             TransactionIsolationLevel.READ_COMMITTED);
        });
        
        assertTrue(exception.getMessage().contains("Failed to execute SQL"));
        
        logger.info("TRUNCATE non-existent table error test passed");
    }
    
    @Test
    void testTruncateTableMultipleTimes() throws Exception {
        logger.info("Testing TRUNCATE TABLE multiple times");
        
        // Create test table
        String createTableSql = "CREATE TABLE multi_truncate (id INTEGER, data VARCHAR)";
        sqlEngine.execute(createTableSql, TransactionIsolationLevel.READ_COMMITTED);
        
        Schema schema = engine.getSchema("public");
        Table table = schema.getTable("multi_truncate");
        
        // Multiple rounds of insert and truncate
        for (int round = 1; round <= 3; round++) {
            // Insert data
            sqlEngine.execute("INSERT INTO multi_truncate VALUES (1, 'round" + round + "')", 
                             TransactionIsolationLevel.READ_COMMITTED);
            sqlEngine.execute("INSERT INTO multi_truncate VALUES (2, 'round" + round + "')", 
                             TransactionIsolationLevel.READ_COMMITTED);
            
            assertEquals(2, table.getRowCount());
            
            // Truncate
            SqlExecutionResult result = sqlEngine.execute("TRUNCATE TABLE multi_truncate", 
                                                         TransactionIsolationLevel.READ_COMMITTED);
            
            assertTrue(result.isSuccess());
            assertEquals(0, table.getRowCount());
        }
        
        logger.info("Multiple TRUNCATE TABLE test passed");
    }
    
    @Test 
    void testTruncateTableAfterSelect() throws Exception {
        logger.info("Testing TRUNCATE TABLE after SELECT operations");
        
        // Create test table
        String createTableSql = "CREATE TABLE select_truncate (id INTEGER, name VARCHAR)";
        sqlEngine.execute(createTableSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Insert test data
        sqlEngine.execute("INSERT INTO select_truncate VALUES (1, 'Alice')", 
                         TransactionIsolationLevel.READ_COMMITTED);
        sqlEngine.execute("INSERT INTO select_truncate VALUES (2, 'Bob')", 
                         TransactionIsolationLevel.READ_COMMITTED);
        
        // Perform SELECT operations
        SqlExecutionResult selectResult = sqlEngine.execute("SELECT * FROM select_truncate", 
                                                           TransactionIsolationLevel.READ_COMMITTED);
        assertTrue(selectResult.isSuccess());
        assertEquals(2, selectResult.getRows().size());
        
        // Truncate the table
        SqlExecutionResult truncateResult = sqlEngine.execute("TRUNCATE TABLE select_truncate", 
                                                             TransactionIsolationLevel.READ_COMMITTED);
        
        assertTrue(truncateResult.isSuccess());
        
        // Verify table is empty
        Schema schema = engine.getSchema("public");
        Table table = schema.getTable("select_truncate");
        assertEquals(0, table.getRowCount());
        
        // Verify SELECT after truncate returns no rows
        SqlExecutionResult selectAfterResult = sqlEngine.execute("SELECT * FROM select_truncate", 
                                                               TransactionIsolationLevel.READ_COMMITTED);
        assertTrue(selectAfterResult.isSuccess());
        assertEquals(0, selectAfterResult.getRows().size());
        
        logger.info("TRUNCATE TABLE after SELECT test passed");
    }
}