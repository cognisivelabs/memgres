package com.memgres.storage;

import com.memgres.types.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents a database sequence for generating sequential numbers.
 * Implements H2-compatible sequence functionality with thread-safe operations.
 */
public class Sequence {
    private static final Logger logger = LoggerFactory.getLogger(Sequence.class);
    
    private final String name;
    private final DataType dataType;
    private final long startWith;
    private final long incrementBy;
    private final Long minValue;
    private final Long maxValue;
    private final boolean cycle;
    private final long cacheSize;
    
    // Current state
    private volatile long currentValue;
    private volatile boolean hasBeenAccessed;
    private final ReadWriteLock sequenceLock;
    
    /**
     * Constructor for sequence with full H2 compatibility options.
     */
    public Sequence(String name, DataType dataType, long startWith, long incrementBy,
                   Long minValue, Long maxValue, boolean cycle, long cacheSize) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Sequence name cannot be null or empty");
        }
        if (incrementBy == 0) {
            throw new IllegalArgumentException("Increment by cannot be zero");
        }
        if (cacheSize < 0) {
            throw new IllegalArgumentException("Cache size cannot be negative");
        }
        
        this.name = name.toLowerCase(); // H2 converts sequence names to lowercase
        this.dataType = dataType != null ? dataType : DataType.BIGINT; // Default to BIGINT
        this.startWith = startWith;
        this.incrementBy = incrementBy;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.cycle = cycle;
        this.cacheSize = cacheSize;
        
        // Initialize current value
        this.currentValue = startWith;
        this.hasBeenAccessed = false;
        this.sequenceLock = new ReentrantReadWriteLock();
        
        logger.debug("Created sequence: {} with start={}, increment={}, min={}, max={}, cycle={}, cache={}",
                    this.name, startWith, incrementBy, minValue, maxValue, cycle, cacheSize);
    }
    
    /**
     * Constructor with default values (matches H2 defaults).
     */
    public Sequence(String name) {
        this(name, DataType.BIGINT, 1L, 1L, null, null, false, 32L);
    }
    
    /**
     * Get the sequence name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the data type of the sequence.
     */
    public DataType getDataType() {
        return dataType;
    }
    
    /**
     * Get the start value.
     */
    public long getStartWith() {
        return startWith;
    }
    
    /**
     * Get the increment value.
     */
    public long getIncrementBy() {
        return incrementBy;
    }
    
    /**
     * Get the minimum value (null if no minimum).
     */
    public Long getMinValue() {
        return minValue;
    }
    
    /**
     * Get the maximum value (null if no maximum).
     */
    public Long getMaxValue() {
        return maxValue;
    }
    
    /**
     * Check if the sequence cycles.
     */
    public boolean isCycle() {
        return cycle;
    }
    
    /**
     * Get the cache size.
     */
    public long getCacheSize() {
        return cacheSize;
    }
    
    /**
     * Get the next value from the sequence.
     * This is thread-safe and implements H2-compatible behavior.
     */
    public long nextValue() throws SequenceException {
        sequenceLock.writeLock().lock();
        try {
            if (!hasBeenAccessed) {
                hasBeenAccessed = true;
                // First access returns start value
                validateValue(currentValue);
                logger.debug("Sequence {} first access, returning start value: {}", name, currentValue);
                return currentValue;
            }
            
            // Calculate next value
            long nextVal = currentValue + incrementBy;
            
            // Check bounds
            if (incrementBy > 0) {
                // Ascending sequence
                if (maxValue != null && nextVal > maxValue) {
                    if (cycle) {
                        nextVal = minValue != null ? minValue : 1L;
                    } else {
                        throw new SequenceException("Sequence " + name + " has reached maximum value " + maxValue);
                    }
                }
                if (minValue != null && nextVal < minValue) {
                    throw new SequenceException("Sequence " + name + " value " + nextVal + " is below minimum " + minValue);
                }
            } else {
                // Descending sequence
                if (minValue != null && nextVal < minValue) {
                    if (cycle) {
                        nextVal = maxValue != null ? maxValue : Long.MAX_VALUE;
                    } else {
                        throw new SequenceException("Sequence " + name + " has reached minimum value " + minValue);
                    }
                }
                if (maxValue != null && nextVal > maxValue) {
                    throw new SequenceException("Sequence " + name + " value " + nextVal + " is above maximum " + maxValue);
                }
            }
            
            currentValue = nextVal;
            validateValue(currentValue);
            
            logger.debug("Sequence {} next value: {}", name, currentValue);
            return currentValue;
            
        } finally {
            sequenceLock.writeLock().unlock();
        }
    }
    
    /**
     * Get the current value of the sequence without advancing it.
     * Throws exception if sequence has never been accessed.
     */
    public long currentValue() throws SequenceException {
        sequenceLock.readLock().lock();
        try {
            if (!hasBeenAccessed) {
                throw new SequenceException("Sequence " + name + " has not been accessed yet");
            }
            
            logger.debug("Sequence {} current value: {}", name, currentValue);
            return currentValue;
            
        } finally {
            sequenceLock.readLock().unlock();
        }
    }
    
    /**
     * Reset the sequence to its start value.
     * This is mainly for testing purposes.
     */
    public void reset() {
        sequenceLock.writeLock().lock();
        try {
            currentValue = startWith;
            hasBeenAccessed = false;
            logger.debug("Sequence {} reset to start value: {}", name, startWith);
        } finally {
            sequenceLock.writeLock().unlock();
        }
    }
    
    /**
     * Validate that a value fits in the sequence's data type.
     */
    private void validateValue(long value) throws SequenceException {
        switch (dataType) {
            case SMALLINT:
                if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
                    throw new SequenceException("Sequence value " + value + " out of SMALLINT range");
                }
                break;
            case INTEGER:
                if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                    throw new SequenceException("Sequence value " + value + " out of INTEGER range");
                }
                break;
            case BIGINT:
                // Long can hold any BIGINT value
                break;
            default:
                throw new SequenceException("Unsupported sequence data type: " + dataType);
        }
    }
    
    @Override
    public String toString() {
        return String.format("Sequence{name='%s', dataType=%s, start=%d, increment=%d, min=%s, max=%s, cycle=%s, cache=%d, current=%d, accessed=%s}",
                           name, dataType, startWith, incrementBy, minValue, maxValue, cycle, cacheSize, currentValue, hasBeenAccessed);
    }
    
    /**
     * Exception thrown when sequence operations fail.
     */
    public static class SequenceException extends Exception {
        public SequenceException(String message) {
            super(message);
        }
        
        public SequenceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}