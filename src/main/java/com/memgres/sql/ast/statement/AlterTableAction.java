package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstNode;

/**
 * Base class for all ALTER TABLE actions.
 */
public abstract class AlterTableAction extends AstNode {
    
    public enum ActionType {
        ADD_COLUMN,
        DROP_COLUMN, 
        RENAME_COLUMN,
        RENAME_TABLE
    }
    
    public abstract ActionType getActionType();
}