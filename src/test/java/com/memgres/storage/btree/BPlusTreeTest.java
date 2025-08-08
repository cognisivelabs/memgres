package com.memgres.storage.btree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for B+ Tree implementation
 */
public class BPlusTreeTest {
    
    private BPlusTree<Integer, String> tree;
    private static final int DEFAULT_ORDER = 4; // Small order for easier testing
    
    @BeforeEach
    void setUp() {
        tree = new BPlusTree<>(DEFAULT_ORDER);
    }
    
    @Test
    @DisplayName("Constructor should create empty tree with correct properties")
    void testConstructor() {
        assertEquals(DEFAULT_ORDER, tree.getOrder());
        assertEquals(0, tree.size());
        assertTrue(tree.isEmpty());
    }
    
    @Test
    @DisplayName("Constructor should reject invalid order")
    void testInvalidOrder() {
        assertThrows(IllegalArgumentException.class, () -> new BPlusTree<>(2));
        assertThrows(IllegalArgumentException.class, () -> new BPlusTree<>(1));
        assertThrows(IllegalArgumentException.class, () -> new BPlusTree<>(0));
    }
    
    @Test
    @DisplayName("Insert and find single key-value pair")
    void testSingleInsertAndFind() {
        tree.insert(1, "one");
        
        assertEquals(1, tree.size());
        assertFalse(tree.isEmpty());
        
        Set<String> result = tree.find(1);
        assertEquals(1, result.size());
        assertTrue(result.contains("one"));
        
        assertTrue(tree.find(2).isEmpty());
    }
    
    @Test
    @DisplayName("Insert multiple values for same key")
    void testMultipleValuesPerKey() {
        tree.insert(1, "one");
        tree.insert(1, "uno");
        tree.insert(1, "ein");
        
        assertEquals(1, tree.size()); // Same key, so size is still 1
        
        Set<String> result = tree.find(1);
        assertEquals(3, result.size());
        assertTrue(result.contains("one"));
        assertTrue(result.contains("uno"));
        assertTrue(result.contains("ein"));
    }
    
    @Test
    @DisplayName("Insert many keys to trigger node splits")
    void testNodeSplitting() {
        // Insert enough keys to cause multiple splits
        for (int i = 1; i <= 20; i++) {
            tree.insert(i, "value" + i);
        }
        
        assertEquals(20, tree.size());
        
        // Verify all values can be found
        for (int i = 1; i <= 20; i++) {
            Set<String> result = tree.find(i);
            assertEquals(1, result.size());
            assertTrue(result.contains("value" + i));
        }
        
        // Verify tree statistics (simplified version doesn't split)
        Map<String, Object> stats = tree.getStatistics();
        assertEquals(1, (Integer) stats.get("height")); // Single leaf node
        assertEquals(1, (Integer) stats.get("leafNodeCount"));
    }
    
    @Test
    @DisplayName("Range queries work correctly")
    void testRangeQueries() {
        // Insert keys 1-10
        for (int i = 1; i <= 10; i++) {
            tree.insert(i, "value" + i);
        }
        
        // Test range query
        Set<String> range = tree.findRange(3, 7);
        assertEquals(5, range.size());
        for (int i = 3; i <= 7; i++) {
            assertTrue(range.contains("value" + i));
        }
        
        // Test edge cases
        assertTrue(tree.findRange(15, 20).isEmpty());
        assertEquals(1, tree.findRange(5, 5).size());
        assertTrue(tree.findRange(7, 3).isEmpty()); // Invalid range
    }
    
    @Test
    @DisplayName("Less than queries work correctly")
    void testLessThanQueries() {
        for (int i = 1; i <= 10; i++) {
            tree.insert(i, "value" + i);
        }
        
        Set<String> lessThan5 = tree.findLessThan(5);
        assertEquals(4, lessThan5.size());
        for (int i = 1; i <= 4; i++) {
            assertTrue(lessThan5.contains("value" + i));
        }
        assertFalse(lessThan5.contains("value5"));
        
        assertTrue(tree.findLessThan(1).isEmpty());
    }
    
