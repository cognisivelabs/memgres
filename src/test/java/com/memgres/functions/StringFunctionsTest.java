package com.memgres.functions;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for string functions in MemGres.
 */
class StringFunctionsTest {
    
    @Test
    void testConcat() {
        assertEquals("", StringFunctions.concat());
        assertEquals("hello", StringFunctions.concat("hello"));
        assertEquals("helloworld", StringFunctions.concat("hello", "world"));
        assertEquals("hello123", StringFunctions.concat("hello", 123));
        assertEquals("helloworld", StringFunctions.concat("hello", null, "world"));
        assertEquals("123456", StringFunctions.concat(123, 456));
    }
    
    @Test
    void testConcatWs() {
        assertNull(StringFunctions.concatWs(null, "a", "b"));
        assertEquals("", StringFunctions.concatWs("-"));
        assertEquals("hello", StringFunctions.concatWs("-", "hello"));
        assertEquals("hello-world", StringFunctions.concatWs("-", "hello", "world"));
        assertEquals("hello-world", StringFunctions.concatWs("-", "hello", null, "world"));
        assertEquals("1-2-3", StringFunctions.concatWs("-", 1, 2, 3));
        assertEquals("a,b,c", StringFunctions.concatWs(",", "a", "b", "c"));
    }
    
    @Test
    void testSubstring() {
        // Test with length parameter
        assertEquals("ell", StringFunctions.substring("hello", 2, 3));
        assertEquals("lo", StringFunctions.substring("hello", 4, 5));
        assertEquals("", StringFunctions.substring("hello", 10, 3));
        assertEquals("", StringFunctions.substring("hello", 2, 0));
        assertEquals("", StringFunctions.substring("hello", 2, -1));
        
        // Test without length parameter
        assertEquals("ello", StringFunctions.substring("hello", 2));
        assertEquals("o", StringFunctions.substring("hello", 5));
        assertEquals("", StringFunctions.substring("hello", 10));
        
        // Test with 1-based indexing (PostgreSQL style)
        assertEquals("h", StringFunctions.substring("hello", 1, 1));
        assertEquals("hello", StringFunctions.substring("hello", 1));
        
        // Test null values
        assertNull(StringFunctions.substring(null, 1, 3));
        assertNull(StringFunctions.substring(null, 1));
        
        // Test edge cases
        assertEquals("h", StringFunctions.substring("hello", 0, 1)); // PostgreSQL treats 0 as position 1
        assertEquals("h", StringFunctions.substring("hello", 1, 1));
    }
    
    @Test
    void testSubstringWithPattern() {
        assertEquals("123", StringFunctions.substring("abc123def", "\\d+"));
        assertEquals("hello", StringFunctions.substring("hello world", "\\w+"));
        assertNull(StringFunctions.substring("hello", "\\d+"));
        assertNull(StringFunctions.substring(null, "\\w+"));
        assertNull(StringFunctions.substring("hello", null));
        
        // Test more complex patterns
        assertEquals("@example.com", StringFunctions.substring("user@example.com", "@[a-zA-Z0-9.-]+\\.[a-zA-Z]+"));
        assertEquals("2023", StringFunctions.substring("Date: 2023-12-25", "\\d{4}"));
    }
    
    @Test
    void testLength() {
        assertEquals(5, StringFunctions.length("hello"));
        assertEquals(0, StringFunctions.length(""));
        assertNull(StringFunctions.length(null));
        assertEquals(11, StringFunctions.length("hello world"));
        assertEquals(3, StringFunctions.length("123"));
    }
    
    @Test
    void testUpper() {
        assertEquals("HELLO", StringFunctions.upper("hello"));
        assertEquals("HELLO WORLD", StringFunctions.upper("Hello World"));
        assertEquals("123", StringFunctions.upper("123"));
        assertEquals("", StringFunctions.upper(""));
        assertNull(StringFunctions.upper(null));
    }
    
    @Test
    void testLower() {
        assertEquals("hello", StringFunctions.lower("HELLO"));
        assertEquals("hello world", StringFunctions.lower("Hello World"));
        assertEquals("123", StringFunctions.lower("123"));
        assertEquals("", StringFunctions.lower(""));
        assertNull(StringFunctions.lower(null));
    }
    
    @Test
    void testTrim() {
        assertEquals("hello", StringFunctions.trim("  hello  "));
        assertEquals("hello", StringFunctions.trim("\t\nhello\r\n"));
        assertEquals("", StringFunctions.trim("   "));
        assertEquals("hello world", StringFunctions.trim(" hello world "));
        assertNull(StringFunctions.trim(null));
        
        // Test trim with specific characters
        assertEquals("hello", StringFunctions.trim("xhellox", "x"));
        assertEquals("hello", StringFunctions.trim("xyhelloyx", "xy"));
        assertEquals("hello", StringFunctions.trim("hello", null));
        assertEquals("hello", StringFunctions.trim("  hello  ", " "));
    }
    
