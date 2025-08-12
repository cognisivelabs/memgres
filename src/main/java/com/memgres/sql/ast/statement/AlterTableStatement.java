package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstVisitor;

/**
 * Represents an ALTER TABLE statement in SQL.
 */
public class AlterTableStatement extends Statement {
    
    private final String tableName;
    private final boolean ifExists;
    private final AlterTableAction action;
    
    public AlterTableStatement(String tableName, boolean ifExists, AlterTableAction action) {
        this.tableName = tableName;
        this.ifExists = ifExists;
        this.action = action;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public boolean isIfExists() {
        return ifExists;
    }
    
    public AlterTableAction getAction() {
        return action;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitAlterTableStatement(this, context);
    }
}