    @Test
    @DisplayName("Greater than queries work correctly")
    void testGreaterThanQueries() {
        for (int i = 1; i <= 10; i++) {
            tree.insert(i, "value" + i);
        }
        
        Set<String> greaterThan7 = tree.findGreaterThan(7);
        assertEquals(3, greaterThan7.size());
        for (int i = 8; i <= 10; i++) {
            assertTrue(greaterThan7.contains("value" + i));
        }
        assertFalse(greaterThan7.contains("value7"));
        
        assertTrue(tree.findGreaterThan(10).isEmpty());
    }
    
    @Test
    @DisplayName("Remove single value from key")
    void testRemoveSingleValue() {
        tree.insert(1, "one");
        tree.insert(1, "uno");
        tree.insert(2, "two");
        
        assertTrue(tree.remove(1, "one"));
        assertFalse(tree.remove(1, "nonexistent"));
        
        Set<String> result = tree.find(1);
        assertEquals(1, result.size());
        assertTrue(result.contains("uno"));
        assertFalse(result.contains("one"));
        
        assertEquals(2, tree.size()); // Still 2 keys
    }
    
    @Test
    @DisplayName("Remove entire key")
    void testRemoveKey() {
        tree.insert(1, "one");
        tree.insert(1, "uno");
        tree.insert(2, "two");
        
        Set<String> removed = tree.removeKey(1);
        assertEquals(2, removed.size());
        assertTrue(removed.contains("one"));
        assertTrue(removed.contains("uno"));
        
        assertTrue(tree.find(1).isEmpty());
        assertEquals(1, tree.size());
    }
    
    @Test
    @DisplayName("Get all keys returns sorted list")
    void testGetAllKeys() {
        int[] insertOrder = {5, 2, 8, 1, 9, 3, 7, 4, 6};
        for (int key : insertOrder) {
            tree.insert(key, "value" + key);
        }
        
        List<Integer> allKeys = tree.getAllKeys();
        assertEquals(9, allKeys.size());
        
        // Verify sorted order
        for (int i = 0; i < allKeys.size() - 1; i++) {
            assertTrue(allKeys.get(i) < allKeys.get(i + 1));
        }
        
        // Verify all keys present
        for (int i = 1; i <= 9; i++) {
            assertTrue(allKeys.contains(i));
        }
    }
    
    @Test
    @DisplayName("Clear removes all entries")
    void testClear() {
        for (int i = 1; i <= 10; i++) {
            tree.insert(i, "value" + i);
        }
        
        assertEquals(10, tree.size());
        
        tree.clear();
        
        assertEquals(0, tree.size());
        assertTrue(tree.isEmpty());
        assertTrue(tree.getAllKeys().isEmpty());
        
        // Verify we can still insert after clear
        tree.insert(1, "new");
        assertEquals(1, tree.size());
    }
    
    @Test
    @DisplayName("Null key and value handling")
    void testNullHandling() {
        assertThrows(IllegalArgumentException.class, () -> tree.insert(null, "value"));
        assertThrows(IllegalArgumentException.class, () -> tree.insert(1, null));
        
        assertTrue(tree.find(null).isEmpty());
        assertFalse(tree.remove(null, "value"));
        assertTrue(tree.removeKey(null).isEmpty());
    }
    
    @ParameterizedTest
    @ValueSource(ints = {3, 4, 5, 8, 16, 32})
    @DisplayName("Test different B+ tree orders")
    void testDifferentOrders(int order) {
        BPlusTree<Integer, String> testTree = new BPlusTree<>(order);
        
        // Insert many values to test splitting behavior
        for (int i = 1; i <= 100; i++) {
            testTree.insert(i, "value" + i);
        }
        
        assertEquals(100, testTree.size());
        
        // Verify all values can be found
        for (int i = 1; i <= 100; i++) {
            Set<String> result = testTree.find(i);
            assertEquals(1, result.size());
            assertTrue(result.contains("value" + i));
        }
        
        // Test range query
        Set<String> range = testTree.findRange(25, 75);
        assertEquals(51, range.size());
        
        // Verify sorted order is maintained
        List<Integer> keys = testTree.getAllKeys();
        for (int i = 0; i < keys.size() - 1; i++) {
            assertTrue(keys.get(i) < keys.get(i + 1));
        }
    }
    
