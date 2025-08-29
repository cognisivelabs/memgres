package com.memgres.testing;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionResult;
import com.memgres.storage.Table;
import com.memgres.transaction.Transaction;
import com.memgres.transaction.TransactionIsolationLevel;
import com.memgres.types.Row;

import java.sql.*;
import java.util.*;
import java.util.concurrent.Executor;

/**
 * JDBC Connection implementation for MemGres testing.
 * 
 * <p>This is a simplified Connection implementation designed for testing scenarios.
 * It provides basic JDBC connectivity to MemGres databases but does not implement
 * all JDBC features. This is sufficient for most testing frameworks and ORM integration.</p>
 * 
 * <p><strong>Supported features:</strong></p>
 * <ul>
 *   <li>SQL statement execution via createStatement()</li>
 *   <li>Transaction management (commit, rollback)</li>
 *   <li>Auto-commit mode</li>
 *   <li>Connection metadata</li>
 * </ul>
 * 
 * <p><strong>Not supported:</strong></p>
 * <ul>
 *   <li>Prepared statements (throws SQLFeatureNotSupportedException)</li>
 *   <li>Callable statements</li>
 *   <li>Savepoints</li>
 *   <li>Advanced JDBC features</li>
 * </ul>
 * 
 * @since 1.0.0
 */
public class MemGresTestConnection implements Connection {
    
    private final MemGresEngine engine;
    private final SqlExecutionEngine sqlEngine;
    private boolean closed = false;
    private boolean autoCommit = true;
    private Transaction currentTransaction;
    
    // Transaction rollback support
    private Map<String, Map<Long, Object[]>> transactionSnapshot;
    
    /**
     * Creates a new MemGresTestConnection.
     * 
     * @param engine the MemGres engine
     * @param sqlEngine the SQL execution engine
     */
    public MemGresTestConnection(MemGresEngine engine, SqlExecutionEngine sqlEngine) {
        this.engine = engine;
        this.sqlEngine = sqlEngine;
        this.transactionSnapshot = new HashMap<>();
    }
    
