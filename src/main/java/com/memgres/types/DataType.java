package com.memgres.types;

import com.memgres.types.jsonb.JsonbValue;

import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Enumeration of supported data types in MemGres.
 * Each data type defines validation logic for values.
 */
public enum DataType {
    
    // Numeric types
    SMALLINT("smallint") {
        @Override
        public boolean isValidValue(Object value) {
            if (value instanceof Short) return true;
            if (value instanceof Integer) {
                int intValue = (Integer) value;
                return intValue >= Short.MIN_VALUE && intValue <= Short.MAX_VALUE;
            }
            return false;
        }
        
        @Override
        public Object convertValue(Object value) {
            if (value instanceof Number) {
                return ((Number) value).shortValue();
            }
            if (value instanceof String) {
                return Short.parseShort((String) value);
            }
            return value;
        }
    },
    
    INTEGER("integer") {
        @Override
        public boolean isValidValue(Object value) {
            return value instanceof Integer || value instanceof Short || value instanceof Byte;
        }
        
        @Override
        public Object convertValue(Object value) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            if (value instanceof String) {
                return Integer.parseInt((String) value);
            }
            return value;
        }
    },
    
    BIGINT("bigint") {
        @Override
        public boolean isValidValue(Object value) {
            return value instanceof Long || value instanceof Integer || 
                   value instanceof Short || value instanceof Byte;
        }
        
        @Override
        public Object convertValue(Object value) {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            if (value instanceof String) {
                return Long.parseLong((String) value);
            }
            return value;
        }
    },
    
    DECIMAL("decimal") {
        @Override
        public boolean isValidValue(Object value) {
            return value instanceof BigDecimal || value instanceof Number;
        }
        
        @Override
        public Object convertValue(Object value) {
            if (value instanceof BigDecimal) {
                return value;
            }
            if (value instanceof Number) {
                return BigDecimal.valueOf(((Number) value).doubleValue());
            }
            if (value instanceof String) {
                return new BigDecimal((String) value);
            }
            return value;
        }
    },
    
    REAL("real") {
        @Override
        public boolean isValidValue(Object value) {
            return value instanceof Float || value instanceof Number;
        }
        
        @Override
        public Object convertValue(Object value) {
            if (value instanceof Number) {
                return ((Number) value).floatValue();
            }
            if (value instanceof String) {
                return Float.parseFloat((String) value);
            }
            return value;
        }
    },
    
    DOUBLE_PRECISION("double precision") {
        @Override
        public boolean isValidValue(Object value) {
            return value instanceof Double || value instanceof Number;
        }
        
        @Override
        public Object convertValue(Object value) {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            if (value instanceof String) {
                return Double.parseDouble((String) value);
            }
            return value;
        }
    },
    
    // String types
    VARCHAR("varchar") {
        @Override
        public boolean isValidValue(Object value) {
            return value instanceof String || value instanceof Character;
        }
        
        @Override
        public Object convertValue(Object value) {
            if (value instanceof Character) {
                return value.toString();
            }
            return value != null ? value.toString() : null;
        }
    },
    
    TEXT("text") {
        @Override
        public boolean isValidValue(Object value) {
            return value instanceof String || value instanceof Character;
        }
        
        @Override
        public Object convertValue(Object value) {
            if (value instanceof Character) {
                return value.toString();
            }
            return value != null ? value.toString() : null;
        }
    },
    
    CHAR("char") {
        @Override
        public boolean isValidValue(Object value) {
            return value instanceof Character || 
                   (value instanceof String && ((String) value).length() == 1);
        }
        
        @Override
        public Object convertValue(Object value) {
            if (value instanceof String) {
                String str = (String) value;
                return str.isEmpty() ? ' ' : str.charAt(0);
            }
            return value;
        }
    },
    
    // Boolean type
    BOOLEAN("boolean") {
        @Override
        public boolean isValidValue(Object value) {
            return value instanceof Boolean;
        }
        
        @Override
        public Object convertValue(Object value) {
            if (value instanceof String) {
                String str = ((String) value).toLowerCase();
                return "true".equals(str) || "t".equals(str) || "1".equals(str) ||
                       "yes".equals(str) || "y".equals(str) || "on".equals(str);
            }
            if (value instanceof Number) {
                return ((Number) value).intValue() != 0;
            }
            return value;
        }
    },
    
    // Date/Time types
    DATE("date") {
        @Override
        public boolean isValidValue(Object value) {
            return value instanceof LocalDate || value instanceof java.sql.Date;
        }
        
        @Override
        public Object convertValue(Object value) {
            if (value instanceof java.sql.Date) {
                return ((java.sql.Date) value).toLocalDate();
            }
            if (value instanceof String) {
                return LocalDate.parse((String) value);
            }
            return value;
        }
    },
    
    TIME("time") {
        @Override
        public boolean isValidValue(Object value) {
            return value instanceof LocalTime || value instanceof java.sql.Time;
        }
        
        @Override
        public Object convertValue(Object value) {
            if (value instanceof java.sql.Time) {
                return ((java.sql.Time) value).toLocalTime();
            }
            if (value instanceof String) {
                return LocalTime.parse((String) value);
            }
            return value;
        }
    },
    
    TIMESTAMP("timestamp") {
        @Override
        public boolean isValidValue(Object value) {
            return value instanceof LocalDateTime || value instanceof java.sql.Timestamp;
        }
        
        @Override
        public Object convertValue(Object value) {
            if (value instanceof java.sql.Timestamp) {
                return ((java.sql.Timestamp) value).toLocalDateTime();
            }
            if (value instanceof String) {
                return LocalDateTime.parse((String) value);
            }
            return value;
        }
    },
    
    TIMESTAMPTZ("timestamptz") {
        @Override
        public boolean isValidValue(Object value) {
            return value instanceof ZonedDateTime || value instanceof OffsetDateTime ||
                   value instanceof Instant || value instanceof java.sql.Timestamp;
        }
        
        @Override
        public Object convertValue(Object value) {
            if (value instanceof java.sql.Timestamp) {
                return ((java.sql.Timestamp) value).toInstant().atZone(ZoneOffset.UTC);
            }
            if (value instanceof String) {
                return ZonedDateTime.parse((String) value);
            }
            return value;
        }
    },
    
    // UUID type
    UUID("uuid") {
        @Override
        public boolean isValidValue(Object value) {
            return value instanceof java.util.UUID;
        }
        
        @Override
        public Object convertValue(Object value) {
            if (value instanceof String) {
                return java.util.UUID.fromString((String) value);
            }
            return value;
        }
        
        /**
         * Generate a random UUID (equivalent to PostgreSQL's gen_random_uuid()).
         * @return a new random UUID
         */
        public java.util.UUID genRandomUuid() {
            return com.memgres.functions.UuidFunctions.genRandomUuid();
        }
        
        /**
         * Generate a UUID version 1 (time-based).
         * @return a new time-based UUID
         */
        public java.util.UUID uuidGenerateV1() {
            return com.memgres.functions.UuidFunctions.uuidGenerateV1();
        }
        
        /**
         * Generate a UUID version 4 (random).
         * @return a new random UUID
         */
        public java.util.UUID uuidGenerateV4() {
            return com.memgres.functions.UuidFunctions.uuidGenerateV4();
        }
    },
    
    // JSONB type
    JSONB("jsonb") {
        @Override
        public boolean isValidValue(Object value) {
            return value instanceof JsonbValue || value instanceof String ||
                   value instanceof java.util.Map || value instanceof java.util.List;
        }
        
        @Override
        public Object convertValue(Object value) {
            if (value instanceof JsonbValue) {
                return value;
            }
            return JsonbValue.from(value);
        }
    },
    
    // Binary types
    BYTEA("bytea") {
        @Override
        public boolean isValidValue(Object value) {
            return value instanceof byte[];
        }
        
        @Override
        public Object convertValue(Object value) {
            if (value instanceof String) {
                return ((String) value).getBytes();
            }
            return value;
        }
    },
    
    BINARY("binary") {
        @Override
        public boolean isValidValue(Object value) {
            return value instanceof byte[];
        }
        
        @Override
        public Object convertValue(Object value) {
            if (value instanceof String) {
                String str = (String) value;
                if (str.startsWith("\\x") || str.startsWith("0x")) {
                    // Hex string format
                    str = str.substring(2);
                    return hexStringToByteArray(str);
                }
                return str.getBytes();
            }
            return value;
        }
    },
    
    VARBINARY("varbinary") {
        @Override
        public boolean isValidValue(Object value) {
            return value instanceof byte[];
        }
        
        @Override
        public Object convertValue(Object value) {
            if (value instanceof String) {
                String str = (String) value;
                if (str.startsWith("\\x") || str.startsWith("0x")) {
                    // Hex string format
                    str = str.substring(2);
                    return hexStringToByteArray(str);
                }
                return str.getBytes();
            }
            return value;
        }
    },
    
    CLOB("clob") {
        @Override
        public boolean isValidValue(Object value) {
            return value instanceof String || value instanceof java.sql.Clob;
        }
        
        @Override
        public Object convertValue(Object value) {
            if (value instanceof java.sql.Clob) {
                try {
                    java.sql.Clob clob = (java.sql.Clob) value;
                    return clob.getSubString(1, (int) clob.length());
                } catch (Exception e) {
                    throw new IllegalArgumentException("Failed to convert CLOB to string", e);
                }
            }
            return value != null ? value.toString() : null;
        }
    },
    
    BLOB("blob") {
        @Override
        public boolean isValidValue(Object value) {
            return value instanceof byte[] || value instanceof java.sql.Blob;
        }
        
        @Override
        public Object convertValue(Object value) {
            if (value instanceof java.sql.Blob) {
                try {
                    java.sql.Blob blob = (java.sql.Blob) value;
                    return blob.getBytes(1, (int) blob.length());
                } catch (Exception e) {
                    throw new IllegalArgumentException("Failed to convert BLOB to byte array", e);
                }
            }
            if (value instanceof String) {
                String str = (String) value;
                if (str.startsWith("\\x") || str.startsWith("0x")) {
                    // Hex string format
                    str = str.substring(2);
                    return hexStringToByteArray(str);
                }
                return str.getBytes();
            }
            return value;
        }
    },
    
    INTERVAL("interval") {
        @Override
        public boolean isValidValue(Object value) {
            return value instanceof Interval || value instanceof String;
        }
        
        @Override
        public Object convertValue(Object value) {
            if (value instanceof Interval) {
                return value;
            }
            if (value instanceof String) {
                return Interval.parse((String) value);
            }
            return value;
        }
    },
    
    // Array types
    INTEGER_ARRAY("integer[]") {
        @Override
        public boolean isValidValue(Object value) {
            if (value instanceof Integer[]) return true;
            if (value instanceof List<?>) {
                List<?> list = (List<?>) value;
                return list.stream().allMatch(item -> item == null || INTEGER.isValidValue(item));
            }
            return false;
        }
        
        @Override
        public Object convertValue(Object value) {
            if (value instanceof Integer[]) {
                return value;
            }
            if (value instanceof List<?>) {
                List<?> list = (List<?>) value;
                Integer[] array = new Integer[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    Object item = list.get(i);
                    array[i] = item == null ? null : (Integer) INTEGER.convertValue(item);
                }
                return array;
            }
            if (value instanceof String) {
                return parseArrayFromString((String) value, INTEGER);
            }
            return value;
        }
    },
    
    TEXT_ARRAY("text[]") {
        @Override
        public boolean isValidValue(Object value) {
            if (value instanceof String[]) return true;
            if (value instanceof List<?>) {
                List<?> list = (List<?>) value;
                return list.stream().allMatch(item -> item == null || TEXT.isValidValue(item));
            }
            return false;
        }
        
        @Override
        public Object convertValue(Object value) {
            if (value instanceof String[]) {
                return value;
            }
            if (value instanceof List<?>) {
                List<?> list = (List<?>) value;
                String[] array = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    Object item = list.get(i);
                    array[i] = item == null ? null : (String) TEXT.convertValue(item);
                }
                return array;
            }
            if (value instanceof String) {
                return parseArrayFromString((String) value, TEXT);
            }
            return value;
        }
    },
    
    UUID_ARRAY("uuid[]") {
        @Override
        public boolean isValidValue(Object value) {
            if (value instanceof UUID[]) return true;
            if (value instanceof List<?>) {
                List<?> list = (List<?>) value;
                return list.stream().allMatch(item -> item == null || UUID.isValidValue(item));
            }
            return false;
        }
        
        @Override
        public Object convertValue(Object value) {
            if (value instanceof UUID[]) {
                return value;
            }
            if (value instanceof List<?>) {
                List<?> list = (List<?>) value;
                java.util.UUID[] array = new java.util.UUID[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    Object item = list.get(i);
                    array[i] = item == null ? null : (java.util.UUID) UUID.convertValue(item);
                }
                return array;
            }
            if (value instanceof String) {
                return parseArrayFromString((String) value, UUID);
            }
            return value;
        }
    };
    
    private final String sqlName;
    
    DataType(String sqlName) {
        this.sqlName = sqlName;
    }
    
    /**
     * Get the SQL name of this data type
     * @return the SQL name
     */
    public String getSqlName() {
        return sqlName;
    }
    
    /**
     * Check if a value is valid for this data type
     * @param value the value to check
     * @return true if the value is valid
     */
    public abstract boolean isValidValue(Object value);
    
    /**
     * Convert a value to the appropriate type for this data type
     * @param value the value to convert
     * @return the converted value
     * @throws IllegalArgumentException if the value cannot be converted
     */
    public abstract Object convertValue(Object value);
    
    /**
     * Parse an array from a PostgreSQL-style string representation.
     * @param arrayString the string representation (e.g., "{1,2,3}" or "{'a','b','c'}")
     * @param elementType the element data type
     * @return the parsed array
     */
    @SuppressWarnings("unchecked")
    private static Object parseArrayFromString(String arrayString, DataType elementType) {
        String trimmed = arrayString.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            throw new IllegalArgumentException("Array string must be enclosed in braces: " + arrayString);
        }
        
        String content = trimmed.substring(1, trimmed.length() - 1).trim();
        if (content.isEmpty()) {
            // Empty array
            switch (elementType) {
                case INTEGER:
                    return new Integer[0];
                case TEXT:
                    return new String[0];
                case UUID:
                    return new java.util.UUID[0];
                default:
                    throw new IllegalArgumentException("Unsupported array element type: " + elementType);
            }
        }
        
        // Parse elements - simple implementation that handles basic cases
        List<String> elements = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '\'' && (i == 0 || content.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                elements.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        elements.add(current.toString().trim());
        
        // Convert elements to appropriate array type
        switch (elementType) {
            case INTEGER:
                Integer[] intArray = new Integer[elements.size()];
                for (int i = 0; i < elements.size(); i++) {
                    String element = elements.get(i);
                    if ("null".equalsIgnoreCase(element)) {
                        intArray[i] = null;
                    } else {
                        intArray[i] = (Integer) INTEGER.convertValue(element);
                    }
                }
                return intArray;
            case TEXT:
                String[] stringArray = new String[elements.size()];
                for (int i = 0; i < elements.size(); i++) {
                    String element = elements.get(i);
                    if ("null".equalsIgnoreCase(element)) {
                        stringArray[i] = null;
                    } else {
                        // Remove surrounding quotes if present
                        if (element.startsWith("'") && element.endsWith("'")) {
                            element = element.substring(1, element.length() - 1);
                        }
                        stringArray[i] = (String) TEXT.convertValue(element);
                    }
                }
                return stringArray;
            case UUID:
                java.util.UUID[] uuidArray = new java.util.UUID[elements.size()];
                for (int i = 0; i < elements.size(); i++) {
                    String element = elements.get(i);
                    if ("null".equalsIgnoreCase(element)) {
                        uuidArray[i] = null;
                    } else {
                        // Remove surrounding quotes if present
                        if (element.startsWith("'") && element.endsWith("'")) {
                            element = element.substring(1, element.length() - 1);
                        }
                        uuidArray[i] = (java.util.UUID) UUID.convertValue(element);
                    }
                }
                return uuidArray;
            default:
                throw new IllegalArgumentException("Unsupported array element type: " + elementType);
        }
    }
    
    /**
     * Convert a hex string to byte array
     * @param hexString the hex string (without 0x prefix)
     * @return the byte array
     */
    private static byte[] hexStringToByteArray(String hexString) {
        if (hexString.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have even length");
        }
        
        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            bytes[i / 2] = (byte) Integer.parseInt(hexString.substring(i, i + 2), 16);
        }
        return bytes;
    }
    
    /**
     * Get a data type by its SQL name
     * @param sqlName the SQL name (case-insensitive)
     * @return the matching data type or null if not found
     */
    public static DataType fromSqlName(String sqlName) {
        if (sqlName == null) {
            return null;
        }
        
        String normalized = sqlName.toLowerCase().trim();
        
        // Check exact matches first
        for (DataType dataType : values()) {
            if (dataType.sqlName.equals(normalized)) {
                return dataType;
            }
        }
        
        // Handle array types with alternative syntax
        if (normalized.endsWith("[]")) {
            String elementType = normalized.substring(0, normalized.length() - 2);
            switch (elementType) {
                case "integer": case "int": case "int4":
                    return INTEGER_ARRAY;
                case "text":
                    return TEXT_ARRAY;
                case "uuid":
                    return UUID_ARRAY;
            }
        }
        
        // Handle other aliases
        switch (normalized) {
            case "int": case "int4":
                return INTEGER;
            case "int2":
                return SMALLINT;
            case "int8":
                return BIGINT;
            case "float4":
                return REAL;
            case "float8":
                return DOUBLE_PRECISION;
            case "numeric":
                return DECIMAL;
            case "string":
                return VARCHAR;
            case "bool":
                return BOOLEAN;
            case "character large object":
                return CLOB;
            case "char large object":
                return CLOB;
            case "binary large object":
                return BLOB;
            case "binary varying":
                return VARBINARY;
            case "interval year":
            case "interval month":
            case "interval day":
            case "interval hour":
            case "interval minute":
            case "interval second":
            case "interval year to month":
            case "interval day to hour":
            case "interval day to minute":
            case "interval day to second":
            case "interval hour to minute":
            case "interval hour to second":
            case "interval minute to second":
                return INTERVAL;
            default:
                return null;
        }
    }
    
    /**
     * Check if this data type is an array type.
     * @return true if this is an array type
     */
    public boolean isArrayType() {
        return this == INTEGER_ARRAY || this == TEXT_ARRAY || this == UUID_ARRAY;
    }
    
    /**
     * Get the element type for array types.
     * @return the element type, or null if this is not an array type
     */
    public DataType getArrayElementType() {
        switch (this) {
            case INTEGER_ARRAY:
                return INTEGER;
            case TEXT_ARRAY:
                return TEXT;
            case UUID_ARRAY:
                return UUID;
            default:
                return null;
        }
    }
    
    @Override
    public String toString() {
        return sqlName;
    }
    
    /**
     * Utility methods for UUID generation that can be used statically
     */
    public static class UuidGenerator {
        /**
         * Generate a random UUID (equivalent to PostgreSQL's gen_random_uuid()).
         * @return a new random UUID
         */
        public static java.util.UUID genRandomUuid() {
            return com.memgres.functions.UuidFunctions.genRandomUuid();
        }
        
        /**
         * Generate a UUID version 1 (time-based).
         * @return a new time-based UUID
         */
        public static java.util.UUID uuidGenerateV1() {
            return com.memgres.functions.UuidFunctions.uuidGenerateV1();
        }
        
        /**
         * Generate a UUID version 4 (random).
         * @return a new random UUID
         */
        public static java.util.UUID uuidGenerateV4() {
            return com.memgres.functions.UuidFunctions.uuidGenerateV4();
        }
    }
    
    /**
     * Utility methods for string functions that can be used statically
     */
    public static class StringFunctions {
        /**
         * Concatenate strings (equivalent to PostgreSQL's CONCAT()).
         * @param strings the strings to concatenate (null values are treated as empty strings)
         * @return the concatenated string
         */
        public static String concat(Object... strings) {
            return com.memgres.functions.StringFunctions.concat(strings);
        }
        
        /**
         * Concatenate strings with a separator (equivalent to PostgreSQL's CONCAT_WS()).
         * @param separator the separator to use between strings
         * @param strings the strings to concatenate (null values are skipped)
         * @return the concatenated string with separators
         */
        public static String concatWs(String separator, Object... strings) {
            return com.memgres.functions.StringFunctions.concatWs(separator, strings);
        }
        
        /**
         * Get substring from a string (equivalent to PostgreSQL's SUBSTRING()).
         * @param string the source string
         * @param start the starting position (1-based, as in PostgreSQL)
         * @param length the length of substring (optional)
         * @return the substring
         */
        public static String substring(String string, int start, Integer length) {
            return com.memgres.functions.StringFunctions.substring(string, start, length);
        }
        
        /**
         * Get substring from a string (2-parameter version).
         * @param string the source string
         * @param start the starting position (1-based)
         * @return the substring from start to end
         */
        public static String substring(String string, int start) {
            return com.memgres.functions.StringFunctions.substring(string, start);
        }
        
        /**
         * Get the length of a string (equivalent to PostgreSQL's LENGTH()).
         * @param string the string to measure
         * @return the length of the string, or null if string is null
         */
        public static Integer length(String string) {
            return com.memgres.functions.StringFunctions.length(string);
        }
        
        /**
         * Convert string to uppercase (equivalent to PostgreSQL's UPPER()).
         * @param string the string to convert
         * @return the uppercase string
         */
        public static String upper(String string) {
            return com.memgres.functions.StringFunctions.upper(string);
        }
        
        /**
         * Convert string to lowercase (equivalent to PostgreSQL's LOWER()).
         * @param string the string to convert
         * @return the lowercase string
         */
        public static String lower(String string) {
            return com.memgres.functions.StringFunctions.lower(string);
        }
        
        /**
         * Trim whitespace from both ends (equivalent to PostgreSQL's TRIM()).
         * @param string the string to trim
         * @return the trimmed string
         */
        public static String trim(String string) {
            return com.memgres.functions.StringFunctions.trim(string);
        }
        
        /**
         * Replace occurrences of a substring (equivalent to PostgreSQL's REPLACE()).
         * @param string the source string
         * @param from the substring to replace
         * @param to the replacement string
         * @return the string with replacements made
         */
        public static String replace(String string, String from, String to) {
            return com.memgres.functions.StringFunctions.replace(string, from, to);
        }
        
        /**
         * Find position of substring in string (equivalent to PostgreSQL's POSITION()).
         * @param substring the substring to find
         * @param string the string to search in
         * @return the 1-based position of the substring, or 0 if not found
         */
        public static Integer position(String substring, String string) {
            return com.memgres.functions.StringFunctions.position(substring, string);
        }
        
        /**
         * Aggregate strings with separator (equivalent to PostgreSQL's STRING_AGG()).
         * @param strings the list of strings to aggregate
         * @param separator the separator
         * @return the aggregated string
         */
        public static String stringAgg(java.util.List<String> strings, String separator) {
            return com.memgres.functions.StringFunctions.stringAgg(strings, separator);
        }
        
        /**
         * Left-pad string to specified length (equivalent to PostgreSQL's LPAD()).
         * @param string the string to pad
         * @param length the target length
         * @param padString the padding string (default is space)
         * @return the padded string
         */
        public static String lpad(String string, int length, String padString) {
            return com.memgres.functions.StringFunctions.lpad(string, length, padString);
        }
        
        /**
         * Right-pad string to specified length (equivalent to PostgreSQL's RPAD()).
         * @param string the string to pad
         * @param length the target length
         * @param padString the padding string (default is space)
         * @return the padded string
         */
        public static String rpad(String string, int length, String padString) {
            return com.memgres.functions.StringFunctions.rpad(string, length, padString);
        }
    }
    
    /**
     * Utility methods for date/time functions that can be used statically
     */
    public static class DateTimeFunctions {
        /**
         * Get the current timestamp (equivalent to PostgreSQL's NOW()).
         * @return the current timestamp with timezone
         */
        public static ZonedDateTime now() {
            return com.memgres.functions.DateTimeFunctions.now();
        }
        
        /**
         * Get the current date (equivalent to PostgreSQL's CURRENT_DATE).
         * @return the current date
         */
        public static LocalDate currentDate() {
            return com.memgres.functions.DateTimeFunctions.currentDate();
        }
        
        /**
         * Get the current time (equivalent to PostgreSQL's CURRENT_TIME).
         * @return the current time with timezone
         */
        public static OffsetTime currentTime() {
            return com.memgres.functions.DateTimeFunctions.currentTime();
        }
        
        /**
         * Get the current timestamp (equivalent to PostgreSQL's CURRENT_TIMESTAMP).
         * @return the current timestamp with timezone
         */
        public static ZonedDateTime currentTimestamp() {
            return com.memgres.functions.DateTimeFunctions.currentTimestamp();
        }
        
        /**
         * Extract a field from a date/time value (equivalent to PostgreSQL's EXTRACT()).
         * @param field the field to extract (year, month, day, hour, minute, second, etc.)
         * @param datetime the date/time value to extract from
         * @return the extracted field value
         */
        public static Double extract(String field, java.time.temporal.TemporalAccessor datetime) {
            return com.memgres.functions.DateTimeFunctions.extract(field, datetime);
        }
        
        /**
         * Add an interval to a date/time value.
         * @param datetime the base date/time
         * @param interval the interval to add (e.g., "1 day", "2 hours", "3 months")
         * @return the resulting date/time
         */
        public static java.time.temporal.TemporalAccessor dateAdd(java.time.temporal.TemporalAccessor datetime, String interval) {
            return com.memgres.functions.DateTimeFunctions.dateAdd(datetime, interval);
        }
        
        /**
         * Format a date/time value as a string.
         * @param datetime the date/time to format
         * @param pattern the format pattern (PostgreSQL-style or Java DateTimeFormatter pattern)
         * @return the formatted string
         */
        public static String formatDateTime(java.time.temporal.TemporalAccessor datetime, String pattern) {
            return com.memgres.functions.DateTimeFunctions.formatDateTime(datetime, pattern);
        }
        
        /**
         * Calculate the age between two dates.
         * @param birthDate the birth date
         * @param currentDate the current date (can be null for current date)
         * @return the age in years
         */
        public static java.time.Period age(LocalDate birthDate, LocalDate currentDate) {
            return com.memgres.functions.DateTimeFunctions.age(birthDate, currentDate);
        }
    }
}