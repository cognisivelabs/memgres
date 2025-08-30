package com.memgres.sql;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionResult;
import com.memgres.types.Row;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for Full-Text Search functionality.
 * Tests H2-compatible FT_* functions and search capabilities.
 */
class FullTextSearchTest {
    
    private MemGresEngine engine;
    private SqlExecutionEngine sqlEngine;
    
    @BeforeEach
    void setUp() throws Exception {
        engine = new MemGresEngine();
        engine.initialize();
        sqlEngine = new SqlExecutionEngine(engine);
        
        // Create test table with text content
        sqlEngine.execute("CREATE TABLE articles (id INTEGER PRIMARY KEY, title VARCHAR(255), content TEXT, author VARCHAR(100))");
        
        // Insert test data
        sqlEngine.execute("INSERT INTO articles VALUES (1, 'Introduction to Databases', 'This article covers the basics of database management systems and SQL queries.', 'John Doe')");
        sqlEngine.execute("INSERT INTO articles VALUES (2, 'Advanced SQL Techniques', 'Learn about complex joins, subqueries, and window functions in modern SQL.', 'Jane Smith')");
        sqlEngine.execute("INSERT INTO articles VALUES (3, 'Database Performance', 'Optimize your database queries for better performance and scalability.', 'Bob Johnson')");
        sqlEngine.execute("INSERT INTO articles VALUES (4, 'NoSQL vs SQL', 'Compare traditional SQL databases with modern NoSQL solutions.', 'Alice Brown')");
        sqlEngine.execute("INSERT INTO articles VALUES (5, 'Data Modeling Best Practices', 'Guidelines for designing efficient and maintainable database schemas.', 'Charlie Wilson')");
    }
    
    @AfterEach
    void tearDown() throws Exception {
        sqlEngine.execute("DROP TABLE IF EXISTS articles");
        engine.shutdown();
        com.memgres.functions.FullTextFunctions.reset();
    }
    
    @Test
    void testFullTextSearchInitialization() throws Exception {
        // Initialize full-text search
        SqlExecutionResult result = sqlEngine.execute("SELECT FT_INIT()");
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        assertEquals("Full-text search initialized", result.getRows().get(0).getData()[0]);
    }
    
    @Test
    void testCreateAndSearchFullTextIndex() throws Exception {
        // Initialize full-text search
        sqlEngine.execute("SELECT FT_INIT()");
        
        // Create full-text index on title and content columns
        SqlExecutionResult createResult = sqlEngine.execute("SELECT FT_CREATE_INDEX('PUBLIC', 'ARTICLES', 'title,content')");
        assertEquals(SqlExecutionResult.ResultType.SELECT, createResult.getType());
        assertEquals("Full-text index created", createResult.getRows().get(0).getData()[0]);
        
        // Search for articles containing "database"
        SqlExecutionResult searchResult = sqlEngine.execute("SELECT * FROM FT_SEARCH('database', 0, 0)");
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, searchResult.getType());
        List<Row> searchRows = searchResult.getRows();
        
        // Should find multiple articles containing "database"
        assertTrue(searchRows.size() >= 2, "Should find at least 2 articles containing 'database'");
        
        // Verify result structure (query, score columns)
        assertEquals(2, searchResult.getColumns().size());
        assertEquals("query", searchResult.getColumns().get(0).getName());
        assertEquals("score", searchResult.getColumns().get(1).getName());
        
