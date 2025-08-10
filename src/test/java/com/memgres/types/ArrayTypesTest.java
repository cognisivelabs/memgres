package com.memgres.types;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for array data type support in MemGres.
 */
class ArrayTypesTest {

    @Test
    void testIntegerArrayFromSqlName() {
        assertEquals(DataType.INTEGER_ARRAY, DataType.fromSqlName("integer[]"));
        assertEquals(DataType.INTEGER_ARRAY, DataType.fromSqlName("int[]"));
        assertEquals(DataType.INTEGER_ARRAY, DataType.fromSqlName("int4[]"));
        assertEquals(DataType.INTEGER_ARRAY, DataType.fromSqlName("INTEGER[]")); // case insensitive
    }

    @Test
    void testTextArrayFromSqlName() {
        assertEquals(DataType.TEXT_ARRAY, DataType.fromSqlName("text[]"));
        assertEquals(DataType.TEXT_ARRAY, DataType.fromSqlName("TEXT[]")); // case insensitive
    }

    @Test
    void testUuidArrayFromSqlName() {
        assertEquals(DataType.UUID_ARRAY, DataType.fromSqlName("uuid[]"));
        assertEquals(DataType.UUID_ARRAY, DataType.fromSqlName("UUID[]")); // case insensitive
    }

    @Test
    void testArrayTypeIdentification() {
        assertTrue(DataType.INTEGER_ARRAY.isArrayType());
        assertTrue(DataType.TEXT_ARRAY.isArrayType());
        assertTrue(DataType.UUID_ARRAY.isArrayType());
        
        assertFalse(DataType.INTEGER.isArrayType());
        assertFalse(DataType.TEXT.isArrayType());
        assertFalse(DataType.UUID.isArrayType());
    }

    @Test
    void testArrayElementTypes() {
        assertEquals(DataType.INTEGER, DataType.INTEGER_ARRAY.getArrayElementType());
        assertEquals(DataType.TEXT, DataType.TEXT_ARRAY.getArrayElementType());
        assertEquals(DataType.UUID, DataType.UUID_ARRAY.getArrayElementType());
        
        assertNull(DataType.INTEGER.getArrayElementType());
        assertNull(DataType.TEXT.getArrayElementType());
    }

    @Test
    void testIntegerArrayValidation() {
        DataType type = DataType.INTEGER_ARRAY;
        
        // Valid arrays
        assertTrue(type.isValidValue(new Integer[]{1, 2, 3}));
        assertTrue(type.isValidValue(new Integer[]{null, 1, null}));
        assertTrue(type.isValidValue(Arrays.asList(1, 2, 3)));
        assertTrue(type.isValidValue(Arrays.asList(null, 1, null)));
        
        // Invalid arrays
        assertFalse(type.isValidValue(new String[]{"a", "b"}));
        assertFalse(type.isValidValue(Arrays.asList("a", "b")));
        assertFalse(type.isValidValue("not an array"));
    }

    @Test
    void testTextArrayValidation() {
        DataType type = DataType.TEXT_ARRAY;
        
        // Valid arrays
        assertTrue(type.isValidValue(new String[]{"hello", "world"}));
        assertTrue(type.isValidValue(new String[]{null, "text", null}));
        assertTrue(type.isValidValue(Arrays.asList("hello", "world")));
        assertTrue(type.isValidValue(Arrays.asList(null, "text", null)));
        
        // Invalid arrays
        assertFalse(type.isValidValue(new Integer[]{1, 2}));
        assertFalse(type.isValidValue(Arrays.asList(1, 2)));
        assertFalse(type.isValidValue("not an array"));
    }

    @Test
    void testUuidArrayValidation() {
        DataType type = DataType.UUID_ARRAY;
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        
        // Valid arrays
        assertTrue(type.isValidValue(new UUID[]{uuid1, uuid2}));
        assertTrue(type.isValidValue(new UUID[]{null, uuid1, null}));
        assertTrue(type.isValidValue(Arrays.asList(uuid1, uuid2)));
        assertTrue(type.isValidValue(Arrays.asList(null, uuid1, null)));
        
        // Invalid arrays
        assertFalse(type.isValidValue(new String[]{"a", "b"}));
        assertFalse(type.isValidValue(Arrays.asList("a", "b")));
        assertFalse(type.isValidValue("not an array"));
    }

    @Test
    void testIntegerArrayConversionFromList() {
        DataType type = DataType.INTEGER_ARRAY;
        List<Integer> list = Arrays.asList(1, 2, 3, null, 4);
        
        Object result = type.convertValue(list);
        assertInstanceOf(Integer[].class, result);
        
        Integer[] array = (Integer[]) result;
        assertEquals(5, array.length);
        assertEquals(Integer.valueOf(1), array[0]);
        assertEquals(Integer.valueOf(2), array[1]);
        assertEquals(Integer.valueOf(3), array[2]);
        assertNull(array[3]);
        assertEquals(Integer.valueOf(4), array[4]);
    }

