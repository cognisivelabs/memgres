package com.memgres.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.Set;

/**
 * Represents a database schema containing tables and other database objects.
 */
public class Schema {
    private static final Logger logger = LoggerFactory.getLogger(Schema.class);
    
    private final String name;
    private final ConcurrentMap<String, Table> tables;
    private final ReadWriteLock schemaLock;
    
    public Schema(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Schema name cannot be null or empty");
        }
        
        this.name = name.toLowerCase(); // PostgreSQL converts schema names to lowercase
        this.tables = new ConcurrentHashMap<>();
        this.schemaLock = new ReentrantReadWriteLock();
        
        logger.debug("Created schema: {}", this.name);
    }
    
    /**
     * Get the schema name
     * @return the schema name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Create a table in this schema
     * @param table the table to create
     * @return true if table was created, false if it already exists
     */
    public boolean createTable(Table table) {
        if (table == null) {
            throw new IllegalArgumentException("Table cannot be null");
        }
        
        String tableName = table.getName().toLowerCase();
        
        schemaLock.writeLock().lock();
        try {
            if (tables.containsKey(tableName)) {
                logger.warn("Table already exists in schema {}: {}", name, tableName);
                return false;
            }
            
            tables.put(tableName, table);
            logger.debug("Created table {} in schema {}", tableName, name);
            return true;
        } finally {
            schemaLock.writeLock().unlock();
        }
    }
    
    /**
     * Drop a table from this schema
     * @param tableName the name of the table to drop
     * @return true if table was dropped, false if it didn't exist
     */
    public boolean dropTable(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        
        String normalizedName = tableName.toLowerCase();
        
        schemaLock.writeLock().lock();
        try {
            Table removedTable = tables.remove(normalizedName);
            if (removedTable != null) {
                logger.debug("Dropped table {} from schema {}", normalizedName, name);
                return true;
            } else {
                logger.warn("Table does not exist in schema {}: {}", name, normalizedName);
                return false;
            }
        } finally {
            schemaLock.writeLock().unlock();
        }
    }
    
    /**
     * Get a table by name
     * @param tableName the table name
     * @return the table or null if not found
     */
    public Table getTable(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            return null;
        }
        
        String normalizedName = tableName.toLowerCase();
        
        schemaLock.readLock().lock();
        try {
            return tables.get(normalizedName);
        } finally {
            schemaLock.readLock().unlock();
        }
    }
    
    /**
     * Check if a table exists in this schema
     * @param tableName the table name
     * @return true if the table exists
     */
    public boolean hasTable(String tableName) {
        return getTable(tableName) != null;
    }
    
    /**
     * Check if this schema has any tables
     * @return true if the schema contains at least one table
     */
    public boolean hasTable() {
        schemaLock.readLock().lock();
        try {
            return !tables.isEmpty();
        } finally {
            schemaLock.readLock().unlock();
        }
    }
    
    /**
     * Get all table names in this schema
     * @return set of table names
     */
    public Set<String> getTableNames() {
        schemaLock.readLock().lock();
        try {
            return Set.copyOf(tables.keySet());
        } finally {
            schemaLock.readLock().unlock();
        }
    }
    
    /**
     * Get the number of tables in this schema
     * @return number of tables
     */
    public int getTableCount() {
        schemaLock.readLock().lock();
        try {
            return tables.size();
        } finally {
            schemaLock.readLock().unlock();
        }
    }
    
    @Override
    public String toString() {
        return "Schema{" +
                "name='" + name + '\'' +
                ", tableCount=" + getTableCount() +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Schema schema = (Schema) o;
        return name.equals(schema.name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}