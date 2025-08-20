package com.memgres.monitoring;

import java.util.List;

/**
 * Results of query performance analysis.
 */
public class QueryAnalysis {
    private final String normalizedSql;
    private final long executionTimeMs;
    private final int rowsReturned;
    private final List<String> issues;
    private final List<String> recommendations;
    private final QueryAnalyzer.QueryPriority priority;
    
    public QueryAnalysis(String normalizedSql, long executionTimeMs, int rowsReturned,
                        List<String> issues, List<String> recommendations,
                        QueryAnalyzer.QueryPriority priority) {
        this.normalizedSql = normalizedSql;
        this.executionTimeMs = executionTimeMs;
        this.rowsReturned = rowsReturned;
        this.issues = List.copyOf(issues);
        this.recommendations = List.copyOf(recommendations);
        this.priority = priority;
    }
    
    public String getNormalizedSql() { return normalizedSql; }
    public long getExecutionTimeMs() { return executionTimeMs; }
    public int getRowsReturned() { return rowsReturned; }
    public List<String> getIssues() { return issues; }
    public List<String> getRecommendations() { return recommendations; }
    public QueryAnalyzer.QueryPriority getPriority() { return priority; }
    
    public boolean hasIssues() {
        return !issues.isEmpty();
    }
    
    @Override
    public String toString() {
        return String.format(
            "QueryAnalysis{executionTime=%dms, rows=%d, issues=%d, priority=%s}",
            executionTimeMs, rowsReturned, issues.size(), priority
        );
    }
}