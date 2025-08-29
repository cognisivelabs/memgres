package com.memgres.types;

import com.memgres.testing.MemGres;
import com.memgres.testing.MemGresExtension;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionResult;
import com.memgres.sql.execution.SqlExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for LOB (Large Object) data types.
 * Tests CLOB, BLOB, BINARY, and VARBINARY types with H2 compatibility.
 */
@ExtendWith(MemGresExtension.class)
public class LobTypesTest {
    
    @Test
    @MemGres
    void testClobDataTypeCreation(SqlExecutionEngine sql) throws SQLException, SqlExecutionException {
        // Test CLOB column creation
        sql.execute("CREATE TABLE clob_test (id INTEGER, content CLOB)");
        
        // Insert CLOB data
        sql.execute("INSERT INTO clob_test VALUES (1, 'This is a character large object')");
        
        var result = sql.execute("SELECT id, content FROM clob_test WHERE id = 1");
        assertEquals(1, result.getRows().size());
        
        Row row = result.getRows().get(0);
        assertEquals(1, row.getValue(0));
        assertEquals("This is a character large object", row.getValue(1));
    }
    
    @Test
    @MemGres
    void testCharacterLargeObjectSyntax(SqlExecutionEngine sql) throws SQLException, SqlExecutionException {
        // Test CHARACTER LARGE OBJECT syntax
        sql.execute("CREATE TABLE char_lob_test (id INTEGER, content CHARACTER LARGE OBJECT)");
        
        sql.execute("INSERT INTO char_lob_test VALUES (1, 'Using CHARACTER LARGE OBJECT syntax')");
        
        var result = sql.execute("SELECT content FROM char_lob_test WHERE id = 1");
        assertEquals("Using CHARACTER LARGE OBJECT syntax", result.getRows().get(0).getValue(0));
    }
    
    @Test
    @MemGres
    void testBlobDataTypeCreation(SqlExecutionEngine sql) throws SQLException, SqlExecutionException {
        // Test BLOB column creation
        sql.execute("CREATE TABLE blob_test (id INTEGER, data BLOB)");
        
        // Insert binary data as hex string
        sql.execute("INSERT INTO blob_test VALUES (1, '\\x48656C6C6F20576F726C64')"); // "Hello World" in hex
        
        var result = sql.execute("SELECT id, data FROM blob_test WHERE id = 1");
        assertEquals(1, result.getRows().size());
        
        Row row = result.getRows().get(0);
        assertEquals(1, row.getValue(0));
        assertArrayEquals("Hello World".getBytes(), (byte[]) row.getValue(1));
    }
    
    @Test
    @MemGres
    void testBinaryLargeObjectSyntax(SqlExecutionEngine sql) throws SQLException, SqlExecutionException {
        // Test BINARY LARGE OBJECT syntax
        sql.execute("CREATE TABLE binary_lob_test (id INTEGER, data BINARY LARGE OBJECT)");
        
        sql.execute("INSERT INTO binary_lob_test VALUES (1, '\\x44617461')"); // "Data" in hex
        
        var result = sql.execute("SELECT data FROM binary_lob_test WHERE id = 1");
        assertArrayEquals("Data".getBytes(), (byte[]) result.getRows().get(0).getValue(0));
    }
    
    @Test
    @MemGres
    void testBinaryDataType(SqlExecutionEngine sql) throws SQLException, SqlExecutionException {
        // Test BINARY data type
        sql.execute("CREATE TABLE binary_test (id INTEGER, data BINARY(10))");
        
        sql.execute("INSERT INTO binary_test VALUES (1, '\\x48656C6C6F')"); // "Hello" in hex
        
        var result = sql.execute("SELECT data FROM binary_test WHERE id = 1");
        assertArrayEquals("Hello".getBytes(), (byte[]) result.getRows().get(0).getValue(0));
    }
    
    @Test
    @MemGres
    void testVarbinaryDataType(SqlExecutionEngine sql) throws SQLException, SqlExecutionException {
        // Test VARBINARY data type
        sql.execute("CREATE TABLE varbinary_test (id INTEGER, data VARBINARY(100))");
        
        sql.execute("INSERT INTO varbinary_test VALUES (1, '\\x5661726961626c65')"); // "Variable" in hex
        
        var result = sql.execute("SELECT data FROM varbinary_test WHERE id = 1");
        assertArrayEquals("Variable".getBytes(), (byte[]) result.getRows().get(0).getValue(0));
    }
    
