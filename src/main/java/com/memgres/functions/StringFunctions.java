package com.memgres.functions;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * String functions for MemGres database.
 * Provides PostgreSQL-compatible string manipulation functions.
 */
public class StringFunctions {
    
    /**
     * Concatenate strings (equivalent to PostgreSQL's CONCAT()).
     * @param strings the strings to concatenate (null values are treated as empty strings)
     * @return the concatenated string
     */
    public static String concat(Object... strings) {
        if (strings == null || strings.length == 0) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        for (Object str : strings) {
            if (str != null) {
                result.append(str.toString());
            }
            // PostgreSQL CONCAT treats null as empty string (skips it)
        }
        return result.toString();
    }
    
    /**
     * Concatenate strings with a separator (equivalent to PostgreSQL's CONCAT_WS()).
     * @param separator the separator to use between strings
     * @param strings the strings to concatenate (null values are skipped)
     * @return the concatenated string with separators
     */
    public static String concatWs(String separator, Object... strings) {
        if (separator == null) {
            return null;
        }
        if (strings == null || strings.length == 0) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        boolean first = true;
        
        for (Object str : strings) {
            if (str != null) {
                if (!first) {
                    result.append(separator);
                }
                result.append(str.toString());
                first = false;
            }
        }
        
        return result.toString();
    }
    
    /**
     * Get substring from a string (equivalent to PostgreSQL's SUBSTRING()).
     * @param string the source string
     * @param start the starting position (1-based, as in PostgreSQL)
     * @param length the length of substring (optional)
     * @return the substring
     */
    public static String substring(String string, int start, Integer length) {
        if (string == null) {
            return null;
        }
        
        // Convert to 0-based indexing, but handle negative/zero start positions
        int startIndex = Math.max(0, start - 1);
        if (start <= 0) {
            // PostgreSQL treats positions <= 0 as starting from beginning
            startIndex = 0;
        }
        
        if (startIndex >= string.length()) {
            return "";
        }
        
        if (length == null) {
            return string.substring(startIndex);
        } else {
            int endIndex = Math.min(string.length(), startIndex + Math.max(0, length));
            return string.substring(startIndex, endIndex);
        }
    }
    
    /**
     * Get substring from a string (2-parameter version).
     * @param string the source string
     * @param start the starting position (1-based)
     * @return the substring from start to end
     */
    public static String substring(String string, int start) {
        return substring(string, start, null);
    }
    
    /**
     * Get substring using pattern matching (equivalent to PostgreSQL's SUBSTRING with regex).
     * @param string the source string
     * @param pattern the regular expression pattern
     * @return the first match of the pattern, or null if no match
     */
    public static String substring(String string, String pattern) {
        if (string == null || pattern == null) {
            return null;
        }
        
        Pattern regex = Pattern.compile(pattern);
        java.util.regex.Matcher matcher = regex.matcher(string);
        
        if (matcher.find()) {
            return matcher.group();
        }
        
        return null;
    }
    
    /**
     * Get the length of a string (equivalent to PostgreSQL's LENGTH()).
     * @param string the string to measure
     * @return the length of the string, or null if string is null
     */
    public static Integer length(String string) {
        return string == null ? null : string.length();
    }
    
    /**
     * Convert string to uppercase (equivalent to PostgreSQL's UPPER()).
     * @param string the string to convert
     * @return the uppercase string
     */
    public static String upper(String string) {
        return string == null ? null : string.toUpperCase();
    }
    
    /**
     * Convert string to lowercase (equivalent to PostgreSQL's LOWER()).
     * @param string the string to convert
     * @return the lowercase string
     */
    public static String lower(String string) {
        return string == null ? null : string.toLowerCase();
    }
    
    /**
     * Trim whitespace from both ends (equivalent to PostgreSQL's TRIM()).
     * @param string the string to trim
     * @return the trimmed string
     */
    public static String trim(String string) {
        return string == null ? null : string.trim();
    }
    
