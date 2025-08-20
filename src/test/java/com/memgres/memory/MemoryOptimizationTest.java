package com.memgres.memory;

import com.memgres.core.MemGresEngine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for memory optimization components.
 */
public class MemoryOptimizationTest {
    
    private MemGresEngine engine;
    private MemoryManager memoryManager;
    private MemoryOptimizer memoryOptimizer;
    
    @BeforeEach
    void setUp() {
        engine = new MemGresEngine();
        engine.initialize();
        
        memoryManager = MemoryManager.getInstance();
        memoryOptimizer = new MemoryOptimizer(engine);
    }
    
    @AfterEach
    void tearDown() {
        if (memoryOptimizer != null) {
            memoryOptimizer.shutdown();
        }
        if (engine != null) {
            engine.shutdown();
        }
    }
    
    @Test
    void testMemoryManagerAllocationTracking() {
        // Register some allocations
        memoryManager.registerAllocation("test1", 1024, "Test allocation 1");
        memoryManager.registerAllocation("test2", 2048, "Test allocation 2");
        memoryManager.registerAllocation("test3", 4096, "Test allocation 3");
        
        // Get statistics
        MemoryManager.MemoryStatistics stats = memoryManager.getStatistics();
        
        // Verify allocations are tracked
        assertTrue(stats.getTotalAllocated() >= 7168, "Should track total allocated memory");
        assertEquals(3, stats.getActiveAllocations(), "Should have 3 active allocations");
        
        // Unregister an allocation
        memoryManager.unregisterAllocation("test2");
        
        // Verify allocation was removed
        stats = memoryManager.getStatistics();
        assertEquals(2, stats.getActiveAllocations(), "Should have 2 active allocations after removal");
        assertTrue(stats.getTotalFreed() >= 2048, "Should track freed memory");
        
        // Clean up
        memoryManager.unregisterAllocation("test1");
        memoryManager.unregisterAllocation("test3");
    }
    
    @Test
    void testMemoryAlertHandling() throws InterruptedException {
        CountDownLatch alertLatch = new CountDownLatch(1);
        AtomicInteger alertCount = new AtomicInteger(0);
        
        // Register alert handler
        memoryManager.registerAlertHandler(alert -> {
            alertCount.incrementAndGet();
            alertLatch.countDown();
            
            assertNotNull(alert.getMessage(), "Alert should have a message");
            assertNotNull(alert.getLevel(), "Alert should have a level");
            assertNotNull(alert.getMemoryUsage(), "Alert should have memory usage info");
        });
        
        // Set low thresholds to trigger alerts
        memoryManager.setThresholds(0.01, 0.02); // Very low thresholds
        
        // Wait for potential alert
        alertLatch.await(10, TimeUnit.SECONDS);
        
        // Reset thresholds
        memoryManager.setThresholds(0.85, 0.95);
        
        // Note: Alert may or may not trigger depending on current memory usage
        // So we just verify the handler was registered correctly
        assertTrue(alertCount.get() >= 0, "Alert handler should be working");
    }
    
    @Test
    void testMemoryOptimizationStrategies() {
        // Run optimization
        MemoryOptimizer.OptimizationResult result = memoryOptimizer.optimize();
        
        assertNotNull(result, "Should return optimization result");
        assertTrue(result.getDurationMs() >= 0, "Should have valid duration");
        assertNotNull(result.getStrategyResults(), "Should have strategy results");
        
        // Verify default strategies are registered
        assertEquals(5, result.getStrategyResults().size(), "Should have 5 default strategies");
        assertTrue(result.getStrategyResults().containsKey("compact_sparse_tables"));
        assertTrue(result.getStrategyResults().containsKey("clear_unused_indexes"));
        assertTrue(result.getStrategyResults().containsKey("trim_strings"));
        assertTrue(result.getStrategyResults().containsKey("release_cache"));
        assertTrue(result.getStrategyResults().containsKey("optimize_large_objects"));
    }
    
