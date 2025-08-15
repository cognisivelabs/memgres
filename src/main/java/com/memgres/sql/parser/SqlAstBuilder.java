package com.memgres.sql.parser;

import com.memgres.sql.MemGresParser;
import com.memgres.sql.MemGresParserBaseVisitor;
import com.memgres.sql.ast.expression.*;
import com.memgres.sql.ast.statement.*;
import com.memgres.types.DataType;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Visitor that converts ANTLR4 parse trees into our SQL AST nodes.
 */
public class SqlAstBuilder extends MemGresParserBaseVisitor<Object> {
    
    /**
     * Visit the top-level SQL context and return a list of statements.
     */
    public List<Statement> visit(MemGresParser.SqlContext ctx) {
        List<Statement> statements = new ArrayList<>();
        for (MemGresParser.StatementContext stmtCtx : ctx.statement()) {
            Statement stmt = (Statement) visit(stmtCtx);
            if (stmt != null) {
                statements.add(stmt);
            }
        }
        return statements;
    }
    
    @Override
    public Statement visitStatement(MemGresParser.StatementContext ctx) {
        if (ctx.selectStatement() != null) {
            return (Statement) visit(ctx.selectStatement());
        } else if (ctx.insertStatement() != null) {
            return (Statement) visit(ctx.insertStatement());
        } else if (ctx.updateStatement() != null) {
            return (Statement) visit(ctx.updateStatement());
        } else if (ctx.deleteStatement() != null) {
            return (Statement) visit(ctx.deleteStatement());
        } else if (ctx.mergeStatement() != null) {
            return (Statement) visit(ctx.mergeStatement());
        } else if (ctx.createTableStatement() != null) {
            return (Statement) visit(ctx.createTableStatement());
        } else if (ctx.alterTableStatement() != null) {
            return (Statement) visit(ctx.alterTableStatement());
        } else if (ctx.dropTableStatement() != null) {
            return (Statement) visit(ctx.dropTableStatement());
        } else if (ctx.truncateTableStatement() != null) {
            return (Statement) visit(ctx.truncateTableStatement());
        } else if (ctx.createViewStatement() != null) {
            return (Statement) visit(ctx.createViewStatement());
        } else if (ctx.dropViewStatement() != null) {
            return (Statement) visit(ctx.dropViewStatement());
        } else if (ctx.createIndexStatement() != null) {
            return (Statement) visit(ctx.createIndexStatement());
        } else if (ctx.dropIndexStatement() != null) {
            return (Statement) visit(ctx.dropIndexStatement());
        } else if (ctx.createSequenceStatement() != null) {
            return (Statement) visit(ctx.createSequenceStatement());
        } else if (ctx.dropSequenceStatement() != null) {
            return (Statement) visit(ctx.dropSequenceStatement());
        }
        return null;
    }
    
    @Override
    public SelectStatement visitSelectStatement(MemGresParser.SelectStatementContext ctx) {
        CompoundSelectStatement compoundSelect = visitCompoundSelectStatement(ctx.compoundSelectStatement());
        return new SelectStatement(compoundSelect);
    }
    
    @Override
    public CompoundSelectStatement visitCompoundSelectStatement(MemGresParser.CompoundSelectStatementContext ctx) {
        List<SimpleSelectStatement> selectStatements = new ArrayList<>();
        List<UnionClause> unionClauses = new ArrayList<>();
        
        // Process all simple SELECT statements
        for (MemGresParser.SimpleSelectStatementContext simpleCtx : ctx.simpleSelectStatement()) {
            selectStatements.add(visitSimpleSelectStatement(simpleCtx));
        }
        
        // Process all UNION clauses
        for (MemGresParser.UnionClauseContext unionCtx : ctx.unionClause()) {
            unionClauses.add(visitUnionClause(unionCtx));
        }
        
        return new CompoundSelectStatement(selectStatements, unionClauses);
    }
    
    @Override
    public SimpleSelectStatement visitSimpleSelectStatement(MemGresParser.SimpleSelectStatementContext ctx) {
        // Parse DISTINCT
        boolean distinct = ctx.selectModifier() != null && 
                          ctx.selectModifier().DISTINCT() != null;
        
        // Parse SELECT list
        List<SelectItem> selectItems = new ArrayList<>();
        if (ctx.selectList().MULTIPLY() != null) {
            selectItems.add(new SelectItem()); // Wildcard
        } else {
            for (MemGresParser.SelectItemContext itemCtx : ctx.selectList().selectItem()) {
                selectItems.add((SelectItem) visit(itemCtx));
            }
        }
        
        // Parse FROM clause (optional for subqueries)
        Optional<FromClause> fromClause = ctx.fromClause() != null ?
            Optional.of((FromClause) visit(ctx.fromClause())) : Optional.empty();
        
        // Parse optional clauses
        Optional<WhereClause> whereClause = ctx.whereClause() != null ? 
            Optional.of((WhereClause) visit(ctx.whereClause())) : Optional.empty();
            
        Optional<GroupByClause> groupByClause = ctx.groupByClause() != null ?
            Optional.of((GroupByClause) visit(ctx.groupByClause())) : Optional.empty();
            
        Optional<HavingClause> havingClause = ctx.havingClause() != null ?
            Optional.of((HavingClause) visit(ctx.havingClause())) : Optional.empty();
            
        Optional<OrderByClause> orderByClause = ctx.orderByClause() != null ?
            Optional.of((OrderByClause) visit(ctx.orderByClause())) : Optional.empty();
            
        Optional<LimitClause> limitClause = ctx.limitClause() != null ?
            Optional.of((LimitClause) visit(ctx.limitClause())) : Optional.empty();
            
        // Parse optional WITH clause
        Optional<WithClause> withClause = ctx.withClause() != null ?
            Optional.of((WithClause) visit(ctx.withClause())) : Optional.empty();
        
        return new SimpleSelectStatement(withClause, distinct, selectItems, fromClause, whereClause,
                                       groupByClause, havingClause, orderByClause, limitClause);
    }
    
    @Override
    public UnionClause visitUnionClause(MemGresParser.UnionClauseContext ctx) {
        UnionClause.UnionType unionType;
        
        if (ctx.INTERSECT() != null) {
            unionType = UnionClause.UnionType.INTERSECT;
        } else if (ctx.EXCEPT() != null) {
            unionType = UnionClause.UnionType.EXCEPT;
        } else if (ctx.UNION() != null) {
            unionType = ctx.ALL() != null ? 
                UnionClause.UnionType.UNION_ALL : UnionClause.UnionType.UNION;
        } else {
            throw new IllegalArgumentException("Unknown union clause type");
        }
        
        return new UnionClause(unionType);
    }
    
