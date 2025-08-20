package com.memgres.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Comprehensive report from benchmark execution.
 */
public class BenchmarkReport {
    private static final Logger logger = LoggerFactory.getLogger(BenchmarkReport.class);
    
    private final Map<String, List<BenchmarkResult>> results;
    private final LocalDateTime timestamp;
    
    public BenchmarkReport(Map<String, List<BenchmarkResult>> results) {
        this.results = new HashMap<>(results);
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Get results for a specific database.
     */
    public List<BenchmarkResult> getResults(String database) {
        return results.getOrDefault(database, Collections.emptyList());
    }
    
    /**
     * Get all database names.
     */
    public Set<String> getDatabaseNames() {
        return results.keySet();
    }
    
    /**
     * Get all scenario names.
     */
    public Set<String> getScenarioNames() {
        return results.values().stream()
            .flatMap(List::stream)
            .map(BenchmarkResult::getScenarioName)
            .collect(Collectors.toSet());
    }
    
    /**
     * Get result for a specific scenario and database.
     */
    public Optional<BenchmarkResult> getResult(String database, String scenario) {
        return getResults(database).stream()
            .filter(r -> r.getScenarioName().equals(scenario))
            .findFirst();
    }
    
    /**
     * Get comparison between two databases for a scenario.
     */
    public Optional<BenchmarkResult.BenchmarkComparison> getComparison(
            String baselineDatabase, String comparisonDatabase, String scenario) {
        
        Optional<BenchmarkResult> baseline = getResult(baselineDatabase, scenario);
        Optional<BenchmarkResult> comparison = getResult(comparisonDatabase, scenario);
        
        if (baseline.isPresent() && comparison.isPresent()) {
            return Optional.of(baseline.get().compareTo(comparison.get()));
        }
        return Optional.empty();
    }
    
    /**
     * Generate a summary report.
     */
    public String generateSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(80)).append("\n");
        sb.append("BENCHMARK REPORT\n");
        sb.append("Generated: ").append(timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n");
        sb.append("=".repeat(80)).append("\n\n");
        
        // Summary by database
        for (String database : getDatabaseNames()) {
            sb.append("Database: ").append(database).append("\n");
            sb.append("-".repeat(40)).append("\n");
            
            List<BenchmarkResult> dbResults = getResults(database);
            for (BenchmarkResult result : dbResults) {
                sb.append("  ").append(result.getSummary()).append("\n");
            }
            sb.append("\n");
        }
        
        // Comparisons (if multiple databases)
        if (getDatabaseNames().size() > 1) {
            sb.append("COMPARISONS\n");
            sb.append("-".repeat(40)).append("\n");
            
            List<String> databases = new ArrayList<>(getDatabaseNames());
            String baseline = databases.get(0);
            
            for (int i = 1; i < databases.size(); i++) {
                String comparison = databases.get(i);
                sb.append(String.format("\n%s vs %s:\n", comparison, baseline));
                
                for (String scenario : getScenarioNames()) {
                    getComparison(baseline, comparison, scenario)
                        .ifPresent(comp -> sb.append("  ").append(comp.getSummary()).append("\n"));
                }
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Generate a detailed report.
     */
    public String generateDetailedReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(100)).append("\n");
        sb.append("DETAILED BENCHMARK REPORT\n");
        sb.append("Generated: ").append(timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n");
        sb.append("=".repeat(100)).append("\n\n");
        
        for (String database : getDatabaseNames()) {
            sb.append("DATABASE: ").append(database).append("\n");
            sb.append("=".repeat(50)).append("\n\n");
            
            List<BenchmarkResult> dbResults = getResults(database);
            for (BenchmarkResult result : dbResults) {
                sb.append(result.getDetailedStats()).append("\n");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Generate a CSV report.
     */
    public String generateCSVReport() {
        StringBuilder sb = new StringBuilder();
        
        // Header
        sb.append("Database,Scenario,Total Operations,Failed Operations,Success Rate %,")
          .append("Total Time (ms),Operations/sec,Avg Latency (ms),Median Latency (ms),")
          .append("P95 Latency (ms),P99 Latency (ms),Min Latency (ms),Max Latency (ms)\n");
        
        // Data rows
        for (String database : getDatabaseNames()) {
            for (BenchmarkResult result : getResults(database)) {
                sb.append(String.format("%s,%s,%d,%d,%.2f,%d,%.2f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f\n",
                    result.getDatabaseName(),
                    result.getScenarioName(),
                    result.getTotalOperations(),
                    result.getFailedOperations(),
                    result.getSuccessRatePercent(),
                    result.getTotalTimeMs(),
                    result.getOperationsPerSecond(),
                    result.getAverageTimeMs(),
                    result.getMedianTimeMs(),
                    result.getP95TimeMs(),
                    result.getP99TimeMs(),
                    result.getMinTimeMs(),
                    result.getMaxTimeMs()
                ));
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Save report to files.
     */
    public void saveToFiles(String basePath) {
        try {
            // Summary report
            try (FileWriter writer = new FileWriter(basePath + "_summary.txt")) {
                writer.write(generateSummary());
            }
            
            // Detailed report
            try (FileWriter writer = new FileWriter(basePath + "_detailed.txt")) {
                writer.write(generateDetailedReport());
            }
            
            // CSV report
            try (FileWriter writer = new FileWriter(basePath + "_data.csv")) {
                writer.write(generateCSVReport());
            }
            
            logger.info("Benchmark reports saved to {}_*.txt and {}_data.csv", basePath, basePath);
            
        } catch (IOException e) {
            logger.error("Error saving benchmark reports", e);
        }
    }
    
    /**
     * Print summary to console.
     */
    public void printSummary() {
        System.out.println(generateSummary());
    }
    
    /**
     * Get the winner (best performing database) for each scenario.
     */
    public Map<String, String> getWinners() {
        Map<String, String> winners = new HashMap<>();
        
        for (String scenario : getScenarioNames()) {
            String winner = getDatabaseNames().stream()
                .map(db -> getResult(db, scenario))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Comparator.comparingDouble(BenchmarkResult::getOperationsPerSecond))
                .map(BenchmarkResult::getDatabaseName)
                .orElse("N/A");
            
            winners.put(scenario, winner);
        }
        
        return winners;
    }
    
    /**
     * Get overall statistics.
     */
    public String getOverallStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("OVERALL STATISTICS\n");
        sb.append("-".repeat(30)).append("\n");
        sb.append(String.format("Databases tested: %d\n", getDatabaseNames().size()));
        sb.append(String.format("Scenarios run: %d\n", getScenarioNames().size()));
        
        long totalOps = results.values().stream()
            .flatMap(List::stream)
            .mapToLong(BenchmarkResult::getTotalOperations)
            .sum();
        sb.append(String.format("Total operations: %d\n", totalOps));
        
        Map<String, String> winners = getWinners();
        sb.append("\nWinners by scenario:\n");
        for (Map.Entry<String, String> entry : winners.entrySet()) {
            sb.append(String.format("  %s: %s\n", entry.getKey(), entry.getValue()));
        }
        
        return sb.toString();
    }
}