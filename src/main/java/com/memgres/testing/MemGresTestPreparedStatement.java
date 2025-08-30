package com.memgres.testing;

import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionResult;
import com.memgres.storage.Table;
import com.memgres.types.Column;
import com.memgres.types.DataType;
import com.memgres.types.Row;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic PreparedStatement implementation for MemGres testing.
 * 
 * <p>This is a simplified PreparedStatement implementation that provides
 * basic parameter binding and SQL execution. It supports common data types
 * and operations needed for testing scenarios.</p>
 * 
 * <p><strong>Supported parameter types:</strong></p>
 * <ul>
 *   <li>String, int, long, double, boolean</li>
 *   <li>BigDecimal, Date, Time, Timestamp</li>
 *   <li>null values</li>
 * </ul>
 * 
 * @since 1.0.0
 */
public class MemGresTestPreparedStatement implements PreparedStatement {
    
    private final MemGresTestConnection connection;
    private final SqlExecutionEngine sqlEngine;
    private final String sql;
    private final Map<Integer, Object> parameters = new HashMap<>();
    private final List<Map<Integer, Object>> batchParameters = new ArrayList<>();
    private boolean closed = false;
    private List<Object> lastGeneratedKeys = new ArrayList<>();
    
    /**
     * Creates a new MemGresTestPreparedStatement.
     * 
     * @param connection the connection
     * @param sqlEngine the SQL execution engine
     * @param sql the SQL statement with parameter placeholders
     */
    public MemGresTestPreparedStatement(MemGresTestConnection connection, SqlExecutionEngine sqlEngine, String sql) {
        this.connection = connection;
        this.sqlEngine = sqlEngine;
        this.sql = sql;
    }
    
