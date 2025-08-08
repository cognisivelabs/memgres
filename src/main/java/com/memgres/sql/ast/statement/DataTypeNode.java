package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstNode;
import com.memgres.sql.ast.AstVisitor;
import com.memgres.types.DataType;

import java.util.Optional;

/**
 * Represents a data type in SQL DDL (e.g., VARCHAR(50), INTEGER, UUID).
 */
public class DataTypeNode extends AstNode {
    
    private final DataType dataType;
    private final Optional<Integer> length;
    private final Optional<Integer> precision;
    
    public DataTypeNode(DataType dataType) {
        this.dataType = dataType;
        this.length = Optional.empty();
        this.precision = Optional.empty();
    }
    
    public DataTypeNode(DataType dataType, int length) {
        this.dataType = dataType;
        this.length = Optional.of(length);
        this.precision = Optional.empty();
    }
    
    public DataTypeNode(DataType dataType, int length, int precision) {
        this.dataType = dataType;
        this.length = Optional.of(length);
        this.precision = Optional.of(precision);
    }
    
    public DataType getDataType() {
        return dataType;
    }
    
    public Optional<Integer> getLength() {
        return length;
    }
    
    public Optional<Integer> getPrecision() {
        return precision;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitDataType(this, context);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(dataType.toString());
        if (length.isPresent()) {
            sb.append("(").append(length.get());
            if (precision.isPresent()) {
                sb.append(",").append(precision.get());
            }
            sb.append(")");
        }
        return sb.toString();
    }
}