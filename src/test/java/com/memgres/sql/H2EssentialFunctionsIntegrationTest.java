package com.memgres.sql;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionResult;
import com.memgres.types.Row;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for H2 Essential Functions (Phase 3.4).
 * Tests the new date/time, system, and string utility functions via SQL execution.
 */
class H2EssentialFunctionsIntegrationTest {
    
    private MemGresEngine engine;
    private SqlExecutionEngine sqlEngine;
    
    @BeforeEach
    void setUp() throws Exception {
        engine = new MemGresEngine();
        engine.initialize();
        sqlEngine = new SqlExecutionEngine(engine);
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (engine != null) {
            engine.shutdown();
        }
    }
    
    // ===== DATE/TIME FUNCTIONS TESTS =====
    
    @Test
    void testCurrentTimestamp() throws Exception {
        SqlExecutionResult result = sqlEngine.execute("SELECT CURRENT_TIMESTAMP() as current_ts");
        
        assertNotNull(result);
        assertEquals(1, result.getRows().size());
        
        Object timestamp = result.getRows().get(0).getValue(0);
        assertInstanceOf(LocalDateTime.class, timestamp);
        
        LocalDateTime ts = (LocalDateTime) timestamp;
        LocalDateTime now = LocalDateTime.now();
        
        // Should be within 5 seconds of current time
        assertTrue(Math.abs(java.time.Duration.between(ts, now).toSeconds()) < 5);
    }
    
    @Test
    void testCurrentDate() throws Exception {
        SqlExecutionResult result = sqlEngine.execute("SELECT CURRENT_DATE() as current_dt");
        
        assertNotNull(result);
        assertEquals(1, result.getRows().size());
        
        Object date = result.getRows().get(0).getValue(0);
        assertInstanceOf(LocalDate.class, date);
        assertEquals(LocalDate.now(), date);
    }
    
    @Test
    void testCurrentTime() throws Exception {
        SqlExecutionResult result = sqlEngine.execute("SELECT CURRENT_TIME() as current_tm");
        
        assertNotNull(result);
        assertEquals(1, result.getRows().size());
        
        Object time = result.getRows().get(0).getValue(0);
        assertInstanceOf(LocalTime.class, time);
        
        LocalTime tm = (LocalTime) time;
        LocalTime now = LocalTime.now();
        
        // Should be within 5 seconds of current time
        assertTrue(Math.abs(java.time.Duration.between(tm, now).toSeconds()) < 5);
    }
    
    @Test
    void testDateAdd() throws Exception {
        // Test adding days
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT DATEADD('DAY', 7, PARSEDATETIME('2023-01-15 10:30:00', 'yyyy-MM-dd HH:mm:ss')) as result"
        );
        
        assertNotNull(result);
        assertEquals(1, result.getRows().size());
        
        Object dateTime = result.getRows().get(0).getValue(0);
        assertInstanceOf(LocalDateTime.class, dateTime);
        
        LocalDateTime expected = LocalDateTime.of(2023, 1, 22, 10, 30, 0);
        assertEquals(expected, dateTime);
        
        // Test adding months
        result = sqlEngine.execute(
            "SELECT DATEADD('MONTH', 2, PARSEDATETIME('2023-01-15 10:30:00', 'yyyy-MM-dd HH:mm:ss')) as result"
        );
        
        dateTime = result.getRows().get(0).getValue(0);
        expected = LocalDateTime.of(2023, 3, 15, 10, 30, 0);
        assertEquals(expected, dateTime);
        
        // Test adding years
        result = sqlEngine.execute(
            "SELECT DATEADD('YEAR', 1, PARSEDATETIME('2023-01-15 10:30:00', 'yyyy-MM-dd HH:mm:ss')) as result"
        );
        
