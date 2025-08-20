package com.memgres.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Defines cache eviction policies for memory management.
 */
public abstract class CacheEvictionPolicy<K, V> {
    private static final Logger logger = LoggerFactory.getLogger(CacheEvictionPolicy.class);
    
    protected final long maxSize;
    protected final AtomicLong currentSize = new AtomicLong();
    protected final AtomicLong evictionCount = new AtomicLong();
    protected final AtomicLong hitCount = new AtomicLong();
    protected final AtomicLong missCount = new AtomicLong();
    
    public CacheEvictionPolicy(long maxSize) {
        this.maxSize = maxSize;
    }
    
    /**
     * Record access to a cache entry.
     */
    public abstract void recordAccess(K key);
    
    /**
     * Record addition of a cache entry.
     */
    public abstract void recordAddition(K key, long size);
    
    /**
     * Get the next key to evict.
     */
    public abstract K selectEvictionCandidate();
    
    /**
     * Record removal of a cache entry.
     */
    public abstract void recordRemoval(K key);
    
    /**
     * Clear all tracking data.
     */
    public abstract void clear();
    
    /**
     * Check if eviction is needed.
     */
    public boolean shouldEvict() {
        return currentSize.get() > maxSize;
    }
    
    /**
     * Get cache statistics.
     */
    public CacheStatistics getStatistics() {
        long hits = hitCount.get();
        long misses = missCount.get();
        double hitRatio = (hits + misses) > 0 ? (double) hits / (hits + misses) : 0.0;
        
        return new CacheStatistics(
            currentSize.get(),
            maxSize,
            evictionCount.get(),
            hits,
            misses,
            hitRatio
        );
    }
    
    /**
     * LRU (Least Recently Used) eviction policy.
     */
    public static class LRUPolicy<K, V> extends CacheEvictionPolicy<K, V> {
        private final Map<K, Node<K>> nodeMap = new ConcurrentHashMap<>();
        private final ReadWriteLock lock = new ReentrantReadWriteLock();
        private Node<K> head;
        private Node<K> tail;
        
        public LRUPolicy(long maxSize) {
            super(maxSize);
            initializeList();
        }
        
        private void initializeList() {
            head = new Node<>(null, 0);
            tail = new Node<>(null, 0);
            head.next = tail;
            tail.prev = head;
        }
        