    @Override
    public SelectItem visitSelectItem(MemGresParser.SelectItemContext ctx) {
        Expression expression = (Expression) visit(ctx.expression());
        Optional<String> alias = ctx.alias() != null ? 
            Optional.of(ctx.alias().getText()) : Optional.empty();
        return new SelectItem(expression, alias);
    }
    
    @Override
    public FromClause visitFromClause(MemGresParser.FromClauseContext ctx) {
        List<JoinableTable> joinableTables = new ArrayList<>();
        for (MemGresParser.JoinableTableContext joinableCtx : ctx.joinableTable()) {
            joinableTables.add((JoinableTable) visit(joinableCtx));
        }
        return new FromClause(joinableTables);
    }
    
    @Override
    public JoinableTable visitJoinableTable(MemGresParser.JoinableTableContext ctx) {
        // Get the base table reference
        TableReference baseTable = (TableReference) visit(ctx.tableReference());
        
        // Get all join clauses
        List<JoinClause> joins = new ArrayList<>();
        for (MemGresParser.JoinClauseContext joinCtx : ctx.joinClause()) {
            joins.add((JoinClause) visit(joinCtx));
        }
        
        return new JoinableTable(baseTable, joins);
    }
    
    @Override
    public JoinClause visitJoinClause(MemGresParser.JoinClauseContext ctx) {
        // Get join type
        JoinClause.JoinType joinType = getJoinType(ctx.joinType());
        
        // Get the table being joined
        TableReference table = (TableReference) visit(ctx.tableReference());
        
        // Get join condition
        Optional<Expression> onCondition = Optional.empty();
        if (ctx.joinCondition() != null) {
            MemGresParser.JoinConditionContext condCtx = ctx.joinCondition();
            // Check if it's an ON condition (onJoinCondition alternative)
            if (condCtx instanceof MemGresParser.OnJoinConditionContext) {
                MemGresParser.OnJoinConditionContext onCtx = (MemGresParser.OnJoinConditionContext) condCtx;
                Expression condition = (Expression) visit(onCtx.expression());
                onCondition = Optional.of(condition);
            }
            // TODO: Implement USING and NATURAL joins
        }
        // Note: USING and NATURAL joins are not implemented yet
        
        return new JoinClause(joinType, table, onCondition);
    }
    
    /**
     * Convert ANTLR join type context to JoinClause.JoinType enum.
     */
    private JoinClause.JoinType getJoinType(MemGresParser.JoinTypeContext ctx) {
        if (ctx == null) {
            return JoinClause.JoinType.INNER; // Default to INNER JOIN
        }
        
        if (ctx instanceof MemGresParser.InnerJoinContext) {
            return JoinClause.JoinType.INNER;
        } else if (ctx instanceof MemGresParser.LeftJoinContext) {
            return JoinClause.JoinType.LEFT;
        } else if (ctx instanceof MemGresParser.RightJoinContext) {
            return JoinClause.JoinType.RIGHT;
        } else if (ctx instanceof MemGresParser.FullOuterJoinContext) {
            return JoinClause.JoinType.FULL_OUTER;
        } else {
            return JoinClause.JoinType.INNER; // Default fallback
        }
    }
    
    @Override
    public TableReference visitTableReference(MemGresParser.TableReferenceContext ctx) {
        String tableName = ctx.tableName().getText();
        Optional<String> alias = ctx.alias() != null ? 
            Optional.of(ctx.alias().getText()) : Optional.empty();
        return new TableReference(tableName, alias);
    }
    
    @Override
    public WhereClause visitWhereClause(MemGresParser.WhereClauseContext ctx) {
        Expression condition = (Expression) visit(ctx.expression());
        return new WhereClause(condition);
    }
    
    @Override
    public OrderByClause visitOrderByClause(MemGresParser.OrderByClauseContext ctx) {
        List<OrderByClause.OrderItem> items = new ArrayList<>();
        for (MemGresParser.OrderItemContext itemCtx : ctx.orderItem()) {
            Expression expr = (Expression) visit(itemCtx.expression());
            boolean ascending = itemCtx.DESC() == null; // Default to ASC
            items.add(new OrderByClause.OrderItem(expr, ascending));
        }
        return new OrderByClause(items);
    }
    
    @Override
    public GroupByClause visitGroupByClause(MemGresParser.GroupByClauseContext ctx) {
        List<Expression> expressions = new ArrayList<>();
        for (MemGresParser.ExpressionContext exprCtx : ctx.expression()) {
            expressions.add((Expression) visit(exprCtx));
        }
        return new GroupByClause(expressions);
    }
    
    @Override
    public HavingClause visitHavingClause(MemGresParser.HavingClauseContext ctx) {
        Expression condition = (Expression) visit(ctx.expression());
        return new HavingClause(condition);
    }
    
    @Override
    public LimitClause visitLimitClause(MemGresParser.LimitClauseContext ctx) {
        Expression limit = (Expression) visit(ctx.expression(0));
        Optional<Expression> offset = ctx.expression().size() > 1 ?
            Optional.of((Expression) visit(ctx.expression(1))) : Optional.empty();
        return new LimitClause(limit, offset);
    }
    
    // Expression visitors
    @Override
    public Expression visitLiteralExpression(MemGresParser.LiteralExpressionContext ctx) {
        return (Expression) visit(ctx.literal());
    }
    
    @Override
    public LiteralExpression visitStringLiteral(MemGresParser.StringLiteralContext ctx) {
        String text = ctx.STRING().getText();
        // Remove quotes
        String value = text.substring(1, text.length() - 1);
        return new LiteralExpression(value, LiteralExpression.LiteralType.STRING);
    }
    
    @Override
    public LiteralExpression visitIntegerLiteral(MemGresParser.IntegerLiteralContext ctx) {
        Long value = Long.parseLong(ctx.INTEGER_LITERAL().getText());
        return new LiteralExpression(value, LiteralExpression.LiteralType.INTEGER);
    }
    
    @Override
    public LiteralExpression visitDecimalLiteral(MemGresParser.DecimalLiteralContext ctx) {
        String decimalText = ctx.DECIMAL_LITERAL().getText();
        BigDecimal value = new BigDecimal(decimalText);
        return new LiteralExpression(value, LiteralExpression.LiteralType.DECIMAL);
    }
    
    @Override
    public LiteralExpression visitBooleanLiteral(MemGresParser.BooleanLiteralContext ctx) {
        Boolean value = ctx.TRUE() != null;
        return new LiteralExpression(value, LiteralExpression.LiteralType.BOOLEAN);
    }
    
    @Override
    public LiteralExpression visitNullLiteral(MemGresParser.NullLiteralContext ctx) {
        return new LiteralExpression(null, LiteralExpression.LiteralType.NULL);
    }
    
