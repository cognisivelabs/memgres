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
        } else if (ctx.createTableStatement() != null) {
            return (Statement) visit(ctx.createTableStatement());
        } else if (ctx.dropTableStatement() != null) {
            return (Statement) visit(ctx.dropTableStatement());
        }
        return null;
    }
    
    @Override
    public SelectStatement visitSelectStatement(MemGresParser.SelectStatementContext ctx) {
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
        
        return new SelectStatement(distinct, selectItems, fromClause, whereClause,
                                 groupByClause, havingClause, orderByClause, limitClause);
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
    public AggregateFunction visitSumFunction(MemGresParser.SumFunctionContext ctx) {
        Expression expr = (Expression) visit(ctx.expression());
        return new AggregateFunction(AggregateFunction.AggregateType.SUM, expr);
    }
    
    @Override
    public AggregateFunction visitAvgFunction(MemGresParser.AvgFunctionContext ctx) {
        Expression expr = (Expression) visit(ctx.expression());
        return new AggregateFunction(AggregateFunction.AggregateType.AVG, expr);
    }
    
    @Override
    public AggregateFunction visitMinFunction(MemGresParser.MinFunctionContext ctx) {
        Expression expr = (Expression) visit(ctx.expression());
        return new AggregateFunction(AggregateFunction.AggregateType.MIN, expr);
    }
    
    @Override
    public AggregateFunction visitMaxFunction(MemGresParser.MaxFunctionContext ctx) {
        Expression expr = (Expression) visit(ctx.expression());
        return new AggregateFunction(AggregateFunction.AggregateType.MAX, expr);
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
    public DropTableStatement visitDropTableStatement(MemGresParser.DropTableStatementContext ctx) {
        String tableName = ctx.tableName().getText();
        return new DropTableStatement(tableName);
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