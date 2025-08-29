package com.memgres.types;

import java.io.*;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * MemGres implementation of the JDBC Blob interface.
 * Stores binary large objects in memory.
 */
public class MemGresBlob implements Blob {
    
    private ByteArrayOutputStream data;
    
    public MemGresBlob() {
        this.data = new ByteArrayOutputStream();
    }
    
    public MemGresBlob(byte[] initialData) {
        this.data = new ByteArrayOutputStream();
        if (initialData != null) {
            this.data.write(initialData, 0, initialData.length);
        }
    }
    
    @Override
    public long length() throws SQLException {
        return data.size();
    }
    
    @Override
    public byte[] getBytes(long pos, int length) throws SQLException {
        if (pos < 1) {
            throw new SQLException("Position must be >= 1");
        }
        
        byte[] bytes = data.toByteArray();
        int start = (int) (pos - 1); // Convert to 0-based indexing
        
        if (start >= bytes.length) {
            return new byte[0];
        }
        
        int end = Math.min(start + length, bytes.length);
        return Arrays.copyOfRange(bytes, start, end);
    }
    
    @Override
    public InputStream getBinaryStream() throws SQLException {
        return new ByteArrayInputStream(data.toByteArray());
    }
    
    @Override
    public long position(byte[] pattern, long start) throws SQLException {
        if (pattern == null || pattern.length == 0 || start < 1) {
            throw new SQLException("Invalid parameters");
        }
        
        byte[] bytes = data.toByteArray();
        int startIndex = (int) (start - 1); // Convert to 0-based indexing
        
        if (startIndex >= bytes.length) {
            return -1;
        }
        
        // Boyer-Moore-like search for efficiency
        for (int i = startIndex; i <= bytes.length - pattern.length; i++) {
            boolean found = true;
            for (int j = 0; j < pattern.length; j++) {
                if (bytes[i + j] != pattern[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return i + 1; // Convert back to 1-based indexing
            }
        }
        
        return -1;
    }
    
    @Override
    public long position(Blob pattern, long start) throws SQLException {
        if (pattern == null) {
            throw new SQLException("Pattern cannot be null");
        }
        return position(pattern.getBytes(1, (int) pattern.length()), start);
    }
    
    @Override
    public int setBytes(long pos, byte[] bytes) throws SQLException {
        if (pos < 1 || bytes == null) {
            throw new SQLException("Invalid parameters");
        }
        
        return setBytes(pos, bytes, 0, bytes.length);
    }
    
    @Override
    public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
        if (pos < 1 || bytes == null || offset < 0 || len < 0 || offset + len > bytes.length) {
            throw new SQLException("Invalid parameters");
        }
        
        byte[] currentData = data.toByteArray();
        int start = (int) (pos - 1); // Convert to 0-based indexing
        
        // Create new data array with appropriate size
        int newSize = Math.max(currentData.length, start + len);
        byte[] newData = new byte[newSize];
        
        // Copy existing data
        System.arraycopy(currentData, 0, newData, 0, Math.min(currentData.length, start));
        
        // Set new bytes
        System.arraycopy(bytes, offset, newData, start, len);
        
        // Copy remaining existing data if it extends beyond the new bytes
        if (currentData.length > start + len) {
            System.arraycopy(currentData, start + len, newData, start + len, 
                           currentData.length - start - len);
        }
        
        // Replace the data
        data = new ByteArrayOutputStream();
        data.write(newData, 0, newData.length);
        
        return len;
    }
    
    @Override
    public OutputStream setBinaryStream(long pos) throws SQLException {
        return new BlobOutputStream(pos);
    }
    
    @Override
    public void truncate(long len) throws SQLException {
        if (len < 0) {
            throw new SQLException("Length cannot be negative");
        }
        
        byte[] currentData = data.toByteArray();
        if (len < currentData.length) {
            data = new ByteArrayOutputStream();
            data.write(currentData, 0, (int) len);
        }
    }
    
    @Override
    public void free() throws SQLException {
        data = null;
    }
    
    @Override
    public InputStream getBinaryStream(long pos, long length) throws SQLException {
        if (pos < 1 || length < 0) {
            throw new SQLException("Invalid parameters");
        }
        
        byte[] bytes = getBytes(pos, (int) length);
        return new ByteArrayInputStream(bytes);
    }
    
    /**
     * Get the internal byte array (for internal use)
     */
    public byte[] toByteArray() {
        return data != null ? data.toByteArray() : new byte[0];
    }
    
    /**
     * OutputStream implementation for BLOB binary stream writing
     */
    private class BlobOutputStream extends OutputStream {
        private long position;
        private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        
        public BlobOutputStream(long pos) {
            this.position = pos;
        }
        
        @Override
        public void write(int b) throws IOException {
            buffer.write(b);
        }
        
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            buffer.write(b, off, len);
        }
        
        @Override
        public void close() throws IOException {
            try {
                setBytes(position, buffer.toByteArray());
            } catch (SQLException e) {
                throw new IOException("Failed to write to BLOB", e);
            }
        }
    }
}