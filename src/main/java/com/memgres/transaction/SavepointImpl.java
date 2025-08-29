package com.memgres.transaction;

import java.sql.SQLException;
import java.sql.Savepoint;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MemGres implementation of JDBC Savepoint interface.
 * 
 * <p>Savepoints provide a way to create nested rollback points within a transaction.
 * This allows partial rollback of transaction changes without rolling back the 
 * entire transaction.</p>
 * 
 * <p>Each savepoint has a unique ID and optional name, and tracks the point in
 * time when it was created within the transaction.</p>
 * 
 * @since 1.0.0
 */
public class SavepointImpl implements Savepoint {
    
    private static final AtomicLong ID_GENERATOR = new AtomicLong(0);
    
    private final int savepointId;
    private final String savepointName;
    private final LocalDateTime createdAt;
    private final long transactionId;
    private volatile boolean released = false;
    private final Map<String, TableSnapshot> tableSnapshots = new ConcurrentHashMap<>();
    
    /**
     * Create an unnamed savepoint.
     * 
     * @param transactionId the ID of the transaction this savepoint belongs to
     */
    public SavepointImpl(long transactionId) {
        this.savepointId = (int) ID_GENERATOR.incrementAndGet();
        this.savepointName = null;
        this.transactionId = transactionId;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * Create a named savepoint.
     * 
     * @param transactionId the ID of the transaction this savepoint belongs to
     * @param name the name of the savepoint
     */
    public SavepointImpl(long transactionId, String name) {
        this.savepointId = (int) ID_GENERATOR.incrementAndGet();
        this.savepointName = Objects.requireNonNull(name, "Savepoint name cannot be null");
        this.transactionId = transactionId;
        this.createdAt = LocalDateTime.now();
    }
    
    @Override
    public int getSavepointId() throws SQLException {
        checkNotReleased();
        if (savepointName != null) {
            throw new SQLException("This is a named savepoint; ID is not available");
        }
        return savepointId;
    }
    
    @Override
    public String getSavepointName() throws SQLException {
        checkNotReleased();
        if (savepointName == null) {
            throw new SQLException("This is an unnamed savepoint; name is not available");
        }
        return savepointName;
    }
    
    /**
     * Get the internal savepoint ID (always available regardless of named/unnamed).
     * 
     * @return the internal savepoint ID
     */
    public int getInternalId() {
        return savepointId;
    }
    
    /**
     * Get the name of the savepoint, or null if unnamed.
     * 
     * @return the savepoint name or null
     */
    public String getInternalName() {
        return savepointName;
    }
    
    /**
     * Check if this is a named savepoint.
     * 
     * @return true if this savepoint has a name
     */
    public boolean isNamed() {
        return savepointName != null;
    }
    
    /**
     * Get the transaction ID this savepoint belongs to.
     * 
     * @return the transaction ID
     */
    public long getTransactionId() {
        return transactionId;
    }
    
    /**
     * Get the time when this savepoint was created.
     * 
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Check if this savepoint has been released.
     * 
     * @return true if released
     */
    public boolean isReleased() {
        return released;
    }
    
    /**
     * Mark this savepoint as released.
     * Once released, the savepoint cannot be used for rollback operations.
     */
    public void release() {
        this.released = true;
        // Clear snapshots to free memory
        tableSnapshots.clear();
    }
    
    /**
     * Add a table snapshot to this savepoint.
     * 
     * @param snapshot the table snapshot
     */
    public void addTableSnapshot(TableSnapshot snapshot) {
        if (!released) {
            tableSnapshots.put(snapshot.getTableKey(), snapshot);
        }
    }
    
    /**
     * Get table snapshots for this savepoint.
     * 
     * @return map of table key to snapshot
     */
    public Map<String, TableSnapshot> getTableSnapshots() {
        return Map.copyOf(tableSnapshots);
    }
    
    /**
     * Check that this savepoint has not been released.
     * 
     * @throws SQLException if the savepoint has been released
     */
    private void checkNotReleased() throws SQLException {
        if (released) {
            throw new SQLException("Savepoint has been released and is no longer valid");
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        SavepointImpl that = (SavepointImpl) o;
        return savepointId == that.savepointId &&
               transactionId == that.transactionId &&
               Objects.equals(savepointName, that.savepointName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(savepointId, savepointName, transactionId);
    }
    
    @Override
    public String toString() {
        return String.format("Savepoint{id=%d, name='%s', transaction=%d, created=%s, released=%s}",
            savepointId, savepointName, transactionId, createdAt, released);
    }
}