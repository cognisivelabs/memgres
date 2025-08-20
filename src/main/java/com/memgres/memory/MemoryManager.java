package com.memgres.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Manages memory usage across the MemGres database system.
 * Provides monitoring, alerting, and optimization capabilities.
 */
public class MemoryManager {
    private static final Logger logger = LoggerFactory.getLogger(MemoryManager.class);
    
    private static final double DEFAULT_HIGH_MEMORY_THRESHOLD = 0.85; // 85% of max heap
    private static final double DEFAULT_CRITICAL_MEMORY_THRESHOLD = 0.95; // 95% of max heap
    private static final long MONITORING_INTERVAL_MS = 5000; // 5 seconds
    
    private final MemoryMXBean memoryMXBean;
    private final List<GarbageCollectorMXBean> gcBeans;
    private final List<MemoryPoolMXBean> memoryPoolBeans;
    
    private final AtomicLong totalAllocatedMemory = new AtomicLong();
    private final AtomicLong totalFreedMemory = new AtomicLong();
    private final Map<String, MemoryAllocation> allocations = new ConcurrentHashMap<>();
    
    private final ScheduledExecutorService monitoringExecutor;
    private final List<Consumer<MemoryAlert>> alertHandlers = new CopyOnWriteArrayList<>();
    
    private volatile double highMemoryThreshold = DEFAULT_HIGH_MEMORY_THRESHOLD;
    private volatile double criticalMemoryThreshold = DEFAULT_CRITICAL_MEMORY_THRESHOLD;
    private volatile boolean monitoringEnabled = true;
    
    // Singleton instance
    private static volatile MemoryManager instance;
    
    /**
     * Get the singleton instance of MemoryManager.
     */
    public static MemoryManager getInstance() {
        if (instance == null) {
            synchronized (MemoryManager.class) {
                if (instance == null) {
                    instance = new MemoryManager();
                }
            }
        }
        return instance;
    }
    