        dateTime = result.getRows().get(0).getValue(0);
        expected = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        assertEquals(expected, dateTime);
    }
    
    @Test
    void testDateDiff() throws Exception {
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT DATEDIFF('DAY', " +
            "PARSEDATETIME('2023-01-15 10:30:00', 'yyyy-MM-dd HH:mm:ss'), " +
            "PARSEDATETIME('2023-01-22 15:45:30', 'yyyy-MM-dd HH:mm:ss')) as result"
        );
        
        assertNotNull(result);
        assertEquals(1, result.getRows().size());
        
        Object diff = result.getRows().get(0).getValue(0);
        assertEquals(7L, diff);
        
        // Test hour difference
        result = sqlEngine.execute(
            "SELECT DATEDIFF('HOUR', " +
            "PARSEDATETIME('2023-01-15 10:30:00', 'yyyy-MM-dd HH:mm:ss'), " +
            "PARSEDATETIME('2023-01-22 15:45:30', 'yyyy-MM-dd HH:mm:ss')) as result"
        );
        
        diff = result.getRows().get(0).getValue(0);
        assertEquals(173L, diff); // 7 days * 24 + 5 hours
    }
    
    @Test
    void testFormatDateTime() throws Exception {
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT FORMATDATETIME(PARSEDATETIME('2023-12-25 14:30:45', 'yyyy-MM-dd HH:mm:ss'), 'dd/MM/yyyy') as result"
        );
        
        assertNotNull(result);
        assertEquals(1, result.getRows().size());
        
        Object formatted = result.getRows().get(0).getValue(0);
        assertEquals("25/12/2023", formatted);
    }
    
    @Test
    void testParseDateTime() throws Exception {
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT PARSEDATETIME('2023-12-25 14:30:45', 'yyyy-MM-dd HH:mm:ss') as result"
        );
        
        assertNotNull(result);
        assertEquals(1, result.getRows().size());
        
        Object parsed = result.getRows().get(0).getValue(0);
        assertInstanceOf(LocalDateTime.class, parsed);
        
        LocalDateTime expected = LocalDateTime.of(2023, 12, 25, 14, 30, 45);
        assertEquals(expected, parsed);
    }
    
    // ===== SYSTEM FUNCTIONS TESTS =====
    
    @Test
    void testH2Version() throws Exception {
        SqlExecutionResult result = sqlEngine.execute("SELECT H2VERSION() as version");
        
        assertNotNull(result);
        assertEquals(1, result.getRows().size());
        
        Object version = result.getRows().get(0).getValue(0);
        assertInstanceOf(String.class, version);
        
        String versionStr = (String) version;
        assertTrue(versionStr.contains("MemGres"));
        assertTrue(versionStr.contains("H2 Compatible"));
    }
    
    @Test
    void testDatabasePath() throws Exception {
        SqlExecutionResult result = sqlEngine.execute("SELECT DATABASE_PATH() as path");
        
        assertNotNull(result);
        assertEquals(1, result.getRows().size());
        
        Object path = result.getRows().get(0).getValue(0);
        assertEquals("mem:memgres", path);
    }
    
    @Test
    void testMemoryFunctions() throws Exception {
        // Test MEMORY_USED
        SqlExecutionResult result = sqlEngine.execute("SELECT MEMORY_USED() as mem_used");
        
        assertNotNull(result);
        assertEquals(1, result.getRows().size());
        
        Object memUsed = result.getRows().get(0).getValue(0);
        assertInstanceOf(Long.class, memUsed);
        assertTrue((Long) memUsed > 0);
        
        // Test MEMORY_FREE
        result = sqlEngine.execute("SELECT MEMORY_FREE() as mem_free");
        
        Object memFree = result.getRows().get(0).getValue(0);
        assertInstanceOf(Long.class, memFree);
        assertTrue((Long) memFree > 0);
    }
    
    // ===== STRING UTILITY FUNCTIONS TESTS =====
    
    @Test
    void testLeftFunction() throws Exception {
        SqlExecutionResult result = sqlEngine.execute("SELECT LEFT('Hello World', 5) as result");
        
        assertNotNull(result);
        assertEquals(1, result.getRows().size());
        assertEquals("Hello", result.getRows().get(0).getValue(0));
        
        // Test with longer length than string
        result = sqlEngine.execute("SELECT LEFT('Hi', 10) as result");
        assertEquals("Hi", result.getRows().get(0).getValue(0));
        
        // Test with zero length
        result = sqlEngine.execute("SELECT LEFT('Hello', 0) as result");
        assertEquals("", result.getRows().get(0).getValue(0));
    }
    
    @Test
    void testRightFunction() throws Exception {
        SqlExecutionResult result = sqlEngine.execute("SELECT RIGHT('Hello World', 5) as result");
        
        assertNotNull(result);
        assertEquals(1, result.getRows().size());
        assertEquals("World", result.getRows().get(0).getValue(0));
        
        // Test with longer length than string
        result = sqlEngine.execute("SELECT RIGHT('Hi', 10) as result");
        assertEquals("Hi", result.getRows().get(0).getValue(0));
        
        // Test with zero length
        result = sqlEngine.execute("SELECT RIGHT('Hello', 0) as result");
        assertEquals("", result.getRows().get(0).getValue(0));
    }
    
    @Test
    void testPositionFunction() throws Exception {
        SqlExecutionResult result = sqlEngine.execute("SELECT POSITION('World', 'Hello World') as result");
        
        assertNotNull(result);
        assertEquals(1, result.getRows().size());
        assertEquals(7, result.getRows().get(0).getValue(0)); // 1-based indexing
        
        // Test substring not found
        result = sqlEngine.execute("SELECT POSITION('xyz', 'Hello World') as result");
        assertEquals(0, result.getRows().get(0).getValue(0));
        
        // Test empty substring
        result = sqlEngine.execute("SELECT POSITION('', 'Hello') as result");
        assertEquals(1, result.getRows().get(0).getValue(0));
    }
    
    @Test
    void testAsciiFunction() throws Exception {
        SqlExecutionResult result = sqlEngine.execute("SELECT ASCII('A') as result");
        
        assertNotNull(result);
        assertEquals(1, result.getRows().size());
        assertEquals(65, result.getRows().get(0).getValue(0));
        
        // Test with lowercase
        result = sqlEngine.execute("SELECT ASCII('a') as result");
        assertEquals(97, result.getRows().get(0).getValue(0));
        
        // Test with multi-character string (should return first character)
        result = sqlEngine.execute("SELECT ASCII('Hello') as result");
        assertEquals(72, result.getRows().get(0).getValue(0)); // 'H'
    }
    
    @Test
    void testCharFunction() throws Exception {
        SqlExecutionResult result = sqlEngine.execute("SELECT CHAR(65) as result");
        
        assertNotNull(result);
        assertEquals(1, result.getRows().size());
        assertEquals("A", result.getRows().get(0).getValue(0));
        
        // Test with lowercase
        result = sqlEngine.execute("SELECT CHAR(97) as result");
        assertEquals("a", result.getRows().get(0).getValue(0));
        
        // Test with space character
        result = sqlEngine.execute("SELECT CHAR(32) as result");
        assertEquals(" ", result.getRows().get(0).getValue(0));
    }
    
    @Test
    void testHexToRawFunction() throws Exception {
        SqlExecutionResult result = sqlEngine.execute("SELECT HEXTORAW('48656C6C6F') as result");
        
        assertNotNull(result);
        assertEquals(1, result.getRows().size());
        
        Object rawBytes = result.getRows().get(0).getValue(0);
        assertInstanceOf(byte[].class, rawBytes);
        
        byte[] bytes = (byte[]) rawBytes;
        String recovered = new String(bytes);
        assertEquals("Hello", recovered);
    }
    
    @Test
    void testRawToHexFunction() throws Exception {
        SqlExecutionResult result = sqlEngine.execute("SELECT RAWTOHEX('Hello') as result");
        
        assertNotNull(result);
        assertEquals(1, result.getRows().size());
        assertEquals("48656C6C6F", result.getRows().get(0).getValue(0));
        
        // Test with simple string
        result = sqlEngine.execute("SELECT RAWTOHEX('AB') as result");
        assertEquals("4142", result.getRows().get(0).getValue(0));
    }
    
    // ===== COMBINED FUNCTION TESTS =====
    
    @Test
    void testCombinedDateTimeFunctions() throws Exception {
        // Test complex date manipulation
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT FORMATDATETIME(" +
            "  DATEADD('MONTH', 6, CURRENT_DATE()), " +
            "  'yyyy-MM-dd'" +
            ") as future_date"
        );
        
        assertNotNull(result);
        assertEquals(1, result.getRows().size());
        
        Object futureDate = result.getRows().get(0).getValue(0);
        assertInstanceOf(String.class, futureDate);
        
        // Should be 6 months from now
        LocalDate expectedDate = LocalDate.now().plusMonths(6);
        String expectedStr = expectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        assertEquals(expectedStr, futureDate);
    }
    
    @Test
    void testCombinedStringFunctions() throws Exception {
        // Test combining string functions
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT LEFT(RIGHT('Hello World Test', 10), 5) as result"
        );
        
        assertNotNull(result);
        assertEquals(1, result.getRows().size());
        assertEquals("World", result.getRows().get(0).getValue(0));
    }
    
    @Test
    void testWithTableData() throws Exception {
        // Create table and insert test data
        sqlEngine.execute("CREATE TABLE test_dates (id INTEGER, name VARCHAR(50), created_date TIMESTAMP)");
        sqlEngine.execute("INSERT INTO test_dates VALUES (1, 'Alice', PARSEDATETIME('2023-01-15 10:30:00', 'yyyy-MM-dd HH:mm:ss'))");
        sqlEngine.execute("INSERT INTO test_dates VALUES (2, 'Bob', PARSEDATETIME('2023-02-20 14:45:30', 'yyyy-MM-dd HH:mm:ss'))");
        
        // Test H2 functions with table data
        SqlExecutionResult result = sqlEngine.execute(
            "SELECT id, " +
            "LEFT(name, 3) as short_name, " +
            "FORMATDATETIME(created_date, 'dd/MM/yyyy') as formatted_date, " +
            "DATEDIFF('DAY', created_date, CURRENT_TIMESTAMP()) as days_since " +
            "FROM test_dates ORDER BY id"
        );
        
        assertNotNull(result);
        assertEquals(2, result.getRows().size());
        
        Row firstRow = result.getRows().get(0);
        assertEquals(1, firstRow.getValue(0));
        assertEquals("Ali", firstRow.getValue(1));
        assertEquals("15/01/2023", firstRow.getValue(2));
        assertTrue((Long) firstRow.getValue(3) > 600); // More than 600 days since 2023-01-15
        
        Row secondRow = result.getRows().get(1);
        assertEquals(2, secondRow.getValue(0));
        assertEquals("Bob", secondRow.getValue(1));
        assertEquals("20/02/2023", secondRow.getValue(2));
        assertTrue((Long) secondRow.getValue(3) > 500); // More than 500 days since 2023-02-20
    }
    
    // ===== ERROR HANDLING TESTS =====
    
    @Test
    void testInvalidDateAddUnit() {
        assertThrows(Exception.class, () -> {
            sqlEngine.execute("SELECT DATEADD('INVALID_UNIT', 1, CURRENT_TIMESTAMP()) as result");
        });
    }
    
    @Test
    void testInvalidAsciiCode() {
        assertThrows(Exception.class, () -> {
            sqlEngine.execute("SELECT CHAR(200) as result"); // Invalid ASCII code
        });
    }
    
    @Test
    void testInvalidHexString() {
        assertThrows(Exception.class, () -> {
            sqlEngine.execute("SELECT HEXTORAW('XYZ') as result"); // Invalid hex
        });
    }
    
    @Test
    void testInvalidDatePattern() {
        assertThrows(Exception.class, () -> {
            sqlEngine.execute("SELECT FORMATDATETIME(CURRENT_TIMESTAMP(), 'invalid-pattern-xxx') as result");
        });
    }
}