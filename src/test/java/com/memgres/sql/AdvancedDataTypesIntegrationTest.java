package com.memgres.sql;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionResult;
import com.memgres.types.Interval;
import com.memgres.types.Row;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test class for advanced H2 data types in SQL operations
 */
class AdvancedDataTypesIntegrationTest {
    
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
    
    @Test
    void testClobInSqlOperations() throws Exception {
        // Create table with CLOB column
        sqlEngine.execute("CREATE TABLE documents (id INTEGER, content CLOB, title VARCHAR(100))");
        
        // Insert data with CLOB values
        sqlEngine.execute("INSERT INTO documents VALUES (1, 'This is a very long document content that would be stored as a CLOB in H2 database', 'Document 1')");
        sqlEngine.execute("INSERT INTO documents VALUES (2, 'Another large text content for testing CLOB data type functionality', 'Document 2')");
        
        // Query CLOB data
        SqlExecutionResult result = sqlEngine.execute("SELECT id, content, title FROM documents WHERE id = 1");
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        
        List<Row> rows = result.getRows();
        assertEquals(1, rows.size());
        
        Object[] rowData = rows.get(0).getData();
        assertEquals(1, ((Number) rowData[0]).intValue());
        assertEquals("This is a very long document content that would be stored as a CLOB in H2 database", (String) rowData[1]);
        assertEquals("Document 1", (String) rowData[2]);
        
        // Test CLOB in WHERE clause with exact match
        SqlExecutionResult searchResult = sqlEngine.execute("SELECT id FROM documents WHERE content = 'This is a very long document content that would be stored as a CLOB in H2 database'");
        assertEquals(1, searchResult.getRows().size());
        assertEquals(1, ((Number) searchResult.getRows().get(0).getData()[0]).intValue());
    }
    