    @Test
    @MemGres
    void testBinaryVaryingSyntax(SqlExecutionEngine sql) throws SQLException, SqlExecutionException {
        // Test BINARY VARYING syntax (alternative to VARBINARY)
        sql.execute("CREATE TABLE binary_varying_test (id INTEGER, data BINARY VARYING(50))");
        
        sql.execute("INSERT INTO binary_varying_test VALUES (1, '\\x54657374')"); // "Test" in hex
        
        var result = sql.execute("SELECT data FROM binary_varying_test WHERE id = 1");
        assertArrayEquals("Test".getBytes(), (byte[]) result.getRows().get(0).getValue(0));
    }
    
    @Test
    void testClobJdbcInterface() throws SQLException, SqlExecutionException {
        // Test MemGresClob JDBC interface
        MemGresClob clob = new MemGresClob("Hello World");
        
        assertEquals(11, clob.length());
        assertEquals("Hello", clob.getSubString(1, 5));
        assertEquals("World", clob.getSubString(7, 5));
        
        // Test position search
        assertEquals(7, clob.position("World", 1));
        assertEquals(-1, clob.position("NotFound", 1));
        
        // Test character stream
        try (var reader = clob.getCharacterStream()) {
            char[] buffer = new char[11];
            int read = reader.read(buffer);
            assertEquals(11, read);
            assertEquals("Hello World", new String(buffer, 0, read));
        } catch (IOException e) {
            fail("IOException reading CLOB: " + e.getMessage());
        }
    }
    
    @Test
    void testBlobJdbcInterface() throws SQLException, SqlExecutionException {
        // Test MemGresBlob JDBC interface
        byte[] testData = "Binary Data".getBytes();
        MemGresBlob blob = new MemGresBlob(testData);
        
        assertEquals(testData.length, blob.length());
        assertArrayEquals("Binary".getBytes(), blob.getBytes(1, 6));
        assertArrayEquals("Data".getBytes(), blob.getBytes(8, 4));
        
        // Test position search
        assertEquals(8, blob.position("Data".getBytes(), 1));
        assertEquals(-1, blob.position("NotFound".getBytes(), 1));
        
        // Test binary stream
        try (var stream = blob.getBinaryStream()) {
            byte[] buffer = new byte[testData.length];
            int read = stream.read(buffer);
            assertEquals(testData.length, read);
            assertArrayEquals(testData, buffer);
        } catch (IOException e) {
            fail("IOException reading BLOB: " + e.getMessage());
        }
    }
    
    @Test
    void testClobModification() throws SQLException, SqlExecutionException {
        MemGresClob clob = new MemGresClob("Original Text");
        
        // Test string setting
        clob.setString(1, "Modified");
        assertEquals("Modified", clob.getSubString(1, 8));
        
        // Test partial string setting
        clob.setString(1, "New Text", 0, 3);
        assertEquals("New", clob.getSubString(1, 3));
        
        // Test truncation
        clob.truncate(3);
        assertEquals(3, clob.length());
        assertEquals("New", clob.toString());
    }
    
    @Test
    void testBlobModification() throws SQLException, SqlExecutionException {
        byte[] original = "Original Data".getBytes();
        MemGresBlob blob = new MemGresBlob(original);
        
        // Test byte setting
        byte[] modified = "Modified".getBytes();
        blob.setBytes(1, modified);
        assertArrayEquals(modified, blob.getBytes(1, modified.length));
        
        // Test partial byte setting
        byte[] partial = "New Data".getBytes();
        blob.setBytes(1, partial, 0, 3);
        assertArrayEquals("New".getBytes(), blob.getBytes(1, 3));
        
        // Test truncation
        blob.truncate(3);
        assertEquals(3, blob.length());
        assertArrayEquals("New".getBytes(), blob.toByteArray());
    }
    
    @Test
    @MemGres
    void testLargeContent(SqlExecutionEngine sql) throws SQLException, SqlExecutionException {
        // Test with larger content (simulate LOB behavior)
        StringBuilder largeText = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeText.append("This is line ").append(i).append(" of large content.\\n");
        }
        
