package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstNode;
import com.memgres.sql.ast.AstVisitor;
import com.memgres.sql.ast.expression.Expression;

import java.util.List;

/**
 * AST node representing a MERGE statement.
 * Supports both H2 MERGE syntax variations:
 * 1. Simple: MERGE INTO table KEY(columns) VALUES(values)
 * 2. Advanced: MERGE INTO target USING source ON condition WHEN...
 */
public class MergeStatement extends Statement {
    
    private final boolean isSimple;
    private final String tableName;
    private final String tableAlias;
    
    // Simple MERGE properties
    private final List<String> keyColumns;
    private final List<List<Expression>> valuesList;
    
    // Advanced MERGE properties
    private final MergeSource source;
    private final String sourceAlias;
    private final Expression onCondition;
    private final List<WhenClause> whenClauses;
    
    /**
     * Constructor for simple MERGE statements.
     */
    public MergeStatement(String tableName, List<String> keyColumns, List<List<Expression>> valuesList) {
        this.isSimple = true;
        this.tableName = tableName;
        this.tableAlias = null;
        this.keyColumns = keyColumns;
        this.valuesList = valuesList;
        this.source = null;
        this.sourceAlias = null;
        this.onCondition = null;
        this.whenClauses = null;
    }
    
    /**
     * Constructor for advanced MERGE statements.
     */
    public MergeStatement(String tableName, String tableAlias, MergeSource source, String sourceAlias,
                         Expression onCondition, List<WhenClause> whenClauses) {
        this.isSimple = false;
        this.tableName = tableName;
        this.tableAlias = tableAlias;
        this.keyColumns = null;
        this.valuesList = null;
        this.source = source;
        this.sourceAlias = sourceAlias;
        this.onCondition = onCondition;
        this.whenClauses = whenClauses;
    }
    
    public boolean isSimple() {
        return isSimple;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public String getTableAlias() {
        return tableAlias;
    }
    
    public List<String> getKeyColumns() {
        return keyColumns;
    }
    
    public List<List<Expression>> getValuesList() {
        return valuesList;
    }
    
    public MergeSource getSource() {
        return source;
    }
    
    public String getSourceAlias() {
        return sourceAlias;
    }
    
    public Expression getOnCondition() {
        return onCondition;
    }
    
    public List<WhenClause> getWhenClauses() {
        return whenClauses;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitMergeStatement(this, context);
    }
    
    /**
     * Represents a MERGE source (table or subquery).
     */
    public static abstract class MergeSource extends AstNode {
    }
    
    /**
     * Table source for MERGE statement.
     */
    public static class TableSource extends MergeSource {
        private final String tableName;
        
        public TableSource(String tableName) {
            this.tableName = tableName;
        }
        
        public String getTableName() {
            return tableName;
        }
        
        @Override
        public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
            // For now, we can reuse table reference handling
            return visitor.visitTableReference(new TableReference(tableName, null), context);
        }
    }
    
    /**
     * Subquery source for MERGE statement.
     */
    public static class SubquerySource extends MergeSource {
        private final SelectStatement selectStatement;
        
        public SubquerySource(SelectStatement selectStatement) {
            this.selectStatement = selectStatement;
        }
        
        public SelectStatement getSelectStatement() {
            return selectStatement;
        }
        
        @Override
        public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
            return selectStatement.accept(visitor, context);
        }
    }
    
    /**
     * Represents a WHEN clause in an advanced MERGE statement.
     */
    public static class WhenClause extends AstNode {
        private final boolean matched;
        private final Expression additionalCondition;
        private final MergeAction action;
        
        public WhenClause(boolean matched, Expression additionalCondition, MergeAction action) {
            this.matched = matched;
            this.additionalCondition = additionalCondition;
            this.action = action;
        }
        
        public boolean isMatched() {
            return matched;
        }
        
        public Expression getAdditionalCondition() {
            return additionalCondition;
        }
        
        public MergeAction getAction() {
            return action;
        }
        
        @Override
        public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
            // WHEN clauses are processed as part of MERGE statement
            if (action != null) {
                return action.accept(visitor, context);
            }
            return null;
        }
    }
    
    /**
     * Represents an action in a WHEN clause.
     */
    public static abstract class MergeAction extends AstNode {
    }
    
    /**
     * UPDATE action in WHEN MATCHED clause.
     */
    public static class UpdateAction extends MergeAction {
        private final List<UpdateItem> updateItems;
        
        public UpdateAction(List<UpdateItem> updateItems) {
            this.updateItems = updateItems;
        }
        
        public List<UpdateItem> getUpdateItems() {
            return updateItems;
        }
        
        @Override
        public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
            // Process each update item
            for (UpdateItem item : updateItems) {
                item.accept(visitor, context);
            }
            return null;
        }
    }
    
    /**
     * DELETE action in WHEN MATCHED clause.
     */
    public static class DeleteAction extends MergeAction {
        
        @Override
        public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
            // DELETE action has no additional processing
            return null;
        }
    }
    
    /**
     * INSERT action in WHEN NOT MATCHED clause.
     */
    public static class InsertAction extends MergeAction {
        private final List<String> columns;
        private final List<Expression> values;
        
        public InsertAction(List<String> columns, List<Expression> values) {
            this.columns = columns;
            this.values = values;
        }
        
        public List<String> getColumns() {
            return columns;
        }
        
        public List<Expression> getValues() {
            return values;
        }
        
        @Override
        public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
            // Process each value expression
            if (values != null) {
                for (Expression value : values) {
                    value.accept(visitor, context);
                }
            }
            return null;
        }
    }
    
    /**
     * Represents an UPDATE item (column = expression).
     */
    public static class UpdateItem extends AstNode {
        private final String columnName;
        private final Expression expression;
        
        public UpdateItem(String columnName, Expression expression) {
            this.columnName = columnName;
            this.expression = expression;
        }
        
        public String getColumnName() {
            return columnName;
        }
        
        public Expression getExpression() {
            return expression;
        }
        
        @Override
        public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
            return expression.accept(visitor, context);
        }
    }
}