package com.memgres.types;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for advanced H2 data types: CLOB, BINARY, VARBINARY, INTERVAL
 */
class AdvancedDataTypesTest {
    
    @Test
    void testClobDataType() {
        // Test basic string values
        assertTrue(DataType.CLOB.isValidValue("Hello World"));
        assertTrue(DataType.CLOB.isValidValue(""));
        assertTrue(DataType.CLOB.isValidValue("This is a very long text that would be stored as a CLOB"));
        
        // Test conversion
        assertEquals("Hello", DataType.CLOB.convertValue("Hello"));
        assertEquals("123", DataType.CLOB.convertValue(123));
        assertNull(DataType.CLOB.convertValue(null));
        
        // Test SQL name and aliases
        assertEquals(DataType.CLOB, DataType.fromSqlName("clob"));
        assertEquals(DataType.CLOB, DataType.fromSqlName("CLOB"));
        assertEquals(DataType.CLOB, DataType.fromSqlName("character large object"));
        assertEquals(DataType.CLOB, DataType.fromSqlName("char large object"));
    }
    
    @Test
    void testBinaryDataType() {
        // Test binary data validation
        byte[] testBytes = {0x01, 0x02, 0x03, 0x04};
        assertTrue(DataType.BINARY.isValidValue(testBytes));
        assertFalse(DataType.BINARY.isValidValue("not bytes"));
        
        // Test string conversion
        assertArrayEquals("Hello".getBytes(), (byte[]) DataType.BINARY.convertValue("Hello"));
        
        // Test hex string conversion
        byte[] expectedHex = {0x01, 0x02, 0x03, 0x04};
        assertArrayEquals(expectedHex, (byte[]) DataType.BINARY.convertValue("\\x01020304"));
        assertArrayEquals(expectedHex, (byte[]) DataType.BINARY.convertValue("0x01020304"));
        
        // Test SQL name
        assertEquals(DataType.BINARY, DataType.fromSqlName("binary"));
        assertEquals(DataType.BINARY, DataType.fromSqlName("BINARY"));
    }
    
    @Test
    void testVarbinaryDataType() {
        // Test binary data validation  
        byte[] testBytes = {0x01, 0x02, 0x03, 0x04};
        assertTrue(DataType.VARBINARY.isValidValue(testBytes));
        assertFalse(DataType.VARBINARY.isValidValue("not bytes"));
        
        // Test string conversion
        assertArrayEquals("Hello".getBytes(), (byte[]) DataType.VARBINARY.convertValue("Hello"));
        
        // Test hex string conversion
        byte[] expectedHex = {(byte) 0xAB, (byte) 0xCD, (byte) 0xEF};
        assertArrayEquals(expectedHex, (byte[]) DataType.VARBINARY.convertValue("\\xABCDEF"));
        assertArrayEquals(expectedHex, (byte[]) DataType.VARBINARY.convertValue("0xABCDEF"));
        
        // Test SQL name and aliases
        assertEquals(DataType.VARBINARY, DataType.fromSqlName("varbinary"));
        assertEquals(DataType.VARBINARY, DataType.fromSqlName("VARBINARY"));
        assertEquals(DataType.VARBINARY, DataType.fromSqlName("binary varying"));
    }
    
    @Test
    void testIntervalDataType() {
        // Test interval validation
        assertTrue(DataType.INTERVAL.isValidValue(Interval.ofYearMonth(1, 6)));
        assertTrue(DataType.INTERVAL.isValidValue(Interval.ofDayTime(5, 12, 30, 45)));
        assertTrue(DataType.INTERVAL.isValidValue("1 YEAR"));
        assertFalse(DataType.INTERVAL.isValidValue(123));
        
        // Test interval creation and parsing
        Interval yearMonth = Interval.ofYearMonth(2, 3);
        assertEquals(Interval.IntervalType.YEAR_MONTH, yearMonth.getType());
        assertEquals(2, yearMonth.getYears());
        assertEquals(3, yearMonth.getMonths());
        
        Interval dayTime = Interval.ofDayTime(1, 2, 30, 45);
        assertEquals(Interval.IntervalType.DAY_TIME, dayTime.getType());
        assertEquals(1, dayTime.getDays());
        assertEquals(2, dayTime.getHours());
        assertEquals(30, dayTime.getMinutes());
        assertEquals(45, dayTime.getSeconds());
        
        // Test string conversion
        Interval converted = (Interval) DataType.INTERVAL.convertValue("5 DAYS");
        assertEquals(5, converted.getDays());
        assertEquals(0, converted.getHours());
        
        // Test SQL name and aliases
        assertEquals(DataType.INTERVAL, DataType.fromSqlName("interval"));
        assertEquals(DataType.INTERVAL, DataType.fromSqlName("INTERVAL"));
        assertEquals(DataType.INTERVAL, DataType.fromSqlName("interval year"));
        assertEquals(DataType.INTERVAL, DataType.fromSqlName("interval day to second"));
    }
    
    @Test
    void testIntervalParsing() {
        // Test various interval format parsing
        
        // Single unit intervals
        Interval years = Interval.parse("3 YEARS");
        assertEquals(3, years.getYears());
        assertEquals(0, years.getMonths());
        
        Interval months = Interval.parse("6 MONTHS");
        assertEquals(0, months.getYears());
        assertEquals(6, months.getMonths());
        
        Interval days = Interval.parse("10 DAYS");
        assertEquals(10, days.getDays());
        assertEquals(0, days.getHours());
        
        Interval hours = Interval.parse("8 HOURS");
        assertEquals(0, hours.getDays());
        assertEquals(8, hours.getHours());
        
        Interval minutes = Interval.parse("45 MINUTES");
        assertEquals(0, minutes.getHours());
        assertEquals(45, minutes.getMinutes());
        
        Interval seconds = Interval.parse("30 SECONDS");
        assertEquals(0, seconds.getMinutes());
        assertEquals(30, seconds.getSeconds());
        
        // Year-month format
        Interval yearMonth = Interval.parse("2-6");
        assertEquals(2, yearMonth.getYears());
        assertEquals(6, yearMonth.getMonths());
        
        // Day-time format
        Interval dayTime = Interval.parse("5 12:30:45");
        assertEquals(5, dayTime.getDays());
        assertEquals(12, dayTime.getHours());
        assertEquals(30, dayTime.getMinutes());
        assertEquals(45, dayTime.getSeconds());
    }
    
