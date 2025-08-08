package com.memgres.sql.parser;

import com.memgres.sql.PostgreSQLParser;
import com.memgres.sql.PostgreSQLParserBaseVisitor;
import com.memgres.sql.ast.expression.*;
import com.memgres.sql.ast.statement.*;
import com.memgres.types.DataType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Visitor that converts ANTLR4 parse trees into our SQL AST nodes.
 */
public class SqlAstBuilder extends PostgreSQLParserBaseVisitor<Object> {
    
    /**
     * Visit the top-level SQL context and return a list of statements.
     */
    public List<Statement> visit(PostgreSQLParser.SqlContext ctx) {
        List<Statement> statements = new ArrayList<>();
        for (PostgreSQLParser.StatementContext stmtCtx : ctx.statement()) {
            Statement stmt = (Statement) visit(stmtCtx);
            if (stmt != null) {
                statements.add(stmt);
            }
        }
        return statements;
    }
    
    @Override
    public Statement visitStatement(PostgreSQLParser.StatementContext ctx) {
        if (ctx.selectStatement() != null) {
            return (Statement) visit(ctx.selectStatement());
        } else if (ctx.insertStatement() != null) {
            return (Statement) visit(ctx.insertStatement());
        } else if (ctx.updateStatement() != null) {
            return (Statement) visit(ctx.updateStatement());
        } else if (ctx.deleteStatement() != null) {
            return (Statement) visit(ctx.deleteStatement());
        } else if (ctx.createTableStatement() != null) {
            return (Statement) visit(ctx.createTableStatement());
        } else if (ctx.dropTableStatement() != null) {
            return (Statement) visit(ctx.dropTableStatement());
        }
        return null;
    }
    
