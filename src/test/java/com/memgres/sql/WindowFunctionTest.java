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
}