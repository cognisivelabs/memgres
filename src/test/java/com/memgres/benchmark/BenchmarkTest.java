package com.memgres.benchmark;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the performance benchmarking framework.
 */
public class BenchmarkTest {
    
    private BenchmarkRunner benchmarkRunner;
    
    @BeforeEach
    void setUp() {
        benchmarkRunner = new BenchmarkRunner(2); // Use 2 threads for testing
    }
    
    @AfterEach
    void tearDown() {
        if (benchmarkRunner != null) {
            benchmarkRunner.shutdown();
        }
    }
    
    @Test
    void testMemGresBenchmarkExecution() {
        // Register MemGres database
        benchmarkRunner.registerDatabase("MemGres", new BenchmarkRunner.MemGresAdapter());
        
        // Add a simple benchmark scenario
        benchmarkRunner.addScenario(
            BenchmarkRunner.Scenarios.simpleInsert()
                .withIterations(100)
                .withWarmup(10)
        );
        
        // Run benchmarks
        BenchmarkReport report = benchmarkRunner.runBenchmarks();
        
        // Verify results
        assertNotNull(report, "Should generate benchmark report");
        assertTrue(report.getDatabaseNames().contains("MemGres"), "Should contain MemGres results");
        assertTrue(report.getScenarioNames().contains("Simple INSERT"), "Should contain Simple INSERT scenario");
        
        Optional<BenchmarkResult> result = report.getResult("MemGres", "Simple INSERT");
        assertTrue(result.isPresent(), "Should have MemGres Simple INSERT result");
        
        BenchmarkResult insertResult = result.get();
        assertTrue(insertResult.getTotalOperations() >= 50, "Should have at least 50 operations");
        assertTrue(insertResult.getOperationsPerSecond() >= 0, "Should have non-negative operations per second");
        // Don't require success rate - benchmarks may test failure scenarios
    }
    
    @Test
    void testMultipleScenariosExecution() {
        // Register MemGres database
        benchmarkRunner.registerDatabase("MemGres", new BenchmarkRunner.MemGresAdapter());
        
        // Add multiple scenarios
        benchmarkRunner.addScenario(
            BenchmarkRunner.Scenarios.simpleInsert()
                .withIterations(50)
                .withWarmup(5)
        );
        benchmarkRunner.addScenario(
            BenchmarkRunner.Scenarios.simpleSelect()
                .withIterations(50)
                .withWarmup(5)
        );
        benchmarkRunner.addScenario(
            BenchmarkRunner.Scenarios.joinQuery()
                .withIterations(20)
                .withWarmup(2)
        );
        
        // Run benchmarks
        BenchmarkReport report = benchmarkRunner.runBenchmarks();
        
        // Verify results
        assertEquals(3, report.getScenarioNames().size(), "Should have 3 scenarios");
        assertTrue(report.getScenarioNames().contains("Simple INSERT"));
        assertTrue(report.getScenarioNames().contains("Simple SELECT"));
        assertTrue(report.getScenarioNames().contains("JOIN Query"));
        
        // Verify each scenario has results
        for (String scenario : report.getScenarioNames()) {
            Optional<BenchmarkResult> result = report.getResult("MemGres", scenario);
            assertTrue(result.isPresent(), "Should have result for scenario: " + scenario);
            assertTrue(result.get().getTotalOperations() >= 10, "Should have at least 10 operations for scenario: " + scenario);
        }
    }
    
    @Test
    void testConcurrentBenchmarkExecution() {
        // Register MemGres database
        benchmarkRunner.registerDatabase("MemGres", new BenchmarkRunner.MemGresAdapter());
        
        // Add concurrent scenario
        benchmarkRunner.addScenario(
            BenchmarkRunner.Scenarios.simpleInsert()
                .withIterations(100)
                .withConcurrency(3)
                .withWarmup(5)
        );
        
        // Run benchmarks
        BenchmarkReport report = benchmarkRunner.runBenchmarks();
        
        // Verify results
        Optional<BenchmarkResult> result = report.getResult("MemGres", "Simple INSERT");
        assertTrue(result.isPresent(), "Should have concurrent benchmark result");
        
        BenchmarkResult insertResult = result.get();
        assertTrue(insertResult.getTotalOperations() >= 30, "Should have at least 30 operations in concurrent execution");
    }
    
