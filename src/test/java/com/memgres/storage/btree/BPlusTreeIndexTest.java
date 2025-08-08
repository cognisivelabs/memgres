package com.memgres.storage.btree;

import com.memgres.storage.Schema;
import com.memgres.storage.Table;
import com.memgres.types.Column;
import com.memgres.types.DataType;
import com.memgres.types.Row;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for B+ Tree Index integration
 */
public class BPlusTreeIndexTest {
    
    private Schema schema;
    private Table table;
    private Column idColumn;
    private Column nameColumn;
    private Column ageColumn;
    private BPlusTreeIndex nameIndex;
    private BPlusTreeIndex ageIndex;
    
    @BeforeEach
    void setUp() {
        schema = new Schema("testdb");
        
        // Create table with columns
        idColumn = new Column.Builder()
                .name("id")
                .dataType(DataType.INTEGER)
                .nullable(false)
                .primaryKey(true)
                .build();
        nameColumn = new Column.Builder()
                .name("name")
                .dataType(DataType.VARCHAR)
                .nullable(false)
                .build();
        ageColumn = new Column.Builder()
                .name("age")
                .dataType(DataType.INTEGER)
                .nullable(true)
                .build();
        
        List<Column> columns = Arrays.asList(idColumn, nameColumn, ageColumn);
        table = new Table("users", columns);
        
        // Create B+ tree indexes
        nameIndex = new BPlusTreeIndex("idx_name", nameColumn, table, 4);
        ageIndex = new BPlusTreeIndex("idx_age", ageColumn, table, 4);
    }
    
    @Test
    @DisplayName("Index creation and basic properties")
    void testIndexCreation() {
        assertEquals("idx_name", nameIndex.getName());
        assertEquals(nameColumn, nameIndex.getIndexedColumn());
        assertEquals(table, nameIndex.getTable());
        assertEquals(0, nameIndex.getEntryCount());
        assertEquals(0, nameIndex.getTotalRowCount());
        
        assertNotNull(nameIndex.getBTree());
        assertEquals(4, nameIndex.getBTree().getOrder());
    }
    
    @Test
    @DisplayName("Invalid index creation parameters")
    void testInvalidIndexCreation() {
        assertThrows(IllegalArgumentException.class, 
                    () -> new BPlusTreeIndex(null, nameColumn, table));
        assertThrows(IllegalArgumentException.class, 
                    () -> new BPlusTreeIndex("", nameColumn, table));
        assertThrows(IllegalArgumentException.class, 
                    () -> new BPlusTreeIndex("test", null, table));
        assertThrows(IllegalArgumentException.class, 
                    () -> new BPlusTreeIndex("test", nameColumn, null));
        assertThrows(IllegalArgumentException.class, 
                    () -> new BPlusTreeIndex("test", nameColumn, table, 2));
    }
    
    @Test
    @DisplayName("Insert rows and verify index updates")
    void testInsertAndIndexUpdate() {
        // Insert rows into table
        long row1Id = table.insertRow(new Object[]{1, "Alice", 25});
        long row2Id = table.insertRow(new Object[]{2, "Bob", 30});
        long row3Id = table.insertRow(new Object[]{3, "Alice", 28}); // Duplicate name
        
        Row row1 = table.getRow(row1Id);
        Row row2 = table.getRow(row2Id);
        Row row3 = table.getRow(row3Id);
        
        // Update indexes
        nameIndex.insert(row1);
        nameIndex.insert(row2);
        nameIndex.insert(row3);
        
        ageIndex.insert(row1);
        ageIndex.insert(row2);
        ageIndex.insert(row3);
        
        // Verify index statistics
        assertEquals(2, nameIndex.getEntryCount()); // Alice, Bob
        assertEquals(3, nameIndex.getTotalRowCount()); // 3 row references
        assertEquals(3, ageIndex.getEntryCount()); // 25, 30, 28
        assertEquals(3, ageIndex.getTotalRowCount());
        
        // Test exact searches
        Set<Long> aliceRows = nameIndex.findEqual("Alice");
        assertEquals(2, aliceRows.size());
        assertTrue(aliceRows.contains(row1.getId()));
        assertTrue(aliceRows.contains(row3.getId()));
        
        Set<Long> bobRows = nameIndex.findEqual("Bob");
        assertEquals(1, bobRows.size());
        assertTrue(bobRows.contains(row2.getId()));
        
        Set<Long> age25Rows = ageIndex.findEqual(25);
        assertEquals(1, age25Rows.size());
        assertTrue(age25Rows.contains(row1.getId()));
    }
    