    @Test
    void testBinaryInSqlOperations() throws Exception {
        // Create table with BINARY column
        sqlEngine.execute("CREATE TABLE binary_data (id INTEGER, data BINARY, metadata VARCHAR(50))");
        
        // Insert binary data using hex notation
        sqlEngine.execute("INSERT INTO binary_data VALUES (1, '\\x01020304', 'Test binary data')");
        sqlEngine.execute("INSERT INTO binary_data VALUES (2, '0xABCDEF', 'More binary data')");
        
        // Query binary data
        SqlExecutionResult result = sqlEngine.execute("SELECT id, data, metadata FROM binary_data WHERE id = 1");
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        
        List<Row> rows = result.getRows();
        assertEquals(1, rows.size());
        
        Object[] rowData = rows.get(0).getData();
        assertEquals(1, ((Number) rowData[0]).intValue());
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04}, (byte[]) rowData[1]);
        assertEquals("Test binary data", (String) rowData[2]);
        
        // Test second row with different hex format
        SqlExecutionResult result2 = sqlEngine.execute("SELECT data FROM binary_data WHERE id = 2");
        byte[] data2 = (byte[]) result2.getRows().get(0).getData()[0];
        assertArrayEquals(new byte[]{(byte) 0xAB, (byte) 0xCD, (byte) 0xEF}, data2);
    }
    
    @Test
    void testVarbinaryInSqlOperations() throws Exception {
        // Create table with VARBINARY column
        sqlEngine.execute("CREATE TABLE variable_binary (id INTEGER, payload VARBINARY, description TEXT)");
        
        // Insert variable length binary data
        sqlEngine.execute("INSERT INTO variable_binary VALUES (1, '\\x010203', 'Short binary')");
        sqlEngine.execute("INSERT INTO variable_binary VALUES (2, '0x0123456789ABCDEF', 'Longer binary')");
        sqlEngine.execute("INSERT INTO variable_binary VALUES (3, 'Hello World', 'Text as binary')");
        
        // Query all data
        SqlExecutionResult result = sqlEngine.execute("SELECT id, payload, description FROM variable_binary ORDER BY id");
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        
        List<Row> rows = result.getRows();
        assertEquals(3, rows.size());
        
        // Test first row (hex format)
        Object[] row1 = rows.get(0).getData();
        assertEquals(1, ((Number) row1[0]).intValue());
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03}, (byte[]) row1[1]);
        assertEquals("Short binary", (String) row1[2]);
        
        // Test second row (longer hex)
        Object[] row2 = rows.get(1).getData();
        assertEquals(2, ((Number) row2[0]).intValue());
        assertArrayEquals(new byte[]{0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF}, (byte[]) row2[1]);
        
        // Test third row (text as binary)
        Object[] row3 = rows.get(2).getData();
        assertEquals(3, ((Number) row3[0]).intValue());
        assertArrayEquals("Hello World".getBytes(), (byte[]) row3[1]);
        assertEquals("Text as binary", (String) row3[2]);
    }
    
    @Test
    void testIntervalInSqlOperations() throws Exception {
        // Create table with INTERVAL column
        sqlEngine.execute("CREATE TABLE schedules (id INTEGER, task_name VARCHAR(100), duration INTERVAL)");
        
        // Insert data with different interval formats
        sqlEngine.execute("INSERT INTO schedules VALUES (1, 'Project Phase 1', '6 MONTHS')");
        sqlEngine.execute("INSERT INTO schedules VALUES (2, 'Daily Meeting', '1 HOUR')");
        sqlEngine.execute("INSERT INTO schedules VALUES (3, 'Sprint Duration', '2 WEEKS')");
        sqlEngine.execute("INSERT INTO schedules VALUES (4, 'Code Review', '30 MINUTES')");
        
        // Query interval data
        SqlExecutionResult result = sqlEngine.execute("SELECT id, task_name, duration FROM schedules WHERE id = 1");
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        
        List<Row> rows = result.getRows();
        assertEquals(1, rows.size());
        
        Object[] rowData = rows.get(0).getData();
        assertEquals(1, ((Number) rowData[0]).intValue());
        assertEquals("Project Phase 1", (String) rowData[1]);
        
        Interval duration = (Interval) rowData[2];
        assertEquals(Interval.IntervalType.YEAR_MONTH, duration.getType());
        assertEquals(0, duration.getYears());
        assertEquals(6, duration.getMonths());
        
        // Test hour interval
        SqlExecutionResult hourResult = sqlEngine.execute("SELECT duration FROM schedules WHERE id = 2");
        Interval hourDuration = (Interval) hourResult.getRows().get(0).getData()[0];
        assertEquals(Interval.IntervalType.DAY_TIME, hourDuration.getType());
        assertEquals(1, hourDuration.getHours());
        
        // Test minute interval
        SqlExecutionResult minuteResult = sqlEngine.execute("SELECT duration FROM schedules WHERE id = 4");
        Interval minuteDuration = (Interval) minuteResult.getRows().get(0).getData()[0];
        assertEquals(Interval.IntervalType.DAY_TIME, minuteDuration.getType());
        assertEquals(30, minuteDuration.getMinutes());
    }
    
    @Test
    void testAdvancedDataTypesWithAliases() throws Exception {
        // Test data type aliases work in CREATE TABLE statements
        
        // Test CLOB aliases
        sqlEngine.execute("CREATE TABLE clob_test1 (id INTEGER, content CLOB)");
        sqlEngine.execute("CREATE TABLE clob_test2 (id INTEGER, content CHARACTER LARGE OBJECT)");
        // Note: CHAR LARGE OBJECT would need to be tested if grammar supports it
        
        // Test VARBINARY alias
        sqlEngine.execute("CREATE TABLE varbinary_test (id INTEGER, data VARBINARY)");
        // Note: BINARY VARYING would need grammar support
        
        // Insert and verify data works the same way
        sqlEngine.execute("INSERT INTO clob_test1 VALUES (1, 'Test CLOB content')");
        sqlEngine.execute("INSERT INTO clob_test2 VALUES (1, 'Test CHARACTER LARGE OBJECT content')");
        sqlEngine.execute("INSERT INTO varbinary_test VALUES (1, '\\x123456')");
        
        // Verify data retrieval
        SqlExecutionResult clobResult1 = sqlEngine.execute("SELECT content FROM clob_test1");
        assertEquals("Test CLOB content", (String) clobResult1.getRows().get(0).getData()[0]);
        
        SqlExecutionResult clobResult2 = sqlEngine.execute("SELECT content FROM clob_test2");
        assertEquals("Test CHARACTER LARGE OBJECT content", (String) clobResult2.getRows().get(0).getData()[0]);
        
        SqlExecutionResult varbinaryResult = sqlEngine.execute("SELECT data FROM varbinary_test");
        assertArrayEquals(new byte[]{0x12, 0x34, 0x56}, (byte[]) varbinaryResult.getRows().get(0).getData()[0]);
    }
    
    @Test
    void testMixedAdvancedDataTypes() throws Exception {
        // Create table with all advanced data types
        sqlEngine.execute("CREATE TABLE mixed_types (id INTEGER, text_data CLOB, binary_data BINARY, var_binary VARBINARY, time_span INTERVAL)");
        
        // Insert mixed data
        sqlEngine.execute("INSERT INTO mixed_types VALUES (1, 'Large text content for CLOB storage', '\\x010203', '0xABCDEF', '2 DAYS')");
        
        // Query and verify all types
        SqlExecutionResult result = sqlEngine.execute("SELECT * FROM mixed_types WHERE id = 1");
        List<Row> rows = result.getRows();
        assertEquals(1, rows.size());
        
        Object[] rowData = rows.get(0).getData();
        assertEquals(1, ((Number) rowData[0]).intValue());
        assertEquals("Large text content for CLOB storage", (String) rowData[1]);
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03}, (byte[]) rowData[2]);
        assertArrayEquals(new byte[]{(byte) 0xAB, (byte) 0xCD, (byte) 0xEF}, (byte[]) rowData[3]);
        
        Interval interval = (Interval) rowData[4];
        assertEquals(2, interval.getDays());
        assertEquals(0, interval.getHours());
    }
    
    @Test
    void testAdvancedDataTypesWithNullValues() throws Exception {
        // Test NULL handling for advanced data types - simplified
        sqlEngine.execute("CREATE TABLE nullable_clob (id INTEGER, content CLOB)");
        
        // Insert NULL CLOB value using explicit NULL syntax
        sqlEngine.execute("INSERT INTO nullable_clob (id, content) VALUES (1, NULL)");
        sqlEngine.execute("INSERT INTO nullable_clob (id, content) VALUES (2, 'Not null content')");
        
        // Query NULL row
        SqlExecutionResult nullResult = sqlEngine.execute("SELECT id, content FROM nullable_clob WHERE id = 1");
        Object[] nullRow = nullResult.getRows().get(0).getData();
        assertEquals(1, ((Number) nullRow[0]).intValue());
        assertNull(nullRow[1]); // CLOB should be NULL
        
        // Query non-NULL row
        SqlExecutionResult nonNullResult = sqlEngine.execute("SELECT id, content FROM nullable_clob WHERE id = 2");
        Object[] nonNullRow = nonNullResult.getRows().get(0).getData();
        assertEquals(2, ((Number) nonNullRow[0]).intValue());
        assertEquals("Not null content", (String) nonNullRow[1]);
    }
}