package com.memgres.wal;

import com.memgres.transaction.Transaction;
import com.memgres.transaction.TransactionIsolationLevel;
import com.memgres.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * WAL-enabled transaction manager that provides transaction durability
 * through Write-Ahead Logging.
 */
public class WalTransactionManager extends TransactionManager {
    private static final Logger logger = LoggerFactory.getLogger(WalTransactionManager.class);
    
    private final WalManager walManager;
    private final ConcurrentMap<Long, WalTransaction> walTransactions;
    private volatile boolean walEnabled;
    
    public WalTransactionManager(String walDirectory) throws IOException {
        super();
        this.walManager = new WalManager(walDirectory);
        this.walTransactions = new ConcurrentHashMap<>();
        this.walEnabled = true;
        
        logger.info("WAL Transaction Manager initialized with directory: {}", walDirectory);
    }
    
    /**
     * Constructor that uses default WAL directory.
     */
    public WalTransactionManager() throws IOException {
        this(getDefaultWalDirectory());
    }
    
    @Override
    public Transaction beginTransaction(TransactionIsolationLevel isolationLevel) {
        Transaction transaction = super.beginTransaction(isolationLevel);
        
        if (walEnabled) {
            try {
                // Create WAL transaction wrapper
                WalTransaction walTransaction = new WalTransaction(transaction, walManager);
                walTransactions.put(transaction.getId(), walTransaction);
                
                // Write BEGIN transaction record
                WalRecord beginRecord = new WalRecord.Builder()
                    .transactionId(transaction.getId())
                    .type(WalRecord.RecordType.BEGIN_TRANSACTION)
                    .sql("BEGIN TRANSACTION")
                    .build();
                
                walManager.writeRecord(beginRecord);
                
                logger.debug("Started WAL transaction: {}", transaction.getId());
                return walTransaction.getDelegate();
                
            } catch (IOException e) {
                logger.error("Failed to write BEGIN record for transaction: " + transaction.getId(), e);
                // Fall back to regular transaction
                return transaction;
            }
        }
        
        return transaction;
    }
    
    /**
     * Commit a transaction and ensure WAL durability.
     */
    public void commitTransaction(long transactionId) throws IOException {
        WalTransaction walTransaction = walTransactions.get(transactionId);
        
        if (walTransaction != null && walEnabled) {
            // Write COMMIT record before actual commit
            WalRecord commitRecord = new WalRecord.Builder()
                .transactionId(transactionId)
                .type(WalRecord.RecordType.COMMIT_TRANSACTION)
                .sql("COMMIT")
                .build();
            
            walManager.writeRecord(commitRecord);
            
            // Force write to disk for durability
            walManager.flush();
            
            // Now commit the actual transaction through parent manager
            super.commitTransaction(walTransaction.getDelegate());
            
            walTransactions.remove(transactionId);
            
            logger.debug("Committed WAL transaction: {}", transactionId);
        }
    }
    
    /**
     * Rollback a transaction and log the abort.
     */
    public void rollbackTransaction(long transactionId) throws IOException {
        WalTransaction walTransaction = walTransactions.get(transactionId);
        
        if (walTransaction != null && walEnabled) {
            // Write ABORT record
            WalRecord abortRecord = new WalRecord.Builder()
                .transactionId(transactionId)
                .type(WalRecord.RecordType.ABORT_TRANSACTION)
                .sql("ROLLBACK")
                .build();
            
            walManager.writeRecord(abortRecord);
            
            // Rollback the actual transaction through parent manager
            super.rollbackTransaction(walTransaction.getDelegate());
            
            walTransactions.remove(transactionId);
            
            logger.debug("Rolled back WAL transaction: {}", transactionId);
        }
    }
    
    /**
     * Log a data modification operation.
     */
    public void logDataModification(long transactionId, WalRecord.RecordType type, 
                                  String schemaName, String tableName, 
                                  Object[] beforeImage, Object[] afterImage, 
                                  String sql) throws IOException {
        
        if (!walEnabled) {
            return;
        }
        
        WalRecord dataRecord = new WalRecord.Builder()
            .transactionId(transactionId)
            .type(type)
            .schema(schemaName)
            .table(tableName)
            .beforeImage(beforeImage)
            .afterImage(afterImage)
            .sql(sql)
            .build();
        
        walManager.writeRecord(dataRecord);
        
        logger.trace("Logged {} operation for transaction {}: {}.{}", 
                    type, transactionId, schemaName, tableName);
    }
    
    /**
     * Perform crash recovery on startup.
     */
    public WalRecoveryInfo performRecovery() throws IOException {
        logger.info("Starting crash recovery...");
        
        WalRecoveryInfo recoveryInfo = walManager.performRecovery();
        
        logger.info("Crash recovery completed: {}", recoveryInfo);
        return recoveryInfo;
    }
    
    /**
     * Create a checkpoint for truncating old WAL files.
     */
    public long checkpoint() throws IOException {
        return walManager.checkpoint();
    }
    
    /**
     * Enable or disable WAL logging.
     */
    public void setWalEnabled(boolean enabled) {
        this.walEnabled = enabled;
        logger.info("WAL logging {}", enabled ? "enabled" : "disabled");
    }
    
    /**
     * Check if WAL is enabled.
     */
    public boolean isWalEnabled() {
        return walEnabled;
    }
    
    /**
     * Get the WAL manager for direct access.
     */
    public WalManager getWalManager() {
        return walManager;
    }
    
    /**
     * Shutdown the WAL transaction manager.
     */
    @Override
    public void shutdown() {
        logger.info("Shutting down WAL Transaction Manager...");
        
        try {
            // Commit or rollback any remaining transactions
            for (WalTransaction walTxn : walTransactions.values()) {
                try {
                    if (walTxn.isActive()) {
                        logger.warn("Rolling back active transaction on shutdown: {}", walTxn.getId());
                        super.rollbackTransaction(walTxn.getDelegate());
                    }
                } catch (Exception e) {
                    logger.error("Error rolling back transaction on shutdown: " + walTxn.getId(), e);
                }
            }
            
            walTransactions.clear();
            
            // Shutdown WAL manager
            walManager.shutdown();
            
        } catch (IOException e) {
            logger.error("Error during WAL shutdown", e);
        } finally {
            // Shutdown parent transaction manager
            super.shutdown();
        }
        
        logger.info("WAL Transaction Manager shutdown complete");
    }
    
    /**
     * Get default WAL directory path.
     */
    private static String getDefaultWalDirectory() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        return Paths.get(tmpDir, "memgres", "wal").toString();
    }
}