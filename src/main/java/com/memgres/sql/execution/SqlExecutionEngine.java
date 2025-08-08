package com.memgres.sql.execution;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.ast.statement.Statement;
import com.memgres.sql.parser.SqlParser;
import com.memgres.sql.parser.SqlParseException;
import com.memgres.transaction.Transaction;
import com.memgres.transaction.TransactionContext;
import com.memgres.transaction.TransactionIsolationLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Main SQL execution engine that parses and executes SQL statements
 * against the MemGres storage layer.
 */
public class SqlExecutionEngine {
    private static final Logger logger = LoggerFactory.getLogger(SqlExecutionEngine.class);
    
    private final MemGresEngine engine;
    private final SqlParser sqlParser;
    private final StatementExecutor statementExecutor;
    
    public SqlExecutionEngine(MemGresEngine engine) {
        this.engine = engine;
        this.sqlParser = new SqlParser();
        this.statementExecutor = new StatementExecutor(engine);
    }
    
    /**
     * Execute a SQL statement string and return the results.
     * 
     * @param sql the SQL statement to execute
     * @return the execution result
     * @throws SqlExecutionException if execution fails
     */
    public SqlExecutionResult execute(String sql) throws SqlExecutionException {
        return execute(sql, TransactionIsolationLevel.READ_COMMITTED);
    }
    
    /**
     * Execute a SQL statement string with specified transaction isolation level.
     * 
     * @param sql the SQL statement to execute
     * @param isolationLevel the transaction isolation level
     * @return the execution result
     * @throws SqlExecutionException if execution fails
     */
    public SqlExecutionResult execute(String sql, TransactionIsolationLevel isolationLevel) throws SqlExecutionException {
        try {
            // Parse SQL into AST statements
            List<Statement> statements = sqlParser.parse(sql);
            
            // Execute statements in a transaction
            Transaction transaction = engine.getTransactionManager().beginTransaction(isolationLevel);
            TransactionContext.setCurrentTransaction(transaction);
            
            try {
                SqlExecutionResult result = null;
                
                // Execute each statement
                for (Statement statement : statements) {
                    result = statementExecutor.execute(statement);
                }
                
                // Commit transaction
                engine.getTransactionManager().commitTransaction(transaction);
                logger.debug("Successfully executed SQL: {}", sql);
                
                return result != null ? result : SqlExecutionResult.empty();
                
            } catch (Exception e) {
                // Rollback transaction on error
                engine.getTransactionManager().rollbackTransaction(transaction);
                throw e;
            } finally {
                TransactionContext.setCurrentTransaction(null);
            }
            
        } catch (SqlParseException e) {
            throw new SqlExecutionException("Failed to parse SQL: " + sql, e);
        } catch (Exception e) {
            throw new SqlExecutionException("Failed to execute SQL: " + sql, e);
        }
    }
    
    /**
     * Execute multiple SQL statements in a single transaction.
     * 
     * @param sqlStatements the SQL statements to execute
     * @return the result of the last statement
     * @throws SqlExecutionException if execution fails
     */
    public SqlExecutionResult executeBatch(List<String> sqlStatements) throws SqlExecutionException {
        return executeBatch(sqlStatements, TransactionIsolationLevel.READ_COMMITTED);
    }
    
    /**
     * Execute multiple SQL statements in a single transaction with specified isolation level.
     * 
     * @param sqlStatements the SQL statements to execute
     * @param isolationLevel the transaction isolation level
     * @return the result of the last statement
     * @throws SqlExecutionException if execution fails
     */
    public SqlExecutionResult executeBatch(List<String> sqlStatements, TransactionIsolationLevel isolationLevel) 
            throws SqlExecutionException {
        
        Transaction transaction = engine.getTransactionManager().beginTransaction(isolationLevel);
        TransactionContext.setCurrentTransaction(transaction);
        
        try {
            SqlExecutionResult result = null;
            
            // Execute each SQL statement
            for (String sql : sqlStatements) {
                List<Statement> statements = sqlParser.parse(sql);
                
                for (Statement statement : statements) {
                    result = statementExecutor.execute(statement);
                }
            }
            
            // Commit transaction
            engine.getTransactionManager().commitTransaction(transaction);
            logger.debug("Successfully executed batch of {} SQL statements", sqlStatements.size());
            
            return result != null ? result : SqlExecutionResult.empty();
            
        } catch (Exception e) {
            // Rollback transaction on error
            engine.getTransactionManager().rollbackTransaction(transaction);
            throw new SqlExecutionException("Failed to execute SQL batch", e);
        } finally {
            TransactionContext.setCurrentTransaction(null);
        }
    }
}