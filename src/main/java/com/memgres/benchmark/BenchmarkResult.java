package com.memgres.benchmark;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Results from a benchmark execution.
 */
public class BenchmarkResult {
    private final String scenarioName;
    private final String databaseName;
    private final long totalOperations;
    private final long failedOperations;
    private final long totalTimeMs;
    private final List<Long> operationTimesNs;
    
    // Computed statistics
    private final double averageTimeNs;
    private final double medianTimeNs;
    private final double p95TimeNs;
    private final double p99TimeNs;
    private final long minTimeNs;
    private final long maxTimeNs;
    private final double operationsPerSecond;
    private final double successRate;
    
    public BenchmarkResult(String scenarioName, String databaseName, 
                          long totalOperations, long failedOperations, 
                          long totalTimeMs, List<Long> operationTimesNs) {
        this.scenarioName = scenarioName;
        this.databaseName = databaseName;
        this.totalOperations = totalOperations;
        this.failedOperations = failedOperations;
        this.totalTimeMs = totalTimeMs;
        this.operationTimesNs = new ArrayList<>(operationTimesNs);
        
        // Compute statistics
        if (!operationTimesNs.isEmpty()) {
            List<Long> sortedTimes = operationTimesNs.stream().sorted().collect(Collectors.toList());
            
            this.averageTimeNs = operationTimesNs.stream().mapToLong(Long::longValue).average().orElse(0.0);
            this.medianTimeNs = getPercentile(sortedTimes, 50.0);
            this.p95TimeNs = getPercentile(sortedTimes, 95.0);
            this.p99TimeNs = getPercentile(sortedTimes, 99.0);
            this.minTimeNs = sortedTimes.get(0);
            this.maxTimeNs = sortedTimes.get(sortedTimes.size() - 1);
        } else {
            this.averageTimeNs = 0.0;
            this.medianTimeNs = 0.0;
            this.p95TimeNs = 0.0;
            this.p99TimeNs = 0.0;
            this.minTimeNs = 0;
            this.maxTimeNs = 0;
        }
        
        this.operationsPerSecond = totalTimeMs > 0 ? (double) totalOperations * 1000.0 / totalTimeMs : 0.0;
        this.successRate = totalOperations > 0 ? (double) (totalOperations - failedOperations) / totalOperations : 0.0;
    }
    
    private double getPercentile(List<Long> sortedValues, double percentile) {
        if (sortedValues.isEmpty()) return 0.0;
        
        double index = percentile / 100.0 * (sortedValues.size() - 1);
        int lowerIndex = (int) Math.floor(index);
        int upperIndex = (int) Math.ceil(index);
        
        if (lowerIndex == upperIndex) {
            return sortedValues.get(lowerIndex);
        } else {
            double weight = index - lowerIndex;
            return sortedValues.get(lowerIndex) * (1 - weight) + sortedValues.get(upperIndex) * weight;
        }
    }
    
    // Getters
    public String getScenarioName() { return scenarioName; }
    public String getDatabaseName() { return databaseName; }
    public long getTotalOperations() { return totalOperations; }
    public long getFailedOperations() { return failedOperations; }
    public long getSuccessfulOperations() { return totalOperations - failedOperations; }
    public long getTotalTimeMs() { return totalTimeMs; }
    public double getAverageTimeNs() { return averageTimeNs; }
    public double getAverageTimeMs() { return averageTimeNs / 1_000_000.0; }
    public double getMedianTimeNs() { return medianTimeNs; }
    public double getMedianTimeMs() { return medianTimeNs / 1_000_000.0; }
    public double getP95TimeNs() { return p95TimeNs; }
    public double getP95TimeMs() { return p95TimeNs / 1_000_000.0; }
    public double getP99TimeNs() { return p99TimeNs; }
    public double getP99TimeMs() { return p99TimeNs / 1_000_000.0; }
    public long getMinTimeNs() { return minTimeNs; }
    public double getMinTimeMs() { return minTimeNs / 1_000_000.0; }
    public long getMaxTimeNs() { return maxTimeNs; }
    public double getMaxTimeMs() { return maxTimeNs / 1_000_000.0; }
    public double getOperationsPerSecond() { return operationsPerSecond; }
    public double getSuccessRate() { return successRate; }
    public double getSuccessRatePercent() { return successRate * 100.0; }
    
