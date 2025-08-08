package com.memgres.types;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a database row with a unique ID and column data.
 * This class is immutable to ensure thread safety.
 */
public class Row {
    private final long id;
    private final Object[] data;
    
    /**
     * Create a new row with the specified ID and data
     * @param id the unique row identifier
     * @param data the column data (will be copied)
     */
    public Row(long id, Object[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Row data cannot be null");
        }
        
        this.id = id;
        this.data = data.clone(); // Defensive copy
    }
    
    /**
     * Get the row ID
     * @return the unique row identifier
     */
    public long getId() {
        return id;
    }
    
    /**
     * Get the row data
     * @return a copy of the column data array
     */
    public Object[] getData() {
        return data.clone(); // Return defensive copy
    }
    
    /**
     * Get the value at the specified column index
     * @param columnIndex the column index (0-based)
     * @return the value at the specified index
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public Object getValue(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= data.length) {
            throw new IndexOutOfBoundsException("Column index out of bounds: " + columnIndex);
        }
        return data[columnIndex];
    }
    
    /**
     * Get the number of columns in this row
     * @return the column count
     */
    public int getColumnCount() {
        return data.length;
    }
    
    /**
     * Check if the value at the specified column index is null
     * @param columnIndex the column index (0-based)
     * @return true if the value is null
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public boolean isNull(int columnIndex) {
        return getValue(columnIndex) == null;
    }
    
    /**
     * Create a new row with updated data at the specified column
     * @param columnIndex the column index to update
     * @param newValue the new value
     * @return a new row with the updated value
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public Row withUpdatedValue(int columnIndex, Object newValue) {
        if (columnIndex < 0 || columnIndex >= data.length) {
            throw new IndexOutOfBoundsException("Column index out of bounds: " + columnIndex);
        }
        
        Object[] newData = data.clone();
        newData[columnIndex] = newValue;
        return new Row(id, newData);
    }
    
    /**
     * Create a new row with the same data but different ID
     * @param newId the new row ID
     * @return a new row with the updated ID
     */
    public Row withId(long newId) {
        return new Row(newId, data);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Row row = (Row) o;
        return id == row.id && Arrays.equals(data, row.data);
    }
    
    @Override
    public int hashCode() {
        int result = Objects.hash(id);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }
    
    @Override
    public String toString() {
        return "Row{" +
                "id=" + id +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}