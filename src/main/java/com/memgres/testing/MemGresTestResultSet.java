package com.memgres.testing;

import com.memgres.types.Column;
import com.memgres.types.DataType;
import com.memgres.types.Row;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * JDBC ResultSet implementation for MemGres testing.
 * 
 * <p>This is a simplified ResultSet implementation that provides basic result
 * navigation and data access for testing scenarios.</p>
 * 
 * @since 1.0.0
 */
public class MemGresTestResultSet implements ResultSet {
    
    private final List<Row> rows;
    private final List<Column> columns;
    private int currentRowIndex = -1;
    private boolean closed = false;
    private boolean wasNull = false;
    
    /**
     * Creates a new MemGresTestResultSet.
     * 
     * @param rows the result rows
     * @param columns the result columns
     */
    public MemGresTestResultSet(List<Row> rows, List<Column> columns) {
        this.rows = rows != null ? rows : List.of();
        this.columns = columns != null ? columns : List.of();
    }
    
    @Override
    public boolean next() throws SQLException {
        checkClosed();
        currentRowIndex++;
        return currentRowIndex < rows.size();
    }
    
    @Override
    public void close() throws SQLException {
        closed = true;
    }
    
    @Override
    public boolean wasNull() throws SQLException {
        return wasNull;
    }
    
    // String getters
    
    @Override
    public String getString(int columnIndex) throws SQLException {
        Object value = getValue(columnIndex);
        if (value == null) {
            wasNull = true;
            return null;
        }
        wasNull = false;
        return value.toString();
    }
    
    @Override
    public String getString(String columnLabel) throws SQLException {
        return getString(findColumn(columnLabel));
    }
    