    @Test
    @DisplayName("Range queries work correctly")
    void testRangeQueries() {
        // Insert test data
        for (int i = 1; i <= 10; i++) {
            long rowId = table.insertRow(new Object[]{i, "User" + i, 20 + i});
            Row row = table.getRow(rowId);
            ageIndex.insert(row);
        }
        
        // Test age range 23-27 (inclusive)
        Set<Long> ageRange = ageIndex.findRange(23, 27);
        assertEquals(5, ageRange.size()); // Ages 23, 24, 25, 26, 27
        
        // Test less than queries
        Set<Long> youngUsers = ageIndex.findLessThan(25);
        assertEquals(4, youngUsers.size()); // Ages 21, 22, 23, 24
        
        // Test greater than queries
        Set<Long> olderUsers = ageIndex.findGreaterThan(27);
        assertEquals(3, olderUsers.size()); // Ages 28, 29, 30
        
        // Test less than or equal
        Set<Long> youngOrEqual = ageIndex.findLessThanOrEqual(25);
        assertEquals(5, youngOrEqual.size()); // Ages 21, 22, 23, 24, 25
        
        // Test greater than or equal
        Set<Long> olderOrEqual = ageIndex.findGreaterThanOrEqual(27);
        assertEquals(4, olderOrEqual.size()); // Ages 27, 28, 29, 30
    }
    
    @Test
    @DisplayName("Delete operations update index correctly")
    void testDeleteOperations() {
        // Insert and index rows
        long row1Id = table.insertRow(new Object[]{1, "Alice", 25});
        long row2Id = table.insertRow(new Object[]{2, "Bob", 30});
        long row3Id = table.insertRow(new Object[]{3, "Alice", 28});
        
        Row row1 = table.getRow(row1Id);
        Row row2 = table.getRow(row2Id);
        Row row3 = table.getRow(row3Id);
        
        nameIndex.insert(row1);
        nameIndex.insert(row2);
        nameIndex.insert(row3);
        
        // Delete one Alice row
        nameIndex.delete(row1);
        
        // Verify Alice still has one entry
        Set<Long> aliceRows = nameIndex.findEqual("Alice");
        assertEquals(1, aliceRows.size());
        assertTrue(aliceRows.contains(row3.getId()));
        assertFalse(aliceRows.contains(row1.getId()));
        
        // Delete the other Alice row
        nameIndex.delete(row3);
        
        // Verify Alice has no entries
        assertTrue(nameIndex.findEqual("Alice").isEmpty());
        
        // Verify Bob is still there
        Set<Long> bobRows = nameIndex.findEqual("Bob");
        assertEquals(1, bobRows.size());
        assertTrue(bobRows.contains(row2.getId()));
        
        // Verify statistics
        assertEquals(1, nameIndex.getEntryCount()); // Only Bob
        assertEquals(1, nameIndex.getTotalRowCount());
    }
    
    @Test
    @DisplayName("Update operations work correctly")
    void testUpdateOperations() {
        // Insert and index a row
        long oldRowId = table.insertRow(new Object[]{1, "Alice", 25});
        Row oldRow = table.getRow(oldRowId);
        nameIndex.insert(oldRow);
        ageIndex.insert(oldRow);
        
        // Create updated row
        Row newRow = new Row(oldRow.getId(), new Object[]{1, "Alicia", 26});
        
        // Update indexes
        nameIndex.update(oldRow, newRow);
        ageIndex.update(oldRow, newRow);
        
        // Verify old values are gone
        assertTrue(nameIndex.findEqual("Alice").isEmpty());
        assertTrue(ageIndex.findEqual(25).isEmpty());
        
        // Verify new values are present
        Set<Long> aliciaRows = nameIndex.findEqual("Alicia");
        assertEquals(1, aliciaRows.size());
        assertTrue(aliciaRows.contains(newRow.getId()));
        
        Set<Long> age26Rows = ageIndex.findEqual(26);
        assertEquals(1, age26Rows.size());
        assertTrue(age26Rows.contains(newRow.getId()));
    }
    
    @Test
    @DisplayName("Null values are handled correctly")
    void testNullValueHandling() {
        // Create a row with null age
        long rowId = table.insertRow(new Object[]{1, "Alice", null});
        Row row = table.getRow(rowId);
        
        // Null values should not be indexed
        ageIndex.insert(row);
        
        assertEquals(0, ageIndex.getEntryCount());
        assertEquals(0, ageIndex.getTotalRowCount());
        
        // Searching for null should return empty set
        assertTrue(ageIndex.findEqual(null).isEmpty());
        
        // Delete with null should not cause issues
        ageIndex.delete(row);
    }
    
    @Test
    @DisplayName("Clear operation removes all entries")
    void testClearOperation() {
        // Insert test data
        for (int i = 1; i <= 5; i++) {
            long rowId = table.insertRow(new Object[]{i, "User" + i, 20 + i});
            Row row = table.getRow(rowId);
            nameIndex.insert(row);
        }
        
        assertEquals(5, nameIndex.getEntryCount());
        assertEquals(5, nameIndex.getTotalRowCount());
        
        // Clear the index
        nameIndex.clear();
        
        assertEquals(0, nameIndex.getEntryCount());
        assertEquals(0, nameIndex.getTotalRowCount());
        assertTrue(nameIndex.getAllKeys().isEmpty());
        
        // Verify searches return empty
        assertTrue(nameIndex.findEqual("User1").isEmpty());
    }
    
