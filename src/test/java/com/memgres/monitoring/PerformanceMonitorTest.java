package com.memgres.monitoring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for PerformanceMonitor functionality.
 */
public class PerformanceMonitorTest {
    
    private PerformanceMonitor monitor;
    
    @BeforeEach
    void setUp() {
        monitor = PerformanceMonitor.getInstance();
        monitor.reset(); // Start with clean state
    }
    
    @Test
    void testQueryRecording() {
        // Record some test queries
        monitor.recordQuery("SELECT * FROM users", 150, true);
        monitor.recordQuery("INSERT INTO users VALUES (?)", 50, true);
        monitor.recordQuery("UPDATE users SET name = ?", 200, true);
        monitor.recordQuery("SELECT * FROM products", 2000, true); // Slow query
        
        PerformanceStatistics stats = monitor.getStatistics();
        
        assertEquals(4, stats.getTotalQueries(), "Should have recorded 4 queries");
        assertEquals(2400, stats.getTotalQueryTime(), "Total query time should be 2400ms");
        assertEquals(1, stats.getSlowQueries(), "Should have 1 slow query");
        assertEquals(2000, stats.getLongestQueryTime(), "Longest query should be 2000ms");
        assertEquals(600.0, stats.getAverageQueryTime(), "Average should be 600ms");
    }
    
    @Test
    void testConnectionEventRecording() {
        // Record connection events
        monitor.recordConnectionEvent(PerformanceMonitor.ConnectionEvent.CREATED, "conn-1");
        monitor.recordConnectionEvent(PerformanceMonitor.ConnectionEvent.CREATED, "conn-2");
        monitor.recordConnectionEvent(PerformanceMonitor.ConnectionEvent.CLOSED, "conn-1");
        monitor.recordConnectionEvent(PerformanceMonitor.ConnectionEvent.ERROR, "conn-3");
        
        PerformanceStatistics stats = monitor.getStatistics();
        
        assertEquals(2, stats.getTotalConnections(), "Should have 2 total connections");
        assertEquals(1, stats.getActiveConnections(), "Should have 1 active connection");
        assertEquals(1, stats.getConnectionErrors(), "Should have 1 connection error");
    }
    
    @Test
    void testTransactionEventRecording() {
        // Record transaction events
        monitor.recordTransactionEvent(PerformanceMonitor.TransactionEvent.STARTED, 1L, 0L);
        monitor.recordTransactionEvent(PerformanceMonitor.TransactionEvent.STARTED, 2L, 0L);
        monitor.recordTransactionEvent(PerformanceMonitor.TransactionEvent.COMMITTED, 1L, 100L);
        monitor.recordTransactionEvent(PerformanceMonitor.TransactionEvent.ROLLED_BACK, 2L, 50L);
        
        PerformanceStatistics stats = monitor.getStatistics();
        
        assertEquals(2, stats.getTotalTransactions(), "Should have 2 total transactions");
        assertEquals(1, stats.getCommittedTransactions(), "Should have 1 committed transaction");
        assertEquals(1, stats.getRolledBackTransactions(), "Should have 1 rolled back transaction");
        assertEquals(50.0, stats.getTransactionSuccessRate(), "Success rate should be 50%");
    }
    
    @Test
    void testMemoryEventRecording() {
        // Record memory events
        monitor.recordMemoryEvent(PerformanceMonitor.MemoryEvent.ALLOCATED, 1024L, "table data");
        monitor.recordMemoryEvent(PerformanceMonitor.MemoryEvent.ALLOCATED, 512L, "index data");
        monitor.recordMemoryEvent(PerformanceMonitor.MemoryEvent.DEALLOCATED, 256L, "cleanup");
        
        PerformanceStatistics stats = monitor.getStatistics();
        
        assertEquals(2, stats.getMemoryAllocations(), "Should have 2 allocations");
        assertEquals(1, stats.getMemoryDeallocations(), "Should have 1 deallocation");
    }
    
    @Test
    void testQueryTypeClassification() {
        // Record different query types
        monitor.recordQuery("SELECT id FROM users", 100, true);
        monitor.recordQuery("INSERT INTO products (name) VALUES ('test')", 80, true);
        monitor.recordQuery("UPDATE users SET active = true", 120, true);
        monitor.recordQuery("DELETE FROM temp_table", 60, true);
        monitor.recordQuery("CREATE TABLE new_table (id INT)", 200, true);
        monitor.recordQuery("EXPLAIN SELECT * FROM users", 50, true);
        
        PerformanceStatistics stats = monitor.getStatistics();
        
        assertEquals(1L, (long) stats.getQueryTypeCounts().get("SELECT"), "Should have 1 SELECT");
        assertEquals(1L, (long) stats.getQueryTypeCounts().get("INSERT"), "Should have 1 INSERT");
        assertEquals(1L, (long) stats.getQueryTypeCounts().get("UPDATE"), "Should have 1 UPDATE");
        assertEquals(1L, (long) stats.getQueryTypeCounts().get("DELETE"), "Should have 1 DELETE");
        assertEquals(1L, (long) stats.getQueryTypeCounts().get("DDL"), "Should have 1 DDL");
        assertEquals(1L, (long) stats.getQueryTypeCounts().get("OTHER"), "Should have 1 OTHER");
    }
    