    /**
     * Trim specified characters from both ends.
     * @param string the string to trim
     * @param characters the characters to trim
     * @return the trimmed string
     */
    public static String trim(String string, String characters) {
        if (string == null || characters == null) {
            return string;
        }
        
        return trimLeading(trimTrailing(string, characters), characters);
    }
    
    /**
     * Trim whitespace from the left end (equivalent to PostgreSQL's LTRIM()).
     * @param string the string to trim
     * @return the left-trimmed string
     */
    public static String ltrim(String string) {
        if (string == null) {
            return null;
        }
        
        return trimLeading(string, " \t\n\r");
    }
    
    /**
     * Trim specified characters from the left end.
     * @param string the string to trim
     * @param characters the characters to trim
     * @return the left-trimmed string
     */
    public static String ltrim(String string, String characters) {
        return string == null ? null : trimLeading(string, characters);
    }
    
    /**
     * Trim whitespace from the right end (equivalent to PostgreSQL's RTRIM()).
     * @param string the string to trim
     * @return the right-trimmed string
     */
    public static String rtrim(String string) {
        if (string == null) {
            return null;
        }
        
        return trimTrailing(string, " \t\n\r");
    }
    
    /**
     * Trim specified characters from the right end.
     * @param string the string to trim
     * @param characters the characters to trim
     * @return the right-trimmed string
     */
    public static String rtrim(String string, String characters) {
        return string == null ? null : trimTrailing(string, characters);
    }
    
    private static String trimLeading(String string, String characters) {
        if (string.isEmpty() || characters.isEmpty()) {
            return string;
        }
        
        int start = 0;
        while (start < string.length() && characters.indexOf(string.charAt(start)) >= 0) {
            start++;
        }
        
        return string.substring(start);
    }
    
    private static String trimTrailing(String string, String characters) {
        if (string.isEmpty() || characters.isEmpty()) {
            return string;
        }
        
        int end = string.length();
        while (end > 0 && characters.indexOf(string.charAt(end - 1)) >= 0) {
            end--;
        }
        
        return string.substring(0, end);
    }
    
    /**
     * Replace occurrences of a substring (equivalent to PostgreSQL's REPLACE()).
     * @param string the source string
     * @param from the substring to replace
     * @param to the replacement string
     * @return the string with replacements made
     */
    public static String replace(String string, String from, String to) {
        if (string == null || from == null || to == null) {
            return string;
        }
        
        return string.replace(from, to);
    }
    
    /**
     * Find position of substring in string (equivalent to PostgreSQL's POSITION()).
     * @param substring the substring to find
     * @param string the string to search in
     * @return the 1-based position of the substring, or 0 if not found
     */
    public static Integer position(String substring, String string) {
        if (string == null || substring == null) {
            return null;
        }
        
        int pos = string.indexOf(substring);
        return pos >= 0 ? pos + 1 : 0; // Convert to 1-based indexing
    }
    
    /**
     * Left-pad string to specified length (equivalent to PostgreSQL's LPAD()).
     * @param string the string to pad
     * @param length the target length
     * @param padString the padding string (default is space)
     * @return the padded string
     */
    public static String lpad(String string, int length, String padString) {
        if (string == null) {
            return null;
        }
        
        if (padString == null || padString.isEmpty()) {
            padString = " ";
        }
        
        if (string.length() >= length) {
            return string.substring(0, length);
        }
        
        StringBuilder result = new StringBuilder();
        int padLength = length - string.length();
        
        // Add the padding
        while (result.length() < padLength) {
            if (result.length() + padString.length() <= padLength) {
                result.append(padString);
            } else {
                result.append(padString.substring(0, padLength - result.length()));
                break;
            }
        }
        
        result.append(string);
        return result.toString();
    }
    
    /**
     * Left-pad string with spaces.
     * @param string the string to pad
     * @param length the target length
     * @return the padded string
     */
    public static String lpad(String string, int length) {
        return lpad(string, length, " ");
    }
    
