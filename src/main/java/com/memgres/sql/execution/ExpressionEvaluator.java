package com.memgres.sql.execution;

import com.memgres.core.MemGresEngine;
import com.memgres.functions.UuidFunctions;
import com.memgres.sql.ast.expression.AggregateFunction;
import com.memgres.sql.ast.expression.BinaryExpression;
import com.memgres.sql.ast.expression.ColumnReference;
import com.memgres.sql.ast.expression.CurrentValueForExpression;
import com.memgres.sql.ast.expression.Expression;
import com.memgres.sql.ast.expression.ExistsExpression;
import com.memgres.sql.ast.expression.FunctionCall;
import com.memgres.sql.ast.expression.InSubqueryExpression;
import com.memgres.sql.ast.expression.LiteralExpression;
import com.memgres.sql.ast.expression.NextValueForExpression;
import com.memgres.sql.ast.expression.SubqueryExpression;
import com.memgres.sql.ast.expression.UnaryExpression;
import com.memgres.storage.Table;
import com.memgres.types.Column;
import com.memgres.types.Row;

import java.util.List;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluates SQL expressions in the context of a row and table.
 */
public class ExpressionEvaluator {
    
    private static final Logger logger = LoggerFactory.getLogger(ExpressionEvaluator.class);
    private final MemGresEngine engine;
    
    public ExpressionEvaluator(MemGresEngine engine) {
        this.engine = engine;
    }
    
    /**
     * Evaluate an expression and return its value.
     */
    public Object evaluate(Expression expression, ExecutionContext context) {
        if (expression instanceof LiteralExpression) {
            return evaluateLiteral((LiteralExpression) expression);
        }
        else if (expression instanceof ColumnReference) {
            return evaluateColumnReference((ColumnReference) expression, context);
        }
        else if (expression instanceof BinaryExpression) {
            return evaluateBinaryExpression((BinaryExpression) expression, context);
        }
        else if (expression instanceof UnaryExpression) {
            return evaluateUnaryExpression((UnaryExpression) expression, context);
        }
        else if (expression instanceof FunctionCall) {
            return evaluateFunctionCall((FunctionCall) expression, context);
        }
        else if (expression instanceof SubqueryExpression) {
            return evaluateSubqueryExpression((SubqueryExpression) expression, context);
        }
        else if (expression instanceof ExistsExpression) {
            return evaluateExistsExpression((ExistsExpression) expression, context);
        }
        else if (expression instanceof InSubqueryExpression) {
            return evaluateInSubqueryExpression((InSubqueryExpression) expression, context);
        }
        else if (expression instanceof AggregateFunction) {
            throw new IllegalStateException("Aggregate functions must be handled at the SELECT statement level");
        }
        else if (expression instanceof NextValueForExpression) {
            return evaluateNextValueForExpression((NextValueForExpression) expression, context);
        }
        else if (expression instanceof CurrentValueForExpression) {
            return evaluateCurrentValueForExpression((CurrentValueForExpression) expression, context);
        }
        else {
            throw new IllegalArgumentException("Unsupported expression type: " + expression.getClass());
        }
    }
    
    private Object evaluateLiteral(LiteralExpression literal) {
        return literal.getValue();
    }
    
