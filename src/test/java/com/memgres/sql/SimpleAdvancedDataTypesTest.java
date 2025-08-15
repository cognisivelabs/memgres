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
 * Simple test for advanced data types without complex operations
 */
class SimpleAdvancedDataTypesTest {
    
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
    void testBasicClobOperations() throws Exception {
        // Create table with CLOB
        sqlEngine.execute("CREATE TABLE test_clob (id INTEGER, content CLOB)");
        
        // Insert simple CLOB data
        sqlEngine.execute("INSERT INTO test_clob VALUES (1, 'Hello CLOB world')");
        
        // Query back
        SqlExecutionResult result = sqlEngine.execute("SELECT id, content FROM test_clob WHERE id = 1");
        
        List<Row> rows = result.getRows();
        assertEquals(1, rows.size());
        
        Object[] rowData = rows.get(0).getData();
        assertEquals(1, ((Number) rowData[0]).intValue());
        assertEquals("Hello CLOB world", (String) rowData[1]);
    }
    
    @Test
    void testBasicBinaryOperations() throws Exception {
        // Create table with BINARY
        sqlEngine.execute("CREATE TABLE test_binary (id INTEGER, data BINARY)");
        
        // Insert simple binary data
        sqlEngine.execute("INSERT INTO test_binary VALUES (1, '\\x010203')");
        
        // Query back
        SqlExecutionResult result = sqlEngine.execute("SELECT id, data FROM test_binary WHERE id = 1");
        
        List<Row> rows = result.getRows();
        assertEquals(1, rows.size());
        
        Object[] rowData = rows.get(0).getData();
        assertEquals(1, ((Number) rowData[0]).intValue());
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03}, (byte[]) rowData[1]);
    }
    
    @Test
    void testBasicVarbinaryOperations() throws Exception {
        // Create table with VARBINARY
        sqlEngine.execute("CREATE TABLE test_varbinary (id INTEGER, data VARBINARY)");
        
        // Insert simple varbinary data
        sqlEngine.execute("INSERT INTO test_varbinary VALUES (1, '0xABCD')");
        
        // Query back
        SqlExecutionResult result = sqlEngine.execute("SELECT id, data FROM test_varbinary WHERE id = 1");
        
        List<Row> rows = result.getRows();
        assertEquals(1, rows.size());
        
        Object[] rowData = rows.get(0).getData();
        assertEquals(1, ((Number) rowData[0]).intValue());
        assertArrayEquals(new byte[]{(byte) 0xAB, (byte) 0xCD}, (byte[]) rowData[1]);
    }
    
    @Test
    void testBasicIntervalOperations() throws Exception {
        // Create table with INTERVAL
        sqlEngine.execute("CREATE TABLE test_interval (id INTEGER, duration INTERVAL)");
        
        // Insert simple interval data
        sqlEngine.execute("INSERT INTO test_interval VALUES (1, '5 DAYS')");
        
        // Query back
        SqlExecutionResult result = sqlEngine.execute("SELECT id, duration FROM test_interval WHERE id = 1");
        
        List<Row> rows = result.getRows();
        assertEquals(1, rows.size());
        
        Object[] rowData = rows.get(0).getData();
        assertEquals(1, ((Number) rowData[0]).intValue());
        
        Interval interval = (Interval) rowData[1];
        assertEquals(5, interval.getDays());
        assertEquals(0, interval.getHours());
    }
}