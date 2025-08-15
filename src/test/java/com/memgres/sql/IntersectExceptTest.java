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
 * Test class for INTERSECT and EXCEPT set operations
 */
class IntersectExceptTest {
    
    private MemGresEngine engine;
    private SqlExecutionEngine sqlEngine;
    
    @BeforeEach
    void setUp() throws Exception {
        engine = new MemGresEngine();
        engine.initialize();
        sqlEngine = new SqlExecutionEngine(engine);
        
        // Create test tables with sample data
        setupTestData();
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (engine != null) {
            engine.shutdown();
        }
    }
    
    private void setupTestData() throws Exception {
        // Create employees table
        sqlEngine.execute("CREATE TABLE employees (id INTEGER, name VARCHAR(50), department VARCHAR(50))");
        sqlEngine.execute("INSERT INTO employees VALUES (1, 'Alice', 'Engineering')");
        sqlEngine.execute("INSERT INTO employees VALUES (2, 'Bob', 'Sales')");
        sqlEngine.execute("INSERT INTO employees VALUES (3, 'Carol', 'Engineering')");
        sqlEngine.execute("INSERT INTO employees VALUES (4, 'David', 'Marketing')");
        sqlEngine.execute("INSERT INTO employees VALUES (5, 'Eve', 'Engineering')");
        
        // Create contractors table with overlapping data
        sqlEngine.execute("CREATE TABLE contractors (id INTEGER, name VARCHAR(50), department VARCHAR(50))");
        sqlEngine.execute("INSERT INTO contractors VALUES (1, 'Alice', 'Engineering')");  // Same as employee
        sqlEngine.execute("INSERT INTO contractors VALUES (6, 'Frank', 'Engineering')");
        sqlEngine.execute("INSERT INTO contractors VALUES (7, 'Grace', 'Sales')");
        sqlEngine.execute("INSERT INTO contractors VALUES (3, 'Carol', 'Engineering')");  // Same as employee
        sqlEngine.execute("INSERT INTO contractors VALUES (8, 'Henry', 'Marketing')");
        
        // Create numbers tables for simple testing
        sqlEngine.execute("CREATE TABLE numbers1 (n INTEGER)");
        sqlEngine.execute("INSERT INTO numbers1 VALUES (1), (2), (3), (4), (5)");
        
        sqlEngine.execute("CREATE TABLE numbers2 (n INTEGER)");
        sqlEngine.execute("INSERT INTO numbers2 VALUES (3), (4), (5), (6), (7)");
    }
    
    @Test
    void testSimpleIntersect() throws Exception {
        // Test INTERSECT with overlapping number sets
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT n FROM numbers1 " +
            "INTERSECT " +
            "SELECT n FROM numbers2 " +
            "ORDER BY n"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(3, rows.size()); // Should return 3, 4, 5
        
        // Verify the intersection values
        assertEquals(3, ((Number) rows.get(0).getData()[0]).intValue());
        assertEquals(4, ((Number) rows.get(1).getData()[0]).intValue());
        assertEquals(5, ((Number) rows.get(2).getData()[0]).intValue());
    }
    
    @Test
    void testSimpleExcept() throws Exception {
        // Test EXCEPT with number sets
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT n FROM numbers1 " +
            "EXCEPT " +
            "SELECT n FROM numbers2 " +
            "ORDER BY n"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(2, rows.size()); // Should return 1, 2
        
        // Verify the except values
        assertEquals(1, ((Number) rows.get(0).getData()[0]).intValue());
        assertEquals(2, ((Number) rows.get(1).getData()[0]).intValue());
    }
    
    @Test
    void testIntersectWithMultipleColumns() throws Exception {
        // Test INTERSECT with employees and contractors
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT id, name, department FROM employees " +
            "INTERSECT " +
            "SELECT id, name, department FROM contractors " +
            "ORDER BY id"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(2, rows.size()); // Should return Alice and Carol
        
        // Verify the common employees/contractors
        Object[] firstRow = rows.get(0).getData();
        assertEquals(1, ((Number) firstRow[0]).intValue());
        assertEquals("Alice", firstRow[1]);
        assertEquals("Engineering", firstRow[2]);
        
        Object[] secondRow = rows.get(1).getData();
        assertEquals(3, ((Number) secondRow[0]).intValue());
        assertEquals("Carol", secondRow[1]);
        assertEquals("Engineering", secondRow[2]);
    }
    
