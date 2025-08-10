package com.memgres.functions;

import com.memgres.types.jsonb.JsonbValue;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for enhanced JSONB functions in MemGres.
 */
class JsonbFunctionsTest {
    
    @Test
    void testJsonbPathQuery() {
        // Test basic path queries
        JsonbValue jsonb = JsonbValue.fromString("{\"name\":\"John\",\"age\":30,\"address\":{\"city\":\"NYC\",\"zip\":\"10001\"}}");
        
        // Root path
        JsonbValue[] result = JsonbFunctions.jsonbPathQuery(jsonb, "$");
        assertEquals(1, result.length);
        assertEquals(jsonb.toString(), result[0].toString());
        
        // Simple field access
        result = JsonbFunctions.jsonbPathQuery(jsonb, "$.name");
        assertEquals(1, result.length);
        assertEquals("John", result[0].asText());
        
        // Nested field access
        result = JsonbFunctions.jsonbPathQuery(jsonb, "$.address.city");
        assertEquals(1, result.length);
        assertEquals("NYC", result[0].asText());
        
        // Non-existent path
        result = JsonbFunctions.jsonbPathQuery(jsonb, "$.nonexistent");
        assertEquals(0, result.length);
        
        // Test with array
        JsonbValue arrayJsonb = JsonbValue.fromString("{\"items\":[\"a\",\"b\",\"c\"]}");
        result = JsonbFunctions.jsonbPathQuery(arrayJsonb, "$.items[*]");
        assertEquals(3, result.length);
        assertEquals("a", result[0].asText());
        assertEquals("b", result[1].asText());
        assertEquals("c", result[2].asText());
        
        // Test array index
        result = JsonbFunctions.jsonbPathQuery(arrayJsonb, "$.items[1]");
        assertEquals(1, result.length);
        assertEquals("b", result[0].asText());
        
        // Test null inputs
        result = JsonbFunctions.jsonbPathQuery(null, "$.test");
        assertEquals(0, result.length);
        
        result = JsonbFunctions.jsonbPathQuery(jsonb, null);
        assertEquals(0, result.length);
    }
    
    @Test
    void testJsonbEachRecursive() {
        JsonbValue jsonb = JsonbValue.fromString("{\"a\":1,\"b\":{\"c\":2,\"d\":[3,4]}}");
        
        JsonbValue[] result = JsonbFunctions.jsonbEachRecursive(jsonb);
        
        // Should extract all values recursively
        assertTrue(result.length > 0);
        
        // Test with null
        result = JsonbFunctions.jsonbEachRecursive(null);
        assertEquals(0, result.length);
        
        // Test with simple value
        JsonbValue simpleJsonb = JsonbValue.fromString("\"hello\"");
        result = JsonbFunctions.jsonbEachRecursive(simpleJsonb);
        assertEquals(1, result.length);
        assertEquals("hello", result[0].asText());
    }
    
    @Test
    void testJsonbObjectKeys() {
        JsonbValue jsonb = JsonbValue.fromString("{\"name\":\"John\",\"age\":30,\"city\":\"NYC\"}");
        
        String[] keys = JsonbFunctions.jsonbObjectKeys(jsonb);
        assertEquals(3, keys.length);
        Arrays.sort(keys); // Sort for predictable testing
        assertArrayEquals(new String[]{"age", "city", "name"}, keys);
        
        // Test with non-object
        JsonbValue arrayJsonb = JsonbValue.fromString("[1,2,3]");
        keys = JsonbFunctions.jsonbObjectKeys(arrayJsonb);
        assertEquals(0, keys.length);
        
        // Test with null
        keys = JsonbFunctions.jsonbObjectKeys(null);
        assertEquals(0, keys.length);
        
        // Test with empty object
        JsonbValue emptyJsonb = JsonbValue.fromString("{}");
        keys = JsonbFunctions.jsonbObjectKeys(emptyJsonb);
        assertEquals(0, keys.length);
    }
    
    @Test
    void testJsonbArrayLength() {
        JsonbValue arrayJsonb = JsonbValue.fromString("[1,2,3,4,5]");
        
        Integer length = JsonbFunctions.jsonbArrayLength(arrayJsonb);
        assertEquals(5, length);
        
        // Test with empty array
        JsonbValue emptyArray = JsonbValue.fromString("[]");
        length = JsonbFunctions.jsonbArrayLength(emptyArray);
        assertEquals(0, length);
        
        // Test with non-array
        JsonbValue objectJsonb = JsonbValue.fromString("{\"a\":1}");
        length = JsonbFunctions.jsonbArrayLength(objectJsonb);
        assertNull(length);
        
        // Test with null
        length = JsonbFunctions.jsonbArrayLength(null);
        assertNull(length);
    }
    