    // Boolean getters
    
    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        Object value = getValue(columnIndex);
        if (value == null) {
            wasNull = true;
            return false;
        }
        wasNull = false;
        
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }
    
    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        return getBoolean(findColumn(columnLabel));
    }
    
    // Byte getters
    
    @Override
    public byte getByte(int columnIndex) throws SQLException {
        Object value = getValue(columnIndex);
        if (value == null) {
            wasNull = true;
            return 0;
        }
        wasNull = false;
        
        if (value instanceof Number) {
            return ((Number) value).byteValue();
        }
        return Byte.parseByte(value.toString());
    }
    
    @Override
    public byte getByte(String columnLabel) throws SQLException {
        return getByte(findColumn(columnLabel));
    }
    
    // Short getters
    
    @Override
    public short getShort(int columnIndex) throws SQLException {
        Object value = getValue(columnIndex);
        if (value == null) {
            wasNull = true;
            return 0;
        }
        wasNull = false;
        
        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }
        return Short.parseShort(value.toString());
    }
    
    @Override
    public short getShort(String columnLabel) throws SQLException {
        return getShort(findColumn(columnLabel));
    }
    
    // Int getters
    
    @Override
    public int getInt(int columnIndex) throws SQLException {
        Object value = getValue(columnIndex);
        if (value == null) {
            wasNull = true;
            return 0;
        }
        wasNull = false;
        
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }
    
    @Override
    public int getInt(String columnLabel) throws SQLException {
        return getInt(findColumn(columnLabel));
    }
    
    // Long getters
    
    @Override
    public long getLong(int columnIndex) throws SQLException {
        Object value = getValue(columnIndex);
        if (value == null) {
            wasNull = true;
            return 0L;
        }
        wasNull = false;
        
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString());
    }
    
    @Override
    public long getLong(String columnLabel) throws SQLException {
        return getLong(findColumn(columnLabel));
    }
    
    // Float getters
    
    @Override
    public float getFloat(int columnIndex) throws SQLException {
        Object value = getValue(columnIndex);
        if (value == null) {
            wasNull = true;
            return 0.0f;
        }
        wasNull = false;
        
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return Float.parseFloat(value.toString());
    }
    
    @Override
    public float getFloat(String columnLabel) throws SQLException {
        return getFloat(findColumn(columnLabel));
    }
    
    // Double getters
    
    @Override
    public double getDouble(int columnIndex) throws SQLException {
        Object value = getValue(columnIndex);
        if (value == null) {
            wasNull = true;
            return 0.0;
        }
        wasNull = false;
        
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.parseDouble(value.toString());
    }
    
    @Override
    public double getDouble(String columnLabel) throws SQLException {
        return getDouble(findColumn(columnLabel));
    }
    
    // Object getter
    
    @Override
    public Object getObject(int columnIndex) throws SQLException {
        Object value = getValue(columnIndex);
        wasNull = (value == null);
        return value;
    }
    
    @Override
    public Object getObject(String columnLabel) throws SQLException {
        return getObject(findColumn(columnLabel));
    }
    
    // ResultSetMetaData
    
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return new MemGresTestResultSetMetaData(columns);
    }
    
    // Column access
    
    @Override
    public int findColumn(String columnLabel) throws SQLException {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).getName().equalsIgnoreCase(columnLabel)) {
                return i + 1; // JDBC uses 1-based indexing
            }
        }
        throw new SQLException("Column not found: " + columnLabel);
    }
    
    // Navigation
    
    @Override
    public boolean isBeforeFirst() throws SQLException {
        return currentRowIndex == -1;
    }
    
    @Override
    public boolean isAfterLast() throws SQLException {
        return currentRowIndex >= rows.size();
    }
    
    @Override
    public boolean isFirst() throws SQLException {
        return currentRowIndex == 0;
    }
    
    @Override
    public boolean isLast() throws SQLException {
        return currentRowIndex == rows.size() - 1;
    }
    
    @Override
    public void beforeFirst() throws SQLException {
        currentRowIndex = -1;
    }
    
    @Override
    public void afterLast() throws SQLException {
        currentRowIndex = rows.size();
    }
    
    @Override
    public boolean first() throws SQLException {
        if (rows.isEmpty()) {
            return false;
        }
        currentRowIndex = 0;
        return true;
    }
    
    @Override
    public boolean last() throws SQLException {
        if (rows.isEmpty()) {
            return false;
        }
        currentRowIndex = rows.size() - 1;
        return true;
    }
    
    @Override
    public int getRow() throws SQLException {
        if (currentRowIndex < 0 || currentRowIndex >= rows.size()) {
            return 0;
        }
        return currentRowIndex + 1; // JDBC uses 1-based indexing
    }
    
    @Override
    public boolean absolute(int row) throws SQLException {
        if (row == 0) {
            beforeFirst();
            return false;
        } else if (row > 0) {
            currentRowIndex = row - 1; // Convert to 0-based
        } else {
            currentRowIndex = rows.size() + row; // Negative indexing from end
        }
        
        return currentRowIndex >= 0 && currentRowIndex < rows.size();
    }
    
    @Override
    public boolean relative(int rows) throws SQLException {
        return absolute(getRow() + rows);
    }
    
    @Override
    public boolean previous() throws SQLException {
        currentRowIndex--;
        return currentRowIndex >= 0;
    }
    
    @Override
    public boolean isClosed() throws SQLException {
        return closed;
    }
    
    // Helper methods
    
    private Object getValue(int columnIndex) throws SQLException {
        checkClosed();
        
        if (columnIndex < 1 || columnIndex > columns.size()) {
            throw new SQLException("Column index out of range: " + columnIndex);
        }
        
        if (currentRowIndex < 0 || currentRowIndex >= rows.size()) {
            throw new SQLException("No current row");
        }
        
        Row currentRow = rows.get(currentRowIndex);
        return currentRow.getValue(columnIndex - 1); // Convert to 0-based
    }
    
    private void checkClosed() throws SQLException {
        if (closed) {
            throw new SQLException("ResultSet is closed");
        }
    }
    
    // Unsupported methods (throwing SQLFeatureNotSupportedException)
    
    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        throw new SQLFeatureNotSupportedException("getBigDecimal with scale not supported");
    }
    
    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        throw new SQLFeatureNotSupportedException("getBigDecimal with scale not supported");
    }
    
    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("Binary data not supported");
    }
    
    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("Binary data not supported");
    }
    
    @Override
    public Date getDate(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("Date not yet supported");
    }
    
    @Override
    public Date getDate(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("Date not yet supported");
    }
    
    @Override
    public Time getTime(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("Time not yet supported");
    }
    
    @Override
    public Time getTime(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("Time not yet supported");
    }
    
    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("Timestamp not yet supported");
    }
    
    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("Timestamp not yet supported");
    }
    
    // All other unsupported methods
    @Override public InputStream getAsciiStream(int columnIndex) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public InputStream getUnicodeStream(int columnIndex) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public InputStream getBinaryStream(int columnIndex) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public InputStream getAsciiStream(String columnLabel) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public InputStream getUnicodeStream(String columnLabel) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public InputStream getBinaryStream(String columnLabel) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public SQLWarning getWarnings() throws SQLException { return null; }
    @Override public void clearWarnings() throws SQLException { }
    @Override public String getCursorName() throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public Reader getCharacterStream(int columnIndex) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public Reader getCharacterStream(String columnLabel) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override 
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        Object value = getValue(columnIndex);
        if (value == null) {
            wasNull = true;
            return null;
        }
        wasNull = false;
        
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else if (value instanceof Number) {
            // Convert to string first to preserve any decimal precision from the database
            String numberStr = value.toString();
            try {
                return new BigDecimal(numberStr);
            } catch (NumberFormatException e) {
                // Fallback to double conversion if string parsing fails
                return BigDecimal.valueOf(((Number) value).doubleValue());
            }
        }
        return new BigDecimal(value.toString());
    }
    
    @Override 
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        return getBigDecimal(findColumn(columnLabel));
    }
    @Override public void setFetchDirection(int direction) throws SQLException { }
    @Override public int getFetchDirection() throws SQLException { return ResultSet.FETCH_FORWARD; }
    @Override public void setFetchSize(int rows) throws SQLException { }
    @Override public int getFetchSize() throws SQLException { return 0; }
    @Override public int getType() throws SQLException { return ResultSet.TYPE_FORWARD_ONLY; }
    @Override public int getConcurrency() throws SQLException { return ResultSet.CONCUR_READ_ONLY; }
    @Override public boolean rowUpdated() throws SQLException { return false; }
    @Override public boolean rowInserted() throws SQLException { return false; }
    @Override public boolean rowDeleted() throws SQLException { return false; }
    @Override public void updateNull(int columnIndex) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateBoolean(int columnIndex, boolean x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateByte(int columnIndex, byte x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateShort(int columnIndex, short x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateInt(int columnIndex, int x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateLong(int columnIndex, long x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateFloat(int columnIndex, float x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateDouble(int columnIndex, double x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateString(int columnIndex, String x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateBytes(int columnIndex, byte[] x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateDate(int columnIndex, Date x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateTime(int columnIndex, Time x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateObject(int columnIndex, Object x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateNull(String columnLabel) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateBoolean(String columnLabel, boolean x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateByte(String columnLabel, byte x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateShort(String columnLabel, short x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateInt(String columnLabel, int x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateLong(String columnLabel, long x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateFloat(String columnLabel, float x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateDouble(String columnLabel, double x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateString(String columnLabel, String x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateBytes(String columnLabel, byte[] x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateDate(String columnLabel, Date x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateTime(String columnLabel, Time x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateObject(String columnLabel, Object x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void insertRow() throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateRow() throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void deleteRow() throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void refreshRow() throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void cancelRowUpdates() throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void moveToInsertRow() throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void moveToCurrentRow() throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public Statement getStatement() throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public Ref getRef(int columnIndex) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public Blob getBlob(int columnIndex) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public Clob getClob(int columnIndex) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public Array getArray(int columnIndex) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public Ref getRef(String columnLabel) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public Blob getBlob(String columnLabel) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public Clob getClob(String columnLabel) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public Array getArray(String columnLabel) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public Date getDate(int columnIndex, Calendar cal) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public Date getDate(String columnLabel, Calendar cal) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public Time getTime(int columnIndex, Calendar cal) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public Time getTime(String columnLabel, Calendar cal) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public URL getURL(int columnIndex) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public URL getURL(String columnLabel) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateRef(int columnIndex, Ref x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateRef(String columnLabel, Ref x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateBlob(int columnIndex, Blob x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateBlob(String columnLabel, Blob x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateClob(int columnIndex, Clob x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateClob(String columnLabel, Clob x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateArray(int columnIndex, Array x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateArray(String columnLabel, Array x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public RowId getRowId(int columnIndex) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public RowId getRowId(String columnLabel) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateRowId(int columnIndex, RowId x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateRowId(String columnLabel, RowId x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public int getHoldability() throws SQLException { return ResultSet.CLOSE_CURSORS_AT_COMMIT; }
    @Override public void updateNString(int columnIndex, String nString) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateNString(String columnLabel, String nString) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateNClob(int columnIndex, NClob nClob) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateNClob(String columnLabel, NClob nClob) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public NClob getNClob(int columnIndex) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public NClob getNClob(String columnLabel) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public SQLXML getSQLXML(int columnIndex) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public SQLXML getSQLXML(String columnLabel) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public String getNString(int columnIndex) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public String getNString(String columnLabel) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public Reader getNCharacterStream(int columnIndex) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public Reader getNCharacterStream(String columnLabel) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateClob(int columnIndex, Reader reader, long length) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateClob(String columnLabel, Reader reader, long length) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateCharacterStream(int columnIndex, Reader x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateClob(int columnIndex, Reader reader) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateClob(String columnLabel, Reader reader) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateNClob(int columnIndex, Reader reader) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public void updateNClob(String columnLabel, Reader reader) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public <T> T getObject(int columnIndex, Class<T> type) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    @Override public <T> T getObject(String columnLabel, Class<T> type) throws SQLException { throw new SQLFeatureNotSupportedException(); }
    
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
}