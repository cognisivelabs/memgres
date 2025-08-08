// Generated from com/memgres/sql/PostgreSQLParser.g4 by ANTLR 4.13.1
package com.memgres.sql;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link PostgreSQLParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface PostgreSQLParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#sql}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql(PostgreSQLParser.SqlContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(PostgreSQLParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#selectStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectStatement(PostgreSQLParser.SelectStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#selectModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectModifier(PostgreSQLParser.SelectModifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#selectList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectList(PostgreSQLParser.SelectListContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#selectItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectItem(PostgreSQLParser.SelectItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#fromClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromClause(PostgreSQLParser.FromClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#joinableTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinableTable(PostgreSQLParser.JoinableTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#tableReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableReference(PostgreSQLParser.TableReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#joinClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinClause(PostgreSQLParser.JoinClauseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code innerJoin}
	 * labeled alternative in {@link PostgreSQLParser#joinType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInnerJoin(PostgreSQLParser.InnerJoinContext ctx);
	/**
	 * Visit a parse tree produced by the {@code leftJoin}
	 * labeled alternative in {@link PostgreSQLParser#joinType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLeftJoin(PostgreSQLParser.LeftJoinContext ctx);
	/**
	 * Visit a parse tree produced by the {@code rightJoin}
	 * labeled alternative in {@link PostgreSQLParser#joinType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRightJoin(PostgreSQLParser.RightJoinContext ctx);
	/**
	 * Visit a parse tree produced by the {@code fullOuterJoin}
	 * labeled alternative in {@link PostgreSQLParser#joinType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFullOuterJoin(PostgreSQLParser.FullOuterJoinContext ctx);
	/**
	 * Visit a parse tree produced by the {@code crossJoin}
	 * labeled alternative in {@link PostgreSQLParser#joinType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCrossJoin(PostgreSQLParser.CrossJoinContext ctx);
	/**
	 * Visit a parse tree produced by the {@code onJoinCondition}
	 * labeled alternative in {@link PostgreSQLParser#joinCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOnJoinCondition(PostgreSQLParser.OnJoinConditionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code usingJoinCondition}
	 * labeled alternative in {@link PostgreSQLParser#joinCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUsingJoinCondition(PostgreSQLParser.UsingJoinConditionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code naturalJoinCondition}
	 * labeled alternative in {@link PostgreSQLParser#joinCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNaturalJoinCondition(PostgreSQLParser.NaturalJoinConditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#whereClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhereClause(PostgreSQLParser.WhereClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#groupByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupByClause(PostgreSQLParser.GroupByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#havingClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHavingClause(PostgreSQLParser.HavingClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#orderByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderByClause(PostgreSQLParser.OrderByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#orderItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderItem(PostgreSQLParser.OrderItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#limitClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimitClause(PostgreSQLParser.LimitClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#insertStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertStatement(PostgreSQLParser.InsertStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#valuesClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValuesClause(PostgreSQLParser.ValuesClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#updateStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdateStatement(PostgreSQLParser.UpdateStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#updateItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdateItem(PostgreSQLParser.UpdateItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#deleteStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeleteStatement(PostgreSQLParser.DeleteStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createTableStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateTableStatement(PostgreSQLParser.CreateTableStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#columnDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnDefinition(PostgreSQLParser.ColumnDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#columnConstraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnConstraint(PostgreSQLParser.ColumnConstraintContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#dropTableStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropTableStatement(PostgreSQLParser.DropTableStatementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code inSubqueryExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInSubqueryExpression(PostgreSQLParser.InSubqueryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code columnReferenceExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnReferenceExpression(PostgreSQLParser.ColumnReferenceExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code notExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotExpression(PostgreSQLParser.NotExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code subqueryExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubqueryExpression(PostgreSQLParser.SubqueryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binaryExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryExpression(PostgreSQLParser.BinaryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code betweenExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBetweenExpression(PostgreSQLParser.BetweenExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code inExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInExpression(PostgreSQLParser.InExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code parenthesizedExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenthesizedExpression(PostgreSQLParser.ParenthesizedExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code existsExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExistsExpression(PostgreSQLParser.ExistsExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code caseExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseExpression(PostgreSQLParser.CaseExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code functionCallExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCallExpression(PostgreSQLParser.FunctionCallExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code likeExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLikeExpression(PostgreSQLParser.LikeExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code literalExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteralExpression(PostgreSQLParser.LiteralExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code isNullExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIsNullExpression(PostgreSQLParser.IsNullExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#whenClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhenClause(PostgreSQLParser.WhenClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#binaryOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryOperator(PostgreSQLParser.BinaryOperatorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringLiteral}
	 * labeled alternative in {@link PostgreSQLParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringLiteral(PostgreSQLParser.StringLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code integerLiteral}
	 * labeled alternative in {@link PostgreSQLParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntegerLiteral(PostgreSQLParser.IntegerLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code decimalLiteral}
	 * labeled alternative in {@link PostgreSQLParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDecimalLiteral(PostgreSQLParser.DecimalLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code scientificLiteral}
	 * labeled alternative in {@link PostgreSQLParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScientificLiteral(PostgreSQLParser.ScientificLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code booleanLiteral}
	 * labeled alternative in {@link PostgreSQLParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanLiteral(PostgreSQLParser.BooleanLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code nullLiteral}
	 * labeled alternative in {@link PostgreSQLParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullLiteral(PostgreSQLParser.NullLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#columnReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnReference(PostgreSQLParser.ColumnReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#tableName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableName(PostgreSQLParser.TableNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#columnName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnName(PostgreSQLParser.ColumnNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlias(PostgreSQLParser.AliasContext ctx);
	/**
	 * Visit a parse tree produced by the {@code genRandomUuidFunction}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenRandomUuidFunction(PostgreSQLParser.GenRandomUuidFunctionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code uuidGenerateV1Function}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUuidGenerateV1Function(PostgreSQLParser.UuidGenerateV1FunctionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code uuidGenerateV4Function}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUuidGenerateV4Function(PostgreSQLParser.UuidGenerateV4FunctionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code countFunction}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCountFunction(PostgreSQLParser.CountFunctionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code sumFunction}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSumFunction(PostgreSQLParser.SumFunctionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code avgFunction}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAvgFunction(PostgreSQLParser.AvgFunctionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code minFunction}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMinFunction(PostgreSQLParser.MinFunctionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code maxFunction}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMaxFunction(PostgreSQLParser.MaxFunctionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code countDistinctFunction}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCountDistinctFunction(PostgreSQLParser.CountDistinctFunctionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code genericFunction}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericFunction(PostgreSQLParser.GenericFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#expressionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionList(PostgreSQLParser.ExpressionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#columnList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnList(PostgreSQLParser.ColumnListContext ctx);
	/**
	 * Visit a parse tree produced by the {@code smallintType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSmallintType(PostgreSQLParser.SmallintTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code integerType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntegerType(PostgreSQLParser.IntegerTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code bigintType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBigintType(PostgreSQLParser.BigintTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code decimalType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDecimalType(PostgreSQLParser.DecimalTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code numericType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericType(PostgreSQLParser.NumericTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code realType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRealType(PostgreSQLParser.RealTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code doublePrecisionType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDoublePrecisionType(PostgreSQLParser.DoublePrecisionTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code varcharType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarcharType(PostgreSQLParser.VarcharTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code charType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharType(PostgreSQLParser.CharTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code textType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTextType(PostgreSQLParser.TextTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code booleanType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanType(PostgreSQLParser.BooleanTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateType(PostgreSQLParser.DateTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeType(PostgreSQLParser.TimeTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timestampType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimestampType(PostgreSQLParser.TimestampTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timestamptzType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimestamptzType(PostgreSQLParser.TimestamptzTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code uuidType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUuidType(PostgreSQLParser.UuidTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbType(PostgreSQLParser.JsonbTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code byteaType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitByteaType(PostgreSQLParser.ByteaTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(PostgreSQLParser.IdentifierContext ctx);
}