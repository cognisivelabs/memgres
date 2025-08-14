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
 * Test class for Recursive Common Table Expressions (CTEs)
 */
class RecursiveCteTest {
    
    private MemGresEngine engine;
    private SqlExecutionEngine sqlEngine;
    
    @BeforeEach
    void setUp() throws Exception {
        engine = new MemGresEngine();
        engine.initialize();
        sqlEngine = new SqlExecutionEngine(engine);
        
        // Create test table for hierarchical data (manager_id can be NULL for CEO)
        // First let's try with a simpler test
        sqlEngine.execute("CREATE TABLE employees (id INTEGER, name VARCHAR(50), manager_id INTEGER)");
        
        // Insert test data - organizational hierarchy (simplified without NULLs for now)
        sqlEngine.execute("INSERT INTO employees VALUES (1, 'CEO', 0)"); // Use 0 instead of NULL
        sqlEngine.execute("INSERT INTO employees VALUES (2, 'VP-Sales', 1)");
        sqlEngine.execute("INSERT INTO employees VALUES (3, 'VP-Tech', 1)");
        sqlEngine.execute("INSERT INTO employees VALUES (4, 'Dir-Sales', 2)");
        sqlEngine.execute("INSERT INTO employees VALUES (5, 'Dir-Product', 3)");
        sqlEngine.execute("INSERT INTO employees VALUES (6, 'Mgr-Sales1', 4)");
        sqlEngine.execute("INSERT INTO employees VALUES (7, 'Mgr-Sales2', 4)");
        sqlEngine.execute("INSERT INTO employees VALUES (8, 'Mgr-Dev', 5)");
        
        // Create numeric sequence table for testing
        sqlEngine.execute("CREATE TABLE numbers (n INTEGER)");
        sqlEngine.execute("INSERT INTO numbers VALUES (1)");
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (engine != null) {
            engine.shutdown();
        }
    }
    
    @Test
    void testSimpleRecursiveCte() throws Exception {
        // Test simple numeric sequence generation
        SqlExecutionResult result = sqlEngine.execute(
            "WITH RECURSIVE numbers_cte(n) AS (" +
                "SELECT 1 " +
                "UNION ALL " +
                "SELECT n + 1 FROM numbers_cte WHERE n < 5" +
            ") " +
            "SELECT n FROM numbers_cte ORDER BY n"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(5, rows.size()); // Should generate 1, 2, 3, 4, 5
        
        // Verify sequence values
        for (int i = 0; i < 5; i++) {
            assertEquals(i + 1, ((Number) rows.get(i).getData()[0]).intValue());
        }
    }
    
    @Test
    void testHierarchicalRecursiveCte() throws Exception {
        // Test organizational hierarchy traversal
        SqlExecutionResult result = sqlEngine.execute(
            "WITH RECURSIVE employee_hierarchy(id, name, manager_id, level) AS (" +
                "SELECT id, name, manager_id, 0 as level FROM employees WHERE manager_id = 0 " +
                "UNION ALL " +
                "SELECT e.id, e.name, e.manager_id, eh.level + 1 " +
                "FROM employees e " +
                "JOIN employee_hierarchy eh ON e.manager_id = eh.id" +
            ") " +
            "SELECT name, level FROM employee_hierarchy ORDER BY level, name"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(8, rows.size()); // All employees
        
        // Check that CEO is at level 0
        Object[] ceoRow = rows.get(0).getData();
        assertEquals("CEO", ceoRow[0]);
        assertEquals(0, ((Number) ceoRow[1]).intValue());
        
        // Check hierarchical levels make sense
        int maxLevel = -1;
        for (Row row : rows) {
            int level = ((Number) row.getData()[1]).intValue();
            assertTrue(level >= 0 && level <= 3, "Level should be between 0-3");
            maxLevel = Math.max(maxLevel, level);
        }
        assertEquals(3, maxLevel); // Should have 4 levels (0-3)
    }
    
    @Test
    void testRecursiveCteWithUnionValidation() throws Exception {
        // Test that UNION (not UNION ALL) is rejected for recursive CTEs
        assertThrows(Exception.class, () -> {
            sqlEngine.execute(
                "WITH RECURSIVE invalid_cte(n) AS (" +
                    "SELECT 1 " +
                    "UNION " + // Should fail - must be UNION ALL
                    "SELECT n + 1 FROM invalid_cte WHERE n < 3" +
                ") " +
                "SELECT n FROM invalid_cte"
            );
        });
    }
    
    @Test
    void testNonRecursiveCteStillWorks() throws Exception {
        // Test that non-recursive CTEs continue to work
        SqlExecutionResult result = sqlEngine.execute(
            "WITH emp_cte AS (" +
                "SELECT id, name FROM employees WHERE id = 1" +
            ") " +
            "SELECT name FROM emp_cte"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(1, rows.size());
        
        // Should return CEO name
        String empName = (String) rows.get(0).getData()[0];
        assertEquals("CEO", empName);
    }
    
    @Test
    void testRecursiveCteTermination() throws Exception {
        // Test that recursive CTE properly terminates when no more rows are generated
        SqlExecutionResult result = sqlEngine.execute(
            "WITH RECURSIVE fib_cte(n, fib_n, fib_n_plus_1) AS (" +
                "SELECT 1, 1, 1 " +
                "UNION ALL " +
                "SELECT n + 1, fib_n_plus_1, fib_n + fib_n_plus_1 " +
                "FROM fib_cte " +
                "WHERE n < 10 AND fib_n_plus_1 < 100" +
            ") " +
            "SELECT n, fib_n FROM fib_cte ORDER BY n"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        
        // Should terminate when fibonacci exceeds 100 or n reaches 10
        assertTrue(rows.size() <= 10);
        assertTrue(rows.size() > 0);
        
        // Verify fibonacci sequence properties
        for (int i = 0; i < Math.min(rows.size(), 5); i++) {
            Object[] row = rows.get(i).getData();
            int n = ((Number) row[0]).intValue();
            int fib_n = ((Number) row[1]).intValue();
            
            assertTrue(n >= 1);
            assertTrue(fib_n >= 1);
        }
    }
}