    @Override
    public Expression visitColumnReferenceExpression(MemGresParser.ColumnReferenceExpressionContext ctx) {
        return (Expression) visit(ctx.columnReference());
    }
    
    @Override
    public ColumnReference visitColumnReference(MemGresParser.ColumnReferenceContext ctx) {
        if (ctx.tableName() != null) {
            String tableName = ctx.tableName().getText();
            String columnName = ctx.columnName().getText();
            return new ColumnReference(tableName, columnName);
        } else {
            String columnName = ctx.columnName().getText();
            return new ColumnReference(columnName);
        }
    }
    
    @Override
    public Expression visitBinaryExpression(MemGresParser.BinaryExpressionContext ctx) {
        Expression left = (Expression) visit(ctx.expression(0));
        Expression right = (Expression) visit(ctx.expression(1));
        BinaryExpression.Operator operator = getBinaryOperator(ctx.binaryOperator());
        return new BinaryExpression(left, operator, right);
    }
    
    @Override
    public Expression visitFunctionCallExpression(MemGresParser.FunctionCallExpressionContext ctx) {
        return (Expression) visit(ctx.functionCall());
    }
    
    @Override
    public FunctionCall visitGenRandomUuidFunction(MemGresParser.GenRandomUuidFunctionContext ctx) {
        return new FunctionCall("gen_random_uuid", List.of());
    }
    
    @Override
    public FunctionCall visitUuidGenerateV1Function(MemGresParser.UuidGenerateV1FunctionContext ctx) {
        return new FunctionCall("uuid_generate_v1", List.of());
    }
    
    @Override
    public FunctionCall visitUuidGenerateV4Function(MemGresParser.UuidGenerateV4FunctionContext ctx) {
        return new FunctionCall("uuid_generate_v4", List.of());
    }
    
    @Override
    public FunctionCall visitGenericFunction(MemGresParser.GenericFunctionContext ctx) {
        String functionName = ctx.identifier().getText();
        List<Expression> arguments = new ArrayList<>();
        if (ctx.expressionList() != null) {
            for (MemGresParser.ExpressionContext exprCtx : ctx.expressionList().expression()) {
                arguments.add((Expression) visit(exprCtx));
            }
        }
        return new FunctionCall(functionName, arguments);
    }
    
    @Override
    public SubqueryExpression visitSubqueryExpression(MemGresParser.SubqueryExpressionContext ctx) {
        SelectStatement selectStatement = (SelectStatement) visit(ctx.selectStatement());
        return new SubqueryExpression(selectStatement);
    }
    
    @Override
    public ExistsExpression visitExistsExpression(MemGresParser.ExistsExpressionContext ctx) {
        SelectStatement subquery = (SelectStatement) visit(ctx.selectStatement());
        return new ExistsExpression(subquery);
    }
    
    @Override
    public InSubqueryExpression visitInSubqueryExpression(MemGresParser.InSubqueryExpressionContext ctx) {
        Expression expression = (Expression) visit(ctx.expression());
        SelectStatement subquery = (SelectStatement) visit(ctx.selectStatement());
        boolean negated = ctx.NOT() != null;
        return new InSubqueryExpression(expression, subquery, negated);
    }
    
    @Override
    public AggregateFunction visitCountFunction(MemGresParser.CountFunctionContext ctx) {
        Expression expr = null;
        if (ctx.MULTIPLY() == null && ctx.expression() != null) {
            expr = (Expression) visit(ctx.expression());
        }
        
        Optional<OverClause> overClause = Optional.empty();
        if (ctx.overClause() != null) {
            overClause = Optional.of((OverClause) visit(ctx.overClause()));
        }
        
        return new AggregateFunction(AggregateFunction.AggregateType.COUNT, expr, overClause);
    }
    
    @Override
    public AggregateFunction visitSumFunction(MemGresParser.SumFunctionContext ctx) {
        Expression expr = (Expression) visit(ctx.expression());
        
        Optional<OverClause> overClause = Optional.empty();
        if (ctx.overClause() != null) {
            overClause = Optional.of((OverClause) visit(ctx.overClause()));
        }
        
        return new AggregateFunction(AggregateFunction.AggregateType.SUM, expr, overClause);
    }
    
    @Override
    public AggregateFunction visitAvgFunction(MemGresParser.AvgFunctionContext ctx) {
        Expression expr = (Expression) visit(ctx.expression());
        
        Optional<OverClause> overClause = Optional.empty();
        if (ctx.overClause() != null) {
            overClause = Optional.of((OverClause) visit(ctx.overClause()));
        }
        
        return new AggregateFunction(AggregateFunction.AggregateType.AVG, expr, overClause);
    }
    
    @Override
    public AggregateFunction visitMinFunction(MemGresParser.MinFunctionContext ctx) {
        Expression expr = (Expression) visit(ctx.expression());
        
        Optional<OverClause> overClause = Optional.empty();
        if (ctx.overClause() != null) {
            overClause = Optional.of((OverClause) visit(ctx.overClause()));
        }
        
        return new AggregateFunction(AggregateFunction.AggregateType.MIN, expr, overClause);
    }
    
    @Override
    public AggregateFunction visitMaxFunction(MemGresParser.MaxFunctionContext ctx) {
        Expression expr = (Expression) visit(ctx.expression());
        
        Optional<OverClause> overClause = Optional.empty();
        if (ctx.overClause() != null) {
            overClause = Optional.of((OverClause) visit(ctx.overClause()));
        }
        
        return new AggregateFunction(AggregateFunction.AggregateType.MAX, expr, overClause);
    }
    
    @Override
    public AggregateFunction visitCountDistinctFunction(MemGresParser.CountDistinctFunctionContext ctx) {
        Expression expr = (Expression) visit(ctx.expression());
        return new AggregateFunction(AggregateFunction.AggregateType.COUNT_DISTINCT, expr);
    }
    
    // INSERT statement
    @Override
    public InsertStatement visitInsertStatement(MemGresParser.InsertStatementContext ctx) {
        String tableName = ctx.tableName().getText();
        
        Optional<List<String>> columns = Optional.empty();
        if (ctx.columnList() != null) {
            List<String> columnNames = ctx.columnList().columnName().stream()
                .map(c -> c.getText())
                .collect(Collectors.toList());
            columns = Optional.of(columnNames);
        }
        
        List<List<Expression>> valuesList = new ArrayList<>();
        for (MemGresParser.ValuesClauseContext valuesCtx : ctx.valuesClause()) {
            List<Expression> values = new ArrayList<>();
            for (MemGresParser.ExpressionContext exprCtx : valuesCtx.expression()) {
                values.add((Expression) visit(exprCtx));
            }
            valuesList.add(values);
        }
        
        return new InsertStatement(tableName, columns, valuesList);
    }
    
