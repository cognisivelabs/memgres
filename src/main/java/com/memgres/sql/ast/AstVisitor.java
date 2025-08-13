package com.memgres.sql.ast;

import com.memgres.sql.ast.expression.*;
import com.memgres.sql.ast.statement.*;

/**
 * Visitor interface for traversing the SQL Abstract Syntax Tree.
 * 
 * @param <T> the return type of visit methods
 * @param <C> the context type passed to visit methods
 */
public interface AstVisitor<T, C> {
    
    // Statement visitors
    T visitSelectStatement(SelectStatement node, C context) throws Exception;
    T visitInsertStatement(InsertStatement node, C context) throws Exception;
    T visitUpdateStatement(UpdateStatement node, C context) throws Exception;
    T visitDeleteStatement(DeleteStatement node, C context) throws Exception;
    T visitMergeStatement(MergeStatement node, C context) throws Exception;
    T visitCreateTableStatement(CreateTableStatement node, C context) throws Exception;
    T visitAlterTableStatement(AlterTableStatement node, C context) throws Exception;
    T visitDropTableStatement(DropTableStatement node, C context) throws Exception;
    T visitCreateViewStatement(CreateViewStatement node, C context) throws Exception;
    T visitDropViewStatement(DropViewStatement node, C context) throws Exception;
    T visitTruncateTableStatement(TruncateTableStatement node, C context) throws Exception;
    T visitCreateIndexStatement(CreateIndexStatement node, C context) throws Exception;
    T visitDropIndexStatement(DropIndexStatement node, C context) throws Exception;
    T visitCreateSequenceStatement(CreateSequenceStatement node, C context) throws Exception;
    T visitDropSequenceStatement(DropSequenceStatement node, C context) throws Exception;
    
    // Expression visitors
    T visitLiteralExpression(LiteralExpression node, C context) throws Exception;
    T visitColumnReference(ColumnReference node, C context) throws Exception;
    T visitBinaryExpression(BinaryExpression node, C context) throws Exception;
    T visitUnaryExpression(UnaryExpression node, C context) throws Exception;
    T visitFunctionCall(FunctionCall node, C context) throws Exception;
    T visitCaseExpression(CaseExpression node, C context) throws Exception;
    T visitSubqueryExpression(SubqueryExpression node, C context) throws Exception;
    T visitExistsExpression(ExistsExpression node, C context) throws Exception;
    T visitInSubqueryExpression(InSubqueryExpression node, C context) throws Exception;
    T visitAggregateFunction(AggregateFunction node, C context) throws Exception;
    T visitNextValueForExpression(NextValueForExpression node, C context) throws Exception;
    T visitCurrentValueForExpression(CurrentValueForExpression node, C context) throws Exception;
    
    // Clause visitors
    T visitFromClause(FromClause node, C context) throws Exception;
    T visitWhereClause(WhereClause node, C context) throws Exception;
    T visitOrderByClause(OrderByClause node, C context) throws Exception;
    T visitGroupByClause(GroupByClause node, C context) throws Exception;
    T visitHavingClause(HavingClause node, C context) throws Exception;
    T visitLimitClause(LimitClause node, C context) throws Exception;
    
    // Other node visitors
    T visitTableReference(TableReference node, C context) throws Exception;
    T visitJoinClause(JoinClause node, C context) throws Exception;
    T visitJoinableTable(JoinableTable node, C context) throws Exception;
    T visitSelectItem(SelectItem node, C context) throws Exception;
    T visitDataType(DataTypeNode node, C context) throws Exception;
    T visitColumnDefinition(ColumnDefinition node, C context) throws Exception;

    // ALTER TABLE action visitors
    T visitAddColumnAction(AddColumnAction node, C context) throws Exception;
    T visitDropColumnAction(DropColumnAction node, C context) throws Exception;
    T visitRenameColumnAction(RenameColumnAction node, C context) throws Exception;
    T visitRenameTableAction(RenameTableAction node, C context) throws Exception;
}