package com.memgres.wal;

/**
 * Contains information about a WAL recovery operation.
 * Provides statistics and results from crash recovery.
 */
public class WalRecoveryInfo {
    private int totalRecords;
    private int committedTransactions;
    private int abortedTransactions;
    private long recoveryTimeMs;
    private long startLsn;
    private long endLsn;
    private boolean successful;
    private String errorMessage;
    
    public WalRecoveryInfo() {
        this.successful = true;
    }
    
    // Getters and setters
    public int getTotalRecords() {
        return totalRecords;
    }
    
    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }
    
    public int getCommittedTransactions() {
        return committedTransactions;
    }
    
    public void setCommittedTransactions(int committedTransactions) {
        this.committedTransactions = committedTransactions;
    }
    
    public void incrementCommittedTransactions() {
        this.committedTransactions++;
    }
    
    public int getAbortedTransactions() {
        return abortedTransactions;
    }
    
    public void setAbortedTransactions(int abortedTransactions) {
        this.abortedTransactions = abortedTransactions;
    }
    
    public void incrementAbortedTransactions() {
        this.abortedTransactions++;
    }
    
    public long getRecoveryTimeMs() {
        return recoveryTimeMs;
    }
    
    public void setRecoveryTimeMs(long recoveryTimeMs) {
        this.recoveryTimeMs = recoveryTimeMs;
    }
    
    public long getStartLsn() {
        return startLsn;
    }
    
    public void setStartLsn(long startLsn) {
        this.startLsn = startLsn;
    }
    
    public long getEndLsn() {
        return endLsn;
    }
    
    public void setEndLsn(long endLsn) {
        this.endLsn = endLsn;
    }
    
    public boolean isSuccessful() {
        return successful;
    }
    
    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        this.successful = false;
    }
    
    public int getTotalTransactions() {
        return committedTransactions + abortedTransactions;
    }
    
    public double getCommitRatio() {
        int total = getTotalTransactions();
        return total > 0 ? (double) committedTransactions / total : 0.0;
    }
    
    @Override
    public String toString() {
        return String.format("WalRecoveryInfo{totalRecords=%d, committedTxn=%d, abortedTxn=%d, " +
                           "recoveryTime=%dms, successful=%s, commitRatio=%.2f}",
                           totalRecords, committedTransactions, abortedTransactions,
                           recoveryTimeMs, successful, getCommitRatio());
    }
}