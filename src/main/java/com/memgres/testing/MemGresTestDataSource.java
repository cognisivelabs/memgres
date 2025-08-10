package com.memgres.testing;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * DataSource implementation for MemGres testing integration.
 * 
 * <p>This class provides a JDBC-compatible DataSource interface for MemGres
 * database instances in testing environments. It allows integration with
 * existing frameworks and tools that expect a standard DataSource.</p>
 * 
 * <p>Note: This is a simplified DataSource implementation designed for testing.
 * It does not support connection pooling or advanced DataSource features.</p>
 * 
 * @since 1.0.0
 */
public class MemGresTestDataSource implements DataSource {
    
    private final MemGresEngine engine;
    private final SqlExecutionEngine sqlEngine;
    private PrintWriter logWriter;
    private int loginTimeout = 0;
    
    /**
     * Creates a new MemGresTestDataSource wrapping the given engine.
     * 
     * @param engine the MemGres engine to wrap
     */
    public MemGresTestDataSource(MemGresEngine engine) {
        this.engine = engine;
        this.sqlEngine = new SqlExecutionEngine(engine);
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        return new MemGresTestConnection(engine, sqlEngine);
    }
    
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        // MemGres doesn't support authentication in test mode
        return getConnection();
    }
    
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return logWriter;
    }
    
    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        this.logWriter = out;
    }
    
    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        this.loginTimeout = seconds;
    }
    
    @Override
    public int getLoginTimeout() throws SQLException {
        return loginTimeout;
    }
    
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("getParentLogger not supported");
    }
    
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
     * Gets the underlying MemGres engine.
     * 
     * @return the MemGres engine
     */
    public MemGresEngine getEngine() {
        return engine;
    }
    
    /**
     * Gets the SQL execution engine.
     * 
     * @return the SQL execution engine
     */
    public SqlExecutionEngine getSqlEngine() {
        return sqlEngine;
    }
}