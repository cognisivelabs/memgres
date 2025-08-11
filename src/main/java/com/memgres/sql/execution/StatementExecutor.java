package com.memgres.sql.execution;

import com.memgres.core.MemGresEngine;
import com.memgres.functions.UuidFunctions;
import com.memgres.sql.ast.AstVisitor;
import com.memgres.sql.ast.expression.*;
import com.memgres.sql.ast.statement.*;
import com.memgres.storage.Schema;
import com.memgres.storage.Table;
import com.memgres.types.Column;
import com.memgres.types.DataType;
import com.memgres.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Executes SQL statement AST nodes against the MemGres storage layer.
 */
public class StatementExecutor implements AstVisitor<SqlExecutionResult, ExecutionContext> {
    private static final Logger logger = LoggerFactory.getLogger(StatementExecutor.class);
    
    private final MemGresEngine engine;
    private final ExpressionEvaluator expressionEvaluator;
    
    public StatementExecutor(MemGresEngine engine) {
        this.engine = engine;
        this.expressionEvaluator = new ExpressionEvaluator(engine);
    }
    
    /**
     * Execute a statement and return the result.
     */
    public SqlExecutionResult execute(Statement statement) throws SqlExecutionException {
        try {
            ExecutionContext context = new ExecutionContext();
            return statement.accept(this, context);
        } catch (Exception e) {
            if (e instanceof SqlExecutionException) {
                throw (SqlExecutionException) e;
            }
            throw new SqlExecutionException("Failed to execute statement", e);
        }
    }
    
    /**
     * Execute a statement with a provided execution context (for correlated subqueries).
     */
    public SqlExecutionResult execute(Statement statement, ExecutionContext context) throws SqlExecutionException {
        try {
            return statement.accept(this, context);
        } catch (Exception e) {
            if (e instanceof SqlExecutionException) {
                throw (SqlExecutionException) e;
            }
            throw new SqlExecutionException("Failed to execute statement", e);
        }
    }
    
    @Override
    public SqlExecutionResult visitSelectStatement(SelectStatement node, ExecutionContext context) throws SqlExecutionException {
        try {
            // Process FROM clause and execute joins (or create empty row for subqueries without FROM)
            JoinResult joinResult;
            if (node.getFromClause().isPresent()) {
                FromClause fromClause = node.getFromClause().get();
                joinResult = executeFromClause(fromClause, context);
            } else {
                // No FROM clause - create a single empty row for expression evaluation
                joinResult = new JoinResult(List.of(), List.of(new Row(0L, new Object[0])));
            }
            
            List<Row> filteredRows = joinResult.rows;
            
            // Apply WHERE clause if present
            if (node.getWhereClause().isPresent()) {
                WhereClause whereClause = node.getWhereClause().get();
                filteredRows = new ArrayList<>();
                
                for (Row row : joinResult.rows) {
                    context.setCurrentRow(row);
                    context.setJoinedColumns(joinResult.columns);
                    
                    Object result = expressionEvaluator.evaluate(whereClause.getCondition(), context);
                    
                    if (Boolean.TRUE.equals(result)) {
                        filteredRows.add(row);
                    }
                }
            }
            
            
            // Handle GROUP BY and aggregation
            List<Row> groupedRows = filteredRows;
            List<Column> groupedColumns = joinResult.columns;
            
            if (node.getGroupByClause().isPresent() || hasAggregateFunction(node.getSelectItems())) {
                AggregationResult aggregationResult = performAggregation(node, filteredRows, joinResult.columns, context);
                groupedRows = aggregationResult.rows;
                groupedColumns = aggregationResult.columns;
            }
            
            // Apply HAVING clause if present
            if (node.getHavingClause().isPresent()) {
                HavingClause havingClause = node.getHavingClause().get();
                List<Row> havingFilteredRows = new ArrayList<>();
                
                for (Row row : groupedRows) {
                    context.setCurrentRow(row);
                    context.setJoinedColumns(groupedColumns);
                    
                    Object result = expressionEvaluator.evaluate(havingClause.getCondition(), context);
                    if (Boolean.TRUE.equals(result)) {
                        havingFilteredRows.add(row);
                    }
                }
                groupedRows = havingFilteredRows;
            }
            
            // Apply ORDER BY if present (on grouped results)
            if (node.getOrderByClause().isPresent()) {
                OrderByClause orderBy = node.getOrderByClause().get();
                groupedRows = applyOrderBy(groupedRows, orderBy, context, groupedColumns);
            }
            
            // Apply LIMIT if present
            if (node.getLimitClause().isPresent()) {
                LimitClause limitClause = node.getLimitClause().get();
                int limit = evaluateIntExpression(limitClause.getLimit(), context);
                int offset = 0;
                if (limitClause.getOffset().isPresent()) {
                    offset = evaluateIntExpression(limitClause.getOffset().get(), context);
                }
                
                int fromIndex = Math.min(offset, groupedRows.size());
                int toIndex = Math.min(fromIndex + limit, groupedRows.size());
                groupedRows = groupedRows.subList(fromIndex, toIndex);
            }
            
            // Project columns based on SELECT list
            List<Column> resultColumns;
            List<Row> resultRows;
            
            if (node.getGroupByClause().isPresent() || hasAggregateFunction(node.getSelectItems())) {
                // Aggregation already handled projection - use results directly
                resultColumns = groupedColumns;
                resultRows = groupedRows;
            } else if (node.getSelectItems().size() == 1 && node.getSelectItems().get(0).isWildcard()) {
                // SELECT * - return all columns
                resultColumns = groupedColumns;
                resultRows = groupedRows;
            } else {
                // Project specific columns/expressions (non-aggregate case)
                resultColumns = new ArrayList<>();
                resultRows = new ArrayList<>();
                
                // Validate column references in SELECT items
                context.setJoinedColumns(groupedColumns);
                for (SelectItem item : node.getSelectItems()) {
                    validateColumnReferences(item.getExpression(), groupedColumns);
                }
                
                // Determine result columns
                for (int i = 0; i < node.getSelectItems().size(); i++) {
                    SelectItem item = node.getSelectItems().get(i);
                    String columnName;
                    if (item.getAlias().isPresent()) {
                        columnName = item.getAlias().get();
                    } else if (item.getExpression() instanceof com.memgres.sql.ast.expression.ColumnReference) {
                        com.memgres.sql.ast.expression.ColumnReference colRef = 
                            (com.memgres.sql.ast.expression.ColumnReference) item.getExpression();
                        columnName = colRef.getColumnName();
                    } else {
                        columnName = "column" + i;
                    }
                    resultColumns.add(new Column.Builder()
                    .name(columnName)
                    .dataType(DataType.TEXT)
                    .build());
                }
                
                // Project rows
                for (Row row : groupedRows) {
                    context.setCurrentRow(row);
                    context.setJoinedColumns(groupedColumns);
                    
                    Object[] projectedData = new Object[node.getSelectItems().size()];
                    for (int i = 0; i < node.getSelectItems().size(); i++) {
                        SelectItem item = node.getSelectItems().get(i);
                        projectedData[i] = expressionEvaluator.evaluate(item.getExpression(), context);
                    }
                    
                    resultRows.add(new Row(row.getId(), projectedData));
                }
            }
            
            logger.debug("SELECT executed: {} rows returned", resultRows.size());
            return new SqlExecutionResult(resultColumns, resultRows);
            
        } catch (Exception e) {
            throw new SqlExecutionException("Failed to execute SELECT statement", e);
        }
    }
    
    @Override
    public SqlExecutionResult visitInsertStatement(InsertStatement node, ExecutionContext context) throws SqlExecutionException {
        try {
            String tableName = node.getTableName();
            Table table = engine.getTable("public", tableName);
            if (table == null) {
                throw new SqlExecutionException("Table not found: " + tableName);
            }
            
            List<Column> tableColumns = table.getColumns();
            int affectedRows = 0;
            
            // Process each set of values
            for (List<Expression> values : node.getValuesList()) {
                if (values.size() != tableColumns.size()) {
                    throw new SqlExecutionException("Column count mismatch: expected " + 
                        tableColumns.size() + ", got " + values.size());
                }
                
                // Evaluate values
                Object[] rowData = new Object[values.size()];
                for (int i = 0; i < values.size(); i++) {
                    rowData[i] = expressionEvaluator.evaluate(values.get(i), context);
                    
                    // Convert and validate data type
                    Column column = tableColumns.get(i);
                    rowData[i] = column.getDataType().convertValue(rowData[i]);
                    
                    if (!column.getDataType().isValidValue(rowData[i])) {
                        throw new SqlExecutionException("Invalid value for column " + 
                            column.getName() + ": " + rowData[i]);
                    }
                }
                
                // Insert row
                table.insertRow(rowData);
                affectedRows++;
            }
            
            logger.debug("INSERT executed: {} rows inserted into {}", affectedRows, tableName);
            return new SqlExecutionResult(SqlExecutionResult.ResultType.INSERT, affectedRows);
            
        } catch (Exception e) {
            throw new SqlExecutionException("Failed to execute INSERT statement", e);
        }
    }
    
