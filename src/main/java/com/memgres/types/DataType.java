package com.memgres.types;

import com.memgres.types.jsonb.JsonbValue;

import java.math.BigDecimal;
import java.time.*;
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
     * Get a data type by its SQL name
     * @param sqlName the SQL name (case-insensitive)
     * @return the matching data type or null if not found
     */
    public static DataType fromSqlName(String sqlName) {
        if (sqlName == null) {
            return null;
        }
        
        String normalized = sqlName.toLowerCase().trim();
        for (DataType dataType : values()) {
            if (dataType.sqlName.equals(normalized)) {
                return dataType;
            }
        }
        
        // Handle aliases
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
}