package com.memgres.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-local transaction context for managing the current transaction in each thread.
 * This allows implicit transaction management without passing transaction objects around.
 */
public class TransactionContext {
    private static final Logger logger = LoggerFactory.getLogger(TransactionContext.class);
    
    private static final ThreadLocal<Transaction> currentTransaction = new ThreadLocal<>();
    
    /**
     * Private constructor to prevent instantiation
     */
    private TransactionContext() {
    }
    
    /**
     * Get the current transaction for this thread
     * @return the current transaction or null if none is set
     */
    public static Transaction getCurrentTransaction() {
        return currentTransaction.get();
    }
    
    /**
     * Set the current transaction for this thread
     * @param transaction the transaction to set (null to clear)
     */
    public static void setCurrentTransaction(Transaction transaction) {
        Transaction previous = currentTransaction.get();
        
        if (transaction == null) {
            currentTransaction.remove();
            if (previous != null) {
                logger.debug("Cleared transaction context for thread {} (was transaction {})", 
                           Thread.currentThread().getName(), previous.getId());
            }
        } else {
            currentTransaction.set(transaction);
            logger.debug("Set transaction context for thread {} to transaction {}", 
                       Thread.currentThread().getName(), transaction.getId());
            
            if (previous != null && previous.getId() != transaction.getId()) {
                logger.warn("Replaced transaction {} with transaction {} in thread {} - " +
                          "previous transaction may be leaked", 
                          previous.getId(), transaction.getId(), Thread.currentThread().getName());
            }
        }
    }
    
    /**
     * Check if there is a current transaction for this thread
     * @return true if a transaction is set
     */
    public static boolean hasCurrentTransaction() {
        return currentTransaction.get() != null;
    }
    
    /**
     * Get the current transaction ID for this thread
     * @return the current transaction ID or -1 if none is set
     */
    public static long getCurrentTransactionId() {
        Transaction transaction = currentTransaction.get();
        return transaction != null ? transaction.getId() : -1;
    }
    
    /**
     * Check if the current transaction is active
     * @return true if there is an active transaction
     */
    public static boolean isCurrentTransactionActive() {
        Transaction transaction = currentTransaction.get();
        return transaction != null && transaction.isActive();
    }
    
    /**
     * Get the current transaction isolation level
     * @return the isolation level or null if no transaction is set
     */
    public static TransactionIsolationLevel getCurrentIsolationLevel() {
        Transaction transaction = currentTransaction.get();
        return transaction != null ? transaction.getIsolationLevel() : null;
    }
    
    /**
     * Execute a block of code with a specific transaction set as current
     * @param transaction the transaction to set as current
     * @param block the code to execute
     * @param <T> the return type
     * @return the result of the block
     * @throws Exception if the block throws an exception
     */
    public static <T> T executeWithTransaction(Transaction transaction, TransactionBlock<T> block) throws Exception {
        Transaction previous = getCurrentTransaction();
        try {
            setCurrentTransaction(transaction);
            return block.execute();
        } finally {
            setCurrentTransaction(previous);
        }
    }
    
    /**
     * Execute a block of code with no current transaction
     * @param block the code to execute
     * @param <T> the return type
     * @return the result of the block
     * @throws Exception if the block throws an exception
     */
    public static <T> T executeWithoutTransaction(TransactionBlock<T> block) throws Exception {
        return executeWithTransaction(null, block);
    }
    
    /**
     * Clear the current transaction context
     * This should be called when a thread finishes to prevent memory leaks
     */
    public static void clearContext() {
        Transaction transaction = currentTransaction.get();
        if (transaction != null) {
            currentTransaction.remove();
            logger.debug("Cleared transaction context for thread {} (was transaction {})", 
                       Thread.currentThread().getName(), transaction.getId());
        }
    }
    
    /**
     * Get information about the current transaction context
     * @return a string describing the current context
     */
    public static String getContextInfo() {
        Transaction transaction = currentTransaction.get();
        String threadName = Thread.currentThread().getName();
        
        if (transaction == null) {
            return "Thread '" + threadName + "': No transaction context";
        } else {
            return String.format("Thread '%s': Transaction %d (%s, %s)", 
                               threadName, 
                               transaction.getId(), 
                               transaction.getState(), 
                               transaction.getIsolationLevel());
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