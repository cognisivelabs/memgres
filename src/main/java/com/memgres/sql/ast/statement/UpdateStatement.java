package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstVisitor;
import com.memgres.sql.ast.expression.Expression;

import java.util.List;
import java.util.Optional;

/**
 * Represents an UPDATE statement in SQL.
 */
public class UpdateStatement extends Statement {
    
    public static class UpdateItem {
        private final String columnName;
        private final Expression value;
        
        public UpdateItem(String columnName, Expression value) {
            this.columnName = columnName;
            this.value = value;
        }
        
        public String getColumnName() {
            return columnName;
        }
        
        public Expression getValue() {
            return value;
        }
    }
    
    private final String tableName;
    private final List<UpdateItem> updateItems;
    private final Optional<WhereClause> whereClause;
    
    public UpdateStatement(String tableName, List<UpdateItem> updateItems, Optional<WhereClause> whereClause) {
        this.tableName = tableName;
        this.updateItems = updateItems;
        this.whereClause = whereClause;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public List<UpdateItem> getUpdateItems() {
        return updateItems;
    }
    
    public Optional<WhereClause> getWhereClause() {
        return whereClause;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitUpdateStatement(this, context);
    }
}