    private MemoryManager() {
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
        this.gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        this.memoryPoolBeans = ManagementFactory.getMemoryPoolMXBeans();
        
        this.monitoringExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "MemoryManager-Monitor");
            thread.setDaemon(true);
            return thread;
        });
        
        startMonitoring();
    }
    
    /**
     * Start memory monitoring.
     */
    private void startMonitoring() {
        monitoringExecutor.scheduleWithFixedDelay(this::checkMemoryUsage, 
            MONITORING_INTERVAL_MS, MONITORING_INTERVAL_MS, TimeUnit.MILLISECONDS);
        logger.info("Memory monitoring started with interval: {}ms", MONITORING_INTERVAL_MS);
    }
    
    /**
     * Check current memory usage and trigger alerts if necessary.
     */
    private void checkMemoryUsage() {
        if (!monitoringEnabled) {
            return;
        }
        
        try {
            MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
            double usageRatio = (double) heapUsage.getUsed() / heapUsage.getMax();
            
            if (usageRatio > criticalMemoryThreshold) {
                triggerAlert(new MemoryAlert(
                    MemoryAlert.Level.CRITICAL,
                    String.format("Critical memory usage: %.1f%% (threshold: %.1f%%)", 
                        usageRatio * 100, criticalMemoryThreshold * 100),
                    heapUsage
                ));
                
                // Attempt to free memory
                suggestGarbageCollection();
                
            } else if (usageRatio > highMemoryThreshold) {
                triggerAlert(new MemoryAlert(
                    MemoryAlert.Level.HIGH,
                    String.format("High memory usage: %.1f%% (threshold: %.1f%%)", 
                        usageRatio * 100, highMemoryThreshold * 100),
                    heapUsage
                ));
            }
            
            // Check for memory leaks
            checkForMemoryLeaks();
            
        } catch (Exception e) {
            logger.error("Error checking memory usage", e);
        }
    }
    
    /**
     * Check for potential memory leaks.
     */
    private void checkForMemoryLeaks() {
        // Check if old generation is continuously growing
        for (MemoryPoolMXBean pool : memoryPoolBeans) {
            if (pool.getType() == java.lang.management.MemoryType.HEAP && 
                pool.getName().contains("Old Gen")) {
                
                MemoryUsage usage = pool.getUsage();
                double usageRatio = (double) usage.getUsed() / usage.getMax();
                
                if (usageRatio > 0.9) {
                    logger.warn("Old generation memory pool '{}' is {}% full - potential memory leak",
                        pool.getName(), (int)(usageRatio * 100));
                }
            }
        }
    }
    
    /**
     * Trigger an alert to all registered handlers.
     */
    private void triggerAlert(MemoryAlert alert) {
        logger.warn("Memory alert: {} - {}", alert.getLevel(), alert.getMessage());
        
        for (Consumer<MemoryAlert> handler : alertHandlers) {
            try {
                handler.accept(alert);
            } catch (Exception e) {
                logger.error("Error in alert handler", e);
            }
        }
    }
    
    /**
     * Register a memory allocation.
     */
    public void registerAllocation(String id, long size, String description) {
        MemoryAllocation allocation = new MemoryAllocation(id, size, description);
        allocations.put(id, allocation);
        totalAllocatedMemory.addAndGet(size);
        
        logger.debug("Registered memory allocation: {} - {} bytes - {}", id, size, description);
    }
    
    /**
     * Unregister a memory allocation.
     */
    public void unregisterAllocation(String id) {
        MemoryAllocation allocation = allocations.remove(id);
        if (allocation != null) {
            totalFreedMemory.addAndGet(allocation.getSize());
            logger.debug("Unregistered memory allocation: {} - {} bytes", id, allocation.getSize());
        }
    }
    
    /**
     * Suggest garbage collection if memory usage is high.
     */
    public void suggestGarbageCollection() {
        MemoryUsage beforeGC = memoryMXBean.getHeapMemoryUsage();
        long beforeUsed = beforeGC.getUsed();
        
        logger.info("Suggesting garbage collection. Current heap usage: {} MB / {} MB",
            beforeUsed / (1024 * 1024), beforeGC.getMax() / (1024 * 1024));
        
        System.gc();
        
        // Wait a bit for GC to complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        MemoryUsage afterGC = memoryMXBean.getHeapMemoryUsage();
        long freed = beforeUsed - afterGC.getUsed();
        
        if (freed > 0) {
            logger.info("Garbage collection freed {} MB", freed / (1024 * 1024));
        }
    }
    
    /**
     * Get current memory statistics.
     */
    public MemoryStatistics getStatistics() {
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();
        
        long totalGCTime = 0;
        long totalGCCount = 0;
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            totalGCTime += gcBean.getCollectionTime();
            totalGCCount += gcBean.getCollectionCount();
        }
        
        return new MemoryStatistics(
            heapUsage,
            nonHeapUsage,
            totalAllocatedMemory.get(),
            totalFreedMemory.get(),
            allocations.size(),
            totalGCCount,
            totalGCTime
        );
    }
    
    /**
     * Register an alert handler.
     */
    public void registerAlertHandler(Consumer<MemoryAlert> handler) {
        alertHandlers.add(handler);
    }
    
    /**
     * Set memory thresholds for alerts.
     */
    public void setThresholds(double highThreshold, double criticalThreshold) {
        if (highThreshold < 0 || highThreshold > 1 || criticalThreshold < 0 || criticalThreshold > 1) {
            throw new IllegalArgumentException("Thresholds must be between 0 and 1");
        }
        if (highThreshold >= criticalThreshold) {
            throw new IllegalArgumentException("High threshold must be less than critical threshold");
        }
        
        this.highMemoryThreshold = highThreshold;
        this.criticalMemoryThreshold = criticalThreshold;
        
        logger.info("Memory thresholds updated - High: {}%, Critical: {}%",
            (int)(highThreshold * 100), (int)(criticalThreshold * 100));
    }
    
    /**
     * Enable or disable monitoring.
     */
    public void setMonitoringEnabled(boolean enabled) {
        this.monitoringEnabled = enabled;
        logger.info("Memory monitoring {}", enabled ? "enabled" : "disabled");
    }
    
    /**
     * Shutdown the memory manager.
     */
    public void shutdown() {
        monitoringExecutor.shutdown();
        try {
            if (!monitoringExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                monitoringExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            monitoringExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("Memory manager shutdown complete");
    }
    
    /**
     * Memory allocation tracking.
     */
    public static class MemoryAllocation {
        private final String id;
        private final long size;
        private final String description;
        private final long timestamp;
        
        public MemoryAllocation(String id, long size, String description) {
            this.id = id;
            this.size = size;
            this.description = description;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getId() { return id; }
        public long getSize() { return size; }
        public String getDescription() { return description; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * Memory alert information.
     */
    public static class MemoryAlert {
        public enum Level {
            INFO, HIGH, CRITICAL
        }
        
        private final Level level;
        private final String message;
        private final MemoryUsage memoryUsage;
        private final long timestamp;
        
        public MemoryAlert(Level level, String message, MemoryUsage memoryUsage) {
            this.level = level;
            this.message = message;
            this.memoryUsage = memoryUsage;
            this.timestamp = System.currentTimeMillis();
        }
        
        public Level getLevel() { return level; }
        public String getMessage() { return message; }
        public MemoryUsage getMemoryUsage() { return memoryUsage; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * Memory statistics.
     */
    public static class MemoryStatistics {
        private final MemoryUsage heapUsage;
        private final MemoryUsage nonHeapUsage;
        private final long totalAllocated;
        private final long totalFreed;
        private final int activeAllocations;
        private final long gcCount;
        private final long gcTime;
        
        public MemoryStatistics(MemoryUsage heapUsage, MemoryUsage nonHeapUsage,
                               long totalAllocated, long totalFreed, int activeAllocations,
                               long gcCount, long gcTime) {
            this.heapUsage = heapUsage;
            this.nonHeapUsage = nonHeapUsage;
            this.totalAllocated = totalAllocated;
            this.totalFreed = totalFreed;
            this.activeAllocations = activeAllocations;
            this.gcCount = gcCount;
            this.gcTime = gcTime;
        }
        
        public MemoryUsage getHeapUsage() { return heapUsage; }
        public MemoryUsage getNonHeapUsage() { return nonHeapUsage; }
        public long getTotalAllocated() { return totalAllocated; }
        public long getTotalFreed() { return totalFreed; }
        public int getActiveAllocations() { return activeAllocations; }
        public long getGcCount() { return gcCount; }
        public long getGcTime() { return gcTime; }
        
        public double getHeapUsagePercentage() {
            return (double) heapUsage.getUsed() / heapUsage.getMax() * 100;
        }
    }
}