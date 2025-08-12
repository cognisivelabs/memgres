package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstVisitor;

/**
 * Represents a RENAME COLUMN action in ALTER TABLE statement.
 */
public class RenameColumnAction extends AlterTableAction {
    
    private final String oldColumnName;
    private final String newColumnName;
    
    public RenameColumnAction(String oldColumnName, String newColumnName) {
        this.oldColumnName = oldColumnName;
        this.newColumnName = newColumnName;
    }
    
    public String getOldColumnName() {
        return oldColumnName;
    }
    
    public String getNewColumnName() {
        return newColumnName;
    }
    
    @Override
    public ActionType getActionType() {
        return ActionType.RENAME_COLUMN;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitRenameColumnAction(this, context);
    }
}