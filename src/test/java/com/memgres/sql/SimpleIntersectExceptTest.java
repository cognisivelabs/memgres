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
 * Simple test class for INTERSECT and EXCEPT set operations without ORDER BY
 */
class SimpleIntersectExceptTest {
    
    private MemGresEngine engine;
    private SqlExecutionEngine sqlEngine;
    
    @BeforeEach
    void setUp() throws Exception {
        engine = new MemGresEngine();
        engine.initialize();
        sqlEngine = new SqlExecutionEngine(engine);
        
        // Create simple test tables
        sqlEngine.execute("CREATE TABLE numbers1 (n INTEGER)");
        sqlEngine.execute("INSERT INTO numbers1 VALUES (1), (2), (3), (4), (5)");
        
        sqlEngine.execute("CREATE TABLE numbers2 (n INTEGER)");
        sqlEngine.execute("INSERT INTO numbers2 VALUES (3), (4), (5), (6), (7)");
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (engine != null) {
            engine.shutdown();
        }
    }
    
    @Test
    void testSimpleIntersectNoOrderBy() throws Exception {
        // Test INTERSECT without ORDER BY
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT n FROM numbers1 " +
            "INTERSECT " +
            "SELECT n FROM numbers2"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(3, rows.size()); // Should return 3, 4, 5
    }
    
    @Test
    void testSimpleExceptNoOrderBy() throws Exception {
        // Test EXCEPT without ORDER BY
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT n FROM numbers1 " +
            "EXCEPT " +
            "SELECT n FROM numbers2"
        );
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        assertEquals(2, rows.size()); // Should return 1, 2
    }
}