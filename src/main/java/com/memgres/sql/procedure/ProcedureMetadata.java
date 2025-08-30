package com.memgres.sql.procedure;

import java.util.List;

/**
 * Metadata for stored procedure parameters.
 */
public class ProcedureMetadata {
    
    /**
     * Parameter direction enum (IN, OUT, INOUT).
     */
    public enum ParameterDirection {
        IN, OUT, INOUT
    }
    
    /**
     * Parameter information.
     */
    public static class Parameter {
        private final String name;
        private final ParameterDirection direction;
        private final String dataType;
        private final int position;
        
        public Parameter(String name, ParameterDirection direction, String dataType, int position) {
            this.name = name;
            this.direction = direction;
            this.dataType = dataType;
            this.position = position;
        }
        
        public String getName() {
            return name;
        }
        
        public ParameterDirection getDirection() {
            return direction;
        }
        
        public String getDataType() {
            return dataType;
        }
        
        public int getPosition() {
            return position;
        }
        
        @Override
        public String toString() {
            return String.format("%s %s %s", direction, name, dataType);
        }
    }
    
    private final String procedureName;
    private final List<Parameter> parameters;
    
    public ProcedureMetadata(String procedureName, List<Parameter> parameters) {
        this.procedureName = procedureName;
        this.parameters = List.copyOf(parameters); // Immutable copy
    }
    
    public String getProcedureName() {
        return procedureName;
    }
    
    public List<Parameter> getParameters() {
        return parameters;
    }
    
    public Parameter getParameter(String name) {
        return parameters.stream()
            .filter(p -> p.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }
    
    public Parameter getParameter(int position) {
        return parameters.stream()
            .filter(p -> p.getPosition() == position)
            .findFirst()
            .orElse(null);
    }
    
    public int getParameterCount() {
        return parameters.size();
    }
    
    @Override
    public String toString() {
        return String.format("Procedure %s(%s)", procedureName, 
            String.join(", ", parameters.stream().map(Parameter::toString).toArray(String[]::new)));
    }
}