    // UPDATE statement
    @Override
    public UpdateStatement visitUpdateStatement(MemGresParser.UpdateStatementContext ctx) {
        String tableName = ctx.tableName().getText();
        
        List<UpdateStatement.UpdateItem> updateItems = new ArrayList<>();
        for (MemGresParser.UpdateItemContext updateCtx : ctx.updateItem()) {
            String columnName = updateCtx.columnName().getText();
            Expression value = (Expression) visit(updateCtx.expression());
            updateItems.add(new UpdateStatement.UpdateItem(columnName, value));
        }
        
        Optional<WhereClause> whereClause = ctx.whereClause() != null ? 
            Optional.of((WhereClause) visit(ctx.whereClause())) : Optional.empty();
        
        return new UpdateStatement(tableName, updateItems, whereClause);
    }
    
    // DELETE statement
    @Override
    public DeleteStatement visitDeleteStatement(MemGresParser.DeleteStatementContext ctx) {
        String tableName = ctx.tableName().getText();
        
        Optional<WhereClause> whereClause = ctx.whereClause() != null ? 
            Optional.of((WhereClause) visit(ctx.whereClause())) : Optional.empty();
        
        return new DeleteStatement(tableName, whereClause);
    }
    
    // MERGE statement - supports both simple and advanced syntax
    @Override
    public MergeStatement visitMergeStatement(MemGresParser.MergeStatementContext ctx) {
        if (ctx.simpleMergeStatement() != null) {
            return visitSimpleMergeStatement(ctx.simpleMergeStatement());
        } else {
            return visitAdvancedMergeStatement(ctx.advancedMergeStatement());
        }
    }
    
    @Override
    public MergeStatement visitSimpleMergeStatement(MemGresParser.SimpleMergeStatementContext ctx) {
        String tableName = ctx.tableName().getText();
        
        // Parse key columns
        List<String> keyColumns = new ArrayList<>();
        for (MemGresParser.IdentifierContext keyCol : ctx.keyColumnList().identifier()) {
            keyColumns.add(keyCol.getText());
        }
        
        // Parse values clauses
        List<List<Expression>> valuesList = new ArrayList<>();
        for (MemGresParser.ValuesClauseContext valuesCtx : ctx.valuesClause()) {
            List<Expression> values = new ArrayList<>();
            if (valuesCtx.expression() != null) {
                for (MemGresParser.ExpressionContext exprCtx : valuesCtx.expression()) {
                    values.add((Expression) visit(exprCtx));
                }
            }
            valuesList.add(values);
        }
        
        return new MergeStatement(tableName, keyColumns, valuesList);
    }
    
    @Override
    public MergeStatement visitAdvancedMergeStatement(MemGresParser.AdvancedMergeStatementContext ctx) {
        String tableName = ctx.tableName().getText();
        String tableAlias = ctx.alias() != null && ctx.alias().size() > 0 ? ctx.alias(0).getText() : null;
        
        // Parse source - delegate to the mergeSource visitor
        MergeStatement.MergeSource source = (MergeStatement.MergeSource) visit(ctx.mergeSource());
        
        String sourceAlias = ctx.alias() != null && ctx.alias().size() > 1 ? ctx.alias(1).getText() : null;
        
        // Parse ON condition
        Expression onCondition = (Expression) visit(ctx.expression());
        
        // Parse WHEN clauses
        List<MergeStatement.WhenClause> whenClauses = new ArrayList<>();
        for (MemGresParser.MergeWhenClauseContext whenCtx : ctx.mergeWhenClause()) {
            // Check if it's WHEN MATCHED (no NOT) or WHEN NOT MATCHED (has NOT)
            boolean matched = whenCtx.MATCHED() != null && whenCtx.NOT() == null;
            Expression additionalCondition = null;
            
            // Check for additional AND condition
            if (whenCtx.expression() != null) {
                additionalCondition = (Expression) visit(whenCtx.expression());
            }
            
            // Parse action based on the WHEN clause type
            MergeStatement.MergeAction action;
            if (matched) {
                // WHEN MATCHED - can have UPDATE or DELETE action
                if (whenCtx.mergeAction() != null) {
                    if (whenCtx.mergeAction().UPDATE() != null) {
                        // UPDATE action
                        List<MergeStatement.UpdateItem> updateItems = new ArrayList<>();
                        for (MemGresParser.UpdateItemContext updateCtx : whenCtx.mergeAction().updateItem()) {
                            String columnName = updateCtx.columnName().getText();
                            Expression expression = (Expression) visit(updateCtx.expression());
                            updateItems.add(new MergeStatement.UpdateItem(columnName, expression));
                        }
                        action = new MergeStatement.UpdateAction(updateItems);
                    } else {
                        // DELETE action
                        action = new MergeStatement.DeleteAction();
                    }
                } else {
                    throw new IllegalStateException("WHEN MATCHED clause must have an action");
                }
            } else {
                // WHEN NOT MATCHED - can only have INSERT action
                if (whenCtx.mergeInsertAction() != null) {
                    List<String> columns = null;
                    if (whenCtx.mergeInsertAction().columnList() != null) {
                        columns = new ArrayList<>();
                        for (MemGresParser.ColumnNameContext colCtx : whenCtx.mergeInsertAction().columnList().columnName()) {
                            columns.add(colCtx.getText());
                        }
                    }
                    
                    List<Expression> values = new ArrayList<>();
                    for (MemGresParser.ExpressionContext exprCtx : whenCtx.mergeInsertAction().valuesClause().expression()) {
                        values.add((Expression) visit(exprCtx));
                    }
                    
                    action = new MergeStatement.InsertAction(columns, values);
                } else {
                    throw new IllegalStateException("WHEN NOT MATCHED clause must have an INSERT action");
                }
            }
            
            whenClauses.add(new MergeStatement.WhenClause(matched, additionalCondition, action));
        }
        
        return new MergeStatement(tableName, tableAlias, source, sourceAlias, onCondition, whenClauses);
    }
    
    @Override
    public MergeStatement.MergeSource visitTableSource(MemGresParser.TableSourceContext ctx) {
        String tableName = ctx.tableName().getText();
        return new MergeStatement.TableSource(tableName);
    }
    
    @Override
    public MergeStatement.MergeSource visitSubquerySource(MemGresParser.SubquerySourceContext ctx) {
        SelectStatement selectStatement = visitSelectStatement(ctx.selectStatement());
        return new MergeStatement.SubquerySource(selectStatement);
    }
    
