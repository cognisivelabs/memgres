package com.memgres.wal;

import com.memgres.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * WAL-enabled transaction that logs all operations for durability.
 * Wraps a regular Transaction and adds WAL logging capabilities.
 */
public class WalTransaction {
    private static final Logger logger = LoggerFactory.getLogger(WalTransaction.class);
    
    private final Transaction delegateTransaction;
    private final WalManager walManager;
    private final List<WalRecord> pendingRecords;
    
    public WalTransaction(Transaction transaction, WalManager walManager) {
        this.delegateTransaction = transaction;
        this.walManager = walManager;
        this.pendingRecords = new ArrayList<>();
    }
    
    /**
     * Log an INSERT operation.
     */
    public void logInsert(String schemaName, String tableName, Object[] afterImage, String sql) {
        try {
            WalRecord record = new WalRecord.Builder()
                .transactionId(delegateTransaction.getId())
                .type(WalRecord.RecordType.INSERT)
                .schema(schemaName)
                .table(tableName)
                .afterImage(afterImage)
                .sql(sql)
                .build();
            
            walManager.writeRecord(record);
            pendingRecords.add(record);
            
            logger.trace("Logged INSERT for transaction {}: {}.{}", delegateTransaction.getId(), schemaName, tableName);
            
        } catch (IOException e) {
            logger.error("Failed to log INSERT operation for transaction: " + delegateTransaction.getId(), e);
            // Don't fail the transaction, but mark for potential issues
        }
    }
    
    /**
     * Log an UPDATE operation.
     */
    public void logUpdate(String schemaName, String tableName, 
                         Object[] beforeImage, Object[] afterImage, String sql) {
        try {
            WalRecord record = new WalRecord.Builder()
                .transactionId(delegateTransaction.getId())
                .type(WalRecord.RecordType.UPDATE)
                .schema(schemaName)
                .table(tableName)
                .beforeImage(beforeImage)
                .afterImage(afterImage)
                .sql(sql)
                .build();
            
            walManager.writeRecord(record);
            pendingRecords.add(record);
            
            logger.trace("Logged UPDATE for transaction {}: {}.{}", delegateTransaction.getId(), schemaName, tableName);
            
        } catch (IOException e) {
            logger.error("Failed to log UPDATE operation for transaction: " + delegateTransaction.getId(), e);
        }
    }
    
    /**
     * Log a DELETE operation.
     */
    public void logDelete(String schemaName, String tableName, Object[] beforeImage, String sql) {
        try {
            WalRecord record = new WalRecord.Builder()
                .transactionId(delegateTransaction.getId())
                .type(WalRecord.RecordType.DELETE)
                .schema(schemaName)
                .table(tableName)
                .beforeImage(beforeImage)
                .sql(sql)
                .build();
            
            walManager.writeRecord(record);
            pendingRecords.add(record);
            
            logger.trace("Logged DELETE for transaction {}: {}.{}", delegateTransaction.getId(), schemaName, tableName);
            
        } catch (IOException e) {
            logger.error("Failed to log DELETE operation for transaction: " + delegateTransaction.getId(), e);
        }
    }
    
    /**
     * Log a schema change operation.
     */
    public void logSchemaChange(String schemaName, String tableName, String sql) {
        try {
            WalRecord record = new WalRecord.Builder()
                .transactionId(delegateTransaction.getId())
                .type(WalRecord.RecordType.SCHEMA_CHANGE)
                .schema(schemaName)
                .table(tableName)
                .sql(sql)
                .build();
            
            walManager.writeRecord(record);
            pendingRecords.add(record);
            
            logger.trace("Logged SCHEMA_CHANGE for transaction {}: {}", delegateTransaction.getId(), sql);
            
        } catch (IOException e) {
            logger.error("Failed to log SCHEMA_CHANGE operation for transaction: " + delegateTransaction.getId(), e);
        }
    }
    
    /**
     * Get all pending WAL records for this transaction.
     */
    public List<WalRecord> getPendingRecords() {
        return new ArrayList<>(pendingRecords);
    }
    
    /**
     * Clear pending records (used after commit/rollback).
     */
    public void clearPendingRecords() {
        pendingRecords.clear();
    }
    
    // Delegate methods to access underlying transaction
    
    public long getId() {
        return delegateTransaction.getId();
    }
    
    public Transaction getDelegate() {
        return delegateTransaction;
    }
    
    public boolean isActive() {
        return delegateTransaction.isActive();
    }
    
    public boolean isCommitted() {
        return delegateTransaction.isCommitted();
    }
    
    public boolean isRolledBack() {
        return delegateTransaction.isRolledBack();
    }
    
    @Override
    public String toString() {
        return String.format("WalTransaction{id=%d, isolation=%s, active=%s, pendingRecords=%d}",
            delegateTransaction.getId(), delegateTransaction.getIsolationLevel(), 
            delegateTransaction.isActive(), pendingRecords.size());
    }
}