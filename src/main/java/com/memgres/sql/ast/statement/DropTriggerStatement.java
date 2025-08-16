package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstVisitor;

import java.util.Objects;

/**
 * AST node representing a DROP TRIGGER statement.
 */
public class DropTriggerStatement extends Statement {
    
    private final String triggerName;
    private final boolean ifExists;
    
    public DropTriggerStatement(String triggerName, boolean ifExists) {
        this.triggerName = triggerName;
        this.ifExists = ifExists;
    }
    
    public String getTriggerName() {
        return triggerName;
    }
    
    public boolean isIfExists() {
        return ifExists;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitDropTriggerStatement(this, context);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DropTriggerStatement that = (DropTriggerStatement) obj;
        return ifExists == that.ifExists &&
               Objects.equals(triggerName, that.triggerName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(triggerName, ifExists);
    }
    
    @Override
    public String toString() {
        return "DropTriggerStatement{" +
                "triggerName='" + triggerName + '\'' +
                ", ifExists=" + ifExists +
                '}';
    }
}