    // CREATE TABLE statement
    @Override
    public CreateTableStatement visitCreateTableStatement(MemGresParser.CreateTableStatementContext ctx) {
        String tableName = ctx.tableName().getText();
        
        List<ColumnDefinition> columnDefinitions = new ArrayList<>();
        for (MemGresParser.ColumnDefinitionContext colCtx : ctx.columnDefinition()) {
            String columnName = colCtx.columnName().getText();
            DataTypeNode dataType = (DataTypeNode) visit(colCtx.dataType());
            
            List<ColumnDefinition.Constraint> constraints = new ArrayList<>();
            if (colCtx.columnConstraint() != null) {
                for (MemGresParser.ColumnConstraintContext constraintCtx : colCtx.columnConstraint()) {
                    if (constraintCtx.NOT() != null && constraintCtx.NULL() != null) {
                        constraints.add(ColumnDefinition.Constraint.NOT_NULL);
                    } else if (constraintCtx.PRIMARY() != null && constraintCtx.KEY() != null) {
                        constraints.add(ColumnDefinition.Constraint.PRIMARY_KEY);
                    } else if (constraintCtx.UNIQUE() != null) {
                        constraints.add(ColumnDefinition.Constraint.UNIQUE);
                    }
                }
            }
            
            columnDefinitions.add(new ColumnDefinition(columnName, dataType, constraints));
        }
        
        return new CreateTableStatement(tableName, columnDefinitions);
    }
    
    // DROP TABLE statement
    @Override
    public AlterTableStatement visitAlterTableStatement(MemGresParser.AlterTableStatementContext ctx) {
        String tableName = ctx.tableName().getText();
        boolean ifExists = ctx.IF() != null && ctx.EXISTS() != null;
        
        AlterTableAction action = (AlterTableAction) visit(ctx.alterTableAction());
        
        return new AlterTableStatement(tableName, ifExists, action);
    }
    
    @Override
    public AlterTableAction visitAddColumnAction(MemGresParser.AddColumnActionContext ctx) {
        // Manually construct ColumnDefinition like in CREATE TABLE
        MemGresParser.ColumnDefinitionContext colCtx = ctx.columnDefinition();
        String columnName = colCtx.columnName().getText();
        DataTypeNode dataType = (DataTypeNode) visit(colCtx.dataType());
        
        List<ColumnDefinition.Constraint> constraints = new ArrayList<>();
        if (colCtx.columnConstraint() != null) {
            for (MemGresParser.ColumnConstraintContext constraintCtx : colCtx.columnConstraint()) {
                if (constraintCtx.NOT() != null && constraintCtx.NULL() != null) {
                    constraints.add(ColumnDefinition.Constraint.NOT_NULL);
                } else if (constraintCtx.PRIMARY() != null && constraintCtx.KEY() != null) {
                    constraints.add(ColumnDefinition.Constraint.PRIMARY_KEY);
                } else if (constraintCtx.UNIQUE() != null) {
                    constraints.add(ColumnDefinition.Constraint.UNIQUE);
                } else if (constraintCtx.NULL() != null && constraintCtx.NOT() == null) {
                    constraints.add(ColumnDefinition.Constraint.NULL);
                }
            }
        }
        
        ColumnDefinition columnDefinition = new ColumnDefinition(columnName, dataType, constraints);
        
        AddColumnAction.Position position = AddColumnAction.Position.DEFAULT;
        String referenceColumnName = null;
        
        if (ctx.BEFORE() != null) {
            position = AddColumnAction.Position.BEFORE;
            // The columnName after BEFORE is the reference column
            if (ctx.columnName() != null) {
                referenceColumnName = ctx.columnName().getText(); // The reference column
            }
        } else if (ctx.AFTER() != null) {
            position = AddColumnAction.Position.AFTER;
            // The columnName after AFTER is the reference column
            if (ctx.columnName() != null) {
                referenceColumnName = ctx.columnName().getText(); // The reference column
            }
        }
        
        return new AddColumnAction(columnDefinition, position, referenceColumnName);
    }
    
    @Override
    public AlterTableAction visitDropColumnAction(MemGresParser.DropColumnActionContext ctx) {
        String columnName = ctx.columnName().getText();
        boolean ifExists = ctx.IF() != null && ctx.EXISTS() != null;
        
        return new DropColumnAction(columnName, ifExists);
    }
    
    @Override
    public AlterTableAction visitRenameColumnAction(MemGresParser.RenameColumnActionContext ctx) {
        // ALTER COLUMN oldName RENAME TO newName
        List<MemGresParser.ColumnNameContext> columnNames = ctx.columnName();
        String oldColumnName = columnNames.get(0).getText();
        String newColumnName = columnNames.get(1).getText();
        
        return new RenameColumnAction(oldColumnName, newColumnName);
    }
    
    @Override
    public AlterTableAction visitRenameTableAction(MemGresParser.RenameTableActionContext ctx) {
        String newTableName = ctx.tableName().getText();
        
        return new RenameTableAction(newTableName);
    }

    @Override
    public DropTableStatement visitDropTableStatement(MemGresParser.DropTableStatementContext ctx) {
        String tableName = ctx.tableName().getText();
        return new DropTableStatement(tableName);
    }
    
    @Override
    public TruncateTableStatement visitTruncateTableStatement(MemGresParser.TruncateTableStatementContext ctx) {
        String tableName = ctx.tableName().getText();
        
        TruncateTableStatement.IdentityOption identityOption = null;
        if (ctx.identityOption() != null) {
            if (ctx.identityOption() instanceof MemGresParser.RestartIdentityOptionContext) {
                identityOption = TruncateTableStatement.IdentityOption.RESTART_IDENTITY;
            } else if (ctx.identityOption() instanceof MemGresParser.ContinueIdentityOptionContext) {
                identityOption = TruncateTableStatement.IdentityOption.CONTINUE_IDENTITY;
            }
        }
        
        return new TruncateTableStatement(tableName, identityOption);
    }
    
    @Override
    public CreateViewStatement visitCreateViewStatement(MemGresParser.CreateViewStatementContext ctx) {
        boolean orReplace = ctx.OR() != null && ctx.REPLACE() != null;
        boolean force = ctx.FORCE() != null;
        boolean ifNotExists = ctx.IF() != null && ctx.NOT() != null && ctx.EXISTS() != null;
        String viewName = ctx.viewName().getText();
        
        // Extract column names if specified
        List<String> columnNames = null;
        if (ctx.columnNameList() != null) {
            columnNames = new ArrayList<>();
            for (MemGresParser.ColumnNameContext colCtx : ctx.columnNameList().columnName()) {
                columnNames.add(colCtx.getText());
            }
        }
        
        // Get the SELECT statement
        SelectStatement selectStatement = (SelectStatement) visit(ctx.selectStatement());
        
        return new CreateViewStatement(orReplace, force, ifNotExists, viewName, columnNames, selectStatement);
    }
    
