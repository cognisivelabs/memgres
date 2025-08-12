package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstVisitor;

/**
 * Represents a DROP COLUMN action in ALTER TABLE statement.
 */
public class DropColumnAction extends AlterTableAction {
    
    private final String columnName;
    private final boolean ifExists;
    
    public DropColumnAction(String columnName, boolean ifExists) {
        this.columnName = columnName;
        this.ifExists = ifExists;
    }
    
    public String getColumnName() {
        return columnName;
    }
    
    public boolean isIfExists() {
        return ifExists;
    }
    
    @Override
    public ActionType getActionType() {
        return ActionType.DROP_COLUMN;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitDropColumnAction(this, context);
    }
}