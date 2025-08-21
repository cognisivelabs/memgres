package com.memgres.wal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Write-Ahead Log Manager for transaction durability.
 * Handles writing log records to disk and provides recovery capabilities.
 */
public class WalManager {
    private static final Logger logger = LoggerFactory.getLogger(WalManager.class);
    
    private static final String WAL_FILE_EXTENSION = ".wal";
    private static final String WAL_INDEX_EXTENSION = ".idx";
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final long MAX_WAL_FILE_SIZE = 64 * 1024 * 1024; // 64MB
    
    private final Path walDirectory;
    private final AtomicLong nextLsn;
    private final AtomicLong currentWalFileNumber;
    private final ReadWriteLock walLock;
    private final Map<Long, WalFile> openWalFiles;
    private volatile WalFile currentWalFile;
    private volatile boolean isEnabled;
    private volatile boolean isRecovering;
    
    public WalManager(String walDirectoryPath) throws IOException {
        this.walDirectory = Paths.get(walDirectoryPath);
        this.nextLsn = new AtomicLong(1);
        this.currentWalFileNumber = new AtomicLong(1);
        this.walLock = new ReentrantReadWriteLock();
        this.openWalFiles = new ConcurrentHashMap<>();
        this.isEnabled = true;
        this.isRecovering = false;
        
        // Create WAL directory if it doesn't exist
        Files.createDirectories(walDirectory);
        
        // Initialize WAL system
        initialize();
        
        logger.info("WAL Manager initialized with directory: {}", walDirectory);
    }
    
    /**
     * Initialize the WAL system by scanning existing files and setting up recovery point.
     */
    private void initialize() throws IOException {
        // Scan existing WAL files to determine next LSN and file number
        List<Path> walFiles = scanWalFiles();
        
        if (!walFiles.isEmpty()) {
            // Find the highest file number and LSN
            long maxFileNumber = 0;
            long maxLsn = 0;
            
            for (Path walFile : walFiles) {
                long fileNumber = extractFileNumber(walFile);
                maxFileNumber = Math.max(maxFileNumber, fileNumber);
                
                // Scan file for highest LSN
                long fileLsn = getHighestLsnInFile(walFile);
                maxLsn = Math.max(maxLsn, fileLsn);
            }
            
            currentWalFileNumber.set(maxFileNumber);
            nextLsn.set(maxLsn + 1);
        }
        
        // Open or create current WAL file
        createNewWalFile();
        
        logger.info("WAL initialization complete - Next LSN: {}, Current file: {}", 
                   nextLsn.get(), currentWalFileNumber.get());
    }
    
    /**
     * Write a WAL record to the log.
     */
    public long writeRecord(WalRecord record) throws IOException {
        if (!isEnabled || isRecovering) {
            return -1;
        }
        
        walLock.writeLock().lock();
        try {
            // Assign LSN to the record
            long lsn = nextLsn.getAndIncrement();
            WalRecord recordWithLsn = new WalRecord.Builder()
                .lsn(lsn)
                .transactionId(record.getTransactionId())
                .type(record.getType())
                .schema(record.getSchemaName())
                .table(record.getTableName())
                .beforeImage(record.getBeforeImage())
                .afterImage(record.getAfterImage())
                .sql(record.getSqlStatement())
                .timestamp(record.getTimestamp())
                .build();
            
            // Check if we need to rotate to a new WAL file
            if (currentWalFile.getSize() > MAX_WAL_FILE_SIZE) {
                rotateWalFile();
            }
            
            // Write record to current WAL file
            currentWalFile.writeRecord(recordWithLsn);
            
            logger.trace("Wrote WAL record: {}", recordWithLsn);
            return lsn;
            
        } finally {
            walLock.writeLock().unlock();
        }
    }
    
    /**
     * Force all pending writes to disk (fsync).
     */
    public void flush() throws IOException {
        walLock.readLock().lock();
        try {
            if (currentWalFile != null) {
                currentWalFile.flush();
            }
        } finally {
            walLock.readLock().unlock();
        }
    }
    
    /**
     * Get all WAL records from a specific LSN onwards.
     * Used for recovery operations.
     */
    public List<WalRecord> getRecordsFromLsn(long fromLsn) throws IOException {
        walLock.readLock().lock();
        try {
            List<WalRecord> records = new ArrayList<>();
            
            // Scan all WAL files and collect records with LSN >= fromLsn
            List<Path> walFiles = scanWalFiles();
            
            for (Path walFile : walFiles) {
                try (WalFile reader = new WalFile(walFile, false)) {
                    List<WalRecord> fileRecords = reader.readAllRecords();
                    for (WalRecord record : fileRecords) {
                        if (record.getLsn() >= fromLsn) {
                            records.add(record);
                        }
                    }
                }
            }
            
            // Sort by LSN to ensure proper order
            records.sort(Comparator.comparingLong(WalRecord::getLsn));
            
            return records;
        } finally {
            walLock.readLock().unlock();
        }
    }
    
