package com.memgres.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Wrapper around a real Connection that manages pooling lifecycle.
 * When close() is called, the connection is returned to the pool instead of being closed.
 */
public class PooledConnection implements Connection {
    private static final Logger logger = LoggerFactory.getLogger(PooledConnection.class);
    private static final AtomicLong CONNECTION_ID_GENERATOR = new AtomicLong(0);
    
    private final Connection realConnection;
    private final ConnectionPool pool;
    private final long connectionId;
    private final long createdTime;
    
    private volatile long lastAccessTime;
    private volatile boolean acquired;
    private volatile boolean closed;
    
    public PooledConnection(Connection realConnection, ConnectionPool pool) {
        this.realConnection = realConnection;
        this.pool = pool;
        this.connectionId = CONNECTION_ID_GENERATOR.incrementAndGet();
        this.createdTime = System.currentTimeMillis();
        this.lastAccessTime = createdTime;
        this.acquired = false;
        this.closed = false;
    }
    
    /**
     * Mark this connection as acquired from the pool.
     */
    public void markAsAcquired() {
        this.acquired = true;
        this.lastAccessTime = System.currentTimeMillis();
    }
    
    /**
     * Mark this connection as returned to the pool.
     */
    public void markAsReturned() {
        this.acquired = false;
        this.lastAccessTime = System.currentTimeMillis();
    }
    
    /**
     * Check if this connection belongs to the given pool.
     */
    public boolean isFromThisPool(ConnectionPool pool) {
        return this.pool == pool;
    }
    
    /**
     * Get the time this connection has been idle.
     */
    public long getIdleTime() {
        return acquired ? 0 : System.currentTimeMillis() - lastAccessTime;
    }
    
    /**
     * Get the age of this connection.
     */
    public long getAge() {
        return System.currentTimeMillis() - createdTime;
    }
    
    /**
     * Get the connection ID.
     */
    public long getConnectionId() {
        return connectionId;
    }
    
    /**
     * Really close the underlying connection (called by pool on destroy).
     */
    public void reallyClose() throws SQLException {
        closed = true;
        realConnection.close();
        logger.debug("Pooled connection {} really closed", connectionId);
    }
    
    // Connection interface implementation - delegate to real connection
    
    @Override
    public void close() throws SQLException {
        if (closed) {
            return;
        }
        
        // Instead of closing, return to pool
        pool.returnConnection(this);
        logger.debug("Pooled connection {} returned to pool", connectionId);
    }
    
    @Override
    public boolean isClosed() throws SQLException {
        return closed || realConnection.isClosed();
    }
    
    @Override
    public boolean isValid(int timeout) throws SQLException {
        updateLastAccess();
        return !closed && realConnection.isValid(timeout);
    }
    
