// Generated from com/memgres/sql/PostgreSQLParser.g4 by ANTLR 4.13.1
package com.memgres.sql;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link PostgreSQLParser}.
 */
public interface PostgreSQLParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#sql}.
	 * @param ctx the parse tree
	 */
	void enterSql(PostgreSQLParser.SqlContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#sql}.
	 * @param ctx the parse tree
	 */
	void exitSql(PostgreSQLParser.SqlContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(PostgreSQLParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(PostgreSQLParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#selectStatement}.
	 * @param ctx the parse tree
	 */
	void enterSelectStatement(PostgreSQLParser.SelectStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#selectStatement}.
	 * @param ctx the parse tree
	 */
	void exitSelectStatement(PostgreSQLParser.SelectStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#selectModifier}.
	 * @param ctx the parse tree
	 */
	void enterSelectModifier(PostgreSQLParser.SelectModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#selectModifier}.
	 * @param ctx the parse tree
	 */
	void exitSelectModifier(PostgreSQLParser.SelectModifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#selectList}.
	 * @param ctx the parse tree
	 */
	void enterSelectList(PostgreSQLParser.SelectListContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#selectList}.
	 * @param ctx the parse tree
	 */
	void exitSelectList(PostgreSQLParser.SelectListContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#selectItem}.
	 * @param ctx the parse tree
	 */
	void enterSelectItem(PostgreSQLParser.SelectItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#selectItem}.
	 * @param ctx the parse tree
	 */
	void exitSelectItem(PostgreSQLParser.SelectItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#fromClause}.
	 * @param ctx the parse tree
	 */
	void enterFromClause(PostgreSQLParser.FromClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#fromClause}.
	 * @param ctx the parse tree
	 */
	void exitFromClause(PostgreSQLParser.FromClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#joinableTable}.
	 * @param ctx the parse tree
	 */
	void enterJoinableTable(PostgreSQLParser.JoinableTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#joinableTable}.
	 * @param ctx the parse tree
	 */
	void exitJoinableTable(PostgreSQLParser.JoinableTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#tableReference}.
	 * @param ctx the parse tree
	 */
	void enterTableReference(PostgreSQLParser.TableReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#tableReference}.
	 * @param ctx the parse tree
	 */
	void exitTableReference(PostgreSQLParser.TableReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#joinClause}.
	 * @param ctx the parse tree
	 */
	void enterJoinClause(PostgreSQLParser.JoinClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#joinClause}.
	 * @param ctx the parse tree
	 */
	void exitJoinClause(PostgreSQLParser.JoinClauseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code innerJoin}
	 * labeled alternative in {@link PostgreSQLParser#joinType}.
	 * @param ctx the parse tree
	 */
	void enterInnerJoin(PostgreSQLParser.InnerJoinContext ctx);
	/**
	 * Exit a parse tree produced by the {@code innerJoin}
	 * labeled alternative in {@link PostgreSQLParser#joinType}.
	 * @param ctx the parse tree
	 */
	void exitInnerJoin(PostgreSQLParser.InnerJoinContext ctx);
	/**
	 * Enter a parse tree produced by the {@code leftJoin}
	 * labeled alternative in {@link PostgreSQLParser#joinType}.
	 * @param ctx the parse tree
	 */
	void enterLeftJoin(PostgreSQLParser.LeftJoinContext ctx);
	/**
	 * Exit a parse tree produced by the {@code leftJoin}
	 * labeled alternative in {@link PostgreSQLParser#joinType}.
	 * @param ctx the parse tree
	 */
	void exitLeftJoin(PostgreSQLParser.LeftJoinContext ctx);
	/**
	 * Enter a parse tree produced by the {@code rightJoin}
	 * labeled alternative in {@link PostgreSQLParser#joinType}.
	 * @param ctx the parse tree
	 */
	void enterRightJoin(PostgreSQLParser.RightJoinContext ctx);
	/**
	 * Exit a parse tree produced by the {@code rightJoin}
	 * labeled alternative in {@link PostgreSQLParser#joinType}.
	 * @param ctx the parse tree
	 */
	void exitRightJoin(PostgreSQLParser.RightJoinContext ctx);
	/**
	 * Enter a parse tree produced by the {@code fullOuterJoin}
	 * labeled alternative in {@link PostgreSQLParser#joinType}.
	 * @param ctx the parse tree
	 */
	void enterFullOuterJoin(PostgreSQLParser.FullOuterJoinContext ctx);
	/**
	 * Exit a parse tree produced by the {@code fullOuterJoin}
	 * labeled alternative in {@link PostgreSQLParser#joinType}.
	 * @param ctx the parse tree
	 */
	void exitFullOuterJoin(PostgreSQLParser.FullOuterJoinContext ctx);
	/**
	 * Enter a parse tree produced by the {@code crossJoin}
	 * labeled alternative in {@link PostgreSQLParser#joinType}.
	 * @param ctx the parse tree
	 */
	void enterCrossJoin(PostgreSQLParser.CrossJoinContext ctx);
	/**
	 * Exit a parse tree produced by the {@code crossJoin}
	 * labeled alternative in {@link PostgreSQLParser#joinType}.
	 * @param ctx the parse tree
	 */
	void exitCrossJoin(PostgreSQLParser.CrossJoinContext ctx);
	/**
	 * Enter a parse tree produced by the {@code onJoinCondition}
	 * labeled alternative in {@link PostgreSQLParser#joinCondition}.
	 * @param ctx the parse tree
	 */
	void enterOnJoinCondition(PostgreSQLParser.OnJoinConditionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code onJoinCondition}
	 * labeled alternative in {@link PostgreSQLParser#joinCondition}.
	 * @param ctx the parse tree
	 */
	void exitOnJoinCondition(PostgreSQLParser.OnJoinConditionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code usingJoinCondition}
	 * labeled alternative in {@link PostgreSQLParser#joinCondition}.
	 * @param ctx the parse tree
	 */
	void enterUsingJoinCondition(PostgreSQLParser.UsingJoinConditionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code usingJoinCondition}
	 * labeled alternative in {@link PostgreSQLParser#joinCondition}.
	 * @param ctx the parse tree
	 */
	void exitUsingJoinCondition(PostgreSQLParser.UsingJoinConditionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code naturalJoinCondition}
	 * labeled alternative in {@link PostgreSQLParser#joinCondition}.
	 * @param ctx the parse tree
	 */
	void enterNaturalJoinCondition(PostgreSQLParser.NaturalJoinConditionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code naturalJoinCondition}
	 * labeled alternative in {@link PostgreSQLParser#joinCondition}.
	 * @param ctx the parse tree
	 */
	void exitNaturalJoinCondition(PostgreSQLParser.NaturalJoinConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#whereClause}.
	 * @param ctx the parse tree
	 */
	void enterWhereClause(PostgreSQLParser.WhereClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#whereClause}.
	 * @param ctx the parse tree
	 */
	void exitWhereClause(PostgreSQLParser.WhereClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#groupByClause}.
	 * @param ctx the parse tree
	 */
	void enterGroupByClause(PostgreSQLParser.GroupByClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#groupByClause}.
	 * @param ctx the parse tree
	 */
	void exitGroupByClause(PostgreSQLParser.GroupByClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#havingClause}.
	 * @param ctx the parse tree
	 */
	void enterHavingClause(PostgreSQLParser.HavingClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#havingClause}.
	 * @param ctx the parse tree
	 */
	void exitHavingClause(PostgreSQLParser.HavingClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#orderByClause}.
	 * @param ctx the parse tree
	 */
	void enterOrderByClause(PostgreSQLParser.OrderByClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#orderByClause}.
	 * @param ctx the parse tree
	 */
	void exitOrderByClause(PostgreSQLParser.OrderByClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#orderItem}.
	 * @param ctx the parse tree
	 */
	void enterOrderItem(PostgreSQLParser.OrderItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#orderItem}.
	 * @param ctx the parse tree
	 */
	void exitOrderItem(PostgreSQLParser.OrderItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#limitClause}.
	 * @param ctx the parse tree
	 */
	void enterLimitClause(PostgreSQLParser.LimitClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#limitClause}.
	 * @param ctx the parse tree
	 */
	void exitLimitClause(PostgreSQLParser.LimitClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#insertStatement}.
	 * @param ctx the parse tree
	 */
	void enterInsertStatement(PostgreSQLParser.InsertStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#insertStatement}.
	 * @param ctx the parse tree
	 */
	void exitInsertStatement(PostgreSQLParser.InsertStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#valuesClause}.
	 * @param ctx the parse tree
	 */
	void enterValuesClause(PostgreSQLParser.ValuesClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#valuesClause}.
	 * @param ctx the parse tree
	 */
	void exitValuesClause(PostgreSQLParser.ValuesClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#updateStatement}.
	 * @param ctx the parse tree
	 */
	void enterUpdateStatement(PostgreSQLParser.UpdateStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#updateStatement}.
	 * @param ctx the parse tree
	 */
	void exitUpdateStatement(PostgreSQLParser.UpdateStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#updateItem}.
	 * @param ctx the parse tree
	 */
	void enterUpdateItem(PostgreSQLParser.UpdateItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#updateItem}.
	 * @param ctx the parse tree
	 */
	void exitUpdateItem(PostgreSQLParser.UpdateItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#deleteStatement}.
	 * @param ctx the parse tree
	 */
	void enterDeleteStatement(PostgreSQLParser.DeleteStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#deleteStatement}.
	 * @param ctx the parse tree
	 */
	void exitDeleteStatement(PostgreSQLParser.DeleteStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#createTableStatement}.
	 * @param ctx the parse tree
	 */
	void enterCreateTableStatement(PostgreSQLParser.CreateTableStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#createTableStatement}.
	 * @param ctx the parse tree
	 */
	void exitCreateTableStatement(PostgreSQLParser.CreateTableStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#columnDefinition}.
	 * @param ctx the parse tree
	 */
	void enterColumnDefinition(PostgreSQLParser.ColumnDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#columnDefinition}.
	 * @param ctx the parse tree
	 */
	void exitColumnDefinition(PostgreSQLParser.ColumnDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void enterColumnConstraint(PostgreSQLParser.ColumnConstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void exitColumnConstraint(PostgreSQLParser.ColumnConstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#dropTableStatement}.
	 * @param ctx the parse tree
	 */
	void enterDropTableStatement(PostgreSQLParser.DropTableStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#dropTableStatement}.
	 * @param ctx the parse tree
	 */
	void exitDropTableStatement(PostgreSQLParser.DropTableStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code inSubqueryExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterInSubqueryExpression(PostgreSQLParser.InSubqueryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code inSubqueryExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitInSubqueryExpression(PostgreSQLParser.InSubqueryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code columnReferenceExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterColumnReferenceExpression(PostgreSQLParser.ColumnReferenceExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code columnReferenceExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitColumnReferenceExpression(PostgreSQLParser.ColumnReferenceExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code notExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterNotExpression(PostgreSQLParser.NotExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code notExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitNotExpression(PostgreSQLParser.NotExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code subqueryExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterSubqueryExpression(PostgreSQLParser.SubqueryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code subqueryExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitSubqueryExpression(PostgreSQLParser.SubqueryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binaryExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBinaryExpression(PostgreSQLParser.BinaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBinaryExpression(PostgreSQLParser.BinaryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code betweenExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBetweenExpression(PostgreSQLParser.BetweenExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code betweenExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBetweenExpression(PostgreSQLParser.BetweenExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code inExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterInExpression(PostgreSQLParser.InExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code inExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitInExpression(PostgreSQLParser.InExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code parenthesizedExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterParenthesizedExpression(PostgreSQLParser.ParenthesizedExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code parenthesizedExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitParenthesizedExpression(PostgreSQLParser.ParenthesizedExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code existsExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExistsExpression(PostgreSQLParser.ExistsExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code existsExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExistsExpression(PostgreSQLParser.ExistsExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code caseExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterCaseExpression(PostgreSQLParser.CaseExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code caseExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitCaseExpression(PostgreSQLParser.CaseExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code functionCallExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCallExpression(PostgreSQLParser.FunctionCallExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code functionCallExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCallExpression(PostgreSQLParser.FunctionCallExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code likeExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterLikeExpression(PostgreSQLParser.LikeExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code likeExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitLikeExpression(PostgreSQLParser.LikeExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code literalExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterLiteralExpression(PostgreSQLParser.LiteralExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code literalExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitLiteralExpression(PostgreSQLParser.LiteralExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code isNullExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterIsNullExpression(PostgreSQLParser.IsNullExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code isNullExpression}
	 * labeled alternative in {@link PostgreSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitIsNullExpression(PostgreSQLParser.IsNullExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#whenClause}.
	 * @param ctx the parse tree
	 */
	void enterWhenClause(PostgreSQLParser.WhenClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#whenClause}.
	 * @param ctx the parse tree
	 */
	void exitWhenClause(PostgreSQLParser.WhenClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#binaryOperator}.
	 * @param ctx the parse tree
	 */
	void enterBinaryOperator(PostgreSQLParser.BinaryOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#binaryOperator}.
	 * @param ctx the parse tree
	 */
	void exitBinaryOperator(PostgreSQLParser.BinaryOperatorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringLiteral}
	 * labeled alternative in {@link PostgreSQLParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterStringLiteral(PostgreSQLParser.StringLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringLiteral}
	 * labeled alternative in {@link PostgreSQLParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitStringLiteral(PostgreSQLParser.StringLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code integerLiteral}
	 * labeled alternative in {@link PostgreSQLParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterIntegerLiteral(PostgreSQLParser.IntegerLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code integerLiteral}
	 * labeled alternative in {@link PostgreSQLParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitIntegerLiteral(PostgreSQLParser.IntegerLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code decimalLiteral}
	 * labeled alternative in {@link PostgreSQLParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterDecimalLiteral(PostgreSQLParser.DecimalLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code decimalLiteral}
	 * labeled alternative in {@link PostgreSQLParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitDecimalLiteral(PostgreSQLParser.DecimalLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code scientificLiteral}
	 * labeled alternative in {@link PostgreSQLParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterScientificLiteral(PostgreSQLParser.ScientificLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code scientificLiteral}
	 * labeled alternative in {@link PostgreSQLParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitScientificLiteral(PostgreSQLParser.ScientificLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code booleanLiteral}
	 * labeled alternative in {@link PostgreSQLParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterBooleanLiteral(PostgreSQLParser.BooleanLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code booleanLiteral}
	 * labeled alternative in {@link PostgreSQLParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitBooleanLiteral(PostgreSQLParser.BooleanLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code nullLiteral}
	 * labeled alternative in {@link PostgreSQLParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterNullLiteral(PostgreSQLParser.NullLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code nullLiteral}
	 * labeled alternative in {@link PostgreSQLParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitNullLiteral(PostgreSQLParser.NullLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#columnReference}.
	 * @param ctx the parse tree
	 */
	void enterColumnReference(PostgreSQLParser.ColumnReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#columnReference}.
	 * @param ctx the parse tree
	 */
	void exitColumnReference(PostgreSQLParser.ColumnReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#tableName}.
	 * @param ctx the parse tree
	 */
	void enterTableName(PostgreSQLParser.TableNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#tableName}.
	 * @param ctx the parse tree
	 */
	void exitTableName(PostgreSQLParser.TableNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#columnName}.
	 * @param ctx the parse tree
	 */
	void enterColumnName(PostgreSQLParser.ColumnNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#columnName}.
	 * @param ctx the parse tree
	 */
	void exitColumnName(PostgreSQLParser.ColumnNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#alias}.
	 * @param ctx the parse tree
	 */
	void enterAlias(PostgreSQLParser.AliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#alias}.
	 * @param ctx the parse tree
	 */
	void exitAlias(PostgreSQLParser.AliasContext ctx);
	/**
	 * Enter a parse tree produced by the {@code genRandomUuidFunction}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterGenRandomUuidFunction(PostgreSQLParser.GenRandomUuidFunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code genRandomUuidFunction}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitGenRandomUuidFunction(PostgreSQLParser.GenRandomUuidFunctionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code uuidGenerateV1Function}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterUuidGenerateV1Function(PostgreSQLParser.UuidGenerateV1FunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code uuidGenerateV1Function}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitUuidGenerateV1Function(PostgreSQLParser.UuidGenerateV1FunctionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code uuidGenerateV4Function}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterUuidGenerateV4Function(PostgreSQLParser.UuidGenerateV4FunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code uuidGenerateV4Function}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitUuidGenerateV4Function(PostgreSQLParser.UuidGenerateV4FunctionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code countFunction}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterCountFunction(PostgreSQLParser.CountFunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code countFunction}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitCountFunction(PostgreSQLParser.CountFunctionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code sumFunction}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterSumFunction(PostgreSQLParser.SumFunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code sumFunction}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitSumFunction(PostgreSQLParser.SumFunctionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code avgFunction}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterAvgFunction(PostgreSQLParser.AvgFunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code avgFunction}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitAvgFunction(PostgreSQLParser.AvgFunctionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code minFunction}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterMinFunction(PostgreSQLParser.MinFunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code minFunction}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitMinFunction(PostgreSQLParser.MinFunctionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code maxFunction}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterMaxFunction(PostgreSQLParser.MaxFunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code maxFunction}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitMaxFunction(PostgreSQLParser.MaxFunctionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code countDistinctFunction}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterCountDistinctFunction(PostgreSQLParser.CountDistinctFunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code countDistinctFunction}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitCountDistinctFunction(PostgreSQLParser.CountDistinctFunctionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code genericFunction}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterGenericFunction(PostgreSQLParser.GenericFunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code genericFunction}
	 * labeled alternative in {@link PostgreSQLParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitGenericFunction(PostgreSQLParser.GenericFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#expressionList}.
	 * @param ctx the parse tree
	 */
	void enterExpressionList(PostgreSQLParser.ExpressionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#expressionList}.
	 * @param ctx the parse tree
	 */
	void exitExpressionList(PostgreSQLParser.ExpressionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#columnList}.
	 * @param ctx the parse tree
	 */
	void enterColumnList(PostgreSQLParser.ColumnListContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#columnList}.
	 * @param ctx the parse tree
	 */
	void exitColumnList(PostgreSQLParser.ColumnListContext ctx);
	/**
	 * Enter a parse tree produced by the {@code smallintType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterSmallintType(PostgreSQLParser.SmallintTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code smallintType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitSmallintType(PostgreSQLParser.SmallintTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code integerType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterIntegerType(PostgreSQLParser.IntegerTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code integerType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitIntegerType(PostgreSQLParser.IntegerTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code bigintType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterBigintType(PostgreSQLParser.BigintTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code bigintType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitBigintType(PostgreSQLParser.BigintTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code decimalType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterDecimalType(PostgreSQLParser.DecimalTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code decimalType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitDecimalType(PostgreSQLParser.DecimalTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numericType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterNumericType(PostgreSQLParser.NumericTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numericType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitNumericType(PostgreSQLParser.NumericTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code realType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterRealType(PostgreSQLParser.RealTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code realType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitRealType(PostgreSQLParser.RealTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code doublePrecisionType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterDoublePrecisionType(PostgreSQLParser.DoublePrecisionTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code doublePrecisionType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitDoublePrecisionType(PostgreSQLParser.DoublePrecisionTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code varcharType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterVarcharType(PostgreSQLParser.VarcharTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code varcharType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitVarcharType(PostgreSQLParser.VarcharTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code charType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterCharType(PostgreSQLParser.CharTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code charType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitCharType(PostgreSQLParser.CharTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code textType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterTextType(PostgreSQLParser.TextTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code textType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitTextType(PostgreSQLParser.TextTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code booleanType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterBooleanType(PostgreSQLParser.BooleanTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code booleanType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitBooleanType(PostgreSQLParser.BooleanTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterDateType(PostgreSQLParser.DateTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitDateType(PostgreSQLParser.DateTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterTimeType(PostgreSQLParser.TimeTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitTimeType(PostgreSQLParser.TimeTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timestampType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterTimestampType(PostgreSQLParser.TimestampTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timestampType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitTimestampType(PostgreSQLParser.TimestampTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timestamptzType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterTimestamptzType(PostgreSQLParser.TimestamptzTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timestamptzType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitTimestamptzType(PostgreSQLParser.TimestamptzTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code uuidType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterUuidType(PostgreSQLParser.UuidTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code uuidType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitUuidType(PostgreSQLParser.UuidTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterJsonbType(PostgreSQLParser.JsonbTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitJsonbType(PostgreSQLParser.JsonbTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code byteaType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterByteaType(PostgreSQLParser.ByteaTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code byteaType}
	 * labeled alternative in {@link PostgreSQLParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitByteaType(PostgreSQLParser.ByteaTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link PostgreSQLParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(PostgreSQLParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link PostgreSQLParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(PostgreSQLParser.IdentifierContext ctx);
}