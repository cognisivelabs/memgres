package com.memgres.core;

import com.memgres.api.Trigger;

import java.util.Objects;

/**
 * Represents a trigger definition in MemGres.
 * Contains all metadata and configuration for a database trigger.
 */
public class TriggerDefinition {
    
    /**
     * Trigger timing enumeration.
     */
    public enum Timing {
        BEFORE, AFTER, INSTEAD_OF
    }
    
    /**
     * Trigger event enumeration.
     */
    public enum Event {
        INSERT(Trigger.INSERT),
        UPDATE(Trigger.UPDATE), 
        DELETE(Trigger.DELETE),
        SELECT(Trigger.SELECT),
        ROLLBACK(Trigger.ROLLBACK);
        
        private final int value;
        
        Event(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }
    
    /**
     * Trigger scope enumeration.
     */
    public enum Scope {
        FOR_EACH_ROW, FOR_EACH_STATEMENT
    }
    
    /**
     * Trigger implementation type.
     */
    public enum ImplementationType {
        JAVA_CLASS,    // CALL "className"
        SOURCE_CODE    // AS "sourceCode"
    }
    
    private final String name;
    private final String schemaName;
    private final String tableName;
    private final Timing timing;
    private final Event event;
    private final Scope scope;
    private final ImplementationType implementationType;
    private final String implementation; // Either class name or source code
    private final boolean ifNotExists;
    private final Integer queueSize;
    private final boolean nowait;
    
    // Cached trigger instance
    private transient Trigger triggerInstance;
    
    private TriggerDefinition(Builder builder) {
        this.name = builder.name;
        this.schemaName = builder.schemaName;
        this.tableName = builder.tableName;
        this.timing = builder.timing;
        this.event = builder.event;
        this.scope = builder.scope;
        this.implementationType = builder.implementationType;
        this.implementation = builder.implementation;
        this.ifNotExists = builder.ifNotExists;
        this.queueSize = builder.queueSize;
        this.nowait = builder.nowait;
    }
    
    // Getters
    public String getName() { return name; }
    public String getSchemaName() { return schemaName; }
    public String getTableName() { return tableName; }
    public Timing getTiming() { return timing; }
    public Event getEvent() { return event; }
    public Scope getScope() { return scope; }
    public ImplementationType getImplementationType() { return implementationType; }
    public String getImplementation() { return implementation; }
    public boolean isIfNotExists() { return ifNotExists; }
    public Integer getQueueSize() { return queueSize; }
    public boolean isNowait() { return nowait; }
    
    /**
     * Get or create the trigger instance.
     */
    public synchronized Trigger getTriggerInstance() throws Exception {
        if (triggerInstance == null) {
            triggerInstance = createTriggerInstance();
        }
        return triggerInstance;
    }
    
    /**
     * Create a new trigger instance based on the implementation type.
     */
    private Trigger createTriggerInstance() throws Exception {
        switch (implementationType) {
            case JAVA_CLASS:
                return createJavaClassTrigger();
            case SOURCE_CODE:
                return createSourceCodeTrigger();
            default:
                throw new IllegalStateException("Unsupported implementation type: " + implementationType);
        }
    }
    
    /**
     * Create trigger instance from Java class name.
     */
    private Trigger createJavaClassTrigger() throws Exception {
        Class<?> triggerClass = Class.forName(implementation);
        if (!Trigger.class.isAssignableFrom(triggerClass)) {
            throw new IllegalArgumentException("Class " + implementation + " does not implement Trigger interface");
        }
        return (Trigger) triggerClass.getDeclaredConstructor().newInstance();
    }
    
    /**
     * Create trigger instance from source code.
     * TODO: Implement source code compilation in Phase 2
     */
    private Trigger createSourceCodeTrigger() throws Exception {
        throw new UnsupportedOperationException("Source code triggers not yet implemented");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TriggerDefinition that = (TriggerDefinition) obj;
        return Objects.equals(name, that.name) &&
               Objects.equals(schemaName, that.schemaName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, schemaName);
    }
    
    @Override
    public String toString() {
        return "TriggerDefinition{" +
                "name='" + name + '\'' +
                ", timing=" + timing +
                ", event=" + event +
                ", tableName='" + tableName + '\'' +
                '}';
    }
    
    /**
     * Builder for TriggerDefinition.
     */
    public static class Builder {
        private String name;
        private String schemaName = "public";
        private String tableName;
        private Timing timing;
        private Event event;
        private Scope scope = Scope.FOR_EACH_ROW;
        private ImplementationType implementationType;
        private String implementation;
        private boolean ifNotExists = false;
        private Integer queueSize;
        private boolean nowait = false;
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder schemaName(String schemaName) {
            this.schemaName = schemaName;
            return this;
        }
        
        public Builder tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }
        
        public Builder timing(Timing timing) {
            this.timing = timing;
            return this;
        }
        
        public Builder event(Event event) {
            this.event = event;
            return this;
        }
        
        public Builder scope(Scope scope) {
            this.scope = scope;
            return this;
        }
        
        public Builder javaClass(String className) {
            this.implementationType = ImplementationType.JAVA_CLASS;
            this.implementation = className;
            return this;
        }
        
        public Builder sourceCode(String sourceCode) {
            this.implementationType = ImplementationType.SOURCE_CODE;
            this.implementation = sourceCode;
            return this;
        }
        
        public Builder ifNotExists(boolean ifNotExists) {
            this.ifNotExists = ifNotExists;
            return this;
        }
        
        public Builder queueSize(Integer queueSize) {
            this.queueSize = queueSize;
            return this;
        }
        
        public Builder nowait(boolean nowait) {
            this.nowait = nowait;
            return this;
        }
        
        public TriggerDefinition build() {
            if (name == null) throw new IllegalArgumentException("Trigger name is required");
            if (tableName == null) throw new IllegalArgumentException("Table name is required");
            if (timing == null) throw new IllegalArgumentException("Trigger timing is required");
            if (event == null) throw new IllegalArgumentException("Trigger event is required");
            if (implementationType == null) throw new IllegalArgumentException("Implementation type is required");
            if (implementation == null) throw new IllegalArgumentException("Implementation is required");
            
            return new TriggerDefinition(this);
        }
    }
}