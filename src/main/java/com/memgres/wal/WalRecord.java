package com.memgres.wal;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a single Write-Ahead Log record.
 * Each record contains transaction information and the operations performed.
 */
public class WalRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final long lsn; // Log Sequence Number
    private final long transactionId;
    private final RecordType type;
    private final String schemaName;
    private final String tableName;
    private final Object[] beforeImage; // Data before the operation (for UNDO)
    private final Object[] afterImage;  // Data after the operation (for REDO)
    private final String sqlStatement;  // Original SQL for debugging
    private final Instant timestamp;
    private final long checksum;
    
    public enum RecordType {
        BEGIN_TRANSACTION,  // Transaction start
        COMMIT_TRANSACTION, // Transaction commit
        ABORT_TRANSACTION,  // Transaction rollback
        INSERT,             // Row insertion
        UPDATE,             // Row update
        DELETE,             // Row deletion
        CHECKPOINT,         // Checkpoint marker
        SCHEMA_CHANGE       // DDL operations
    }
    
    private WalRecord(Builder builder) {
        this.lsn = builder.lsn;
        this.transactionId = builder.transactionId;
        this.type = builder.type;
        this.schemaName = builder.schemaName;
        this.tableName = builder.tableName;
        this.beforeImage = builder.beforeImage;
        this.afterImage = builder.afterImage;
        this.sqlStatement = builder.sqlStatement;
        this.timestamp = builder.timestamp != null ? builder.timestamp : Instant.now();
        this.checksum = calculateChecksum();
    }
    
    // Getters
    public long getLsn() { return lsn; }
    public long getTransactionId() { return transactionId; }
    public RecordType getType() { return type; }
    public String getSchemaName() { return schemaName; }
    public String getTableName() { return tableName; }
    public Object[] getBeforeImage() { return beforeImage != null ? beforeImage.clone() : null; }
    public Object[] getAfterImage() { return afterImage != null ? afterImage.clone() : null; }
    public String getSqlStatement() { return sqlStatement; }
    public Instant getTimestamp() { return timestamp; }
    public long getChecksum() { return checksum; }
    
    /**
     * Calculate checksum for integrity verification.
     */
    private long calculateChecksum() {
        long hash = 17;
        hash = 31 * hash + lsn;
        hash = 31 * hash + transactionId;
        hash = 31 * hash + Objects.hashCode(type);
        hash = 31 * hash + Objects.hashCode(schemaName);
        hash = 31 * hash + Objects.hashCode(tableName);
        hash = 31 * hash + Objects.hashCode(sqlStatement);
        if (beforeImage != null) {
            for (Object obj : beforeImage) {
                hash = 31 * hash + Objects.hashCode(obj);
            }
        }
        if (afterImage != null) {
            for (Object obj : afterImage) {
                hash = 31 * hash + Objects.hashCode(obj);
            }
        }
        return hash;
    }
    
    /**
     * Verify record integrity using checksum.
     */
    public boolean isValid() {
        return checksum == calculateChecksum();
    }
    
    /**
     * Check if this record represents a data modification.
     */
    public boolean isDataModification() {
        return type == RecordType.INSERT || type == RecordType.UPDATE || type == RecordType.DELETE;
    }
    
    /**
     * Check if this record represents a transaction boundary.
     */
    public boolean isTransactionBoundary() {
        return type == RecordType.BEGIN_TRANSACTION || 
               type == RecordType.COMMIT_TRANSACTION || 
               type == RecordType.ABORT_TRANSACTION;
    }
    
    @Override
    public String toString() {
        return String.format("WalRecord{lsn=%d, txnId=%d, type=%s, table=%s.%s, sql='%s', timestamp=%s}",
            lsn, transactionId, type, schemaName, tableName, 
            sqlStatement != null ? sqlStatement.substring(0, Math.min(50, sqlStatement.length())) : null,
            timestamp);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        WalRecord that = (WalRecord) obj;
        return lsn == that.lsn && transactionId == that.transactionId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(lsn, transactionId);
    }
    
    /**
     * Builder for creating WalRecord instances.
     */
    public static class Builder {
        private long lsn;
        private long transactionId;
        private RecordType type;
        private String schemaName;
        private String tableName;
        private Object[] beforeImage;
        private Object[] afterImage;
        private String sqlStatement;
        private Instant timestamp;
        
        public Builder lsn(long lsn) {
            this.lsn = lsn;
            return this;
        }
        
        public Builder transactionId(long transactionId) {
            this.transactionId = transactionId;
            return this;
        }
        
        public Builder type(RecordType type) {
            this.type = type;
            return this;
        }
        
        public Builder schema(String schemaName) {
            this.schemaName = schemaName;
            return this;
        }
        
        public Builder table(String tableName) {
            this.tableName = tableName;
            return this;
        }
        
        public Builder beforeImage(Object[] beforeImage) {
            this.beforeImage = beforeImage != null ? beforeImage.clone() : null;
            return this;
        }
        
        public Builder afterImage(Object[] afterImage) {
            this.afterImage = afterImage != null ? afterImage.clone() : null;
            return this;
        }
        
        public Builder sql(String sqlStatement) {
            this.sqlStatement = sqlStatement;
            return this;
        }
        
        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public WalRecord build() {
            Objects.requireNonNull(type, "Record type is required");
            // LSN can be 0 initially; WalManager will assign it during writeRecord
            return new WalRecord(this);
        }
    }
}