    @Override
    public DropViewStatement visitDropViewStatement(MemGresParser.DropViewStatementContext ctx) {
        boolean ifExists = ctx.IF() != null && ctx.EXISTS() != null;
        String viewName = ctx.viewName().getText();
        
        DropViewStatement.DropOption dropOption = null;
        if (ctx.restrictOrCascade() != null) {
            if (ctx.restrictOrCascade() instanceof MemGresParser.RestrictOptionContext) {
                dropOption = DropViewStatement.DropOption.RESTRICT;
            } else if (ctx.restrictOrCascade() instanceof MemGresParser.CascadeOptionContext) {
                dropOption = DropViewStatement.DropOption.CASCADE;
            }
        }
        
        return new DropViewStatement(ifExists, viewName, dropOption);
    }
    
    // Data type visitors
    @Override
    public DataTypeNode visitIntegerType(MemGresParser.IntegerTypeContext ctx) {
        return new DataTypeNode(DataType.INTEGER);
    }
    
    @Override
    public DataTypeNode visitBigintType(MemGresParser.BigintTypeContext ctx) {
        return new DataTypeNode(DataType.BIGINT);
    }
    
    @Override
    public DataTypeNode visitSmallintType(MemGresParser.SmallintTypeContext ctx) {
        return new DataTypeNode(DataType.SMALLINT);
    }
    
    @Override
    public DataTypeNode visitVarcharType(MemGresParser.VarcharTypeContext ctx) {
        return new DataTypeNode(DataType.VARCHAR);
    }
    
    @Override
    public DataTypeNode visitTextType(MemGresParser.TextTypeContext ctx) {
        return new DataTypeNode(DataType.TEXT);
    }
    
    @Override
    public DataTypeNode visitCharType(MemGresParser.CharTypeContext ctx) {
        return new DataTypeNode(DataType.CHAR);
    }
    
    @Override
    public DataTypeNode visitBooleanType(MemGresParser.BooleanTypeContext ctx) {
        return new DataTypeNode(DataType.BOOLEAN);
    }
    
    @Override
    public DataTypeNode visitUuidType(MemGresParser.UuidTypeContext ctx) {
        return new DataTypeNode(DataType.UUID);
    }
    
    @Override
    public DataTypeNode visitJsonbType(MemGresParser.JsonbTypeContext ctx) {
        return new DataTypeNode(DataType.JSONB);
    }
    
    @Override
    public DataTypeNode visitRealType(MemGresParser.RealTypeContext ctx) {
        return new DataTypeNode(DataType.REAL);
    }
    
    @Override
    public DataTypeNode visitDoublePrecisionType(MemGresParser.DoublePrecisionTypeContext ctx) {
        return new DataTypeNode(DataType.DOUBLE_PRECISION);
    }
    
    @Override
    public DataTypeNode visitDecimalType(MemGresParser.DecimalTypeContext ctx) {
        return new DataTypeNode(DataType.DECIMAL);
    }
    
    @Override
    public DataTypeNode visitNumericType(MemGresParser.NumericTypeContext ctx) {
        return new DataTypeNode(DataType.DECIMAL);
    }
    
    @Override
    public DataTypeNode visitDateType(MemGresParser.DateTypeContext ctx) {
        return new DataTypeNode(DataType.DATE);
    }
    
    @Override
    public DataTypeNode visitTimeType(MemGresParser.TimeTypeContext ctx) {
        return new DataTypeNode(DataType.TIME);
    }
    
    @Override
    public DataTypeNode visitTimestampType(MemGresParser.TimestampTypeContext ctx) {
        return new DataTypeNode(DataType.TIMESTAMP);
    }
    
    @Override
    public DataTypeNode visitTimestamptzType(MemGresParser.TimestamptzTypeContext ctx) {
        return new DataTypeNode(DataType.TIMESTAMPTZ);
    }
    
    @Override
    public DataTypeNode visitByteaType(MemGresParser.ByteaTypeContext ctx) {
        return new DataTypeNode(DataType.BYTEA);
    }
    
    /**
     * Convert ANTLR4 binary operator context to our BinaryExpression.Operator enum.
     */
    private BinaryExpression.Operator getBinaryOperator(MemGresParser.BinaryOperatorContext ctx) {
        if (ctx.EQ() != null) return BinaryExpression.Operator.EQUALS;
        if (ctx.NE() != null) return BinaryExpression.Operator.NOT_EQUALS;
        if (ctx.LT() != null) return BinaryExpression.Operator.LESS_THAN;
        if (ctx.LE() != null) return BinaryExpression.Operator.LESS_THAN_EQUALS;
        if (ctx.GT() != null) return BinaryExpression.Operator.GREATER_THAN;
        if (ctx.GE() != null) return BinaryExpression.Operator.GREATER_THAN_EQUALS;
        if (ctx.PLUS() != null) return BinaryExpression.Operator.ADD;
        if (ctx.MINUS() != null) return BinaryExpression.Operator.SUBTRACT;
        if (ctx.MULTIPLY() != null) return BinaryExpression.Operator.MULTIPLY;
        if (ctx.DIVIDE() != null) return BinaryExpression.Operator.DIVIDE;
        if (ctx.MODULO() != null) return BinaryExpression.Operator.MODULO;
        if (ctx.EXPONENT() != null) return BinaryExpression.Operator.POWER;
        if (ctx.AND() != null) return BinaryExpression.Operator.AND;
        if (ctx.OR() != null) return BinaryExpression.Operator.OR;
        if (ctx.CONCAT() != null) return BinaryExpression.Operator.CONCAT;
        if (ctx.JSONB_CONTAINS() != null) return BinaryExpression.Operator.JSONB_CONTAINS;
        if (ctx.JSONB_CONTAINED() != null) return BinaryExpression.Operator.JSONB_CONTAINED;
        if (ctx.JSONB_EXISTS() != null) return BinaryExpression.Operator.JSONB_EXISTS;
        if (ctx.JSONB_EXTRACT() != null) return BinaryExpression.Operator.JSONB_EXTRACT;
        if (ctx.JSONB_EXTRACT_TEXT() != null) return BinaryExpression.Operator.JSONB_EXTRACT_TEXT;
        if (ctx.JSONB_PATH_EXTRACT() != null) return BinaryExpression.Operator.JSONB_PATH_EXTRACT;
        if (ctx.JSONB_PATH_EXTRACT_TEXT() != null) return BinaryExpression.Operator.JSONB_PATH_EXTRACT_TEXT;
        
        throw new IllegalArgumentException("Unknown binary operator: " + ctx.getText());
    }
    