        // Verify that results contain valid queries
        for (Row row : searchRows) {
            String query = (String) row.getData()[0];
            Float score = (Float) row.getData()[1];
            
            assertNotNull(query);
            assertTrue(query.startsWith("SELECT * FROM public.articles"));
            assertNotNull(score);
            assertTrue(score > 0.0f);
        }
    }
    
    @Test
    void testFullTextSearchWithLimit() throws Exception {
        sqlEngine.execute("SELECT FT_INIT()");
        sqlEngine.execute("SELECT FT_CREATE_INDEX('PUBLIC', 'ARTICLES', NULL)");
        
        // Search with limit
        SqlExecutionResult result = sqlEngine.execute("SELECT * FROM FT_SEARCH('SQL', 2, 0)");
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        
        // Should respect the limit
        assertTrue(rows.size() <= 2, "Should respect limit of 2");
        
        // Verify results are ordered by relevance (score descending)
        if (rows.size() > 1) {
            Float firstScore = (Float) rows.get(0).getData()[1];
            Float secondScore = (Float) rows.get(1).getData()[1];
            assertTrue(firstScore >= secondScore, "Results should be ordered by relevance");
        }
    }
    
    @Test
    void testFullTextSearchWithOffset() throws Exception {
        sqlEngine.execute("SELECT FT_INIT()");
        sqlEngine.execute("SELECT FT_CREATE_INDEX('PUBLIC', 'ARTICLES', NULL)");
        
        // Search without offset
        SqlExecutionResult allResults = sqlEngine.execute("SELECT * FROM FT_SEARCH('database', 0, 0)");
        
        // Search with offset
        SqlExecutionResult offsetResults = sqlEngine.execute("SELECT * FROM FT_SEARCH('database', 0, 1)");
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, offsetResults.getType());
        
        // Offset results should have one less row (if there are multiple results)
        if (allResults.getRows().size() > 1) {
            assertEquals(allResults.getRows().size() - 1, offsetResults.getRows().size());
        }
    }
    
    @Test
    void testFullTextIndexUpdatesWithDataChanges() throws Exception {
        sqlEngine.execute("SELECT FT_INIT()");
        sqlEngine.execute("SELECT FT_CREATE_INDEX('PUBLIC', 'ARTICLES', NULL)");
        
        // Search for 'technology' - should find no results initially
        SqlExecutionResult initialSearch = sqlEngine.execute("SELECT * FROM FT_SEARCH('technology', 0, 0)");
        assertEquals(0, initialSearch.getRows().size());
        
        // Insert new article with 'technology'
        sqlEngine.execute("INSERT INTO articles VALUES (6, 'Modern Technology', 'Exploring the latest technology trends and innovations.', 'Tech Expert')");
        
        // Search again - should now find the new article
        SqlExecutionResult afterInsert = sqlEngine.execute("SELECT * FROM FT_SEARCH('technology', 0, 0)");
        assertEquals(1, afterInsert.getRows().size());
        
        // Update the article
        sqlEngine.execute("UPDATE articles SET title = 'Advanced Technology Solutions' WHERE id = 6");
        
        // Search should still find it
        SqlExecutionResult afterUpdate = sqlEngine.execute("SELECT * FROM FT_SEARCH('technology', 0, 0)");
        assertEquals(1, afterUpdate.getRows().size());
        
        // Delete the article
        sqlEngine.execute("DELETE FROM articles WHERE id = 6");
        
        // Search should find no results again
        SqlExecutionResult afterDelete = sqlEngine.execute("SELECT * FROM FT_SEARCH('technology', 0, 0)");
        assertEquals(0, afterDelete.getRows().size());
    }
    
    @Test
    void testFullTextSearchCaseInsensitive() throws Exception {
        sqlEngine.execute("SELECT FT_INIT()");
        sqlEngine.execute("SELECT FT_CREATE_INDEX('PUBLIC', 'ARTICLES', NULL)");
        
        // Search with different cases
        SqlExecutionResult upperCase = sqlEngine.execute("SELECT * FROM FT_SEARCH('DATABASE', 0, 0)");
        SqlExecutionResult lowerCase = sqlEngine.execute("SELECT * FROM FT_SEARCH('database', 0, 0)");
        SqlExecutionResult mixedCase = sqlEngine.execute("SELECT * FROM FT_SEARCH('Database', 0, 0)");
        
        // All should return the same results
        assertEquals(upperCase.getRows().size(), lowerCase.getRows().size());
        assertEquals(lowerCase.getRows().size(), mixedCase.getRows().size());
        
        // Should find at least one result
        assertTrue(upperCase.getRows().size() > 0, "Should find articles containing 'database'");
    }
    
    @Test
    void testFullTextSearchMultipleWords() throws Exception {
        sqlEngine.execute("SELECT FT_INIT()");
        sqlEngine.execute("SELECT FT_CREATE_INDEX('PUBLIC', 'ARTICLES', NULL)");
        
        // Search for multiple words
        SqlExecutionResult result = sqlEngine.execute("SELECT * FROM FT_SEARCH('SQL queries', 0, 0)");
        
        assertEquals(SqlExecutionResult.ResultType.SELECT, result.getType());
        List<Row> rows = result.getRows();
        
        // Should find articles containing either "SQL" or "queries"
        assertTrue(rows.size() > 0, "Should find articles containing 'SQL' or 'queries'");
        
        // Verify scores are calculated
        for (Row row : rows) {
            Float score = (Float) row.getData()[1];
            assertTrue(score > 0.0f, "Each result should have a positive score");
        }
    }
    
    @Test
    void testFullTextIndexManagement() throws Exception {
        sqlEngine.execute("SELECT FT_INIT()");
        
        // Create index
        sqlEngine.execute("SELECT FT_CREATE_INDEX('PUBLIC', 'ARTICLES', NULL)");
        
        // Search should work
        SqlExecutionResult searchResult = sqlEngine.execute("SELECT * FROM FT_SEARCH('database', 0, 0)");
        assertTrue(searchResult.getRows().size() > 0);
        
        // Drop index
        SqlExecutionResult dropResult = sqlEngine.execute("SELECT FT_DROP_INDEX('PUBLIC', 'ARTICLES')");
        assertEquals("Full-text index dropped", dropResult.getRows().get(0).getData()[0]);
        
        // Search should return no results after dropping index
        SqlExecutionResult afterDrop = sqlEngine.execute("SELECT * FROM FT_SEARCH('database', 0, 0)");
        assertEquals(0, afterDrop.getRows().size());
    }
    
    @Test
    void testFullTextReindex() throws Exception {
        sqlEngine.execute("SELECT FT_INIT()");
        sqlEngine.execute("SELECT FT_CREATE_INDEX('PUBLIC', 'ARTICLES', NULL)");
        
        // Reindex operation
        SqlExecutionResult reindexResult = sqlEngine.execute("SELECT FT_REINDEX()");
        assertEquals("Full-text indexes rebuilt", reindexResult.getRows().get(0).getData()[0]);
        
        // Search should still work after reindex
        SqlExecutionResult searchResult = sqlEngine.execute("SELECT * FROM FT_SEARCH('database', 0, 0)");
        assertTrue(searchResult.getRows().size() > 0);
    }
    
    @Test
    void testFullTextDropAll() throws Exception {
        sqlEngine.execute("SELECT FT_INIT()");
        sqlEngine.execute("SELECT FT_CREATE_INDEX('PUBLIC', 'ARTICLES', NULL)");
        
        // Verify search works
        SqlExecutionResult beforeDrop = sqlEngine.execute("SELECT * FROM FT_SEARCH('database', 0, 0)");
        assertTrue(beforeDrop.getRows().size() > 0);
        
        // Drop all indexes
        SqlExecutionResult dropAllResult = sqlEngine.execute("SELECT FT_DROP_ALL()");
        assertEquals("All full-text indexes dropped", dropAllResult.getRows().get(0).getData()[0]);
        
        // Search should return no results
        SqlExecutionResult afterDropAll = sqlEngine.execute("SELECT * FROM FT_SEARCH('database', 0, 0)");
        assertEquals(0, afterDropAll.getRows().size());
    }
    
    @Test
    void testFullTextSetIgnoreList() throws Exception {
        sqlEngine.execute("SELECT FT_INIT()");
        
        // Set custom ignore list
        SqlExecutionResult setIgnoreResult = sqlEngine.execute("SELECT FT_SET_IGNORE_LIST('the,and,of,to,a,an')");
        assertEquals("Ignore list updated", setIgnoreResult.getRows().get(0).getData()[0]);
        
        // Create index (should respect ignore list)
        sqlEngine.execute("SELECT FT_CREATE_INDEX('PUBLIC', 'ARTICLES', NULL)");
        
        // Search for ignored words should return fewer/no results
        SqlExecutionResult ignoredWordSearch = sqlEngine.execute("SELECT * FROM FT_SEARCH('the', 0, 0)");
        // Ignored words should not be indexed, so no results expected
        assertEquals(0, ignoredWordSearch.getRows().size());
    }
    
    @Test
    void testFullTextSpecificColumns() throws Exception {
        sqlEngine.execute("SELECT FT_INIT()");
        
        // Create index only on title column
        sqlEngine.execute("SELECT FT_CREATE_INDEX('PUBLIC', 'ARTICLES', 'title')");
        
        // Search for word that appears in content but not title
        SqlExecutionResult result = sqlEngine.execute("SELECT * FROM FT_SEARCH('covers', 0, 0)");
        
        // Should find no results since 'covers' is only in content, not title
        assertEquals(0, result.getRows().size());
        
        // Search for word in title
        SqlExecutionResult titleResult = sqlEngine.execute("SELECT * FROM FT_SEARCH('Introduction', 0, 0)");
        assertTrue(titleResult.getRows().size() > 0, "Should find articles with 'Introduction' in title");
    }
    
    @Test 
    void testFullTextErrorHandling() throws Exception {
        // Try to create index before initialization
        assertThrows(Exception.class, () -> {
            sqlEngine.execute("SELECT FT_CREATE_INDEX('PUBLIC', 'ARTICLES', NULL)");
        });
        
        // Try to search before initialization
        assertThrows(Exception.class, () -> {
            sqlEngine.execute("SELECT * FROM FT_SEARCH('test', 0, 0)");
        });
        
        sqlEngine.execute("SELECT FT_INIT()");
        
        // Try to create index on non-existent table
        assertThrows(Exception.class, () -> {
            sqlEngine.execute("SELECT FT_CREATE_INDEX('PUBLIC', 'NONEXISTENT', NULL)");
        });
        
        // Try to create index on non-text column (using existing articles table)
        assertThrows(Exception.class, () -> {
            sqlEngine.execute("SELECT FT_CREATE_INDEX('PUBLIC', 'ARTICLES', 'id')");
        });
    }
}