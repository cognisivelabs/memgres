package com.memgres.sql.optimizer;

import com.memgres.storage.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an optimized query execution plan with cost estimates and chosen access methods.
 */
public class QueryExecutionPlan {
    
    private Table primaryTable;
    private AccessMethod accessMethod;
    private String selectedIndex;
    private List<String> predicates;
    private double estimatedCost;
    private long estimatedRowCount;
    
    // Join information
    private boolean hasJoins;
    private List<JoinInfo> joinOperations;
    
    // Aggregation information
    private boolean requiresGrouping;
    private List<String> groupByColumns;
    
    // Sorting information  
    private boolean requiresSorting;
    private List<String> orderByColumns;
    
    public QueryExecutionPlan() {
        this.predicates = new ArrayList<>();
        this.joinOperations = new ArrayList<>();
        this.groupByColumns = new ArrayList<>();
        this.orderByColumns = new ArrayList<>();
        this.estimatedCost = 0.0;
        this.estimatedRowCount = 0;
    }
    
    // Primary table access
    public Table getPrimaryTable() { return primaryTable; }
    public void setPrimaryTable(Table primaryTable) { this.primaryTable = primaryTable; }
    
    public AccessMethod getAccessMethod() { return accessMethod; }
    public void setAccessMethod(AccessMethod accessMethod) { this.accessMethod = accessMethod; }
    
    public String getSelectedIndex() { return selectedIndex; }
    public void setSelectedIndex(String selectedIndex) { this.selectedIndex = selectedIndex; }
    
    public List<String> getPredicates() { return predicates; }
    public void setPredicates(List<String> predicates) { this.predicates = predicates; }
    
    // Cost estimates
    public double getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(double estimatedCost) { this.estimatedCost = estimatedCost; }
    
    public long getEstimatedRowCount() { return estimatedRowCount; }
    public void setEstimatedRowCount(long estimatedRowCount) { this.estimatedRowCount = estimatedRowCount; }
    
    // Join operations
    public boolean hasJoins() { return hasJoins; }
    public void setHasJoins(boolean hasJoins) { this.hasJoins = hasJoins; }
    
    public List<JoinInfo> getJoinOperations() { return joinOperations; }
    public void setJoinOperations(List<JoinInfo> joinOperations) { this.joinOperations = joinOperations; }
    
    // Aggregation
    public boolean requiresGrouping() { return requiresGrouping; }
    public void setRequiresGrouping(boolean requiresGrouping) { this.requiresGrouping = requiresGrouping; }
    
    public List<String> getGroupByColumns() { return groupByColumns; }
    public void setGroupByColumns(List<String> groupByColumns) { this.groupByColumns = groupByColumns; }
    
    // Sorting
    public boolean requiresSorting() { return requiresSorting; }
    public void setRequiresSorting(boolean requiresSorting) { this.requiresSorting = requiresSorting; }
    
    public List<String> getOrderByColumns() { return orderByColumns; }
    public void setOrderByColumns(List<String> orderByColumns) { this.orderByColumns = orderByColumns; }
    
    /**
     * Get a human-readable summary of the execution plan.
     */
    public String getExecutionSummary() {
        StringBuilder summary = new StringBuilder();
        
        if (primaryTable != null) {
            summary.append("Table: ").append(primaryTable.getName()).append("\n");
        }
        
        summary.append("Access Method: ").append(accessMethod);
        if (selectedIndex != null) {
            summary.append(" using index ").append(selectedIndex);
        }
        summary.append("\n");
        
        if (!predicates.isEmpty()) {
            summary.append("Predicates: ").append(String.join(" AND ", predicates)).append("\n");
        }
        
        summary.append("Estimated Cost: ").append(String.format("%.2f", estimatedCost)).append("\n");
        summary.append("Estimated Rows: ").append(estimatedRowCount).append("\n");
        
        if (hasJoins) {
            summary.append("Join Operations: ").append(joinOperations.size()).append("\n");
        }
        
        if (requiresGrouping) {
            summary.append("Grouping: ").append(groupByColumns).append("\n");
        }
        
        if (requiresSorting) {
            summary.append("Ordering: ").append(orderByColumns).append("\n");
        }
        
        return summary.toString();
    }
    
    /**
     * Check if this plan uses index access.
     */
    public boolean usesIndex() {
        return accessMethod == AccessMethod.INDEX_SCAN || 
               accessMethod == AccessMethod.INDEX_SEEK;
    }
    
    /**
     * Get the table name being queried.
     */
    public String getTableName() {
        return primaryTable != null ? primaryTable.getName() : "unknown";
    }
    
    @Override
    public String toString() {
        return String.format("QueryExecutionPlan{table='%s', method=%s, cost=%.2f, rows=%d}",
                getTableName(), accessMethod, estimatedCost, estimatedRowCount);
    }
    
    /**
     * Information about a join operation in the query plan.
     */
    public static class JoinInfo {
        private final String leftTable;
        private final String rightTable;
        private final String joinType;
        private final String joinCondition;
        private final JoinAlgorithm algorithm;
        private final double estimatedCost;
        
        public JoinInfo(String leftTable, String rightTable, String joinType, 
                       String joinCondition, JoinAlgorithm algorithm, double estimatedCost) {
            this.leftTable = leftTable;
            this.rightTable = rightTable;
            this.joinType = joinType;
            this.joinCondition = joinCondition;
            this.algorithm = algorithm;
            this.estimatedCost = estimatedCost;
        }
        
        public String getLeftTable() { return leftTable; }
        public String getRightTable() { return rightTable; }
        public String getJoinType() { return joinType; }
        public String getJoinCondition() { return joinCondition; }
        public JoinAlgorithm getAlgorithm() { return algorithm; }
        public double getEstimatedCost() { return estimatedCost; }
        
        @Override
        public String toString() {
            return String.format("%s %s JOIN %s ON %s (algorithm: %s, cost: %.2f)",
                    leftTable, joinType, rightTable, joinCondition, algorithm, estimatedCost);
        }
    }
    
    /**
     * Available join algorithms.
     */
    public enum JoinAlgorithm {
        NESTED_LOOP,
        HASH_JOIN,
        SORT_MERGE_JOIN
    }
}