    @Test
    void testTextArrayConversionFromList() {
        DataType type = DataType.TEXT_ARRAY;
        List<String> list = Arrays.asList("hello", "world", null, "test");
        
        Object result = type.convertValue(list);
        assertInstanceOf(String[].class, result);
        
        String[] array = (String[]) result;
        assertEquals(4, array.length);
        assertEquals("hello", array[0]);
        assertEquals("world", array[1]);
        assertNull(array[2]);
        assertEquals("test", array[3]);
    }

    @Test
    void testUuidArrayConversionFromList() {
        DataType type = DataType.UUID_ARRAY;
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        List<UUID> list = Arrays.asList(uuid1, null, uuid2);
        
        Object result = type.convertValue(list);
        assertInstanceOf(UUID[].class, result);
        
        UUID[] array = (UUID[]) result;
        assertEquals(3, array.length);
        assertEquals(uuid1, array[0]);
        assertNull(array[1]);
        assertEquals(uuid2, array[2]);
    }

    @Test
    void testIntegerArrayStringParsing() {
        DataType type = DataType.INTEGER_ARRAY;
        
        // Basic array
        Object result = type.convertValue("{1,2,3}");
        assertInstanceOf(Integer[].class, result);
        Integer[] array = (Integer[]) result;
        assertArrayEquals(new Integer[]{1, 2, 3}, array);
        
        // Array with nulls
        result = type.convertValue("{1,null,3}");
        array = (Integer[]) result;
        assertEquals(3, array.length);
        assertEquals(Integer.valueOf(1), array[0]);
        assertNull(array[1]);
        assertEquals(Integer.valueOf(3), array[2]);
        
        // Empty array
        result = type.convertValue("{}");
        array = (Integer[]) result;
        assertEquals(0, array.length);
        
        // Array with spaces
        result = type.convertValue("{ 1 , 2 , 3 }");
        array = (Integer[]) result;
        assertArrayEquals(new Integer[]{1, 2, 3}, array);
    }

    @Test
    void testTextArrayStringParsing() {
        DataType type = DataType.TEXT_ARRAY;
        
        // Basic array with quotes
        Object result = type.convertValue("{'hello','world'}");
        assertInstanceOf(String[].class, result);
        String[] array = (String[]) result;
        assertArrayEquals(new String[]{"hello", "world"}, array);
        
        // Array with nulls
        result = type.convertValue("{'hello',null,'world'}");
        array = (String[]) result;
        assertEquals(3, array.length);
        assertEquals("hello", array[0]);
        assertNull(array[1]);
        assertEquals("world", array[2]);
        
        // Empty array
        result = type.convertValue("{}");
        array = (String[]) result;
        assertEquals(0, array.length);
        
        // Array without quotes (should still work)
        result = type.convertValue("{hello,world}");
        array = (String[]) result;
        assertArrayEquals(new String[]{"hello", "world"}, array);
    }

    @Test
    void testUuidArrayStringParsing() {
        DataType type = DataType.UUID_ARRAY;
        String uuid1Str = "550e8400-e29b-41d4-a716-446655440000";
        String uuid2Str = "6ba7b810-9dad-11d1-80b4-00c04fd430c8";
        
        // Basic array with quotes
        Object result = type.convertValue("{'" + uuid1Str + "','" + uuid2Str + "'}");
        assertInstanceOf(UUID[].class, result);
        UUID[] array = (UUID[]) result;
        assertEquals(2, array.length);
        assertEquals(UUID.fromString(uuid1Str), array[0]);
        assertEquals(UUID.fromString(uuid2Str), array[1]);
        
        // Array with nulls
        result = type.convertValue("{'" + uuid1Str + "',null,'" + uuid2Str + "'}");
        array = (UUID[]) result;
        assertEquals(3, array.length);
        assertEquals(UUID.fromString(uuid1Str), array[0]);
        assertNull(array[1]);
        assertEquals(UUID.fromString(uuid2Str), array[2]);
        
        // Empty array
        result = type.convertValue("{}");
        array = (UUID[]) result;
        assertEquals(0, array.length);
    }

    @Test
    void testInvalidArrayStringFormat() {
        DataType type = DataType.INTEGER_ARRAY;
        
        // Missing braces
        assertThrows(IllegalArgumentException.class, () -> type.convertValue("1,2,3"));
        
        // Only opening brace
        assertThrows(IllegalArgumentException.class, () -> type.convertValue("{1,2,3"));
        
        // Only closing brace
        assertThrows(IllegalArgumentException.class, () -> type.convertValue("1,2,3}"));
    }

    @Test
    void testArrayTypesSqlNames() {
        assertEquals("integer[]", DataType.INTEGER_ARRAY.getSqlName());
        assertEquals("text[]", DataType.TEXT_ARRAY.getSqlName());
        assertEquals("uuid[]", DataType.UUID_ARRAY.getSqlName());
        
        assertEquals("integer[]", DataType.INTEGER_ARRAY.toString());
        assertEquals("text[]", DataType.TEXT_ARRAY.toString());
        assertEquals("uuid[]", DataType.UUID_ARRAY.toString());
    }
}