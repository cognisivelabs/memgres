package com.memgres.sql;

import com.memgres.sql.MemGresLexer;
import com.memgres.sql.MemGresParser;
import com.memgres.sql.ast.statement.*;
import com.memgres.sql.parser.SqlAstBuilder;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for NATURAL and USING join clauses.
 */
class NaturalAndUsingJoinTest {
    
    private static final Logger logger = LoggerFactory.getLogger(NaturalAndUsingJoinTest.class);
    
    private SelectStatement parseSelectStatement(String sql) {
        MemGresLexer lexer = new MemGresLexer(CharStreams.fromString(sql));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MemGresParser parser = new MemGresParser(tokens);
        
        SqlAstBuilder builder = new SqlAstBuilder();
        List<Statement> statements = builder.visit(parser.sql());
        
        assertNotNull(statements);
        assertEquals(1, statements.size());
        assertTrue(statements.get(0) instanceof SelectStatement);
        
        return (SelectStatement) statements.get(0);
    }
    
    @Test
    void testNaturalJoinParsing() {
        logger.info("Testing NATURAL JOIN parsing");
        
        String sql = "SELECT * FROM employees NATURAL JOIN departments";
        SelectStatement stmt = parseSelectStatement(sql);
        
        assertNotNull(stmt);
        SimpleSelectStatement simpleSelect = stmt.getCompoundSelectStatement().getSelectStatements().get(0);
        assertTrue(simpleSelect.getFromClause().isPresent());
        
        FromClause fromClause = simpleSelect.getFromClause().get();
        JoinableTable joinableTable = fromClause.getJoinableTables().get(0);
        
        assertEquals(1, joinableTable.getJoins().size());
        JoinClause joinClause = joinableTable.getJoins().get(0);
        
        // Verify it's a NATURAL join
        assertEquals(JoinClause.JoinType.INNER, joinClause.getJoinType());
        assertEquals(JoinClause.JoinConditionType.NATURAL, joinClause.getConditionType());
        assertTrue(joinClause.isNaturalJoin());
        assertFalse(joinClause.isUsingJoin());
        assertFalse(joinClause.getOnCondition().isPresent());
        assertFalse(joinClause.getUsingColumns().isPresent());
        
        logger.info("NATURAL JOIN parsing test passed");
    }
    
    @Test
    void testNaturalLeftJoinParsing() {
        logger.info("Testing NATURAL LEFT JOIN parsing");
        
        String sql = "SELECT * FROM employees NATURAL LEFT JOIN departments";
        SelectStatement stmt = parseSelectStatement(sql);
        
        assertNotNull(stmt);
        SimpleSelectStatement simpleSelect = stmt.getCompoundSelectStatement().getSelectStatements().get(0);
        FromClause fromClause = simpleSelect.getFromClause().get();
        JoinableTable joinableTable = fromClause.getJoinableTables().get(0);
        JoinClause joinClause = joinableTable.getJoins().get(0);
        
        // Verify it's a NATURAL LEFT join
        assertEquals(JoinClause.JoinType.LEFT, joinClause.getJoinType());
        assertEquals(JoinClause.JoinConditionType.NATURAL, joinClause.getConditionType());
        assertTrue(joinClause.isNaturalJoin());
        
        logger.info("NATURAL LEFT JOIN parsing test passed");
    }
    
    @Test
    void testUsingJoinParsing() {
        logger.info("Testing JOIN USING clause parsing");
        
        String sql = "SELECT * FROM employees JOIN departments USING (dept_id)";
        SelectStatement stmt = parseSelectStatement(sql);
        
        assertNotNull(stmt);
        SimpleSelectStatement simpleSelect = stmt.getCompoundSelectStatement().getSelectStatements().get(0);
        FromClause fromClause = simpleSelect.getFromClause().get();
        JoinableTable joinableTable = fromClause.getJoinableTables().get(0);
        JoinClause joinClause = joinableTable.getJoins().get(0);
        
        // Verify it's a USING join
        assertEquals(JoinClause.JoinType.INNER, joinClause.getJoinType());
        assertEquals(JoinClause.JoinConditionType.USING, joinClause.getConditionType());
        assertFalse(joinClause.isNaturalJoin());
        assertTrue(joinClause.isUsingJoin());
        assertFalse(joinClause.getOnCondition().isPresent());
        assertTrue(joinClause.getUsingColumns().isPresent());
        
        List<String> usingColumns = joinClause.getUsingColumns().get();
        assertEquals(1, usingColumns.size());
        assertEquals("dept_id", usingColumns.get(0));
        
        logger.info("JOIN USING clause parsing test passed");
    }
    
