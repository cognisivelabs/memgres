package com.memgres.types;

import java.io.*;
import java.sql.Clob;
import java.sql.SQLException;

/**
 * MemGres implementation of the JDBC Clob interface.
 * Stores character large objects in memory.
 */
public class MemGresClob implements Clob {
    
    private StringBuilder data;
    
    public MemGresClob() {
        this.data = new StringBuilder();
    }
    
    public MemGresClob(String initialData) {
        this.data = new StringBuilder(initialData != null ? initialData : "");
    }
    
    @Override
    public long length() throws SQLException {
        return data.length();
    }
    
    @Override
    public String getSubString(long pos, int length) throws SQLException {
        if (pos < 1) {
            throw new SQLException("Position must be >= 1");
        }
        
        int start = (int) (pos - 1); // Convert to 0-based indexing
        if (start >= data.length()) {
            return "";
        }
        
        int end = Math.min(start + length, data.length());
        return data.substring(start, end);
    }
    
    @Override
    public Reader getCharacterStream() throws SQLException {
        return new StringReader(data.toString());
    }
    
    @Override
    public InputStream getAsciiStream() throws SQLException {
        return new ByteArrayInputStream(data.toString().getBytes());
    }
    
    @Override
    public long position(String searchstr, long start) throws SQLException {
        if (searchstr == null || start < 1) {
            throw new SQLException("Invalid parameters");
        }
        
        int startIndex = (int) (start - 1); // Convert to 0-based indexing
        if (startIndex >= data.length()) {
            return -1;
        }
        
        int pos = data.indexOf(searchstr, startIndex);
        return pos == -1 ? -1 : pos + 1; // Convert back to 1-based indexing
    }
    
    @Override
    public long position(Clob searchstr, long start) throws SQLException {
        if (searchstr == null) {
            throw new SQLException("Search string cannot be null");
        }
        return position(searchstr.getSubString(1, (int) searchstr.length()), start);
    }
    
    @Override
    public int setString(long pos, String str) throws SQLException {
        if (pos < 1 || str == null) {
            throw new SQLException("Invalid parameters");
        }
        
        int start = (int) (pos - 1); // Convert to 0-based indexing
        
        // Extend data if necessary
        while (data.length() < start) {
            data.append(' ');
        }
        
        // Replace characters
        int end = Math.min(start + str.length(), data.length());
        data.replace(start, end, str);
        
        // If the new string extends beyond current length, append the rest
        if (start + str.length() > data.length()) {
            data.append(str.substring(data.length() - start));
        }
        
        return str.length();
    }
    
    @Override
    public int setString(long pos, String str, int offset, int len) throws SQLException {
        if (str == null || offset < 0 || len < 0 || offset + len > str.length()) {
            throw new SQLException("Invalid parameters");
        }
        return setString(pos, str.substring(offset, offset + len));
    }
    
    @Override
    public OutputStream setAsciiStream(long pos) throws SQLException {
        return new ClobOutputStream(pos);
    }
    
    @Override
    public Writer setCharacterStream(long pos) throws SQLException {
        return new ClobWriter(pos);
    }
    
    @Override
    public void truncate(long len) throws SQLException {
        if (len < 0) {
            throw new SQLException("Length cannot be negative");
        }
        
        if (len < data.length()) {
            data.setLength((int) len);
        }
    }
    
    @Override
    public void free() throws SQLException {
        data = null;
    }
    
    @Override
    public Reader getCharacterStream(long pos, long length) throws SQLException {
        if (pos < 1 || length < 0) {
            throw new SQLException("Invalid parameters");
        }
        
        String substring = getSubString(pos, (int) length);
        return new StringReader(substring);
    }
    
    @Override
    public String toString() {
        return data != null ? data.toString() : "";
    }
    
    /**
     * OutputStream implementation for CLOB ASCII stream writing
     */
    private class ClobOutputStream extends OutputStream {
        private long position;
        private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        
        public ClobOutputStream(long pos) {
            this.position = pos;
        }
        
        @Override
        public void write(int b) throws IOException {
            buffer.write(b);
        }
        
        @Override
        public void close() throws IOException {
            try {
                String str = buffer.toString();
                setString(position, str);
            } catch (SQLException e) {
                throw new IOException("Failed to write to CLOB", e);
            }
        }
    }
    
    /**
     * Writer implementation for CLOB character stream writing
     */
    private class ClobWriter extends Writer {
        private long position;
        private StringBuilder buffer = new StringBuilder();
        
        public ClobWriter(long pos) {
            this.position = pos;
        }
        
        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            buffer.append(cbuf, off, len);
        }
        
        @Override
        public void flush() throws IOException {
            // Nothing to flush for in-memory implementation
        }
        
        @Override
        public void close() throws IOException {
            try {
                setString(position, buffer.toString());
            } catch (SQLException e) {
                throw new IOException("Failed to write to CLOB", e);
            }
        }
    }
}