    @Override
    public SqlExecutionResult visitUpdateStatement(UpdateStatement node, ExecutionContext context) throws SqlExecutionException {
        try {
            String tableName = node.getTableName();
            Table table = engine.getTable("public", tableName);
            if (table == null) {
                throw new SqlExecutionException("Table not found: " + tableName);
            }
            
            // Get all rows and filter by WHERE clause if present
            List<Row> allRows = table.getAllRows();
            List<Row> rowsToUpdate = allRows;
            
            if (node.getWhereClause().isPresent()) {
                WhereClause whereClause = node.getWhereClause().get();
                rowsToUpdate = new ArrayList<>();
                
                for (Row row : allRows) {
                    context.setCurrentRow(row);
                    context.setCurrentTable(table);
                    
                    Object result = expressionEvaluator.evaluate(whereClause.getCondition(), context);
                    if (Boolean.TRUE.equals(result)) {
                        rowsToUpdate.add(row);
                    }
                }
            }
            
            int affectedRows = 0;
            List<Column> columns = table.getColumns();
            Map<String, Integer> columnIndexMap = new HashMap<>();
            for (int i = 0; i < columns.size(); i++) {
                columnIndexMap.put(columns.get(i).getName().toLowerCase(), i);
            }
            
            // Update each matching row
            for (Row row : rowsToUpdate) {
                context.setCurrentRow(row);
                context.setCurrentTable(table);
                
                Object[] newData = row.getData().clone();
                
                // Apply updates
                for (UpdateStatement.UpdateItem updateItem : node.getUpdateItems()) {
                    String columnName = updateItem.getColumnName().toLowerCase();
                    Integer columnIndex = columnIndexMap.get(columnName);
                    if (columnIndex == null) {
                        throw new SqlExecutionException("Column not found: " + columnName);
                    }
                    
                    Object newValue = expressionEvaluator.evaluate(updateItem.getValue(), context);
                    Column column = columns.get(columnIndex);
                    
                    // Convert and validate value
                    newValue = column.getDataType().convertValue(newValue);
                    if (!column.getDataType().isValidValue(newValue)) {
                        throw new SqlExecutionException("Invalid value for column " + 
                            columnName + ": " + newValue);
                    }
                    
                    newData[columnIndex] = newValue;
                }
                
                // Update the row
                table.updateRow(row.getId(), newData);
                affectedRows++;
            }
            
            logger.debug("UPDATE executed: {} rows updated in {}", affectedRows, tableName);
            return new SqlExecutionResult(SqlExecutionResult.ResultType.UPDATE, affectedRows);
            
        } catch (Exception e) {
            throw new SqlExecutionException("Failed to execute UPDATE statement", e);
        }
    }
    
    @Override
    public SqlExecutionResult visitDeleteStatement(DeleteStatement node, ExecutionContext context) throws SqlExecutionException {
        try {
            String tableName = node.getTableName();
            Table table = engine.getTable("public", tableName);
            if (table == null) {
                throw new SqlExecutionException("Table not found: " + tableName);
            }
            
            // Get all rows and filter by WHERE clause if present
            List<Row> allRows = table.getAllRows();
            List<Row> rowsToDelete = allRows;
            
            if (node.getWhereClause().isPresent()) {
                WhereClause whereClause = node.getWhereClause().get();
                rowsToDelete = new ArrayList<>();
                
                for (Row row : allRows) {
                    context.setCurrentRow(row);
                    context.setCurrentTable(table);
                    
                    Object result = expressionEvaluator.evaluate(whereClause.getCondition(), context);
                    if (Boolean.TRUE.equals(result)) {
                        rowsToDelete.add(row);
                    }
                }
            }
            
            int affectedRows = 0;
            
            // Delete matching rows
            for (Row row : rowsToDelete) {
                if (table.deleteRow(row.getId())) {
                    affectedRows++;
                }
            }
            
            logger.debug("DELETE executed: {} rows deleted from {}", affectedRows, tableName);
            return new SqlExecutionResult(SqlExecutionResult.ResultType.DELETE, affectedRows);
            
        } catch (Exception e) {
            throw new SqlExecutionException("Failed to execute DELETE statement", e);
        }
    }
    
    @Override
    public SqlExecutionResult visitCreateTableStatement(CreateTableStatement node, ExecutionContext context) throws SqlExecutionException {
        try {
            String tableName = node.getTableName();
            
            // Build columns from column definitions
            List<Column> columns = new ArrayList<>();
            for (ColumnDefinition colDef : node.getColumnDefinitions()) {
                String columnName = colDef.getColumnName();
                DataType dataType = colDef.getDataType().getDataType();
                
                // Check constraints (simplified - only handling NOT NULL for now)
                boolean nullable = !colDef.getConstraints().contains(ColumnDefinition.Constraint.NOT_NULL);
                
                columns.add(new Column.Builder()
                    .name(columnName)
                    .dataType(dataType)
                    .nullable(nullable)
                    .build());
            }
            
            // Create table
            Table table = new Table(tableName, columns);
            boolean created = engine.createTable("public", table);
            
            if (created) {
                logger.debug("CREATE TABLE executed: {} created", tableName);
                return new SqlExecutionResult(SqlExecutionResult.ResultType.DDL, true, 
                    "Table " + tableName + " created successfully");
            } else {
                return new SqlExecutionResult(SqlExecutionResult.ResultType.DDL, false, 
                    "Table " + tableName + " already exists");
            }
            
        } catch (Exception e) {
            throw new SqlExecutionException("Failed to execute CREATE TABLE statement", e);
        }
    }
    
    @Override
    public SqlExecutionResult visitDropTableStatement(DropTableStatement node, ExecutionContext context) throws SqlExecutionException {
        try {
            String tableName = node.getTableName();
            
            // For now, we don't have a direct dropTable method, so we'll simulate it
            Table table = engine.getTable("public", tableName);
            if (table == null) {
                return new SqlExecutionResult(SqlExecutionResult.ResultType.DDL, false, 
                    "Table " + tableName + " does not exist");
            }
            
            // TODO: Implement actual table dropping in the engine
            logger.debug("DROP TABLE executed: {} (simulated)", tableName);
            return new SqlExecutionResult(SqlExecutionResult.ResultType.DDL, true, 
                "Table " + tableName + " would be dropped (not implemented)");
            
        } catch (Exception e) {
            throw new SqlExecutionException("Failed to execute DROP TABLE statement", e);
        }
    }
    
    /**
     * Result of executing a FROM clause with potential joins.
     */
    private static class JoinResult {
        final List<Column> columns;
        final List<Row> rows;
        
        JoinResult(List<Column> columns, List<Row> rows) {
            this.columns = columns;
            this.rows = rows;
        }
    }
    
    /**
     * Execute the FROM clause, handling both simple table references and joins.
     */
    private JoinResult executeFromClause(FromClause fromClause, ExecutionContext context) throws SqlExecutionException {
        List<JoinableTable> joinableTables = fromClause.getJoinableTables();
        
        if (joinableTables.size() != 1) {
            throw new SqlExecutionException("Multi-table FROM clause not yet supported");
        }
        
        JoinableTable joinableTable = joinableTables.get(0);
        
        // Get base table
        TableReference baseTableRef = joinableTable.getBaseTable();
        String baseTableName = baseTableRef.getTableName();
        Table baseTable = engine.getTable("public", baseTableName);
        if (baseTable == null) {
            throw new SqlExecutionException("Table not found: " + baseTableName);
        }
        
        // Start with base table data
        List<Column> resultColumns = new ArrayList<>(baseTable.getColumns());
        List<Row> resultRows = baseTable.getAllRows();
        
        
        // Apply joins if present
        if (joinableTable.hasJoins()) {
            TableReference baseTableReference = baseTableRef; // Track the left table reference for alias support
            for (JoinClause joinClause : joinableTable.getJoins()) {
                JoinResult joinResult = executeJoin(resultColumns, resultRows, joinClause, context, baseTableReference);
                resultColumns = joinResult.columns;
                resultRows = joinResult.rows;
                // For subsequent joins, the left side becomes the previous join result
                baseTableReference = null; // No simple reference for joined result
            }
        }
        
        return new JoinResult(resultColumns, resultRows);
    }
    