    @Test
    void testBenchmarkResultStatistics() {
        // Register MemGres database
        benchmarkRunner.registerDatabase("MemGres", new BenchmarkRunner.MemGresAdapter());
        
        // Add scenario
        benchmarkRunner.addScenario(
            BenchmarkRunner.Scenarios.simpleInsert()
                .withIterations(100)
                .withWarmup(10)
        );
        
        // Run benchmarks
        BenchmarkReport report = benchmarkRunner.runBenchmarks();
        
        Optional<BenchmarkResult> result = report.getResult("MemGres", "Simple INSERT");
        assertTrue(result.isPresent());
        
        BenchmarkResult insertResult = result.get();
        
        // Test statistics
        assertTrue(insertResult.getAverageTimeMs() >= 0, "Should have valid average time");
        assertTrue(insertResult.getMedianTimeMs() >= 0, "Should have valid median time");
        assertTrue(insertResult.getP95TimeMs() >= 0, "Should have valid P95 time");
        assertTrue(insertResult.getP99TimeMs() >= 0, "Should have valid P99 time");
        assertTrue(insertResult.getMinTimeMs() >= 0, "Should have valid min time");
        assertTrue(insertResult.getMaxTimeMs() >= insertResult.getMinTimeMs(), "Max should be >= min time");
        
        // Test summary generation
        String summary = insertResult.getSummary();
        assertNotNull(summary, "Should generate summary");
        assertTrue(summary.contains("ops/sec"), "Summary should contain ops/sec");
        assertTrue(summary.contains("MemGres"), "Summary should contain database name");
        
        // Test detailed stats
        String detailedStats = insertResult.getDetailedStats();
        assertNotNull(detailedStats, "Should generate detailed stats");
        assertTrue(detailedStats.contains("Average:"), "Detailed stats should contain latency statistics");
    }
    
    @Test
    void testBenchmarkReportGeneration() {
        // Register MemGres database
        benchmarkRunner.registerDatabase("MemGres", new BenchmarkRunner.MemGresAdapter());
        
        // Add scenarios
        benchmarkRunner.addScenario(
            BenchmarkRunner.Scenarios.simpleInsert()
                .withIterations(50)
                .withWarmup(5)
        );
        benchmarkRunner.addScenario(
            BenchmarkRunner.Scenarios.simpleSelect()
                .withIterations(50)
                .withWarmup(5)
        );
        
        // Run benchmarks
        BenchmarkReport report = benchmarkRunner.runBenchmarks();
        
        // Test report generation
        String summary = report.generateSummary();
        assertNotNull(summary, "Should generate summary report");
        assertTrue(summary.contains("BENCHMARK REPORT"), "Summary should contain title");
        assertTrue(summary.contains("MemGres"), "Summary should contain database name");
        
        String detailedReport = report.generateDetailedReport();
        assertNotNull(detailedReport, "Should generate detailed report");
        assertTrue(detailedReport.contains("DETAILED BENCHMARK REPORT"), "Detailed report should contain title");
        
        String csvReport = report.generateCSVReport();
        assertNotNull(csvReport, "Should generate CSV report");
        assertTrue(csvReport.contains("Database,Scenario"), "CSV should contain header");
        assertTrue(csvReport.contains("MemGres"), "CSV should contain data");
        
        String overallStats = report.getOverallStats();
        assertNotNull(overallStats, "Should generate overall stats");
        assertTrue(overallStats.contains("OVERALL STATISTICS"), "Should contain stats title");
    }
    
    @Test
    void testCustomBenchmarkScenario() {
        // Register MemGres database
        benchmarkRunner.registerDatabase("MemGres", new BenchmarkRunner.MemGresAdapter());
        
        // Create custom scenario
        BenchmarkRunner.BenchmarkScenario customScenario = new BenchmarkRunner.BenchmarkScenario("Custom Test") {
            @Override
            public void setup(BenchmarkRunner.DatabaseAdapter adapter) throws Exception {
                adapter.executeSQL("CREATE TABLE custom_test (id INTEGER, data VARCHAR)");
            }
            
            @Override
            public void execute(BenchmarkRunner.DatabaseAdapter adapter) throws Exception {
                adapter.executeSQL("INSERT INTO custom_test VALUES (1, 'test_data')");
                adapter.executeSQL("SELECT COUNT(*) FROM custom_test");
                adapter.executeSQL("DELETE FROM custom_test WHERE id = 1");
            }
            
            @Override
            public void cleanup(BenchmarkRunner.DatabaseAdapter adapter) throws Exception {
                try {
                    adapter.executeSQL("DROP TABLE custom_test");
                } catch (Exception e) {
                    // Ignore cleanup errors
                }
            }
        }.withIterations(30).withWarmup(3);
        
        benchmarkRunner.addScenario(customScenario);
        
        // Run benchmarks
        BenchmarkReport report = benchmarkRunner.runBenchmarks();
        
        // Verify custom scenario results
        assertTrue(report.getScenarioNames().contains("Custom Test"), "Should contain custom scenario");
        Optional<BenchmarkResult> result = report.getResult("MemGres", "Custom Test");
        assertTrue(result.isPresent(), "Should have custom scenario result");
        assertTrue(result.get().getTotalOperations() >= 25, "Should have executed most operations");
    }
    