    @Test
    void testSlowQueryThreshold() {
        // Set custom threshold
        monitor.setSlowQueryThreshold(500);
        
        monitor.recordQuery("SELECT * FROM users", 400, true); // Not slow
        monitor.recordQuery("SELECT * FROM products", 600, true); // Slow
        
        PerformanceStatistics stats = monitor.getStatistics();
        
        assertEquals(2, stats.getTotalQueries(), "Should have 2 total queries");
        assertEquals(1, stats.getSlowQueries(), "Should have 1 slow query with custom threshold");
    }
    
    @Test
    void testFailedQueryRecording() {
        // Record successful and failed queries
        monitor.recordQuery("SELECT * FROM users", 100, true);
        monitor.recordQuery("SELECT * FROM missing_table", 50, false);
        monitor.recordQuery("INSERT INTO users VALUES (1)", 75, true);
        
        PerformanceStatistics stats = monitor.getStatistics();
        
        assertEquals(3, stats.getTotalQueries(), "Should record both successful and failed queries");
        assertEquals(225, stats.getTotalQueryTime(), "Should include time for all queries");
    }
    
    @Test
    void testStatisticsCalculations() {
        // Add test data with known values
        monitor.recordQuery("SELECT 1", 100, true);
        monitor.recordQuery("SELECT 2", 200, true);
        monitor.recordQuery("SELECT 3", 300, true);
        monitor.recordQuery("SELECT 4", 1500, true); // Slow query
        
        monitor.recordConnectionEvent(PerformanceMonitor.ConnectionEvent.CREATED, "conn-1");
        monitor.recordConnectionEvent(PerformanceMonitor.ConnectionEvent.CREATED, "conn-2");
        monitor.recordConnectionEvent(PerformanceMonitor.ConnectionEvent.ERROR, "conn-3");
        
        monitor.recordTransactionEvent(PerformanceMonitor.TransactionEvent.STARTED, 1L, 0L);
        monitor.recordTransactionEvent(PerformanceMonitor.TransactionEvent.STARTED, 2L, 0L);
        monitor.recordTransactionEvent(PerformanceMonitor.TransactionEvent.COMMITTED, 1L, 100L);
        monitor.recordTransactionEvent(PerformanceMonitor.TransactionEvent.ROLLED_BACK, 2L, 50L);
        
        PerformanceStatistics stats = monitor.getStatistics();
        
        // Verify calculated metrics
        assertEquals(525.0, stats.getAverageQueryTime(), "Average query time should be 525ms");
        assertEquals(25.0, stats.getSlowQueryPercentage(), "25% of queries should be slow");
        assertEquals(50.0, stats.getConnectionErrorRate(), "50% connection error rate");
        assertEquals(50.0, stats.getTransactionSuccessRate(), "50% transaction success rate");
    }
    
    @Test
    void testReset() {
        // Add some data
        monitor.recordQuery("SELECT * FROM users", 100, true);
        monitor.recordConnectionEvent(PerformanceMonitor.ConnectionEvent.CREATED, "conn-1");
        monitor.recordTransactionEvent(PerformanceMonitor.TransactionEvent.STARTED, 1L, 0L);
        monitor.recordMemoryEvent(PerformanceMonitor.MemoryEvent.ALLOCATED, 1024L, "test");
        
        // Verify data exists
        PerformanceStatistics beforeReset = monitor.getStatistics();
        assertTrue(beforeReset.getTotalQueries() > 0, "Should have queries before reset");
        
        // Reset and verify clean state
        monitor.reset();
        
        PerformanceStatistics afterReset = monitor.getStatistics();
        assertEquals(0, afterReset.getTotalQueries(), "Should have no queries after reset");
        assertEquals(0, afterReset.getTotalConnections(), "Should have no connections after reset");
        assertEquals(0, afterReset.getTotalTransactions(), "Should have no transactions after reset");
        assertEquals(0, afterReset.getMemoryAllocations(), "Should have no memory events after reset");
    }
    
    @Test
    void testSystemHealthLogging() {
        // Add some sample data
        monitor.recordQuery("SELECT * FROM users", 150, true);
        monitor.recordQuery("INSERT INTO products VALUES (?)", 2000, true); // Slow query
        monitor.recordConnectionEvent(PerformanceMonitor.ConnectionEvent.CREATED, "conn-1");
        monitor.recordTransactionEvent(PerformanceMonitor.TransactionEvent.STARTED, 1L, 0L);
        monitor.recordTransactionEvent(PerformanceMonitor.TransactionEvent.COMMITTED, 1L, 100L);
        
        // This should not throw an exception and should log health metrics
        assertDoesNotThrow(() -> monitor.logSystemHealth(),
            "System health logging should not throw exceptions");
    }
}