    /**
     * Right-pad string to specified length (equivalent to PostgreSQL's RPAD()).
     * @param string the string to pad
     * @param length the target length
     * @param padString the padding string (default is space)
     * @return the padded string
     */
    public static String rpad(String string, int length, String padString) {
        if (string == null) {
            return null;
        }
        
        if (padString == null || padString.isEmpty()) {
            padString = " ";
        }
        
        if (string.length() >= length) {
            return string.substring(0, length);
        }
        
        StringBuilder result = new StringBuilder(string);
        int padLength = length - string.length();
        
        while (result.length() < length) {
            if (result.length() + padString.length() <= length) {
                result.append(padString);
            } else {
                result.append(padString.substring(0, length - result.length()));
            }
        }
        
        return result.toString();
    }
    
    /**
     * Right-pad string with spaces.
     * @param string the string to pad
     * @param length the target length
     * @return the padded string
     */
    public static String rpad(String string, int length) {
        return rpad(string, length, " ");
    }
    
    /**
     * Reverse a string (equivalent to PostgreSQL's REVERSE()).
     * @param string the string to reverse
     * @return the reversed string
     */
    public static String reverse(String string) {
        if (string == null) {
            return null;
        }
        
        return new StringBuilder(string).reverse().toString();
    }
    
    /**
     * Split string into array (equivalent to PostgreSQL's STRING_TO_ARRAY()).
     * @param string the string to split
     * @param delimiter the delimiter
     * @return array of strings
     */
    public static String[] stringToArray(String string, String delimiter) {
        if (string == null || delimiter == null) {
            return null;
        }
        
        if (delimiter.isEmpty()) {
            // Split into individual characters
            return string.split("");
        }
        
        return string.split(Pattern.quote(delimiter));
    }
    
    /**
     * Join array of strings (equivalent to PostgreSQL's ARRAY_TO_STRING()).
     * @param array the array of strings
     * @param delimiter the delimiter
     * @return the joined string
     */
    public static String arrayToString(String[] array, String delimiter) {
        if (array == null || delimiter == null) {
            return null;
        }
        
        return String.join(delimiter, array);
    }
    
    /**
     * Aggregate strings with separator (equivalent to PostgreSQL's STRING_AGG()).
     * This is typically used in GROUP BY queries.
     * @param strings the list of strings to aggregate
     * @param separator the separator
     * @return the aggregated string
     */
    public static String stringAgg(List<String> strings, String separator) {
        if (strings == null || separator == null) {
            return null;
        }
        
        return strings.stream()
                .filter(s -> s != null)
                .collect(Collectors.joining(separator));
    }
    
    /**
     * Check if string starts with prefix (equivalent to PostgreSQL's starts_with()).
     * @param string the string to check
     * @param prefix the prefix to look for
     * @return true if string starts with prefix
     */
    public static Boolean startsWith(String string, String prefix) {
        if (string == null || prefix == null) {
            return null;
        }
        
        return string.startsWith(prefix);
    }
    
    /**
     * Repeat string n times (equivalent to PostgreSQL's REPEAT()).
     * @param string the string to repeat
     * @param count the number of times to repeat
     * @return the repeated string
     */
    public static String repeat(String string, int count) {
        if (string == null || count <= 0) {
            return string == null ? null : "";
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            result.append(string);
        }
        return result.toString();
    }
    
    /**
     * Get leftmost n characters (equivalent to PostgreSQL's LEFT()).
     * @param string the source string
     * @param length the number of characters
     * @return the leftmost characters
     */
    public static String left(String string, int length) {
        if (string == null) {
            return null;
        }
        
        if (length <= 0) {
            return "";
        }
        
        return string.substring(0, Math.min(length, string.length()));
    }
    
    /**
     * Get rightmost n characters (equivalent to PostgreSQL's RIGHT()).
     * @param string the source string
     * @param length the number of characters
     * @return the rightmost characters
     */
    public static String right(String string, int length) {
        if (string == null) {
            return null;
        }
        
        if (length <= 0) {
            return "";
        }
        
        if (length >= string.length()) {
            return string;
        }
        
        return string.substring(string.length() - length);
    }
}