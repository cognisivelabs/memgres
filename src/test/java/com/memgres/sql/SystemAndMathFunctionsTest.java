package com.memgres.sql;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionResult;
import com.memgres.types.Row;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for H2 System and Math functions.
 * Tests DATABASE(), USER(), SESSION_ID(), SQRT(), POWER(), ABS(), ROUND(), RAND() functions.
 */
public class SystemAndMathFunctionsTest {
    private static final Logger logger = LoggerFactory.getLogger(SystemAndMathFunctionsTest.class);
    
    private MemGresEngine engine;
    private SqlExecutionEngine sqlEngine;
    
    @BeforeEach
    void setUp() throws Exception {
        logger.info("Setting up test environment");
        engine = new MemGresEngine();
        engine.initialize();
        sqlEngine = new SqlExecutionEngine(engine);
        
        // Create a test table for function testing
        sqlEngine.execute("CREATE TABLE test_numbers (id INTEGER, number_value DECIMAL(10,2), negative INTEGER)");
        sqlEngine.execute("INSERT INTO test_numbers VALUES (1, 16.0, 5)");
        sqlEngine.execute("INSERT INTO test_numbers VALUES (2, 25.0, 10)");
        sqlEngine.execute("INSERT INTO test_numbers VALUES (3, 3.14159, 7)");
        // Update to negative values
        sqlEngine.execute("UPDATE test_numbers SET negative = 0 - negative");
        
        logger.info("Test setup complete - table created and populated");
    }
    
    @AfterEach
    void tearDown() throws Exception {
        logger.info("Tearing down test environment");
        if (engine != null) {
            engine.shutdown();
        }
        logger.info("Test teardown complete - engine shutdown");
    }
    
    @Test
    void testSystemFunctions() throws Exception {
        logger.info("Starting system functions test");
        
        // Test DATABASE() function
        SqlExecutionResult result1 = sqlEngine.execute("SELECT DATABASE()");
        assertTrue(result1.isSuccess());
        List<Row> rows1 = result1.getRows();
        assertEquals(1, rows1.size());
        assertEquals("memgres", rows1.get(0).getData()[0]);
        logger.info("DATABASE() function test passed");
        
        // Test USER() function
        SqlExecutionResult result2 = sqlEngine.execute("SELECT USER()");
        assertTrue(result2.isSuccess());
        List<Row> rows2 = result2.getRows();
        assertEquals(1, rows2.size());
        assertEquals("sa", rows2.get(0).getData()[0]);
        logger.info("USER() function test passed");
        
        // Test CURRENT_USER() function
        SqlExecutionResult result3 = sqlEngine.execute("SELECT CURRENT_USER()");
        assertTrue(result3.isSuccess());
        List<Row> rows3 = result3.getRows();
        assertEquals(1, rows3.size());
        assertEquals("sa", rows3.get(0).getData()[0]);
        logger.info("CURRENT_USER() function test passed");
        
        // Test SESSION_USER() function
        SqlExecutionResult result4 = sqlEngine.execute("SELECT SESSION_USER()");
        assertTrue(result4.isSuccess());
        List<Row> rows4 = result4.getRows();
        assertEquals(1, rows4.size());
        assertEquals("sa", rows4.get(0).getData()[0]);
        logger.info("SESSION_USER() function test passed");
        
        // Test SESSION_ID() function
        SqlExecutionResult result5 = sqlEngine.execute("SELECT SESSION_ID()");
        assertTrue(result5.isSuccess());
        List<Row> rows5 = result5.getRows();
        assertEquals(1, rows5.size());
        assertNotNull(rows5.get(0).getData()[0]);
        assertTrue(rows5.get(0).getData()[0] instanceof Long);
        logger.info("SESSION_ID() function test passed");
        
        logger.info("All system functions test passed");
    }
    
    @Test
    void testSqrtFunction() throws Exception {
        logger.info("Starting SQRT function test");
        
        // Test SQRT with literal values
        SqlExecutionResult result1 = sqlEngine.execute("SELECT SQRT(16), SQRT(25), SQRT(2)");
        assertTrue(result1.isSuccess());
        List<Row> rows1 = result1.getRows();
        assertEquals(1, rows1.size());
        assertEquals(4.0, (Double) rows1.get(0).getData()[0], 0.0001);
        assertEquals(5.0, (Double) rows1.get(0).getData()[1], 0.0001);
        assertEquals(Math.sqrt(2), (Double) rows1.get(0).getData()[2], 0.0001);
        
        // Test SQRT with column values
        SqlExecutionResult result2 = sqlEngine.execute("SELECT id, SQRT(number_value) FROM test_numbers ORDER BY id");
        assertTrue(result2.isSuccess());
        List<Row> rows2 = result2.getRows();
        assertEquals(3, rows2.size());
        assertEquals(4.0, (Double) rows2.get(0).getData()[1], 0.0001); // SQRT(16)
        assertEquals(5.0, (Double) rows2.get(1).getData()[1], 0.0001); // SQRT(25)
        assertEquals(Math.sqrt(3.14159), (Double) rows2.get(2).getData()[1], 0.0001);
        
        logger.info("SQRT function test passed");
    }
    