    /**
     * Execute a single join operation with optimization.
     */
    private JoinResult executeJoin(List<Column> leftColumns, List<Row> leftRows, 
                                  JoinClause joinClause, ExecutionContext context, TableReference leftTableRef) throws SqlExecutionException {
        
        // Get right table
        TableReference rightTableRef = joinClause.getTable();
        String rightTableName = rightTableRef.getTableName();
        Table rightTable = engine.getTable("public", rightTableName);
        if (rightTable == null) {
            throw new SqlExecutionException("Table not found: " + rightTableName);
        }
        
        List<Row> rightRows = rightTable.getAllRows();
        List<Column> rightColumns = rightTable.getColumns();
        
        // Combine column schemas
        List<Column> combinedColumns = new ArrayList<>(leftColumns);
        combinedColumns.addAll(rightColumns);
        
        // Set up table information for proper column resolution
        Map<String, List<Column>> tableColumns = new HashMap<>();
        List<String> tableOrder = new ArrayList<>();
        
        // Add base table info (use alias if available)
        if (leftTableRef != null) {
            String leftTableKey = leftTableRef.getAlias().isPresent() ?
                leftTableRef.getAlias().get().toLowerCase() : leftTableRef.getTableName().toLowerCase();
            tableColumns.put(leftTableKey, new ArrayList<>(leftColumns));
            tableOrder.add(leftTableKey);
        }
        
        // Add right table info (use alias if available)
        String rightTableKey = rightTableRef.getAlias().isPresent() ? 
            rightTableRef.getAlias().get().toLowerCase() : rightTableName.toLowerCase();
        tableColumns.put(rightTableKey, new ArrayList<>(rightColumns));
        tableOrder.add(rightTableKey);
        
        // Update context with table information
        context.setTableColumns(tableColumns);
        context.setTableOrder(tableOrder);
        
        List<Row> joinedRows = new ArrayList<>();
        
        // Choose optimal join algorithm based on conditions and data size
        JoinAlgorithm algorithm = chooseOptimalJoinAlgorithm(
            leftRows, rightRows, leftColumns, rightColumns, joinClause.getOnCondition()
        );
        
        logger.debug("Using {} algorithm for {} join between {} and {} rows", 
                    algorithm, joinClause.getJoinType(), leftRows.size(), rightRows.size());
        
        // Execute join based on join type and chosen algorithm
        switch (joinClause.getJoinType()) {
            case INNER:
                executeInnerJoinOptimized(leftRows, rightRows, leftColumns, rightColumns, 
                                        joinClause.getOnCondition(), combinedColumns, joinedRows, context, algorithm);
                break;
            case LEFT:
                executeLeftOuterJoinOptimized(leftRows, rightRows, leftColumns, rightColumns, 
                                            joinClause.getOnCondition(), combinedColumns, joinedRows, context, algorithm);
                break;
            case RIGHT:
                executeRightOuterJoinOptimized(leftRows, rightRows, leftColumns, rightColumns, 
                                             joinClause.getOnCondition(), combinedColumns, joinedRows, context, algorithm);
                break;
            case FULL_OUTER:
                executeFullOuterJoinOptimized(leftRows, rightRows, leftColumns, rightColumns, 
                                            joinClause.getOnCondition(), combinedColumns, joinedRows, context, algorithm);
                break;
            default:
                throw new SqlExecutionException("Unsupported join type: " + joinClause.getJoinType());
        }
        
        return new JoinResult(combinedColumns, joinedRows);
    }
    
    
    /**
     * Available JOIN algorithms for optimization.
     */
    private enum JoinAlgorithm {
        NESTED_LOOP,  // Original O(n*m) algorithm
        HASH_JOIN,    // Hash-based join for equi-joins
        SORT_MERGE    // Sort-merge join (future enhancement)
    }
    
    /**
     * Choose optimal join algorithm based on data characteristics.
     */
    private JoinAlgorithm chooseOptimalJoinAlgorithm(List<Row> leftRows, List<Row> rightRows, 
                                                   List<Column> leftColumns, List<Column> rightColumns,
                                                   Optional<Expression> joinCondition) {
        
        int leftSize = leftRows.size();
        int rightSize = rightRows.size();
        
        // For small datasets (< 100 rows each), nested loop is efficient due to simplicity
        if (leftSize < 100 && rightSize < 100) {
            return JoinAlgorithm.NESTED_LOOP;
        }
        
        // If we have a simple equi-join condition, prefer hash join for larger datasets
        if (joinCondition.isPresent() && isSimpleEquiJoin(joinCondition.get(), leftColumns, rightColumns)) {
            // Hash join is particularly effective when one table is much smaller
            if (Math.min(leftSize, rightSize) < Math.max(leftSize, rightSize) / 3) {
                return JoinAlgorithm.HASH_JOIN;
            }
            // Also prefer hash join for medium-to-large datasets
            if (leftSize > 50 || rightSize > 50) {
                return JoinAlgorithm.HASH_JOIN;
            }
        }
        
        // Default to nested loop for complex conditions or small datasets
        return JoinAlgorithm.NESTED_LOOP;
    }
    
    /**
     * Check if the join condition is a simple equi-join (col1 = col2).
     */
    private boolean isSimpleEquiJoin(Expression condition, List<Column> leftColumns, List<Column> rightColumns) {
        if (!(condition instanceof BinaryExpression)) {
            return false;
        }
        
        BinaryExpression binary = (BinaryExpression) condition;
        if (!"=".equals(binary.getOperator())) {
            return false;
        }
        
        // Check if both sides are simple column references
        return (binary.getLeft() instanceof ColumnReference) && 
               (binary.getRight() instanceof ColumnReference);
    }
    
    /**
     * Execute INNER JOIN operation with optimization.
     */
    private void executeInnerJoinOptimized(List<Row> leftRows, List<Row> rightRows,
                                         List<Column> leftColumns, List<Column> rightColumns,
                                         Optional<Expression> onCondition, List<Column> combinedColumns,
                                         List<Row> result, ExecutionContext context, JoinAlgorithm algorithm) throws SqlExecutionException {
        
        switch (algorithm) {
            case HASH_JOIN:
                executeHashInnerJoin(leftRows, rightRows, leftColumns, rightColumns, 
                                   onCondition, combinedColumns, result, context);
                break;
            case NESTED_LOOP:
            default:
                executeNestedLoopInnerJoin(leftRows, rightRows, leftColumns, rightColumns, 
                                         onCondition, combinedColumns, result, context);
                break;
        }
    }
    
    /**
     * Execute INNER JOIN using nested loop algorithm.
     */
    private void executeNestedLoopInnerJoin(List<Row> leftRows, List<Row> rightRows,
                                          List<Column> leftColumns, List<Column> rightColumns,
                                          Optional<Expression> onCondition, List<Column> combinedColumns,
                                          List<Row> result, ExecutionContext context) throws SqlExecutionException {
        
        for (Row leftRow : leftRows) {
            for (Row rightRow : rightRows) {
                if (evaluateJoinCondition(leftRow, rightRow, leftColumns, rightColumns, 
                                        onCondition, combinedColumns, context)) {
                    Row joinedRow = combineRows(leftRow, rightRow);
                    result.add(joinedRow);
                }
            }
        }
    }
    
    /**
     * Execute INNER JOIN using hash join algorithm for equi-joins.
     */
    private void executeHashInnerJoin(List<Row> leftRows, List<Row> rightRows,
                                    List<Column> leftColumns, List<Column> rightColumns,
                                    Optional<Expression> onCondition, List<Column> combinedColumns,
                                    List<Row> result, ExecutionContext context) throws SqlExecutionException {
        
        if (!onCondition.isPresent()) {
            // Fall back to nested loop for cross joins
            executeNestedLoopInnerJoin(leftRows, rightRows, leftColumns, rightColumns, 
                                     onCondition, combinedColumns, result, context);
            return;
        }
        
        // Extract join keys from the equi-join condition
        JoinKeys joinKeys = extractJoinKeys(onCondition.get(), leftColumns, rightColumns);
        if (joinKeys == null) {
            // Fall back to nested loop for complex conditions
            executeNestedLoopInnerJoin(leftRows, rightRows, leftColumns, rightColumns, 
                                     onCondition, combinedColumns, result, context);
            return;
        }
        
        // Choose smaller table for building hash table
        List<Row> buildRows;
        List<Row> probeRows;
        int buildKeyIndex;
        int probeKeyIndex;
        boolean leftIsBuild;
        
        if (leftRows.size() <= rightRows.size()) {
            buildRows = leftRows;
            probeRows = rightRows;
            buildKeyIndex = joinKeys.leftKeyIndex;
            probeKeyIndex = joinKeys.rightKeyIndex;
            leftIsBuild = true;
        } else {
            buildRows = rightRows;
            probeRows = leftRows;
            buildKeyIndex = joinKeys.rightKeyIndex;
            probeKeyIndex = joinKeys.leftKeyIndex;
            leftIsBuild = false;
        }
        
        // Build phase: create hash table
        Map<Object, List<Row>> hashTable = new HashMap<>();
        for (Row buildRow : buildRows) {
            Object key = buildRow.getData()[buildKeyIndex];
            if (key != null) { // Skip null keys
                hashTable.computeIfAbsent(key, k -> new ArrayList<>()).add(buildRow);
            }
        }
        
        // Probe phase: lookup and join
        for (Row probeRow : probeRows) {
            Object key = probeRow.getData()[probeKeyIndex];
            if (key != null) {
                List<Row> matchingRows = hashTable.get(key);
                if (matchingRows != null) {
                    for (Row matchingRow : matchingRows) {
                        Row joinedRow = leftIsBuild ? 
                            combineRows(matchingRow, probeRow) :
                            combineRows(probeRow, matchingRow);
                        result.add(joinedRow);
                    }
                }
            }
        }
    }
    
    /**
     * Helper class to hold join key information.
     */
    private static class JoinKeys {
        final int leftKeyIndex;
        final int rightKeyIndex;
        
        JoinKeys(int leftKeyIndex, int rightKeyIndex) {
            this.leftKeyIndex = leftKeyIndex;
            this.rightKeyIndex = rightKeyIndex;
        }
    }
    
