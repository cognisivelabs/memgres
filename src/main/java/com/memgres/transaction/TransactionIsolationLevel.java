package com.memgres.transaction;

/**
 * Enumeration of transaction isolation levels supported by MemGres.
 * Based on PostgreSQL isolation levels with simplified implementation.
 */
public enum TransactionIsolationLevel {
    
    /**
     * Read Uncommitted - allows dirty reads, non-repeatable reads, and phantom reads.
     * Minimal isolation, highest concurrency.
     */
    READ_UNCOMMITTED("READ UNCOMMITTED", 0),
    
    /**
     * Read Committed - prevents dirty reads, but allows non-repeatable reads and phantom reads.
     * PostgreSQL default isolation level.
     */
    READ_COMMITTED("READ COMMITTED", 1),
    
    /**
     * Repeatable Read - prevents dirty reads and non-repeatable reads, but allows phantom reads.
     * Provides snapshot isolation within a transaction.
     */
    REPEATABLE_READ("REPEATABLE READ", 2),
    
    /**
     * Serializable - prevents all read phenomena (dirty reads, non-repeatable reads, phantom reads).
     * Highest isolation, may reduce concurrency due to serialization conflicts.
     */
    SERIALIZABLE("SERIALIZABLE", 3);
    
    private final String sqlName;
    private final int level;
    
    TransactionIsolationLevel(String sqlName, int level) {
        this.sqlName = sqlName;
        this.level = level;
    }
    
    /**
     * Get the SQL name of this isolation level
     * @return the SQL name
     */
    public String getSqlName() {
        return sqlName;
    }
    
    /**
     * Get the numeric level of this isolation level (higher = more isolated)
     * @return the numeric level
     */
    public int getLevel() {
        return level;
    }
    
    /**
     * Check if this isolation level prevents dirty reads
     * @return true if dirty reads are prevented
     */
    public boolean preventsDirtyReads() {
        return this != READ_UNCOMMITTED;
    }
    
    /**
     * Check if this isolation level prevents non-repeatable reads
     * @return true if non-repeatable reads are prevented
     */
    public boolean preventsNonRepeatableReads() {
        return this == REPEATABLE_READ || this == SERIALIZABLE;
    }
    
    /**
     * Check if this isolation level prevents phantom reads
     * @return true if phantom reads are prevented
     */
    public boolean preventsPhantomReads() {
        return this == SERIALIZABLE;
    }
    
    /**
     * Check if this isolation level provides snapshot isolation
     * @return true if snapshot isolation is provided
     */
    public boolean providesSnapshotIsolation() {
        return this == REPEATABLE_READ || this == SERIALIZABLE;
    }
    
    /**
     * Check if this isolation level requires serialization checks
     * @return true if serialization checks are required
     */
    public boolean requiresSerializationChecks() {
        return this == SERIALIZABLE;
    }
    
    /**
     * Compare isolation levels
     * @param other the other isolation level
     * @return true if this level is more restrictive than the other
     */
    public boolean isMoreRestrictiveThan(TransactionIsolationLevel other) {
        return this.level > other.level;
    }
    
    /**
     * Compare isolation levels
     * @param other the other isolation level
     * @return true if this level is less restrictive than the other
     */
    public boolean isLessRestrictiveThan(TransactionIsolationLevel other) {
        return this.level < other.level;
    }
    
    /**
     * Get an isolation level by its SQL name
     * @param sqlName the SQL name (case-insensitive)
     * @return the matching isolation level or null if not found
     */
    public static TransactionIsolationLevel fromSqlName(String sqlName) {
        if (sqlName == null) {
            return null;
        }
        
        String normalized = sqlName.toUpperCase().trim();
        for (TransactionIsolationLevel level : values()) {
            if (level.sqlName.equals(normalized)) {
                return level;
            }
        }
        
        return null;
    }
    
    @Override
    public String toString() {
        return sqlName;
    }
}