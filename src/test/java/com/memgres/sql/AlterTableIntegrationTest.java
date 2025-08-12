package com.memgres.sql;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionResult;
import com.memgres.storage.Schema;
import com.memgres.storage.Table;
import com.memgres.transaction.TransactionIsolationLevel;
import com.memgres.types.Column;
import com.memgres.types.Row;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ALTER TABLE statements.
 * Tests H2-compatible ALTER TABLE DDL parsing and execution including:
 * - ADD COLUMN (with positioning)
 * - DROP COLUMN (with IF EXISTS)  
 * - RENAME COLUMN
 * - RENAME TABLE
 */
class AlterTableIntegrationTest {
    
    private static final Logger logger = LoggerFactory.getLogger(AlterTableIntegrationTest.class);
    
    private MemGresEngine engine;
    private SqlExecutionEngine sqlEngine;
    
    @BeforeEach
    void setUp() {
        engine = new MemGresEngine();
        engine.initialize();
        sqlEngine = new SqlExecutionEngine(engine);
        logger.info("ALTER TABLE test setup complete");
    }
    
    @AfterEach
    void tearDown() {
        if (engine != null) {
            engine.shutdown();
        }
        logger.info("ALTER TABLE test teardown complete");
    }
    
    @Test
    void testAddColumnBasic() throws Exception {
        logger.info("Testing basic ADD COLUMN functionality");
        
        // Create test table
        String createTableSql = "CREATE TABLE employees (id INTEGER, name VARCHAR)";
        SqlExecutionResult createResult = sqlEngine.execute(createTableSql, TransactionIsolationLevel.READ_COMMITTED);
        assertEquals(SqlExecutionResult.ResultType.DDL, createResult.getType());
        assertTrue(createResult.isSuccess());
        
        // Insert test data
        String insertSql = "INSERT INTO employees (id, name) VALUES (1, 'Alice'), (2, 'Bob')";
        sqlEngine.execute(insertSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Add column at end (default position)
        String alterSql = "ALTER TABLE employees ADD COLUMN email VARCHAR";
        SqlExecutionResult alterResult = sqlEngine.execute(alterSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.DDL, alterResult.getType());
        assertTrue(alterResult.isSuccess());
        assertEquals("ALTER TABLE ADD COLUMN completed successfully", alterResult.getMessage());
        
        // Verify table structure
        Schema schema = engine.getSchema("public");
        Table table = schema.getTable("employees");
        assertNotNull(table);
        
        List<Column> columns = table.getColumns();
        assertEquals(3, columns.size());
        assertEquals("id", columns.get(0).getName());
        assertEquals("name", columns.get(1).getName());
        assertEquals("email", columns.get(2).getName());
        
        // Verify existing data is preserved with NULL for new column
        String selectSql = "SELECT * FROM employees ORDER BY id";
        SqlExecutionResult selectResult = sqlEngine.execute(selectSql, TransactionIsolationLevel.READ_COMMITTED);
        
        List<Row> rows = selectResult.getRows();
        assertEquals(2, rows.size());
        
        Row row1 = rows.get(0);
        assertEquals(1, row1.getValue(0)); // id
        assertEquals("Alice", row1.getValue(1)); // name
        assertNull(row1.getValue(2)); // email (new column)
        
        Row row2 = rows.get(1);
        assertEquals(2, row2.getValue(0));
        assertEquals("Bob", row2.getValue(1));
        assertNull(row2.getValue(2));
        
        logger.info("Basic ADD COLUMN test passed");
    }
    
    @Test
    void testAddColumnWithConstraints() throws Exception {
        logger.info("Testing ADD COLUMN with constraints");
        
        // Create test table
        String createTableSql = "CREATE TABLE products (id INTEGER, name VARCHAR)";
        sqlEngine.execute(createTableSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Add column with NOT NULL constraint
        String alterSql = "ALTER TABLE products ADD COLUMN price DECIMAL NOT NULL";
        
        // This should succeed even though existing rows will have NULL
        // (In real H2, this might require a default value, but our implementation allows it)
        SqlExecutionResult alterResult = sqlEngine.execute(alterSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.DDL, alterResult.getType());
        assertTrue(alterResult.isSuccess());
        
        // Verify column was added with proper constraints
        Schema schema = engine.getSchema("public");
        Table table = schema.getTable("products");
        List<Column> columns = table.getColumns();
        
        assertEquals(3, columns.size());
        Column priceColumn = columns.get(2);
        assertEquals("price", priceColumn.getName());
        assertFalse(priceColumn.isNullable()); // Should be NOT NULL
        
        logger.info("ADD COLUMN with constraints test passed");
    }
    
    @Test
    void testAddColumnWithPositioning() throws Exception {
        logger.info("Testing ADD COLUMN with BEFORE/AFTER positioning");
        
        // Create test table with multiple columns
        String createTableSql = "CREATE TABLE users (id INTEGER, email VARCHAR, created_at TIMESTAMP)";
        sqlEngine.execute(createTableSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Add column AFTER id
        String alterSql1 = "ALTER TABLE users ADD COLUMN name VARCHAR AFTER id";
        SqlExecutionResult alterResult1 = sqlEngine.execute(alterSql1, TransactionIsolationLevel.READ_COMMITTED);
        assertTrue(alterResult1.isSuccess());
        
        // Add column BEFORE email  
        String alterSql2 = "ALTER TABLE users ADD COLUMN username VARCHAR BEFORE email";
        SqlExecutionResult alterResult2 = sqlEngine.execute(alterSql2, TransactionIsolationLevel.READ_COMMITTED);
        assertTrue(alterResult2.isSuccess());
        
        // Verify column order: id, name, username, email, created_at
        Schema schema = engine.getSchema("public");
        Table table = schema.getTable("users");
        List<Column> columns = table.getColumns();
        
        assertEquals(5, columns.size());
        assertEquals("id", columns.get(0).getName());
        assertEquals("name", columns.get(1).getName());        // Added AFTER id
        assertEquals("username", columns.get(2).getName());    // Added BEFORE email
        assertEquals("email", columns.get(3).getName());
        assertEquals("created_at", columns.get(4).getName());
        
        logger.info("ADD COLUMN with positioning test passed");
    }
    
    @Test
    void testDropColumnBasic() throws Exception {
        logger.info("Testing basic DROP COLUMN functionality");
        
        // Create test table with multiple columns
        String createTableSql = "CREATE TABLE customers (id INTEGER, name VARCHAR, email VARCHAR, phone VARCHAR)";
        sqlEngine.execute(createTableSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Insert test data
        String insertSql = "INSERT INTO customers (id, name, email, phone) VALUES (1, 'John', 'john@test.com', '123-456-7890')";
        sqlEngine.execute(insertSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Drop middle column
        String alterSql = "ALTER TABLE customers DROP COLUMN email";
        SqlExecutionResult alterResult = sqlEngine.execute(alterSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.DDL, alterResult.getType());
        assertTrue(alterResult.isSuccess());
        assertEquals("ALTER TABLE DROP COLUMN completed successfully", alterResult.getMessage());
        
        // Verify table structure
        Schema schema = engine.getSchema("public");
        Table table = schema.getTable("customers");
        List<Column> columns = table.getColumns();
        
        assertEquals(3, columns.size());
        assertEquals("id", columns.get(0).getName());
        assertEquals("name", columns.get(1).getName());
        assertEquals("phone", columns.get(2).getName()); // email was removed
        
        // Verify data integrity - remaining columns should be intact
        String selectSql = "SELECT * FROM customers WHERE id = 1";
        SqlExecutionResult selectResult = sqlEngine.execute(selectSql, TransactionIsolationLevel.READ_COMMITTED);
        
        List<Row> rows = selectResult.getRows();
        assertEquals(1, rows.size());
        
        Row row = rows.get(0);
        assertEquals(3, row.getColumnCount()); // Now 3 columns instead of 4
        assertEquals(1, row.getValue(0)); // id
        assertEquals("John", row.getValue(1)); // name
        assertEquals("123-456-7890", row.getValue(2)); // phone (was index 3, now 2)
        
        logger.info("Basic DROP COLUMN test passed");
    }
    
    @Test
    void testDropColumnIfExists() throws Exception {
        logger.info("Testing DROP COLUMN IF EXISTS functionality");
        
        // Create test table
        String createTableSql = "CREATE TABLE orders (id INTEGER, product_name VARCHAR)";
        sqlEngine.execute(createTableSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Drop non-existent column with IF EXISTS - should succeed
        String alterSql1 = "ALTER TABLE orders DROP COLUMN IF EXISTS description";
        SqlExecutionResult alterResult1 = sqlEngine.execute(alterSql1, TransactionIsolationLevel.READ_COMMITTED);
        
        assertTrue(alterResult1.isSuccess());
        assertEquals("Column does not exist", alterResult1.getMessage());
        
        // Drop non-existent column without IF EXISTS - should fail
        String alterSql2 = "ALTER TABLE orders DROP COLUMN description";
        
        Exception exception = assertThrows(Exception.class, () -> {
            sqlEngine.execute(alterSql2, TransactionIsolationLevel.READ_COMMITTED);
        });
        
        assertTrue(exception.getMessage().contains("Failed to execute SQL"));
        
        logger.info("DROP COLUMN IF EXISTS test passed");
    }
    
    @Test
    void testDropColumnLastColumnError() throws Exception {
        logger.info("Testing DROP COLUMN error when dropping last column");
        
        // Create table with single column
        String createTableSql = "CREATE TABLE single_col (id INTEGER)";
        sqlEngine.execute(createTableSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Try to drop the only column - should fail
        String alterSql = "ALTER TABLE single_col DROP COLUMN id";
        
        Exception exception = assertThrows(Exception.class, () -> {
            sqlEngine.execute(alterSql, TransactionIsolationLevel.READ_COMMITTED);
        });
        
        assertTrue(exception.getMessage().contains("Failed to execute SQL"));
        
        logger.info("DROP COLUMN last column error test passed");
    }
    
    @Test
    void testRenameColumn() throws Exception {
        logger.info("Testing RENAME COLUMN functionality");
        
        // Create test table
        String createTableSql = "CREATE TABLE articles (id INTEGER, title VARCHAR, content TEXT)";
        sqlEngine.execute(createTableSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Insert test data
        String insertSql = "INSERT INTO articles (id, title, content) VALUES (1, 'Test Article', 'This is test content')";
        sqlEngine.execute(insertSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Rename column
        String alterSql = "ALTER TABLE articles ALTER COLUMN title RENAME TO headline";
        SqlExecutionResult alterResult = sqlEngine.execute(alterSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.DDL, alterResult.getType());
        assertTrue(alterResult.isSuccess());
        assertEquals("ALTER TABLE RENAME COLUMN completed successfully", alterResult.getMessage());
        
        // Verify column was renamed
        Schema schema = engine.getSchema("public");
        Table table = schema.getTable("articles");
        List<Column> columns = table.getColumns();
        
        assertEquals(3, columns.size());
        assertEquals("id", columns.get(0).getName());
        assertEquals("headline", columns.get(1).getName()); // Renamed from title
        assertEquals("content", columns.get(2).getName());
        
        // Verify data is preserved
        String selectSql = "SELECT * FROM articles WHERE id = 1";
        SqlExecutionResult selectResult = sqlEngine.execute(selectSql, TransactionIsolationLevel.READ_COMMITTED);
        
        List<Row> rows = selectResult.getRows();
        assertEquals(1, rows.size());
        
        Row row = rows.get(0);
        assertEquals(1, row.getValue(0)); // id
        assertEquals("Test Article", row.getValue(1)); // headline (was title)
        assertEquals("This is test content", row.getValue(2)); // content
        
        logger.info("RENAME COLUMN test passed");
    }
    
    @Test
    void testRenameTable() throws Exception {
        logger.info("Testing RENAME TABLE functionality");
        
        // Create test table
        String createTableSql = "CREATE TABLE temp_users (id INTEGER, name VARCHAR)";
        sqlEngine.execute(createTableSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Insert test data
        String insertSql = "INSERT INTO temp_users (id, name) VALUES (1, 'Alice')";
        sqlEngine.execute(insertSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Rename table
        String alterSql = "ALTER TABLE temp_users RENAME TO users";
        SqlExecutionResult alterResult = sqlEngine.execute(alterSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.DDL, alterResult.getType());
        assertTrue(alterResult.isSuccess());
        assertEquals("ALTER TABLE RENAME TO completed successfully", alterResult.getMessage());
        
        // Verify old table name doesn't exist
        Schema schema = engine.getSchema("public");
        Table oldTable = schema.getTable("temp_users");
        assertNull(oldTable);
        
        // Verify new table name exists
        Table newTable = schema.getTable("users");
        assertNotNull(newTable);
        
        // Verify data is preserved
        String selectSql = "SELECT * FROM users WHERE id = 1";
        SqlExecutionResult selectResult = sqlEngine.execute(selectSql, TransactionIsolationLevel.READ_COMMITTED);
        
        List<Row> rows = selectResult.getRows();
        assertEquals(1, rows.size());
        
        Row row = rows.get(0);
        assertEquals(1, row.getValue(0));
        assertEquals("Alice", row.getValue(1));
        
        logger.info("RENAME TABLE test passed");
    }
    
    @Test
    void testAlterTableIfExists() throws Exception {
        logger.info("Testing ALTER TABLE IF EXISTS functionality");
        
        // Try to alter non-existent table with IF EXISTS
        String alterSql = "ALTER TABLE IF EXISTS non_existent ADD COLUMN test_col VARCHAR";
        SqlExecutionResult alterResult = sqlEngine.execute(alterSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertTrue(alterResult.isSuccess());
        assertEquals("Table does not exist", alterResult.getMessage());
        
        // Try to alter non-existent table without IF EXISTS - should fail
        String alterSql2 = "ALTER TABLE non_existent ADD COLUMN test_col VARCHAR";
        
        Exception exception = assertThrows(Exception.class, () -> {
            sqlEngine.execute(alterSql2, TransactionIsolationLevel.READ_COMMITTED);
        });
        
        assertTrue(exception.getMessage().contains("Failed to execute SQL"));
        
        logger.info("ALTER TABLE IF EXISTS test passed");
    }
    
    @Test
    void testComplexAlterTableWorkflow() throws Exception {
        logger.info("Testing complex ALTER TABLE workflow");
        
        // Create initial table
        String createTableSql = "CREATE TABLE employees (id INTEGER, first_name VARCHAR, last_name VARCHAR)";
        sqlEngine.execute(createTableSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Insert initial data
        String insertSql = "INSERT INTO employees (id, first_name, last_name) VALUES (1, 'John', 'Doe'), (2, 'Jane', 'Smith')";
        sqlEngine.execute(insertSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // 1. Add email column
        String alterSql1 = "ALTER TABLE employees ADD COLUMN email VARCHAR AFTER last_name";
        SqlExecutionResult result1 = sqlEngine.execute(alterSql1, TransactionIsolationLevel.READ_COMMITTED);
        assertTrue(result1.isSuccess());
        
        // 2. Add phone column at beginning
        String alterSql2 = "ALTER TABLE employees ADD COLUMN phone VARCHAR AFTER id";
        SqlExecutionResult result2 = sqlEngine.execute(alterSql2, TransactionIsolationLevel.READ_COMMITTED);
        assertTrue(result2.isSuccess());
        
        // 3. Rename first_name to given_name
        String alterSql3 = "ALTER TABLE employees ALTER COLUMN first_name RENAME TO given_name";
        SqlExecutionResult result3 = sqlEngine.execute(alterSql3, TransactionIsolationLevel.READ_COMMITTED);
        assertTrue(result3.isSuccess());
        
        // 4. Drop last_name column
        String alterSql4 = "ALTER TABLE employees DROP COLUMN last_name";
        SqlExecutionResult result4 = sqlEngine.execute(alterSql4, TransactionIsolationLevel.READ_COMMITTED);
        assertTrue(result4.isSuccess());
        
        // 5. Rename table
        String alterSql5 = "ALTER TABLE employees RENAME TO staff";
        SqlExecutionResult result5 = sqlEngine.execute(alterSql5, TransactionIsolationLevel.READ_COMMITTED);
        assertTrue(result5.isSuccess());
        
        // Verify final table structure: id, phone, given_name, email
        Schema schema = engine.getSchema("public");
        Table table = schema.getTable("staff");
        assertNotNull(table);
        
        List<Column> columns = table.getColumns();
        assertEquals(4, columns.size());
        assertEquals("id", columns.get(0).getName());
        assertEquals("phone", columns.get(1).getName());      // Added after id
        assertEquals("given_name", columns.get(2).getName()); // Renamed from first_name
        assertEquals("email", columns.get(3).getName());      // Added after last_name (which was dropped)
        
        // Verify data integrity
        String selectSql = "SELECT * FROM staff ORDER BY id";
        SqlExecutionResult selectResult = sqlEngine.execute(selectSql, TransactionIsolationLevel.READ_COMMITTED);
        
        List<Row> rows = selectResult.getRows();
        assertEquals(2, rows.size());
        
        Row row1 = rows.get(0);
        assertEquals(4, row1.getColumnCount());
        assertEquals(1, row1.getValue(0));        // id
        assertNull(row1.getValue(1));            // phone (new column)
        assertEquals("John", row1.getValue(2));   // given_name (was first_name)
        assertNull(row1.getValue(3));            // email (new column)
        
        Row row2 = rows.get(1);
        assertEquals(2, row2.getValue(0));        // id
        assertNull(row2.getValue(1));            // phone
        assertEquals("Jane", row2.getValue(2));   // given_name
        assertNull(row2.getValue(3));            // email
        
        logger.info("Complex ALTER TABLE workflow test passed");
    }
    
    @Test
    void testAlterTableErrorHandling() throws Exception {
        logger.info("Testing ALTER TABLE error handling");
        
        // Create test table
        String createTableSql = "CREATE TABLE test_errors (id INTEGER, name VARCHAR)";
        sqlEngine.execute(createTableSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Test 1: Add column that already exists
        Exception exception1 = assertThrows(Exception.class, () -> {
            sqlEngine.execute("ALTER TABLE test_errors ADD COLUMN name VARCHAR", TransactionIsolationLevel.READ_COMMITTED);
        });
        assertTrue(exception1.getMessage().contains("Failed to execute SQL"));
        
        // Test 2: Drop column that doesn't exist
        Exception exception2 = assertThrows(Exception.class, () -> {
            sqlEngine.execute("ALTER TABLE test_errors DROP COLUMN non_existent", TransactionIsolationLevel.READ_COMMITTED);
        });
        assertTrue(exception2.getMessage().contains("Failed to execute SQL"));
        
        // Test 3: Rename column that doesn't exist
        Exception exception3 = assertThrows(Exception.class, () -> {
            sqlEngine.execute("ALTER TABLE test_errors ALTER COLUMN non_existent RENAME TO new_name", TransactionIsolationLevel.READ_COMMITTED);
        });
        assertTrue(exception3.getMessage().contains("Failed to execute SQL"));
        
        // Test 4: Rename column to existing column name
        Exception exception4 = assertThrows(Exception.class, () -> {
            sqlEngine.execute("ALTER TABLE test_errors ALTER COLUMN name RENAME TO id", TransactionIsolationLevel.READ_COMMITTED);
        });
        assertTrue(exception4.getMessage().contains("Failed to execute SQL"));
        
        // Test 5: Rename table to existing table name
        sqlEngine.execute("CREATE TABLE other_table (col1 INTEGER)", TransactionIsolationLevel.READ_COMMITTED);
        
        Exception exception5 = assertThrows(Exception.class, () -> {
            sqlEngine.execute("ALTER TABLE test_errors RENAME TO other_table", TransactionIsolationLevel.READ_COMMITTED);
        });
        assertTrue(exception5.getMessage().contains("Failed to execute SQL"));
        
        logger.info("ALTER TABLE error handling test passed");
    }
}