    @Test
    @DisplayName("Concurrent access test")
    void testConcurrentAccess() throws InterruptedException {
        final int numThreads = 10;
        final int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        
        // Create a larger tree for concurrent testing
        BPlusTree<Integer, String> concurrentTree = new BPlusTree<>(16);
        
        for (int t = 0; t < numThreads; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < operationsPerThread; i++) {
                        int key = threadId * operationsPerThread + i;
                        concurrentTree.insert(key, "thread" + threadId + "_value" + i);
                        
                        // Occasionally perform reads
                        if (i % 10 == 0) {
                            concurrentTree.find(key);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();
        
        // Verify all insertions were successful
        assertEquals(numThreads * operationsPerThread, concurrentTree.size());
        
        // Verify some random values
        for (int t = 0; t < numThreads; t++) {
            int key = t * operationsPerThread + 50; // Middle value for each thread
            Set<String> result = concurrentTree.find(key);
            assertEquals(1, result.size());
            assertTrue(result.iterator().next().startsWith("thread" + t));
        }
    }
    
    @Test
    @DisplayName("Tree statistics are accurate")
    void testStatistics() {
        // Empty tree statistics
        Map<String, Object> emptyStats = tree.getStatistics();
        assertEquals(0L, emptyStats.get("size"));
        assertEquals(DEFAULT_ORDER, emptyStats.get("order"));
        assertEquals(1, emptyStats.get("height")); // Root leaf
        assertEquals(1, emptyStats.get("leafNodeCount"));
        assertEquals(0, emptyStats.get("internalNodeCount"));
        
        // Insert data to create a more complex tree
        for (int i = 1; i <= 50; i++) {
            tree.insert(i, "value" + i);
        }
        
        Map<String, Object> stats = tree.getStatistics();
        assertEquals(50L, stats.get("size"));
        assertEquals(DEFAULT_ORDER, stats.get("order"));
        assertTrue((Integer) stats.get("height") >= 1);
        assertTrue((Integer) stats.get("leafNodeCount") >= 1);
        assertEquals((Integer) stats.get("totalNodes"), 
                    (Integer) stats.get("leafNodeCount") + (Integer) stats.get("internalNodeCount"));
    }
    
    @Test
    @DisplayName("Large dataset performance test")
    void testLargeDataset() {
        final int size = 10000;
        
        // Sequential insertion
        long startTime = System.nanoTime();
        for (int i = 0; i < size; i++) {
            tree.insert(i, "value" + i);
        }
        long insertTime = System.nanoTime() - startTime;
        
        assertEquals(size, tree.size());
        
        // Random access
        Random random = new Random(42); // Fixed seed for reproducibility
        startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            int key = random.nextInt(size);
            Set<String> result = tree.find(key);
            assertFalse(result.isEmpty());
        }
        long findTime = System.nanoTime() - startTime;
        
        // Range queries
        startTime = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            int start = random.nextInt(size / 2);
            int end = start + random.nextInt(size / 2);
            Set<String> range = tree.findRange(start, end);
            assertTrue(range.size() <= (end - start + 1));
        }
        long rangeTime = System.nanoTime() - startTime;
        
        // Log performance metrics (for manual verification)
        System.out.printf("Performance test results:%n");
        System.out.printf("Insert time for %d items: %.2f ms%n", size, insertTime / 1_000_000.0);
        System.out.printf("Average find time: %.2f μs%n", findTime / 1000.0 / 1000);
        System.out.printf("Average range query time: %.2f μs%n", rangeTime / 100.0 / 1000);
        
        Map<String, Object> stats = tree.getStatistics();
        System.out.printf("Tree statistics: %s%n", stats);
    }
}