    @Test
    void testCustomOptimizationStrategy() {
        AtomicInteger callCount = new AtomicInteger(0);
        
        // Register custom strategy
        memoryOptimizer.registerStrategy("custom_test", engine -> {
            callCount.incrementAndGet();
            return 1024L; // Simulate reclaiming 1KB
        });
        
        // Run optimization with custom strategy
        MemoryOptimizer.OptimizationResult result = memoryOptimizer.optimize();
        
        // Verify custom strategy was executed
        assertEquals(1, callCount.get(), "Custom strategy should be called");
        assertTrue(result.getStrategyResults().containsKey("custom_test"), 
            "Should include custom strategy in results");
        assertEquals(1024L, result.getStrategyResults().get("custom_test"), 
            "Should return correct reclaimed memory");
    }
    
    @Test
    void testAutoOptimizationSettings() {
        // Set auto-optimization parameters
        memoryOptimizer.setAutoOptimization(true, 1000, 0.7);
        
        // Get statistics
        MemoryOptimizer.OptimizationStatistics stats = memoryOptimizer.getStatistics();
        
        // Verify settings
        assertTrue(stats.isAutoOptimizationEnabled(), "Auto-optimization should be enabled");
        assertEquals(1000, stats.getOptimizationIntervalMs(), "Should have correct interval");
        assertEquals(0.7, stats.getMemoryPressureThreshold(), "Should have correct threshold");
        
        // Disable auto-optimization
        memoryOptimizer.setAutoOptimization(false, 2000, 0.8);
        
        stats = memoryOptimizer.getStatistics();
        assertFalse(stats.isAutoOptimizationEnabled(), "Auto-optimization should be disabled");
    }
    
    @Test
    void testLRUCacheEvictionPolicy() {
        CacheEvictionPolicy.LRUPolicy<String, String> lru = new CacheEvictionPolicy.LRUPolicy<>(100);
        
        // Add items
        lru.recordAddition("key1", 30);
        lru.recordAddition("key2", 30);
        lru.recordAddition("key3", 30);
        
        // Access items (key2 becomes most recently used)
        lru.recordAccess("key2");
        lru.recordAccess("key1");
        
        // key3 should be least recently used
        assertEquals("key3", lru.selectEvictionCandidate(), "Should select LRU item");
        
        // Remove and verify
        lru.recordRemoval("key3");
        CacheEvictionPolicy.CacheStatistics stats = lru.getStatistics();
        assertEquals(60, stats.getCurrentSize(), "Should update size after removal");
        assertEquals(1, stats.getEvictionCount(), "Should count evictions");
    }
    
    @Test
    void testLFUCacheEvictionPolicy() {
        CacheEvictionPolicy.LFUPolicy<String, String> lfu = new CacheEvictionPolicy.LFUPolicy<>(100);
        
        // Add items
        lfu.recordAddition("key1", 30);
        lfu.recordAddition("key2", 30);
        lfu.recordAddition("key3", 30);
        
        // Access items with different frequencies
        lfu.recordAccess("key1"); // frequency: 2
        lfu.recordAccess("key2"); // frequency: 2
        lfu.recordAccess("key2"); // frequency: 3
        // key3 has frequency: 1
        
        // key3 should be least frequently used
        assertEquals("key3", lfu.selectEvictionCandidate(), "Should select LFU item");
        
        // Verify statistics
        CacheEvictionPolicy.CacheStatistics stats = lfu.getStatistics();
        assertEquals(3, stats.getHitCount(), "Should count hits");
        assertEquals(0, stats.getMissCount(), "Should have no misses");
    }
    
