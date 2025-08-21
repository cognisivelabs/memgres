package com.memgres.wal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles reading and writing of WAL records to/from files.
 * Uses efficient binary serialization with checksums for data integrity.
 */
public class WalFile implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(WalFile.class);
    
    private static final int RECORD_HEADER_SIZE = 16; // 8 bytes length + 8 bytes checksum
    private static final int BUFFER_SIZE = 8192;
    
    private final Path filePath;
    private final boolean writeMode;
    private FileChannel channel;
    private volatile long fileSize;
    
    public WalFile(Path filePath, boolean writeMode) throws IOException {
        this.filePath = filePath;
        this.writeMode = writeMode;
        
        if (writeMode) {
            this.channel = FileChannel.open(filePath, 
                StandardOpenOption.CREATE, 
                StandardOpenOption.WRITE,
                StandardOpenOption.APPEND);
        } else {
            this.channel = FileChannel.open(filePath, StandardOpenOption.READ);
        }
        
        this.fileSize = channel.size();
        
        logger.debug("Opened WAL file: {} (mode: {}, size: {} bytes)", 
                    filePath, writeMode ? "WRITE" : "READ", fileSize);
    }
    
    /**
     * Write a WAL record to the file.
     */
    public synchronized void writeRecord(WalRecord record) throws IOException {
        if (!writeMode) {
            throw new IllegalStateException("File opened in read-only mode");
        }
        
        // Serialize the record
        byte[] recordData = serializeRecord(record);
        
        // Create buffer with header + data
        ByteBuffer buffer = ByteBuffer.allocate(RECORD_HEADER_SIZE + recordData.length);
        
        // Write header: length (8 bytes) + checksum (8 bytes)
        buffer.putLong(recordData.length);
        buffer.putLong(record.getChecksum());
        
        // Write record data
        buffer.put(recordData);
        
        buffer.flip();
        
        // Write to file
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
        
        fileSize += RECORD_HEADER_SIZE + recordData.length;
        
        logger.trace("Wrote record to WAL file: {} bytes, LSN: {}", 
                    RECORD_HEADER_SIZE + recordData.length, record.getLsn());
    }
    
    /**
     * Read all records from the file.
     */
    public List<WalRecord> readAllRecords() throws IOException {
        List<WalRecord> records = new ArrayList<>();
        
        channel.position(0);
        ByteBuffer headerBuffer = ByteBuffer.allocate(RECORD_HEADER_SIZE);
        
        while (channel.position() < channel.size()) {
            // Read record header
            headerBuffer.clear();
            int headerBytesRead = channel.read(headerBuffer);
            
            if (headerBytesRead != RECORD_HEADER_SIZE) {
                logger.warn("Incomplete header at end of WAL file: {}", filePath);
                break;
            }
            
            headerBuffer.flip();
            long recordLength = headerBuffer.getLong();
            long expectedChecksum = headerBuffer.getLong();
            
            // Validate record length
            if (recordLength < 0 || recordLength > 1024 * 1024) { // Max 1MB per record
                logger.error("Invalid record length in WAL file: {} at position {}", 
                           recordLength, channel.position() - RECORD_HEADER_SIZE);
                break;
            }
            
            // Read record data
            ByteBuffer dataBuffer = ByteBuffer.allocate((int) recordLength);
            int dataBytesRead = channel.read(dataBuffer);
            
            if (dataBytesRead != recordLength) {
                logger.warn("Incomplete record data at end of WAL file: {}", filePath);
                break;
            }
            
            dataBuffer.flip();
            
            try {
                // Deserialize record
                WalRecord record = deserializeRecord(dataBuffer.array());
                
                // Verify checksum
                if (record.getChecksum() != expectedChecksum) {
                    logger.error("Checksum mismatch in WAL record at LSN: {}", record.getLsn());
                    continue; // Skip corrupted record
                }
                
                // Verify record integrity
                if (!record.isValid()) {
                    logger.error("Invalid WAL record at LSN: {}", record.getLsn());
                    continue; // Skip invalid record
                }
                
                records.add(record);
                
            } catch (Exception e) {
                logger.error("Error deserializing WAL record at position: " + 
                           (channel.position() - recordLength), e);
                // Continue reading next record
            }
        }
        
        logger.debug("Read {} records from WAL file: {}", records.size(), filePath);
        return records;
    }
    
    /**
     * Force all pending writes to disk.
     */
    public void flush() throws IOException {
        if (writeMode && channel.isOpen()) {
            channel.force(true);
        }
    }
    
    public long getSize() {
        return fileSize;
    }
    
    public Path getFilePath() {
        return filePath;
    }
    
    @Override
    public void close() throws IOException {
        if (channel != null && channel.isOpen()) {
            if (writeMode) {
                flush();
            }
            channel.close();
        }
        logger.debug("Closed WAL file: {}", filePath);
    }
    
    /**
     * Serialize a WAL record to bytes using Java serialization.
     */
    private byte[] serializeRecord(WalRecord record) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            
            oos.writeObject(record);
            oos.flush();
            return baos.toByteArray();
        }
    }
    
    /**
     * Deserialize a WAL record from bytes.
     */
    private WalRecord deserializeRecord(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            
            return (WalRecord) ois.readObject();
        }
    }
}