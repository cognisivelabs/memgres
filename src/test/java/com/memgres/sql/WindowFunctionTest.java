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
 * Test class for Window Functions (ROW_NUMBER, RANK, DENSE_RANK, etc.)
 */
class WindowFunctionTest {
    
    private MemGresEngine engine;
    private SqlExecutionEngine sqlEngine;
    
    @BeforeEach
    void setUp() throws Exception {
        engine = new MemGresEngine();
        engine.initialize();
        sqlEngine = new SqlExecutionEngine(engine);
        
        // Create test table and insert test data
        sqlEngine.execute("CREATE TABLE employees (id INTEGER, name VARCHAR(50), department VARCHAR(50), salary INTEGER)");
        sqlEngine.execute("INSERT INTO employees VALUES (1, 'Alice', 'Engineering', 75000)");
        sqlEngine.execute("INSERT INTO employees VALUES (2, 'Bob', 'Engineering', 80000)");
        sqlEngine.execute("INSERT INTO employees VALUES (3, 'Charlie', 'Sales', 60000)");
        sqlEngine.execute("INSERT INTO employees VALUES (4, 'Diana', 'Sales', 65000)");
        sqlEngine.execute("INSERT INTO employees VALUES (5, 'Eve', 'Engineering', 90000)");
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (engine != null) {
            engine.shutdown();
        }
    }
    
    @Test
    void testRowNumberWithoutPartition() throws Exception {
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT name, ROW_NUMBER() OVER (ORDER BY salary) as row_num FROM employees ORDER BY salary"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(5, rows.size());
        
        // Check ROW_NUMBER values
        assertEquals("Charlie", rows.get(0).getData()[0]);
        assertEquals(1L, rows.get(0).getData()[1]);
        
        assertEquals("Diana", rows.get(1).getData()[0]);
        assertEquals(2L, rows.get(1).getData()[1]);
        
        assertEquals("Alice", rows.get(2).getData()[0]);
        assertEquals(3L, rows.get(2).getData()[1]);
        
        assertEquals("Bob", rows.get(3).getData()[0]);
        assertEquals(4L, rows.get(3).getData()[1]);
        
        assertEquals("Eve", rows.get(4).getData()[0]);
        assertEquals(5L, rows.get(4).getData()[1]);
    }
    
    @Test
    void testRowNumberWithPartition() throws Exception {
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT name, department, ROW_NUMBER() OVER (PARTITION BY department ORDER BY salary) as row_num " +
            "FROM employees ORDER BY department, salary"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(5, rows.size());
        
        // Check partitioned ROW_NUMBER values
        // Engineering department should have row numbers 1, 2, 3
        // Sales department should have row numbers 1, 2
        
        // Find rows by department and verify row numbers
        for (Row row : rows) {
            String department = (String) row.getData()[1];
            Long rowNum = (Long) row.getData()[2];
            
            if ("Engineering".equals(department)) {
                assertTrue(rowNum >= 1L && rowNum <= 3L, "Engineering row number should be 1-3, got " + rowNum);
            } else if ("Sales".equals(department)) {
                assertTrue(rowNum >= 1L && rowNum <= 2L, "Sales row number should be 1-2, got " + rowNum);
            }
        }
    }
    
    @Test
    void testSimpleRank() throws Exception {
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT name, salary, RANK() OVER (ORDER BY salary DESC) as rank_num FROM employees ORDER BY salary DESC"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(5, rows.size());
        
        // Since all salaries are different, RANK should be 1, 2, 3, 4, 5
        assertEquals("Eve", rows.get(0).getData()[0]);
        assertEquals(1L, rows.get(0).getData()[2]);
        
        assertEquals("Bob", rows.get(1).getData()[0]);
        assertEquals(2L, rows.get(1).getData()[2]);
    }
    
    @Test
    void testLagFunction() throws Exception {
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT name, salary, LAG(salary) OVER (ORDER BY salary) as prev_salary FROM employees ORDER BY salary"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(5, rows.size());
        
        // First row should have null as previous salary
        assertEquals("Charlie", rows.get(0).getData()[0]);
        assertNull(rows.get(0).getData()[2]);
        
        // Second row should have Charlie's salary as previous
        assertEquals("Diana", rows.get(1).getData()[0]);
        assertEquals(60000, rows.get(1).getData()[2]);
    }
    
    @Test
    void testLagWithOffset() throws Exception {
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT name, salary, LAG(salary, 2) OVER (ORDER BY salary) as prev_salary FROM employees ORDER BY salary"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(5, rows.size());
        
        // First two rows should have null
        assertNull(rows.get(0).getData()[2]);
        assertNull(rows.get(1).getData()[2]);
        
        // Third row should have first row's salary
        assertEquals("Alice", rows.get(2).getData()[0]);
        assertEquals(60000, rows.get(2).getData()[2]);
    }
    
