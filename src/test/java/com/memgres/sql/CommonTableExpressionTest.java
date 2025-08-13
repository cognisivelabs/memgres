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
 * Test class for Common Table Expressions (CTEs) with WITH clause
 */
class CommonTableExpressionTest {
    
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
    void testSimpleNonRecursiveCTE() throws Exception {
        SqlExecutionResult result = sqlEngine.execute(
            "WITH high_earners AS (" +
            "    SELECT name, salary FROM employees WHERE salary > 70000" +
            ") " +
            "SELECT * FROM high_earners ORDER BY salary"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(3, rows.size());
        
        // Check data is ordered by salary
        assertEquals("Alice", rows.get(0).getData()[0]);
        assertEquals(75000, rows.get(0).getData()[1]);
        
        assertEquals("Bob", rows.get(1).getData()[0]);
        assertEquals(80000, rows.get(1).getData()[1]);
        
        assertEquals("Eve", rows.get(2).getData()[0]);
        assertEquals(90000, rows.get(2).getData()[1]);
    }
    
    @Test
    void testCTEWithSpecifiedColumns() throws Exception {
        SqlExecutionResult result = sqlEngine.execute(
            "WITH dept_info(department_name, employee_count) AS (" +
            "    SELECT department, COUNT(*) FROM employees GROUP BY department" +
            ") " +
            "SELECT department_name, employee_count FROM dept_info ORDER BY department_name"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(2, rows.size());
        
        // Check Engineering department
        assertEquals("Engineering", rows.get(0).getData()[0]);
        assertEquals(3L, rows.get(0).getData()[1]);
        
        // Check Sales department
        assertEquals("Sales", rows.get(1).getData()[0]);
        assertEquals(2L, rows.get(1).getData()[1]);
    }
    
    @Test
    void testMultipleCTEs() throws Exception {
        SqlExecutionResult result = sqlEngine.execute(
            "WITH engineering_emp AS (" +
            "    SELECT name, salary FROM employees WHERE department = 'Engineering'" +
            "), " +
            "sales_emp AS (" +
            "    SELECT name, salary FROM employees WHERE department = 'Sales'" +
            ") " +
            "SELECT COUNT(*) as eng_count FROM engineering_emp"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(1, rows.size());
        
        // Check Engineering department count
        assertEquals(3L, rows.get(0).getData()[0]);
    }
    
    @Test
    void testCTEWithJoin() throws Exception {
        // Create another table for joining
        sqlEngine.execute("CREATE TABLE departments (name VARCHAR(50), budget INTEGER)");
        sqlEngine.execute("INSERT INTO departments VALUES ('Engineering', 500000)");
        sqlEngine.execute("INSERT INTO departments VALUES ('Sales', 300000)");
        
        SqlExecutionResult result = sqlEngine.execute(
            "WITH emp_dept AS (" +
            "    SELECT e.name, e.department, d.budget " +
            "    FROM employees e JOIN departments d ON e.department = d.name" +
            ") " +
            "SELECT name, budget FROM emp_dept WHERE budget > 400000 ORDER BY name"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(3, rows.size()); // Only Engineering employees (budget > 400000)
        
        // Check that all results are Engineering employees
        for (Row row : rows) {
            assertEquals(500000, row.getData()[1]); // Engineering budget
        }
    }
    
    @Test
    void testNestedCTEReference() throws Exception {
        SqlExecutionResult result = sqlEngine.execute(
            "WITH base_data AS (" +
            "    SELECT name, salary FROM employees WHERE salary > 60000" +
            "), " +
            "filtered_data AS (" +
            "    SELECT name, salary FROM base_data WHERE salary < 85000" +
            ") " +
            "SELECT COUNT(*) as total_count FROM filtered_data"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(1, rows.size());
        
        // Should have Alice (75000), Diana (65000), and Bob (80000)
        assertEquals(3L, rows.get(0).getData()[0]);
    }
}