    /**
     * Get a formatted summary of the results.
     */
    public String getSummary() {
        return String.format(
            "%s on %s: %.0f ops/sec, avg=%.2fms, p95=%.2fms, success=%.1f%% (%d/%d ops)",
            scenarioName, databaseName, operationsPerSecond,
            getAverageTimeMs(), getP95TimeMs(), getSuccessRatePercent(),
            getSuccessfulOperations(), totalOperations
        );
    }
    
    /**
     * Get detailed statistics.
     */
    public String getDetailedStats() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=== %s on %s ===\n", scenarioName, databaseName));
        sb.append(String.format("Total Operations: %d\n", totalOperations));
        sb.append(String.format("Failed Operations: %d\n", failedOperations));
        sb.append(String.format("Success Rate: %.2f%%\n", getSuccessRatePercent()));
        sb.append(String.format("Total Time: %d ms\n", totalTimeMs));
        sb.append(String.format("Operations/sec: %.2f\n", operationsPerSecond));
        sb.append("\nLatency Statistics:\n");
        sb.append(String.format("  Average: %.3f ms\n", getAverageTimeMs()));
        sb.append(String.format("  Median:  %.3f ms\n", getMedianTimeMs()));
        sb.append(String.format("  P95:     %.3f ms\n", getP95TimeMs()));
        sb.append(String.format("  P99:     %.3f ms\n", getP99TimeMs()));
        sb.append(String.format("  Min:     %.3f ms\n", getMinTimeMs()));
        sb.append(String.format("  Max:     %.3f ms\n", getMaxTimeMs()));
        return sb.toString();
    }
    
    /**
     * Compare this result with another result.
     */
    public BenchmarkComparison compareTo(BenchmarkResult other) {
        return new BenchmarkComparison(this, other);
    }
    
    /**
     * Comparison between two benchmark results.
     */
    public static class BenchmarkComparison {
        private final BenchmarkResult baseline;
        private final BenchmarkResult comparison;
        
        public BenchmarkComparison(BenchmarkResult baseline, BenchmarkResult comparison) {
            this.baseline = baseline;
            this.comparison = comparison;
        }
        
        public double getThroughputRatio() {
            return baseline.getOperationsPerSecond() > 0 ? 
                comparison.getOperationsPerSecond() / baseline.getOperationsPerSecond() : 0.0;
        }
        
        public double getLatencyRatio() {
            return baseline.getAverageTimeNs() > 0 ? 
                comparison.getAverageTimeNs() / baseline.getAverageTimeNs() : 0.0;
        }
        
        public String getThroughputImprovement() {
            double ratio = getThroughputRatio();
            if (ratio > 1.0) {
                return String.format("%.1fx faster", ratio);
            } else if (ratio < 1.0 && ratio > 0) {
                return String.format("%.1fx slower", 1.0 / ratio);
            } else {
                return "N/A";
            }
        }
        
        public String getLatencyImprovement() {
            double ratio = getLatencyRatio();
            if (ratio < 1.0 && ratio > 0) {
                return String.format("%.1fx lower latency", 1.0 / ratio);
            } else if (ratio > 1.0) {
                return String.format("%.1fx higher latency", ratio);
            } else {
                return "N/A";
            }
        }
        
        public String getSummary() {
            return String.format(
                "%s vs %s (%s): %s throughput, %s",
                comparison.getDatabaseName(), baseline.getDatabaseName(),
                baseline.getScenarioName(),
                getThroughputImprovement(),
                getLatencyImprovement()
            );
        }
    }
}