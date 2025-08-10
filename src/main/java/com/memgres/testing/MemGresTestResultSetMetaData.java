package com.memgres.testing;

import com.memgres.types.Column;
import com.memgres.types.DataType;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * ResultSetMetaData implementation for MemGres testing.
 * 
 * @since 1.0.0
 */
public class MemGresTestResultSetMetaData implements ResultSetMetaData {
    
    private final List<Column> columns;
    
    public MemGresTestResultSetMetaData(List<Column> columns) {
        this.columns = columns;
    }
    
    @Override
    public int getColumnCount() throws SQLException {
        return columns.size();
    }
    
    @Override
    public boolean isAutoIncrement(int column) throws SQLException {
        return false; // MemGres doesn't support auto-increment yet
    }
    
    @Override
    public boolean isCaseSensitive(int column) throws SQLException {
        return true; // MemGres is case-sensitive
    }
    
    @Override
    public boolean isSearchable(int column) throws SQLException {
        return true; // All columns are searchable
    }
    
    @Override
    public boolean isCurrency(int column) throws SQLException {
        return false; // No currency types in MemGres
    }
    
    @Override
    public int isNullable(int column) throws SQLException {
        Column col = getColumn(column);
        return col.isNullable() ? columnNullable : columnNoNulls;
    }
    
    @Override
    public boolean isSigned(int column) throws SQLException {
        DataType dataType = getColumn(column).getDataType();
        return dataType == DataType.INTEGER || dataType == DataType.BIGINT || 
               dataType == DataType.DECIMAL || dataType == DataType.DOUBLE_PRECISION;
    }
    
    @Override
    public int getColumnDisplaySize(int column) throws SQLException {
        DataType dataType = getColumn(column).getDataType();
        switch (dataType) {
            case INTEGER:
                return 11;
            case BIGINT:
                return 20;
            case BOOLEAN:
                return 5;
            case UUID:
                return 36;
            case VARCHAR:
                return getColumn(column).getMaxLength() > 0 ? getColumn(column).getMaxLength() : 255;
            case TEXT:
                return Integer.MAX_VALUE;
            case DECIMAL:
            case DOUBLE_PRECISION:
                return 20;
            case JSONB:
                return Integer.MAX_VALUE;
            default:
                return 0;
        }
    }
    
    @Override
    public String getColumnLabel(int column) throws SQLException {
        return getColumn(column).getName();
    }
    
    @Override
    public String getColumnName(int column) throws SQLException {
        return getColumn(column).getName();
    }
    
    @Override
    public String getSchemaName(int column) throws SQLException {
        return "test"; // Default schema
    }
    
    @Override
    public int getPrecision(int column) throws SQLException {
        DataType dataType = getColumn(column).getDataType();
        switch (dataType) {
            case INTEGER:
                return 10;
            case BIGINT:
                return 19;
            case DECIMAL:
                return 38;
            case DOUBLE_PRECISION:
                return 15;
            case VARCHAR:
                return getColumn(column).getMaxLength() > 0 ? getColumn(column).getMaxLength() : 255;
            default:
                return 0;
        }
    }
    
    @Override
    public int getScale(int column) throws SQLException {
        DataType dataType = getColumn(column).getDataType();
        switch (dataType) {
            case DECIMAL:
                return 10;
            case DOUBLE_PRECISION:
                return 15;
            default:
                return 0;
        }
    }
    
    @Override
    public String getTableName(int column) throws SQLException {
        return ""; // Table name not tracked in Column
    }
    
    @Override
    public String getCatalogName(int column) throws SQLException {
        return ""; // MemGres doesn't support catalogs
    }
    
    @Override
    public int getColumnType(int column) throws SQLException {
        DataType dataType = getColumn(column).getDataType();
        switch (dataType) {
            case INTEGER:
                return Types.INTEGER;
            case BIGINT:
                return Types.BIGINT;
            case BOOLEAN:
                return Types.BOOLEAN;
            case VARCHAR:
                return Types.VARCHAR;
            case TEXT:
                return Types.LONGVARCHAR;
            case UUID:
                return Types.OTHER;
            case DECIMAL:
                return Types.DECIMAL;
            case DOUBLE_PRECISION:
                return Types.DOUBLE;
            case JSONB:
                return Types.OTHER;
            default:
                return Types.OTHER;
        }
    }
    
    @Override
    public String getColumnTypeName(int column) throws SQLException {
        return getColumn(column).getDataType().name();
    }
    
    @Override
    public boolean isReadOnly(int column) throws SQLException {
        return false; // All columns are read-write
    }
    
    @Override
    public boolean isWritable(int column) throws SQLException {
        return true; // All columns are writable
    }
    
    @Override
    public boolean isDefinitelyWritable(int column) throws SQLException {
        return true; // All columns are definitely writable
    }
    
    @Override
    public String getColumnClassName(int column) throws SQLException {
        DataType dataType = getColumn(column).getDataType();
        switch (dataType) {
            case INTEGER:
                return Integer.class.getName();
            case BIGINT:
                return Long.class.getName();
            case BOOLEAN:
                return Boolean.class.getName();
            case VARCHAR:
            case TEXT:
                return String.class.getName();
            case UUID:
                return String.class.getName();
            case DECIMAL:
                return java.math.BigDecimal.class.getName();
            case DOUBLE_PRECISION:
                return Double.class.getName();
            case JSONB:
                return String.class.getName();
            default:
                return Object.class.getName();
        }
    }
    
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
    
    private Column getColumn(int column) throws SQLException {
        if (column < 1 || column > columns.size()) {
            throw new SQLException("Column index out of range: " + column);
        }
        return columns.get(column - 1); // Convert to 0-based indexing
    }
}