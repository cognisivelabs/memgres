package com.memgres.types;

import java.util.Objects;

/**
 * Represents a database column with name, data type, and constraints.
 * This class is immutable to ensure thread safety.
 */
public class Column {
    private final String name;
    private final DataType dataType;
    private final boolean nullable;
    private final boolean primaryKey;
    private final boolean unique;
    private final Object defaultValue;
    private final int maxLength;
    
    /**
     * Builder class for creating Column instances
     */
    public static class Builder {
        private String name;
        private DataType dataType;
        private boolean nullable = true;
        private boolean primaryKey = false;
        private boolean unique = false;
        private Object defaultValue = null;
        private int maxLength = -1;
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder dataType(DataType dataType) {
            this.dataType = dataType;
            return this;
        }
        
        public Builder nullable(boolean nullable) {
            this.nullable = nullable;
            return this;
        }
        
        public Builder primaryKey(boolean primaryKey) {
            this.primaryKey = primaryKey;
            if (primaryKey) {
                this.nullable = false; // Primary keys cannot be null
                this.unique = true; // Primary keys are unique
            }
            return this;
        }
        
        public Builder unique(boolean unique) {
            this.unique = unique;
            return this;
        }
        
        public Builder defaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }
        
        public Builder maxLength(int maxLength) {
            this.maxLength = maxLength;
            return this;
        }
        
        public Column build() {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Column name cannot be null or empty");
            }
            if (dataType == null) {
                throw new IllegalArgumentException("Column data type cannot be null");
            }
            
            return new Column(this);
        }
    }
    
    private Column(Builder builder) {
        this.name = builder.name.toLowerCase(); // PostgreSQL converts column names to lowercase
        this.dataType = builder.dataType;
        this.nullable = builder.nullable;
        this.primaryKey = builder.primaryKey;
        this.unique = builder.unique;
        this.defaultValue = builder.defaultValue;
        this.maxLength = builder.maxLength;
        
        // Validate default value against data type
        if (defaultValue != null && !dataType.isValidValue(defaultValue)) {
            throw new IllegalArgumentException("Default value is not compatible with column data type");
        }
    }
    
    /**
     * Create a simple column with name and data type
     * @param name the column name
     * @param dataType the data type
     * @return a new column
     */
    public static Column of(String name, DataType dataType) {
        return new Builder()
                .name(name)
                .dataType(dataType)
                .build();
    }
    
    /**
     * Create a not-null column
     * @param name the column name
     * @param dataType the data type
     * @return a new not-null column
     */
    public static Column notNull(String name, DataType dataType) {
        return new Builder()
                .name(name)
                .dataType(dataType)
                .nullable(false)
                .build();
    }
    
    /**
     * Create a primary key column
     * @param name the column name
     * @param dataType the data type
     * @return a new primary key column
     */
    public static Column primaryKey(String name, DataType dataType) {
        return new Builder()
                .name(name)
                .dataType(dataType)
                .primaryKey(true)
                .build();
    }
    
    /**
     * Get the column name
     * @return the column name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the data type
     * @return the data type
     */
    public DataType getDataType() {
        return dataType;
    }
    
    /**
     * Check if the column allows null values
     * @return true if nullable
     */
    public boolean isNullable() {
        return nullable;
    }
    
    /**
     * Check if the column is a primary key
     * @return true if primary key
     */
    public boolean isPrimaryKey() {
        return primaryKey;
    }
    
    /**
     * Check if the column has a unique constraint
     * @return true if unique
     */
    public boolean isUnique() {
        return unique;
    }
    
    /**
     * Get the default value
     * @return the default value or null if none
     */
    public Object getDefaultValue() {
        return defaultValue;
    }
    
    /**
     * Get the maximum length for variable-length data types
     * @return the maximum length or -1 if not applicable
     */
    public int getMaxLength() {
        return maxLength;
    }
    
    /**
     * Check if the column has a default value
     * @return true if a default value is set
     */
    public boolean hasDefaultValue() {
        return defaultValue != null;
    }
    
    /**
     * Validate a value against this column's constraints
     * @param value the value to validate
     * @return true if the value is valid for this column
     */
    public boolean isValidValue(Object value) {
        // Check null constraint
        if (value == null) {
            return nullable;
        }
        
        // Check data type compatibility
        if (!dataType.isValidValue(value)) {
            return false;
        }
        
        // Check length constraint for string types
        if (maxLength > 0 && dataType == DataType.VARCHAR && value instanceof String) {
            return ((String) value).length() <= maxLength;
        }
        
        return true;
    }
    
    /**
     * Get the effective value for this column, applying default if necessary
     * @param providedValue the provided value (may be null)
     * @return the effective value to use
     */
    public Object getEffectiveValue(Object providedValue) {
        if (providedValue != null) {
            return providedValue;
        }
        
        if (hasDefaultValue()) {
            return defaultValue;
        }
        
        if (!nullable) {
            throw new IllegalArgumentException("Column " + name + " cannot be null and has no default value");
        }
        
        return null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Column column = (Column) o;
        return nullable == column.nullable &&
                primaryKey == column.primaryKey &&
                unique == column.unique &&
                maxLength == column.maxLength &&
                Objects.equals(name, column.name) &&
                dataType == column.dataType &&
                Objects.equals(defaultValue, column.defaultValue);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, dataType, nullable, primaryKey, unique, defaultValue, maxLength);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Column{name='").append(name).append('\'');
        sb.append(", dataType=").append(dataType);
        if (!nullable) sb.append(", NOT NULL");
        if (primaryKey) sb.append(", PRIMARY KEY");
        if (unique && !primaryKey) sb.append(", UNIQUE");
        if (defaultValue != null) sb.append(", DEFAULT=").append(defaultValue);
        if (maxLength > 0) sb.append(", maxLength=").append(maxLength);
        sb.append('}');
        return sb.toString();
    }
}