    /**
     * Extract join keys from equi-join condition.
     */
    private JoinKeys extractJoinKeys(Expression condition, List<Column> leftColumns, List<Column> rightColumns) {
        if (!(condition instanceof BinaryExpression)) {
            return null;
        }
        
        BinaryExpression binary = (BinaryExpression) condition;
        if (!"=".equals(binary.getOperator())) {
            return null;
        }
        
        Expression left = binary.getLeft();
        Expression right = binary.getRight();
        
        if (!(left instanceof ColumnReference) || !(right instanceof ColumnReference)) {
            return null;
        }
        
        ColumnReference leftCol = (ColumnReference) left;
        ColumnReference rightCol = (ColumnReference) right;
        
        // Find column indexes
        Integer leftIndex = findColumnIndex(leftCol.getColumnName(), leftColumns);
        Integer rightIndex = findColumnIndex(rightCol.getColumnName(), rightColumns);
        
        if (leftIndex != null && rightIndex != null) {
            return new JoinKeys(leftIndex, rightIndex + leftColumns.size());
        }
        
        // Try the reverse (right column might be from left table)
        rightIndex = findColumnIndex(leftCol.getColumnName(), rightColumns);
        leftIndex = findColumnIndex(rightCol.getColumnName(), leftColumns);
        
        if (leftIndex != null && rightIndex != null) {
            return new JoinKeys(leftIndex, rightIndex + leftColumns.size());
        }
        
        return null;
    }
    
