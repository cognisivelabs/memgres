package com.memgres.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents a database transaction with ACID properties.
 * This is a simplified implementation that will be enhanced with full MVCC support.
 */
public class Transaction {
    private static final Logger logger = LoggerFactory.getLogger(Transaction.class);
    
    private final long id;
    private final TransactionIsolationLevel isolationLevel;
    private final TransactionManager manager;
    private final LocalDateTime startTime;
    private final ConcurrentMap<String, Object> attributes;
    private final ReadWriteLock transactionLock;
    
    private volatile TransactionState state;
    private volatile LocalDateTime commitTime;
    private volatile LocalDateTime rollbackTime;
    private volatile String rollbackReason;
    
    /**
     * Transaction states
     */
    public enum TransactionState {
        ACTIVE,
        COMMITTED,
        ROLLED_BACK
    }
    
    Transaction(long id, TransactionIsolationLevel isolationLevel, TransactionManager manager) {
        this.id = id;
        this.isolationLevel = isolationLevel;
        this.manager = manager;
        this.startTime = LocalDateTime.now();
        this.attributes = new ConcurrentHashMap<>();
        this.transactionLock = new ReentrantReadWriteLock();
        this.state = TransactionState.ACTIVE;
        
        logger.debug("Created transaction {} with isolation level {}", id, isolationLevel);
    }
    
    /**
     * Get the transaction ID
     * @return the unique transaction identifier
     */
    public long getId() {
        return id;
    }
    
    /**
     * Get the isolation level
     * @return the transaction isolation level
     */
    public TransactionIsolationLevel getIsolationLevel() {
        return isolationLevel;
    }
    
    /**
     * Get the transaction manager
     * @return the transaction manager
     */
    public TransactionManager getManager() {
        return manager;
    }
    
    /**
     * Get the start time
     * @return when the transaction was started
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    /**
     * Get the current state
     * @return the transaction state
     */
    public TransactionState getState() {
        transactionLock.readLock().lock();
        try {
            return state;
        } finally {
            transactionLock.readLock().unlock();
        }
    }
    
    /**
     * Get the commit time
     * @return when the transaction was committed, or null if not committed
     */
    public LocalDateTime getCommitTime() {
        transactionLock.readLock().lock();
        try {
            return commitTime;
        } finally {
            transactionLock.readLock().unlock();
        }
    }
    
    /**
     * Get the rollback time
     * @return when the transaction was rolled back, or null if not rolled back
     */
    public LocalDateTime getRollbackTime() {
        transactionLock.readLock().lock();
        try {
            return rollbackTime;
        } finally {
            transactionLock.readLock().unlock();
        }
    }
    
    /**
     * Get the rollback reason
     * @return the reason for rollback, or null if not rolled back
     */
    public String getRollbackReason() {
        transactionLock.readLock().lock();
        try {
            return rollbackReason;
        } finally {
            transactionLock.readLock().unlock();
        }
    }
    
    /**
     * Check if the transaction is active
     * @return true if the transaction is active
     */
    public boolean isActive() {
        return getState() == TransactionState.ACTIVE;
    }
    
    /**
     * Check if the transaction is committed
     * @return true if the transaction is committed
     */
    public boolean isCommitted() {
        return getState() == TransactionState.COMMITTED;
    }
    
    /**
     * Check if the transaction is rolled back
     * @return true if the transaction is rolled back
     */
    public boolean isRolledBack() {
        return getState() == TransactionState.ROLLED_BACK;
    }
    
    /**
     * Check if the transaction is completed (committed or rolled back)
     * @return true if the transaction is completed
     */
    public boolean isCompleted() {
        TransactionState currentState = getState();
        return currentState == TransactionState.COMMITTED || currentState == TransactionState.ROLLED_BACK;
    }
    
    /**
     * Commit the transaction
     * This method is called by the TransactionManager
     */
    void commit() {
        transactionLock.writeLock().lock();
        try {
            if (state != TransactionState.ACTIVE) {
                throw new IllegalStateException("Transaction is not active: " + state);
            }
            
            // Perform commit operations here
            // This is where we would:
            // 1. Validate serialization constraints (for SERIALIZABLE isolation)
            // 2. Make changes visible to other transactions
            // 3. Release locks
            // 4. Clean up transaction resources
            
            state = TransactionState.COMMITTED;
            commitTime = LocalDateTime.now();
            
            logger.debug("Transaction {} committed successfully", id);
        } finally {
            transactionLock.writeLock().unlock();
        }
    }
    
    /**
     * Roll back the transaction
     * This method is called by the TransactionManager
     */
    void rollback() {
        rollback("Explicit rollback");
    }
    
    /**
     * Roll back the transaction with a reason
     * This method is called by the TransactionManager
     */
    void rollback(String reason) {
        transactionLock.writeLock().lock();
        try {
            if (state == TransactionState.ROLLED_BACK) {
                logger.warn("Transaction {} already rolled back", id);
                return;
            }
            
            if (state == TransactionState.COMMITTED) {
                throw new IllegalStateException("Cannot rollback committed transaction");
            }
            
            // Perform rollback operations here
            // This is where we would:
            // 1. Undo all changes made by this transaction
            // 2. Release locks
            // 3. Clean up transaction resources
            // 4. Invalidate any cached data
            
            state = TransactionState.ROLLED_BACK;
            rollbackTime = LocalDateTime.now();
            rollbackReason = reason;
            
            logger.debug("Transaction {} rolled back: {}", id, reason);
        } finally {
            transactionLock.writeLock().unlock();
        }
    }
    
    /**
     * Set a transaction attribute
     * @param key the attribute key
     * @param value the attribute value
     */
    public void setAttribute(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("Attribute key cannot be null");
        }
        
        attributes.put(key, value);
    }
    
    /**
     * Get a transaction attribute
     * @param key the attribute key
     * @return the attribute value or null if not found
     */
    public Object getAttribute(String key) {
        if (key == null) {
            return null;
        }
        
        return attributes.get(key);
    }
    
    /**
     * Get a transaction attribute with type casting
     * @param key the attribute key
     * @param type the expected type
     * @param <T> the type parameter
     * @return the attribute value cast to the specified type, or null if not found
     * @throws ClassCastException if the attribute cannot be cast to the specified type
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = getAttribute(key);
        if (value == null) {
            return null;
        }
        
        if (type.isInstance(value)) {
            return (T) value;
        } else {
            throw new ClassCastException("Attribute " + key + " is not of type " + type.getName());
        }
    }
    
    /**
     * Remove a transaction attribute
     * @param key the attribute key
     * @return the previous value or null if not found
     */
    public Object removeAttribute(String key) {
        if (key == null) {
            return null;
        }
        
        return attributes.remove(key);
    }
    
    /**
     * Check if a transaction attribute exists
     * @param key the attribute key
     * @return true if the attribute exists
     */
    public boolean hasAttribute(String key) {
        return key != null && attributes.containsKey(key);
    }
    
    /**
     * Get the transaction duration
     * @return the duration since the transaction started, in milliseconds
     */
    public long getDurationMillis() {
        LocalDateTime endTime = commitTime != null ? commitTime : 
                               rollbackTime != null ? rollbackTime : 
                               LocalDateTime.now();
        
        return java.time.Duration.between(startTime, endTime).toMillis();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Transaction that = (Transaction) o;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
    
    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", isolationLevel=" + isolationLevel +
                ", state=" + getState() +
                ", startTime=" + startTime +
                ", durationMs=" + getDurationMillis() +
                '}';
    }
}