    @Test
    void testIntervalToString() {
        // Test string representation
        assertEquals("2 YEARS", Interval.ofYearMonth(2, 0).toString());
        assertEquals("1 YEAR", Interval.ofYearMonth(1, 0).toString());
        assertEquals("3 MONTHS", Interval.ofYearMonth(0, 3).toString());
        assertEquals("1 MONTH", Interval.ofYearMonth(0, 1).toString());
        assertEquals("2-6", Interval.ofYearMonth(2, 6).toString());
        
        assertEquals("5 DAYS", Interval.ofDayTime(5, 0, 0, 0).toString());
        assertEquals("1 DAY", Interval.ofDayTime(1, 0, 0, 0).toString());
        assertEquals("3 HOURS", Interval.ofDayTime(0, 3, 0, 0).toString());
        assertEquals("1 HOUR", Interval.ofDayTime(0, 1, 0, 0).toString());
        assertEquals("30 MINUTES", Interval.ofDayTime(0, 0, 30, 0).toString());
        assertEquals("1 MINUTE", Interval.ofDayTime(0, 0, 1, 0).toString());
        assertEquals("45 SECONDS", Interval.ofDayTime(0, 0, 0, 45).toString());
        assertEquals("1 SECOND", Interval.ofDayTime(0, 0, 0, 1).toString());
        assertEquals("2 05:30:15", Interval.ofDayTime(2, 5, 30, 15).toString());
    }
    
    @Test
    void testIntervalEquality() {
        // Test equality
        Interval interval1 = Interval.ofYearMonth(1, 6);
        Interval interval2 = Interval.ofYearMonth(1, 6);
        Interval interval3 = Interval.ofYearMonth(2, 3);
        
        assertEquals(interval1, interval2);
        assertNotEquals(interval1, interval3);
        assertEquals(interval1.hashCode(), interval2.hashCode());
        
        Interval dayTime1 = Interval.ofDayTime(1, 2, 30, 45);
        Interval dayTime2 = Interval.ofDayTime(1, 2, 30, 45);
        Interval dayTime3 = Interval.ofDayTime(2, 1, 15, 30);
        
        assertEquals(dayTime1, dayTime2);
        assertNotEquals(dayTime1, dayTime3);
        assertEquals(dayTime1.hashCode(), dayTime2.hashCode());
    }
    
    @Test
    void testInvalidIntervalFormats() {
        // Test invalid interval strings
        assertThrows(IllegalArgumentException.class, () -> Interval.parse(""));
        assertThrows(IllegalArgumentException.class, () -> Interval.parse("invalid"));
        assertThrows(IllegalArgumentException.class, () -> Interval.parse("123"));
        assertThrows(IllegalArgumentException.class, () -> Interval.parse("1-"));
        assertThrows(IllegalArgumentException.class, () -> Interval.parse("-5"));
    }
    
    @Test
    void testHexStringConversion() {
        // Test hex string to byte array conversion
        DataType binaryType = DataType.BINARY;
        
        // Test even length hex strings
        byte[] result1 = (byte[]) binaryType.convertValue("\\x01020304");
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04}, result1);
        
        byte[] result2 = (byte[]) binaryType.convertValue("0xABCDEF");
        assertArrayEquals(new byte[]{(byte)0xAB, (byte)0xCD, (byte)0xEF}, result2);
        
        // Test case insensitive
        byte[] result3 = (byte[]) binaryType.convertValue("0xabcdef");
        assertArrayEquals(new byte[]{(byte)0xAB, (byte)0xCD, (byte)0xEF}, result3);
        
        // Test invalid hex should throw exception through the conversion
        assertThrows(IllegalArgumentException.class, () -> {
            binaryType.convertValue("\\x0102030"); // Odd length
        });
    }
    
    @Test
    void testDataTypeCompatibility() {
        // Test that new data types integrate properly with existing system
        
        // Test enum values
        assertTrue(DataType.valueOf("CLOB") == DataType.CLOB);
        assertTrue(DataType.valueOf("BINARY") == DataType.BINARY);
        assertTrue(DataType.valueOf("VARBINARY") == DataType.VARBINARY);
        assertTrue(DataType.valueOf("INTERVAL") == DataType.INTERVAL);
        
        // Test SQL names
        assertEquals("clob", DataType.CLOB.getSqlName());
        assertEquals("binary", DataType.BINARY.getSqlName());
        assertEquals("varbinary", DataType.VARBINARY.getSqlName());
        assertEquals("interval", DataType.INTERVAL.getSqlName());
        
        // Test that these are not array types
        assertFalse(DataType.CLOB.isArrayType());
        assertFalse(DataType.BINARY.isArrayType());
        assertFalse(DataType.VARBINARY.isArrayType());
        assertFalse(DataType.INTERVAL.isArrayType());
        
        // Test array element type returns null for non-array types
        assertNull(DataType.CLOB.getArrayElementType());
        assertNull(DataType.BINARY.getArrayElementType());
        assertNull(DataType.VARBINARY.getArrayElementType());
        assertNull(DataType.INTERVAL.getArrayElementType());
    }
}