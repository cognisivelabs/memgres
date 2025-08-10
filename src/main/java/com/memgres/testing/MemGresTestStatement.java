package com.memgres.testing;

import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionResult;

import java.sql.*;
import java.util.List;

/**
 * JDBC Statement implementation for MemGres testing.
 * 
 * <p>This is a simplified Statement implementation that provides basic SQL execution
 * capabilities for testing. It supports most common SQL operations but does not
 * implement all JDBC Statement features.</p>
 * 
 * @since 1.0.0
 */
public class MemGresTestStatement implements Statement {
    
    private final MemGresTestConnection connection;
    private final SqlExecutionEngine sqlEngine;
    private boolean closed = false;
    private ResultSet lastResultSet;
    private int updateCount = -1;
    private int maxRows = 0;
    private int queryTimeout = 0;
    
    /**
     * Creates a new MemGresTestStatement.
     * 
     * @param connection the connection
     * @param sqlEngine the SQL execution engine
     */
    public MemGresTestStatement(MemGresTestConnection connection, SqlExecutionEngine sqlEngine) {
        this.connection = connection;
        this.sqlEngine = sqlEngine;
    }
    
    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        checkClosed();
        
        // Ensure transaction exists if needed
        connection.ensureTransaction();
        
        SqlExecutionResult result;
        try {
            result = sqlEngine.execute(sql);
        } catch (com.memgres.sql.execution.SqlExecutionException e) {
            throw new SQLException("Query execution failed: " + e.getMessage(), e);
        }
        if (!result.isSuccess()) {
            throw new SQLException("Query execution failed: " + result.getMessage());
        }
        
        lastResultSet = new MemGresTestResultSet(result.getRows(), result.getColumns());
        updateCount = -1;
        