    @Override
    public CreateIndexStatement visitCreateIndexStatement(MemGresParser.CreateIndexStatementContext ctx) {
        boolean unique = false;
        boolean nullsDistinct = true;  // H2 default
        boolean spatial = false;
        
        // Parse index type options
        if (ctx.UNIQUE() != null) {
            unique = true;
            if (ctx.NULLS() != null && ctx.DISTINCT() != null) {
                nullsDistinct = true; // NULLS DISTINCT (default)
            }
        } else if (ctx.SPATIAL() != null) {
            spatial = true;
        }
        
        // Parse IF NOT EXISTS
        boolean ifNotExists = ctx.IF() != null && ctx.NOT() != null && ctx.EXISTS() != null;
        
        // Parse index name (optional in H2)
        String indexName = null;
        if (ctx.indexName() != null) {
            indexName = ctx.indexName().identifier().getText();
        }
        
        // Parse table name
        String tableName = ctx.tableName().identifier().getText();
        
        // Parse index columns
        List<CreateIndexStatement.IndexColumn> indexColumns = new ArrayList<>();
        for (MemGresParser.IndexColumnContext colCtx : ctx.indexColumnList(0).indexColumn()) {
            String columnName = colCtx.columnName().identifier().getText();
            
            CreateIndexStatement.SortOrder sortOrder = CreateIndexStatement.SortOrder.ASC;
            if (colCtx.DESC() != null) {
                sortOrder = CreateIndexStatement.SortOrder.DESC;
            }
            
            CreateIndexStatement.NullsOrdering nullsOrdering = null;
            if (colCtx.NULLS() != null) {
                if (colCtx.FIRST() != null) {
                    nullsOrdering = CreateIndexStatement.NullsOrdering.FIRST;
                } else if (colCtx.LAST() != null) {
                    nullsOrdering = CreateIndexStatement.NullsOrdering.LAST;
                }
            }
            
            indexColumns.add(new CreateIndexStatement.IndexColumn(columnName, sortOrder, nullsOrdering));
        }
        
        // Parse INCLUDE columns (optional)
        List<CreateIndexStatement.IndexColumn> includeColumns = new ArrayList<>();
        if (ctx.INCLUDE() != null && ctx.indexColumnList().size() > 1) {
            MemGresParser.IndexColumnListContext includeCtx = ctx.indexColumnList(1);
            for (MemGresParser.IndexColumnContext colCtx : includeCtx.indexColumn()) {
                String columnName = colCtx.columnName().identifier().getText();
                includeColumns.add(new CreateIndexStatement.IndexColumn(columnName, 
                    CreateIndexStatement.SortOrder.ASC, null));
            }
        }
        
        return new CreateIndexStatement(unique, nullsDistinct, spatial, ifNotExists, 
                                       indexName, tableName, indexColumns, includeColumns);
    }
    
    @Override
    public DropIndexStatement visitDropIndexStatement(MemGresParser.DropIndexStatementContext ctx) {
        boolean ifExists = ctx.IF() != null && ctx.EXISTS() != null;
        String indexName = ctx.indexName().identifier().getText();
        
        return new DropIndexStatement(ifExists, indexName);
    }
    
    // CREATE SEQUENCE statement
    @Override
    public CreateSequenceStatement visitCreateSequenceStatement(MemGresParser.CreateSequenceStatementContext ctx) {
        boolean ifNotExists = ctx.IF() != null && ctx.NOT() != null && ctx.EXISTS() != null;
        String sequenceName = ctx.sequenceName().identifier().getText();
        
        // Parse optional data type
        DataTypeNode dataType = null;
        if (ctx.AS() != null && ctx.dataType() != null) {
            dataType = (DataTypeNode) visit(ctx.dataType());
        }
        
        // Parse sequence options
        List<CreateSequenceStatement.SequenceOption> options = new ArrayList<>();
        for (MemGresParser.SequenceOptionContext optionCtx : ctx.sequenceOption()) {
            CreateSequenceStatement.SequenceOption option = parseSequenceOption(optionCtx);
            if (option != null) {
                options.add(option);
            }
        }
        
        return new CreateSequenceStatement(ifNotExists, sequenceName, dataType, options);
    }
    
    private CreateSequenceStatement.SequenceOption parseSequenceOption(MemGresParser.SequenceOptionContext ctx) {
        if (ctx.START() != null && ctx.WITH() != null) {
            long startValue = parseSignedIntegerLiteral(ctx.signedIntegerLiteral());
            return new CreateSequenceStatement.StartWithOption(startValue);
        } else if (ctx.INCREMENT() != null && ctx.BY() != null) {
            long incrementValue = parseSignedIntegerLiteral(ctx.signedIntegerLiteral());
            return new CreateSequenceStatement.IncrementByOption(incrementValue);
        } else if (ctx.MINVALUE() != null) {
            long minValue = parseSignedIntegerLiteral(ctx.signedIntegerLiteral());
            return new CreateSequenceStatement.MinValueOption(minValue);
        } else if (ctx.MAXVALUE() != null) {
            long maxValue = parseSignedIntegerLiteral(ctx.signedIntegerLiteral());
            return new CreateSequenceStatement.MaxValueOption(maxValue);
        } else if (ctx.NOMINVALUE() != null) {
            return new CreateSequenceStatement.NoMinValueOption();
        } else if (ctx.NOMAXVALUE() != null) {
            return new CreateSequenceStatement.NoMaxValueOption();
        } else if (ctx.CYCLE() != null) {
            return new CreateSequenceStatement.CycleOption();
        } else if (ctx.NOCYCLE() != null) {
            return new CreateSequenceStatement.NoCycleOption();
        } else if (ctx.CACHE() != null) {
            long cacheSize = parseSignedIntegerLiteral(ctx.signedIntegerLiteral());
            return new CreateSequenceStatement.CacheOption(cacheSize);
        } else if (ctx.NOCACHE() != null) {
            return new CreateSequenceStatement.NoCacheOption();
        }
        
        return null; // Unknown option
    }
    
    private long parseSignedIntegerLiteral(MemGresParser.SignedIntegerLiteralContext ctx) {
        String integerText = ctx.INTEGER_LITERAL().getText();
        long value = Long.parseLong(integerText);
        
        // Check if there's a MINUS token before the integer
        if (ctx.MINUS() != null) {
            value = -value;
        }
        // PLUS is optional and doesn't change the value
        
        return value;
    }
    
    // DROP SEQUENCE statement  
    @Override
    public DropSequenceStatement visitDropSequenceStatement(MemGresParser.DropSequenceStatementContext ctx) {
        boolean ifExists = ctx.IF() != null && ctx.EXISTS() != null;
        String sequenceName = ctx.sequenceName().identifier().getText();
        
        return new DropSequenceStatement(ifExists, sequenceName);
    }
    
