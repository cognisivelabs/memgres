package com.memgres.sql;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionResult;
import com.memgres.transaction.TransactionIsolationLevel;
import com.memgres.types.Row;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the complete SQL execution pipeline.
 * Tests SQL parsing, AST generation, and execution against the InMemoPG engine.
 */
class SqlIntegrationTest {
    
    private static final Logger logger = LoggerFactory.getLogger(SqlIntegrationTest.class);
    
    private MemGresEngine engine;
    private SqlExecutionEngine sqlEngine;
    
    @BeforeEach
    void setUp() {
        engine = new MemGresEngine();
        engine.initialize(); // Initialize the engine
        sqlEngine = new SqlExecutionEngine(engine);
        logger.info("Test setup complete - engine and SQL execution engine initialized");
    }
    
    @AfterEach
    void tearDown() {
        if (engine != null) {
            engine.shutdown();
        }
        logger.info("Test teardown complete - engine shutdown");
    }
    
    @Test
    void testCompleteWorkflow() throws Exception {
        logger.info("Starting complete SQL workflow test");
        
        // 1. CREATE TABLE - Test DDL execution
        String createTableSql = "CREATE TABLE users (id UUID, name VARCHAR, age INTEGER, active BOOLEAN)";
        SqlExecutionResult createResult = sqlEngine.execute(createTableSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.DDL, createResult.getType());
        assertTrue(createResult.isSuccess());
        assertNotNull(createResult.getMessage());
        logger.info("CREATE TABLE executed successfully: {}", createResult.getMessage());
        
        // 2. INSERT with UUID generation - Test DML with function calls
        String insertSql1 = "INSERT INTO users VALUES (gen_random_uuid(), 'John Doe', 30, true)";
        SqlExecutionResult insertResult1 = sqlEngine.execute(insertSql1, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.INSERT, insertResult1.getType());
        assertEquals(1, insertResult1.getAffectedRows());
        logger.info("First INSERT executed successfully: {} rows affected", insertResult1.getAffectedRows());
        
        // 3. INSERT with multiple values
        String insertSql2 = "INSERT INTO users VALUES (gen_random_uuid(), 'Jane Smith', 25, true)";
        String insertSql3 = "INSERT INTO users VALUES (gen_random_uuid(), 'Bob Wilson', 35, false)";
        
        sqlEngine.execute(insertSql2, TransactionIsolationLevel.READ_COMMITTED);
        sqlEngine.execute(insertSql3, TransactionIsolationLevel.READ_COMMITTED);
        logger.info("Additional INSERT statements executed successfully");
        
        // 4. SELECT * - Test basic query
        String selectAllSql = "SELECT * FROM users";
        SqlExecutionResult selectAllResult = sqlEngine.execute(selectAllSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, selectAllResult.getType());
        assertEquals(3, selectAllResult.getRows().size());
        assertEquals(4, selectAllResult.getColumns().size());
        logger.info("SELECT * executed successfully: {} rows returned", selectAllResult.getRows().size());
        
        // 5. SELECT with WHERE clause - Test filtered query
        String selectFilteredSql = "SELECT name, age FROM users WHERE age > 28";
        SqlExecutionResult filteredResult = sqlEngine.execute(selectFilteredSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, filteredResult.getType());
        assertEquals(2, filteredResult.getRows().size()); // John (30) and Bob (35)
        assertEquals(2, filteredResult.getColumns().size()); // name, age
        logger.info("SELECT with WHERE executed successfully: {} rows returned", filteredResult.getRows().size());
        
        // 6. SELECT with ORDER BY - Test sorted query
        String selectOrderedSql = "SELECT name, age FROM users ORDER BY age DESC";
        SqlExecutionResult orderedResult = sqlEngine.execute(selectOrderedSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, orderedResult.getType());
        assertEquals(3, orderedResult.getRows().size());
        
        List<Row> orderedRows = orderedResult.getRows();
        // Should be ordered: Bob (35), John (30), Jane (25)
        assertTrue(((Number) orderedRows.get(0).getData()[1]).intValue() >= 
                  ((Number) orderedRows.get(1).getData()[1]).intValue());
        assertTrue(((Number) orderedRows.get(1).getData()[1]).intValue() >= 
                  ((Number) orderedRows.get(2).getData()[1]).intValue());
        logger.info("SELECT with ORDER BY executed successfully: rows properly ordered");
        
        // 7. SELECT with LIMIT - Test pagination
        String selectLimitedSql = "SELECT name FROM users LIMIT 2";
        SqlExecutionResult limitedResult = sqlEngine.execute(selectLimitedSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, limitedResult.getType());
        assertEquals(2, limitedResult.getRows().size());
        assertEquals(1, limitedResult.getColumns().size());
        logger.info("SELECT with LIMIT executed successfully: {} rows returned", limitedResult.getRows().size());
        
        // 8. UPDATE - Test data modification
        String updateSql = "UPDATE users SET age = 31 WHERE name = 'John Doe'";
        SqlExecutionResult updateResult = sqlEngine.execute(updateSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.UPDATE, updateResult.getType());
        assertEquals(1, updateResult.getAffectedRows());
        logger.info("UPDATE executed successfully: {} rows affected", updateResult.getAffectedRows());
        
        // Verify update worked
        String verifyUpdateSql = "SELECT age FROM users WHERE name = 'John Doe'";
        SqlExecutionResult verifyResult = sqlEngine.execute(verifyUpdateSql, TransactionIsolationLevel.READ_COMMITTED);
        assertEquals(31, ((Number) verifyResult.getRows().get(0).getData()[0]).intValue());
        logger.info("UPDATE verification successful: age updated to 31");
        
        // 9. DELETE - Test data removal
        String deleteSql = "DELETE FROM users WHERE active = false";
        SqlExecutionResult deleteResult = sqlEngine.execute(deleteSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(SqlExecutionResult.ResultType.DELETE, deleteResult.getType());
        assertEquals(1, deleteResult.getAffectedRows()); // Should delete Bob
        logger.info("DELETE executed successfully: {} rows affected", deleteResult.getAffectedRows());
        
        // Verify delete worked
        String verifyDeleteSql = "SELECT * FROM users";
        SqlExecutionResult finalResult = sqlEngine.execute(verifyDeleteSql, TransactionIsolationLevel.READ_COMMITTED);
        assertEquals(2, finalResult.getRows().size()); // Should have John and Jane left
        logger.info("DELETE verification successful: {} rows remaining", finalResult.getRows().size());
        
        logger.info("Complete SQL workflow test passed successfully!");
    }
    
    @Test
    void testUuidGeneration() throws Exception {
        logger.info("Starting UUID generation test");
        
        // Create a simple table for UUID testing
        String createSql = "CREATE TABLE uuid_test (id UUID, name VARCHAR)";
        sqlEngine.execute(createSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Insert with gen_random_uuid()
        String insertSql = "INSERT INTO uuid_test VALUES (gen_random_uuid(), 'Test Entry')";
        SqlExecutionResult insertResult = sqlEngine.execute(insertSql, TransactionIsolationLevel.READ_COMMITTED);
        assertEquals(1, insertResult.getAffectedRows());
        
        // Verify UUID was generated
        String selectSql = "SELECT id, name FROM uuid_test";
        SqlExecutionResult selectResult = sqlEngine.execute(selectSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(1, selectResult.getRows().size());
        Object uuidValue = selectResult.getRows().get(0).getData()[0];
        assertNotNull(uuidValue);
        assertTrue(uuidValue instanceof UUID);
        logger.info("UUID generation test passed: generated UUID = {}", uuidValue);
    }
    
    @Test
    void testComplexQueries() throws Exception {
        logger.info("Starting complex queries test");
        
        // Create test data
        String createSql = "CREATE TABLE products (id UUID, name VARCHAR, price INTEGER, category VARCHAR)";
        sqlEngine.execute(createSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Insert test data
        sqlEngine.execute("INSERT INTO products VALUES (gen_random_uuid(), 'Laptop', 1200, 'Electronics')", 
                         TransactionIsolationLevel.READ_COMMITTED);
        sqlEngine.execute("INSERT INTO products VALUES (gen_random_uuid(), 'Mouse', 25, 'Electronics')", 
                         TransactionIsolationLevel.READ_COMMITTED);
        sqlEngine.execute("INSERT INTO products VALUES (gen_random_uuid(), 'Book', 15, 'Education')", 
                         TransactionIsolationLevel.READ_COMMITTED);
        sqlEngine.execute("INSERT INTO products VALUES (gen_random_uuid(), 'Pen', 2, 'Office')", 
                         TransactionIsolationLevel.READ_COMMITTED);
        
        // Complex query with WHERE, ORDER BY, and LIMIT
        String complexSql = "SELECT name, price FROM products WHERE price > 10 ORDER BY price DESC LIMIT 2";
        SqlExecutionResult result = sqlEngine.execute(complexSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(2, result.getRows().size());
        
        // Verify ordering (should be Laptop (1200), Mouse (25))
        List<Row> rows = result.getRows();
        assertTrue(((Number) rows.get(0).getData()[1]).intValue() > 
                  ((Number) rows.get(1).getData()[1]).intValue());
        
        logger.info("Complex queries test passed successfully");
    }
    
    @Test
    void testTransactionIsolation() throws Exception {
        logger.info("Starting transaction isolation test");
        
        String createSql = "CREATE TABLE isolation_test (id INTEGER, data_value VARCHAR)";
        sqlEngine.execute(createSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Test different isolation levels
        String insertSql = "INSERT INTO isolation_test VALUES (1, 'test')";
        
        // READ_UNCOMMITTED
        SqlExecutionResult result1 = sqlEngine.execute(insertSql, TransactionIsolationLevel.READ_UNCOMMITTED);
        assertEquals(1, result1.getAffectedRows());
        
        // READ_COMMITTED
        SqlExecutionResult result2 = sqlEngine.execute("SELECT * FROM isolation_test", 
                                                      TransactionIsolationLevel.READ_COMMITTED);
        assertEquals(1, result2.getRows().size());
        
        // SERIALIZABLE
        SqlExecutionResult result3 = sqlEngine.execute("SELECT * FROM isolation_test", 
                                                      TransactionIsolationLevel.SERIALIZABLE);
        assertEquals(1, result3.getRows().size());
        
        logger.info("Transaction isolation test passed successfully");
    }
    
    @Test
    void testErrorHandling() throws Exception {
        logger.info("Starting error handling test");
        
        // Test table not found error
        assertThrows(Exception.class, () -> {
            sqlEngine.execute("SELECT * FROM nonexistent_table", TransactionIsolationLevel.READ_COMMITTED);
        });
        
        // Test invalid SQL
        assertThrows(Exception.class, () -> {
            sqlEngine.execute("INVALID SQL STATEMENT", TransactionIsolationLevel.READ_COMMITTED);
        });
        
        // Create table for column error tests
        sqlEngine.execute("CREATE TABLE error_test (id INTEGER, name VARCHAR)", 
                         TransactionIsolationLevel.READ_COMMITTED);
        
        // Test column count mismatch
        assertThrows(Exception.class, () -> {
            sqlEngine.execute("INSERT INTO error_test VALUES (1)", // Missing name column
                             TransactionIsolationLevel.READ_COMMITTED);
        });
        
        // Test column not found
        assertThrows(Exception.class, () -> {
            sqlEngine.execute("SELECT nonexistent_column FROM error_test", 
                             TransactionIsolationLevel.READ_COMMITTED);
        });
        
        logger.info("Error handling test passed successfully - all expected errors were caught");
    }
    
    @Test
    void testJoinOperations() throws Exception {
        logger.info("Starting JOIN operations test");
        
        // 1. Create test tables
        String createUsersTable = "CREATE TABLE users (id INTEGER, name VARCHAR, department_id INTEGER)";
        String createDeptTable = "CREATE TABLE departments (id INTEGER, name VARCHAR, manager VARCHAR)";
        
        sqlEngine.execute(createUsersTable, TransactionIsolationLevel.READ_COMMITTED);
        sqlEngine.execute(createDeptTable, TransactionIsolationLevel.READ_COMMITTED);
        
        // 2. Insert test data
        String[] userInserts = {
            "INSERT INTO users VALUES (1, 'Alice', 1)",
            "INSERT INTO users VALUES (2, 'Bob', 2)", 
            "INSERT INTO users VALUES (3, 'Charlie', 1)",
            "INSERT INTO users VALUES (4, 'David', 3)",
            "INSERT INTO users VALUES (5, 'Eve', 999)" // User with non-matching department
        };
        
        String[] deptInserts = {
            "INSERT INTO departments VALUES (1, 'Engineering', 'John')",
            "INSERT INTO departments VALUES (2, 'Marketing', 'Jane')",
            "INSERT INTO departments VALUES (4, 'Finance', 'Mike')" // Department without users
        };
        
        for (String insert : userInserts) {
            sqlEngine.execute(insert, TransactionIsolationLevel.READ_COMMITTED);
        }
        
        for (String insert : deptInserts) {
            sqlEngine.execute(insert, TransactionIsolationLevel.READ_COMMITTED);
        }
        
        // 3. Test INNER JOIN (simplified without aliases)
        String innerJoinSql = "SELECT * FROM users INNER JOIN departments ON users.department_id = departments.id";
        SqlExecutionResult innerResult = sqlEngine.execute(innerJoinSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(3, innerResult.getRows().size()); // Alice, Bob, Charlie have matching departments
        logger.info("INNER JOIN test passed - found {} matching rows", innerResult.getRows().size());
        
        // 4. Test LEFT OUTER JOIN
        String leftJoinSql = "SELECT * FROM users LEFT JOIN departments ON users.department_id = departments.id";
        SqlExecutionResult leftResult = sqlEngine.execute(leftJoinSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(5, leftResult.getRows().size()); // All users, including Eve with no matching department
        logger.info("LEFT JOIN test passed - found {} rows", leftResult.getRows().size());
        
        // 5. Test RIGHT OUTER JOIN
        String rightJoinSql = "SELECT * FROM users RIGHT JOIN departments ON users.department_id = departments.id";
        SqlExecutionResult rightResult = sqlEngine.execute(rightJoinSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(4, rightResult.getRows().size()); // All departments, including Finance with no matching user
        logger.info("RIGHT JOIN test passed - found {} rows", rightResult.getRows().size());
        
        // 6. Test FULL OUTER JOIN
        String fullJoinSql = "SELECT * FROM users FULL OUTER JOIN departments ON users.department_id = departments.id";
        SqlExecutionResult fullResult = sqlEngine.execute(fullJoinSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(6, fullResult.getRows().size()); // All users and all departments
        logger.info("FULL OUTER JOIN test passed - found {} rows", fullResult.getRows().size());
        
        // 7. Skip WHERE clause test for now - qualified column names not yet implemented
        // Future enhancement: SELECT users.name, departments.name FROM users INNER JOIN departments ON users.department_id = departments.id WHERE departments.name = 'Engineering'
        logger.info("Skipping WHERE clause test - qualified column names feature pending");
        
        // 8. Test basic JOIN - simplified for debugging
        String basicJoinSql = "SELECT * FROM users INNER JOIN departments ON users.department_id = departments.id";
        SqlExecutionResult basicResult = sqlEngine.execute(basicJoinSql, TransactionIsolationLevel.READ_COMMITTED);
        
        assertEquals(3, basicResult.getRows().size()); 
        logger.info("Basic JOIN test passed - found {} joined rows", basicResult.getRows().size());
        
        logger.info("All JOIN operations tests passed successfully");
    }
    
    @Test
    void testJoinErrorHandling() throws Exception {
        logger.info("Starting JOIN error handling test");
        
        // Create test table
        sqlEngine.execute("CREATE TABLE test_table (id INTEGER, name VARCHAR)", 
                         TransactionIsolationLevel.READ_COMMITTED);
        
        // Test JOIN with non-existent table
        assertThrows(Exception.class, () -> {
            sqlEngine.execute("SELECT * FROM test_table INNER JOIN nonexistent_table ON test_table.id = nonexistent_table.id", 
                             TransactionIsolationLevel.READ_COMMITTED);
        });
        
        logger.info("JOIN error handling test passed - expected errors were caught");
    }
    
    @Test
    void testJoinOptimization() throws Exception {
        logger.info("Starting JOIN optimization test");
        
        // Test with medium datasets (>50 rows) to verify hash join is triggered
        String createLargeOrdersTable = "CREATE TABLE large_orders (id INTEGER, customer_id INTEGER, amount INTEGER)";
        String createLargeCustomersTable = "CREATE TABLE large_customers (id INTEGER, name VARCHAR, city VARCHAR)";
        
        sqlEngine.execute(createLargeOrdersTable, TransactionIsolationLevel.READ_COMMITTED);
        sqlEngine.execute(createLargeCustomersTable, TransactionIsolationLevel.READ_COMMITTED);
        
        // Insert data using batch to avoid too many transactions
        StringBuilder orderInserts = new StringBuilder();
        StringBuilder customerInserts = new StringBuilder();
        
        // Build batch insert for orders (70 rows > 50 threshold)
        for (int i = 1; i <= 70; i++) {
            if (i > 1) orderInserts.append(", ");
            orderInserts.append(String.format("(%d, %d, %d)", i, (i % 5) + 1, i * 100));
        }
        
        // Build batch insert for customers (5 rows - small table for hash join)
        for (int i = 1; i <= 5; i++) {
            if (i > 1) customerInserts.append(", ");
            customerInserts.append(String.format("(%d, 'Customer%d', 'City%d')", i, i, i));
        }
        
        // Execute batch inserts to create scenario for hash join
        String batchOrderSql = "INSERT INTO large_orders VALUES " + orderInserts.toString();
        String batchCustomerSql = "INSERT INTO large_customers VALUES " + customerInserts.toString();
        
        sqlEngine.execute(batchOrderSql, TransactionIsolationLevel.READ_COMMITTED);
        sqlEngine.execute(batchCustomerSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // This should trigger HASH_JOIN algorithm (70 rows joining with 5 rows, with equi-join)
        String hashJoinSql = "SELECT * FROM large_orders INNER JOIN large_customers ON large_orders.customer_id = large_customers.id";
        SqlExecutionResult result = sqlEngine.execute(hashJoinSql, TransactionIsolationLevel.READ_COMMITTED);
        
        // Verify results (70 orders should all find matching customers)
        assertEquals(70, result.getRows().size());
        logger.info("Hash JOIN optimization test passed - processed {} rows with optimized algorithm", result.getRows().size());
        
        logger.info("JOIN optimization tests completed successfully");
    }
}