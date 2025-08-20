package com.memgres.monitoring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for QueryAnalyzer functionality.
 */
public class QueryAnalyzerTest {
    
    private QueryAnalyzer analyzer;
    
    @BeforeEach
    void setUp() {
        analyzer = QueryAnalyzer.getInstance();
        analyzer.reset(); // Start with clean state
    }
    
    @Test
    void testSlowQueryAnalysis() {
        QueryAnalysis analysis = analyzer.analyzeQuery(
            "SELECT * FROM users WHERE id = 1", 2000, 1);
        
        assertTrue(analysis.hasIssues(), "Slow query should have issues");
        assertEquals(QueryAnalyzer.QueryPriority.MEDIUM, analysis.getPriority(), 
            "Slow query should have medium priority");
        
        // Check for slow query issue
        assertTrue(analysis.getIssues().stream()
            .anyMatch(issue -> issue.contains("Slow query")),
            "Should identify slow query issue");
    }
    
    @Test
    void testVerySlowQueryAnalysis() {
        // Set custom thresholds
        analyzer.setThresholds(1000, 3000);
        
        QueryAnalysis analysis = analyzer.analyzeQuery(
            "SELECT * FROM products", 4000, 1000);
        
        assertTrue(analysis.hasIssues(), "Very slow query should have issues");
        assertEquals(QueryAnalyzer.QueryPriority.HIGH, analysis.getPriority(), 
            "Very slow query should have high priority");
        
        // Check for very slow query issue
        assertTrue(analysis.getIssues().stream()
            .anyMatch(issue -> issue.contains("Very slow")),
            "Should identify very slow query issue");
    }
    
    @Test
    void testFullTableScanDetection() {
        QueryAnalysis analysis = analyzer.analyzeQuery(
            "SELECT * FROM users", 500, 10000);
        
        assertTrue(analysis.hasIssues(), "Full table scan should be detected");
        
        // Check for full table scan issue
        assertTrue(analysis.getIssues().stream()
            .anyMatch(issue -> issue.contains("full table scan")),
            "Should detect full table scan");
        
        // Should recommend adding WHERE clause
        assertTrue(analysis.getRecommendations().stream()
            .anyMatch(rec -> rec.contains("WHERE")),
            "Should recommend adding WHERE clause");
    }
    
    @Test
    void testComplexJoinAnalysis() {
        QueryAnalysis analysis = analyzer.analyzeQuery(
            "SELECT * FROM users u JOIN orders o ON u.id = o.user_id " +
            "JOIN products p ON o.product_id = p.id " +
            "JOIN categories c ON p.category_id = c.id", 1500, 100);
        
        assertTrue(analysis.hasIssues(), "Complex join should be flagged");
        
        // Check for complex join issue
        assertTrue(analysis.getIssues().stream()
            .anyMatch(issue -> issue.contains("Complex multi-table join")),
            "Should detect complex joins");
    }
    
    @Test
    void testSubqueryInSelectAnalysis() {
        QueryAnalysis analysis = analyzer.analyzeQuery(
            "SELECT id, (SELECT COUNT(*) FROM orders WHERE user_id = users.id) FROM users", 
            800, 50);
        
        assertTrue(analysis.hasIssues(), "Subquery in SELECT should be flagged");
        
        // Check for subquery issue
        assertTrue(analysis.getIssues().stream()
            .anyMatch(issue -> issue.contains("Subquery in SELECT")),
            "Should detect subquery in SELECT clause");
    }
    
    @Test
    void testLargeResultSetAnalysis() {
        QueryAnalysis analysis = analyzer.analyzeQuery(
            "SELECT * FROM products", 300, 15000);
        
        assertTrue(analysis.hasIssues(), "Large result set should be flagged");
        
        // Check for large result set issue
        assertTrue(analysis.getIssues().stream()
            .anyMatch(issue -> issue.contains("Large result set")),
            "Should detect large result set");
        
        // Should recommend LIMIT
        assertTrue(analysis.getRecommendations().stream()
            .anyMatch(rec -> rec.contains("LIMIT")),
            "Should recommend using LIMIT clause");
    }
    
    @Test
    void testOrderByWithoutLimitAnalysis() {
        QueryAnalysis analysis = analyzer.analyzeQuery(
            "SELECT * FROM users ORDER BY created_date DESC", 600, 1000);
        
        assertTrue(analysis.hasIssues(), "ORDER BY without LIMIT should be flagged");
        
        // Check for ORDER BY issue
        assertTrue(analysis.getIssues().stream()
            .anyMatch(issue -> issue.contains("ORDER BY without LIMIT")),
            "Should detect ORDER BY without LIMIT");
    }
    