    @Test
    void testLagWithDefault() throws Exception {
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT name, salary, LAG(salary, 1, 0) OVER (ORDER BY salary) as prev_salary FROM employees ORDER BY salary"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(5, rows.size());
        
        // First row should have default value 0
        assertEquals("Charlie", rows.get(0).getData()[0]);
        assertEquals(0L, rows.get(0).getData()[2]); // Expect Long, not Integer
        
        // Second row should have Charlie's salary
        assertEquals("Diana", rows.get(1).getData()[0]);
        assertEquals(60000, rows.get(1).getData()[2]);
    }
    
    @Test
    void testLeadFunction() throws Exception {
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT name, salary, LEAD(salary) OVER (ORDER BY salary) as next_salary FROM employees ORDER BY salary"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(5, rows.size());
        
        // First row should have Diana's salary as next
        assertEquals("Charlie", rows.get(0).getData()[0]);
        assertEquals(65000, rows.get(0).getData()[2]);
        
        // Last row should have null as next salary
        assertEquals("Eve", rows.get(4).getData()[0]);
        assertNull(rows.get(4).getData()[2]);
    }
    
    @Test
    void testFirstValue() throws Exception {
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT name, salary, FIRST_VALUE(name) OVER (ORDER BY salary) as first_name FROM employees ORDER BY salary"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(5, rows.size());
        
        // All rows should have Charlie as first value (lowest salary)
        for (Row row : rows) {
            assertEquals("Charlie", row.getData()[2]);
        }
    }
    
    @Test
    void testLastValue() throws Exception {
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT name, salary, LAST_VALUE(name) OVER (ORDER BY salary) as last_name FROM employees ORDER BY salary"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(5, rows.size());
        
        // LAST_VALUE with default frame returns the last row in the entire window
        // All rows should have Eve as last value (highest salary)
        for (Row row : rows) {
            assertEquals("Eve", row.getData()[2]);
        }
    }
    
    @Test
    void testNthValue() throws Exception {
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT name, salary, NTH_VALUE(name, 2) OVER (ORDER BY salary) as second_name FROM employees ORDER BY salary"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(5, rows.size());
        
        // First row should have null (no second value yet)
        assertNull(rows.get(0).getData()[2]);
        
        // All subsequent rows should have Diana as second value
        for (int i = 1; i < rows.size(); i++) {
            assertEquals("Diana", rows.get(i).getData()[2]);
        }
    }
    
    @Test
    void testNtileFunction() throws Exception {
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT name, salary, NTILE(3) OVER (ORDER BY salary) as bucket FROM employees ORDER BY salary"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(5, rows.size());
        
        // Check bucket distribution: 5 rows into 3 buckets should be 2,2,1
        assertEquals(1L, rows.get(0).getData()[2]); // Charlie - bucket 1
        assertEquals(1L, rows.get(1).getData()[2]); // Diana - bucket 1
        assertEquals(2L, rows.get(2).getData()[2]); // Alice - bucket 2
        assertEquals(2L, rows.get(3).getData()[2]); // Bob - bucket 2
        assertEquals(3L, rows.get(4).getData()[2]); // Eve - bucket 3
    }
    
    @Test
    void testWindowFunctionsWithPartitioning() throws Exception {
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT name, department, salary, " +
            "LAG(salary) OVER (PARTITION BY department ORDER BY salary) as prev_salary, " +
            "LEAD(salary) OVER (PARTITION BY department ORDER BY salary) as next_salary " +
            "FROM employees ORDER BY department, salary"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(5, rows.size());
        
        // Engineering department (Alice, Bob, Eve)
        boolean foundEngineering = false;
        for (Row row : rows) {
            if ("Engineering".equals(row.getData()[1])) {
                foundEngineering = true;
                String name = (String) row.getData()[0];
                Integer salary = (Integer) row.getData()[2];
                
                if ("Alice".equals(name)) {
                    // Alice is first in Engineering, should have null prev and Bob's salary as next
                    assertNull(row.getData()[3]);
                    assertEquals(80000, row.getData()[4]);
                } else if ("Eve".equals(name)) {
                    // Eve is last in Engineering, should have Bob's salary as prev and null as next
                    assertEquals(80000, row.getData()[3]);
                    assertNull(row.getData()[4]);
                }
            }
        }
        assertTrue(foundEngineering, "Should find Engineering department rows");
    }
    
    @Test
    void testFirstLastValueWithPartitioning() throws Exception {
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT name, department, salary, " +
            "FIRST_VALUE(name) OVER (PARTITION BY department ORDER BY salary) as first_name, " +
            "LAST_VALUE(name) OVER (PARTITION BY department ORDER BY salary) as last_name " +
            "FROM employees ORDER BY department, salary"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(5, rows.size());
        
        for (Row row : rows) {
            String department = (String) row.getData()[1];
            String firstName = (String) row.getData()[3];
            String name = (String) row.getData()[0];
            
            if ("Engineering".equals(department)) {
                assertEquals("Alice", firstName); // Alice has lowest salary in Engineering
                // Last value depends on current position in frame
            } else if ("Sales".equals(department)) {
                assertEquals("Charlie", firstName); // Charlie has lowest salary in Sales
                // Last value depends on current position in frame
            }
        }
    }
}