    @Test
    void testPowerFunction() throws Exception {
        logger.info("Starting POWER function test");
        
        // Test POWER with literal values
        SqlExecutionResult result1 = sqlEngine.execute("SELECT POWER(2, 3), POWER(4, 2), POWER(9, 0.5)");
        assertTrue(result1.isSuccess());
        List<Row> rows1 = result1.getRows();
        assertEquals(1, rows1.size());
        assertEquals(8.0, (Double) rows1.get(0).getData()[0], 0.0001);
        assertEquals(16.0, (Double) rows1.get(0).getData()[1], 0.0001);
        assertEquals(3.0, (Double) rows1.get(0).getData()[2], 0.0001); // 9^0.5 = sqrt(9) = 3
        
        // Test POWER with column values
        SqlExecutionResult result2 = sqlEngine.execute("SELECT id, POWER(number_value, 2) FROM test_numbers WHERE id <= 2 ORDER BY id");
        assertTrue(result2.isSuccess());
        List<Row> rows2 = result2.getRows();
        assertEquals(2, rows2.size());
        assertEquals(256.0, (Double) rows2.get(0).getData()[1], 0.0001); // 16^2
        assertEquals(625.0, (Double) rows2.get(1).getData()[1], 0.0001); // 25^2
        
        logger.info("POWER function test passed");
    }
    
    @Test
    void testAbsFunction() throws Exception {
        logger.info("Starting ABS function test");
        
        // Test ABS with literal values
        SqlExecutionResult result1 = sqlEngine.execute("SELECT ABS(5), ABS(0 - 10), ABS(0), ABS(0 - 3.14)");
        assertTrue(result1.isSuccess());
        List<Row> rows1 = result1.getRows();
        assertEquals(1, rows1.size());
        // ABS preserves original type but some operations may return different types
        assertEquals(5, ((Number) rows1.get(0).getData()[0]).intValue());
        assertEquals(10, ((Number) rows1.get(0).getData()[1]).intValue());
        assertEquals(0, ((Number) rows1.get(0).getData()[2]).intValue());
        assertEquals(3.14, (Double) rows1.get(0).getData()[3], 0.0001);
        
        // Test ABS with column values (negative column)
        SqlExecutionResult result2 = sqlEngine.execute("SELECT id, negative, ABS(negative) FROM test_numbers ORDER BY id");
        assertTrue(result2.isSuccess());
        List<Row> rows2 = result2.getRows();
        assertEquals(3, rows2.size());
        assertEquals(-5, rows2.get(0).getData()[1]);
        assertEquals(5, rows2.get(0).getData()[2]);
        assertEquals(-10, rows2.get(1).getData()[1]);
        assertEquals(10, rows2.get(1).getData()[2]);
        assertEquals(-7, rows2.get(2).getData()[1]);
        assertEquals(7, rows2.get(2).getData()[2]);
        
        logger.info("ABS function test passed");
    }
    
    @Test
    void testRoundFunction() throws Exception {
        logger.info("Starting ROUND function test");
        
        // Test ROUND with default precision (0 decimal places)
        SqlExecutionResult result1 = sqlEngine.execute("SELECT ROUND(3.14159), ROUND(3.6), ROUND(2.5), ROUND(3.5)");
        assertTrue(result1.isSuccess());
        List<Row> rows1 = result1.getRows();
        assertEquals(1, rows1.size());
        assertEquals(3L, rows1.get(0).getData()[0]); // Default precision rounds to long
        assertEquals(4L, rows1.get(0).getData()[1]);
        assertEquals(2L, rows1.get(0).getData()[2]); // HALF_EVEN rounding: 2.5 -> 2
        assertEquals(4L, rows1.get(0).getData()[3]); // HALF_EVEN rounding: 3.5 -> 4
        
        // Test ROUND with specific precision (avoid negative literals for now)
        SqlExecutionResult result2 = sqlEngine.execute("SELECT ROUND(3.14159, 2), ROUND(123.456, 1)");
        assertTrue(result2.isSuccess());
        List<Row> rows2 = result2.getRows();
        assertEquals(1, rows2.size());
        assertEquals(3.14, (Double) rows2.get(0).getData()[0], 0.0001);
        assertEquals(123.5, (Double) rows2.get(0).getData()[1], 0.0001);
        
        // Test ROUND with column values
        SqlExecutionResult result3 = sqlEngine.execute("SELECT id, number_value, ROUND(number_value, 1) FROM test_numbers WHERE id = 3");
        assertTrue(result3.isSuccess());
        List<Row> rows3 = result3.getRows();
        assertEquals(1, rows3.size());
        assertEquals(3.1, (Double) rows3.get(0).getData()[2], 0.0001); // ROUND(3.14159, 1) = 3.1
        
        logger.info("ROUND function test passed");
    }
    
