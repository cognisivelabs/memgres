package com.memgres.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Properties;

/**
 * H2 Database adapter for benchmarking comparisons.
 * Provides a standardized interface to benchmark against H2 in-memory database.
 */
public class H2DatabaseAdapter implements BenchmarkRunner.DatabaseAdapter {
    private static final Logger logger = LoggerFactory.getLogger(H2DatabaseAdapter.class);
    
    private static final String H2_DRIVER = "org.h2.Driver";
    private static final String DEFAULT_URL = "jdbc:h2:mem:benchmark;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE";
    
    private final String jdbcUrl;
    private final Properties connectionProperties;
    private Connection connection;
    
    public H2DatabaseAdapter() {
        this(DEFAULT_URL);
    }
    
    public H2DatabaseAdapter(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        this.connectionProperties = new Properties();
        this.connectionProperties.setProperty("user", "sa");
        this.connectionProperties.setProperty("password", "");
    }
    
    /**
     * Set additional connection properties.
     */
    public H2DatabaseAdapter withProperty(String key, String value) {
        connectionProperties.setProperty(key, value);
        return this;
    }
    
    @Override
    public void initialize() throws Exception {
        try {
            // Load H2 driver
            Class.forName(H2_DRIVER);
            
            // Create connection
            connection = DriverManager.getConnection(jdbcUrl, connectionProperties);
            connection.setAutoCommit(true);
            
            // Configure H2 for optimal performance
            executeSQL("SET CACHE_SIZE 262144"); // 256MB cache
            executeSQL("SET LOCK_TIMEOUT 10000"); // 10 second lock timeout
            executeSQL("SET LOG 0"); // Disable transaction log for performance
            executeSQL("SET UNDO_LOG 0"); // Disable undo log for performance
            executeSQL("SET MAX_MEMORY_ROWS 1000000"); // Allow large result sets in memory
            
            logger.info("H2 database adapter initialized with URL: {}", jdbcUrl);
            
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("H2 database driver not found. Add H2 dependency to classpath.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize H2 database connection", e);
        }
    }
    
    @Override
    public void cleanup() throws Exception {
        if (connection != null && !connection.isClosed()) {
            try {
                // Drop all objects to clean up
                executeSQL("DROP ALL OBJECTS");
            } catch (SQLException e) {
                logger.debug("Error dropping objects during cleanup", e);
            }
            
            connection.close();
            connection = null;
            logger.info("H2 database adapter cleaned up");
        }
    }
    
    @Override
    public void executeSQL(String sql) throws SQLException {
        validateConnection();
        
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            logger.debug("SQL execution failed: {} - Error: {}", sql, e.getMessage());
            throw e;
        }
    }
    
    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        validateConnection();
        
        try {
            Statement statement = connection.createStatement();
            return statement.executeQuery(sql);
        } catch (SQLException e) {
            logger.debug("Query execution failed: {} - Error: {}", sql, e.getMessage());
            throw e;
        }
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        validateConnection();
        return connection;
    }
    
    /**
     * Execute a prepared statement with parameters.
     */
    public void executePrepared(String sql, Object... parameters) throws SQLException {
        validateConnection();
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            statement.execute();
        }
    }
    
    /**
     * Execute a prepared query with parameters.
     */
    public ResultSet queryPrepared(String sql, Object... parameters) throws SQLException {
        validateConnection();
        
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < parameters.length; i++) {
            statement.setObject(i + 1, parameters[i]);
        }
        return statement.executeQuery();
    }
    
    /**
     * Get database metadata and version information.
     */
    public String getDatabaseInfo() throws SQLException {
        validateConnection();
        
        DatabaseMetaData metaData = connection.getMetaData();
        return String.format("H2 Database %s.%s (%s)",
            metaData.getDatabaseMajorVersion(),
            metaData.getDatabaseMinorVersion(),
            metaData.getDatabaseProductVersion());
    }
    
    /**
     * Enable/disable auto-commit mode.
     */
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        validateConnection();
        connection.setAutoCommit(autoCommit);
    }
    
    /**
     * Commit current transaction.
     */
    public void commit() throws SQLException {
        validateConnection();
        connection.commit();
    }
    
    /**
     * Rollback current transaction.
     */
    public void rollback() throws SQLException {
        validateConnection();
        connection.rollback();
    }
    
    /**
     * Get table row count.
     */
    public long getRowCount(String tableName) throws SQLException {
        try (ResultSet rs = executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        }
    }
    
    /**
     * Check if table exists.
     */
    public boolean tableExists(String tableName) throws SQLException {
        validateConnection();
        
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getTables(null, null, tableName.toUpperCase(), new String[]{"TABLE"})) {
            return rs.next();
        }
    }
    
    /**
     * Get H2 performance statistics.
     */
    public String getPerformanceStats() throws SQLException {
        StringBuilder stats = new StringBuilder();
        
        try (ResultSet rs = executeQuery("SELECT * FROM INFORMATION_SCHEMA.SETTINGS WHERE SETTING_NAME LIKE '%CACHE%'")) {
            stats.append("H2 Cache Settings:\n");
            while (rs.next()) {
                stats.append(String.format("  %s = %s\n", 
                    rs.getString("SETTING_NAME"), 
                    rs.getString("SETTING_VALUE")));
            }
        }
        
        try (ResultSet rs = executeQuery("SELECT * FROM INFORMATION_SCHEMA.MEMORY_USAGE")) {
            stats.append("\nMemory Usage:\n");
            while (rs.next()) {
                stats.append(String.format("  %s: %s KB\n",
                    rs.getString("OBJECT_NAME"),
                    rs.getString("MEMORY_USED")));
            }
        } catch (SQLException e) {
            // Memory usage view might not be available in all H2 versions
            logger.debug("Memory usage information not available", e);
        }
        
        return stats.toString();
    }
    
    /**
     * Validate that connection is available.
     */
    private void validateConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new SQLException("H2 database connection is not available");
        }
    }
    
    /**
     * Create an H2 adapter with optimizations for benchmarking.
     */
    public static H2DatabaseAdapter createOptimized() {
        String optimizedUrl = "jdbc:h2:mem:benchmark;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;" +
            "DATABASE_TO_LOWER=TRUE;CACHE_SIZE=262144;MAX_MEMORY_ROWS=1000000;" +
            "LOCK_TIMEOUT=10000;LOG=0;UNDO_LOG=0";
        
        return new H2DatabaseAdapter(optimizedUrl);
    }
    
    /**
     * Create an H2 adapter with file-based storage (for persistent benchmarks).
     */
    public static H2DatabaseAdapter createFileBased(String dbPath) {
        String fileUrl = "jdbc:h2:" + dbPath + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE";
        return new H2DatabaseAdapter(fileUrl);
    }
}