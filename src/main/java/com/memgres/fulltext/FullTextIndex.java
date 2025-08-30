package com.memgres.fulltext;

import com.memgres.types.Column;
import com.memgres.types.DataType;
import com.memgres.types.Row;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Full-text index for a specific table, maintaining inverted index structure
 * for efficient text search with relevance scoring.
 */
public class FullTextIndex {
    
    private final String schemaName;
    private final String tableName;
    private final List<String> columns;
    
    // Inverted index: word -> set of document IDs containing that word
    private final Map<String, Set<String>> wordToDocuments;
    
    // Document storage: document ID -> document content and metadata
    private final Map<String, FullTextDocument> documents;
    
    // Word frequency: word -> document ID -> frequency count
    private final Map<String, Map<String, Integer>> wordFrequency;
    
    public FullTextIndex(String schemaName, String tableName, List<String> columns) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columns = new ArrayList<>(columns);
        this.wordToDocuments = new ConcurrentHashMap<>();
        this.documents = new ConcurrentHashMap<>();
        this.wordFrequency = new ConcurrentHashMap<>();
    }
    
    /**
     * Adds a row to the full-text index.
     */
    public void addRow(Row row, List<Column> tableColumns, Set<String> ignoreWords, Pattern whitespacePattern) {
        String documentId = generateDocumentId(row, tableColumns);
        
        // Extract text content from indexed columns
        StringBuilder contentBuilder = new StringBuilder();
        Map<String, Object> columnData = new HashMap<>();
        
        for (String columnName : columns) {
            Column column = findColumn(columnName, tableColumns);
            if (column != null) {
                int columnIndex = tableColumns.indexOf(column);
                Object value = row.getData()[columnIndex];
                
                if (value != null) {
                    String textValue = value.toString();
                    contentBuilder.append(textValue).append(" ");
                    columnData.put(columnName, textValue);
                }
            }
        }
        
        String content = contentBuilder.toString().trim();
        if (!content.isEmpty()) {
            FullTextDocument document = new FullTextDocument(documentId, content, columnData, row);
            documents.put(documentId, document);
            
            // Tokenize and index words
            indexWords(content, documentId, ignoreWords, whitespacePattern);
        }
    }
    
    /**
     * Removes a row from the full-text index.
     */
    public void removeRow(Row row, List<Column> tableColumns, Set<String> ignoreWords, Pattern whitespacePattern) {
        String documentId = generateDocumentId(row, tableColumns);
        FullTextDocument document = documents.remove(documentId);
        
        if (document != null) {
            // Remove words from inverted index
            Set<String> words = tokenizeText(document.getContent(), ignoreWords, whitespacePattern);
            for (String word : words) {
                Set<String> docSet = wordToDocuments.get(word);
                if (docSet != null) {
                    docSet.remove(documentId);
                    if (docSet.isEmpty()) {
                        wordToDocuments.remove(word);
                    }
                }
                
                Map<String, Integer> freqMap = wordFrequency.get(word);
                if (freqMap != null) {
                    freqMap.remove(documentId);
                    if (freqMap.isEmpty()) {
                        wordFrequency.remove(word);
                    }
                }
            }
        }
    }
    
    /**
     * Searches the index for the given text query.
     */
    public List<FullTextSearchResult> search(String queryText, Set<String> ignoreWords, Pattern whitespacePattern) {
        Set<String> queryWords = tokenizeText(queryText, ignoreWords, whitespacePattern);
        if (queryWords.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Find documents containing any of the query words
        Map<String, Float> documentScores = new HashMap<>();
        
        for (String word : queryWords) {
            Set<String> matchingDocs = wordToDocuments.get(word);
            if (matchingDocs != null) {
                for (String docId : matchingDocs) {
                    float score = calculateRelevanceScore(word, docId, queryWords.size());
                    documentScores.merge(docId, score, Float::sum);
                }
            }
        }
        
        // Convert to results
        List<FullTextSearchResult> results = new ArrayList<>();
        for (Map.Entry<String, Float> entry : documentScores.entrySet()) {
            String docId = entry.getKey();
            Float score = entry.getValue();
            FullTextDocument document = documents.get(docId);
            
            if (document != null) {
                String query = buildQueryString(document.getRow(), null); // Table columns not available here
                results.add(new FullTextSearchResult(query, score, schemaName, tableName));
            }
        }
        
        return results;
    }
    
    /**
     * Clears all indexed data.
     */
    public void clear() {
        wordToDocuments.clear();
        documents.clear();
        wordFrequency.clear();
    }
    
    private void indexWords(String content, String documentId, Set<String> ignoreWords, Pattern whitespacePattern) {
        Set<String> words = tokenizeText(content, ignoreWords, whitespacePattern);
        
        for (String word : words) {
            // Add to inverted index
            wordToDocuments.computeIfAbsent(word, k -> ConcurrentHashMap.newKeySet()).add(documentId);
            
            // Update frequency count
            wordFrequency.computeIfAbsent(word, k -> new ConcurrentHashMap<>())
                         .merge(documentId, 1, Integer::sum);
        }
    }
    
    private Set<String> tokenizeText(String text, Set<String> ignoreWords, Pattern whitespacePattern) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptySet();
        }
        
        Set<String> words = new HashSet<>();
        String[] tokens = whitespacePattern.split(text.toLowerCase());
        
        for (String token : tokens) {
            // Remove punctuation and normalize
            String word = token.replaceAll("[^a-zA-Z0-9]", "").trim();
            
            if (!word.isEmpty() && word.length() > 1 && !ignoreWords.contains(word)) {
                words.add(word);
            }
        }
        
        return words;
    }
    
    private float calculateRelevanceScore(String word, String documentId, int querySize) {
        // Simple TF-IDF inspired scoring
        Map<String, Integer> docFreq = wordFrequency.get(word);
        if (docFreq == null) {
            return 0.0f;
        }
        
        int termFreq = docFreq.getOrDefault(documentId, 0);
        int documentCount = documents.size();
        int documentFreq = wordToDocuments.get(word).size();
        
        // TF * IDF calculation
        float tf = 1.0f + (float) Math.log(termFreq);
        float idf = (float) Math.log((double) documentCount / documentFreq);
        
        return tf * idf;
    }
    
    private String generateDocumentId(Row row, List<Column> tableColumns) {
        // Use primary key or row hash as document ID
        StringBuilder idBuilder = new StringBuilder();
        
        for (int i = 0; i < tableColumns.size(); i++) {
            Column column = tableColumns.get(i);
            if (column.isPrimaryKey()) {
                Object value = row.getData()[i];
                idBuilder.append(value != null ? value.toString() : "null");
                break;
            }
        }
        
        // If no primary key, use row hash
        if (idBuilder.length() == 0) {
            idBuilder.append(row.hashCode());
        }
        
        return idBuilder.toString();
    }
    
    private String buildQueryString(Row row, List<Column> allTableColumns) {
        // Build a simple query string using row ID
        return "SELECT * FROM " + schemaName + "." + tableName + " WHERE rowid = " + row.getId();
    }
    
    private String formatValue(Object value, DataType dataType) {
        if (value == null) {
            return "NULL";
        }
        
        return switch (dataType) {
            case VARCHAR, CHAR, TEXT, CLOB -> "'" + value.toString().replace("'", "''") + "'";
            default -> value.toString();
        };
    }
    
    private Column findColumn(String columnName, List<Column> tableColumns) {
        return tableColumns.stream()
                .filter(col -> col.getName().toLowerCase().equals(columnName.toLowerCase()))
                .findFirst()
                .orElse(null);
    }
    
    // Getters
    public String getSchemaName() { return schemaName; }
    public String getTableName() { return tableName; }
    public List<String> getColumns() { return new ArrayList<>(columns); }
}