    @Override
    public Statement createStatement() throws SQLException {
        checkClosed();
        return new MemGresTestStatement(this, sqlEngine);
    }
    
    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        checkClosed();
        return new MemGresTestPreparedStatement(this, sqlEngine, sql);
    }
    
    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        throw new SQLFeatureNotSupportedException("Callable statements not supported");
    }
    
    @Override
    public String nativeSQL(String sql) throws SQLException {
        checkClosed();
        return sql; // MemGres uses PostgreSQL-compatible SQL
    }
    
    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        checkClosed();
        this.autoCommit = autoCommit;
        
        if (autoCommit && currentTransaction != null) {
            // Auto-commit enabled, commit current transaction
            engine.getTransactionManager().commitTransaction(currentTransaction);
            currentTransaction = null;
            transactionSnapshot.clear();
        } else if (!autoCommit && currentTransaction == null) {
            // Begin transaction - snapshot will be taken lazily when needed
            currentTransaction = engine.getTransactionManager().beginTransaction();
        }
    }
    
    @Override
    public boolean getAutoCommit() throws SQLException {
        checkClosed();
        return autoCommit;
    }
    
    @Override
    public void commit() throws SQLException {
        checkClosed();
        if (autoCommit) {
            throw new SQLException("Cannot commit when auto-commit is enabled");
        }
        
        if (currentTransaction != null) {
            engine.getTransactionManager().commitTransaction(currentTransaction);
            currentTransaction = null;
            transactionSnapshot.clear();
        }
    }
    
    @Override
    public void rollback() throws SQLException {
        checkClosed();
        if (autoCommit) {
            throw new SQLException("Cannot rollback when auto-commit is enabled");
        }
        
        if (currentTransaction != null) {
            // Restore data from snapshot before rolling back transaction
            restoreFromSnapshot();
            engine.getTransactionManager().rollbackTransaction(currentTransaction);
            currentTransaction = null;
            transactionSnapshot.clear();
        }
    }
    
    @Override
    public void close() throws SQLException {
        if (!closed) {
            if (currentTransaction != null) {
                restoreFromSnapshot();
                engine.getTransactionManager().rollbackTransaction(currentTransaction);
                currentTransaction = null;
                transactionSnapshot.clear();
            }
            closed = true;
        }
    }
    
    @Override
    public boolean isClosed() throws SQLException {
        return closed;
    }
    
    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        checkClosed();
        return new MemGresTestDatabaseMetaData(this);
    }
    
    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        checkClosed();
        // MemGres supports read-only mode conceptually but doesn't enforce it
    }
    
    @Override
    public boolean isReadOnly() throws SQLException {
        checkClosed();
        return false; // MemGres is always read-write
    }
    
    @Override
    public void setCatalog(String catalog) throws SQLException {
        checkClosed();
        // MemGres doesn't support catalogs, ignore
    }
    
    @Override
    public String getCatalog() throws SQLException {
        checkClosed();
        return null; // MemGres doesn't support catalogs
    }
    
    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        checkClosed();
        // MemGres supports transaction isolation but through its own API
    }
    
    @Override
    public int getTransactionIsolation() throws SQLException {
        checkClosed();
        return Connection.TRANSACTION_READ_COMMITTED; // Default level
    }
    
    @Override
    public SQLWarning getWarnings() throws SQLException {
        checkClosed();
        return null; // No warnings in MemGres
    }
    
    @Override
    public void clearWarnings() throws SQLException {
        checkClosed();
        // No-op
    }
    
    // Unsupported JDBC 2.0+ methods
    
    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        throw new SQLFeatureNotSupportedException("Advanced statement creation not supported");
    }
    
    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        throw new SQLFeatureNotSupportedException("Prepared statements not supported");
    }
    
    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        throw new SQLFeatureNotSupportedException("Callable statements not supported");
    }
    
    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        throw new SQLFeatureNotSupportedException("Type map not supported");
    }
    
    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException("Type map not supported");
    }
    
    // JDBC 3.0 methods
    
    @Override
    public void setHoldability(int holdability) throws SQLException {
        throw new SQLFeatureNotSupportedException("Holdability not supported");
    }
    
    @Override
    public int getHoldability() throws SQLException {
        return ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }
    
    @Override
    public Savepoint setSavepoint() throws SQLException {
        throw new SQLFeatureNotSupportedException("Savepoints not supported");
    }
    
    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        throw new SQLFeatureNotSupportedException("Savepoints not supported");
    }
    
    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        throw new SQLFeatureNotSupportedException("Savepoints not supported");
    }
    
    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        throw new SQLFeatureNotSupportedException("Savepoints not supported");
    }
    
    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw new SQLFeatureNotSupportedException("Advanced statement creation not supported");
    }
    
    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw new SQLFeatureNotSupportedException("Prepared statements not supported");
    }
    
    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw new SQLFeatureNotSupportedException("Callable statements not supported");
    }
    
    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        throw new SQLFeatureNotSupportedException("Prepared statements not supported");
    }
    
    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLFeatureNotSupportedException("Prepared statements not supported");
    }
    
    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        throw new SQLFeatureNotSupportedException("Prepared statements not supported");
    }
    
    // JDBC 4.0+ methods
    
    @Override
    public Clob createClob() throws SQLException {
        throw new SQLFeatureNotSupportedException("CLOB not supported");
    }
    
    @Override
    public Blob createBlob() throws SQLException {
        throw new SQLFeatureNotSupportedException("BLOB not supported");
    }
    
    @Override
    public NClob createNClob() throws SQLException {
        throw new SQLFeatureNotSupportedException("NCLOB not supported");
    }
    
    @Override
    public SQLXML createSQLXML() throws SQLException {
        throw new SQLFeatureNotSupportedException("SQLXML not supported");
    }
    
    @Override
    public boolean isValid(int timeout) throws SQLException {
        return !closed;
    }
    
    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        // No-op
    }
    
    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        // No-op
    }
    
    @Override
    public String getClientInfo(String name) throws SQLException {
        return null;
    }
    
    @Override
    public Properties getClientInfo() throws SQLException {
        return new Properties();
    }
    
    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        throw new SQLFeatureNotSupportedException("Array creation not supported");
    }
    
    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        throw new SQLFeatureNotSupportedException("Struct creation not supported");
    }
    
    // JDBC 4.1 methods
    
    @Override
    public void setSchema(String schema) throws SQLException {
        checkClosed();
        // MemGres supports schemas through its own API
    }
    
    @Override
    public String getSchema() throws SQLException {
        checkClosed();
        return "test"; // Default schema
    }
    
    @Override
    public void abort(Executor executor) throws SQLException {
        close();
    }
    
    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        throw new SQLFeatureNotSupportedException("Network timeout not supported");
    }
    
    @Override
    public int getNetworkTimeout() throws SQLException {
        return 0;
    }
    
    // Wrapper interface
    
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isAssignableFrom(MemGresEngine.class)) {
            return iface.cast(engine);
        } else if (iface.isAssignableFrom(SqlExecutionEngine.class)) {
            return iface.cast(sqlEngine);
        } else if (iface.isAssignableFrom(getClass())) {
            return iface.cast(this);
        }
        throw new SQLException("Cannot unwrap to " + iface.getName());
    }
    
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(MemGresEngine.class) ||
               iface.isAssignableFrom(SqlExecutionEngine.class) ||
               iface.isAssignableFrom(getClass());
    }
    
    /**
     * Ensures the current transaction exists if not in auto-commit mode.
     */
    public void ensureTransaction() throws SQLException {
        if (!autoCommit && currentTransaction == null) {
            currentTransaction = engine.getTransactionManager()
                    .beginTransaction(TransactionIsolationLevel.READ_COMMITTED);
        }
    }
    
    /**
     * Ensures a transaction snapshot is taken if we're in a transaction and haven't taken one yet.
     * This is called AFTER the first successful data modification operation (INSERT/UPDATE/DELETE).
     */
    public void ensureTransactionSnapshotAfterDML(String sql) throws SQLException {
        if (currentTransaction != null && transactionSnapshot.isEmpty()) {
            // Only take snapshot after successful DML operations, not DDL
            String upperSql = sql.trim().toUpperCase();
            if (upperSql.startsWith("INSERT") || upperSql.startsWith("UPDATE") || upperSql.startsWith("DELETE")) {
                takeTransactionSnapshot();
            }
        }
    }
    
    /**
     * Gets the current transaction.
     * 
     * @return the current transaction, or null if in auto-commit mode
     */
    public Transaction getCurrentTransaction() {
        return currentTransaction;
    }
    
    /**
     * Gets the MemGres engine.
     * 
     * @return the MemGres engine
     */
    public MemGresEngine getEngine() {
        return engine;
    }
    
    private void checkClosed() throws SQLException {
        if (closed) {
            throw new SQLException("Connection is closed");
        }
    }
    
    /**
     * Takes a snapshot of all tables in the current schema for transaction rollback.
     */
    private void takeTransactionSnapshot() throws SQLException {
        try {
            transactionSnapshot.clear();
            
            // Get the public schema (default schema used in tests)
            com.memgres.storage.Schema schema = engine.getSchema("public");
            if (schema != null) {
                // Snapshot all tables in the schema
                for (String tableName : schema.getTableNames()) {
                    Table table = schema.getTable(tableName);
                    if (table != null) {
                        Map<Long, Object[]> tableSnapshot = new HashMap<>();
                        
                        // Copy all row data
                        for (Row row : table.getAllRows()) {
                            Object[] dataCopy = new Object[row.getData().length];
                            System.arraycopy(row.getData(), 0, dataCopy, 0, row.getData().length);
                            tableSnapshot.put(row.getId(), dataCopy);
                        }
                        
                        transactionSnapshot.put(tableName, tableSnapshot);
                    }
                }
            }
        } catch (Exception e) {
            throw new SQLException("Failed to take transaction snapshot", e);
        }
    }
    
    /**
     * Restores all tables from the transaction snapshot.
     */
    private void restoreFromSnapshot() throws SQLException {
        try {
            // Get the public schema
            com.memgres.storage.Schema schema = engine.getSchema("public");
            if (schema != null && !transactionSnapshot.isEmpty()) {
                
                for (Map.Entry<String, Map<Long, Object[]>> tableEntry : transactionSnapshot.entrySet()) {
                    String tableName = tableEntry.getKey();
                    Map<Long, Object[]> tableSnapshot = tableEntry.getValue();
                    
                    Table table = schema.getTable(tableName);
                    if (table != null) {
                        // Clear current table data
                        List<Row> currentRows = new ArrayList<>(table.getAllRows());
                        for (Row row : currentRows) {
                            table.deleteRow(row.getId());
                        }
                        
                        // Restore from snapshot
                        for (Map.Entry<Long, Object[]> rowEntry : tableSnapshot.entrySet()) {
                            Long rowId = rowEntry.getKey();
                            Object[] rowData = rowEntry.getValue();
                            
                            // Create copy of data to avoid reference issues
                            Object[] dataCopy = new Object[rowData.length];
                            System.arraycopy(rowData, 0, dataCopy, 0, rowData.length);
                            
                            // Insert the row back with original ID
                            table.insertRowWithId(rowId, dataCopy);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new SQLException("Failed to restore from transaction snapshot", e);
        }
    }
}