    @Override
    public Statement createStatement() throws SQLException {
        updateLastAccess();
        return realConnection.createStatement();
    }
    
    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        updateLastAccess();
        return realConnection.prepareStatement(sql);
    }
    
    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        updateLastAccess();
        return realConnection.prepareCall(sql);
    }
    
    @Override
    public String nativeSQL(String sql) throws SQLException {
        updateLastAccess();
        return realConnection.nativeSQL(sql);
    }
    
    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        updateLastAccess();
        realConnection.setAutoCommit(autoCommit);
    }
    
    @Override
    public boolean getAutoCommit() throws SQLException {
        updateLastAccess();
        return realConnection.getAutoCommit();
    }
    
    @Override
    public void commit() throws SQLException {
        updateLastAccess();
        realConnection.commit();
    }
    
    @Override
    public void rollback() throws SQLException {
        updateLastAccess();
        realConnection.rollback();
    }
    
    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        updateLastAccess();
        return realConnection.getMetaData();
    }
    
    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        updateLastAccess();
        realConnection.setReadOnly(readOnly);
    }
    
    @Override
    public boolean isReadOnly() throws SQLException {
        updateLastAccess();
        return realConnection.isReadOnly();
    }
    
    @Override
    public void setCatalog(String catalog) throws SQLException {
        updateLastAccess();
        realConnection.setCatalog(catalog);
    }
    
    @Override
    public String getCatalog() throws SQLException {
        updateLastAccess();
        return realConnection.getCatalog();
    }
    
    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        updateLastAccess();
        realConnection.setTransactionIsolation(level);
    }
    
    @Override
    public int getTransactionIsolation() throws SQLException {
        updateLastAccess();
        return realConnection.getTransactionIsolation();
    }
    
    @Override
    public SQLWarning getWarnings() throws SQLException {
        updateLastAccess();
        return realConnection.getWarnings();
    }
    
    @Override
    public void clearWarnings() throws SQLException {
        updateLastAccess();
        realConnection.clearWarnings();
    }
    
    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        updateLastAccess();
        return realConnection.createStatement(resultSetType, resultSetConcurrency);
    }
    
    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        updateLastAccess();
        return realConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }
    
    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        updateLastAccess();
        return realConnection.prepareCall(sql, resultSetType, resultSetConcurrency);
    }
    
    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        updateLastAccess();
        return realConnection.getTypeMap();
    }
    
    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        updateLastAccess();
        realConnection.setTypeMap(map);
    }
    
    @Override
    public void setHoldability(int holdability) throws SQLException {
        updateLastAccess();
        realConnection.setHoldability(holdability);
    }
    
    @Override
    public int getHoldability() throws SQLException {
        updateLastAccess();
        return realConnection.getHoldability();
    }
    
    @Override
    public Savepoint setSavepoint() throws SQLException {
        updateLastAccess();
        return realConnection.setSavepoint();
    }
    
    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        updateLastAccess();
        return realConnection.setSavepoint(name);
    }
    
    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        updateLastAccess();
        realConnection.rollback(savepoint);
    }
    
    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        updateLastAccess();
        realConnection.releaseSavepoint(savepoint);
    }
    
    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        updateLastAccess();
        return realConnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        updateLastAccess();
        return realConnection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        updateLastAccess();
        return realConnection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        updateLastAccess();
        return realConnection.prepareStatement(sql, autoGeneratedKeys);
    }
    
    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        updateLastAccess();
        return realConnection.prepareStatement(sql, columnIndexes);
    }
    
    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        updateLastAccess();
        return realConnection.prepareStatement(sql, columnNames);
    }
    
    @Override
    public Clob createClob() throws SQLException {
        updateLastAccess();
        return realConnection.createClob();
    }
    
    @Override
    public Blob createBlob() throws SQLException {
        updateLastAccess();
        return realConnection.createBlob();
    }
    
    @Override
    public NClob createNClob() throws SQLException {
        updateLastAccess();
        return realConnection.createNClob();
    }
    
    @Override
    public SQLXML createSQLXML() throws SQLException {
        updateLastAccess();
        return realConnection.createSQLXML();
    }
    
    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        updateLastAccess();
        realConnection.setClientInfo(name, value);
    }
    
    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        updateLastAccess();
        realConnection.setClientInfo(properties);
    }
    
    @Override
    public String getClientInfo(String name) throws SQLException {
        updateLastAccess();
        return realConnection.getClientInfo(name);
    }
    
    @Override
    public Properties getClientInfo() throws SQLException {
        updateLastAccess();
        return realConnection.getClientInfo();
    }
    
    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        updateLastAccess();
        return realConnection.createArrayOf(typeName, elements);
    }
    
    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        updateLastAccess();
        return realConnection.createStruct(typeName, attributes);
    }
    
    @Override
    public void setSchema(String schema) throws SQLException {
        updateLastAccess();
        realConnection.setSchema(schema);
    }
    
    @Override
    public String getSchema() throws SQLException {
        updateLastAccess();
        return realConnection.getSchema();
    }
    
    @Override
    public void abort(Executor executor) throws SQLException {
        updateLastAccess();
        realConnection.abort(executor);
    }
    
    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        updateLastAccess();
        realConnection.setNetworkTimeout(executor, milliseconds);
    }
    
    @Override
    public int getNetworkTimeout() throws SQLException {
        updateLastAccess();
        return realConnection.getNetworkTimeout();
    }
    
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        updateLastAccess();
        return realConnection.unwrap(iface);
    }
    
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        updateLastAccess();
        return realConnection.isWrapperFor(iface);
    }
    
    private void updateLastAccess() {
        this.lastAccessTime = System.currentTimeMillis();
    }
    
    @Override
    public String toString() {
        return String.format("PooledConnection{id=%d, acquired=%s, age=%dms, idle=%dms}",
                connectionId, acquired, getAge(), getIdleTime());
    }
}