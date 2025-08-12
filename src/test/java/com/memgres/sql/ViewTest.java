package com.memgres.sql;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionResult;
import com.memgres.storage.View;
import com.memgres.types.Column;
import com.memgres.types.DataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for CREATE VIEW and DROP VIEW statements.
 */
public class ViewTest {
    private static final Logger logger = LoggerFactory.getLogger(ViewTest.class);
    
    private MemGresEngine engine;
    private SqlExecutionEngine sqlEngine;
    
    @BeforeEach
    void setUp() {
        engine = new MemGresEngine();
        engine.initialize();
        sqlEngine = new SqlExecutionEngine(engine);
        
        // Create a test table
        String createTableSql = """
            CREATE TABLE employees (
                id INTEGER,
                name TEXT,
                salary INTEGER,
                department TEXT
            )
        """;
        
        String insertSql1 = "INSERT INTO employees VALUES (1, 'Alice', 75000, 'Engineering')";
        String insertSql2 = "INSERT INTO employees VALUES (2, 'Bob', 80000, 'Engineering')";
        String insertSql3 = "INSERT INTO employees VALUES (3, 'Carol', 65000, 'Sales')";
        
        try {
            sqlEngine.execute(createTableSql);
            sqlEngine.execute(insertSql1);
            sqlEngine.execute(insertSql2);
            sqlEngine.execute(insertSql3);
            logger.info("Test setup complete - table created and populated");
        } catch (Exception e) {
            fail("Failed to set up test data: " + e.getMessage());
        }
    }
    
    @AfterEach
    void tearDown() {
        if (engine != null) {
            engine.shutdown();
            logger.info("Test teardown complete - engine shutdown");
        }
    }
    
    @Test
    void testCreateSimpleView() throws Exception {
        logger.info("Starting simple CREATE VIEW test");
        
        String createViewSql = "CREATE VIEW engineering_employees AS SELECT name, salary FROM employees WHERE department = 'Engineering'";
        
        SqlExecutionResult result = sqlEngine.execute(createViewSql);
        
        assertTrue(result.isSuccess());
        assertEquals(SqlExecutionResult.ResultType.DDL, result.getType());
        assertTrue(result.getMessage().contains("created successfully"));
        
        // Verify view exists in schema
        View view = engine.getSchema("public").getView("engineering_employees");
        assertNotNull(view);
        assertEquals("engineering_employees", view.getName());
        assertFalse(view.isForce());
        assertNull(view.getColumnNames()); // No explicit columns specified
        
        logger.info("Simple CREATE VIEW test passed");
    }
    
    @Test
    void testCreateViewWithExplicitColumns() throws Exception {
        logger.info("Starting CREATE VIEW with explicit columns test");
        
        String createViewSql = "CREATE VIEW emp_view (employee_name, employee_salary) AS SELECT name, salary FROM employees";
        
        SqlExecutionResult result = sqlEngine.execute(createViewSql);
        
        assertTrue(result.isSuccess());
        
        // Verify view has explicit column names
        View view = engine.getSchema("public").getView("emp_view");
        assertNotNull(view);
        assertEquals(Arrays.asList("employee_name", "employee_salary"), view.getColumnNames());
        
        logger.info("CREATE VIEW with explicit columns test passed");
    }
    
    @Test
    void testCreateOrReplaceView() throws Exception {
        logger.info("Starting CREATE OR REPLACE VIEW test");
        
        // Create initial view
        String createViewSql = "CREATE VIEW test_view AS SELECT name FROM employees";
        sqlEngine.execute(createViewSql);
        
        // Replace with OR REPLACE
        String replaceViewSql = "CREATE OR REPLACE VIEW test_view AS SELECT name, salary FROM employees";
        SqlExecutionResult result = sqlEngine.execute(replaceViewSql);
        
        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("created or replaced"));
        
        logger.info("CREATE OR REPLACE VIEW test passed");
    }
    
    @Test
    void testCreateViewIfNotExists() throws Exception {
        logger.info("Starting CREATE VIEW IF NOT EXISTS test");
        
        // Create view first time
        String createViewSql = "CREATE VIEW IF NOT EXISTS test_view AS SELECT name FROM employees";
        SqlExecutionResult result1 = sqlEngine.execute(createViewSql);
        assertTrue(result1.isSuccess());
        
        // Try to create again with IF NOT EXISTS
        SqlExecutionResult result2 = sqlEngine.execute(createViewSql);
        assertTrue(result2.isSuccess());
        assertTrue(result2.getMessage().contains("already exists") || result2.getMessage().contains("skipped"));
        
        logger.info("CREATE VIEW IF NOT EXISTS test passed");
    }
    
    @Test
    void testQueryView() throws Exception {
        logger.info("Starting query VIEW test");
        
        // Create view
        String createViewSql = "CREATE VIEW engineering_view AS SELECT name, salary FROM employees WHERE department = 'Engineering'";
        sqlEngine.execute(createViewSql);
        
        // Query the view
        String queryViewSql = "SELECT * FROM engineering_view ORDER BY name";
        SqlExecutionResult result = sqlEngine.execute(queryViewSql);
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        assertEquals(2, result.getRows().size());
        
        // Check that we got Engineering employees
        String firstName = (String) result.getRows().get(0).getData()[0];
        String secondName = (String) result.getRows().get(1).getData()[0];
        assertTrue(Arrays.asList(firstName, secondName).containsAll(Arrays.asList("Alice", "Bob")));
        
        logger.info("Query VIEW test passed - {} rows returned", result.getRows().size());
    }
    
    @Test
    void testDropView() throws Exception {
        logger.info("Starting DROP VIEW test");
        
        // Create view first
        String createViewSql = "CREATE VIEW test_view AS SELECT name FROM employees";
        sqlEngine.execute(createViewSql);
        
        // Verify it exists
        assertNotNull(engine.getSchema("public").getView("test_view"));
        
        // Drop the view
        String dropViewSql = "DROP VIEW test_view";
        SqlExecutionResult result = sqlEngine.execute(dropViewSql);
        
        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("dropped successfully"));
        
        // Verify it's gone
        assertNull(engine.getSchema("public").getView("test_view"));
        
        logger.info("DROP VIEW test passed");
    }
    
    @Test
    void testDropViewIfExists() throws Exception {
        logger.info("Starting DROP VIEW IF EXISTS test");
        
        // Drop non-existent view with IF EXISTS
        String dropViewSql = "DROP VIEW IF EXISTS non_existent_view";
        SqlExecutionResult result = sqlEngine.execute(dropViewSql);
        
        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("does not exist") || result.getMessage().contains("skipped"));
        
        logger.info("DROP VIEW IF EXISTS test passed");
    }
    
    @Test
    void testViewErrorCases() throws Exception {
        logger.info("Starting VIEW error cases test");
        
        // Try to create view with same name twice
        String createViewSql = "CREATE VIEW test_view AS SELECT name FROM employees";
        sqlEngine.execute(createViewSql);
        
        assertThrows(Exception.class, () -> {
            sqlEngine.execute(createViewSql);
        });
        
        // Try to drop non-existent view
        assertThrows(Exception.class, () -> {
            sqlEngine.execute("DROP VIEW non_existent_view");
        });
        
        logger.info("VIEW error cases test passed");
    }
}