    /**
     * Find column index by name.
     */
    private Integer findColumnIndex(String columnName, List<Column> columns) {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).getName().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return null;
    }
    
    /**
     * Execute LEFT OUTER JOIN operation with optimization.
     */
    private void executeLeftOuterJoinOptimized(List<Row> leftRows, List<Row> rightRows,
                                             List<Column> leftColumns, List<Column> rightColumns,
                                             Optional<Expression> onCondition, List<Column> combinedColumns,
                                             List<Row> result, ExecutionContext context, JoinAlgorithm algorithm) throws SqlExecutionException {
        
        switch (algorithm) {
            case HASH_JOIN:
                executeHashLeftOuterJoin(leftRows, rightRows, leftColumns, rightColumns, 
                                       onCondition, combinedColumns, result, context);
                break;
            case NESTED_LOOP:
            default:
                executeNestedLoopLeftOuterJoin(leftRows, rightRows, leftColumns, rightColumns, 
                                             onCondition, combinedColumns, result, context);
                break;
        }
    }
    
    /**
     * Execute LEFT OUTER JOIN using nested loop algorithm.
     */
    private void executeNestedLoopLeftOuterJoin(List<Row> leftRows, List<Row> rightRows,
                                              List<Column> leftColumns, List<Column> rightColumns,
                                              Optional<Expression> onCondition, List<Column> combinedColumns,
                                              List<Row> result, ExecutionContext context) throws SqlExecutionException {
        
        for (Row leftRow : leftRows) {
            boolean hasMatch = false;
            
            for (Row rightRow : rightRows) {
                if (evaluateJoinCondition(leftRow, rightRow, leftColumns, rightColumns, 
                                        onCondition, combinedColumns, context)) {
                    Row joinedRow = combineRows(leftRow, rightRow);
                    result.add(joinedRow);
                    hasMatch = true;
                }
            }
            
            // If no match found, add left row with nulls for right columns
            if (!hasMatch) {
                Row nullRightRow = createNullRow(rightColumns.size());
                Row joinedRow = combineRows(leftRow, nullRightRow);
                result.add(joinedRow);
            }
        }
    }
    
    /**
     * Execute LEFT OUTER JOIN using hash join algorithm for equi-joins.
     */
    private void executeHashLeftOuterJoin(List<Row> leftRows, List<Row> rightRows,
                                        List<Column> leftColumns, List<Column> rightColumns,
                                        Optional<Expression> onCondition, List<Column> combinedColumns,
                                        List<Row> result, ExecutionContext context) throws SqlExecutionException {
        
        if (!onCondition.isPresent()) {
            // Fall back to nested loop for cross joins
            executeNestedLoopLeftOuterJoin(leftRows, rightRows, leftColumns, rightColumns, 
                                         onCondition, combinedColumns, result, context);
            return;
        }
        
        // Extract join keys from the equi-join condition
        JoinKeys joinKeys = extractJoinKeys(onCondition.get(), leftColumns, rightColumns);
        if (joinKeys == null) {
            // Fall back to nested loop for complex conditions
            executeNestedLoopLeftOuterJoin(leftRows, rightRows, leftColumns, rightColumns, 
                                         onCondition, combinedColumns, result, context);
            return;
        }
        
        // Build hash table from right table (probe side)
        Map<Object, List<Row>> hashTable = new HashMap<>();
        for (Row rightRow : rightRows) {
            Object key = rightRow.getData()[joinKeys.rightKeyIndex];
            if (key != null) { // Skip null keys
                hashTable.computeIfAbsent(key, k -> new ArrayList<>()).add(rightRow);
            }
        }
        
        // Probe with left table and preserve all left rows
        for (Row leftRow : leftRows) {
            Object key = leftRow.getData()[joinKeys.leftKeyIndex];
            List<Row> matchingRows = (key != null) ? hashTable.get(key) : null;
            
            if (matchingRows != null && !matchingRows.isEmpty()) {
                // Found matches, add all combinations
                for (Row rightRow : matchingRows) {
                    Row joinedRow = combineRows(leftRow, rightRow);
                    result.add(joinedRow);
                }
            } else {
                // No match found, add left row with nulls for right columns
                Row nullRightRow = createNullRow(rightColumns.size());
                Row joinedRow = combineRows(leftRow, nullRightRow);
                result.add(joinedRow);
            }
        }
    }
    
    /**
     * Execute RIGHT OUTER JOIN operation with optimization.
     */
    private void executeRightOuterJoinOptimized(List<Row> leftRows, List<Row> rightRows,
                                               List<Column> leftColumns, List<Column> rightColumns,
                                               Optional<Expression> onCondition, List<Column> combinedColumns,
                                               List<Row> result, ExecutionContext context, JoinAlgorithm algorithm) throws SqlExecutionException {
        
        switch (algorithm) {
            case HASH_JOIN:
                executeHashRightOuterJoin(leftRows, rightRows, leftColumns, rightColumns, 
                                        onCondition, combinedColumns, result, context);
                break;
            case NESTED_LOOP:
            default:
                executeNestedLoopRightOuterJoin(leftRows, rightRows, leftColumns, rightColumns, 
                                              onCondition, combinedColumns, result, context);
                break;
        }
    }
    
    /**
     * Execute RIGHT OUTER JOIN using nested loop algorithm.
     */
    private void executeNestedLoopRightOuterJoin(List<Row> leftRows, List<Row> rightRows,
                                                List<Column> leftColumns, List<Column> rightColumns,
                                                Optional<Expression> onCondition, List<Column> combinedColumns,
                                                List<Row> result, ExecutionContext context) throws SqlExecutionException {
        
        for (Row rightRow : rightRows) {
            boolean hasMatch = false;
            
            for (Row leftRow : leftRows) {
                if (evaluateJoinCondition(leftRow, rightRow, leftColumns, rightColumns, 
                                        onCondition, combinedColumns, context)) {
                    Row joinedRow = combineRows(leftRow, rightRow);
                    result.add(joinedRow);
                    hasMatch = true;
                }
            }
            
            // If no match found, add right row with nulls for left columns
            if (!hasMatch) {
                Row nullLeftRow = createNullRow(leftColumns.size());
                Row joinedRow = combineRows(nullLeftRow, rightRow);
                result.add(joinedRow);
            }
        }
    }
    
    /**
     * Execute RIGHT OUTER JOIN using hash join algorithm for equi-joins.
     */
    private void executeHashRightOuterJoin(List<Row> leftRows, List<Row> rightRows,
                                         List<Column> leftColumns, List<Column> rightColumns,
                                         Optional<Expression> onCondition, List<Column> combinedColumns,
                                         List<Row> result, ExecutionContext context) throws SqlExecutionException {
        
        if (!onCondition.isPresent()) {
            // Fall back to nested loop for cross joins
            executeNestedLoopRightOuterJoin(leftRows, rightRows, leftColumns, rightColumns, 
                                          onCondition, combinedColumns, result, context);
            return;
        }
        
        // Extract join keys from the equi-join condition
        JoinKeys joinKeys = extractJoinKeys(onCondition.get(), leftColumns, rightColumns);
        if (joinKeys == null) {
            // Fall back to nested loop for complex conditions
            executeNestedLoopRightOuterJoin(leftRows, rightRows, leftColumns, rightColumns, 
                                          onCondition, combinedColumns, result, context);
            return;
        }
        
        // Build hash table from left table
        Map<Object, List<Row>> hashTable = new HashMap<>();
        for (Row leftRow : leftRows) {
            Object key = leftRow.getData()[joinKeys.leftKeyIndex];
            if (key != null) { // Skip null keys
                hashTable.computeIfAbsent(key, k -> new ArrayList<>()).add(leftRow);
            }
        }
        
        // Probe with right table and preserve all right rows
        for (Row rightRow : rightRows) {
            Object key = rightRow.getData()[joinKeys.rightKeyIndex];
            List<Row> matchingRows = (key != null) ? hashTable.get(key) : null;
            
            if (matchingRows != null && !matchingRows.isEmpty()) {
                // Found matches, add all combinations
                for (Row leftRow : matchingRows) {
                    Row joinedRow = combineRows(leftRow, rightRow);
                    result.add(joinedRow);
                }
            } else {
                // No match found, add right row with nulls for left columns
                Row nullLeftRow = createNullRow(leftColumns.size());
                Row joinedRow = combineRows(nullLeftRow, rightRow);
                result.add(joinedRow);
            }
        }
    }
    
    /**
     * Execute FULL OUTER JOIN operation with optimization.
     */
    private void executeFullOuterJoinOptimized(List<Row> leftRows, List<Row> rightRows,
                                              List<Column> leftColumns, List<Column> rightColumns,
                                              Optional<Expression> onCondition, List<Column> combinedColumns,
                                              List<Row> result, ExecutionContext context, JoinAlgorithm algorithm) throws SqlExecutionException {
        
        switch (algorithm) {
            case HASH_JOIN:
                executeHashFullOuterJoin(leftRows, rightRows, leftColumns, rightColumns, 
                                       onCondition, combinedColumns, result, context);
                break;
            case NESTED_LOOP:
            default:
                executeNestedLoopFullOuterJoin(leftRows, rightRows, leftColumns, rightColumns, 
                                             onCondition, combinedColumns, result, context);
                break;
        }
    }
    
    /**
     * Execute FULL OUTER JOIN using nested loop algorithm.
     */
    private void executeNestedLoopFullOuterJoin(List<Row> leftRows, List<Row> rightRows,
                                               List<Column> leftColumns, List<Column> rightColumns,
                                               Optional<Expression> onCondition, List<Column> combinedColumns,
                                               List<Row> result, ExecutionContext context) throws SqlExecutionException {
        
        Set<Row> matchedRightRows = new HashSet<>();
        
        // First pass: find all matches and left outer join
        for (Row leftRow : leftRows) {
            boolean hasMatch = false;
            
            for (Row rightRow : rightRows) {
                if (evaluateJoinCondition(leftRow, rightRow, leftColumns, rightColumns, 
                                        onCondition, combinedColumns, context)) {
                    Row joinedRow = combineRows(leftRow, rightRow);
                    result.add(joinedRow);
                    matchedRightRows.add(rightRow);
                    hasMatch = true;
                }
            }
            
            // If no match found, add left row with nulls for right columns
            if (!hasMatch) {
                Row nullRightRow = createNullRow(rightColumns.size());
                Row joinedRow = combineRows(leftRow, nullRightRow);
                result.add(joinedRow);
            }
        }
        
        // Second pass: add unmatched right rows
        for (Row rightRow : rightRows) {
            if (!matchedRightRows.contains(rightRow)) {
                Row nullLeftRow = createNullRow(leftColumns.size());
                Row joinedRow = combineRows(nullLeftRow, rightRow);
                result.add(joinedRow);
            }
        }
    }
    
    /**
     * Execute FULL OUTER JOIN using hash join algorithm for equi-joins.
     */
    private void executeHashFullOuterJoin(List<Row> leftRows, List<Row> rightRows,
                                        List<Column> leftColumns, List<Column> rightColumns,
                                        Optional<Expression> onCondition, List<Column> combinedColumns,
                                        List<Row> result, ExecutionContext context) throws SqlExecutionException {
        
        if (!onCondition.isPresent()) {
            // Fall back to nested loop for cross joins
            executeNestedLoopFullOuterJoin(leftRows, rightRows, leftColumns, rightColumns, 
                                         onCondition, combinedColumns, result, context);
            return;
        }
        
        // Extract join keys from the equi-join condition
        JoinKeys joinKeys = extractJoinKeys(onCondition.get(), leftColumns, rightColumns);
        if (joinKeys == null) {
            // Fall back to nested loop for complex conditions
            executeNestedLoopFullOuterJoin(leftRows, rightRows, leftColumns, rightColumns, 
                                         onCondition, combinedColumns, result, context);
            return;
        }
        
        // Build hash table from smaller table
        Map<Object, List<Row>> leftHashTable = new HashMap<>();
        for (Row leftRow : leftRows) {
            Object key = leftRow.getData()[joinKeys.leftKeyIndex];
            if (key != null) {
                leftHashTable.computeIfAbsent(key, k -> new ArrayList<>()).add(leftRow);
            }
        }
        
        Set<Object> matchedKeys = new HashSet<>();
        
        // Process right table: find matches and add unmatched rights
        for (Row rightRow : rightRows) {
            Object key = rightRow.getData()[joinKeys.rightKeyIndex];
            List<Row> matchingLeftRows = (key != null) ? leftHashTable.get(key) : null;
            
            if (matchingLeftRows != null && !matchingLeftRows.isEmpty()) {
                // Found matches
                matchedKeys.add(key);
                for (Row leftRow : matchingLeftRows) {
                    Row joinedRow = combineRows(leftRow, rightRow);
                    result.add(joinedRow);
                }
            } else {
                // No match found, add right row with nulls for left columns
                Row nullLeftRow = createNullRow(leftColumns.size());
                Row joinedRow = combineRows(nullLeftRow, rightRow);
                result.add(joinedRow);
            }
        }
        
        // Add unmatched left rows
        for (Row leftRow : leftRows) {
            Object key = leftRow.getData()[joinKeys.leftKeyIndex];
            if (key == null || !matchedKeys.contains(key)) {
                Row nullRightRow = createNullRow(rightColumns.size());
                Row joinedRow = combineRows(leftRow, nullRightRow);
                result.add(joinedRow);
            }
        }
    }
    
    /**
     * Evaluate join condition between two rows.
     */
    private boolean evaluateJoinCondition(Row leftRow, Row rightRow, 
                                        List<Column> leftColumns, List<Column> rightColumns,
                                        Optional<Expression> onCondition, List<Column> combinedColumns,
                                        ExecutionContext context) throws SqlExecutionException {
        if (!onCondition.isPresent()) {
            // Cross join - always true
            return true;
        }
        
        // Create combined row for condition evaluation
        Row combinedRow = combineRows(leftRow, rightRow);
        context.setCurrentRow(combinedRow);
        context.setJoinedColumns(combinedColumns);
        
        Object result = expressionEvaluator.evaluate(onCondition.get(), context);
        return Boolean.TRUE.equals(result);
    }
    
    /**
     * Combine two rows into a single row.
     */
    private Row combineRows(Row leftRow, Row rightRow) {
        Object[] leftData = leftRow.getData();
        Object[] rightData = rightRow.getData();
        
        Object[] combinedData = new Object[leftData.length + rightData.length];
        System.arraycopy(leftData, 0, combinedData, 0, leftData.length);
        System.arraycopy(rightData, 0, combinedData, leftData.length, rightData.length);
        
        return new Row(leftRow.getId(), combinedData);
    }
    
    /**
     * Create a row with all null values for outer join.
     */
    private Row createNullRow(int columnCount) {
        Object[] nullData = new Object[columnCount];
        Arrays.fill(nullData, null);
        return new Row(0L, nullData);
    }

    // Helper methods
    private List<Row> applyOrderBy(List<Row> rows, OrderByClause orderBy, 
                                  ExecutionContext context, List<Column> columns) {
        return rows.stream()
            .sorted((row1, row2) -> {
                for (OrderByClause.OrderItem item : orderBy.getOrderItems()) {
                    context.setCurrentRow(row1);
                    context.setJoinedColumns(columns);
                    Object value1 = expressionEvaluator.evaluate(item.getExpression(), context);
                    
                    context.setCurrentRow(row2);
                    context.setJoinedColumns(columns);
                    Object value2 = expressionEvaluator.evaluate(item.getExpression(), context);
                    
                    int comparison = compareValues(value1, value2);
                    if (comparison != 0) {
                        return item.isAscending() ? comparison : -comparison;
                    }
                }
                return 0;
            })
            .collect(Collectors.toList());
    }
    
    private int evaluateIntExpression(Expression expr, ExecutionContext context) {
        Object result = expressionEvaluator.evaluate(expr, context);
        if (result instanceof Number) {
            return ((Number) result).intValue();
        }
        throw new IllegalArgumentException("Expression does not evaluate to a number: " + result);
    }
    
    @SuppressWarnings("unchecked")
    private int compareValues(Object value1, Object value2) {
        if (value1 == null && value2 == null) return 0;
        if (value1 == null) return -1;
        if (value2 == null) return 1;
        
        if (value1 instanceof Comparable && value2 instanceof Comparable) {
            try {
                return ((Comparable) value1).compareTo(value2);
            } catch (ClassCastException e) {
                // Fall back to string comparison
                return value1.toString().compareTo(value2.toString());
            }
        }
        
        return value1.toString().compareTo(value2.toString());
    }
    
    /**
     * Validate that all column references in an expression exist in the given columns.
     */
    private void validateColumnReferences(Expression expression, List<Column> columns) throws SqlExecutionException {
        if (expression instanceof ColumnReference) {
            ColumnReference colRef = (ColumnReference) expression;
            String columnName = colRef.getColumnName().toLowerCase();
            
            boolean found = columns.stream()
                .anyMatch(col -> col.getName().toLowerCase().equals(columnName));
            
            if (!found) {
                throw new SqlExecutionException("Column not found: " + columnName);
            }
        }
        else if (expression instanceof BinaryExpression) {
            BinaryExpression binary = (BinaryExpression) expression;
            validateColumnReferences(binary.getLeft(), columns);
            validateColumnReferences(binary.getRight(), columns);
        }
        else if (expression instanceof UnaryExpression) {
            UnaryExpression unary = (UnaryExpression) expression;
            validateColumnReferences(unary.getOperand(), columns);
        }
        else if (expression instanceof FunctionCall) {
            FunctionCall func = (FunctionCall) expression;
            for (Expression arg : func.getArguments()) {
                validateColumnReferences(arg, columns);
            }
        }
        // LiteralExpression and other types don't contain column references
    }
    
    // Unimplemented visitor methods for AST nodes that don't require direct execution
    @Override public SqlExecutionResult visitJoinableTable(JoinableTable node, ExecutionContext context) { return null; }
    @Override public SqlExecutionResult visitLiteralExpression(LiteralExpression node, ExecutionContext context) { return null; }
    @Override public SqlExecutionResult visitColumnReference(ColumnReference node, ExecutionContext context) { return null; }
    @Override public SqlExecutionResult visitBinaryExpression(BinaryExpression node, ExecutionContext context) { return null; }
    @Override public SqlExecutionResult visitUnaryExpression(UnaryExpression node, ExecutionContext context) { return null; }
    @Override public SqlExecutionResult visitFunctionCall(FunctionCall node, ExecutionContext context) { return null; }
    @Override public SqlExecutionResult visitCaseExpression(CaseExpression node, ExecutionContext context) { return null; }
    @Override public SqlExecutionResult visitFromClause(FromClause node, ExecutionContext context) { return null; }
    @Override public SqlExecutionResult visitWhereClause(WhereClause node, ExecutionContext context) { return null; }
    @Override public SqlExecutionResult visitOrderByClause(OrderByClause node, ExecutionContext context) { return null; }
    @Override public SqlExecutionResult visitGroupByClause(GroupByClause node, ExecutionContext context) { return null; }
    @Override public SqlExecutionResult visitHavingClause(HavingClause node, ExecutionContext context) { return null; }
    @Override public SqlExecutionResult visitLimitClause(LimitClause node, ExecutionContext context) { return null; }
    @Override public SqlExecutionResult visitTableReference(TableReference node, ExecutionContext context) { return null; }
    @Override public SqlExecutionResult visitJoinClause(JoinClause node, ExecutionContext context) { return null; }
    @Override public SqlExecutionResult visitSelectItem(SelectItem node, ExecutionContext context) { return null; }
    @Override public SqlExecutionResult visitDataType(DataTypeNode node, ExecutionContext context) { return null; }
    @Override public SqlExecutionResult visitColumnDefinition(ColumnDefinition node, ExecutionContext context) { return null; }
    @Override public SqlExecutionResult visitSubqueryExpression(SubqueryExpression node, ExecutionContext context) { return null; }
    @Override public SqlExecutionResult visitExistsExpression(ExistsExpression node, ExecutionContext context) { return null; }
    @Override public SqlExecutionResult visitInSubqueryExpression(InSubqueryExpression node, ExecutionContext context) { return null; }
    @Override public SqlExecutionResult visitAggregateFunction(AggregateFunction node, ExecutionContext context) { return null; }
    
    // Helper methods for aggregation
    private boolean hasAggregateFunction(List<SelectItem> selectItems) {
        for (SelectItem item : selectItems) {
            if (containsAggregateFunction(item.getExpression())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsAggregateFunction(Expression expression) {
        if (expression instanceof AggregateFunction) {
            return true;
        }
        // TODO: Recursively check nested expressions
        return false;
    }
    
    private static class AggregationResult {
        final List<Column> columns;
        final List<Row> rows;
        
        AggregationResult(List<Column> columns, List<Row> rows) {
            this.columns = columns;
            this.rows = rows;
        }
    }
    
    private AggregationResult performAggregation(SelectStatement selectStatement, List<Row> rows, 
                                                List<Column> columns, ExecutionContext context) throws SqlExecutionException {
        
        // If there's a GROUP BY clause, group the rows
        if (selectStatement.getGroupByClause().isPresent()) {
            return performGroupByAggregation(selectStatement, rows, columns, context);
        } else {
            // Simple aggregation without grouping - treat all rows as one group
            return performSimpleAggregation(selectStatement, rows, columns, context);
        }
    }
    
    private AggregationResult performSimpleAggregation(SelectStatement selectStatement, List<Row> rows, 
                                                      List<Column> columns, ExecutionContext context) throws SqlExecutionException {
        
        // Calculate aggregate values for all rows as a single group
        List<Object> aggregatedValues = new ArrayList<>();
        List<Column> resultColumns = new ArrayList<>();
        
        for (int i = 0; i < selectStatement.getSelectItems().size(); i++) {
            SelectItem item = selectStatement.getSelectItems().get(i);
            String columnName;
            if (item.getAlias().isPresent()) {
                columnName = item.getAlias().get();
            } else if (item.getExpression() instanceof com.memgres.sql.ast.expression.ColumnReference) {
                com.memgres.sql.ast.expression.ColumnReference colRef = 
                    (com.memgres.sql.ast.expression.ColumnReference) item.getExpression();
                columnName = colRef.getColumnName();
            } else {
                columnName = "column" + i;
            }
            
            if (item.getExpression() instanceof AggregateFunction) {
                AggregateFunction aggregateFunc = (AggregateFunction) item.getExpression();
                Object aggregatedValue = calculateAggregateValue(aggregateFunc, rows, columns, context);
                aggregatedValues.add(aggregatedValue);
            } else {
                // Non-aggregate expression in aggregate query - use first row's value
                if (!rows.isEmpty()) {
                    context.setCurrentRow(rows.get(0));
                    context.setJoinedColumns(columns);
                    Object value = expressionEvaluator.evaluate(item.getExpression(), context);
                    aggregatedValues.add(value);
                } else {
                    aggregatedValues.add(null);
                }
            }
            
            resultColumns.add(new Column.Builder()
                .name(columnName)
                .dataType(DataType.TEXT)
                .build());
        }
        
        Row resultRow = new Row(1L, aggregatedValues.toArray());
        return new AggregationResult(resultColumns, List.of(resultRow));
    }
    
    private AggregationResult performGroupByAggregation(SelectStatement selectStatement, List<Row> rows, 
                                                       List<Column> columns, ExecutionContext context) throws SqlExecutionException {
        
        GroupByClause groupBy = selectStatement.getGroupByClause().get();
        
        // Group rows by GROUP BY expressions
        Map<String, List<Row>> groups = new LinkedHashMap<>();
        
        for (Row row : rows) {
            context.setCurrentRow(row);
            context.setJoinedColumns(columns);
            
            // Calculate group key
            StringBuilder keyBuilder = new StringBuilder();
            for (int i = 0; i < groupBy.getGroupingExpressions().size(); i++) {
                Expression groupExpr = groupBy.getGroupingExpressions().get(i);
                Object value = expressionEvaluator.evaluate(groupExpr, context);
                if (i > 0) keyBuilder.append("|");
                keyBuilder.append(value != null ? value.toString() : "NULL");
            }
            String groupKey = keyBuilder.toString();
            
            groups.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(row);
        }
        
        // Calculate aggregated results for each group
        List<Row> resultRows = new ArrayList<>();
        List<Column> resultColumns = new ArrayList<>();
        
        // Determine result columns
        for (int i = 0; i < selectStatement.getSelectItems().size(); i++) {
            SelectItem item = selectStatement.getSelectItems().get(i);
            String columnName;
            if (item.getAlias().isPresent()) {
                columnName = item.getAlias().get();
            } else if (item.getExpression() instanceof com.memgres.sql.ast.expression.ColumnReference) {
                com.memgres.sql.ast.expression.ColumnReference colRef = 
                    (com.memgres.sql.ast.expression.ColumnReference) item.getExpression();
                columnName = colRef.getColumnName();
            } else {
                columnName = "column" + i;
            }
            resultColumns.add(new Column.Builder()
                .name(columnName)
                .dataType(DataType.TEXT)
                .build());
        }
        
        // Process each group
        long rowId = 1;
        for (Map.Entry<String, List<Row>> group : groups.entrySet()) {
            List<Row> groupRows = group.getValue();
            List<Object> aggregatedValues = new ArrayList<>();
            
            for (SelectItem item : selectStatement.getSelectItems()) {
                if (item.getExpression() instanceof AggregateFunction) {
                    AggregateFunction aggregateFunc = (AggregateFunction) item.getExpression();
                    Object aggregatedValue = calculateAggregateValue(aggregateFunc, groupRows, columns, context);
                    aggregatedValues.add(aggregatedValue);
                } else {
                    // Non-aggregate expression in GROUP BY query - use first row's value
                    if (!groupRows.isEmpty()) {
                        context.setCurrentRow(groupRows.get(0));
                        context.setJoinedColumns(columns);
                        Object value = expressionEvaluator.evaluate(item.getExpression(), context);
                        aggregatedValues.add(value);
                    } else {
                        aggregatedValues.add(null);
                    }
                }
            }
            
            Row resultRow = new Row(rowId++, aggregatedValues.toArray());
            resultRows.add(resultRow);
        }
        
        return new AggregationResult(resultColumns, resultRows);
    }
    
    private Object calculateAggregateValue(AggregateFunction aggregateFunc, List<Row> rows, 
                                          List<Column> columns, ExecutionContext context) {
        
        switch (aggregateFunc.getAggregateType()) {
            case COUNT:
                if (aggregateFunc.isCountStar()) {
                    return (long) rows.size();
                } else {
                    // COUNT with expression - count non-null values
                    long count = 0;
                    for (Row row : rows) {
                        context.setCurrentRow(row);
                        context.setJoinedColumns(columns);
                        Object value = expressionEvaluator.evaluate(aggregateFunc.getExpression(), context);
                        if (value != null) {
                            count++;
                        }
                    }
                    return count;
                }
                
            case COUNT_DISTINCT:
                // COUNT DISTINCT - count unique non-null values
                Set<Object> uniqueValues = new HashSet<>();
                for (Row row : rows) {
                    context.setCurrentRow(row);
                    context.setJoinedColumns(columns);
                    Object value = expressionEvaluator.evaluate(aggregateFunc.getExpression(), context);
                    if (value != null) {
                        uniqueValues.add(value);
                    }
                }
                return (long) uniqueValues.size();
                
            case SUM:
                // SUM - sum numeric values
                double sum = 0;
                for (Row row : rows) {
                    context.setCurrentRow(row);
                    context.setJoinedColumns(columns);
                    Object value = expressionEvaluator.evaluate(aggregateFunc.getExpression(), context);
                    if (value instanceof Number) {
                        sum += ((Number) value).doubleValue();
                    }
                }
                return sum;
                
            case AVG:
                // AVG - average of numeric values
                double total = 0;
                int count = 0;
                for (Row row : rows) {
                    context.setCurrentRow(row);
                    context.setJoinedColumns(columns);
                    Object value = expressionEvaluator.evaluate(aggregateFunc.getExpression(), context);
                    if (value instanceof Number) {
                        total += ((Number) value).doubleValue();
                        count++;
                    }
                }
                return count > 0 ? total / count : null;
                
            case MIN:
                // MIN - minimum value
                Object min = null;
                for (Row row : rows) {
                    context.setCurrentRow(row);
                    context.setJoinedColumns(columns);
                    Object value = expressionEvaluator.evaluate(aggregateFunc.getExpression(), context);
                    if (value != null) {
                        if (min == null || compareObjects(value, min) < 0) {
                            min = value;
                        }
                    }
                }
                return min;
                
            case MAX:
                // MAX - maximum value
                Object max = null;
                for (Row row : rows) {
                    context.setCurrentRow(row);
                    context.setJoinedColumns(columns);
                    Object value = expressionEvaluator.evaluate(aggregateFunc.getExpression(), context);
                    if (value != null) {
                        if (max == null || compareObjects(value, max) > 0) {
                            max = value;
                        }
                    }
                }
                return max;
                
            default:
                throw new IllegalArgumentException("Unsupported aggregate function: " + aggregateFunc.getAggregateType());
        }
    }
    
    @SuppressWarnings("unchecked")
    private int compareObjects(Object a, Object b) {
        if (a instanceof Comparable && b instanceof Comparable) {
            try {
                return ((Comparable) a).compareTo(b);
            } catch (ClassCastException e) {
                return a.toString().compareTo(b.toString());
            }
        }
        return a.toString().compareTo(b.toString());
    }
    
    @Override
    public SqlExecutionResult visitCreateIndexStatement(CreateIndexStatement node, ExecutionContext context) throws Exception {
        String tableName = node.getTableName();
        String indexName = node.getIndexName();
        List<String> columnNames = new ArrayList<>();
        
        // Extract column names from IndexColumn objects
        for (CreateIndexStatement.IndexColumn indexCol : node.getIndexColumns()) {
            columnNames.add(indexCol.getColumnName());
        }
        
        // Get the table from the engine
        Table table = engine.getTable("public", tableName);
        if (table == null) {
            throw new IllegalArgumentException("Table does not exist: " + tableName);
        }
        
        try {
            // Create the index with H2-compatible options
            boolean created = table.createIndex(indexName, columnNames, node.isUnique(), node.isIfNotExists());
            
            String message;
            if (created) {
                message = "Index " + (indexName != null ? indexName : "unnamed") + " created successfully";
                logger.info("Created index on table {}: {}", tableName, message);
            } else {
                message = "Index " + indexName + " already exists, creation skipped";
                logger.info("Skipped index creation on table {}: {}", tableName, message);
            }
            
            return new SqlExecutionResult(SqlExecutionResult.ResultType.DDL, true, message);
        } catch (Exception e) {
            logger.error("Failed to create index on table {}: {}", tableName, e.getMessage());
            return new SqlExecutionResult(SqlExecutionResult.ResultType.DDL, false, "Failed to create index: " + e.getMessage());
        }
    }
    
    @Override
    public SqlExecutionResult visitMergeStatement(MergeStatement node, ExecutionContext context) throws Exception {
        try {
            if (node.isSimple()) {
                return executeSimpleMerge(node, context);
            } else {
                return executeAdvancedMerge(node, context);
            }
        } catch (Exception e) {
            throw new SqlExecutionException("Failed to execute MERGE statement", e);
        }
    }
    
    /**
     * Execute simple MERGE statement: MERGE INTO table KEY(columns) VALUES(...)
     */
    private SqlExecutionResult executeSimpleMerge(MergeStatement node, ExecutionContext context) throws Exception {
        String tableName = node.getTableName();
        Table table = engine.getTable("public", tableName);
        if (table == null) {
            throw new SqlExecutionException("Table not found: " + tableName);
        }
        
        List<Column> tableColumns = table.getColumns();
        List<String> keyColumns = node.getKeyColumns();
        int insertedRows = 0;
        int updatedRows = 0;
        
        // Validate key columns exist
        Map<String, Integer> columnIndexMap = new HashMap<>();
        for (int i = 0; i < tableColumns.size(); i++) {
            columnIndexMap.put(tableColumns.get(i).getName().toLowerCase(), i);
        }
        
        List<Integer> keyColumnIndices = new ArrayList<>();
        for (String keyColumn : keyColumns) {
            Integer index = columnIndexMap.get(keyColumn.toLowerCase());
            if (index == null) {
                throw new SqlExecutionException("Key column not found: " + keyColumn);
            }
            keyColumnIndices.add(index);
        }
        
        // Process each set of values
        for (List<Expression> values : node.getValuesList()) {
            if (values.size() != tableColumns.size()) {
                throw new SqlExecutionException("Column count mismatch: expected " + 
                    tableColumns.size() + ", got " + values.size());
            }
            
            // Evaluate values
            Object[] rowData = new Object[values.size()];
            for (int i = 0; i < values.size(); i++) {
                rowData[i] = expressionEvaluator.evaluate(values.get(i), context);
                
                // Convert and validate data type
                Column column = tableColumns.get(i);
                rowData[i] = column.getDataType().convertValue(rowData[i]);
                
                if (!column.getDataType().isValidValue(rowData[i])) {
                    throw new SqlExecutionException("Invalid value for column " + 
                        column.getName() + ": " + rowData[i]);
                }
            }
            
            // Find existing row by key columns
            Row existingRow = findRowByKeys(table, keyColumnIndices, rowData);
            
            if (existingRow != null) {
                // Update existing row
                table.updateRow(existingRow.getId(), rowData);
                updatedRows++;
            } else {
                // Insert new row
                table.insertRow(rowData);
                insertedRows++;
            }
        }
        
        String message = String.format("MERGE completed: %d rows inserted, %d rows updated", 
                                     insertedRows, updatedRows);
        logger.debug("Simple MERGE executed on {}: {}", tableName, message);
        
        return new SqlExecutionResult(SqlExecutionResult.ResultType.MERGE, 
                                    insertedRows + updatedRows);
    }
    
    /**
     * Execute advanced MERGE statement: MERGE INTO target USING source ON condition WHEN...
     */
    private SqlExecutionResult executeAdvancedMerge(MergeStatement node, ExecutionContext context) throws Exception {
        String tableName = node.getTableName();
        Table targetTable = engine.getTable("public", tableName);
        if (targetTable == null) {
            throw new SqlExecutionException("Target table not found: " + tableName);
        }
        
        // Get source rows
        List<Row> sourceRows = getSourceRows(node.getSource(), context);
        List<Row> targetRows = targetTable.getAllRows();
        
        int insertedRows = 0;
        int updatedRows = 0;
        int deletedRows = 0;
        
        List<Column> targetColumns = targetTable.getColumns();
        Map<String, Integer> targetColumnIndexMap = new HashMap<>();
        for (int i = 0; i < targetColumns.size(); i++) {
            targetColumnIndexMap.put(targetColumns.get(i).getName().toLowerCase(), i);
        }
        
        // Process each source row
        for (Row sourceRow : sourceRows) {
            context.setCurrentRow(sourceRow);
            
            // Find matching target row based on ON condition
            Row matchedTargetRow = null;
            for (Row targetRow : targetRows) {
                context.setCurrentRow(targetRow);
                context.setCurrentTable(targetTable);
                
                // Evaluate ON condition with both source and target context
                Object onResult = evaluateOnCondition(node.getOnCondition(), sourceRow, targetRow, context);
                if (Boolean.TRUE.equals(onResult)) {
                    matchedTargetRow = targetRow;
                    break;
                }
            }
            
            // Process WHEN clauses
            for (MergeStatement.WhenClause whenClause : node.getWhenClauses()) {
                boolean shouldApplyClause = (whenClause.isMatched() && matchedTargetRow != null) ||
                                          (!whenClause.isMatched() && matchedTargetRow == null);
                
                if (shouldApplyClause) {
                    // Check additional condition if present
                    boolean conditionMet = true;
                    if (whenClause.getAdditionalCondition() != null) {
                        Object conditionResult = expressionEvaluator.evaluate(
                            whenClause.getAdditionalCondition(), context);
                        conditionMet = Boolean.TRUE.equals(conditionResult);
                    }
                    
                    if (conditionMet) {
                        // Execute the action
                        MergeStatement.MergeAction action = whenClause.getAction();
                        if (action instanceof MergeStatement.UpdateAction) {
                            executeUpdateAction((MergeStatement.UpdateAction) action, 
                                             matchedTargetRow, targetTable, targetColumnIndexMap, context);
                            updatedRows++;
                        } else if (action instanceof MergeStatement.DeleteAction) {
                            targetTable.deleteRow(matchedTargetRow.getId());
                            deletedRows++;
                        } else if (action instanceof MergeStatement.InsertAction) {
                            executeInsertAction((MergeStatement.InsertAction) action, 
                                             targetTable, targetColumns, context);
                            insertedRows++;
                        }
                        break; // Only execute first matching WHEN clause
                    }
                }
            }
        }
        
        String message = String.format("MERGE completed: %d rows inserted, %d rows updated, %d rows deleted", 
                                     insertedRows, updatedRows, deletedRows);
        logger.debug("Advanced MERGE executed on {}: {}", tableName, message);
        
        return new SqlExecutionResult(SqlExecutionResult.ResultType.MERGE, 
                                    insertedRows + updatedRows + deletedRows);
    }
    
    /**
     * Find a row in the table by matching key column values.
     */
    private Row findRowByKeys(Table table, List<Integer> keyColumnIndices, Object[] values) {
        List<Row> allRows = table.getAllRows();
        
        for (Row row : allRows) {
            boolean matches = true;
            Object[] rowData = row.getData();
            
            for (Integer keyIndex : keyColumnIndices) {
                Object keyValue = values[keyIndex];
                Object rowValue = rowData[keyIndex];
                
                if (!Objects.equals(keyValue, rowValue)) {
                    matches = false;
                    break;
                }
            }
            
            if (matches) {
                return row;
            }
        }
        
        return null;
    }
    
    /**
     * Get source rows for advanced MERGE.
     */
    private List<Row> getSourceRows(MergeStatement.MergeSource source, ExecutionContext context) throws Exception {
        if (source instanceof MergeStatement.TableSource) {
            MergeStatement.TableSource tableSource = (MergeStatement.TableSource) source;
            Table sourceTable = engine.getTable("public", tableSource.getTableName());
            if (sourceTable == null) {
                throw new SqlExecutionException("Source table not found: " + tableSource.getTableName());
            }
            return sourceTable.getAllRows();
        } else if (source instanceof MergeStatement.SubquerySource) {
            MergeStatement.SubquerySource subquerySource = (MergeStatement.SubquerySource) source;
            SqlExecutionResult subqueryResult = (SqlExecutionResult) subquerySource.getSelectStatement().accept(this, context);
            return subqueryResult.getRows();
        } else {
            throw new SqlExecutionException("Unknown source type: " + source.getClass().getSimpleName());
        }
    }
    
    /**
     * Evaluate ON condition with source and target row context.
     */
    private Object evaluateOnCondition(Expression onCondition, Row sourceRow, Row targetRow, 
                                     ExecutionContext context) throws Exception {
        // This is simplified - in a full implementation, we'd need to handle 
        // source/target column references properly
        context.setCurrentRow(targetRow);
        return expressionEvaluator.evaluate(onCondition, context);
    }
    
    /**
     * Execute UPDATE action in WHEN MATCHED clause.
     */
    private void executeUpdateAction(MergeStatement.UpdateAction action, Row targetRow, Table targetTable,
                                   Map<String, Integer> columnIndexMap, ExecutionContext context) throws Exception {
        Object[] newData = targetRow.getData().clone();
        List<Column> columns = targetTable.getColumns();
        
        for (MergeStatement.UpdateItem updateItem : action.getUpdateItems()) {
            String columnName = updateItem.getColumnName().toLowerCase();
            Integer columnIndex = columnIndexMap.get(columnName);
            if (columnIndex == null) {
                throw new SqlExecutionException("Column not found: " + columnName);
            }
            
            Object newValue = expressionEvaluator.evaluate(updateItem.getExpression(), context);
            Column column = columns.get(columnIndex);
            
            // Convert and validate value
            newValue = column.getDataType().convertValue(newValue);
            if (!column.getDataType().isValidValue(newValue)) {
                throw new SqlExecutionException("Invalid value for column " + columnName + ": " + newValue);
            }
            
            newData[columnIndex] = newValue;
        }
        
        targetTable.updateRow(targetRow.getId(), newData);
    }
    
    /**
     * Execute INSERT action in WHEN NOT MATCHED clause.
     */
    private void executeInsertAction(MergeStatement.InsertAction action, Table targetTable, 
                                   List<Column> targetColumns, ExecutionContext context) throws Exception {
        List<Expression> values = action.getValues();
        List<String> specifiedColumns = action.getColumns();
        
        Object[] rowData;
        
        if (specifiedColumns != null && !specifiedColumns.isEmpty()) {
            // Insert into specific columns
            rowData = new Object[targetColumns.size()];
            
            Map<String, Integer> columnIndexMap = new HashMap<>();
            for (int i = 0; i < targetColumns.size(); i++) {
                columnIndexMap.put(targetColumns.get(i).getName().toLowerCase(), i);
            }
            
            for (int i = 0; i < specifiedColumns.size(); i++) {
                String columnName = specifiedColumns.get(i).toLowerCase();
                Integer columnIndex = columnIndexMap.get(columnName);
                if (columnIndex == null) {
                    throw new SqlExecutionException("Column not found: " + columnName);
                }
                
                Object value = expressionEvaluator.evaluate(values.get(i), context);
                Column column = targetColumns.get(columnIndex);
                value = column.getDataType().convertValue(value);
                
                if (!column.getDataType().isValidValue(value)) {
                    throw new SqlExecutionException("Invalid value for column " + columnName + ": " + value);
                }
                
                rowData[columnIndex] = value;
            }
        } else {
            // Insert into all columns
            if (values.size() != targetColumns.size()) {
                throw new SqlExecutionException("Column count mismatch: expected " + 
                    targetColumns.size() + ", got " + values.size());
            }
            
            rowData = new Object[values.size()];
            for (int i = 0; i < values.size(); i++) {
                rowData[i] = expressionEvaluator.evaluate(values.get(i), context);
                
                Column column = targetColumns.get(i);
                rowData[i] = column.getDataType().convertValue(rowData[i]);
                
                if (!column.getDataType().isValidValue(rowData[i])) {
                    throw new SqlExecutionException("Invalid value for column " + 
                        column.getName() + ": " + rowData[i]);
                }
            }
        }
        
        targetTable.insertRow(rowData);
    }
    
    @Override
    public SqlExecutionResult visitDropIndexStatement(DropIndexStatement node, ExecutionContext context) throws Exception {
        String indexName = node.getIndexName();
        
        // Note: In H2, DROP INDEX only requires the index name, not the table name
        // For now, we'll search through all tables to find the index
        // TODO: Implement schema-level index registry for better performance
        
        // Search for the index across all tables in the public schema
        Schema publicSchema = engine.getSchema("public");
        if (publicSchema == null) {
            throw new IllegalArgumentException("Default schema 'public' not found");
        }
        
        try {
            boolean found = false;
            for (String tableName : publicSchema.getTableNames()) {
                Table table = publicSchema.getTable(tableName);
                if (table != null && table.getIndex(indexName) != null) {
                    boolean dropped = table.dropIndex(indexName, node.isIfExists());
                    if (dropped) {
                        String message = "Index " + indexName + " dropped successfully";
                        logger.info("Dropped index {} from table {}", indexName, tableName);
                        return new SqlExecutionResult(SqlExecutionResult.ResultType.DDL, true, message);
                    }
                    found = true;
                    break;
                }
            }
            
            if (!found && !node.isIfExists()) {
                throw new IllegalArgumentException("Index does not exist: " + indexName);
            }
            
            String message = "Index " + indexName + (found ? " already dropped" : " does not exist, drop skipped");
            logger.info("Drop index {}: {}", indexName, message);
            return new SqlExecutionResult(SqlExecutionResult.ResultType.DDL, true, message);
            
        } catch (Exception e) {
            logger.error("Failed to drop index {}: {}", indexName, e.getMessage());
            return new SqlExecutionResult(SqlExecutionResult.ResultType.DDL, false, "Failed to drop index: " + e.getMessage());
        }
    }
}