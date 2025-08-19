package com.memgres.storage.statistics;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Maintains statistics for a single database column.
 * Used for selectivity estimation and query optimization.
 */
public class ColumnStatistics {
    
    private final String columnName;
    private final AtomicLong distinctValues; // Cardinality
    private final AtomicLong nullCount;
    private volatile Object minValue;
    private volatile Object maxValue;
    private final Map<Object, Long> valueFrequency; // Most common values
    private volatile double averageLength; // For string columns
    
    // Histogram for range queries (simplified)
    private final int maxHistogramBuckets = 10;
    private volatile List<HistogramBucket> histogram;
    
    public ColumnStatistics(String columnName) {
        this.columnName = columnName;
        this.distinctValues = new AtomicLong(0);
        this.nullCount = new AtomicLong(0);
        this.valueFrequency = new HashMap<>();
        this.averageLength = 0.0;
        this.histogram = new ArrayList<>();
    }
    
    /**
     * Update statistics based on column data analysis.
     */
    public void updateFromData(List<Object> columnValues) {
        Set<Object> distinct = new HashSet<>();
        Map<Object, Long> frequency = new HashMap<>();
        long nulls = 0;
        double totalLength = 0.0;
        Object min = null, max = null;
        
        for (Object value : columnValues) {
            if (value == null) {
                nulls++;
                continue;
            }
            
            distinct.add(value);
            frequency.merge(value, 1L, Long::sum);
            
            // Update min/max for comparable values
            if (value instanceof Comparable) {
                @SuppressWarnings("unchecked")
                Comparable<Object> comparableValue = (Comparable<Object>) value;
                if (min == null || comparableValue.compareTo(min) < 0) {
                    min = value;
                }
                if (max == null || comparableValue.compareTo(max) > 0) {
                    max = value;
                }
            }
            
            // Calculate average length for strings
            if (value instanceof String) {
                totalLength += ((String) value).length();
            }
        }
        
        // Update statistics
        distinctValues.set(distinct.size());
        nullCount.set(nulls);
        minValue = min;
        maxValue = max;
        
        // Keep only most frequent values (top 20)
        valueFrequency.clear();
        frequency.entrySet().stream()
                .sorted(Map.Entry.<Object, Long>comparingByValue().reversed())
                .limit(20)
                .forEach(entry -> valueFrequency.put(entry.getKey(), entry.getValue()));
        
        if (!columnValues.isEmpty() && totalLength > 0) {
            averageLength = totalLength / (columnValues.size() - nulls);
        }
        
        // Build simple histogram for range queries
        buildHistogram(columnValues);
    }
    
    /**
     * Estimate selectivity for equality predicate (column = value).
     */
    public double estimateEqualitySelectivity(Object value) {
        if (value == null) {
            // NULL selectivity
            long totalRows = distinctValues.get() * 2; // Rough estimate
            return nullCount.get() / (double) Math.max(totalRows, 1);
        }
        
        Long frequency = valueFrequency.get(value);
        if (frequency != null) {
            // Known frequent value
            long totalRows = valueFrequency.values().stream().mapToLong(Long::longValue).sum();
            return frequency / (double) Math.max(totalRows, 1);
        }
        
        // Unknown value - estimate based on cardinality
        long cardinality = distinctValues.get();
        return cardinality > 0 ? 1.0 / cardinality : 0.1;
    }
    
    /**
     * Estimate selectivity for range predicate (column BETWEEN min AND max).
     */
    @SuppressWarnings("unchecked")
    public double estimateRangeSelectivity(Object minVal, Object maxVal) {
        if (minValue == null || maxValue == null || !(minValue instanceof Comparable)) {
            return 0.3; // Default estimate
        }
        
        try {
            Comparable<Object> colMin = (Comparable<Object>) minValue;
            Comparable<Object> colMax = (Comparable<Object>) maxValue;
            Comparable<Object> queryMin = (Comparable<Object>) minVal;
            Comparable<Object> queryMax = (Comparable<Object>) maxVal;
            
            // Simple linear interpolation
            if (queryMin.compareTo(colMax) > 0 || queryMax.compareTo(colMin) < 0) {
                return 0.0; // No overlap
            }
            
            // Calculate overlap ratio (simplified)
            return 0.3; // Placeholder - would need proper range calculation
            
        } catch (ClassCastException e) {
            return 0.3; // Fallback
        }
    }
    
    /**
     * Build histogram for range selectivity estimation.
     */
    private void buildHistogram(List<Object> values) {
        List<HistogramBucket> buckets = new ArrayList<>();
        
        // Simple implementation - equal-width histogram
        if (minValue instanceof Comparable && maxValue instanceof Comparable && !values.isEmpty()) {
            // For now, just create empty buckets - can be enhanced later
            for (int i = 0; i < Math.min(maxHistogramBuckets, distinctValues.get()); i++) {
                buckets.add(new HistogramBucket(null, null, 0));
            }
        }
        
        this.histogram = buckets;
    }
    
    // Getters
    public String getColumnName() { return columnName; }
    public long getDistinctValues() { return distinctValues.get(); }
    public long getNullCount() { return nullCount.get(); }
    public Object getMinValue() { return minValue; }
    public Object getMaxValue() { return maxValue; }
    public double getAverageLength() { return averageLength; }
    public Map<Object, Long> getValueFrequency() { return new HashMap<>(valueFrequency); }
    
    /**
     * Get cardinality ratio (distinctness).
     */
    public double getCardinalityRatio(long totalRows) {
        return totalRows > 0 ? distinctValues.get() / (double) totalRows : 0.0;
    }
    
    @Override
    public String toString() {
        return String.format("ColumnStatistics{column='%s', distinct=%d, nulls=%d, min=%s, max=%s, avgLen=%.1f}",
                columnName, distinctValues.get(), nullCount.get(), minValue, maxValue, averageLength);
    }
    
    /**
     * Simple histogram bucket for range estimation.
     */
    public static class HistogramBucket {
        private final Object minValue;
        private final Object maxValue;
        private final long count;
        
        public HistogramBucket(Object minValue, Object maxValue, long count) {
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.count = count;
        }
        
        public Object getMinValue() { return minValue; }
        public Object getMaxValue() { return maxValue; }
        public long getCount() { return count; }
    }
}