    private Object evaluateColumnReference(ColumnReference colRef, ExecutionContext context) {
        Row currentRow = context.getCurrentRow();
        
        if (currentRow == null) {
            throw new IllegalStateException("Cannot evaluate column reference without row context");
        }
        
        // Use joined columns if available, otherwise fall back to current table
        java.util.List<Column> columns;
        if (context.getJoinedColumns() != null) {
            columns = context.getJoinedColumns();
        } else {
            Table currentTable = context.getCurrentTable();
            if (currentTable == null) {
                throw new IllegalStateException("Cannot evaluate column reference without table context");
            }
            columns = currentTable.getColumns();
        }
        
        // Find column index
        String columnName = colRef.getColumnName().toLowerCase();
        Integer columnIndex = null;
        
        // For table-qualified references (e.g., authors.id, posts.author_id)
        if (colRef.getTableName().isPresent()) {
            String tableName = colRef.getTableName().get().toLowerCase();
            
            // In join context, we need to properly resolve table-qualified column references
            // The combined columns are ordered as [leftTable columns..., rightTable columns...]
            // We need to get the table information from the execution context
            Map<String, List<Column>> tableColumns = context.getTableColumns();
            if (tableColumns != null && tableColumns.containsKey(tableName)) {
                List<Column> targetTableColumns = tableColumns.get(tableName);
                
                logger.debug("Looking for column '{}' in table '{}', available columns: {}", 
                    columnName, tableName, 
                    targetTableColumns.stream().map(c -> c.getName()).collect(java.util.stream.Collectors.toList()));
                
                // Find the column within the specific table
                for (int i = 0; i < targetTableColumns.size(); i++) {
                    if (targetTableColumns.get(i).getName().toLowerCase().equals(columnName)) {
                        // Now find the absolute index in the combined columns list
                        columnIndex = findColumnIndexInCombined(columns, tableName, columnName, context);
                        break;
                    }
                }
            } else {
                logger.debug("Table '{}' not found in tableColumns. Available tables: {}", 
                    tableName, tableColumns != null ? tableColumns.keySet() : "null");
            }
            
            if (columnIndex == null) {
                // Fallback: search all columns but prefer exact matches first
                for (int i = 0; i < columns.size(); i++) {
                    Column col = columns.get(i);
                    if (col.getName().toLowerCase().equals(columnName)) {
                        columnIndex = i;
                        break; // Take first match - this is the old buggy behavior but safer fallback
                    }
                }
            }
        } else {
            // Unqualified column reference - search all columns
            for (int i = 0; i < columns.size(); i++) {
                Column col = columns.get(i);
                if (col.getName().toLowerCase().equals(columnName)) {
                    columnIndex = i;
                    break;
                }
            }
        }
        
        if (columnIndex == null) {
            throw new IllegalArgumentException("Column not found: " + columnName + 
                (colRef.getTableName().isPresent() ? " in table " + colRef.getTableName().get() : ""));
        }
        
        return currentRow.getData()[columnIndex];
    }
    
    /**
     * Find the absolute index of a table-qualified column in the combined columns list.
     * For JOIN operations, columns are combined as [leftTable columns..., rightTable columns...]
     */
    private Integer findColumnIndexInCombined(List<Column> combinedColumns, String tableName, 
                                            String columnName, ExecutionContext context) {
        Map<String, List<Column>> tableColumns = context.getTableColumns();
        if (tableColumns == null) {
            return null;
        }
        
        List<String> tableOrder = context.getTableOrder();
        if (tableOrder == null) {
            return null;
        }
        
        int absoluteIndex = 0;
        for (String currentTable : tableOrder) {
            List<Column> currentTableColumns = tableColumns.get(currentTable.toLowerCase());
            if (currentTableColumns == null) {
                continue;
            }
            
            if (currentTable.toLowerCase().equals(tableName.toLowerCase())) {
                // Found the target table, now find the column within it
                for (int i = 0; i < currentTableColumns.size(); i++) {
                    if (currentTableColumns.get(i).getName().toLowerCase().equals(columnName.toLowerCase())) {
                        return absoluteIndex + i;
                    }
                }
                // Column not found in this table
                return null;
            }
            
            // Move to next table's columns
            absoluteIndex += currentTableColumns.size();
        }
        
        return null;
    }
    
    private Object evaluateBinaryExpression(BinaryExpression binary, ExecutionContext context) {
        Object left = evaluate(binary.getLeft(), context);
        Object right = evaluate(binary.getRight(), context);
        
        switch (binary.getOperator()) {
            // Arithmetic operators
            case ADD:
                return addValues(left, right);
            case SUBTRACT:
                return subtractValues(left, right);
            case MULTIPLY:
                return multiplyValues(left, right);
            case DIVIDE:
                return divideValues(left, right);
            case MODULO:
                return moduloValues(left, right);
                
            // Comparison operators
            case EQUALS:
                return equalValues(left, right);
            case NOT_EQUALS:
                return !equalValues(left, right);
            case LESS_THAN:
                return compareValues(left, right) < 0;
            case LESS_THAN_EQUALS:
                return compareValues(left, right) <= 0;
            case GREATER_THAN:
                return compareValues(left, right) > 0;
            case GREATER_THAN_EQUALS:
                return compareValues(left, right) >= 0;
                
            // Logical operators
            case AND:
                return isTruthy(left) && isTruthy(right);
            case OR:
                return isTruthy(left) || isTruthy(right);
                
            // String operators
            case CONCAT:
                return String.valueOf(left) + String.valueOf(right);
            case LIKE:
                return likeMatch(String.valueOf(left), String.valueOf(right));
                
            // JSONB operators (simplified implementation)
            case JSONB_CONTAINS:
            case JSONB_CONTAINED:
            case JSONB_EXISTS:
            case JSONB_EXTRACT:
            case JSONB_EXTRACT_TEXT:
            case JSONB_PATH_EXTRACT:
            case JSONB_PATH_EXTRACT_TEXT:
                throw new UnsupportedOperationException("JSONB operators not yet implemented");
                
            default:
                throw new IllegalArgumentException("Unsupported binary operator: " + binary.getOperator());
        }
    }
    