    @Override
    public ResultSet executeQuery() throws SQLException {
        checkClosed();
        String processedSql = replaceParameters();
        connection.ensureTransaction();
        
        try {
            SqlExecutionResult result = sqlEngine.execute(processedSql);
            if (!result.isSuccess()) {
                throw new SQLException("Query execution failed: " + result.getMessage());
            }
            return new MemGresTestResultSet(result.getRows(), result.getColumns());
        } catch (com.memgres.sql.execution.SqlExecutionException e) {
            throw new SQLException("SQL execution failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public int executeUpdate() throws SQLException {
        checkClosed();
        String processedSql = replaceParameters();
        connection.ensureTransaction();
        
        try {
            SqlExecutionResult result = sqlEngine.execute(processedSql);
            if (!result.isSuccess()) {
                throw new SQLException("Update execution failed: " + result.getMessage());
            }
            
            // Take snapshot after successful DML operations if in transaction
            connection.ensureTransactionSnapshotAfterDML(processedSql);
            
            // Capture generated keys for INSERT operations
            captureGeneratedKeys(processedSql, result);
            
            return result.getAffectedRows();
        } catch (com.memgres.sql.execution.SqlExecutionException e) {
            throw new SQLException("SQL execution failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean execute() throws SQLException {
        checkClosed();
        String processedSql = replaceParameters();
        connection.ensureTransaction();
        
        try {
            SqlExecutionResult result = sqlEngine.execute(processedSql);
            if (!result.isSuccess()) {
                throw new SQLException("SQL execution failed: " + result.getMessage());
            }
            
            // Take snapshot after successful DML operations if in transaction
            connection.ensureTransactionSnapshotAfterDML(processedSql);
            
            // Capture generated keys for INSERT operations
            captureGeneratedKeys(processedSql, result);
            
            return result.getType() == SqlExecutionResult.ResultType.SELECT;
        } catch (com.memgres.sql.execution.SqlExecutionException e) {
            throw new SQLException("SQL execution failed: " + e.getMessage(), e);
        }
    }
    
    // Parameter setters
    
    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        checkClosed();
        // ConcurrentHashMap doesn't allow null values, so we use a special marker
        parameters.put(parameterIndex, "NULL_VALUE_MARKER");
    }
    
    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        checkClosed();
        parameters.put(parameterIndex, x);
    }
    
    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        checkClosed();
        parameters.put(parameterIndex, x);
    }
    
    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        checkClosed();
        parameters.put(parameterIndex, x);
    }
    
    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        checkClosed();
        parameters.put(parameterIndex, x);
    }
    
    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        checkClosed();
        parameters.put(parameterIndex, x);
    }
    
    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        checkClosed();
        parameters.put(parameterIndex, x);
    }
    
    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        checkClosed();
        parameters.put(parameterIndex, x);
    }
    
    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        checkClosed();
        parameters.put(parameterIndex, x);
    }
    
    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        checkClosed();
        parameters.put(parameterIndex, x);
    }
    
    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        checkClosed();
        parameters.put(parameterIndex, x);
    }
    
    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        checkClosed();
        parameters.put(parameterIndex, x);
    }
    
    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        checkClosed();
        parameters.put(parameterIndex, x);
    }
    
    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        checkClosed();
        parameters.put(parameterIndex, x);
    }
    
    @Override
    public void clearParameters() throws SQLException {
        checkClosed();
        parameters.clear();
    }
    
    // ResultSet methods (delegate to Statement)
    
    @Override
    public ResultSet getResultSet() throws SQLException {
        // This should be called after execute() returns true
        return null;
    }
    
    @Override
    public int getUpdateCount() throws SQLException {
        return -1;
    }
    
    @Override
    public boolean getMoreResults() throws SQLException {
        return false;
    }
    
    @Override
    public void close() throws SQLException {
        closed = true;
        parameters.clear();
    }
    
    @Override
    public boolean isClosed() throws SQLException {
        return closed;
    }
    
    // Unsupported methods
    
    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("ASCII streams not supported");
    }
    
    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Unicode streams not supported");
    }
    
    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Binary streams not supported");
    }
    
    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        checkClosed();
        parameters.put(parameterIndex, x);
    }
    
    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        checkClosed();
        parameters.put(parameterIndex, x);
    }
    
    @Override
    public void addBatch() throws SQLException {
        checkClosed();
        // Add current parameter set to batch
        batchParameters.add(new HashMap<>(parameters));
        // Clear current parameters for next batch entry
        parameters.clear();
    }
    
    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Character streams not supported");
    }
    
    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Ref not supported");
    }
    
    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Blob not supported");
    }
    
    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Clob not supported");
    }
    
    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Array not supported");
    }
    
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        throw new SQLFeatureNotSupportedException("PreparedStatement metadata not supported");
    }
    
    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        setDate(parameterIndex, x);
    }
    
    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        setTime(parameterIndex, x);
    }
    
    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        setTimestamp(parameterIndex, x);
    }
    
    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        setNull(parameterIndex, sqlType);
    }
    
    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        throw new SQLFeatureNotSupportedException("URL parameters not supported");
    }
    
    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        throw new SQLFeatureNotSupportedException("Parameter metadata not supported");
    }
    
    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        throw new SQLFeatureNotSupportedException("RowId not supported");
    }
    
    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        setString(parameterIndex, value);
    }
    
    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("NCharacter streams not supported");
    }
    
    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        throw new SQLFeatureNotSupportedException("NClob not supported");
    }
    
    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Clob not supported");
    }
    
    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Blob not supported");
    }
    
    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("NClob not supported");
    }
    
    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        throw new SQLFeatureNotSupportedException("SQLXML not supported");
    }
    
    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        setObject(parameterIndex, x, targetSqlType);
    }
    
    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("ASCII streams not supported");
    }
    
    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Binary streams not supported");
    }
    
    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Character streams not supported");
    }
    
    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException("ASCII streams not supported");
    }
    
    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Binary streams not supported");
    }
    
    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("Character streams not supported");
    }
    
    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        throw new SQLFeatureNotSupportedException("NCharacter streams not supported");
    }
    
    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("Clob not supported");
    }
    
    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        throw new SQLFeatureNotSupportedException("Blob not supported");
    }
    
    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("NClob not supported");
    }
    
    // Statement interface delegation
    
    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        throw new SQLException("Use executeQuery() without parameters for PreparedStatement");
    }
    
    @Override
    public int executeUpdate(String sql) throws SQLException {
        throw new SQLException("Use executeUpdate() without parameters for PreparedStatement");
    }
    
    @Override
    public boolean execute(String sql) throws SQLException {
        throw new SQLException("Use execute() without parameters for PreparedStatement");
    }
    
    @Override
    public int getMaxFieldSize() throws SQLException {
        return 0;
    }
    
    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        // No-op
    }
    
    @Override
    public int getMaxRows() throws SQLException {
        return 0;
    }
    
    @Override
    public void setMaxRows(int max) throws SQLException {
        // No-op
    }
    
    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        // No-op
    }
    
    @Override
    public int getQueryTimeout() throws SQLException {
        return 0;
    }
    
    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        // No-op
    }
    
    @Override
    public void cancel() throws SQLException {
        // No-op
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
        throw new SQLFeatureNotSupportedException("Cursor names not supported");
    }
    
    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return false;
    }
    
    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        checkClosed();
        
        // Create a ResultSet with the generated keys
        if (lastGeneratedKeys.isEmpty()) {
            // Return empty result set
            return new MemGresTestResultSet(new ArrayList<>(), 
                List.of(new Column.Builder()
                    .name("GENERATED_KEY")
                    .dataType(DataType.BIGINT)
                    .nullable(false)
                    .build()));
        }
        
        // Convert generated keys to rows
        List<Row> rows = new ArrayList<>();
        long rowId = 1;
        for (Object key : lastGeneratedKeys) {
            rows.add(new Row(rowId++, new Object[]{key}));
        }
        
        return new MemGresTestResultSet(rows, 
            List.of(new Column.Builder()
                .name("GENERATED_KEY")
                .dataType(DataType.BIGINT)
                .nullable(false)
                .build()));
    }
    
    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        throw new SQLException("Use executeUpdate() without parameters for PreparedStatement");
    }
    
    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLException("Use executeUpdate() without parameters for PreparedStatement");
    }
    
    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        throw new SQLException("Use executeUpdate() without parameters for PreparedStatement");
    }
    
    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        throw new SQLException("Use execute() without parameters for PreparedStatement");
    }
    
    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLException("Use execute() without parameters for PreparedStatement");
    }
    
    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        throw new SQLException("Use execute() without parameters for PreparedStatement");
    }
    
    @Override
    public int getResultSetHoldability() throws SQLException {
        return ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }
    
    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        // No-op
    }
    
    @Override
    public boolean isPoolable() throws SQLException {
        return false;
    }
    
    @Override
    public void closeOnCompletion() throws SQLException {
        // No-op
    }
    
    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return false;
    }
    
    // Connection methods
    
    @Override
    public Connection getConnection() throws SQLException {
        return connection;
    }
    
    @Override
    public int getFetchDirection() throws SQLException {
        return ResultSet.FETCH_FORWARD;
    }
    
    @Override
    public void setFetchDirection(int direction) throws SQLException {
        // No-op
    }
    
    @Override
    public int getFetchSize() throws SQLException {
        return 0;
    }
    
    @Override
    public void setFetchSize(int rows) throws SQLException {
        // No-op
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
        throw new SQLFeatureNotSupportedException("Batch operations not supported");
    }
    
    @Override
    public void clearBatch() throws SQLException {
        checkClosed();
        batchParameters.clear();
    }
    
    @Override
    public int[] executeBatch() throws SQLException {
        checkClosed();
        
        if (batchParameters.isEmpty()) {
            return new int[0];
        }
        
        List<Integer> results = new ArrayList<>();
        List<Map<Integer, Object>> currentBatch = new ArrayList<>(batchParameters);
        
        try {
            // Execute the prepared statement for each parameter set
            for (Map<Integer, Object> parameterSet : currentBatch) {
                try {
                    // Temporarily set parameters for this batch entry
                    Map<Integer, Object> originalParameters = new HashMap<>(parameters);
                    parameters.clear();
                    parameters.putAll(parameterSet);
                    
                    // Replace placeholders and execute
                    String executableSql = replaceParameters();
                    SqlExecutionResult result = sqlEngine.execute(executableSql);
                    
                    // Restore original parameters
                    parameters.clear();
                    parameters.putAll(originalParameters);
                    
                    // Return affected rows for DML operations, SUCCESS_NO_INFO for others
                    if (result.getType() == SqlExecutionResult.ResultType.INSERT ||
                        result.getType() == SqlExecutionResult.ResultType.UPDATE ||
                        result.getType() == SqlExecutionResult.ResultType.DELETE) {
                        results.add(result.getAffectedRows());
                    } else {
                        results.add(SUCCESS_NO_INFO);
                    }
                } catch (Exception e) {
                    // For batch execution failures, we continue but mark as failed
                    results.add(EXECUTE_FAILED);
                }
            }
            
            // Clear the batch after successful execution
            batchParameters.clear();
            
            // Convert results to int array
            return results.stream().mapToInt(Integer::intValue).toArray();
            
        } catch (Exception e) {
            throw new BatchUpdateException("Batch execution failed", results.stream().mapToInt(Integer::intValue).toArray(), e);
        }
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
    
    /**
     * Replaces parameter placeholders (?) in the SQL with actual parameter values.
     * 
     * @return the processed SQL string
     */
    private String replaceParameters() {
        String result = sql;
        
        // Replace parameters in positional order (1-based JDBC indexing)
        for (int i = 1; i <= parameters.size(); i++) {
            if (parameters.containsKey(i)) {
                String value = formatParameterValue(parameters.get(i));
                result = result.replaceFirst("\\?", value);
            }
        }
        
        return result;
    }
    
    /**
     * Formats a parameter value for SQL insertion.
     * 
     * @param value the parameter value
     * @return the formatted value string
     */
    private String formatParameterValue(Object value) {
        if (value == null) {
            return "NULL";
        } else if (value instanceof String) {
            return "'" + value.toString().replace("'", "''") + "'";
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? "TRUE" : "FALSE";
        } else {
            return value.toString();
        }
    }
    
    private void checkClosed() throws SQLException {
        if (closed) {
            throw new SQLException("PreparedStatement is closed");
        }
    }
    
    /**
     * Capture generated keys from INSERT operations.
     */
    private void captureGeneratedKeys(String sql, SqlExecutionResult result) {
        lastGeneratedKeys.clear();
        
        // Only capture keys for INSERT operations
        if (result.getType() != SqlExecutionResult.ResultType.INSERT) {
            return;
        }
        
        // Parse table name from SQL (simple approach)
        String upperSql = sql.trim().toUpperCase();
        if (!upperSql.startsWith("INSERT")) {
            return;
        }
        
        // Extract table name from INSERT INTO table_name ...
        String[] parts = sql.trim().split("\\s+");
        if (parts.length < 3) {
            return;
        }
        
        String tableName = parts[2]; // INSERT INTO table_name
        // Remove parentheses if present
        int parenIndex = tableName.indexOf('(');
        if (parenIndex > 0) {
            tableName = tableName.substring(0, parenIndex);
        }
        
        // Get the table and retrieve generated keys
        try {
            Table table = connection.getEngine().getTable("public", tableName);
            if (table != null) {
                lastGeneratedKeys.addAll(table.getLastGeneratedKeys());
            }
        } catch (Exception e) {
            // Ignore errors in key retrieval
        }
    }
}