    @Override
    public SelectStatement visitSelectStatement(PostgreSQLParser.SelectStatementContext ctx) {
        // Parse DISTINCT
        boolean distinct = ctx.selectModifier() != null && 
                          ctx.selectModifier().DISTINCT() != null;
        
        // Parse SELECT list
        List<SelectItem> selectItems = new ArrayList<>();
        if (ctx.selectList().MULTIPLY() != null) {
            selectItems.add(new SelectItem()); // Wildcard
        } else {
            for (PostgreSQLParser.SelectItemContext itemCtx : ctx.selectList().selectItem()) {
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
        
        return new SelectStatement(distinct, selectItems, fromClause, whereClause,
                                 groupByClause, havingClause, orderByClause, limitClause);
    }
    
    @Override
    public SelectItem visitSelectItem(PostgreSQLParser.SelectItemContext ctx) {
        Expression expression = (Expression) visit(ctx.expression());
        Optional<String> alias = ctx.alias() != null ? 
            Optional.of(ctx.alias().getText()) : Optional.empty();
        return new SelectItem(expression, alias);
    }
    
    @Override
    public FromClause visitFromClause(PostgreSQLParser.FromClauseContext ctx) {
        List<JoinableTable> joinableTables = new ArrayList<>();
        for (PostgreSQLParser.JoinableTableContext joinableCtx : ctx.joinableTable()) {
            joinableTables.add((JoinableTable) visit(joinableCtx));
        }
        return new FromClause(joinableTables);
    }
    
    @Override
    public JoinableTable visitJoinableTable(PostgreSQLParser.JoinableTableContext ctx) {
        // Get the base table reference
        TableReference baseTable = (TableReference) visit(ctx.tableReference());
        
        // Get all join clauses
        List<JoinClause> joins = new ArrayList<>();
        for (PostgreSQLParser.JoinClauseContext joinCtx : ctx.joinClause()) {
            joins.add((JoinClause) visit(joinCtx));
        }
        
        return new JoinableTable(baseTable, joins);
    }
    
    @Override
    public JoinClause visitJoinClause(PostgreSQLParser.JoinClauseContext ctx) {
        // Get join type
        JoinClause.JoinType joinType = getJoinType(ctx.joinType());
        
        // Get the table being joined
        TableReference table = (TableReference) visit(ctx.tableReference());
        
        // Get join condition
        Optional<Expression> onCondition = Optional.empty();
        if (ctx.joinCondition() != null) {
            PostgreSQLParser.JoinConditionContext condCtx = ctx.joinCondition();
            // Check if it's an ON condition (onJoinCondition alternative)
            if (condCtx instanceof PostgreSQLParser.OnJoinConditionContext) {
                PostgreSQLParser.OnJoinConditionContext onCtx = (PostgreSQLParser.OnJoinConditionContext) condCtx;
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
    private JoinClause.JoinType getJoinType(PostgreSQLParser.JoinTypeContext ctx) {
        if (ctx == null) {
            return JoinClause.JoinType.INNER; // Default to INNER JOIN
        }
        
        if (ctx instanceof PostgreSQLParser.InnerJoinContext) {
            return JoinClause.JoinType.INNER;
        } else if (ctx instanceof PostgreSQLParser.LeftJoinContext) {
            return JoinClause.JoinType.LEFT;
        } else if (ctx instanceof PostgreSQLParser.RightJoinContext) {
            return JoinClause.JoinType.RIGHT;
        } else if (ctx instanceof PostgreSQLParser.FullOuterJoinContext) {
            return JoinClause.JoinType.FULL_OUTER;
        } else {
            return JoinClause.JoinType.INNER; // Default fallback
        }
    }
    
    @Override
    public TableReference visitTableReference(PostgreSQLParser.TableReferenceContext ctx) {
        String tableName = ctx.tableName().getText();
        Optional<String> alias = ctx.alias() != null ? 
            Optional.of(ctx.alias().getText()) : Optional.empty();
        return new TableReference(tableName, alias);
    }
    
    @Override
    public WhereClause visitWhereClause(PostgreSQLParser.WhereClauseContext ctx) {
        Expression condition = (Expression) visit(ctx.expression());
        return new WhereClause(condition);
    }
    
    @Override
    public OrderByClause visitOrderByClause(PostgreSQLParser.OrderByClauseContext ctx) {
        List<OrderByClause.OrderItem> items = new ArrayList<>();
        for (PostgreSQLParser.OrderItemContext itemCtx : ctx.orderItem()) {
            Expression expr = (Expression) visit(itemCtx.expression());
            boolean ascending = itemCtx.DESC() == null; // Default to ASC
            items.add(new OrderByClause.OrderItem(expr, ascending));
        }
        return new OrderByClause(items);
    }
    
    @Override
    public GroupByClause visitGroupByClause(PostgreSQLParser.GroupByClauseContext ctx) {
        List<Expression> expressions = new ArrayList<>();
        for (PostgreSQLParser.ExpressionContext exprCtx : ctx.expression()) {
            expressions.add((Expression) visit(exprCtx));
        }
        return new GroupByClause(expressions);
    }
    
    @Override
    public HavingClause visitHavingClause(PostgreSQLParser.HavingClauseContext ctx) {
        Expression condition = (Expression) visit(ctx.expression());
        return new HavingClause(condition);
    }
    
    @Override
    public LimitClause visitLimitClause(PostgreSQLParser.LimitClauseContext ctx) {
        Expression limit = (Expression) visit(ctx.expression(0));
        Optional<Expression> offset = ctx.expression().size() > 1 ?
            Optional.of((Expression) visit(ctx.expression(1))) : Optional.empty();
        return new LimitClause(limit, offset);
    }
    
    // Expression visitors
    @Override
    public Expression visitLiteralExpression(PostgreSQLParser.LiteralExpressionContext ctx) {
        return (Expression) visit(ctx.literal());
    }
    
    @Override
    public LiteralExpression visitStringLiteral(PostgreSQLParser.StringLiteralContext ctx) {
        String text = ctx.STRING().getText();
        // Remove quotes
        String value = text.substring(1, text.length() - 1);
        return new LiteralExpression(value, LiteralExpression.LiteralType.STRING);
    }
    
    @Override
    public LiteralExpression visitIntegerLiteral(PostgreSQLParser.IntegerLiteralContext ctx) {
        Long value = Long.parseLong(ctx.INTEGER_LITERAL().getText());
        return new LiteralExpression(value, LiteralExpression.LiteralType.INTEGER);
    }
    
    @Override
    public LiteralExpression visitDecimalLiteral(PostgreSQLParser.DecimalLiteralContext ctx) {
        Double value = Double.parseDouble(ctx.DECIMAL_LITERAL().getText());
        return new LiteralExpression(value, LiteralExpression.LiteralType.DECIMAL);
    }
    
    @Override
    public LiteralExpression visitBooleanLiteral(PostgreSQLParser.BooleanLiteralContext ctx) {
        Boolean value = ctx.TRUE() != null;
        return new LiteralExpression(value, LiteralExpression.LiteralType.BOOLEAN);
    }
    
    @Override
    public LiteralExpression visitNullLiteral(PostgreSQLParser.NullLiteralContext ctx) {
        return new LiteralExpression(null, LiteralExpression.LiteralType.NULL);
    }
    
    @Override
    public Expression visitColumnReferenceExpression(PostgreSQLParser.ColumnReferenceExpressionContext ctx) {
        return (Expression) visit(ctx.columnReference());
    }
    
    @Override
    public ColumnReference visitColumnReference(PostgreSQLParser.ColumnReferenceContext ctx) {
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
    public Expression visitBinaryExpression(PostgreSQLParser.BinaryExpressionContext ctx) {
        Expression left = (Expression) visit(ctx.expression(0));
        Expression right = (Expression) visit(ctx.expression(1));
        BinaryExpression.Operator operator = getBinaryOperator(ctx.binaryOperator());
        return new BinaryExpression(left, operator, right);
    }
    
    @Override
    public Expression visitFunctionCallExpression(PostgreSQLParser.FunctionCallExpressionContext ctx) {
        return (Expression) visit(ctx.functionCall());
    }
    
    @Override
    public FunctionCall visitGenRandomUuidFunction(PostgreSQLParser.GenRandomUuidFunctionContext ctx) {
        return new FunctionCall("gen_random_uuid", List.of());
    }
    
    @Override
    public FunctionCall visitUuidGenerateV1Function(PostgreSQLParser.UuidGenerateV1FunctionContext ctx) {
        return new FunctionCall("uuid_generate_v1", List.of());
    }
    
    @Override
    public FunctionCall visitUuidGenerateV4Function(PostgreSQLParser.UuidGenerateV4FunctionContext ctx) {
        return new FunctionCall("uuid_generate_v4", List.of());
    }
    
    @Override
    public FunctionCall visitGenericFunction(PostgreSQLParser.GenericFunctionContext ctx) {
        String functionName = ctx.identifier().getText();
        List<Expression> arguments = new ArrayList<>();
        if (ctx.expressionList() != null) {
            for (PostgreSQLParser.ExpressionContext exprCtx : ctx.expressionList().expression()) {
                arguments.add((Expression) visit(exprCtx));
            }
        }
        return new FunctionCall(functionName, arguments);
    }
    
    @Override
    public SubqueryExpression visitSubqueryExpression(PostgreSQLParser.SubqueryExpressionContext ctx) {
        SelectStatement selectStatement = (SelectStatement) visit(ctx.selectStatement());
        return new SubqueryExpression(selectStatement);
    }
    
    @Override
    public ExistsExpression visitExistsExpression(PostgreSQLParser.ExistsExpressionContext ctx) {
        SelectStatement subquery = (SelectStatement) visit(ctx.selectStatement());
        return new ExistsExpression(subquery);
    }
    
    @Override
    public InSubqueryExpression visitInSubqueryExpression(PostgreSQLParser.InSubqueryExpressionContext ctx) {
        Expression expression = (Expression) visit(ctx.expression());
        SelectStatement subquery = (SelectStatement) visit(ctx.selectStatement());
        boolean negated = ctx.NOT() != null;
        return new InSubqueryExpression(expression, subquery, negated);
    }
    
    @Override
    public AggregateFunction visitCountFunction(PostgreSQLParser.CountFunctionContext ctx) {
        if (ctx.MULTIPLY() != null) {
            // COUNT(*)
            return new AggregateFunction(AggregateFunction.AggregateType.COUNT, null);
        } else {
            // COUNT(expression)
            Expression expr = (Expression) visit(ctx.expression());
            return new AggregateFunction(AggregateFunction.AggregateType.COUNT, expr);
        }
    }
    
    @Override
    public AggregateFunction visitSumFunction(PostgreSQLParser.SumFunctionContext ctx) {
        Expression expr = (Expression) visit(ctx.expression());
        return new AggregateFunction(AggregateFunction.AggregateType.SUM, expr);
    }
    
    @Override
    public AggregateFunction visitAvgFunction(PostgreSQLParser.AvgFunctionContext ctx) {
        Expression expr = (Expression) visit(ctx.expression());
        return new AggregateFunction(AggregateFunction.AggregateType.AVG, expr);
    }
    
    @Override
    public AggregateFunction visitMinFunction(PostgreSQLParser.MinFunctionContext ctx) {
        Expression expr = (Expression) visit(ctx.expression());
        return new AggregateFunction(AggregateFunction.AggregateType.MIN, expr);
    }
    
    @Override
    public AggregateFunction visitMaxFunction(PostgreSQLParser.MaxFunctionContext ctx) {
        Expression expr = (Expression) visit(ctx.expression());
        return new AggregateFunction(AggregateFunction.AggregateType.MAX, expr);
    }
    
    @Override
    public AggregateFunction visitCountDistinctFunction(PostgreSQLParser.CountDistinctFunctionContext ctx) {
        Expression expr = (Expression) visit(ctx.expression());
        return new AggregateFunction(AggregateFunction.AggregateType.COUNT_DISTINCT, expr);
    }
    
    // INSERT statement
    @Override
    public InsertStatement visitInsertStatement(PostgreSQLParser.InsertStatementContext ctx) {
        String tableName = ctx.tableName().getText();
        
        Optional<List<String>> columns = Optional.empty();
        if (ctx.columnList() != null) {
            List<String> columnNames = ctx.columnList().columnName().stream()
                .map(c -> c.getText())
                .collect(Collectors.toList());
            columns = Optional.of(columnNames);
        }
        
        List<List<Expression>> valuesList = new ArrayList<>();
        for (PostgreSQLParser.ValuesClauseContext valuesCtx : ctx.valuesClause()) {
            List<Expression> values = new ArrayList<>();
            for (PostgreSQLParser.ExpressionContext exprCtx : valuesCtx.expression()) {
                values.add((Expression) visit(exprCtx));
            }
            valuesList.add(values);
        }
        
        return new InsertStatement(tableName, columns, valuesList);
    }
    
    // UPDATE statement
    @Override
    public UpdateStatement visitUpdateStatement(PostgreSQLParser.UpdateStatementContext ctx) {
        String tableName = ctx.tableName().getText();
        
        List<UpdateStatement.UpdateItem> updateItems = new ArrayList<>();
        for (PostgreSQLParser.UpdateItemContext updateCtx : ctx.updateItem()) {
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
    public DeleteStatement visitDeleteStatement(PostgreSQLParser.DeleteStatementContext ctx) {
        String tableName = ctx.tableName().getText();
        
        Optional<WhereClause> whereClause = ctx.whereClause() != null ? 
            Optional.of((WhereClause) visit(ctx.whereClause())) : Optional.empty();
        
        return new DeleteStatement(tableName, whereClause);
    }
    
    // CREATE TABLE statement
    @Override
    public CreateTableStatement visitCreateTableStatement(PostgreSQLParser.CreateTableStatementContext ctx) {
        String tableName = ctx.tableName().getText();
        
        List<ColumnDefinition> columnDefinitions = new ArrayList<>();
        for (PostgreSQLParser.ColumnDefinitionContext colCtx : ctx.columnDefinition()) {
            String columnName = colCtx.columnName().getText();
            DataTypeNode dataType = (DataTypeNode) visit(colCtx.dataType());
            
            List<ColumnDefinition.Constraint> constraints = new ArrayList<>();
            if (colCtx.columnConstraint() != null) {
                for (PostgreSQLParser.ColumnConstraintContext constraintCtx : colCtx.columnConstraint()) {
                    if (constraintCtx.NOT() != null && constraintCtx.NULL() != null) {
                        constraints.add(ColumnDefinition.Constraint.NOT_NULL);
                    }
                }
            }
            
            columnDefinitions.add(new ColumnDefinition(columnName, dataType, constraints));
        }
        
        return new CreateTableStatement(tableName, columnDefinitions);
    }
    
    // DROP TABLE statement
    @Override
    public DropTableStatement visitDropTableStatement(PostgreSQLParser.DropTableStatementContext ctx) {
        String tableName = ctx.tableName().getText();
        return new DropTableStatement(tableName);
    }
    
    // Data type visitors
    @Override
    public DataTypeNode visitIntegerType(PostgreSQLParser.IntegerTypeContext ctx) {
        return new DataTypeNode(DataType.INTEGER);
    }
    
    @Override
    public DataTypeNode visitBigintType(PostgreSQLParser.BigintTypeContext ctx) {
        return new DataTypeNode(DataType.BIGINT);
    }
    
    @Override
    public DataTypeNode visitSmallintType(PostgreSQLParser.SmallintTypeContext ctx) {
        return new DataTypeNode(DataType.SMALLINT);
    }
    
    @Override
    public DataTypeNode visitVarcharType(PostgreSQLParser.VarcharTypeContext ctx) {
        return new DataTypeNode(DataType.VARCHAR);
    }
    
    @Override
    public DataTypeNode visitTextType(PostgreSQLParser.TextTypeContext ctx) {
        return new DataTypeNode(DataType.TEXT);
    }
    
    @Override
    public DataTypeNode visitCharType(PostgreSQLParser.CharTypeContext ctx) {
        return new DataTypeNode(DataType.CHAR);
    }
    
    @Override
    public DataTypeNode visitBooleanType(PostgreSQLParser.BooleanTypeContext ctx) {
        return new DataTypeNode(DataType.BOOLEAN);
    }
    
    @Override
    public DataTypeNode visitUuidType(PostgreSQLParser.UuidTypeContext ctx) {
        return new DataTypeNode(DataType.UUID);
    }
    
    @Override
    public DataTypeNode visitJsonbType(PostgreSQLParser.JsonbTypeContext ctx) {
        return new DataTypeNode(DataType.JSONB);
    }
    
    @Override
    public DataTypeNode visitRealType(PostgreSQLParser.RealTypeContext ctx) {
        return new DataTypeNode(DataType.REAL);
    }
    
    @Override
    public DataTypeNode visitDoublePrecisionType(PostgreSQLParser.DoublePrecisionTypeContext ctx) {
        return new DataTypeNode(DataType.DOUBLE_PRECISION);
    }
    
    @Override
    public DataTypeNode visitDecimalType(PostgreSQLParser.DecimalTypeContext ctx) {
        return new DataTypeNode(DataType.DECIMAL);
    }
    
    @Override
    public DataTypeNode visitNumericType(PostgreSQLParser.NumericTypeContext ctx) {
        return new DataTypeNode(DataType.DECIMAL);
    }
    
    @Override
    public DataTypeNode visitDateType(PostgreSQLParser.DateTypeContext ctx) {
        return new DataTypeNode(DataType.DATE);
    }
    
    @Override
    public DataTypeNode visitTimeType(PostgreSQLParser.TimeTypeContext ctx) {
        return new DataTypeNode(DataType.TIME);
    }
    
    @Override
    public DataTypeNode visitTimestampType(PostgreSQLParser.TimestampTypeContext ctx) {
        return new DataTypeNode(DataType.TIMESTAMP);
    }
    
    @Override
    public DataTypeNode visitTimestamptzType(PostgreSQLParser.TimestamptzTypeContext ctx) {
        return new DataTypeNode(DataType.TIMESTAMPTZ);
    }
    
    @Override
    public DataTypeNode visitByteaType(PostgreSQLParser.ByteaTypeContext ctx) {
        return new DataTypeNode(DataType.BYTEA);
    }
    
    /**
     * Convert ANTLR4 binary operator context to our BinaryExpression.Operator enum.
     */
    private BinaryExpression.Operator getBinaryOperator(PostgreSQLParser.BinaryOperatorContext ctx) {
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
        if (ctx.POWER() != null) return BinaryExpression.Operator.POWER;
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
}