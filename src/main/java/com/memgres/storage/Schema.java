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
    private final ConcurrentMap<String, View> views;
    private final ConcurrentMap<String, Sequence> sequences;
    private final ReadWriteLock schemaLock;
    
    public Schema(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Schema name cannot be null or empty");
        }
        
        this.name = name.toLowerCase(); // PostgreSQL converts schema names to lowercase
        this.tables = new ConcurrentHashMap<>();
        this.views = new ConcurrentHashMap<>();
        this.sequences = new ConcurrentHashMap<>();
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
    
    // ===== VIEW MANAGEMENT METHODS =====
    
    /**
     * Create a view in this schema
     * @param view the view to create
     * @return true if view was created, false if it already exists
     */
    public boolean createView(View view) {
        if (view == null) {
            throw new IllegalArgumentException("View cannot be null");
        }
        
        String viewName = view.getName().toLowerCase();
        
        schemaLock.writeLock().lock();
        try {
            if (views.containsKey(viewName)) {
                logger.warn("View already exists in schema {}: {}", name, viewName);
                return false;
            }
            
            views.put(viewName, view);
            logger.debug("Created view {} in schema {}", viewName, name);
            return true;
        } finally {
            schemaLock.writeLock().unlock();
        }
    }
    
    /**
     * Create or replace a view in this schema
     * @param view the view to create or replace
     * @return true if view was created or replaced
     */
    public boolean createOrReplaceView(View view) {
        if (view == null) {
            throw new IllegalArgumentException("View cannot be null");
        }
        
        String viewName = view.getName().toLowerCase();
        
        schemaLock.writeLock().lock();
        try {
            View existingView = views.put(viewName, view);
            if (existingView != null) {
                logger.debug("Replaced existing view {} in schema {}", viewName, name);
            } else {
                logger.debug("Created view {} in schema {}", viewName, name);
            }
            return true;
        } finally {
            schemaLock.writeLock().unlock();
        }
    }
    
    /**
     * Drop a view from this schema
     * @param viewName the name of the view to drop
     * @return true if view was dropped, false if it didn't exist
     */
    public boolean dropView(String viewName) {
        if (viewName == null || viewName.trim().isEmpty()) {
            throw new IllegalArgumentException("View name cannot be null or empty");
        }
        
        String normalizedName = viewName.toLowerCase();
        
        schemaLock.writeLock().lock();
        try {
            View removedView = views.remove(normalizedName);
            if (removedView != null) {
                logger.debug("Dropped view {} from schema {}", normalizedName, name);
                return true;
            } else {
                logger.warn("View does not exist in schema {}: {}", name, normalizedName);
                return false;
            }
        } finally {
            schemaLock.writeLock().unlock();
        }
    }
    
    /**
     * Get a view by name
     * @param viewName the view name
     * @return the view or null if not found
     */
    public View getView(String viewName) {
        if (viewName == null || viewName.trim().isEmpty()) {
            return null;
        }
        
        String normalizedName = viewName.toLowerCase();
        
        schemaLock.readLock().lock();
        try {
            return views.get(normalizedName);
        } finally {
            schemaLock.readLock().unlock();
        }
    }
    
    /**
     * Check if a view exists in this schema
     * @param viewName the view name
     * @return true if the view exists
     */
    public boolean hasView(String viewName) {
        return getView(viewName) != null;
    }
    
    /**
     * Get all view names in this schema
     * @return set of view names
     */
    public Set<String> getViewNames() {
        schemaLock.readLock().lock();
        try {
            return Set.copyOf(views.keySet());
        } finally {
            schemaLock.readLock().unlock();
        }
    }
    
    /**
     * Get the number of views in this schema
     * @return number of views
     */
    public int getViewCount() {
        schemaLock.readLock().lock();
        try {
            return views.size();
        } finally {
            schemaLock.readLock().unlock();
        }
    }
    
    // ===== SEQUENCE MANAGEMENT METHODS =====
    
    /**
     * Create a sequence in this schema
     * @param sequence the sequence to create
     * @return true if sequence was created, false if it already exists
     */
    public boolean createSequence(Sequence sequence) {
        if (sequence == null) {
            throw new IllegalArgumentException("Sequence cannot be null");
        }
        
        String sequenceName = sequence.getName().toLowerCase();
        
        schemaLock.writeLock().lock();
        try {
            if (sequences.containsKey(sequenceName)) {
                logger.warn("Sequence already exists in schema {}: {}", name, sequenceName);
                return false;
            }
            
            sequences.put(sequenceName, sequence);
            logger.debug("Created sequence {} in schema {}", sequenceName, name);
            return true;
        } finally {
            schemaLock.writeLock().unlock();
        }
    }
    
    /**
     * Drop a sequence from this schema
     * @param sequenceName the name of the sequence to drop
     * @return true if sequence was dropped, false if it didn't exist
     */
    public boolean dropSequence(String sequenceName) {
        if (sequenceName == null || sequenceName.trim().isEmpty()) {
            throw new IllegalArgumentException("Sequence name cannot be null or empty");
        }
        
        String normalizedName = sequenceName.toLowerCase();
        
        schemaLock.writeLock().lock();
        try {
            Sequence removedSequence = sequences.remove(normalizedName);
            if (removedSequence != null) {
                logger.debug("Dropped sequence {} from schema {}", normalizedName, name);
                return true;
            } else {
                logger.warn("Sequence does not exist in schema {}: {}", name, normalizedName);
                return false;
            }
        } finally {
            schemaLock.writeLock().unlock();
        }
    }
    
    /**
     * Get a sequence by name
     * @param sequenceName the sequence name
     * @return the sequence or null if not found
     */
    public Sequence getSequence(String sequenceName) {
        if (sequenceName == null || sequenceName.trim().isEmpty()) {
            return null;
        }
        
        String normalizedName = sequenceName.toLowerCase();
        
        schemaLock.readLock().lock();
        try {
            return sequences.get(normalizedName);
        } finally {
            schemaLock.readLock().unlock();
        }
    }
    
    /**
     * Check if a sequence exists in this schema
     * @param sequenceName the sequence name
     * @return true if the sequence exists
     */
    public boolean hasSequence(String sequenceName) {
        return getSequence(sequenceName) != null;
    }
    
    /**
     * Get all sequence names in this schema
     * @return set of sequence names
     */
    public Set<String> getSequenceNames() {
        schemaLock.readLock().lock();
        try {
            return Set.copyOf(sequences.keySet());
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
                ", viewCount=" + getViewCount() +
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