    @Test
    void testQueryPatternTracking() {
        String sql1 = "SELECT * FROM users WHERE id = 1";
        String sql2 = "SELECT * FROM users WHERE id = 2";
        String sql3 = "SELECT * FROM products WHERE name = 'test'";
        
        // Execute same pattern multiple times
        analyzer.analyzeQuery(sql1, 100, 1);
        analyzer.analyzeQuery(sql2, 150, 1); // Same pattern, different literal
        analyzer.analyzeQuery(sql3, 200, 1); // Different pattern
        analyzer.analyzeQuery(sql1, 120, 1); // Same pattern again
        
        // Check pattern tracking
        String normalizedPattern1 = "select * from users where id = ?";
        String normalizedPattern2 = "select * from products where name = ?";
        
        QueryAnalyzer.QueryPattern pattern1 = analyzer.getQueryPattern(normalizedPattern1);
        QueryAnalyzer.QueryPattern pattern2 = analyzer.getQueryPattern(normalizedPattern2);
        
        assertNotNull(pattern1, "Should track pattern 1");
        assertNotNull(pattern2, "Should track pattern 2");
        
        assertEquals(3, pattern1.getExecutionCount(), "Pattern 1 should have 3 executions");
        assertEquals(1, pattern2.getExecutionCount(), "Pattern 2 should have 1 execution");
        
        assertEquals(123.33, pattern1.getAverageExecutionTime(), 0.01, "Pattern 1 avg time");
        assertEquals(200.0, pattern2.getAverageExecutionTime(), 0.01, "Pattern 2 avg time");
    }
    
    @Test
    void testFrequentSlowQueryAnalysis() {
        String slowSql = "SELECT * FROM large_table WHERE unindexed_column = 'value'";
        
        // Execute the same slow query multiple times to trigger frequency analysis
        for (int i = 0; i < 101; i++) {
            analyzer.analyzeQuery(slowSql, 1200, 100);
        }
        
        QueryAnalysis analysis = analyzer.analyzeQuery(slowSql, 1200, 100);
        
        assertEquals(QueryAnalyzer.QueryPriority.HIGH, analysis.getPriority(),
            "Frequently executed slow query should have high priority");
        
        assertTrue(analysis.getIssues().stream()
            .anyMatch(issue -> issue.contains("Frequently executed slow query")),
            "Should detect frequently executed slow query");
        
        assertTrue(analysis.getRecommendations().stream()
            .anyMatch(rec -> rec.contains("High-priority optimization")),
            "Should recommend high-priority optimization");
    }
    
    @Test
    void testOptimizationReportGeneration() {
        // Add various queries with different patterns
        analyzer.analyzeQuery("SELECT * FROM users", 2000, 1000); // Slow, high impact
        analyzer.analyzeQuery("SELECT id FROM products", 100, 1);
        
        // Make one query very frequent
        for (int i = 0; i < 50; i++) {
            analyzer.analyzeQuery("SELECT COUNT(*) FROM orders", 200, 1);
        }
        
        // Make another query slow but infrequent
        analyzer.analyzeQuery("SELECT * FROM audit_log WHERE date > ?", 3000, 5000);
        
        OptimizationReport report = analyzer.generateOptimizationReport(5);
        
        assertNotNull(report, "Should generate optimization report");
        assertFalse(report.getTopSlowQueries().isEmpty(), "Should have slow queries");
        assertFalse(report.getMostFrequentQueries().isEmpty(), "Should have frequent queries");
        
        String recommendations = report.generateRecommendations();
        assertNotNull(recommendations, "Should generate recommendations");
        assertTrue(recommendations.contains("MemGres Query Optimization Report"), 
            "Should include report header");
    }
    
    @Test
    void testNoIssuesForGoodQuery() {
        QueryAnalysis analysis = analyzer.analyzeQuery(
            "SELECT id, name FROM users WHERE active = true LIMIT 10", 50, 10);
        
        assertFalse(analysis.hasIssues(), "Well-written query should have no issues");
        assertEquals(QueryAnalyzer.QueryPriority.LOW, analysis.getPriority(),
            "Good query should have low priority");
        assertTrue(analysis.getIssues().isEmpty(), "Should have no issues");
    }
    
    @Test
    void testSqlNormalization() {
        // Test that different literal values result in same pattern
        analyzer.analyzeQuery("SELECT * FROM users WHERE id = 123", 100, 1);
        analyzer.analyzeQuery("SELECT * FROM users WHERE id = 456", 120, 1);
        analyzer.analyzeQuery("SELECT * FROM users WHERE name = 'John'", 110, 1);
        analyzer.analyzeQuery("SELECT * FROM users WHERE name = 'Jane'", 130, 1);
        
        // Should have 2 distinct patterns
        String pattern1 = "select * from users where id = ?";
        String pattern2 = "select * from users where name = ?";
        
        QueryAnalyzer.QueryPattern p1 = analyzer.getQueryPattern(pattern1);
        QueryAnalyzer.QueryPattern p2 = analyzer.getQueryPattern(pattern2);
        
        assertNotNull(p1, "Should normalize numeric literals");
        assertNotNull(p2, "Should normalize string literals");
        
        assertEquals(2, p1.getExecutionCount(), "Should group numeric literal queries");
        assertEquals(2, p2.getExecutionCount(), "Should group string literal queries");
    }
    
    @Test
    void testAnalyzerReset() {
        // Add some patterns
        analyzer.analyzeQuery("SELECT * FROM users", 100, 1);
        analyzer.analyzeQuery("INSERT INTO products VALUES (?)", 80, 0);
        
        assertFalse(analyzer.getAllQueryPatterns().isEmpty(), "Should have patterns before reset");
        
        // Reset and verify clean state
        analyzer.reset();
        
        assertTrue(analyzer.getAllQueryPatterns().isEmpty(), "Should have no patterns after reset");
    }
}