    // NEXT VALUE FOR function
    @Override
    public NextValueForExpression visitNextValueForFunction(MemGresParser.NextValueForFunctionContext ctx) {
        String sequenceName = ctx.sequenceName().identifier().getText();
        return new NextValueForExpression(sequenceName);
    }
    
    // CURRENT VALUE FOR function
    @Override
    public CurrentValueForExpression visitCurrentValueForFunction(MemGresParser.CurrentValueForFunctionContext ctx) {
        String sequenceName = ctx.sequenceName().identifier().getText();
        return new CurrentValueForExpression(sequenceName);
    }
    
    // System Functions
    @Override
    public FunctionCall visitDatabaseFunction(MemGresParser.DatabaseFunctionContext ctx) {
        return new FunctionCall("DATABASE", List.of());
    }
    
    @Override
    public FunctionCall visitUserFunction(MemGresParser.UserFunctionContext ctx) {
        return new FunctionCall("USER", List.of());
    }
    
    @Override
    public FunctionCall visitCurrentUserFunction(MemGresParser.CurrentUserFunctionContext ctx) {
        return new FunctionCall("CURRENT_USER", List.of());
    }
    
    @Override
    public FunctionCall visitSessionUserFunction(MemGresParser.SessionUserFunctionContext ctx) {
        return new FunctionCall("SESSION_USER", List.of());
    }
    
    @Override
    public FunctionCall visitSessionIdFunction(MemGresParser.SessionIdFunctionContext ctx) {
        return new FunctionCall("SESSION_ID", List.of());
    }
    
    // Math Functions
    @Override
    public FunctionCall visitSqrtFunction(MemGresParser.SqrtFunctionContext ctx) {
        Expression argument = (Expression) visit(ctx.expression());
        return new FunctionCall("SQRT", List.of(argument));
    }
    
    @Override
    public FunctionCall visitPowerFunction(MemGresParser.PowerFunctionContext ctx) {
        Expression base = (Expression) visit(ctx.expression(0));
        Expression exponent = (Expression) visit(ctx.expression(1));
        return new FunctionCall("POWER", List.of(base, exponent));
    }
    
    @Override
    public FunctionCall visitAbsFunction(MemGresParser.AbsFunctionContext ctx) {
        Expression argument = (Expression) visit(ctx.expression());
        return new FunctionCall("ABS", List.of(argument));
    }
    
    @Override
    public FunctionCall visitRoundFunction(MemGresParser.RoundFunctionContext ctx) {
        Expression value = (Expression) visit(ctx.expression(0));
        List<Expression> arguments = new ArrayList<>();
        arguments.add(value);
        
        // Optional precision argument
        if (ctx.expression().size() > 1) {
            Expression precision = (Expression) visit(ctx.expression(1));
            arguments.add(precision);
        }
        
        return new FunctionCall("ROUND", arguments);
    }
    
    @Override
    public FunctionCall visitRandFunction(MemGresParser.RandFunctionContext ctx) {
        return new FunctionCall("RAND", List.of());
    }
    
    // Window function visitors
    @Override
    public WindowFunction visitRowNumberFunction(MemGresParser.RowNumberFunctionContext ctx) {
        OverClause overClause = (OverClause) visit(ctx.overClause());
        return new WindowFunction(WindowFunction.WindowFunctionType.ROW_NUMBER, overClause);
    }
    
    @Override
    public WindowFunction visitRankFunction(MemGresParser.RankFunctionContext ctx) {
        OverClause overClause = (OverClause) visit(ctx.overClause());
        return new WindowFunction(WindowFunction.WindowFunctionType.RANK, overClause);
    }
    
    @Override
    public WindowFunction visitDenseRankFunction(MemGresParser.DenseRankFunctionContext ctx) {
        OverClause overClause = (OverClause) visit(ctx.overClause());
        return new WindowFunction(WindowFunction.WindowFunctionType.DENSE_RANK, overClause);
    }
    
    @Override
    public WindowFunction visitPercentRankFunction(MemGresParser.PercentRankFunctionContext ctx) {
        OverClause overClause = (OverClause) visit(ctx.overClause());
        return new WindowFunction(WindowFunction.WindowFunctionType.PERCENT_RANK, overClause);
    }
    
    @Override
    public WindowFunction visitCumeDistFunction(MemGresParser.CumeDistFunctionContext ctx) {
        OverClause overClause = (OverClause) visit(ctx.overClause());
        return new WindowFunction(WindowFunction.WindowFunctionType.CUME_DIST, overClause);
    }
    
    @Override
    public OverClause visitOverClause(MemGresParser.OverClauseContext ctx) {
        Optional<List<Expression>> partitionByExpressions = Optional.empty();
        if (ctx.PARTITION() != null && ctx.expressionList() != null) {
            List<Expression> expressions = new ArrayList<>();
            for (MemGresParser.ExpressionContext exprCtx : ctx.expressionList().expression()) {
                expressions.add((Expression) visit(exprCtx));
            }
            partitionByExpressions = Optional.of(expressions);
        }
        
        Optional<List<OrderByClause.OrderItem>> orderByItems = Optional.empty();
        if (ctx.ORDER() != null && ctx.orderItemList() != null) {
            List<OrderByClause.OrderItem> items = new ArrayList<>();
            for (MemGresParser.OrderItemContext itemCtx : ctx.orderItemList().orderItem()) {
                Expression expr = (Expression) visit(itemCtx.expression());
                boolean ascending = itemCtx.DESC() == null; // Default to ASC
                items.add(new OrderByClause.OrderItem(expr, ascending));
            }
            orderByItems = Optional.of(items);
        }
        
        return new OverClause(partitionByExpressions, orderByItems);
    }
    
    @Override
    public WithClause visitWithClause(MemGresParser.WithClauseContext ctx) {
        boolean recursive = ctx.RECURSIVE() != null;
        
        List<CommonTableExpression> ctes = new ArrayList<>();
        for (MemGresParser.CommonTableExpressionContext cteCtx : ctx.commonTableExpression()) {
            ctes.add((CommonTableExpression) visit(cteCtx));
        }
        
        return new WithClause(recursive, ctes);
    }
    
    @Override
    public CommonTableExpression visitCommonTableExpression(MemGresParser.CommonTableExpressionContext ctx) {
        String name = ctx.identifier().getText();
        
        Optional<List<String>> columnNames = Optional.empty();
        if (ctx.columnNameList() != null) {
            List<String> names = new ArrayList<>();
            for (MemGresParser.ColumnNameContext colCtx : ctx.columnNameList().columnName()) {
                names.add(colCtx.identifier().getText());
            }
            columnNames = Optional.of(names);
        }
        
        SelectStatement selectStatement = (SelectStatement) visit(ctx.selectStatement());
        
        return new CommonTableExpression(name, columnNames, selectStatement);
    }
}