package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstVisitor;

import java.util.List;
import java.util.Objects;

/**
 * AST node representing a CREATE TRIGGER statement.
 */
public class CreateTriggerStatement extends Statement {
    
    public enum Timing {
        BEFORE, AFTER, INSTEAD_OF
    }
    
    public enum Event {
        INSERT, UPDATE, DELETE, SELECT
    }
    
    public enum Scope {
        FOR_EACH_ROW, FOR_EACH_STATEMENT
    }
    
    public static class Implementation {
        public enum Type {
            CALL, AS
        }
        
        private final Type type;
        private final String value;
        
        public Implementation(Type type, String value) {
            this.type = type;
            this.value = value;
        }
        
        public Type getType() { return type; }
        public String getValue() { return value; }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Implementation that = (Implementation) obj;
            return type == that.type && Objects.equals(value, that.value);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(type, value);
        }
        
        @Override
        public String toString() {
            return type + " " + value;
        }
    }
    
    private final String triggerName;
    private final boolean ifNotExists;
    private final Timing timing;
    private final List<Event> events;
    private final String tableName;
    private final Scope scope;
    private final Integer queueSize;
    private final boolean nowait;
    private final Implementation implementation;
    
    public CreateTriggerStatement(String triggerName, boolean ifNotExists, Timing timing,
                                 List<Event> events, String tableName, Scope scope,
                                 Integer queueSize, boolean nowait, Implementation implementation) {
        this.triggerName = triggerName;
        this.ifNotExists = ifNotExists;
        this.timing = timing;
        this.events = events;
        this.tableName = tableName;
        this.scope = scope;
        this.queueSize = queueSize;
        this.nowait = nowait;
        this.implementation = implementation;
    }
    
    public String getTriggerName() { return triggerName; }
    public boolean isIfNotExists() { return ifNotExists; }
    public Timing getTiming() { return timing; }
    public List<Event> getEvents() { return events; }
    public String getTableName() { return tableName; }
    public Scope getScope() { return scope; }
    public Integer getQueueSize() { return queueSize; }
    public boolean isNowait() { return nowait; }
    public Implementation getImplementation() { return implementation; }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitCreateTriggerStatement(this, context);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CreateTriggerStatement that = (CreateTriggerStatement) obj;
        return ifNotExists == that.ifNotExists &&
               nowait == that.nowait &&
               Objects.equals(triggerName, that.triggerName) &&
               timing == that.timing &&
               Objects.equals(events, that.events) &&
               Objects.equals(tableName, that.tableName) &&
               scope == that.scope &&
               Objects.equals(queueSize, that.queueSize) &&
               Objects.equals(implementation, that.implementation);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(triggerName, ifNotExists, timing, events, tableName, 
                          scope, queueSize, nowait, implementation);
    }
    
    @Override
    public String toString() {
        return "CreateTriggerStatement{" +
                "triggerName='" + triggerName + '\'' +
                ", timing=" + timing +
                ", events=" + events +
                ", tableName='" + tableName + '\'' +
                ", implementation=" + implementation +
                '}';
    }
}