    @Test
    void testLtrim() {
        assertEquals("hello  ", StringFunctions.ltrim("  hello  "));
        assertEquals("hello world", StringFunctions.ltrim("   hello world"));
        assertEquals("", StringFunctions.ltrim("   "));
        assertNull(StringFunctions.ltrim(null));
        
        // Test ltrim with specific characters
        assertEquals("hellox", StringFunctions.ltrim("xhellox", "x"));
        assertEquals("helloyx", StringFunctions.ltrim("xyhelloyx", "xy"));
    }
    
    @Test
    void testRtrim() {
        assertEquals("  hello", StringFunctions.rtrim("  hello  "));
        assertEquals("hello world", StringFunctions.rtrim("hello world   "));
        assertEquals("", StringFunctions.rtrim("   "));
        assertNull(StringFunctions.rtrim(null));
        
        // Test rtrim with specific characters
        assertEquals("xhello", StringFunctions.rtrim("xhellox", "x"));
        assertEquals("xyhello", StringFunctions.rtrim("xyhelloyx", "xy"));
    }
    
    @Test
    void testReplace() {
        assertEquals("hi world", StringFunctions.replace("hello world", "hello", "hi"));
        assertEquals("hello world", StringFunctions.replace("hello world", "xyz", "abc"));
        assertEquals("aaa", StringFunctions.replace("aba", "b", "a"));
        assertEquals("", StringFunctions.replace("hello", "hello", ""));
        assertNull(StringFunctions.replace(null, "a", "b"));
        assertEquals("hello", StringFunctions.replace("hello", null, "b"));
        assertEquals("hello", StringFunctions.replace("hello", "a", null));
    }
    
    @Test
    void testPosition() {
        assertEquals(2, StringFunctions.position("ell", "hello"));
        assertEquals(7, StringFunctions.position("world", "hello world"));
        assertEquals(0, StringFunctions.position("xyz", "hello"));
        assertEquals(1, StringFunctions.position("h", "hello"));
        assertEquals(5, StringFunctions.position("o", "hello"));
        assertNull(StringFunctions.position("a", null));
        assertNull(StringFunctions.position(null, "hello"));
        
        // Test empty string
        assertEquals(1, StringFunctions.position("", "hello"));
        assertEquals(0, StringFunctions.position("a", ""));
    }
    
    @Test
    void testLpad() {
        assertEquals("  hello", StringFunctions.lpad("hello", 7));
        assertEquals("hello", StringFunctions.lpad("hello", 5));
        assertEquals("hel", StringFunctions.lpad("hello", 3));
        assertEquals("***hello", StringFunctions.lpad("hello", 8, "*"));
        assertEquals("abchello", StringFunctions.lpad("hello", 8, "abc"));
        assertEquals("abcabcab", StringFunctions.lpad("", 8, "abc"));
        assertNull(StringFunctions.lpad(null, 5));
        assertEquals(" hello", StringFunctions.lpad("hello", 6, null));
        assertEquals("", StringFunctions.lpad("hello", 0));
    }
    
    @Test
    void testRpad() {
        assertEquals("hello  ", StringFunctions.rpad("hello", 7));
        assertEquals("hello", StringFunctions.rpad("hello", 5));
        assertEquals("hel", StringFunctions.rpad("hello", 3));
        assertEquals("hello***", StringFunctions.rpad("hello", 8, "*"));
        assertEquals("helloabc", StringFunctions.rpad("hello", 8, "abc"));
        assertEquals("abcabcab", StringFunctions.rpad("", 8, "abc"));
        assertNull(StringFunctions.rpad(null, 5));
        assertEquals("hello ", StringFunctions.rpad("hello", 6, null));
        assertEquals("", StringFunctions.rpad("hello", 0));
    }
    
    @Test
    void testReverse() {
        assertEquals("olleh", StringFunctions.reverse("hello"));
        assertEquals("dlrow olleh", StringFunctions.reverse("hello world"));
        assertEquals("321", StringFunctions.reverse("123"));
        assertEquals("", StringFunctions.reverse(""));
        assertNull(StringFunctions.reverse(null));
    }
    
