package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstVisitor;

/**
 * Represents a RENAME TO action in ALTER TABLE statement.
 */
public class RenameTableAction extends AlterTableAction {
    
    private final String newTableName;
    
    public RenameTableAction(String newTableName) {
        this.newTableName = newTableName;
    }
    
    public String getNewTableName() {
        return newTableName;
    }
    
    @Override
    public ActionType getActionType() {
        return ActionType.RENAME_TABLE;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitRenameTableAction(this, context);
    }
}