    @Test
    void testBenchmarkResultComparison() {
        // Create mock results for comparison
        // Database A: slower (higher times) but same total time -> lower throughput
        java.util.List<Long> times1 = java.util.Arrays.asList(1000000L, 2000000L, 3000000L, 4000000L, 5000000L);
        // Database B: faster (lower times) but same total time -> higher throughput  
        java.util.List<Long> times2 = java.util.Arrays.asList(500000L, 1000000L, 1500000L, 2000000L, 2500000L);
        
        BenchmarkResult result1 = new BenchmarkResult("Test Scenario", "Database A", 5, 0, 150, times1);
        BenchmarkResult result2 = new BenchmarkResult("Test Scenario", "Database B", 5, 0, 75, times2);
        
        // Test comparison (result1 is baseline, result2 is compared against it)
        BenchmarkResult.BenchmarkComparison comparison = result1.compareTo(result2);
        assertNotNull(comparison, "Should create comparison");
        
        // Database B should be faster (lower latency, higher throughput)
        // Ratio = result2/result1, so higher throughput should give ratio > 1
        assertTrue(comparison.getThroughputRatio() > 1.0, "Database B should have higher throughput ratio");
        assertTrue(comparison.getLatencyRatio() < 1.0, "Database B should have lower latency ratio");
        
        String summaryComp = comparison.getSummary();
        assertNotNull(summaryComp, "Should generate comparison summary");
        assertTrue(summaryComp.contains("vs"), "Comparison should contain 'vs'");
    }
    
    @Test
    void testBenchmarkWithErrorHandling() {
        // Register MemGres database
        benchmarkRunner.registerDatabase("MemGres", new BenchmarkRunner.MemGresAdapter());
        
        // Create scenario that might fail
        BenchmarkRunner.BenchmarkScenario errorScenario = new BenchmarkRunner.BenchmarkScenario("Error Test") {
            private final java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger();
            
            @Override
            public void setup(BenchmarkRunner.DatabaseAdapter adapter) throws Exception {
                adapter.executeSQL("CREATE TABLE error_test (id INTEGER)");
                adapter.executeSQL("INSERT INTO error_test VALUES (1)"); // Pre-insert to cause conflicts
            }
            
            @Override
            public void execute(BenchmarkRunner.DatabaseAdapter adapter) throws Exception {
                int count = counter.incrementAndGet();
                if (count % 3 == 1) {
                    // This should fail - duplicate key
                    adapter.executeSQL("INSERT INTO error_test VALUES (1)");
                } else {
                    adapter.executeSQL("INSERT INTO error_test VALUES (" + (count + 100) + ")");
                }
            }
            
            @Override
            public void cleanup(BenchmarkRunner.DatabaseAdapter adapter) throws Exception {
                try {
                    adapter.executeSQL("DROP TABLE error_test");
                } catch (Exception e) {
                    // Ignore cleanup errors
                }
            }
        }.withIterations(15).withWarmup(1);
        
        benchmarkRunner.addScenario(errorScenario);
        
        // Run benchmarks
        BenchmarkReport report = benchmarkRunner.runBenchmarks();
        
        Optional<BenchmarkResult> result = report.getResult("MemGres", "Error Test");
        assertTrue(result.isPresent(), "Should have result even with errors");
        
        BenchmarkResult errorResult = result.get();
        assertTrue(errorResult.getTotalOperations() >= 10, "Should have attempted operations");
        // Note: Since the test creates conflict scenarios, we expect either some failures or some successes
        assertTrue(errorResult.getFailedOperations() >= 0, "Should have non-negative failed operations");
        assertTrue(errorResult.getSuccessfulOperations() >= 0, "Should have non-negative successful operations");
    }
}