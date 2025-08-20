package com.memgres.monitoring;

import java.util.List;

/**
 * Comprehensive optimization report with performance insights and recommendations.
 */
public class OptimizationReport {
    private final List<QueryAnalyzer.QueryPattern> topSlowQueries;
    private final List<QueryAnalyzer.QueryPattern> mostFrequentQueries;
    private final long generatedAt;
    
    public OptimizationReport(List<QueryAnalyzer.QueryPattern> topSlowQueries,
                            List<QueryAnalyzer.QueryPattern> mostFrequentQueries) {
        this.topSlowQueries = List.copyOf(topSlowQueries);
        this.mostFrequentQueries = List.copyOf(mostFrequentQueries);
        this.generatedAt = System.currentTimeMillis();
    }
    
    public List<QueryAnalyzer.QueryPattern> getTopSlowQueries() { return topSlowQueries; }
    public List<QueryAnalyzer.QueryPattern> getMostFrequentQueries() { return mostFrequentQueries; }
    public long getGeneratedAt() { return generatedAt; }
    
    /**
     * Generate human-readable optimization recommendations.
     */
    public String generateRecommendations() {
        StringBuilder report = new StringBuilder();
        
        report.append("MemGres Query Optimization Report\n");
        report.append("Generated at: ").append(new java.util.Date(generatedAt)).append("\n\n");
        
        // Top slow queries analysis
        report.append("TOP SLOW QUERIES (by total impact):\n");
        report.append("=".repeat(50)).append("\n");
        
        for (int i = 0; i < topSlowQueries.size(); i++) {
            QueryAnalyzer.QueryPattern pattern = topSlowQueries.get(i);
            long totalImpact = (long) (pattern.getAverageExecutionTime() * pattern.getExecutionCount());
            
            report.append(String.format("%d. Average: %.2fms | Executions: %d | Total Impact: %dms\n",
                i + 1, pattern.getAverageExecutionTime(), pattern.getExecutionCount(), totalImpact));
            report.append("   SQL: ").append(truncateSql(pattern.getNormalizedSql())).append("\n");
            report.append("   Range: ").append(pattern.getMinExecutionTime())
                  .append("ms - ").append(pattern.getMaxExecutionTime()).append("ms\n");
            report.append("   Avg Rows: ").append(String.format("%.1f", pattern.getAverageRowsReturned())).append("\n\n");
        }
        
        // Most frequent queries analysis
        report.append("\nMOST FREQUENT QUERIES:\n");
        report.append("=".repeat(30)).append("\n");
        
        for (int i = 0; i < mostFrequentQueries.size(); i++) {
            QueryAnalyzer.QueryPattern pattern = mostFrequentQueries.get(i);
            
            report.append(String.format("%d. Executions: %d | Average: %.2fms\n",
                i + 1, pattern.getExecutionCount(), pattern.getAverageExecutionTime()));
            report.append("   SQL: ").append(truncateSql(pattern.getNormalizedSql())).append("\n");
            report.append("   Total Time: ").append(pattern.getTotalExecutionTime()).append("ms\n\n");
        }
        
        // General recommendations
        report.append("\nGENERAL RECOMMENDATIONS:\n");
        report.append("=".repeat(35)).append("\n");
        
        if (!topSlowQueries.isEmpty()) {
            report.append("1. Focus optimization efforts on the top slow queries listed above\n");
            report.append("2. Consider adding indexes on frequently queried columns\n");
            report.append("3. Review WHERE clauses for selectivity improvements\n");
        }
        
        if (!mostFrequentQueries.isEmpty()) {
            report.append("4. Cache results for frequently executed queries where appropriate\n");
            report.append("5. Consider connection pooling for high-frequency access patterns\n");
        }
        
        report.append("6. Monitor query patterns regularly to identify new optimization opportunities\n");
        report.append("7. Use EXPLAIN to understand query execution plans\n");
        
        return report.toString();
    }
    
    private String truncateSql(String sql) {
        if (sql == null) return "null";
        if (sql.length() <= 80) return sql;
        return sql.substring(0, 80) + "...";
    }
    
    @Override
    public String toString() {
        return String.format("OptimizationReport{slowQueries=%d, frequentQueries=%d, generatedAt=%s}",
            topSlowQueries.size(), mostFrequentQueries.size(), new java.util.Date(generatedAt));
    }
}