    private Object evaluateUnaryExpression(UnaryExpression unary, ExecutionContext context) {
        Object operand = evaluate(unary.getOperand(), context);
        
        switch (unary.getOperator()) {
            case NOT:
                return !isTruthy(operand);
            case MINUS:
                if (operand instanceof Number) {
                    if (operand instanceof Integer) {
                        return -((Integer) operand);
                    } else if (operand instanceof Long) {
                        return -((Long) operand);
                    } else if (operand instanceof Double) {
                        return -((Double) operand);
                    } else if (operand instanceof Float) {
                        return -((Float) operand);
                    }
                }
                throw new IllegalArgumentException("Cannot apply MINUS to non-numeric value");
            case PLUS:
                return operand; // Unary plus doesn't change the value
            default:
                throw new IllegalArgumentException("Unsupported unary operator: " + unary.getOperator());
        }
    }
    
    private Object evaluateFunctionCall(FunctionCall function, ExecutionContext context) {
        String functionName = function.getFunctionName().toLowerCase();
        List<Expression> arguments = function.getArguments();
        
        switch (functionName) {
            case "gen_random_uuid":
                return UuidFunctions.genRandomUuid();
            case "uuid_generate_v1":
                return UuidFunctions.uuidGenerateV1();
            case "uuid_generate_v4":
                return UuidFunctions.uuidGenerateV4();
            
            // System Functions
            case "database":
                return evaluateDatabaseFunction();
            case "user":
            case "current_user":
            case "session_user":
                return evaluateUserFunction();
            case "session_id":
                return evaluateSessionIdFunction();
                
            // Math Functions
            case "sqrt":
                return evaluateSqrtFunction(arguments, context);
            case "power":
                return evaluatePowerFunction(arguments, context);
            case "abs":
                return evaluateAbsFunction(arguments, context);
            case "round":
                return evaluateRoundFunction(arguments, context);
            case "rand":
                return evaluateRandFunction();
                
            default:
                throw new UnsupportedOperationException("Function not supported: " + functionName);
        }
    }
    
    // System Function Implementations
    private String evaluateDatabaseFunction() {
        // Return the current database name - using "memgres" as our database name
        return "memgres";
    }
    
    private String evaluateUserFunction() {
        // Return the current user name - using "sa" as default H2 system admin user
        return "sa";
    }
    
    private Long evaluateSessionIdFunction() {
        // Return a unique session ID - using current thread ID as a simple implementation
        return Thread.currentThread().getId();
    }
    
    // Math Function Implementations
    private Double evaluateSqrtFunction(List<Expression> arguments, ExecutionContext context) {
        if (arguments.size() != 1) {
            throw new IllegalArgumentException("SQRT function requires exactly 1 argument");
        }
        
        Object value = evaluate(arguments.get(0), context);
        if (value == null) return null;
        
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException("SQRT function requires a numeric argument");
        }
        
        double numericValue = ((Number) value).doubleValue();
        if (numericValue < 0) {
            throw new ArithmeticException("SQRT function cannot be applied to negative numbers");
        }
        