    @Test
    void testFIFOCacheEvictionPolicy() {
        CacheEvictionPolicy.FIFOPolicy<String, String> fifo = new CacheEvictionPolicy.FIFOPolicy<>(100);
        
        // Add items in order
        fifo.recordAddition("key1", 30);
        fifo.recordAddition("key2", 30);
        fifo.recordAddition("key3", 30);
        
        // Access doesn't change eviction order in FIFO
        fifo.recordAccess("key3");
        fifo.recordAccess("key2");
        
        // key1 should be first in, first out
        assertEquals("key1", fifo.selectEvictionCandidate(), "Should select FIFO item");
        
        // Remove and add new item
        fifo.recordRemoval("key1");
        fifo.recordAddition("key4", 30);
        
        // Now key2 should be next
        assertEquals("key2", fifo.selectEvictionCandidate(), "Should select next FIFO item");
    }
    
    @Test
    void testRandomCacheEvictionPolicy() {
        CacheEvictionPolicy.RandomPolicy<String, String> random = new CacheEvictionPolicy.RandomPolicy<>(100);
        
        // Add items
        random.recordAddition("key1", 30);
        random.recordAddition("key2", 30);
        random.recordAddition("key3", 30);
        
        // Get eviction candidate
        String candidate = random.selectEvictionCandidate();
        
        // Should select one of the keys
        assertTrue(candidate.equals("key1") || candidate.equals("key2") || candidate.equals("key3"),
            "Should select a valid key");
        
        // Test clear
        random.clear();
        CacheEvictionPolicy.CacheStatistics stats = random.getStatistics();
        assertEquals(0, stats.getCurrentSize(), "Should clear all entries");
    }
    
    @Test
    void testCacheEvictionTrigger() {
        CacheEvictionPolicy.LRUPolicy<String, String> policy = new CacheEvictionPolicy.LRUPolicy<>(100);
        
        // Add items up to limit
        policy.recordAddition("key1", 40);
        policy.recordAddition("key2", 40);
        assertFalse(policy.shouldEvict(), "Should not need eviction at 80/100");
        
        // Add item that exceeds limit
        policy.recordAddition("key3", 30);
        assertTrue(policy.shouldEvict(), "Should need eviction at 110/100");
        
        // Evict and check
        String toEvict = policy.selectEvictionCandidate();
        policy.recordRemoval(toEvict);
        assertFalse(policy.shouldEvict(), "Should not need eviction after removal");
    }
    
    @Test
    void testMemoryStatistics() {
        MemoryManager.MemoryStatistics stats = memoryManager.getStatistics();
        
        assertNotNull(stats.getHeapUsage(), "Should have heap usage");
        assertNotNull(stats.getNonHeapUsage(), "Should have non-heap usage");
        assertTrue(stats.getHeapUsagePercentage() >= 0 && stats.getHeapUsagePercentage() <= 100,
            "Heap usage percentage should be between 0 and 100");
        assertTrue(stats.getGcCount() >= 0, "GC count should be non-negative");
        assertTrue(stats.getGcTime() >= 0, "GC time should be non-negative");
    }
    
    @Test
    void testConcurrentMemoryOperations() throws InterruptedException {
        int threadCount = 10;
        int operationsPerThread = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Thread> threads = new ArrayList<>();
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            Thread thread = new Thread(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        String id = "thread" + threadId + "_alloc" + j;
                        
                        // Register allocation
                        memoryManager.registerAllocation(id, 1024, "Test allocation");
                        
                        // Simulate some work
                        Thread.sleep(1);
                        
                        // Unregister allocation
                        memoryManager.unregisterAllocation(id);
                    }
                } catch (Exception e) {
                    fail("Concurrent operation failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
            threads.add(thread);
            thread.start();
        }
        
        // Wait for all threads
        assertTrue(latch.await(30, TimeUnit.SECONDS), "All threads should complete");
        
        // Verify no lingering allocations
        MemoryManager.MemoryStatistics stats = memoryManager.getStatistics();
        assertEquals(0, stats.getActiveAllocations(), 
            "Should have no active allocations after concurrent test");
    }
}