    @Test
    @DisplayName("Rebuild operation reconstructs index")
    void testRebuildOperation() {
        // Insert rows into table (but not index yet)
        long row1Id = table.insertRow(new Object[]{1, "Alice", 25});
        long row2Id = table.insertRow(new Object[]{2, "Bob", 30});
        long row3Id = table.insertRow(new Object[]{3, "Charlie", 35});
        
        Row row1 = table.getRow(row1Id);
        Row row2 = table.getRow(row2Id);
        Row row3 = table.getRow(row3Id);
        
        // Initially empty index
        assertEquals(0, nameIndex.getEntryCount());
        
        // Rebuild should populate from table
        nameIndex.rebuild();
        
        assertEquals(3, nameIndex.getEntryCount());
        assertEquals(3, nameIndex.getTotalRowCount());
        
        // Verify all entries are found
        assertFalse(nameIndex.findEqual("Alice").isEmpty());
        assertFalse(nameIndex.findEqual("Bob").isEmpty());
        assertFalse(nameIndex.findEqual("Charlie").isEmpty());
    }
    
    @Test
    @DisplayName("Get all keys returns sorted list")
    void testGetAllKeys() {
        // Insert data in random order
        String[] names = {"Zebra", "Alice", "Bob", "Charlie", "David"};
        for (int i = 0; i < names.length; i++) {
            long rowId = table.insertRow(new Object[]{i + 1, names[i], 20 + i});
            Row row = table.getRow(rowId);
            nameIndex.insert(row);
        }
        
        List<Comparable> keys = nameIndex.getAllKeys();
        assertEquals(5, keys.size());
        
        // Verify sorted order
        List<String> sortedNames = Arrays.asList("Alice", "Bob", "Charlie", "David", "Zebra");
        for (int i = 0; i < keys.size(); i++) {
            assertEquals(sortedNames.get(i), keys.get(i));
        }
    }
    
    @Test
    @DisplayName("Statistics provide accurate information")
    void testStatistics() {
        // Insert test data
        for (int i = 1; i <= 10; i++) {
            long rowId = table.insertRow(new Object[]{i, "User" + i, 20 + i});
            Row row = table.getRow(rowId);
            nameIndex.insert(row);
        }
        
        Map<String, Object> stats = nameIndex.getStatistics();
        
        assertEquals("idx_name", stats.get("name"));
        assertEquals("name", stats.get("columnName"));
        assertEquals("users", stats.get("tableName"));
        assertEquals(1, stats.get("columnIndex")); // name is at index 1
        assertEquals("BPlusTreeIndex", stats.get("type"));
        assertEquals(10L, stats.get("size")); // From B+ tree stats
        assertEquals(4, stats.get("order"));
        assertTrue(stats.containsKey("height"));
        assertTrue(stats.containsKey("leafNodeCount"));
    }
    
    @Test
    @DisplayName("Index works with different column types")
    void testDifferentColumnTypes() {
        // Test with string column (already done above)
        // Test with integer column
        for (int i = 1; i <= 10; i++) {
            long rowId = table.insertRow(new Object[]{i, "User" + i, 20 + i});
            Row row = table.getRow(rowId);
            ageIndex.insert(row);
        }
        
        // Test range query on integers
        Set<Long> result = ageIndex.findRange(23, 27);
        assertEquals(5, result.size());
        
        // Test with non-comparable values (should be ignored)
        Column objColumn = new Column.Builder()
                .name("data")
                .dataType(DataType.VARCHAR)
                .nullable(true)
                .build();
        List<Column> newColumns = Arrays.asList(idColumn, objColumn);
        Table newTable = new Table("test_table", newColumns);
        
        // This should work but ignore non-comparable values
        BPlusTreeIndex objIndex = new BPlusTreeIndex("idx_obj", objColumn, newTable);
        
        long rowId = newTable.insertRow(new Object[]{1, "comparable_string"});
        Row row = newTable.getRow(rowId);
        objIndex.insert(row);
        
        assertEquals(1, objIndex.getEntryCount());
    }
    
    @Test
    @DisplayName("Column not found in table throws exception")
    void testColumnNotFound() {
        Column orphanColumn = new Column.Builder()
                .name("orphan")
                .dataType(DataType.INTEGER)
                .nullable(false)
                .build();
        
        assertThrows(IllegalArgumentException.class, 
                    () -> new BPlusTreeIndex("idx_orphan", orphanColumn, table));
    }
    
    @Test
    @DisplayName("ToString provides meaningful output")
    void testToString() {
        long rowId = table.insertRow(new Object[]{1, "Alice", 25});
        Row row = table.getRow(rowId);
        nameIndex.insert(row);
        
        String str = nameIndex.toString();
        assertTrue(str.contains("BPlusTreeIndex"));
        assertTrue(str.contains("idx_name"));
        assertTrue(str.contains("name"));
        assertTrue(str.contains("users"));
        assertTrue(str.contains("entries=1"));
        assertTrue(str.contains("totalRows=1"));
    }
}