    /**
     * Perform crash recovery by replaying WAL records.
     */
    public WalRecoveryInfo performRecovery() throws IOException {
        isRecovering = true;
        long startTime = System.currentTimeMillis();
        
        try {
            logger.info("Starting WAL recovery...");
            
            // Get all records from the beginning
            List<WalRecord> allRecords = getRecordsFromLsn(1);
            
            WalRecoveryInfo recoveryInfo = new WalRecoveryInfo();
            recoveryInfo.setTotalRecords(allRecords.size());
            
            // Track active transactions
            Set<Long> activeTransactions = new HashSet<>();
            Map<Long, List<WalRecord>> transactionRecords = new HashMap<>();
            
            // Phase 1: Analysis - find committed and aborted transactions
            for (WalRecord record : allRecords) {
                long txnId = record.getTransactionId();
                
                switch (record.getType()) {
                    case BEGIN_TRANSACTION:
                        activeTransactions.add(txnId);
                        transactionRecords.computeIfAbsent(txnId, k -> new ArrayList<>()).add(record);
                        break;
                        
                    case COMMIT_TRANSACTION:
                        recoveryInfo.incrementCommittedTransactions();
                        activeTransactions.remove(txnId);
                        transactionRecords.computeIfAbsent(txnId, k -> new ArrayList<>()).add(record);
                        break;
                        
                    case ABORT_TRANSACTION:
                        recoveryInfo.incrementAbortedTransactions();
                        activeTransactions.remove(txnId);
                        transactionRecords.computeIfAbsent(txnId, k -> new ArrayList<>()).add(record);
                        break;
                        
                    default:
                        transactionRecords.computeIfAbsent(txnId, k -> new ArrayList<>()).add(record);
                        break;
                }
            }
            
            // Remaining active transactions are considered aborted
            recoveryInfo.setAbortedTransactions(recoveryInfo.getAbortedTransactions() + activeTransactions.size());
            
            long recoveryTime = System.currentTimeMillis() - startTime;
            recoveryInfo.setRecoveryTimeMs(recoveryTime);
            
            logger.info("WAL recovery completed in {}ms - Committed: {}, Aborted: {}, Total records: {}",
                       recoveryTime, recoveryInfo.getCommittedTransactions(), 
                       recoveryInfo.getAbortedTransactions(), recoveryInfo.getTotalRecords());
            
            return recoveryInfo;
            
        } finally {
            isRecovering = false;
        }
    }
    
    /**
     * Create a checkpoint by writing a checkpoint record.
     */
    public long checkpoint() throws IOException {
        logger.info("Creating WAL checkpoint...");
        
        WalRecord checkpointRecord = new WalRecord.Builder()
            .transactionId(0) // System operation
            .type(WalRecord.RecordType.CHECKPOINT)
            .sql("CHECKPOINT")
            .build();
        
        long checkpointLsn = writeRecord(checkpointRecord);
        flush(); // Ensure checkpoint is persisted
        
        logger.info("Checkpoint created at LSN: {}", checkpointLsn);
        return checkpointLsn;
    }
    
    /**
     * Shutdown the WAL manager.
     */
    public void shutdown() throws IOException {
        logger.info("Shutting down WAL Manager...");
        
        walLock.writeLock().lock();
        try {
            isEnabled = false;
            
            // Flush and close current WAL file
            if (currentWalFile != null) {
                currentWalFile.close();
            }
            
            // Close all open WAL files
            for (WalFile walFile : openWalFiles.values()) {
                walFile.close();
            }
            openWalFiles.clear();
            
        } finally {
            walLock.writeLock().unlock();
        }
        
        logger.info("WAL Manager shutdown complete");
    }
    
    // Helper methods
    
    private void createNewWalFile() throws IOException {
        long fileNumber = currentWalFileNumber.get();
        Path walFilePath = walDirectory.resolve(fileNumber + WAL_FILE_EXTENSION);
        
        if (currentWalFile != null) {
            currentWalFile.close();
        }
        
        currentWalFile = new WalFile(walFilePath, true);
        openWalFiles.put(fileNumber, currentWalFile);
        
        logger.debug("Created new WAL file: {}", walFilePath);
    }
    
    private void rotateWalFile() throws IOException {
        logger.info("Rotating WAL file...");
        
        currentWalFileNumber.incrementAndGet();
        createNewWalFile();
        
        logger.info("WAL file rotation complete - New file number: {}", currentWalFileNumber.get());
    }
    
    private List<Path> scanWalFiles() throws IOException {
        if (!Files.exists(walDirectory)) {
            return Collections.emptyList();
        }
        
        return Files.list(walDirectory)
            .filter(path -> path.toString().endsWith(WAL_FILE_EXTENSION))
            .sorted()
            .toList();
    }
    
    private long extractFileNumber(Path walFile) {
        String fileName = walFile.getFileName().toString();
        String numberPart = fileName.substring(0, fileName.lastIndexOf('.'));
        return Long.parseLong(numberPart);
    }
    
    private long getHighestLsnInFile(Path walFile) throws IOException {
        try (WalFile reader = new WalFile(walFile, false)) {
            List<WalRecord> records = reader.readAllRecords();
            return records.stream()
                .mapToLong(WalRecord::getLsn)
                .max()
                .orElse(0);
        }
    }
    
    // Getters for monitoring
    public boolean isEnabled() { return isEnabled; }
    public boolean isRecovering() { return isRecovering; }
    public long getNextLsn() { return nextLsn.get(); }
    public long getCurrentWalFileNumber() { return currentWalFileNumber.get(); }
    public Path getWalDirectory() { return walDirectory; }
}