    @Test
    void testExceptWithMultipleColumns() throws Exception {
        // Test EXCEPT with employees and contractors
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT id, name, department FROM employees " +
            "EXCEPT " +
            "SELECT id, name, department FROM contractors " +
            "ORDER BY id"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(3, rows.size()); // Should return Bob, David, Eve
        
        // Verify employees who are not contractors
        Object[] firstRow = rows.get(0).getData();
        assertEquals(2, ((Number) firstRow[0]).intValue());
        assertEquals("Bob", firstRow[1]);
        
        Object[] secondRow = rows.get(1).getData();
        assertEquals(4, ((Number) secondRow[0]).intValue());
        assertEquals("David", secondRow[1]);
        
        Object[] thirdRow = rows.get(2).getData();
        assertEquals(5, ((Number) thirdRow[0]).intValue());
        assertEquals("Eve", thirdRow[1]);
    }
    
    @Test
    void testIntersectNoDuplicates() throws Exception {
        // Create a table with duplicates
        sqlEngine.execute("CREATE TABLE duplicates (n INTEGER)");
        sqlEngine.execute("INSERT INTO duplicates VALUES (3), (3), (4), (4), (5)");
        
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT n FROM duplicates " +
            "INTERSECT " +
            "SELECT n FROM numbers2 " +
            "ORDER BY n"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(3, rows.size()); // Should return 3, 4, 5 (no duplicates)
        
        // Verify values are unique
        assertEquals(3, ((Number) rows.get(0).getData()[0]).intValue());
        assertEquals(4, ((Number) rows.get(1).getData()[0]).intValue());
        assertEquals(5, ((Number) rows.get(2).getData()[0]).intValue());
    }
    
    @Test
    void testExceptNoDuplicates() throws Exception {
        // Create a table with duplicates
        sqlEngine.execute("CREATE TABLE duplicates2 (n INTEGER)");
        sqlEngine.execute("INSERT INTO duplicates2 VALUES (1), (1), (2), (2), (8), (9)");
        
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT n FROM duplicates2 " +
            "EXCEPT " +
            "SELECT n FROM numbers2 " +
            "ORDER BY n"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(4, rows.size()); // Should return 1, 2, 8, 9 (no duplicates)
        
        // Verify unique values
        assertEquals(1, ((Number) rows.get(0).getData()[0]).intValue());
        assertEquals(2, ((Number) rows.get(1).getData()[0]).intValue());
        assertEquals(8, ((Number) rows.get(2).getData()[0]).intValue());
        assertEquals(9, ((Number) rows.get(3).getData()[0]).intValue());
    }
    
    @Test
    void testIntersectEmptyResult() throws Exception {
        // Create non-overlapping tables
        sqlEngine.execute("CREATE TABLE range1 (n INTEGER)");
        sqlEngine.execute("INSERT INTO range1 VALUES (1), (2), (3)");
        
        sqlEngine.execute("CREATE TABLE range2 (n INTEGER)");
        sqlEngine.execute("INSERT INTO range2 VALUES (4), (5), (6)");
        
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT n FROM range1 " +
            "INTERSECT " +
            "SELECT n FROM range2"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(0, rows.size()); // Should return no rows
    }
    
    @Test
    void testExceptEmptyResult() throws Exception {
        // Test EXCEPT where all rows are removed
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT n FROM numbers1 " +
            "EXCEPT " +
            "SELECT n FROM numbers1"  // Same table
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(0, rows.size()); // Should return no rows
    }
    
    @Test
    void testColumnMismatchError() throws Exception {
        // Test error when column counts don't match
        assertThrows(Exception.class, () -> {
            sqlEngine.execute(
                "SELECT id, name FROM employees " +
                "INTERSECT " +
                "SELECT n FROM numbers1"
            );
        });
        
        assertThrows(Exception.class, () -> {
            sqlEngine.execute(
                "SELECT id, name FROM employees " +
                "EXCEPT " +
                "SELECT n FROM numbers1"
            );
        });
    }
    
    @Test
    void testChainedSetOperations() throws Exception {
        // Create a third numbers table for chaining
        sqlEngine.execute("CREATE TABLE numbers3 (n INTEGER)");
        sqlEngine.execute("INSERT INTO numbers3 VALUES (2), (3), (4), (8), (9)");
        
        // Test INTERSECT followed by EXCEPT
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT n FROM numbers1 " +           // 1, 2, 3, 4, 5
            "INTERSECT " +
            "SELECT n FROM numbers2 " +           // 3, 4, 5, 6, 7  -> intersection: 3, 4, 5
            "EXCEPT " +
            "SELECT n FROM numbers3 " +           // 2, 3, 4, 8, 9  -> except: 5
            "ORDER BY n"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(1, rows.size()); // Should return only 5
        
        assertEquals(5, ((Number) rows.get(0).getData()[0]).intValue());
    }
}