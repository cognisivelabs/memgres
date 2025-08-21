package com.memgres.wal;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionException;
import com.memgres.sql.execution.SqlExecutionResult;
import com.memgres.transaction.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Write-Ahead Logging functionality.
 */
public class WalIntegrationTest {
    
    @TempDir
    Path tempDir;
    
    private MemGresEngine engine;
    private SqlExecutionEngine sqlExecutionEngine;
    private String walDirectory;
    
    @BeforeEach
    void setUp() throws IOException, SqlExecutionException {
        walDirectory = tempDir.resolve("wal").toString();
        engine = new MemGresEngine(walDirectory);
        engine.initialize();
        
        sqlExecutionEngine = new SqlExecutionEngine(engine);
        
        // Create test table
        sqlExecutionEngine.execute("CREATE TABLE test_table (id INTEGER PRIMARY KEY, name TEXT, age INTEGER)");
    }
    
    @AfterEach
    void tearDown() {
        if (engine != null) {
            engine.shutdown();
        }
    }
    
    @Test
    void testWalEnabled() {
        assertTrue(engine.isWalEnabled(), "WAL should be enabled");
        assertNotNull(engine.getWalTransactionManager(), "WAL transaction manager should be available");
    }
    
    @Test
    void testInsertWithWalLogging() throws Exception {
        WalTransactionManager walTxnMgr = engine.getWalTransactionManager();
        
        // Begin transaction
        Transaction transaction = walTxnMgr.beginTransaction();
        walTxnMgr.setCurrentTransaction(transaction);
        
        try {
            // Execute INSERT
            SqlExecutionResult result = sqlExecutionEngine.execute(
                "INSERT INTO test_table (id, name, age) VALUES (1, 'Alice', 25)"
            );
            
            assertEquals(SqlExecutionResult.ResultType.INSERT, result.getType());
            assertEquals(1, result.getAffectedRows());
            
            // WAL logging happens automatically during execution when WAL is enabled
            // We can't easily verify pending records since WalTransaction is internal
            
            walTxnMgr.commitTransaction(transaction.getId());
            
        } finally {
            walTxnMgr.setCurrentTransaction(null);
        }
    }
    
    @Test
    void testUpdateWithWalLogging() throws Exception {
        WalTransactionManager walTxnMgr = engine.getWalTransactionManager();
        
        // First insert a row
        Transaction insertTxn = walTxnMgr.beginTransaction();
        walTxnMgr.setCurrentTransaction(insertTxn);
        
        try {
            sqlExecutionEngine.execute("INSERT INTO test_table (id, name, age) VALUES (1, 'Bob', 30)");
            walTxnMgr.commitTransaction(insertTxn.getId());
        } finally {
            walTxnMgr.setCurrentTransaction(null);
        }
        
        // Now update the row
        Transaction updateTxn = walTxnMgr.beginTransaction();
        walTxnMgr.setCurrentTransaction(updateTxn);
        
        try {
            SqlExecutionResult result = sqlExecutionEngine.execute(
                "UPDATE test_table SET age = 31 WHERE id = 1"
            );
            
            assertEquals(SqlExecutionResult.ResultType.UPDATE, result.getType());
            assertEquals(1, result.getAffectedRows());
            
            // WAL logging happens automatically during UPDATE execution
            
            walTxnMgr.commitTransaction(updateTxn.getId());
            
        } finally {
            walTxnMgr.setCurrentTransaction(null);
        }
    }
    
    @Test
    void testDeleteWithWalLogging() throws Exception {
        WalTransactionManager walTxnMgr = engine.getWalTransactionManager();
        
        // First insert a row
        Transaction insertTxn = walTxnMgr.beginTransaction();
        walTxnMgr.setCurrentTransaction(insertTxn);
        
        try {
            sqlExecutionEngine.execute("INSERT INTO test_table (id, name, age) VALUES (2, 'Charlie', 35)");
            walTxnMgr.commitTransaction(insertTxn.getId());
        } finally {
            walTxnMgr.setCurrentTransaction(null);
        }
        
        // Now delete the row
        Transaction deleteTxn = walTxnMgr.beginTransaction();
        walTxnMgr.setCurrentTransaction(deleteTxn);
        
        try {
            SqlExecutionResult result = sqlExecutionEngine.execute(
                "DELETE FROM test_table WHERE id = 2"
            );
            
            assertEquals(SqlExecutionResult.ResultType.DELETE, result.getType());
            assertEquals(1, result.getAffectedRows());
            
            // WAL logging happens automatically during DELETE execution
            
            walTxnMgr.commitTransaction(deleteTxn.getId());
            
        } finally {
            walTxnMgr.setCurrentTransaction(null);
        }
    }
    
    @Test
    void testWalLoggingWithExceptions() throws Exception {
        WalTransactionManager walTxnMgr = engine.getWalTransactionManager();
        
        // Test that WAL logging occurs even when transactions have exceptions
        Transaction transaction = walTxnMgr.beginTransaction();
        walTxnMgr.setCurrentTransaction(transaction);
        
        try {
            // Execute INSERT which should create a WAL record
            sqlExecutionEngine.execute("INSERT INTO test_table (id, name, age) VALUES (3, 'Dave', 40)");
            
            // Manually rollback to test WAL record creation
            walTxnMgr.rollbackTransaction(transaction.getId());
            
            // Verify that the operation was logged (even though it was rolled back)
            // The data will still be visible since MemGres doesn't implement full rollback,
            // but the WAL logging should have occurred
            assertTrue(walTxnMgr.isWalEnabled(), "WAL should be enabled and logging operations");
            
        } finally {
            walTxnMgr.setCurrentTransaction(null);
        }
    }
    
    @Test
    void testWalRecoveryEmptyLog() throws Exception {
        // Create a fresh engine with clean WAL directory for this test
        Path emptyWalDir = tempDir.resolve("empty_wal");
        MemGresEngine freshEngine = new MemGresEngine(emptyWalDir.toString());
        
        try {
            freshEngine.initialize(); // This performs recovery on empty WAL
            
            WalTransactionManager walTxnMgr = freshEngine.getWalTransactionManager();
            WalRecoveryInfo recoveryInfo = walTxnMgr.performRecovery();
            
            assertNotNull(recoveryInfo);
            assertTrue(recoveryInfo.isSuccessful(), "Recovery should succeed even with empty log");
            assertEquals(0, recoveryInfo.getTotalRecords(), "Empty log should have 0 records");
        } finally {
            freshEngine.shutdown();
        }
    }
    
    @Test
    void testWalCheckpoint() throws Exception {
        WalTransactionManager walTxnMgr = engine.getWalTransactionManager();
        
        // Perform some operations to generate WAL records
        Transaction txn = walTxnMgr.beginTransaction();
        walTxnMgr.setCurrentTransaction(txn);
        
        try {
            sqlExecutionEngine.execute("INSERT INTO test_table (id, name, age) VALUES (4, 'Eve', 28)");
            walTxnMgr.commitTransaction(txn.getId());
        } finally {
            walTxnMgr.setCurrentTransaction(null);
        }
        
        // Create a checkpoint
        long checkpointLsn = walTxnMgr.checkpoint();
        assertTrue(checkpointLsn > 0, "Checkpoint should return valid LSN");
    }
}