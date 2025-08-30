package com.memgres.fulltext;

import com.memgres.types.Row;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a document in the full-text search index.
 * Contains the original row data and extracted text content.
 */
public class FullTextDocument {
    
    private final String documentId;
    private final String content;
    private final Map<String, Object> columnData;
    private final Row row;
    
    public FullTextDocument(String documentId, String content, Map<String, Object> columnData, Row row) {
        this.documentId = documentId;
        this.content = content;
        this.columnData = Map.copyOf(columnData);
        this.row = row;
    }
    
    public String getDocumentId() {
        return documentId;
    }
    
    public String getContent() {
        return content;
    }
    
    public Map<String, Object> getColumnData() {
        return columnData;
    }
    
    public Row getRow() {
        return row;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        FullTextDocument that = (FullTextDocument) obj;
        return Objects.equals(documentId, that.documentId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(documentId);
    }
    
    @Override
    public String toString() {
        return "FullTextDocument{" +
               "documentId='" + documentId + '\'' +
               ", content='" + content + '\'' +
               '}';
    }
}