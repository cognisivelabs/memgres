package com.memgres.sql;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionResult;
import com.memgres.types.Row;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for UNION and UNION ALL operations
 */
class UnionOperationsTest {
    
    private MemGresEngine engine;
    private SqlExecutionEngine sqlEngine;
    
    @BeforeEach
    void setUp() throws Exception {
        engine = new MemGresEngine();
        engine.initialize();
        sqlEngine = new SqlExecutionEngine(engine);
        
        // Create test table and insert test data
        sqlEngine.execute("CREATE TABLE employees (id INTEGER, name VARCHAR(50), department VARCHAR(50))");
        sqlEngine.execute("INSERT INTO employees VALUES (1, 'Alice', 'Engineering')");
        sqlEngine.execute("INSERT INTO employees VALUES (2, 'Bob', 'Engineering')");
        sqlEngine.execute("INSERT INTO employees VALUES (3, 'Charlie', 'Sales')");
        
        sqlEngine.execute("CREATE TABLE contractors (id INTEGER, name VARCHAR(50), department VARCHAR(50))");
        sqlEngine.execute("INSERT INTO contractors VALUES (4, 'Diana', 'Engineering')");
        sqlEngine.execute("INSERT INTO contractors VALUES (5, 'Eve', 'Marketing')");
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (engine != null) {
            engine.shutdown();
        }
    }
    
    @Test
    void testSimpleUnionAll() throws Exception {
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT name, department FROM employees " +
            "UNION ALL " +
            "SELECT name, department FROM contractors"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(5, rows.size()); // 3 employees + 2 contractors
        
        // Verify all names are present
        String[] expectedNames = {"Alice", "Bob", "Charlie", "Diana", "Eve"};
        for (String name : expectedNames) {
            boolean found = false;
            for (Row row : rows) {
                if (name.equals(row.getData()[0])) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Name " + name + " should be found in UNION ALL result");
        }
    }
    
    @Test
    void testUnionAllWithLiterals() throws Exception {
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT 'Employee' as type, name FROM employees WHERE department = 'Engineering' " +
            "UNION ALL " +
            "SELECT 'Contractor' as type, name FROM contractors WHERE department = 'Engineering'"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(3, rows.size()); // 2 engineering employees + 1 engineering contractor
        
        // Check that we have the right mix of types
        int employeeCount = 0;
        int contractorCount = 0;
        
        for (Row row : rows) {
            String type = (String) row.getData()[0];
            if ("Employee".equals(type)) {
                employeeCount++;
            } else if ("Contractor".equals(type)) {
                contractorCount++;
            }
        }
        
        assertEquals(2, employeeCount);
        assertEquals(1, contractorCount);
    }
    
    @Test
    void testUnionWithDuplicateRemoval() throws Exception {
        // Add a duplicate entry to test UNION (not UNION ALL)
        sqlEngine.execute("INSERT INTO contractors VALUES (6, 'Alice', 'Engineering')");
        
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT name, department FROM employees " +
            "UNION " +
            "SELECT name, department FROM contractors"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        
        // Should have removed the duplicate Alice, Engineering entry
        // Original: Alice, Bob, Charlie (employees) + Diana, Eve, Alice (contractors)
        // After UNION: Alice, Bob, Charlie, Diana, Eve (5 unique entries)
        assertEquals(5, rows.size());
        
        // Verify Alice appears only once
        int aliceCount = 0;
        for (Row row : rows) {
            if ("Alice".equals(row.getData()[0])) {
                aliceCount++;
            }
        }
        assertEquals(1, aliceCount, "Alice should appear only once in UNION result");
    }
    
    @Test
    void testUnionColumnCountMismatch() throws Exception {
        // Test error handling for column count mismatch
        assertThrows(Exception.class, () -> {
            sqlEngine.execute(
                "SELECT name FROM employees " +
                "UNION ALL " +
                "SELECT name, department FROM contractors"
            );
        });
    }
}