        return lastResultSet;
    }
    
    @Override
    public int executeUpdate(String sql) throws SQLException {
        checkClosed();
        
        // Ensure transaction exists if needed
        connection.ensureTransaction();
        
        SqlExecutionResult result;
        try {
            result = sqlEngine.execute(sql);
        } catch (com.memgres.sql.execution.SqlExecutionException e) {
            throw new SQLException("Update execution failed: " + e.getMessage(), e);
        }
        if (!result.isSuccess()) {
            throw new SQLException("Update execution failed: " + result.getMessage());
        }
        
        // Take snapshot after successful DML operations if in transaction
        connection.ensureTransactionSnapshotAfterDML(sql);
        
        lastResultSet = null;
        updateCount = result.getAffectedRows();
        
        return updateCount;
    }
    
    @Override
    public void close() throws SQLException {
        if (!closed) {
            if (lastResultSet != null) {
                lastResultSet.close();
            }
            closed = true;
        }
    }
    
    @Override
    public int getMaxFieldSize() throws SQLException {
        return 0; // No limit
    }
    
    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        checkClosed();
        // No-op
    }
    
    @Override
    public int getMaxRows() throws SQLException {
        return maxRows;
    }
    
    @Override
    public void setMaxRows(int max) throws SQLException {
        checkClosed();
        this.maxRows = max;
    }
    
    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        checkClosed();
        // No-op
    }
    
    @Override
    public int getQueryTimeout() throws SQLException {
        return queryTimeout;
    }
    
    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        checkClosed();
        this.queryTimeout = seconds;
    }
    
    @Override
    public void cancel() throws SQLException {
        checkClosed();
        // MemGres queries are fast, cancellation not needed
    }
    
    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }
    
    @Override
    public void clearWarnings() throws SQLException {
        // No-op
    }
    
    @Override
    public void setCursorName(String name) throws SQLException {
        throw new SQLFeatureNotSupportedException("Named cursors not supported");
    }
    
    @Override
    public boolean execute(String sql) throws SQLException {
        checkClosed();
        
        // Ensure transaction exists if needed
        connection.ensureTransaction();
        
        // Will take snapshot after successful execution if needed
        
        SqlExecutionResult result;
        try {
            result = sqlEngine.execute(sql);
        } catch (com.memgres.sql.execution.SqlExecutionException e) {
            throw new SQLException("SQL execution failed: " + e.getMessage(), e);
        }
        if (!result.isSuccess()) {
            throw new SQLException("SQL execution failed: " + result.getMessage());
        }
        
        // Take snapshot after successful DML operations if in transaction
        connection.ensureTransactionSnapshotAfterDML(sql);
        
        if (result.getRows() != null) {
            // Query that returns results
            lastResultSet = new MemGresTestResultSet(result.getRows(), result.getColumns());
            updateCount = -1;
            return true;
        } else {
            // Update/DDL statement
            lastResultSet = null;
            updateCount = result.getAffectedRows();
            return false;
        }
    }
    
    @Override
    public ResultSet getResultSet() throws SQLException {
        checkClosed();
        return lastResultSet;
    }
    
    @Override
    public int getUpdateCount() throws SQLException {
        checkClosed();
        return updateCount;
    }
    
    @Override
    public boolean getMoreResults() throws SQLException {
        checkClosed();
        return false; // MemGres doesn't support multiple result sets
    }
    
    // JDBC 2.0 methods
    
    @Override
    public void setFetchDirection(int direction) throws SQLException {
        checkClosed();
        if (direction != ResultSet.FETCH_FORWARD) {
            throw new SQLFeatureNotSupportedException("Only FETCH_FORWARD supported");
        }
    }
    
    @Override
    public int getFetchDirection() throws SQLException {
        return ResultSet.FETCH_FORWARD;
    }
    
    @Override
    public void setFetchSize(int rows) throws SQLException {
        checkClosed();
        // No-op
    }
    
    @Override
    public int getFetchSize() throws SQLException {
        return 0;
    }
    
    @Override
    public int getResultSetConcurrency() throws SQLException {
        return ResultSet.CONCUR_READ_ONLY;
    }
    
    @Override
    public int getResultSetType() throws SQLException {
        return ResultSet.TYPE_FORWARD_ONLY;
    }
    
    @Override
    public void addBatch(String sql) throws SQLException {
        throw new SQLFeatureNotSupportedException("Batch updates not supported");
    }
    
    @Override
    public void clearBatch() throws SQLException {
        throw new SQLFeatureNotSupportedException("Batch updates not supported");
    }
    
    @Override
    public int[] executeBatch() throws SQLException {
        throw new SQLFeatureNotSupportedException("Batch updates not supported");
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        return connection;
    }
    
    // JDBC 3.0 methods
    
    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return false;
    }
    
    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        throw new SQLFeatureNotSupportedException("Generated keys not supported");
    }
    
    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return executeUpdate(sql); // Ignore auto-generated keys
    }
    
    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return executeUpdate(sql); // Ignore column indexes
    }
    
    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return executeUpdate(sql); // Ignore column names
    }
    
    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return execute(sql); // Ignore auto-generated keys
    }
    
    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return execute(sql); // Ignore column indexes
    }
    
    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return execute(sql); // Ignore column names
    }
    
    @Override
    public int getResultSetHoldability() throws SQLException {
        return ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }
    
    @Override
    public boolean isClosed() throws SQLException {
        return closed;
    }
    
    // JDBC 4.0 methods
    
    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        // No-op
    }
    
    @Override
    public boolean isPoolable() throws SQLException {
        return false;
    }
    
    // JDBC 4.1 methods
    
    @Override
    public void closeOnCompletion() throws SQLException {
        throw new SQLFeatureNotSupportedException("Close on completion not supported");
    }
    
    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return false;
    }
    
    // Wrapper interface
    
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isAssignableFrom(getClass())) {
            return iface.cast(this);
        }
        throw new SQLException("Cannot unwrap to " + iface.getName());
    }
    
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(getClass());
    }
    
    private void checkClosed() throws SQLException {
        if (closed) {
            throw new SQLException("Statement is closed");
        }
    }
}