        @Override
        public void recordAccess(K key) {
            lock.writeLock().lock();
            try {
                Node<K> node = nodeMap.get(key);
                if (node != null) {
                    removeNode(node);
                    addToHead(node);
                    hitCount.incrementAndGet();
                } else {
                    missCount.incrementAndGet();
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
        
        @Override
        public void recordAddition(K key, long size) {
            lock.writeLock().lock();
            try {
                Node<K> node = new Node<>(key, size);
                nodeMap.put(key, node);
                addToHead(node);
                currentSize.addAndGet(size);
            } finally {
                lock.writeLock().unlock();
            }
        }
        
        @Override
        public K selectEvictionCandidate() {
            lock.readLock().lock();
            try {
                Node<K> candidate = tail.prev;
                if (candidate != head) {
                    return candidate.key;
                }
                return null;
            } finally {
                lock.readLock().unlock();
            }
        }
        
        @Override
        public void recordRemoval(K key) {
            lock.writeLock().lock();
            try {
                Node<K> node = nodeMap.remove(key);
                if (node != null) {
                    removeNode(node);
                    currentSize.addAndGet(-node.size);
                    evictionCount.incrementAndGet();
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
        
        @Override
        public void clear() {
            lock.writeLock().lock();
            try {
                nodeMap.clear();
                initializeList();
                currentSize.set(0);
            } finally {
                lock.writeLock().unlock();
            }
        }
        
        private void addToHead(Node<K> node) {
            node.prev = head;
            node.next = head.next;
            head.next.prev = node;
            head.next = node;
        }
        
        private void removeNode(Node<K> node) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }
        
        private static class Node<K> {
            K key;
            long size;
            Node<K> prev;
            Node<K> next;
            
            Node(K key, long size) {
                this.key = key;
                this.size = size;
            }
        }
    }
    
    /**
     * LFU (Least Frequently Used) eviction policy.
     */
    public static class LFUPolicy<K, V> extends CacheEvictionPolicy<K, V> {
        private final Map<K, FrequencyNode<K>> nodeMap = new ConcurrentHashMap<>();
        private final TreeMap<Integer, Set<K>> frequencyMap = new TreeMap<>();
        private final ReadWriteLock lock = new ReentrantReadWriteLock();
        
        public LFUPolicy(long maxSize) {
            super(maxSize);
        }
        
        @Override
        public void recordAccess(K key) {
            lock.writeLock().lock();
            try {
                FrequencyNode<K> node = nodeMap.get(key);
                if (node != null) {
                    updateFrequency(node);
                    hitCount.incrementAndGet();
                } else {
                    missCount.incrementAndGet();
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
        
        @Override
        public void recordAddition(K key, long size) {
            lock.writeLock().lock();
            try {
                FrequencyNode<K> node = new FrequencyNode<>(key, size, 1);
                nodeMap.put(key, node);
                frequencyMap.computeIfAbsent(1, k -> new HashSet<>()).add(key);
                currentSize.addAndGet(size);
            } finally {
                lock.writeLock().unlock();
            }
        }
        
        @Override
        public K selectEvictionCandidate() {
            lock.readLock().lock();
            try {
                if (!frequencyMap.isEmpty()) {
                    Map.Entry<Integer, Set<K>> entry = frequencyMap.firstEntry();
                    if (entry != null && !entry.getValue().isEmpty()) {
                        return entry.getValue().iterator().next();
                    }
                }
                return null;
            } finally {
                lock.readLock().unlock();
            }
        }
        
        @Override
        public void recordRemoval(K key) {
            lock.writeLock().lock();
            try {
                FrequencyNode<K> node = nodeMap.remove(key);
                if (node != null) {
                    Set<K> keys = frequencyMap.get(node.frequency);
                    if (keys != null) {
                        keys.remove(key);
                        if (keys.isEmpty()) {
                            frequencyMap.remove(node.frequency);
                        }
                    }
                    currentSize.addAndGet(-node.size);
                    evictionCount.incrementAndGet();
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
        
        @Override
        public void clear() {
            lock.writeLock().lock();
            try {
                nodeMap.clear();
                frequencyMap.clear();
                currentSize.set(0);
            } finally {
                lock.writeLock().unlock();
            }
        }
        
        private void updateFrequency(FrequencyNode<K> node) {
            Set<K> oldSet = frequencyMap.get(node.frequency);
            if (oldSet != null) {
                oldSet.remove(node.key);
                if (oldSet.isEmpty()) {
                    frequencyMap.remove(node.frequency);
                }
            }
            
            node.frequency++;
            frequencyMap.computeIfAbsent(node.frequency, k -> new HashSet<>()).add(node.key);
        }
        
        private static class FrequencyNode<K> {
            K key;
            long size;
            int frequency;
            
            FrequencyNode(K key, long size, int frequency) {
                this.key = key;
                this.size = size;
                this.frequency = frequency;
            }
        }
    }
    
    /**
     * FIFO (First In First Out) eviction policy.
     */
    public static class FIFOPolicy<K, V> extends CacheEvictionPolicy<K, V> {
        private final Queue<K> queue = new ConcurrentLinkedQueue<>();
        private final Map<K, Long> sizeMap = new ConcurrentHashMap<>();
        
        public FIFOPolicy(long maxSize) {
            super(maxSize);
        }
        
        @Override
        public void recordAccess(K key) {
            if (sizeMap.containsKey(key)) {
                hitCount.incrementAndGet();
            } else {
                missCount.incrementAndGet();
            }
        }
        
        @Override
        public void recordAddition(K key, long size) {
            queue.offer(key);
            sizeMap.put(key, size);
            currentSize.addAndGet(size);
        }
        
        @Override
        public K selectEvictionCandidate() {
            return queue.peek();
        }
        
        @Override
        public void recordRemoval(K key) {
            queue.remove(key);
            Long size = sizeMap.remove(key);
            if (size != null) {
                currentSize.addAndGet(-size);
                evictionCount.incrementAndGet();
            }
        }
        
        @Override
        public void clear() {
            queue.clear();
            sizeMap.clear();
            currentSize.set(0);
        }
    }
    
    /**
     * Random eviction policy.
     */
    public static class RandomPolicy<K, V> extends CacheEvictionPolicy<K, V> {
        private final Map<K, Long> sizeMap = new ConcurrentHashMap<>();
        private final Random random = new Random();
        
        public RandomPolicy(long maxSize) {
            super(maxSize);
        }
        
        @Override
        public void recordAccess(K key) {
            if (sizeMap.containsKey(key)) {
                hitCount.incrementAndGet();
            } else {
                missCount.incrementAndGet();
            }
        }
        
        @Override
        public void recordAddition(K key, long size) {
            sizeMap.put(key, size);
            currentSize.addAndGet(size);
        }
        
        @Override
        public K selectEvictionCandidate() {
            List<K> keys = new ArrayList<>(sizeMap.keySet());
            if (!keys.isEmpty()) {
                return keys.get(random.nextInt(keys.size()));
            }
            return null;
        }
        
        @Override
        public void recordRemoval(K key) {
            Long size = sizeMap.remove(key);
            if (size != null) {
                currentSize.addAndGet(-size);
                evictionCount.incrementAndGet();
            }
        }
        
        @Override
        public void clear() {
            sizeMap.clear();
            currentSize.set(0);
        }
    }
    
    /**
     * Cache statistics.
     */
    public static class CacheStatistics {
        private final long currentSize;
        private final long maxSize;
        private final long evictionCount;
        private final long hitCount;
        private final long missCount;
        private final double hitRatio;
        
        public CacheStatistics(long currentSize, long maxSize, long evictionCount,
                              long hitCount, long missCount, double hitRatio) {
            this.currentSize = currentSize;
            this.maxSize = maxSize;
            this.evictionCount = evictionCount;
            this.hitCount = hitCount;
            this.missCount = missCount;
            this.hitRatio = hitRatio;
        }
        
        public long getCurrentSize() { return currentSize; }
        public long getMaxSize() { return maxSize; }
        public long getEvictionCount() { return evictionCount; }
        public long getHitCount() { return hitCount; }
        public long getMissCount() { return missCount; }
        public double getHitRatio() { return hitRatio; }
    }
}