    @Test
    void testRandFunction() throws Exception {
        logger.info("Starting RAND function test");
        
        // Test RAND function returns values in range [0, 1)
        SqlExecutionResult result = sqlEngine.execute("SELECT RAND(), RAND(), RAND()");
        assertTrue(result.isSuccess());
        List<Row> rows = result.getRows();
        assertEquals(1, rows.size());
        
        // Check that all three RAND() calls return different values in valid range
        Double rand1 = (Double) rows.get(0).getData()[0];
        Double rand2 = (Double) rows.get(0).getData()[1];
        Double rand3 = (Double) rows.get(0).getData()[2];
        
        assertNotNull(rand1);
        assertNotNull(rand2);
        assertNotNull(rand3);
        
        // Verify range [0, 1)
        assertTrue(rand1 >= 0.0 && rand1 < 1.0);
        assertTrue(rand2 >= 0.0 && rand2 < 1.0);
        assertTrue(rand3 >= 0.0 && rand3 < 1.0);
        
        // Verify randomness (very unlikely all three are the same)
        assertFalse(rand1.equals(rand2) && rand2.equals(rand3), "All RAND() calls returned the same value - highly unlikely");
        
        logger.info("RAND function test passed");
    }
    
    @Test
    void testFunctionCombinations() throws Exception {
        logger.info("Starting function combinations test");
        
        // Test combining multiple functions
        SqlExecutionResult result1 = sqlEngine.execute(
            "SELECT ABS(negative), SQRT(ABS(negative)), ROUND(SQRT(ABS(negative)), 2) FROM test_numbers WHERE id = 1"
        );
        assertTrue(result1.isSuccess());
        List<Row> rows1 = result1.getRows();
        assertEquals(1, rows1.size());
        assertEquals(5, rows1.get(0).getData()[0]); // ABS(-5) = 5
        assertEquals(Math.sqrt(5), (Double) rows1.get(0).getData()[1], 0.0001); // SQRT(5)
        assertEquals(2.24, (Double) rows1.get(0).getData()[2], 0.0001); // ROUND(SQRT(5), 2)
        
        // Test system function with math function
        SqlExecutionResult result2 = sqlEngine.execute(
            "SELECT DATABASE(), POWER(2, 3), USER(), SQRT(16)"
        );
        assertTrue(result2.isSuccess());
        List<Row> rows2 = result2.getRows();
        assertEquals(1, rows2.size());
        assertEquals("memgres", rows2.get(0).getData()[0]);
        assertEquals(8.0, (Double) rows2.get(0).getData()[1], 0.0001);
        assertEquals("sa", rows2.get(0).getData()[2]);
        assertEquals(4.0, (Double) rows2.get(0).getData()[3], 0.0001);
        
        logger.info("Function combinations test passed");
    }
    
    @Test
    void testErrorCases() throws Exception {
        logger.info("Starting error cases test");
        
        // Test SQRT with negative number
        assertThrows(Exception.class, () -> {
            sqlEngine.execute("SELECT SQRT(-1)");
        }, "SQRT of negative number should throw exception");
        
        // Test POWER with invalid arguments count  
        assertThrows(Exception.class, () -> {
            sqlEngine.execute("SELECT POWER(2)");
        }, "POWER with wrong argument count should throw exception");
        
        // Test ABS with non-numeric argument would be caught at parse/type level
        // Testing with NULL values
        SqlExecutionResult result1 = sqlEngine.execute("SELECT ABS(NULL), SQRT(NULL), POWER(NULL, 2), ROUND(NULL)");
        assertTrue(result1.isSuccess());
        List<Row> rows1 = result1.getRows();
        assertEquals(1, rows1.size());
        assertNull(rows1.get(0).getData()[0]); // ABS(NULL) = NULL
        assertNull(rows1.get(0).getData()[1]); // SQRT(NULL) = NULL
        assertNull(rows1.get(0).getData()[2]); // POWER(NULL, 2) = NULL
        assertNull(rows1.get(0).getData()[3]); // ROUND(NULL) = NULL
        
        logger.info("Error cases test passed");
    }
}