    @Test
    void testJsonbExtractPathText() {
        JsonbValue jsonb = JsonbValue.fromString("{\"person\":{\"name\":\"John\",\"age\":30}}");
        
        String result = JsonbFunctions.jsonbExtractPathText(jsonb, "person", "name");
        assertEquals("John", result);
        
        result = JsonbFunctions.jsonbExtractPathText(jsonb, "person", "age");
        assertEquals("30", result);
        
        // Non-existent path
        result = JsonbFunctions.jsonbExtractPathText(jsonb, "person", "nonexistent");
        assertNull(result);
        
        // Test with null
        result = JsonbFunctions.jsonbExtractPathText(null, "test");
        assertNull(result);
    }
    
    @Test
    void testJsonbBuildObject() {
        JsonbValue result = JsonbFunctions.jsonbBuildObject("name", "John", "age", 30);
        
        assertTrue(result.isObject());
        assertEquals("John", result.getFieldAsText("name"));
        assertEquals("30", result.getFieldAsText("age"));
        
        // Test empty object
        result = JsonbFunctions.jsonbBuildObject();
        assertTrue(result.isObject());
        assertEquals(0, result.size());
        
        // Test with null key (should be skipped)
        result = JsonbFunctions.jsonbBuildObject("name", "John", null, "ignored", "age", 30);
        assertEquals(2, result.size());
        assertEquals("John", result.getFieldAsText("name"));
        assertEquals("30", result.getFieldAsText("age"));
        
        // Test odd number of arguments (should throw)
        assertThrows(IllegalArgumentException.class, () -> {
            JsonbFunctions.jsonbBuildObject("name", "John", "age");
        });
        
        // Test null arguments - this should throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            JsonbFunctions.jsonbBuildObject((Object[]) null);
        });
    }
    
    @Test
    void testJsonbBuildArray() {
        JsonbValue result = JsonbFunctions.jsonbBuildArray("a", "b", "c");
        
        assertTrue(result.isArray());
        assertEquals(3, result.size());
        assertEquals("a", result.getElementAsText(0));
        assertEquals("b", result.getElementAsText(1));
        assertEquals("c", result.getElementAsText(2));
        
        // Test empty array
        result = JsonbFunctions.jsonbBuildArray();
        assertTrue(result.isArray());
        assertEquals(0, result.size());
        
        // Test mixed types
        result = JsonbFunctions.jsonbBuildArray("string", 123, true);
        assertEquals(3, result.size());
        assertEquals("string", result.getElementAsText(0));
        assertEquals("123", result.getElementAsText(1));
        assertEquals("true", result.getElementAsText(2));
        
        // Test with null
        result = JsonbFunctions.jsonbBuildArray((Object[]) null);
        assertTrue(result.isArray());
        assertEquals(0, result.size());
    }
    
    @Test
    void testJsonbAgg() {
        List<JsonbValue> values = Arrays.asList(
            JsonbValue.fromString("\"a\""),
            JsonbValue.fromString("\"b\""),
            JsonbValue.fromString("\"c\"")
        );
        
        JsonbValue result = JsonbFunctions.jsonbAgg(values);
        
        assertTrue(result.isArray());
        assertEquals(3, result.size());
        
        // Test with empty list
        result = JsonbFunctions.jsonbAgg(new ArrayList<>());
        assertTrue(result.isArray());
        assertEquals(0, result.size());
        
        // Test with null list
        result = JsonbFunctions.jsonbAgg(null);
        assertTrue(result.isArray());
        assertEquals(0, result.size());
        
        // Test with null values in list (should be filtered out)
        List<JsonbValue> valuesWithNull = Arrays.asList(
            JsonbValue.fromString("\"a\""),
            null,
            JsonbValue.fromString("\"c\"")
        );
        
        result = JsonbFunctions.jsonbAgg(valuesWithNull);
        assertEquals(2, result.size());
    }
    
    @Test
    void testJsonbObjectAgg() {
        List<JsonbValue> objects = Arrays.asList(
            JsonbValue.fromString("{\"a\":1}"),
            JsonbValue.fromString("{\"b\":2}"),
            JsonbValue.fromString("{\"c\":3}")
        );
        
        JsonbValue result = JsonbFunctions.jsonbObjectAgg(objects);
        
        assertTrue(result.isObject());
        assertEquals(3, result.size());
        
        // Test with empty list
        result = JsonbFunctions.jsonbObjectAgg(new ArrayList<>());
        assertTrue(result.isObject());
        assertEquals(0, result.size());
        
        // Test with null list
        result = JsonbFunctions.jsonbObjectAgg(null);
        assertTrue(result.isObject());
        assertEquals(0, result.size());
        
        // Test with non-object values (should be ignored)
        List<JsonbValue> mixedValues = Arrays.asList(
            JsonbValue.fromString("{\"a\":1}"),
            JsonbValue.fromString("\"not an object\""),
            JsonbValue.fromString("{\"b\":2}")
        );
        
        result = JsonbFunctions.jsonbObjectAgg(mixedValues);
        assertEquals(2, result.size());
    }
    
    @Test
    void testJsonbRemoveKey() {
        JsonbValue jsonb = JsonbValue.fromString("{\"name\":\"John\",\"age\":30,\"city\":\"NYC\"}");
        
        JsonbValue result = JsonbFunctions.jsonbRemoveKey(jsonb, "age");
        
        assertTrue(result.isObject());
        assertEquals(2, result.size());
        assertFalse(result.hasKey("age"));
        assertTrue(result.hasKey("name"));
        assertTrue(result.hasKey("city"));
        
        // Test removing non-existent key
        result = JsonbFunctions.jsonbRemoveKey(jsonb, "nonexistent");
        assertEquals(jsonb.size(), result.size());
        
        // Test with non-object
        JsonbValue arrayJsonb = JsonbValue.fromString("[1,2,3]");
        result = JsonbFunctions.jsonbRemoveKey(arrayJsonb, "test");
        assertEquals(arrayJsonb, result);
        
        // Test with null inputs
        result = JsonbFunctions.jsonbRemoveKey(null, "test");
        assertNull(result);
        
        result = JsonbFunctions.jsonbRemoveKey(jsonb, null);
        assertEquals(jsonb, result);
    }
    
    @Test
    void testJsonbSetPath() {
        JsonbValue jsonb = JsonbValue.fromString("{\"person\":{\"name\":\"John\"}}");
        JsonbValue newValue = JsonbValue.fromString("\"Jane\"");
        
        JsonbValue result = JsonbFunctions.jsonbSetPath(jsonb, new String[]{"person", "name"}, newValue);
        
        assertEquals("Jane", result.getPathAsText("person", "name"));
        
        // Test setting new path
        JsonbValue ageValue = JsonbValue.fromString("30");
        result = JsonbFunctions.jsonbSetPath(result, new String[]{"person", "age"}, ageValue);
        
        assertEquals("30", result.getPathAsText("person", "age"));
        assertEquals("Jane", result.getPathAsText("person", "name"));
        
        // Test with null inputs
        result = JsonbFunctions.jsonbSetPath(null, new String[]{"test"}, newValue);
        assertNull(result);
        
        result = JsonbFunctions.jsonbSetPath(jsonb, null, newValue);
        assertEquals(jsonb, result);
        
        result = JsonbFunctions.jsonbSetPath(jsonb, new String[]{}, newValue);
        assertEquals(jsonb, result);
        
        result = JsonbFunctions.jsonbSetPath(jsonb, new String[]{"test"}, null);
        assertEquals(jsonb, result);
    }
    
    @Test
    void testJsonbMatches() {
        JsonbValue jsonb = JsonbValue.fromString("{\"name\":\"John\",\"email\":\"john@example.com\"}");
        
        // Test pattern matching
        assertTrue(JsonbFunctions.jsonbMatches(jsonb, ".*john.*"));
        assertTrue(JsonbFunctions.jsonbMatches(jsonb, ".*@example\\.com.*"));
        assertFalse(JsonbFunctions.jsonbMatches(jsonb, ".*xyz.*"));
        
        // Test with simple string
        JsonbValue stringJsonb = JsonbValue.fromString("\"hello world\"");
        assertTrue(JsonbFunctions.jsonbMatches(stringJsonb, ".*world.*"));
        assertFalse(JsonbFunctions.jsonbMatches(stringJsonb, ".*xyz.*"));
        
        // Test with null inputs
        assertFalse(JsonbFunctions.jsonbMatches(null, ".*test.*"));
        assertFalse(JsonbFunctions.jsonbMatches(jsonb, null));
    }
    
    @Test
    void testJsonbTypeof() {
        assertEquals("object", JsonbFunctions.jsonbTypeof(JsonbValue.fromString("{\"a\":1}")));
        assertEquals("array", JsonbFunctions.jsonbTypeof(JsonbValue.fromString("[1,2,3]")));
        assertEquals("string", JsonbFunctions.jsonbTypeof(JsonbValue.fromString("\"hello\"")));
        assertEquals("number", JsonbFunctions.jsonbTypeof(JsonbValue.fromString("123")));
        assertEquals("boolean", JsonbFunctions.jsonbTypeof(JsonbValue.fromString("true")));
        assertEquals("null", JsonbFunctions.jsonbTypeof(JsonbValue.fromString("null")));
        assertEquals("null", JsonbFunctions.jsonbTypeof(null));
    }
    
    @Test
    void testJsonbPretty() {
        JsonbValue jsonb = JsonbValue.fromString("{\"name\":\"John\",\"age\":30}");
        
        String pretty = JsonbFunctions.jsonbPretty(jsonb);
        
        assertNotNull(pretty);
        assertTrue(pretty.contains("name"));
        assertTrue(pretty.contains("John"));
        assertTrue(pretty.contains("age"));
        assertTrue(pretty.contains("30"));
        
        // Should be formatted (contain newlines)
        assertTrue(pretty.contains("\n"));
        
        // Test with null
        assertNull(JsonbFunctions.jsonbPretty(null));
    }
    
    @Test
    void testJsonbStripNulls() {
        JsonbValue jsonb = JsonbValue.fromString("{\"name\":\"John\",\"age\":null,\"city\":\"NYC\",\"country\":null}");
        
        JsonbValue result = JsonbFunctions.jsonbStripNulls(jsonb);
        
        assertTrue(result.isObject());
        assertEquals(2, result.size());
        assertTrue(result.hasKey("name"));
        assertTrue(result.hasKey("city"));
        assertFalse(result.hasKey("age"));
        assertFalse(result.hasKey("country"));
        
        // Test with array containing nulls
        JsonbValue arrayJsonb = JsonbValue.fromString("[1,null,3,null,5]");
        result = JsonbFunctions.jsonbStripNulls(arrayJsonb);
        
        assertTrue(result.isArray());
        assertEquals(3, result.size());
        assertEquals("1", result.getElementAsText(0));
        assertEquals("3", result.getElementAsText(1));
        assertEquals("5", result.getElementAsText(2));
        
        // Test with nested objects
        JsonbValue nestedJsonb = JsonbValue.fromString("{\"person\":{\"name\":\"John\",\"age\":null},\"active\":null}");
        result = JsonbFunctions.jsonbStripNulls(nestedJsonb);
        
        assertEquals(1, result.size());
        assertTrue(result.hasKey("person"));
        JsonbValue person = result.getField("person");
        assertEquals(1, person.size());
        assertTrue(person.hasKey("name"));
        assertFalse(person.hasKey("age"));
        
        // Test with null
        assertNull(JsonbFunctions.jsonbStripNulls(null));
    }
    
    @Test
    void testComplexJsonPathQueries() {
        JsonbValue complexJsonb = JsonbValue.fromString(
            "{\"users\":[" +
                "{\"name\":\"John\",\"age\":30,\"hobbies\":[\"reading\",\"gaming\"]}," +
                "{\"name\":\"Jane\",\"age\":25,\"hobbies\":[\"painting\",\"music\"]}" +
            "]}"
        );
        
        // Test wildcard with arrays
        JsonbValue[] result = JsonbFunctions.jsonbPathQuery(complexJsonb, "$.users[*]");
        assertEquals(2, result.length);
        
        // Test nested array access
        result = JsonbFunctions.jsonbPathQuery(complexJsonb, "$.users[0].hobbies[*]");
        assertEquals(2, result.length);
        assertEquals("reading", result[0].asText());
        assertEquals("gaming", result[1].asText());
        
        // Test specific array index with nested field
        result = JsonbFunctions.jsonbPathQuery(complexJsonb, "$.users[1].name");
        assertEquals(1, result.length);
        assertEquals("Jane", result[0].asText());
    }
    
    @Test
    void testEdgeCasesAndErrorHandling() {
        // Test with malformed JSON paths
        JsonbValue jsonb = JsonbValue.fromString("{\"test\":\"value\"}");
        
        JsonbValue[] result = JsonbFunctions.jsonbPathQuery(jsonb, "invalid.path");
        assertEquals(0, result.length);
        
        // Test array operations on non-arrays
        Integer length = JsonbFunctions.jsonbArrayLength(JsonbValue.fromString("\"not an array\""));
        assertNull(length);
        
        // Test object operations on non-objects
        String[] keys = JsonbFunctions.jsonbObjectKeys(JsonbValue.fromString("[1,2,3]"));
        assertEquals(0, keys.length);
        
        // Test path operations with empty paths
        String pathText = JsonbFunctions.jsonbExtractPathText(jsonb);
        assertEquals("value", jsonb.getPathAsText("test"));
        
        // Test aggregation with mixed null values
        List<JsonbValue> mixedList = Arrays.asList(
            JsonbValue.fromString("1"),
            null,
            JsonbValue.fromString("3"),
            null
        );
        JsonbValue aggResult = JsonbFunctions.jsonbAgg(mixedList);
        assertEquals(2, aggResult.size());
    }
}