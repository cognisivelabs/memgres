package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstVisitor;

import java.util.List;

/**
 * AST node representing a CREATE SEQUENCE statement.
 * Supports H2-compatible CREATE SEQUENCE syntax with all options.
 * 
 * H2 Syntax:
 * CREATE SEQUENCE [IF NOT EXISTS] sequenceName 
 *   [AS dataType] 
 *   [START WITH value] 
 *   [INCREMENT BY value] 
 *   [MINVALUE value | NOMINVALUE] 
 *   [MAXVALUE value | NOMAXVALUE] 
 *   [CYCLE | NOCYCLE] 
 *   [CACHE value | NOCACHE]
 */
public class CreateSequenceStatement extends Statement {
    
    private final boolean ifNotExists;
    private final String sequenceName;
    private final DataTypeNode dataType;
    private final List<SequenceOption> options;
    
    public CreateSequenceStatement(boolean ifNotExists, String sequenceName, 
                                 DataTypeNode dataType, List<SequenceOption> options) {
        this.ifNotExists = ifNotExists;
        this.sequenceName = sequenceName;
        this.dataType = dataType;
        this.options = options;
    }
    
    public boolean isIfNotExists() {
        return ifNotExists;
    }
    
    public String getSequenceName() {
        return sequenceName;
    }
    
    public DataTypeNode getDataType() {
        return dataType;
    }
    
    public List<SequenceOption> getOptions() {
        return options;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitCreateSequenceStatement(this, context);
    }
    
    
    /**
     * Base class for sequence options.
     */
    public static abstract class SequenceOption {
        public abstract <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception;
    }
    
    /**
     * START WITH option.
     */
    public static class StartWithOption extends SequenceOption {
        private final long startValue;
        
        public StartWithOption(long startValue) {
            this.startValue = startValue;
        }
        
        public long getStartValue() {
            return startValue;
        }
        
        @Override
        public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
            // No specific visitor method needed for options
            return null;
        }
    }
    
    /**
     * INCREMENT BY option.
     */
    public static class IncrementByOption extends SequenceOption {
        private final long incrementValue;
        
        public IncrementByOption(long incrementValue) {
            this.incrementValue = incrementValue;
        }
        
        public long getIncrementValue() {
            return incrementValue;
        }
        
        @Override
        public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
            return null;
        }
    }
    
    /**
     * MINVALUE option.
     */
    public static class MinValueOption extends SequenceOption {
        private final long minValue;
        
        public MinValueOption(long minValue) {
            this.minValue = minValue;
        }
        
        public long getMinValue() {
            return minValue;
        }
        
        @Override
        public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
            return null;
        }
    }
    
    /**
     * MAXVALUE option.
     */
    public static class MaxValueOption extends SequenceOption {
        private final long maxValue;
        
        public MaxValueOption(long maxValue) {
            this.maxValue = maxValue;
        }
        
        public long getMaxValue() {
            return maxValue;
        }
        
        @Override
        public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
            return null;
        }
    }
    
    /**
     * NOMINVALUE option.
     */
    public static class NoMinValueOption extends SequenceOption {
        @Override
        public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
            return null;
        }
    }
    
    /**
     * NOMAXVALUE option.
     */
    public static class NoMaxValueOption extends SequenceOption {
        @Override
        public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
            return null;
        }
    }
    
    /**
     * CYCLE option.
     */
    public static class CycleOption extends SequenceOption {
        @Override
        public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
            return null;
        }
    }
    
    /**
     * NOCYCLE option.
     */
    public static class NoCycleOption extends SequenceOption {
        @Override
        public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
            return null;
        }
    }
    
    /**
     * CACHE option.
     */
    public static class CacheOption extends SequenceOption {
        private final long cacheSize;
        
        public CacheOption(long cacheSize) {
            this.cacheSize = cacheSize;
        }
        
        public long getCacheSize() {
            return cacheSize;
        }
        
        @Override
        public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
            return null;
        }
    }
    
    /**
     * NOCACHE option.
     */
    public static class NoCacheOption extends SequenceOption {
        @Override
        public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
            return null;
        }
    }
}