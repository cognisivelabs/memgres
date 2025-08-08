package com.memgres.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Basic transaction manager for MemGres.
 * This is a simplified implementation that will be enhanced with full ACID properties.
 */
public class TransactionManager {
    private static final Logger logger = LoggerFactory.getLogger(TransactionManager.class);
    
    private final AtomicLong transactionIdGenerator;
    private final ConcurrentMap<Long, Transaction> activeTransactions;
    private final ReadWriteLock managerLock;
    private volatile boolean shutdown;
    
    public TransactionManager() {
        this.transactionIdGenerator = new AtomicLong(0);
        this.activeTransactions = new ConcurrentHashMap<>();
        this.managerLock = new ReentrantReadWriteLock();
        this.shutdown = false;
        
        logger.info("Transaction manager initialized");
    }
    
    /**
     * Begin a new transaction
     * @return the transaction object
     */
    public Transaction beginTransaction() {
        return beginTransaction(TransactionIsolationLevel.READ_COMMITTED);
    }
    
    /**
     * Begin a new transaction with the specified isolation level
     * @param isolationLevel the isolation level
     * @return the transaction object
     */
    public Transaction beginTransaction(TransactionIsolationLevel isolationLevel) {
        if (shutdown) {
            throw new IllegalStateException("Transaction manager is shutdown");
        }
        
        managerLock.readLock().lock();
        try {
            long transactionId = transactionIdGenerator.incrementAndGet();
            Transaction transaction = new Transaction(transactionId, isolationLevel, this);
            activeTransactions.put(transactionId, transaction);
            
            logger.debug("Started transaction {} with isolation level {}", 
                        transactionId, isolationLevel);
            
            return transaction;
        } finally {
            managerLock.readLock().unlock();
        }
    }
    
    /**
     * Commit a transaction
     * @param transaction the transaction to commit
     */
    public void commitTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }
        
        Long transactionId = transaction.getId();
        Transaction activeTransaction = activeTransactions.get(transactionId);
        
        if (activeTransaction == null) {
            throw new IllegalStateException("Transaction not found or already completed: " + transactionId);
        }
        
        if (activeTransaction != transaction) {
            throw new IllegalStateException("Transaction object mismatch");
        }
        
        managerLock.readLock().lock();
        try {
            if (shutdown) {
                throw new IllegalStateException("Transaction manager is shutdown");
            }
            
            transaction.commit();
            activeTransactions.remove(transactionId);
            
            logger.debug("Committed transaction {}", transactionId);
        } catch (Exception e) {
            logger.error("Failed to commit transaction {}", transactionId, e);
            rollbackTransaction(transaction);
            throw new RuntimeException("Transaction commit failed", e);
        } finally {
            managerLock.readLock().unlock();
        }
    }
    
    /**
     * Rollback a transaction
     * @param transaction the transaction to rollback
     */
    public void rollbackTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }
        
        Long transactionId = transaction.getId();
        Transaction activeTransaction = activeTransactions.get(transactionId);
        
        if (activeTransaction == null) {
            logger.warn("Transaction not found or already completed: {}", transactionId);
            return;
        }
        
        managerLock.readLock().lock();
        try {
            transaction.rollback();
            activeTransactions.remove(transactionId);
            
            logger.debug("Rolled back transaction {}", transactionId);
        } catch (Exception e) {
            logger.error("Failed to rollback transaction {}", transactionId, e);
            // Remove from active transactions even if rollback fails
            activeTransactions.remove(transactionId);
        } finally {
            managerLock.readLock().unlock();
        }
    }
    
    /**
     * Get an active transaction by ID
     * @param transactionId the transaction ID
     * @return the transaction or null if not found
     */
    public Transaction getTransaction(long transactionId) {
        managerLock.readLock().lock();
        try {
            return activeTransactions.get(transactionId);
        } finally {
            managerLock.readLock().unlock();
        }
    }
    
    /**
     * Get the number of active transactions
     * @return the number of active transactions
     */
    public int getActiveTransactionCount() {
        managerLock.readLock().lock();
        try {
            return activeTransactions.size();
        } finally {
            managerLock.readLock().unlock();
        }
    }
    
    /**
     * Check if a transaction is active
     * @param transactionId the transaction ID
     * @return true if the transaction is active
     */
    public boolean isTransactionActive(long transactionId) {
        managerLock.readLock().lock();
        try {
            return activeTransactions.containsKey(transactionId);
        } finally {
            managerLock.readLock().unlock();
        }
    }
    
    /**
     * Get the current thread's transaction, if any
     * @return the current transaction or null
     */
    public Transaction getCurrentTransaction() {
        return TransactionContext.getCurrentTransaction();
    }
    
    /**
     * Set the current thread's transaction
     * @param transaction the transaction to set (null to clear)
     */
    public void setCurrentTransaction(Transaction transaction) {
        TransactionContext.setCurrentTransaction(transaction);
    }
    
    /**
     * Execute a block of code within a transaction
     * @param block the code to execute
     * @param <T> the return type
     * @return the result of the block
     * @throws Exception if the block throws an exception
     */
    public <T> T executeInTransaction(TransactionBlock<T> block) throws Exception {
        return executeInTransaction(block, TransactionIsolationLevel.READ_COMMITTED);
    }
    
    /**
     * Execute a block of code within a transaction with specified isolation level
     * @param block the code to execute
     * @param isolationLevel the isolation level
     * @param <T> the return type
     * @return the result of the block
     * @throws Exception if the block throws an exception
     */
    public <T> T executeInTransaction(TransactionBlock<T> block, 
                                     TransactionIsolationLevel isolationLevel) throws Exception {
        Transaction transaction = beginTransaction(isolationLevel);
        Transaction previousTransaction = getCurrentTransaction();
        
        try {
            setCurrentTransaction(transaction);
            T result = block.execute();
            commitTransaction(transaction);
            return result;
        } catch (Exception e) {
            rollbackTransaction(transaction);
            throw e;
        } finally {
            setCurrentTransaction(previousTransaction);
        }
    }
    
    /**
     * Shutdown the transaction manager
     */
    public void shutdown() {
        managerLock.writeLock().lock();
        try {
            if (shutdown) {
                return;
            }
            
            logger.info("Shutting down transaction manager with {} active transactions", 
                       activeTransactions.size());
            
            // Rollback all active transactions
            for (Transaction transaction : activeTransactions.values()) {
                try {
                    transaction.rollback();
                } catch (Exception e) {
                    logger.error("Failed to rollback transaction {} during shutdown", 
                               transaction.getId(), e);
                }
            }
            
            activeTransactions.clear();
            shutdown = true;
            
            logger.info("Transaction manager shutdown complete");
        } finally {
            managerLock.writeLock().unlock();
        }
    }
    
    /**
     * Check if the transaction manager is shutdown
     * @return true if shutdown
     */
    public boolean isShutdown() {
        managerLock.readLock().lock();
        try {
            return shutdown;
        } finally {
            managerLock.readLock().unlock();
        }
    }
    
    /**
     * Functional interface for transaction blocks
     * @param <T> the return type
     */
    @FunctionalInterface
    public interface TransactionBlock<T> {
        T execute() throws Exception;
    }
}