    @Test
    void testUsingJoinWithMultipleColumns() {
        logger.info("Testing JOIN USING with multiple columns");
        
        String sql = "SELECT * FROM orders LEFT JOIN order_details USING (order_id, product_id)";
        SelectStatement stmt = parseSelectStatement(sql);
        
        assertNotNull(stmt);
        SimpleSelectStatement simpleSelect = stmt.getCompoundSelectStatement().getSelectStatements().get(0);
        FromClause fromClause = simpleSelect.getFromClause().get();
        JoinableTable joinableTable = fromClause.getJoinableTables().get(0);
        JoinClause joinClause = joinableTable.getJoins().get(0);
        
        // Verify it's a LEFT USING join with multiple columns
        assertEquals(JoinClause.JoinType.LEFT, joinClause.getJoinType());
        assertEquals(JoinClause.JoinConditionType.USING, joinClause.getConditionType());
        assertTrue(joinClause.isUsingJoin());
        assertTrue(joinClause.getUsingColumns().isPresent());
        
        List<String> usingColumns = joinClause.getUsingColumns().get();
        assertEquals(2, usingColumns.size());
        assertEquals("order_id", usingColumns.get(0));
        assertEquals("product_id", usingColumns.get(1));
        
        logger.info("JOIN USING with multiple columns test passed");
    }
    
    @Test
    void testMixedJoinTypes() {
        logger.info("Testing mixed join types in a single query");
        
        String sql = """
            SELECT * 
            FROM employees e
            NATURAL JOIN departments d
            LEFT JOIN projects p ON e.emp_id = p.emp_id
            JOIN locations l USING (location_id)
        """;
        
        SelectStatement stmt = parseSelectStatement(sql);
        assertNotNull(stmt);
        
        SimpleSelectStatement simpleSelect = stmt.getCompoundSelectStatement().getSelectStatements().get(0);
        FromClause fromClause = simpleSelect.getFromClause().get();
        JoinableTable joinableTable = fromClause.getJoinableTables().get(0);
        
        assertEquals(3, joinableTable.getJoins().size());
        
        // First join: NATURAL JOIN
        JoinClause join1 = joinableTable.getJoins().get(0);
        assertEquals(JoinClause.JoinConditionType.NATURAL, join1.getConditionType());
        assertTrue(join1.isNaturalJoin());
        
        // Second join: LEFT JOIN ON
        JoinClause join2 = joinableTable.getJoins().get(1);
        assertEquals(JoinClause.JoinType.LEFT, join2.getJoinType());
        assertEquals(JoinClause.JoinConditionType.ON, join2.getConditionType());
        assertTrue(join2.getOnCondition().isPresent());
        
        // Third join: JOIN USING
        JoinClause join3 = joinableTable.getJoins().get(2);
        assertEquals(JoinClause.JoinConditionType.USING, join3.getConditionType());
        assertTrue(join3.isUsingJoin());
        assertTrue(join3.getUsingColumns().isPresent());
        assertEquals("location_id", join3.getUsingColumns().get().get(0));
        
        logger.info("Mixed join types test passed");
    }
    
    @Test
    void testFullOuterNaturalJoin() {
        logger.info("Testing NATURAL FULL OUTER JOIN");
        
        String sql = "SELECT * FROM employees NATURAL FULL OUTER JOIN departments";
        SelectStatement stmt = parseSelectStatement(sql);
        
        assertNotNull(stmt);
        SimpleSelectStatement simpleSelect = stmt.getCompoundSelectStatement().getSelectStatements().get(0);
        FromClause fromClause = simpleSelect.getFromClause().get();
        JoinableTable joinableTable = fromClause.getJoinableTables().get(0);
        JoinClause joinClause = joinableTable.getJoins().get(0);
        
        // Verify it's a NATURAL FULL OUTER join
        assertEquals(JoinClause.JoinType.FULL_OUTER, joinClause.getJoinType());
        assertEquals(JoinClause.JoinConditionType.NATURAL, joinClause.getConditionType());
        assertTrue(joinClause.isNaturalJoin());
        
        logger.info("NATURAL FULL OUTER JOIN test passed");
    }
}