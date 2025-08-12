package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstVisitor;

/**
 * Represents an ADD COLUMN action in ALTER TABLE statement.
 */
public class AddColumnAction extends AlterTableAction {
    
    public enum Position {
        FIRST, BEFORE, AFTER, DEFAULT
    }
    
    private final ColumnDefinition columnDefinition;
    private final Position position;
    private final String referenceColumnName; // For BEFORE/AFTER positioning
    
    public AddColumnAction(ColumnDefinition columnDefinition) {
        this(columnDefinition, Position.DEFAULT, null);
    }
    
    public AddColumnAction(ColumnDefinition columnDefinition, Position position, String referenceColumnName) {
        this.columnDefinition = columnDefinition;
        this.position = position;
        this.referenceColumnName = referenceColumnName;
    }
    
    public ColumnDefinition getColumnDefinition() {
        return columnDefinition;
    }
    
    public Position getPosition() {
        return position;
    }
    
    public String getReferenceColumnName() {
        return referenceColumnName;
    }
    
    @Override
    public ActionType getActionType() {
        return ActionType.ADD_COLUMN;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitAddColumnAction(this, context);
    }
}