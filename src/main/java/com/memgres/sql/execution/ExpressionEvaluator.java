package com.memgres.sql.execution;

import com.memgres.core.MemGresEngine;
import com.memgres.functions.StringFunctions;
import com.memgres.functions.UuidFunctions;
import com.memgres.types.jsonb.JsonbValue;
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
import com.memgres.sql.ast.expression.OverClause;
import com.memgres.sql.ast.expression.SubqueryExpression;
import com.memgres.sql.ast.expression.UnaryExpression;
import com.memgres.sql.ast.expression.WindowFunction;
import com.memgres.storage.Table;
import com.memgres.types.Column;
import com.memgres.types.Row;

import java.util.List;

import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

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
        else if (expression instanceof WindowFunction) {
            throw new IllegalStateException("Window functions must be handled at the SELECT statement level");
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
            case JSONB_CONTAINS: // @> operator
                if (left instanceof JsonbValue && right instanceof JsonbValue) {
                    return ((JsonbValue) left).contains((JsonbValue) right);
                }
                throw new IllegalArgumentException("JSONB_CONTAINS requires JSONB operands");
                
            case JSONB_CONTAINED: // <@ operator
                if (left instanceof JsonbValue && right instanceof JsonbValue) {
                    return ((JsonbValue) left).containedBy((JsonbValue) right);
                }
                throw new IllegalArgumentException("JSONB_CONTAINED requires JSONB operands");
                
            case JSONB_EXISTS: // ? operator
                if (left instanceof JsonbValue && right instanceof String) {
                    return ((JsonbValue) left).hasKey((String) right);
                }
                throw new IllegalArgumentException("JSONB_EXISTS requires JSONB and string operands");
                
            case JSONB_EXTRACT: // -> operator (returns JSONB)
                if (left instanceof JsonbValue) {
                    if (right instanceof String) {
                        return ((JsonbValue) left).getField((String) right);
                    } else if (right instanceof Number) {
                        return ((JsonbValue) left).getElement(((Number) right).intValue());
                    }
                }
                throw new IllegalArgumentException("JSONB_EXTRACT requires JSONB left operand and string/int right operand");
                
            case JSONB_EXTRACT_TEXT: // ->> operator (returns text)
                if (left instanceof JsonbValue) {
                    if (right instanceof String) {
                        return ((JsonbValue) left).getFieldAsText((String) right);
                    } else if (right instanceof Number) {
                        return ((JsonbValue) left).getElementAsText(((Number) right).intValue());
                    }
                }
                throw new IllegalArgumentException("JSONB_EXTRACT_TEXT requires JSONB left operand and string/int right operand");
                
            case JSONB_PATH_EXTRACT: // #> operator (returns JSONB)
                if (left instanceof JsonbValue && right instanceof List) {
                    List<?> pathList = (List<?>) right;
                    String[] path = pathList.stream()
                        .map(Object::toString)
                        .toArray(String[]::new);
                    return ((JsonbValue) left).getPath(path);
                }
                throw new IllegalArgumentException("JSONB_PATH_EXTRACT requires JSONB left operand and array right operand");
                
            case JSONB_PATH_EXTRACT_TEXT: // #>> operator (returns text)
                if (left instanceof JsonbValue && right instanceof List) {
                    List<?> pathList = (List<?>) right;
                    String[] path = pathList.stream()
                        .map(Object::toString)
                        .toArray(String[]::new);
                    return ((JsonbValue) left).getPathAsText(path);
                }
                throw new IllegalArgumentException("JSONB_PATH_EXTRACT_TEXT requires JSONB left operand and array right operand");
                
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
                
            // H2 String Functions
            case "regexp_replace":
                return evaluateRegexpReplaceFunction(arguments, context);
            case "soundex":
                return evaluateSoundexFunction(arguments, context);
            case "regexp_like":
                return evaluateRegexpLikeFunction(arguments, context);
            case "regexp_substr":
                return evaluateRegexpSubstrFunction(arguments, context);
            case "initcap":
                return evaluateInitcapFunction(arguments, context);
                
            // H2 Date/Time Functions
            case "current_timestamp":
                return evaluateCurrentTimestampFunction();
            case "current_date":
                return evaluateCurrentDateFunction();
            case "current_time":
                return evaluateCurrentTimeFunction();
            case "dateadd":
                return evaluateDateAddFunction(arguments, context);
            case "datediff":
                return evaluateDateDiffFunction(arguments, context);
            case "formatdatetime":
                return evaluateFormatDateTimeFunction(arguments, context);
            case "parsedatetime":
                return evaluateParseDateTimeFunction(arguments, context);
                
            // H2 System Functions
            case "h2version":
                return evaluateH2VersionFunction();
            case "database_path":
                return evaluateDatabasePathFunction();
            case "memory_used":
                return evaluateMemoryUsedFunction();
            case "memory_free":
                return evaluateMemoryFreeFunction();
                
            // H2 String Utility Functions
            case "left":
                return evaluateLeftFunction(arguments, context);
            case "right":
                return evaluateRightFunction(arguments, context);
            case "position":
                return evaluatePositionFunction(arguments, context);
            case "ascii":
                return evaluateAsciiFunction(arguments, context);
            case "char":
                return evaluateCharFunction(arguments, context);
            case "hextoraw":
                return evaluateHexToRawFunction(arguments, context);
            case "rawtohex":
                return evaluateRawToHexFunction(arguments, context);
                
            // Full-Text Search Functions
            case "ft_init":
                return evaluateFtInitFunction();
            case "ft_create_index":
                return evaluateFtCreateIndexFunction(arguments, context);
            case "ft_drop_index":
                return evaluateFtDropIndexFunction(arguments, context);
            case "ft_search":
                return evaluateFtSearchFunction(arguments, context);
            case "ft_reindex":
                return evaluateFtReindexFunction();
            case "ft_drop_all":
                return evaluateFtDropAllFunction();
            case "ft_set_ignore_list":
                return evaluateFtSetIgnoreListFunction(arguments, context);
            case "ft_set_whitespace_chars":
                return evaluateFtSetWhitespaceCharsFunction(arguments, context);
                
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
    
    // H2 String Function Implementations
    
    /**
     * Evaluate REGEXP_REPLACE function.
     */
    private String evaluateRegexpReplaceFunction(List<Expression> arguments, ExecutionContext context) {
        if (arguments.size() < 3 || arguments.size() > 4) {
            throw new IllegalArgumentException("REGEXP_REPLACE function requires 3 or 4 arguments");
        }
        
        Object inputValue = evaluate(arguments.get(0), context);
        Object regexValue = evaluate(arguments.get(1), context);
        Object replacementValue = evaluate(arguments.get(2), context);
        Object flagsValue = arguments.size() > 3 ? evaluate(arguments.get(3), context) : null;
        
        if (inputValue == null || regexValue == null) {
            return (String) inputValue;
        }
        
        String inputString = inputValue.toString();
        String regexString = regexValue.toString();
        String replacementString = replacementValue != null ? replacementValue.toString() : "";
        String flagsString = flagsValue != null ? flagsValue.toString() : null;
        
        return StringFunctions.regexpReplace(inputString, regexString, replacementString, flagsString);
    }
    
    /**
     * Evaluate SOUNDEX function.
     */
    private String evaluateSoundexFunction(List<Expression> arguments, ExecutionContext context) {
        if (arguments.size() != 1) {
            throw new IllegalArgumentException("SOUNDEX function requires exactly 1 argument");
        }
        
        Object value = evaluate(arguments.get(0), context);
        if (value == null) {
            return null;
        }
        
        String inputString = value.toString();
        return StringFunctions.soundex(inputString);
    }
    
    /**
     * Evaluate REGEXP_LIKE function.
     */
    private Boolean evaluateRegexpLikeFunction(List<Expression> arguments, ExecutionContext context) {
        if (arguments.size() < 2 || arguments.size() > 3) {
            throw new IllegalArgumentException("REGEXP_LIKE function requires 2 or 3 arguments");
        }
        
        Object inputValue = evaluate(arguments.get(0), context);
        Object regexValue = evaluate(arguments.get(1), context);
        Object flagsValue = arguments.size() > 2 ? evaluate(arguments.get(2), context) : null;
        
        if (inputValue == null || regexValue == null) {
            return null;
        }
        
        String inputString = inputValue.toString();
        String regexString = regexValue.toString();
        String flagsString = flagsValue != null ? flagsValue.toString() : null;
        
        return StringFunctions.regexpLike(inputString, regexString, flagsString);
    }
    
    /**
     * Evaluate REGEXP_SUBSTR function.
     */
    private String evaluateRegexpSubstrFunction(List<Expression> arguments, ExecutionContext context) {
        if (arguments.size() < 2 || arguments.size() > 5) {
            throw new IllegalArgumentException("REGEXP_SUBSTR function requires 2 to 5 arguments");
        }
        
        Object inputValue = evaluate(arguments.get(0), context);
        Object regexValue = evaluate(arguments.get(1), context);
        
        if (inputValue == null || regexValue == null) {
            return null;
        }
        
        String inputString = inputValue.toString();
        String regexString = regexValue.toString();
        
        if (arguments.size() == 2) {
            return StringFunctions.regexpSubstr(inputString, regexString);
        }
        
        Object positionValue = arguments.size() > 2 ? evaluate(arguments.get(2), context) : 1;
        Object occurrenceValue = arguments.size() > 3 ? evaluate(arguments.get(3), context) : 1;
        Object flagsValue = arguments.size() > 4 ? evaluate(arguments.get(4), context) : null;
        
        Integer position = positionValue instanceof Number ? ((Number) positionValue).intValue() : 1;
        Integer occurrence = occurrenceValue instanceof Number ? ((Number) occurrenceValue).intValue() : 1;
        String flagsString = flagsValue != null ? flagsValue.toString() : null;
        
        return StringFunctions.regexpSubstr(inputString, regexString, position, occurrence, flagsString);
    }
    
    /**
     * Evaluate INITCAP function.
     */
    private String evaluateInitcapFunction(List<Expression> arguments, ExecutionContext context) {
        if (arguments.size() != 1) {
            throw new IllegalArgumentException("INITCAP function requires exactly 1 argument");
        }
        
        Object value = evaluate(arguments.get(0), context);
        if (value == null) {
            return null;
        }
        
        String inputString = value.toString();
        return StringFunctions.initcap(inputString);
    }
    
    // ===== H2 DATE/TIME FUNCTION IMPLEMENTATIONS =====
    
    /**
     * Evaluate CURRENT_TIMESTAMP function.
     */
    private LocalDateTime evaluateCurrentTimestampFunction() {
        return LocalDateTime.now();
    }
    
    /**
     * Evaluate CURRENT_DATE function.
     */
    private LocalDate evaluateCurrentDateFunction() {
        return LocalDate.now();
    }
    
    /**
     * Evaluate CURRENT_TIME function.
     */
    private LocalTime evaluateCurrentTimeFunction() {
        return LocalTime.now();
    }
    
    /**
     * Evaluate DATEADD function.
     * DATEADD(unit, amount, date)
     */
    private Object evaluateDateAddFunction(List<Expression> arguments, ExecutionContext context) {
        if (arguments.size() != 3) {
            throw new IllegalArgumentException("DATEADD function requires exactly 3 arguments");
        }
        
        Object unitValue = evaluate(arguments.get(0), context);
        Object amountValue = evaluate(arguments.get(1), context);
        Object dateValue = evaluate(arguments.get(2), context);
        
        if (unitValue == null || amountValue == null || dateValue == null) {
            return null;
        }
        
        String unit = unitValue.toString().toUpperCase();
        long amount = ((Number) amountValue).longValue();
        
        if (dateValue instanceof LocalDateTime) {
            LocalDateTime dateTime = (LocalDateTime) dateValue;
            return addToDateTime(dateTime, unit, amount);
        } else if (dateValue instanceof LocalDate) {
            LocalDate date = (LocalDate) dateValue;
            LocalDateTime dateTime = date.atStartOfDay();
            LocalDateTime result = addToDateTime(dateTime, unit, amount);
            // For date operations, return date if time part is unchanged
            if (result.toLocalTime().equals(LocalTime.MIDNIGHT)) {
                return result.toLocalDate();
            }
            return result;
        } else if (dateValue instanceof LocalTime) {
            LocalTime time = (LocalTime) dateValue;
            if (unit.equals("HOUR") || unit.equals("MINUTE") || unit.equals("SECOND")) {
                return addToTime(time, unit, amount);
            } else {
                throw new IllegalArgumentException("Cannot add " + unit + " to TIME value");
            }
        }
        
        throw new IllegalArgumentException("DATEADD requires a date/time value");
    }
    
    private LocalDateTime addToDateTime(LocalDateTime dateTime, String unit, long amount) {
        switch (unit) {
            case "YEAR":
                return dateTime.plusYears(amount);
            case "MONTH":
                return dateTime.plusMonths(amount);
            case "DAY":
                return dateTime.plusDays(amount);
            case "HOUR":
                return dateTime.plusHours(amount);
            case "MINUTE":
                return dateTime.plusMinutes(amount);
            case "SECOND":
                return dateTime.plusSeconds(amount);
            case "MILLISECOND":
                return dateTime.plus(amount, ChronoUnit.MILLIS);
            default:
                throw new IllegalArgumentException("Unsupported DATEADD unit: " + unit);
        }
    }
    
    private LocalTime addToTime(LocalTime time, String unit, long amount) {
        switch (unit) {
            case "HOUR":
                return time.plusHours(amount);
            case "MINUTE":
                return time.plusMinutes(amount);
            case "SECOND":
                return time.plusSeconds(amount);
            default:
                throw new IllegalArgumentException("Unsupported time unit: " + unit);
        }
    }
    
    /**
     * Evaluate DATEDIFF function.
     * DATEDIFF(unit, start_date, end_date)
     */
    private Long evaluateDateDiffFunction(List<Expression> arguments, ExecutionContext context) {
        if (arguments.size() != 3) {
            throw new IllegalArgumentException("DATEDIFF function requires exactly 3 arguments");
        }
        
        Object unitValue = evaluate(arguments.get(0), context);
        Object startValue = evaluate(arguments.get(1), context);
        Object endValue = evaluate(arguments.get(2), context);
        
        if (unitValue == null || startValue == null || endValue == null) {
            return null;
        }
        
        String unit = unitValue.toString().toUpperCase();
        
        LocalDateTime startDateTime = convertToDateTime(startValue);
        LocalDateTime endDateTime = convertToDateTime(endValue);
        
        switch (unit) {
            case "YEAR":
                return ChronoUnit.YEARS.between(startDateTime, endDateTime);
            case "MONTH":
                return ChronoUnit.MONTHS.between(startDateTime, endDateTime);
            case "DAY":
                return ChronoUnit.DAYS.between(startDateTime, endDateTime);
            case "HOUR":
                return ChronoUnit.HOURS.between(startDateTime, endDateTime);
            case "MINUTE":
                return ChronoUnit.MINUTES.between(startDateTime, endDateTime);
            case "SECOND":
                return ChronoUnit.SECONDS.between(startDateTime, endDateTime);
            case "MILLISECOND":
                return ChronoUnit.MILLIS.between(startDateTime, endDateTime);
            default:
                throw new IllegalArgumentException("Unsupported DATEDIFF unit: " + unit);
        }
    }
    
    private LocalDateTime convertToDateTime(Object value) {
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        } else if (value instanceof LocalDate) {
            return ((LocalDate) value).atStartOfDay();
        } else if (value instanceof LocalTime) {
            return LocalDate.now().atTime((LocalTime) value);
        } else {
            throw new IllegalArgumentException("Cannot convert to DateTime: " + value.getClass());
        }
    }
    
    /**
     * Evaluate FORMATDATETIME function.
     * FORMATDATETIME(date, pattern)
     */
    private String evaluateFormatDateTimeFunction(List<Expression> arguments, ExecutionContext context) {
        if (arguments.size() != 2) {
            throw new IllegalArgumentException("FORMATDATETIME function requires exactly 2 arguments");
        }
        
        Object dateValue = evaluate(arguments.get(0), context);
        Object patternValue = evaluate(arguments.get(1), context);
        
        if (dateValue == null || patternValue == null) {
            return null;
        }
        
        String pattern = patternValue.toString();
        
        try {
            if (dateValue instanceof LocalDateTime) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return ((LocalDateTime) dateValue).format(formatter);
            } else if (dateValue instanceof LocalDate) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return ((LocalDate) dateValue).format(formatter);
            } else if (dateValue instanceof LocalTime) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return ((LocalTime) dateValue).format(formatter);
            } else {
                throw new IllegalArgumentException("FORMATDATETIME requires a date/time value");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format pattern: " + pattern, e);
        }
    }
    
    /**
     * Evaluate PARSEDATETIME function.
     * PARSEDATETIME(string, pattern)
     */
    private LocalDateTime evaluateParseDateTimeFunction(List<Expression> arguments, ExecutionContext context) {
        if (arguments.size() != 2) {
            throw new IllegalArgumentException("PARSEDATETIME function requires exactly 2 arguments");
        }
        
        Object stringValue = evaluate(arguments.get(0), context);
        Object patternValue = evaluate(arguments.get(1), context);
        
        if (stringValue == null || patternValue == null) {
            return null;
        }
        
        String dateString = stringValue.toString();
        String pattern = patternValue.toString();
        
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return LocalDateTime.parse(dateString, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Cannot parse date '" + dateString + "' with pattern '" + pattern + "'", e);
        }
    }
    
    // ===== H2 SYSTEM FUNCTION IMPLEMENTATIONS =====
    
    /**
     * Evaluate H2VERSION function.
     */
    private String evaluateH2VersionFunction() {
        // Return MemGres version with H2 compatibility note
        return "MemGres 1.0.0 (H2 Compatible)";
    }
    
    /**
     * Evaluate DATABASE_PATH function.
     */
    private String evaluateDatabasePathFunction() {
        // For in-memory database, return a virtual path
        return "mem:memgres";
    }
    
    /**
     * Evaluate MEMORY_USED function.
     */
    private Long evaluateMemoryUsedFunction() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        return totalMemory - freeMemory;
    }
    
    /**
     * Evaluate MEMORY_FREE function.
     */
    private Long evaluateMemoryFreeFunction() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.freeMemory();
    }
    
    // ===== H2 STRING UTILITY FUNCTION IMPLEMENTATIONS =====
    
    /**
     * Evaluate LEFT function.
     * LEFT(string, length)
     */
    private String evaluateLeftFunction(List<Expression> arguments, ExecutionContext context) {
        if (arguments.size() != 2) {
            throw new IllegalArgumentException("LEFT function requires exactly 2 arguments");
        }
        
        Object stringValue = evaluate(arguments.get(0), context);
        Object lengthValue = evaluate(arguments.get(1), context);
        
        if (stringValue == null) {
            return null;
        }
        
        if (lengthValue == null || !(lengthValue instanceof Number)) {
            throw new IllegalArgumentException("LEFT function requires a numeric length argument");
        }
        
        String inputString = stringValue.toString();
        int length = ((Number) lengthValue).intValue();
        
        return StringFunctions.left(inputString, length);
    }
    
    /**
     * Evaluate RIGHT function.
     * RIGHT(string, length)
     */
    private String evaluateRightFunction(List<Expression> arguments, ExecutionContext context) {
        if (arguments.size() != 2) {
            throw new IllegalArgumentException("RIGHT function requires exactly 2 arguments");
        }
        
        Object stringValue = evaluate(arguments.get(0), context);
        Object lengthValue = evaluate(arguments.get(1), context);
        
        if (stringValue == null) {
            return null;
        }
        
        if (lengthValue == null || !(lengthValue instanceof Number)) {
            throw new IllegalArgumentException("RIGHT function requires a numeric length argument");
        }
        
        String inputString = stringValue.toString();
        int length = ((Number) lengthValue).intValue();
        
        return StringFunctions.right(inputString, length);
    }
    
    /**
     * Evaluate POSITION function.
     * POSITION(substring, string)
     */
    private Integer evaluatePositionFunction(List<Expression> arguments, ExecutionContext context) {
        if (arguments.size() != 2) {
            throw new IllegalArgumentException("POSITION function requires exactly 2 arguments");
        }
        
        Object substringValue = evaluate(arguments.get(0), context);
        Object stringValue = evaluate(arguments.get(1), context);
        
        if (substringValue == null || stringValue == null) {
            return null;
        }
        
        String substring = substringValue.toString();
        String string = stringValue.toString();
        
        return StringFunctions.position(substring, string);
    }
    
    /**
     * Evaluate ASCII function.
     * ASCII(string)
     */
    private Integer evaluateAsciiFunction(List<Expression> arguments, ExecutionContext context) {
        if (arguments.size() != 1) {
            throw new IllegalArgumentException("ASCII function requires exactly 1 argument");
        }
        
        Object stringValue = evaluate(arguments.get(0), context);
        
        if (stringValue == null) {
            return null;
        }
        
        String inputString = stringValue.toString();
        if (inputString.isEmpty()) {
            return null;
        }
        
        return (int) inputString.charAt(0);
    }
    
    /**
     * Evaluate CHAR function.
     * CHAR(ascii_code)
     */
    private String evaluateCharFunction(List<Expression> arguments, ExecutionContext context) {
        if (arguments.size() != 1) {
            throw new IllegalArgumentException("CHAR function requires exactly 1 argument");
        }
        
        Object codeValue = evaluate(arguments.get(0), context);
        
        if (codeValue == null || !(codeValue instanceof Number)) {
            return null;
        }
        
        int asciiCode = ((Number) codeValue).intValue();
        
        if (asciiCode < 0 || asciiCode > 127) {
            throw new IllegalArgumentException("ASCII code must be between 0 and 127");
        }
        
        return String.valueOf((char) asciiCode);
    }
    
    /**
     * Evaluate HEXTORAW function.
     * HEXTORAW(hex_string)
     */
    private byte[] evaluateHexToRawFunction(List<Expression> arguments, ExecutionContext context) {
        if (arguments.size() != 1) {
            throw new IllegalArgumentException("HEXTORAW function requires exactly 1 argument");
        }
        
        Object hexValue = evaluate(arguments.get(0), context);
        
        if (hexValue == null) {
            return null;
        }
        
        String hexString = hexValue.toString().trim();
        
        if (hexString.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have even length");
        }
        
        try {
            byte[] result = new byte[hexString.length() / 2];
            for (int i = 0; i < result.length; i++) {
                int index = i * 2;
                int value = Integer.parseInt(hexString.substring(index, index + 2), 16);
                result[i] = (byte) value;
            }
            return result;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex string: " + hexString, e);
        }
    }
    
    /**
     * Evaluate RAWTOHEX function.
     * RAWTOHEX(byte_array)
     */
    private String evaluateRawToHexFunction(List<Expression> arguments, ExecutionContext context) {
        if (arguments.size() != 1) {
            throw new IllegalArgumentException("RAWTOHEX function requires exactly 1 argument");
        }
        
        Object rawValue = evaluate(arguments.get(0), context);
        
        if (rawValue == null) {
            return null;
        }
        
        byte[] bytes;
        if (rawValue instanceof byte[]) {
            bytes = (byte[]) rawValue;
        } else if (rawValue instanceof String) {
            bytes = ((String) rawValue).getBytes();
        } else {
            throw new IllegalArgumentException("RAWTOHEX requires byte array or string input");
        }
        
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X", b & 0xFF));
        }
        
        return result.toString();
    }
    
    // Full-Text Search Function Implementations
    private Object evaluateFtInitFunction() {
        try {
            com.memgres.functions.FullTextFunctions.ftInit(engine);
            return "Full-text search initialized";
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize full-text search", e);
        }
    }
    
    private Object evaluateFtCreateIndexFunction(List<Expression> arguments, ExecutionContext context) {
        if (arguments.size() != 3) {
            throw new IllegalArgumentException("FT_CREATE_INDEX requires exactly 3 arguments");
        }
        
        try {
            Object schemaValue = evaluate(arguments.get(0), context);
            Object tableValue = evaluate(arguments.get(1), context);
            Object columnListValue = evaluate(arguments.get(2), context);
            
            String schema = schemaValue != null ? schemaValue.toString() : "PUBLIC";
            String table = tableValue.toString();
            String columnList = columnListValue != null ? columnListValue.toString() : null;
            
            com.memgres.functions.FullTextFunctions.ftCreateIndex(schema, table, columnList);
            return "Full-text index created";
        } catch (Exception e) {
            throw new RuntimeException("Failed to create full-text index", e);
        }
    }
    
    private Object evaluateFtDropIndexFunction(List<Expression> arguments, ExecutionContext context) {
        if (arguments.size() != 2) {
            throw new IllegalArgumentException("FT_DROP_INDEX requires exactly 2 arguments");
        }
        
        Object schemaValue = evaluate(arguments.get(0), context);
        Object tableValue = evaluate(arguments.get(1), context);
        
        String schema = schemaValue != null ? schemaValue.toString() : "PUBLIC";
        String table = tableValue.toString();
        
        com.memgres.functions.FullTextFunctions.ftDropIndex(schema, table);
        return "Full-text index dropped";
    }
    
    private Object evaluateFtSearchFunction(List<Expression> arguments, ExecutionContext context) {
        if (arguments.size() != 3) {
            throw new IllegalArgumentException("FT_SEARCH requires exactly 3 arguments");
        }
        
        Object textValue = evaluate(arguments.get(0), context);
        Object limitValue = evaluate(arguments.get(1), context);
        Object offsetValue = evaluate(arguments.get(2), context);
        
        String text = textValue != null ? textValue.toString() : "";
        int limit = limitValue != null ? ((Number) limitValue).intValue() : 0;
        int offset = offsetValue != null ? ((Number) offsetValue).intValue() : 0;
        
        return com.memgres.functions.FullTextFunctions.ftSearch(text, limit, offset);
    }
    
    private Object evaluateFtReindexFunction() {
        try {
            com.memgres.functions.FullTextFunctions.ftReindex();
            return "Full-text indexes rebuilt";
        } catch (Exception e) {
            throw new RuntimeException("Failed to reindex full-text search", e);
        }
    }
    
    private Object evaluateFtDropAllFunction() {
        com.memgres.functions.FullTextFunctions.ftDropAll();
        return "All full-text indexes dropped";
    }
    
    private Object evaluateFtSetIgnoreListFunction(List<Expression> arguments, ExecutionContext context) {
        if (arguments.size() != 1) {
            throw new IllegalArgumentException("FT_SET_IGNORE_LIST requires exactly 1 argument");
        }
        
        Object ignoreListValue = evaluate(arguments.get(0), context);
        String ignoreList = ignoreListValue != null ? ignoreListValue.toString() : "";
        
        com.memgres.functions.FullTextFunctions.ftSetIgnoreList(ignoreList);
        return "Ignore list updated";
    }
    
    private Object evaluateFtSetWhitespaceCharsFunction(List<Expression> arguments, ExecutionContext context) {
        if (arguments.size() != 1) {
            throw new IllegalArgumentException("FT_SET_WHITESPACE_CHARS requires exactly 1 argument");
        }
        
        Object whitespaceValue = evaluate(arguments.get(0), context);
        String whitespaceChars = whitespaceValue != null ? whitespaceValue.toString() : " \t\n\r";
        
        com.memgres.functions.FullTextFunctions.ftSetWhitespaceChars(whitespaceChars);
        return "Whitespace characters updated";
    }
}