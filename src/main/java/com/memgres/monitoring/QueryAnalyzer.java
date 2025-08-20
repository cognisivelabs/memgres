package com.memgres.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Advanced query analysis and optimization recommendations for MemGres.
 * Analyzes query patterns, identifies performance issues, and suggests optimizations.
 */
public class QueryAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(QueryAnalyzer.class);
    private static final QueryAnalyzer INSTANCE = new QueryAnalyzer();
    
    // Query pattern tracking
    private final Map<String, QueryPattern> queryPatterns = new ConcurrentHashMap<>();
    
    // Performance thresholds
    private volatile long slowQueryThreshold = 1000; // milliseconds
    private volatile long verySlowQueryThreshold = 5000; // milliseconds
    
    // Pattern matching for common issues
    private static final Pattern FULL_TABLE_SCAN = Pattern.compile(
        "SELECT\\s+\\*\\s+FROM\\s+\\w+(?:\\s+WHERE\\s+.*)?(?:\\s+ORDER\\s+BY\\s+.*)?",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    
    private static final Pattern MISSING_WHERE = Pattern.compile(
        "SELECT\\s+.*\\s+FROM\\s+\\w+(?:\\s+ORDER\\s+BY\\s+.*)?\\s*$",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    
    private static final Pattern COMPLEX_JOIN = Pattern.compile(
        ".*JOIN.*JOIN.*JOIN.*",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    
    private static final Pattern SUBQUERY_IN_SELECT = Pattern.compile(
        "SELECT\\s+.*\\(\\s*SELECT\\s+.*",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    
    private QueryAnalyzer() {
        logger.info("Query analyzer initialized with thresholds - slow: {}ms, very slow: {}ms",
            slowQueryThreshold, verySlowQueryThreshold);
    }
    
    public static QueryAnalyzer getInstance() {
        return INSTANCE;
    }
    
    /**
     * Analyze a query execution and provide performance insights.
     */
    public QueryAnalysis analyzeQuery(String sql, long executionTimeMs, int rowsReturned) {
        try {
            // Set MDC for structured logging
            MDC.put("analysisExecutionTime", String.valueOf(executionTimeMs));
            MDC.put("analysisRowsReturned", String.valueOf(rowsReturned));
            
            // Normalize SQL for pattern recognition
            String normalizedSql = normalizeSql(sql);
            
            // Track query pattern
            QueryPattern pattern = queryPatterns.computeIfAbsent(normalizedSql, k -> new QueryPattern(k));
            pattern.recordExecution(executionTimeMs, rowsReturned);
            
            // Perform analysis
            List<String> issues = new ArrayList<>();
            List<String> recommendations = new ArrayList<>();
            QueryPriority priority = QueryPriority.LOW;
            
            // Performance analysis
            if (executionTimeMs >= verySlowQueryThreshold) {
                issues.add("Very slow query execution (" + executionTimeMs + "ms)");
                recommendations.add("Consider adding indexes on frequently queried columns");
                recommendations.add("Review query structure for optimization opportunities");
                priority = QueryPriority.HIGH;
            } else if (executionTimeMs >= slowQueryThreshold) {
                issues.add("Slow query execution (" + executionTimeMs + "ms)");
                recommendations.add("Consider query optimization or indexing");
                priority = QueryPriority.MEDIUM;
            }
            
            // Pattern-based analysis
            analyzeQueryPatterns(sql, issues, recommendations, priority);
            
            // Row count analysis
            if (rowsReturned > 10000) {
                issues.add("Large result set returned (" + rowsReturned + " rows)");
                recommendations.add("Consider using LIMIT clause or pagination");
                if (priority == QueryPriority.LOW) priority = QueryPriority.MEDIUM;
            }
            
            // Frequency analysis
            if (pattern.getExecutionCount() > 100) {
                if (pattern.getAverageExecutionTime() > slowQueryThreshold) {
                    issues.add("Frequently executed slow query");
                    recommendations.add("High-priority optimization candidate - optimize this query first");
                    priority = QueryPriority.HIGH;
                } else if (rowsReturned == 0 && sql.toUpperCase().trim().startsWith("SELECT")) {
                    issues.add("Frequently executed query returning no results");
                    recommendations.add("Consider caching or query logic review");
                }
            }
            
            QueryAnalysis analysis = new QueryAnalysis(
                normalizedSql, executionTimeMs, rowsReturned, issues, recommendations, priority);
            
            // Log analysis results
            if (!issues.isEmpty()) {
                logger.warn("Query analysis found {} issues | Priority: {} | Time: {}ms | SQL: {}",
                    issues.size(), priority, executionTimeMs, truncateSql(sql));
                logger.debug("Issues: {}", issues);
                logger.debug("Recommendations: {}", recommendations);
            } else {
                logger.debug("Query analysis: no issues found | Time: {}ms | Rows: {}",
                    executionTimeMs, rowsReturned);
            }
            
            return analysis;
            
        } finally {
            MDC.remove("analysisExecutionTime");
            MDC.remove("analysisRowsReturned");
        }
    }
    
    /**
     * Get analysis for a specific query pattern.
     */
    public QueryPattern getQueryPattern(String normalizedSql) {
        return queryPatterns.get(normalizedSql);
    }
    
    /**
     * Get all tracked query patterns.
     */
    public Map<String, QueryPattern> getAllQueryPatterns() {
        return new ConcurrentHashMap<>(queryPatterns);
    }
    
    /**
     * Generate optimization report for top slow queries.
     */
    public OptimizationReport generateOptimizationReport(int topCount) {
        List<QueryPattern> topSlowQueries = queryPatterns.values().stream()
            .filter(p -> p.getExecutionCount() > 0)
            .sorted((a, b) -> {
                // Sort by total time impact (average time * execution count)
                long impactA = (long) (a.getAverageExecutionTime() * a.getExecutionCount());
                long impactB = (long) (b.getAverageExecutionTime() * b.getExecutionCount());
                return Long.compare(impactB, impactA);
            })
            .limit(topCount)
            .toList();
        
        List<QueryPattern> mostFrequent = queryPatterns.values().stream()
            .filter(p -> p.getExecutionCount() > 0)
            .sorted((a, b) -> Long.compare(b.getExecutionCount(), a.getExecutionCount()))
            .limit(topCount)
            .toList();
        
        OptimizationReport report = new OptimizationReport(topSlowQueries, mostFrequent);
        
        logger.info("Generated optimization report: {} slow queries, {} frequent queries analyzed",
            topSlowQueries.size(), mostFrequent.size());
        
        return report;
    }
    
    /**
     * Clear all query analysis data.
     */
    public void reset() {
        queryPatterns.clear();
        logger.info("Query analyzer data reset");
    }
    
    private void analyzeQueryPatterns(String sql, List<String> issues, List<String> recommendations, QueryPriority priority) {
        String upperSql = sql.toUpperCase();
        
        // Full table scan detection
        if (FULL_TABLE_SCAN.matcher(sql).matches() && !sql.contains("WHERE")) {
            issues.add("Potential full table scan - SELECT * without WHERE clause");
            recommendations.add("Add WHERE clause to filter results");
            recommendations.add("Consider selecting only needed columns instead of *");
        }
        
        // Missing WHERE clause
        if (upperSql.contains("SELECT") && upperSql.contains("FROM") && !upperSql.contains("WHERE")) {
            issues.add("Query without WHERE clause may scan entire table");
            recommendations.add("Add appropriate WHERE conditions");
        }
        
        // Complex joins
        if (COMPLEX_JOIN.matcher(sql).matches()) {
            issues.add("Complex multi-table join detected");
            recommendations.add("Consider breaking into smaller queries or adding appropriate indexes");
            recommendations.add("Verify join order is optimal");
        }
        
        // Subquery in SELECT
        if (SUBQUERY_IN_SELECT.matcher(sql).matches()) {
            issues.add("Subquery in SELECT clause may cause performance issues");
            recommendations.add("Consider using JOIN instead of subquery");
            recommendations.add("Evaluate if subquery can be moved to WHERE clause");
        }
        
        // ORDER BY without LIMIT
        if (upperSql.contains("ORDER BY") && !upperSql.contains("LIMIT")) {
            issues.add("ORDER BY without LIMIT may sort unnecessary data");
            recommendations.add("Consider adding LIMIT clause if appropriate");
        }
        
        // Large IN clauses
        if (upperSql.contains(" IN (") && countCommas(sql, upperSql.indexOf(" IN (")) > 100) {
            issues.add("Large IN clause with many values");
            recommendations.add("Consider using temporary table or EXISTS clause");
        }
    }
    
    private String normalizeSql(String sql) {
        if (sql == null) return "";
        
        // Remove extra whitespace and normalize case
        String normalized = sql.trim().replaceAll("\\s+", " ");
        
        // Replace literal values with placeholders for pattern recognition
        normalized = normalized.replaceAll("'[^']*'", "?"); // String literals
        normalized = normalized.replaceAll("\\b\\d+\\b", "?"); // Numeric literals
        
        return normalized.toLowerCase();
    }
    
    private int countCommas(String sql, int startIndex) {
        int count = 0;
        int endIndex = sql.indexOf(')', startIndex);
        if (endIndex == -1) return 0;
        
        for (int i = startIndex; i < endIndex; i++) {
            if (sql.charAt(i) == ',') count++;
        }
        return count;
    }
    
    private String truncateSql(String sql) {
        if (sql == null) return "null";
        if (sql.length() <= 100) return sql;
        return sql.substring(0, 100) + "...";
    }
    
    /**
     * Set analysis thresholds.
     */
    public void setThresholds(long slowQueryThreshold, long verySlowQueryThreshold) {
        this.slowQueryThreshold = slowQueryThreshold;
        this.verySlowQueryThreshold = verySlowQueryThreshold;
        logger.info("Query analysis thresholds updated - slow: {}ms, very slow: {}ms",
            slowQueryThreshold, verySlowQueryThreshold);
    }
    
    // Inner classes for analysis results
    public static class QueryPattern {
        private final String normalizedSql;
        private final LongAdder executionCount = new LongAdder();
        private final LongAdder totalExecutionTime = new LongAdder();
        private final LongAdder totalRowsReturned = new LongAdder();
        private final AtomicLong maxExecutionTime = new AtomicLong(0);
        private final AtomicLong minExecutionTime = new AtomicLong(Long.MAX_VALUE);
        
        public QueryPattern(String normalizedSql) {
            this.normalizedSql = normalizedSql;
        }
        
        public void recordExecution(long executionTimeMs, int rowsReturned) {
            executionCount.increment();
            totalExecutionTime.add(executionTimeMs);
            totalRowsReturned.add(rowsReturned);
            
            // Update min/max execution times
            long currentMax = maxExecutionTime.get();
            if (executionTimeMs > currentMax) {
                maxExecutionTime.compareAndSet(currentMax, executionTimeMs);
            }
            
            long currentMin = minExecutionTime.get();
            if (executionTimeMs < currentMin) {
                minExecutionTime.compareAndSet(currentMin, executionTimeMs);
            }
        }
        
        public String getNormalizedSql() { return normalizedSql; }
        public long getExecutionCount() { return executionCount.sum(); }
        public long getTotalExecutionTime() { return totalExecutionTime.sum(); }
        public long getTotalRowsReturned() { return totalRowsReturned.sum(); }
        public long getMaxExecutionTime() { return maxExecutionTime.get(); }
        public long getMinExecutionTime() { 
            long min = minExecutionTime.get();
            return min == Long.MAX_VALUE ? 0 : min;
        }
        
        public double getAverageExecutionTime() {
            long count = executionCount.sum();
            return count > 0 ? (double) totalExecutionTime.sum() / count : 0.0;
        }
        
        public double getAverageRowsReturned() {
            long count = executionCount.sum();
            return count > 0 ? (double) totalRowsReturned.sum() / count : 0.0;
        }
    }
    
    public enum QueryPriority {
        LOW, MEDIUM, HIGH
    }
}