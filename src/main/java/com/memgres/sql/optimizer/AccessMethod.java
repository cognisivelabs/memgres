package com.memgres.sql.optimizer;

/**
 * Enumeration of available access methods for query execution.
 */
public enum AccessMethod {
    
    /**
     * Full table scan - read all rows in the table.
     * Used when no indexes are available or when the optimizer
     * determines a scan is more efficient than index access.
     */
    TABLE_SCAN("Table Scan"),
    
    /**
     * Index scan - use an index to access rows.
     * More efficient when selective predicates are present.
     */
    INDEX_SCAN("Index Scan"),
    
    /**
     * Index seek - direct lookup in index.
     * Most efficient for equality predicates on indexed columns.
     */
    INDEX_SEEK("Index Seek"),
    
    /**
     * Empty result - query returns no rows without execution.
     * Used for queries with contradictory WHERE clauses.
     */
    EMPTY_RESULT("Empty Result"),
    
    /**
     * Error state - table not found or other issues.
     */
    TABLE_NOT_FOUND("Table Not Found");
    
    private final String description;
    
    AccessMethod(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if this access method uses an index.
     */
    public boolean usesIndex() {
        return this == INDEX_SCAN || this == INDEX_SEEK;
    }
    
    /**
     * Check if this access method reads the full table.
     */
    public boolean isFullScan() {
        return this == TABLE_SCAN;
    }
    
    /**
     * Get the relative efficiency of this access method.
     * Lower numbers indicate more efficient methods.
     */
    public int getEfficiencyRank() {
        switch (this) {
            case INDEX_SEEK: return 1;
            case INDEX_SCAN: return 2;
            case TABLE_SCAN: return 3;
            case EMPTY_RESULT: return 0;
            case TABLE_NOT_FOUND: return 999;
            default: return 100;
        }
    }
    
    @Override
    public String toString() {
        return description;
    }
}