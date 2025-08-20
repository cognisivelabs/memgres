package com.memgres.sql;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.ast.expression.*;
import com.memgres.sql.execution.ExecutionContext;
import com.memgres.sql.execution.StatementExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for recursive aggregate function detection in nested expressions.
 */
class RecursiveAggregateDetectionTest {
    
    private StatementExecutor executor;
    private MemGresEngine engine;
    
    @BeforeEach
    void setUp() throws Exception {
        engine = new MemGresEngine();
        engine.initialize();
        executor = new StatementExecutor(engine);
    }
    
    @Test
    void testBinaryExpressionWithAggregateFunction() {
        // Test expression: SUM(salary) + 1000
        AggregateFunction sumFunc = new AggregateFunction(
            AggregateFunction.AggregateType.SUM,
            new ColumnReference("salary")
        );
        
        LiteralExpression thousand = new LiteralExpression(1000, LiteralExpression.LiteralType.INTEGER);
        BinaryExpression binaryExpr = new BinaryExpression(sumFunc, BinaryExpression.Operator.ADD, thousand);
        
        // Use reflection to call the private method
        boolean result = callContainsAggregateFunction(binaryExpr);
        assertTrue(result, "Should detect aggregate function in binary expression");
    }
    
    @Test
    void testNestedBinaryExpressionWithAggregateFunction() {
        // Test expression: (COUNT(*) * 2) + (AVG(salary) - 100)
        AggregateFunction countFunc = new AggregateFunction(
            AggregateFunction.AggregateType.COUNT,
            null
        );
        
        AggregateFunction avgFunc = new AggregateFunction(
            AggregateFunction.AggregateType.AVG,
            new ColumnReference("salary")
        );
        
        LiteralExpression two = new LiteralExpression(2, LiteralExpression.LiteralType.INTEGER);
        LiteralExpression hundred = new LiteralExpression(100, LiteralExpression.LiteralType.INTEGER);
        
        BinaryExpression leftSide = new BinaryExpression(countFunc, BinaryExpression.Operator.MULTIPLY, two);
        BinaryExpression rightSide = new BinaryExpression(avgFunc, BinaryExpression.Operator.SUBTRACT, hundred);
        BinaryExpression outerExpr = new BinaryExpression(leftSide, BinaryExpression.Operator.ADD, rightSide);
        
        boolean result = callContainsAggregateFunction(outerExpr);
        assertTrue(result, "Should detect aggregate functions in nested binary expressions");
    }
    
    @Test
    void testFunctionCallWithAggregateArgument() {
        // Test expression: UPPER(CONCAT('Total: ', SUM(amount)))
        AggregateFunction sumFunc = new AggregateFunction(
            AggregateFunction.AggregateType.SUM,
            new ColumnReference("amount")
        );
        
        LiteralExpression prefix = new LiteralExpression("Total: ", LiteralExpression.LiteralType.STRING);
        FunctionCall concatFunc = new FunctionCall("CONCAT", Arrays.asList(prefix, sumFunc));
        FunctionCall upperFunc = new FunctionCall("UPPER", Arrays.asList(concatFunc));
        
        boolean result = callContainsAggregateFunction(upperFunc);
        assertTrue(result, "Should detect aggregate function in nested function arguments");
    }
    
    @Test
    void testCaseExpressionWithAggregateFunction() {
        // Test expression: CASE WHEN COUNT(*) > 10 THEN 'Many' ELSE SUM(amount) END
        AggregateFunction countFunc = new AggregateFunction(
            AggregateFunction.AggregateType.COUNT,
            null
        );
        
        AggregateFunction sumFunc = new AggregateFunction(
            AggregateFunction.AggregateType.SUM,
            new ColumnReference("amount")
        );
        
        LiteralExpression ten = new LiteralExpression(10, LiteralExpression.LiteralType.INTEGER);
        BinaryExpression condition = new BinaryExpression(countFunc, BinaryExpression.Operator.GREATER_THAN, ten);
        
        LiteralExpression thenResult = new LiteralExpression("Many", LiteralExpression.LiteralType.STRING);
        CaseExpression.WhenClause whenClause = new CaseExpression.WhenClause(condition, thenResult);
        
        CaseExpression caseExpr = new CaseExpression(
            Arrays.asList(whenClause),
            Optional.of(sumFunc)
        );
        
        boolean result = callContainsAggregateFunction(caseExpr);
        assertTrue(result, "Should detect aggregate functions in CASE expression conditions and results");
    }
    
    @Test
    void testUnaryExpressionWithAggregateFunction() {
        // Test expression: -SUM(balance)
        AggregateFunction sumFunc = new AggregateFunction(
            AggregateFunction.AggregateType.SUM,
            new ColumnReference("balance")
        );
        
        UnaryExpression unaryExpr = new UnaryExpression(UnaryExpression.Operator.MINUS, sumFunc);
        
        boolean result = callContainsAggregateFunction(unaryExpr);
        assertTrue(result, "Should detect aggregate function in unary expression");
    }
    
    @Test
    void testSimpleExpressionWithoutAggregateFunction() {
        // Test expression: name + ' - ' + department
        ColumnReference name = new ColumnReference("name");
        ColumnReference dept = new ColumnReference("department");
        LiteralExpression separator = new LiteralExpression(" - ", LiteralExpression.LiteralType.STRING);
        
        BinaryExpression leftSide = new BinaryExpression(name, BinaryExpression.Operator.CONCAT, separator);
        BinaryExpression result = new BinaryExpression(leftSide, BinaryExpression.Operator.CONCAT, dept);
        
        boolean hasAggregate = callContainsAggregateFunction(result);
        assertFalse(hasAggregate, "Should not detect aggregate functions in simple column references");
    }
    
    /**
     * Helper method to call the private containsAggregateFunction method using reflection.
     */
    private boolean callContainsAggregateFunction(Expression expression) {
        try {
            var method = StatementExecutor.class.getDeclaredMethod("containsAggregateFunction", Expression.class);
            method.setAccessible(true);
            return (Boolean) method.invoke(executor, expression);
        } catch (Exception e) {
            throw new RuntimeException("Failed to call containsAggregateFunction", e);
        }
    }
}