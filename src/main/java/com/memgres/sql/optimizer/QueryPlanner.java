package com.memgres.sql.optimizer;

import com.memgres.sql.ast.statement.SelectStatement;
import com.memgres.sql.ast.statement.FromClause;
import com.memgres.sql.ast.statement.WhereClause;
import com.memgres.sql.ast.statement.TableReference;
import com.memgres.sql.ast.expression.Expression;
import com.memgres.sql.ast.expression.ColumnReference;
import com.memgres.sql.ast.expression.LiteralExpression;
import com.memgres.sql.ast.expression.BinaryExpression;
import com.memgres.storage.statistics.StatisticsManager;
import com.memgres.storage.Table;
import com.memgres.storage.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Cost-based query planner that uses table statistics to optimize query execution.
 * Simplified version focusing on single table queries for Phase 4.1.
 */
public class QueryPlanner {
    
    private static final Logger logger = LoggerFactory.getLogger(QueryPlanner.class);
    
    private final StatisticsManager statisticsManager;
    private final Schema schema;
    
    public QueryPlanner(StatisticsManager statisticsManager, Schema schema) {
        this.statisticsManager = statisticsManager;
        this.schema = schema;
    }
    
    /**
     * Create an optimized execution plan for a SELECT statement.
     */
    public QueryExecutionPlan planQuery(SelectStatement selectStatement) {
        logger.debug("Planning query execution for SELECT statement");
        
        QueryExecutionPlan plan = new QueryExecutionPlan();
        
        // Extract primary table information from FROM clause
        String tableName = extractPrimaryTableName(selectStatement);
        if (tableName == null) {
            // Handle cases with no FROM clause or complex queries
            plan.setPrimaryTable(null);
            plan.setAccessMethod(AccessMethod.EMPTY_RESULT);
            plan.setEstimatedCost(0.0);
            return plan;
        }
        
        Table table = schema.getTable(tableName);
        if (table == null) {
            logger.warn("Table not found: {}", tableName);
            plan.setPrimaryTable(null);
            plan.setAccessMethod(AccessMethod.TABLE_NOT_FOUND);
            plan.setEstimatedCost(Double.MAX_VALUE);
            return plan;
        }
        
        plan.setPrimaryTable(table);
        
        // For now, use simple costing without complex predicate analysis
        AccessMethodChoice choice = chooseSimpleAccessMethod(tableName);
        plan.setAccessMethod(choice.method);
        plan.setSelectedIndex(choice.indexName);
        plan.setEstimatedCost(choice.cost);
        plan.setEstimatedRowCount(choice.estimatedRows);
        
        logger.debug("Query execution plan: {}", plan);
        return plan;
    }
    
    /**
     * Extract the primary table name from the FROM clause.
     */
    private String extractPrimaryTableName(SelectStatement selectStatement) {
        Optional<FromClause> fromClause = selectStatement.getFromClause();
        if (!fromClause.isPresent()) {
            return null;
        }
        
        try {
            List<TableReference> tables = fromClause.get().getTableReferences();
            if (tables != null && !tables.isEmpty()) {
                return tables.get(0).getTableName();
            }
        } catch (Exception e) {
            logger.debug("Error extracting table name: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Choose access method with simple cost calculation.
     */
    private AccessMethodChoice chooseSimpleAccessMethod(String tableName) {
        // For Phase 4.1, use simple logic: prefer table scan for now
        // This will be enhanced in later phases
        
        double scanCost = statisticsManager.estimateScanCost(tableName);
        long rowCount;
        
        try {
            rowCount = statisticsManager.getTableStatistics(tableName).getRowCount();
        } catch (Exception e) {
            logger.debug("Error getting table statistics for {}: {}", tableName, e.getMessage());
            rowCount = 1000; // Default estimate
            scanCost = 1000.0; // Default cost
        }
        
        return new AccessMethodChoice(AccessMethod.TABLE_SCAN, null, scanCost, rowCount);
    }
    
    /**
     * Represents a choice of access method with cost estimates.
     */
    private static class AccessMethodChoice {
        final AccessMethod method;
        final String indexName;
        final double cost;
        final long estimatedRows;
        
        AccessMethodChoice(AccessMethod method, String indexName, double cost, long estimatedRows) {
            this.method = method;
            this.indexName = indexName;
            this.cost = cost;
            this.estimatedRows = estimatedRows;
        }
    }
}