    @Test
    void testStringToArray() {
        assertArrayEquals(new String[]{"a", "b", "c"}, StringFunctions.stringToArray("a,b,c", ","));
        assertArrayEquals(new String[]{"hello", "world"}, StringFunctions.stringToArray("hello world", " "));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringFunctions.stringToArray("a|b|c", "|"));
        assertArrayEquals(new String[]{"hello"}, StringFunctions.stringToArray("hello", ","));
        assertArrayEquals(new String[]{"h", "e", "l", "l", "o"}, StringFunctions.stringToArray("hello", ""));
        assertNull(StringFunctions.stringToArray(null, ","));
        assertNull(StringFunctions.stringToArray("hello", null));
    }
    
    @Test
    void testArrayToString() {
        assertEquals("a,b,c", StringFunctions.arrayToString(new String[]{"a", "b", "c"}, ","));
        assertEquals("hello world", StringFunctions.arrayToString(new String[]{"hello", "world"}, " "));
        assertEquals("a|b|c", StringFunctions.arrayToString(new String[]{"a", "b", "c"}, "|"));
        assertEquals("hello", StringFunctions.arrayToString(new String[]{"hello"}, ","));
        assertEquals("", StringFunctions.arrayToString(new String[]{}, ","));
        assertNull(StringFunctions.arrayToString(null, ","));
        assertNull(StringFunctions.arrayToString(new String[]{"a", "b"}, null));
    }
    
    @Test
    void testStringAgg() {
        assertEquals("a,b,c", StringFunctions.stringAgg(Arrays.asList("a", "b", "c"), ","));
        assertEquals("hello world", StringFunctions.stringAgg(Arrays.asList("hello", "world"), " "));
        assertEquals("a|b|c", StringFunctions.stringAgg(Arrays.asList("a", "b", "c"), "|"));
        assertEquals("hello", StringFunctions.stringAgg(Arrays.asList("hello"), ","));
        assertEquals("", StringFunctions.stringAgg(Arrays.asList(), ","));
        assertEquals("a,c", StringFunctions.stringAgg(Arrays.asList("a", null, "c"), ","));
        assertNull(StringFunctions.stringAgg(null, ","));
        assertNull(StringFunctions.stringAgg(Arrays.asList("a", "b"), null));
    }
    
    @Test
    void testStartsWith() {
        assertTrue(StringFunctions.startsWith("hello", "hel"));
        assertTrue(StringFunctions.startsWith("hello", ""));
        assertTrue(StringFunctions.startsWith("hello", "hello"));
        assertFalse(StringFunctions.startsWith("hello", "ell"));
        assertFalse(StringFunctions.startsWith("hello", "world"));
        assertNull(StringFunctions.startsWith(null, "hel"));
        assertNull(StringFunctions.startsWith("hello", null));
    }
    
    @Test
    void testRepeat() {
        assertEquals("hellohellohello", StringFunctions.repeat("hello", 3));
        assertEquals("", StringFunctions.repeat("hello", 0));
        assertEquals("", StringFunctions.repeat("hello", -1));
        assertEquals("aaa", StringFunctions.repeat("a", 3));
        assertEquals("", StringFunctions.repeat("", 5));
        assertNull(StringFunctions.repeat(null, 3));
    }
    
    @Test
    void testLeft() {
        assertEquals("hel", StringFunctions.left("hello", 3));
        assertEquals("hello", StringFunctions.left("hello", 10));
        assertEquals("", StringFunctions.left("hello", 0));
        assertEquals("", StringFunctions.left("hello", -1));
        assertEquals("h", StringFunctions.left("hello", 1));
        assertNull(StringFunctions.left(null, 3));
    }
    
    @Test
    void testRight() {
        assertEquals("llo", StringFunctions.right("hello", 3));
        assertEquals("hello", StringFunctions.right("hello", 10));
        assertEquals("", StringFunctions.right("hello", 0));
        assertEquals("", StringFunctions.right("hello", -1));
        assertEquals("o", StringFunctions.right("hello", 1));
        assertNull(StringFunctions.right(null, 3));
    }
    
    @Test
    void testEdgeCasesWithEmptyStrings() {
        assertEquals("", StringFunctions.concat(""));
        assertEquals("hello", StringFunctions.concat("hello", ""));
        assertEquals("", StringFunctions.upper(""));
        assertEquals("", StringFunctions.lower(""));
        assertEquals("", StringFunctions.trim(""));
        assertEquals("", StringFunctions.ltrim(""));
        assertEquals("", StringFunctions.rtrim(""));
        assertEquals(0, StringFunctions.length(""));
        assertEquals(1, StringFunctions.position("", "hello"));
        assertEquals("", StringFunctions.replace("", "a", "b"));
        assertEquals("", StringFunctions.reverse(""));
        assertEquals("  ", StringFunctions.lpad("", 2));
        assertEquals("  ", StringFunctions.rpad("", 2));
    }
    
    @Test
    void testCaseInsensitiveOperations() {
        // Most string functions are case-sensitive by design
        // But we can test behavior with mixed case
        assertEquals("Hello", StringFunctions.substring("Hello World", 1, 5));
        assertEquals(7, StringFunctions.position("World", "Hello World"));
        assertEquals(0, StringFunctions.position("world", "Hello World")); // Case sensitive
        assertEquals("Hello Universe", StringFunctions.replace("Hello World", "World", "Universe"));
    }
}