        return Math.sqrt(numericValue);
    }
    
    private Double evaluatePowerFunction(List<Expression> arguments, ExecutionContext context) {
        if (arguments.size() != 2) {
            throw new IllegalArgumentException("POWER function requires exactly 2 arguments");
        }
        
        Object baseValue = evaluate(arguments.get(0), context);
        Object exponentValue = evaluate(arguments.get(1), context);
        
        if (baseValue == null || exponentValue == null) return null;
        
        if (!(baseValue instanceof Number) || !(exponentValue instanceof Number)) {
            throw new IllegalArgumentException("POWER function requires numeric arguments");
        }
        
        double base = ((Number) baseValue).doubleValue();
        double exponent = ((Number) exponentValue).doubleValue();
        
        return Math.pow(base, exponent);
    }
    
    private Object evaluateAbsFunction(List<Expression> arguments, ExecutionContext context) {
        if (arguments.size() != 1) {
            throw new IllegalArgumentException("ABS function requires exactly 1 argument");
        }
        
        Object value = evaluate(arguments.get(0), context);
        if (value == null) return null;
        
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException("ABS function requires a numeric argument");
        }
        
        // Preserve original type for integers
        if (value instanceof Integer) {
            return Math.abs((Integer) value);
        } else if (value instanceof Long) {
            return Math.abs((Long) value);
        } else if (value instanceof Float) {
            return Math.abs((Float) value);
        } else if (value instanceof Double) {
            return Math.abs((Double) value);
        } else {
            // For other numeric types, convert to double
            return Math.abs(((Number) value).doubleValue());
        }
    }
    
    private Object evaluateRoundFunction(List<Expression> arguments, ExecutionContext context) {
        if (arguments.size() < 1 || arguments.size() > 2) {
            throw new IllegalArgumentException("ROUND function requires 1 or 2 arguments");
        }
        
        Object value = evaluate(arguments.get(0), context);
        if (value == null) return null;
        
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException("ROUND function requires a numeric first argument");
        }
        
        double numericValue = ((Number) value).doubleValue();
        
        // Default precision is 0 (round to integer)
        int precision = 0;
        if (arguments.size() == 2) {
            Object precisionValue = evaluate(arguments.get(1), context);
            if (precisionValue == null) return null;
            
            if (!(precisionValue instanceof Number)) {
                throw new IllegalArgumentException("ROUND function requires a numeric second argument");
            }
            precision = ((Number) precisionValue).intValue();
        }
        
        // Use BigDecimal for precise rounding
        java.math.BigDecimal bd = java.math.BigDecimal.valueOf(numericValue);
        bd = bd.setScale(precision, java.math.RoundingMode.HALF_EVEN);
        
        // Return appropriate type
        if (precision == 0) {
            return bd.longValue();
        } else {
            return bd.doubleValue();
        }
    }
    
    private Double evaluateRandFunction() {
        return Math.random();
    }
    
    private Object evaluateSubqueryExpression(SubqueryExpression subquery, ExecutionContext context) {
        try {
            // Execute the subquery using StatementExecutor with current execution context for correlated subqueries
            StatementExecutor executor = new StatementExecutor(engine);
            SqlExecutionResult result = executor.execute(subquery.getSelectStatement(), context);
            
            if (result.getType() != SqlExecutionResult.ResultType.SELECT) {
                throw new IllegalStateException("Subquery must return a SELECT result");
            }
            
            List<Row> rows = result.getRows();
            if (rows.isEmpty()) {
                return null; // Empty result set returns NULL
            }
            
            if (rows.size() > 1) {
                throw new IllegalStateException("Scalar subquery returned more than one row");
            }
            
            Row row = rows.get(0);
            Object[] data = row.getData();
            if (data.length != 1) {
                throw new IllegalStateException("Scalar subquery must return exactly one column");
            }
            
            return data[0];
        } catch (SqlExecutionException e) {
            throw new RuntimeException("Failed to execute subquery", e);
        }
    }
    
    private Object evaluateExistsExpression(ExistsExpression exists, ExecutionContext context) {
        try {
            // Execute the subquery using StatementExecutor
            StatementExecutor executor = new StatementExecutor(engine);
            SqlExecutionResult result = executor.execute(exists.getSubquery());
            
            if (result.getType() != SqlExecutionResult.ResultType.SELECT) {
                throw new IllegalStateException("EXISTS subquery must return a SELECT result");
            }
            
            // EXISTS returns true if at least one row is returned, false otherwise
            return !result.getRows().isEmpty();
        } catch (SqlExecutionException e) {
            throw new RuntimeException("Failed to execute EXISTS subquery", e);
        }
    }
    
    private Object evaluateInSubqueryExpression(InSubqueryExpression inSubquery, ExecutionContext context) {
        // First evaluate the left-hand expression
        Object leftValue = evaluate(inSubquery.getExpression(), context);
        
        try {
            // Execute the subquery using StatementExecutor
            StatementExecutor executor = new StatementExecutor(engine);
            SqlExecutionResult result = executor.execute(inSubquery.getSubquery());
            
            if (result.getType() != SqlExecutionResult.ResultType.SELECT) {
                throw new IllegalStateException("IN subquery must return a SELECT result");
            }
            
            List<Row> rows = result.getRows();
            
            // Check if the left value exists in any of the result rows
            boolean found = false;
            for (Row row : rows) {
                Object[] data = row.getData();
                if (data.length != 1) {
                    throw new IllegalStateException("IN subquery must return exactly one column");
                }
                
                if (equalValues(leftValue, data[0])) {
                    found = true;
                    break;
                }
            }
            
            // Apply negation if needed
            return inSubquery.isNegated() ? !found : found;
        } catch (SqlExecutionException e) {
            throw new RuntimeException("Failed to execute IN subquery", e);
        }
    }
    
    // Helper methods for operations
    private Object addValues(Object left, Object right) {
        if (left instanceof Number && right instanceof Number) {
            Number leftNum = (Number) left;
            Number rightNum = (Number) right;
            
            // Handle BigDecimal operations to preserve precision
            if (left instanceof java.math.BigDecimal || right instanceof java.math.BigDecimal) {
                java.math.BigDecimal leftBD = (left instanceof java.math.BigDecimal) ? 
                    (java.math.BigDecimal) left : java.math.BigDecimal.valueOf(leftNum.doubleValue());
                java.math.BigDecimal rightBD = (right instanceof java.math.BigDecimal) ? 
                    (java.math.BigDecimal) right : java.math.BigDecimal.valueOf(rightNum.doubleValue());
                return leftBD.add(rightBD);
            } else if (left instanceof Double || right instanceof Double) {
                return leftNum.doubleValue() + rightNum.doubleValue();
            } else if (left instanceof Float || right instanceof Float) {
                return leftNum.floatValue() + rightNum.floatValue();
            } else if (left instanceof Long || right instanceof Long) {
                return leftNum.longValue() + rightNum.longValue();
            } else {
                return leftNum.intValue() + rightNum.intValue();
            }
        }
        return String.valueOf(left) + String.valueOf(right);
    }
    
    private Object subtractValues(Object left, Object right) {
        if (left instanceof Number && right instanceof Number) {
            Number leftNum = (Number) left;
            Number rightNum = (Number) right;
            
            // Handle BigDecimal operations to preserve precision
            if (left instanceof java.math.BigDecimal || right instanceof java.math.BigDecimal) {
                java.math.BigDecimal leftBD = (left instanceof java.math.BigDecimal) ? 
                    (java.math.BigDecimal) left : java.math.BigDecimal.valueOf(leftNum.doubleValue());
                java.math.BigDecimal rightBD = (right instanceof java.math.BigDecimal) ? 
                    (java.math.BigDecimal) right : java.math.BigDecimal.valueOf(rightNum.doubleValue());
                return leftBD.subtract(rightBD);
            } else if (left instanceof Double || right instanceof Double) {
                return leftNum.doubleValue() - rightNum.doubleValue();
            } else if (left instanceof Float || right instanceof Float) {
                return leftNum.floatValue() - rightNum.floatValue();
            } else if (left instanceof Long || right instanceof Long) {
                return leftNum.longValue() - rightNum.longValue();
            } else {
                return leftNum.intValue() - rightNum.intValue();
            }
        }
        throw new IllegalArgumentException("Cannot subtract non-numeric values");
    }
    
    private Object multiplyValues(Object left, Object right) {
        if (left instanceof Number && right instanceof Number) {
            Number leftNum = (Number) left;
            Number rightNum = (Number) right;
            
            if (left instanceof Double || right instanceof Double) {
                return leftNum.doubleValue() * rightNum.doubleValue();
            } else if (left instanceof Float || right instanceof Float) {
                return leftNum.floatValue() * rightNum.floatValue();
            } else if (left instanceof Long || right instanceof Long) {
                return leftNum.longValue() * rightNum.longValue();
            } else {
                return leftNum.intValue() * rightNum.intValue();
            }
        }
        throw new IllegalArgumentException("Cannot multiply non-numeric values");
    }
    
    private Object divideValues(Object left, Object right) {
        if (left instanceof Number && right instanceof Number) {
            Number leftNum = (Number) left;
            Number rightNum = (Number) right;
            
            if (rightNum.doubleValue() == 0) {
                throw new ArithmeticException("Division by zero");
            }
            
            return leftNum.doubleValue() / rightNum.doubleValue();
        }
        throw new IllegalArgumentException("Cannot divide non-numeric values");
    }
    
    private Object moduloValues(Object left, Object right) {
        if (left instanceof Number && right instanceof Number) {
            Number leftNum = (Number) left;
            Number rightNum = (Number) right;
            
            if (rightNum.doubleValue() == 0) {
                throw new ArithmeticException("Division by zero");
            }
            
            return leftNum.doubleValue() % rightNum.doubleValue();
        }
        throw new IllegalArgumentException("Cannot modulo non-numeric values");
    }
    
    private boolean equalValues(Object left, Object right) {
        if (left == null && right == null) return true;
        if (left == null || right == null) return false;
        
        // Handle numeric type conversions
        if (left instanceof Number && right instanceof Number) {
            // Convert both to the same numeric type for comparison
            double leftValue = ((Number) left).doubleValue();
            double rightValue = ((Number) right).doubleValue();
            return leftValue == rightValue;
        }
        
        return left.equals(right);
    }
    
    @SuppressWarnings("unchecked")
    private int compareValues(Object left, Object right) {
        if (left == null && right == null) return 0;
        if (left == null) return -1;
        if (right == null) return 1;
        
        if (left instanceof Comparable && right instanceof Comparable) {
            try {
                return ((Comparable) left).compareTo(right);
            } catch (ClassCastException e) {
                return left.toString().compareTo(right.toString());
            }
        }
        
        return left.toString().compareTo(right.toString());
    }
    
    private boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).doubleValue() != 0;
        if (value instanceof String) return !((String) value).isEmpty();
        return true;
    }
    
    private boolean likeMatch(String text, String pattern) {
        // Simple LIKE implementation - convert SQL LIKE pattern to regex
        String regexPattern = pattern
            .replace("%", ".*")
            .replace("_", ".");
        return text.matches(regexPattern);
    }
    
    /**
     * Evaluate NEXT VALUE FOR sequence_name expression.
     */
    private Object evaluateNextValueForExpression(NextValueForExpression expr, ExecutionContext context) {
        try {
            String sequenceName = expr.getSequenceName();
            logger.debug("Evaluating NEXT VALUE FOR: {}", sequenceName);
            
            // Get the sequence from the engine
            com.memgres.storage.Sequence sequence = engine.getSequence("public", sequenceName);
            if (sequence == null) {
                throw new IllegalArgumentException("Sequence does not exist: " + sequenceName);
            }
            
            // Get the next value
            long nextValue = sequence.nextValue();
            logger.debug("NEXT VALUE FOR {} returned: {}", sequenceName, nextValue);
            
            return nextValue;
            
        } catch (com.memgres.storage.Sequence.SequenceException e) {
            throw new IllegalStateException("Sequence error for " + expr.getSequenceName() + ": " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Failed to evaluate NEXT VALUE FOR {}: {}", expr.getSequenceName(), e.getMessage());
            throw new IllegalStateException("Failed to evaluate NEXT VALUE FOR: " + e.getMessage(), e);
        }
    }
    
    /**
     * Evaluate CURRENT VALUE FOR sequence_name expression.
     */
    private Object evaluateCurrentValueForExpression(CurrentValueForExpression expr, ExecutionContext context) {
        try {
            String sequenceName = expr.getSequenceName();
            logger.debug("Evaluating CURRENT VALUE FOR: {}", sequenceName);
            
            // Get the sequence from the engine
            com.memgres.storage.Sequence sequence = engine.getSequence("public", sequenceName);
            if (sequence == null) {
                throw new IllegalArgumentException("Sequence does not exist: " + sequenceName);
            }
            
            // Get the current value
            long currentValue = sequence.currentValue();
            logger.debug("CURRENT VALUE FOR {} returned: {}", sequenceName, currentValue);
            
            return currentValue;
            
        } catch (com.memgres.storage.Sequence.SequenceException e) {
            throw new IllegalStateException("Sequence error for " + expr.getSequenceName() + ": " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Failed to evaluate CURRENT VALUE FOR {}: {}", expr.getSequenceName(), e.getMessage());
            throw new IllegalStateException("Failed to evaluate CURRENT VALUE FOR: " + e.getMessage(), e);
        }
    }
}