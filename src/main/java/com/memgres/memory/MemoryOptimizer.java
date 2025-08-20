package com.memgres.memory;

import com.memgres.core.MemGresEngine;
import com.memgres.storage.Schema;
import com.memgres.storage.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Provides memory optimization strategies for the MemGres database.
 * Implements various techniques to reduce memory footprint and improve efficiency.
 */
public class MemoryOptimizer {
    private static final Logger logger = LoggerFactory.getLogger(MemoryOptimizer.class);
    
    private final MemGresEngine engine;
    private final MemoryManager memoryManager;
    private volatile ScheduledExecutorService optimizationExecutor;
    
    // Optimization settings
    private volatile boolean autoOptimizationEnabled = true;
    private volatile long optimizationIntervalMs = 30000; // 30 seconds
    private volatile double memoryPressureThreshold = 0.75; // 75% memory usage
    
    // Statistics
    private final AtomicLong totalOptimizations = new AtomicLong();
    private final AtomicLong totalMemoryReclaimed = new AtomicLong();
    private final Map<String, OptimizationStrategy> strategies = new ConcurrentHashMap<>();
    
    // Cache for frequently accessed data
    private final Map<String, WeakReference<Object>> weakCache = new ConcurrentHashMap<>();
    
    public MemoryOptimizer(MemGresEngine engine) {
        this.engine = engine;
        this.memoryManager = MemoryManager.getInstance();
        
        this.optimizationExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "MemoryOptimizer");
            thread.setDaemon(true);
            return thread;
        });
        
        registerDefaultStrategies();
        startAutoOptimization();
    }
    
    /**
     * Register default optimization strategies.
     */
    private void registerDefaultStrategies() {
        // Strategy 1: Compact sparse tables
        registerStrategy("compact_sparse_tables", new CompactSparseTablesStrategy());
        
        // Strategy 2: Clear unused indexes
        registerStrategy("clear_unused_indexes", new ClearUnusedIndexesStrategy());
        
        // Strategy 3: Trim string values
        registerStrategy("trim_strings", new TrimStringValuesStrategy());
        
        // Strategy 4: Release expired cache entries
        registerStrategy("release_cache", new ReleaseCacheStrategy());
        
        // Strategy 5: Optimize large objects
        registerStrategy("optimize_large_objects", new OptimizeLargeObjectsStrategy());
        
        logger.info("Registered {} optimization strategies", strategies.size());
    }
    
    /**
     * Start automatic optimization.
     */
    private void startAutoOptimization() {
        if (autoOptimizationEnabled) {
            optimizationExecutor.scheduleWithFixedDelay(
                this::performAutoOptimization,
                optimizationIntervalMs,
                optimizationIntervalMs,
                TimeUnit.MILLISECONDS
            );
            logger.info("Auto-optimization started with interval: {}ms", optimizationIntervalMs);
        }
    }
    
    /**
     * Perform automatic optimization based on memory pressure.
     */
    private void performAutoOptimization() {
        try {
            MemoryManager.MemoryStatistics stats = memoryManager.getStatistics();
            double memoryUsage = stats.getHeapUsagePercentage() / 100.0;
            
            if (memoryUsage > memoryPressureThreshold) {
                logger.info("Memory pressure detected ({}%), running optimization", 
                    (int)(memoryUsage * 100));
                OptimizationResult result = optimize();
                logger.info("Optimization completed: {} bytes reclaimed", result.getMemoryReclaimed());
            }
        } catch (Exception e) {
            logger.error("Error during auto-optimization", e);
        }
    }
    
    /**
     * Run all optimization strategies.
     */
    public OptimizationResult optimize() {
        return optimize(new HashSet<>(strategies.keySet()));
    }
    
    /**
     * Run specific optimization strategies.
     */
    public OptimizationResult optimize(Set<String> strategyNames) {
        long startTime = System.currentTimeMillis();
        long memoryBefore = memoryManager.getStatistics().getHeapUsage().getUsed();
        
        Map<String, Long> strategyResults = new HashMap<>();
        long totalReclaimed = 0;
        
        for (String strategyName : strategyNames) {
            OptimizationStrategy strategy = strategies.get(strategyName);
            if (strategy != null) {
                try {
                    long reclaimed = strategy.optimize(engine);
                    strategyResults.put(strategyName, reclaimed);
                    totalReclaimed += reclaimed;
                    logger.debug("Strategy '{}' reclaimed {} bytes", strategyName, reclaimed);
                } catch (Exception e) {
                    logger.error("Error executing strategy '{}'", strategyName, e);
                    strategyResults.put(strategyName, 0L);
                }
            }
        }
        
        // Suggest GC after optimization
        memoryManager.suggestGarbageCollection();
        
        long memoryAfter = memoryManager.getStatistics().getHeapUsage().getUsed();
        long actualReclaimed = Math.max(0, memoryBefore - memoryAfter);
        
        totalOptimizations.incrementAndGet();
        totalMemoryReclaimed.addAndGet(actualReclaimed);
        
        long duration = System.currentTimeMillis() - startTime;
        
        return new OptimizationResult(
            actualReclaimed,
            duration,
            strategyResults,
            memoryBefore,
            memoryAfter
        );
    }
    
    /**
     * Register a custom optimization strategy.
     */
    public void registerStrategy(String name, OptimizationStrategy strategy) {
        strategies.put(name, strategy);
        logger.info("Registered optimization strategy: {}", name);
    }
    
    /**
     * Set auto-optimization parameters.
     */
    public void setAutoOptimization(boolean enabled, long intervalMs, double threshold) {
        this.autoOptimizationEnabled = enabled;
        this.optimizationIntervalMs = intervalMs;
        this.memoryPressureThreshold = threshold;
        
        // Restart optimization if needed
        optimizationExecutor.shutdown();
        try {
            if (!optimizationExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                optimizationExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            optimizationExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        if (enabled) {
            // Create new executor since the old one was shutdown
            this.optimizationExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "MemoryOptimizer");
                thread.setDaemon(true);
                return thread;
            });
            startAutoOptimization();
        }
        
        logger.info("Auto-optimization updated: enabled={}, interval={}ms, threshold={}%",
            enabled, intervalMs, (int)(threshold * 100));
    }
    
    /**
     * Get optimization statistics.
     */
    public OptimizationStatistics getStatistics() {
        return new OptimizationStatistics(
            totalOptimizations.get(),
            totalMemoryReclaimed.get(),
            strategies.size(),
            autoOptimizationEnabled,
            optimizationIntervalMs,
            memoryPressureThreshold
        );
    }
    
    /**
     * Shutdown the optimizer.
     */
    public void shutdown() {
        optimizationExecutor.shutdown();
        try {
            if (!optimizationExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                optimizationExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            optimizationExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("Memory optimizer shutdown complete");
    }
    
    /**
     * Interface for optimization strategies.
     */
    public interface OptimizationStrategy {
        long optimize(MemGresEngine engine);
    }
    
    /**
     * Strategy to compact sparse tables.
     */
    private static class CompactSparseTablesStrategy implements OptimizationStrategy {
        @Override
        public long optimize(MemGresEngine engine) {
            long reclaimed = 0;
            
            for (Schema schema : engine.getAllSchemas()) {
                for (String tableName : schema.getTableNames()) {
                    Table table = schema.getTable(tableName);
                    if (table != null) {
                        // Compact table if it has significant deleted rows
                        // This is a placeholder - actual implementation would compact the storage
                        reclaimed += 0; // Would return actual bytes reclaimed
                    }
                }
            }
            
            return reclaimed;
        }
    }
    
    /**
     * Strategy to clear unused indexes.
     */
    private static class ClearUnusedIndexesStrategy implements OptimizationStrategy {
        @Override
        public long optimize(MemGresEngine engine) {
            // Clear index caches that haven't been used recently
            // This is a placeholder - actual implementation would track index usage
            return 0;
        }
    }
    
    /**
     * Strategy to trim string values.
     */
    private static class TrimStringValuesStrategy implements OptimizationStrategy {
        @Override
        public long optimize(MemGresEngine engine) {
            // Trim excess capacity in string storage
            // This is a placeholder - actual implementation would compact strings
            return 0;
        }
    }
    
    /**
     * Strategy to release cache entries.
     */
    private static class ReleaseCacheStrategy implements OptimizationStrategy {
        @Override
        public long optimize(MemGresEngine engine) {
            // Clear various caches
            System.runFinalization(); // Help clear weak references
            return 0;
        }
    }
    
    /**
     * Strategy to optimize large objects.
     */
    private static class OptimizeLargeObjectsStrategy implements OptimizationStrategy {
        @Override
        public long optimize(MemGresEngine engine) {
            // Compress or offload large objects
            // This is a placeholder - actual implementation would handle LOBs
            return 0;
        }
    }
    
    /**
     * Result of an optimization run.
     */
    public static class OptimizationResult {
        private final long memoryReclaimed;
        private final long durationMs;
        private final Map<String, Long> strategyResults;
        private final long memoryBefore;
        private final long memoryAfter;
        
        public OptimizationResult(long memoryReclaimed, long durationMs,
                                 Map<String, Long> strategyResults,
                                 long memoryBefore, long memoryAfter) {
            this.memoryReclaimed = memoryReclaimed;
            this.durationMs = durationMs;
            this.strategyResults = new HashMap<>(strategyResults);
            this.memoryBefore = memoryBefore;
            this.memoryAfter = memoryAfter;
        }
        
        public long getMemoryReclaimed() { return memoryReclaimed; }
        public long getDurationMs() { return durationMs; }
        public Map<String, Long> getStrategyResults() { return new HashMap<>(strategyResults); }
        public long getMemoryBefore() { return memoryBefore; }
        public long getMemoryAfter() { return memoryAfter; }
    }
    
    /**
     * Optimization statistics.
     */
    public static class OptimizationStatistics {
        private final long totalOptimizations;
        private final long totalMemoryReclaimed;
        private final int strategyCount;
        private final boolean autoOptimizationEnabled;
        private final long optimizationIntervalMs;
        private final double memoryPressureThreshold;
        
        public OptimizationStatistics(long totalOptimizations, long totalMemoryReclaimed,
                                     int strategyCount, boolean autoOptimizationEnabled,
                                     long optimizationIntervalMs, double memoryPressureThreshold) {
            this.totalOptimizations = totalOptimizations;
            this.totalMemoryReclaimed = totalMemoryReclaimed;
            this.strategyCount = strategyCount;
            this.autoOptimizationEnabled = autoOptimizationEnabled;
            this.optimizationIntervalMs = optimizationIntervalMs;
            this.memoryPressureThreshold = memoryPressureThreshold;
        }
        
        public long getTotalOptimizations() { return totalOptimizations; }
        public long getTotalMemoryReclaimed() { return totalMemoryReclaimed; }
        public int getStrategyCount() { return strategyCount; }
        public boolean isAutoOptimizationEnabled() { return autoOptimizationEnabled; }
        public long getOptimizationIntervalMs() { return optimizationIntervalMs; }
        public double getMemoryPressureThreshold() { return memoryPressureThreshold; }
    }
}