        sql.execute("CREATE TABLE large_clob_test (id INTEGER, content CLOB)");
        sql.execute("INSERT INTO large_clob_test VALUES (1, '" + largeText.toString() + "')");
        
        var result = sql.execute("SELECT content FROM large_clob_test WHERE id = 1");
        String content = (String) result.getRows().get(0).getValue(0);
        assertTrue(content.length() > 30000);
    }
    
    @Test
    @MemGres
    void testNullLobValues(SqlExecutionEngine sql) throws SQLException, SqlExecutionException {
        sql.execute("CREATE TABLE null_lob_test (id INTEGER, clob_data CLOB, blob_data BLOB, binary_data BINARY(10), varbinary_data VARBINARY(50))");
        
        // Insert null LOB values
        sql.execute("INSERT INTO null_lob_test VALUES (1, NULL, NULL, NULL, NULL)");
        
        var result = sql.execute("SELECT * FROM null_lob_test WHERE id = 1");
        Row row = result.getRows().get(0);
        
        assertEquals(1, row.getValue(0)); // id should not be null
        assertNull(row.getValue(1)); // clob_data should be null
        assertNull(row.getValue(2)); // blob_data should be null
        assertNull(row.getValue(3)); // binary_data should be null
        assertNull(row.getValue(4)); // varbinary_data should be null
    }
    
    @Test
    void testDataTypeValidation() {
        // Test DataType enum validation for LOB types
        assertTrue(DataType.CLOB.isValidValue("String content"));
        assertTrue(DataType.CLOB.isValidValue(new MemGresClob("Test")));
        assertFalse(DataType.CLOB.isValidValue(123));
        
        assertTrue(DataType.BLOB.isValidValue("Binary content".getBytes()));
        assertTrue(DataType.BLOB.isValidValue(new MemGresBlob("Test".getBytes())));
        assertFalse(DataType.BLOB.isValidValue("String not bytes"));
        
        assertTrue(DataType.BINARY.isValidValue("Binary".getBytes()));
        assertFalse(DataType.BINARY.isValidValue("String"));
        
        assertTrue(DataType.VARBINARY.isValidValue("VarBinary".getBytes()));
        assertFalse(DataType.VARBINARY.isValidValue(123));
    }
    
    @Test
    void testDataTypeConversion() {
        // Test DataType conversion for LOB types
        String testString = "Conversion Test";
        assertEquals(testString, DataType.CLOB.convertValue(testString));
        
        byte[] testBytes = testString.getBytes();
        assertArrayEquals(testBytes, (byte[]) DataType.BLOB.convertValue(testBytes));
        
        // Test hex string conversion for binary types
        byte[] fromHex = (byte[]) DataType.BINARY.convertValue("\\x48656c6c6f");
        assertArrayEquals("Hello".getBytes(), fromHex);
        
        byte[] fromHexVarBinary = (byte[]) DataType.VARBINARY.convertValue("\\x576f726c64");
        assertArrayEquals("World".getBytes(), fromHexVarBinary);
    }
    
    @Test
    void testFromSqlNameLobTypes() {
        // Test DataType.fromSqlName for all LOB type variations
        assertEquals(DataType.CLOB, DataType.fromSqlName("CLOB"));
        assertEquals(DataType.CLOB, DataType.fromSqlName("clob"));
        assertEquals(DataType.CLOB, DataType.fromSqlName("character large object"));
        assertEquals(DataType.CLOB, DataType.fromSqlName("char large object"));
        
        assertEquals(DataType.BLOB, DataType.fromSqlName("BLOB"));
        assertEquals(DataType.BLOB, DataType.fromSqlName("blob"));
        assertEquals(DataType.BLOB, DataType.fromSqlName("binary large object"));
        
        assertEquals(DataType.BINARY, DataType.fromSqlName("BINARY"));
        assertEquals(DataType.BINARY, DataType.fromSqlName("binary"));
        
        assertEquals(DataType.VARBINARY, DataType.fromSqlName("VARBINARY"));
        assertEquals(DataType.VARBINARY, DataType.fromSqlName("varbinary"));
        assertEquals(DataType.VARBINARY, DataType.fromSqlName("binary varying"));
    }
}