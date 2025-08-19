package com.memgres.storage;

import com.memgres.types.Column;
import com.memgres.types.DataType;
import com.memgres.types.Row;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for CompositeIndex functionality.
 */
public class CompositeIndexTest {
    
    private Table table;
    private CompositeIndex compositeIndex;
    
    @BeforeEach
    void setUp() {
        // Create test table with multiple columns
        List<Column> columns = Arrays.asList(
            Column.of("id", DataType.INTEGER),
            Column.of("last_name", DataType.VARCHAR),
            Column.of("first_name", DataType.VARCHAR),
            Column.of("age", DataType.INTEGER),
            Column.of("city", DataType.VARCHAR)
        );
        
        table = new Table("users", columns);
        
        // Insert test data
        table.insertRow(new Object[]{1, "Smith", "John", 30, "New York"});
        table.insertRow(new Object[]{2, "Johnson", "Jane", 25, "Los Angeles"});
        table.insertRow(new Object[]{3, "Smith", "Bob", 35, "New York"});
        table.insertRow(new Object[]{4, "Brown", "Alice", 28, "Chicago"});
        table.insertRow(new Object[]{5, "Smith", "John", 32, "Boston"}); // Different John Smith
        
        // Create composite index on (last_name, first_name) using the table's method
        List<String> columnNames = Arrays.asList("last_name", "first_name");
        table.createCompositeIndex("idx_name", columnNames, false, false);
        compositeIndex = table.getCompositeIndex("idx_name");
    }
    
    @Test
    void testExactMatch() {
        // Find exact match for "Smith, John"
        Set<Long> results = compositeIndex.findExact("Smith", "John");
        
        assertEquals(2, results.size(), "Should find 2 John Smiths");
        assertTrue(results.contains(1L), "Should include first John Smith");
        assertTrue(results.contains(5L), "Should include second John Smith");
    }
    
    @Test
    void testExactMatchNotFound() {
        // Find exact match that doesn't exist
        Set<Long> results = compositeIndex.findExact("Wilson", "Tom");
        
        assertTrue(results.isEmpty(), "Should find no results for non-existent name");
    }
    
    @Test
    void testPrefixMatch() {
        // Find all people with last name "Smith"
        Set<Long> results = compositeIndex.findPrefix("Smith");
        
        assertEquals(3, results.size(), "Should find 3 Smiths");
        assertTrue(results.contains(1L), "Should include John Smith (id=1)");
        assertTrue(results.contains(3L), "Should include Bob Smith");
        assertTrue(results.contains(5L), "Should include John Smith (id=5)");
    }
    
    @Test
    void testInsertAndFind() {
        // Add a new person
        long newRowId = table.insertRow(new Object[]{6, "Davis", "Mike", 27, "Seattle"});
        
        // Verify the index was updated
        Set<Long> results = compositeIndex.findExact("Davis", "Mike");
        assertEquals(1, results.size(), "Should find newly inserted Mike Davis");
        assertTrue(results.contains(newRowId), "Should contain the correct row ID");
    }
    
    @Test
    void testUpdateAndFind() {
        // Update a person's name
        table.updateRow(1L, new Object[]{1, "Johnson", "John", 30, "New York"});
        
        // Verify old key is gone
        Set<Long> oldResults = compositeIndex.findExact("Smith", "John");
        assertEquals(1, oldResults.size(), "Should only find one John Smith after update");
        assertFalse(oldResults.contains(1L), "Should not contain updated row in old key");
        
        // Verify new key exists
        Set<Long> newResults = compositeIndex.findExact("Johnson", "John");
        assertEquals(1, newResults.size(), "Should find John Johnson after update");
        assertTrue(newResults.contains(1L), "Should contain updated row in new key");
    }
    
    @Test
    void testDeleteAndFind() {
        // Delete a person
        table.deleteRow(3L); // Delete Bob Smith
        
        // Verify the index was updated
        Set<Long> results = compositeIndex.findPrefix("Smith");
        assertEquals(2, results.size(), "Should find 2 Smiths after deletion");
        assertFalse(results.contains(3L), "Should not contain deleted Bob Smith");
    }
    
    @Test
    void testUniqueConstraint() {
        // This should fail because we already have duplicate (Smith, John) entries
        assertThrows(IllegalStateException.class, () -> {
            table.createCompositeIndex("idx_unique_name", Arrays.asList("last_name", "first_name"), true, false);
        }, "Should throw exception when creating unique index on duplicate data");
    }
    
    @Test
    void testUniqueConstraintOnInsert() {
        // Create table with unique data
        Table uniqueTable = new Table("unique_users", table.getColumns());
        uniqueTable.insertRow(new Object[]{1, "Smith", "John", 30, "New York"});
        uniqueTable.insertRow(new Object[]{2, "Johnson", "Jane", 25, "Los Angeles"});
        
        uniqueTable.createCompositeIndex("idx_unique_name", Arrays.asList("last_name", "first_name"), true, false);
        CompositeIndex uniqueIndex = uniqueTable.getCompositeIndex("idx_unique_name");
        
        // Try to insert duplicate
        uniqueTable.insertRow(new Object[]{3, "Brown", "Alice", 28, "Chicago"});
        
        // This should fail
        assertThrows(IllegalStateException.class, () -> {
            uniqueTable.insertRow(new Object[]{4, "Smith", "John", 35, "Boston"});
        }, "Should throw exception when inserting duplicate key in unique index");
    }
    
    @Test
    void testIndexStatistics() {
        assertEquals(4, compositeIndex.getKeyCount(), 
                "Should have 4 unique keys: (Smith,John), (Johnson,Jane), (Smith,Bob), (Brown,Alice)");
        assertEquals(5, compositeIndex.getTotalRowCount(), 
                "Should have 5 total row references");
    }
    
    @Test
    void testInvalidArguments() {
        // Test invalid number of values for exact match
        assertThrows(IllegalArgumentException.class, () -> {
            compositeIndex.findExact("Smith"); // Missing first name
        }, "Should throw exception for incomplete exact match");
        
        assertThrows(IllegalArgumentException.class, () -> {
            compositeIndex.findExact("Smith", "John", "Extra"); // Too many values
        }, "Should throw exception for too many exact match values");
        
        // Test invalid prefix length
        assertThrows(IllegalArgumentException.class, () -> {
            compositeIndex.findPrefix(); // Empty prefix
        }, "Should throw exception for empty prefix");
        
        assertThrows(IllegalArgumentException.class, () -> {
            compositeIndex.findPrefix("Smith", "John", "Extra"); // Prefix longer than index
        }, "Should throw exception for prefix longer than index");
    }
    
    @Test
    void testCompositeKeyComparison() {
        CompositeIndex.CompositeKey key1 = new CompositeIndex.CompositeKey(Arrays.asList("Smith", "John"));
        CompositeIndex.CompositeKey key2 = new CompositeIndex.CompositeKey(Arrays.asList("Smith", "John"));
        CompositeIndex.CompositeKey key3 = new CompositeIndex.CompositeKey(Arrays.asList("Smith", "Bob"));
        CompositeIndex.CompositeKey key4 = new CompositeIndex.CompositeKey(Arrays.asList("Johnson", "Jane"));
        
        // Test equality
        assertEquals(key1, key2, "Identical keys should be equal");
        assertNotEquals(key1, key3, "Different keys should not be equal");
        
        // Test comparison
        assertTrue(key1.compareTo(key3) > 0, "John should come after Bob");
        assertTrue(key4.compareTo(key1) < 0, "Johnson should come before Smith");
        assertEquals(0, key1.compareTo(key2), "Identical keys should compare as equal");
    }
}