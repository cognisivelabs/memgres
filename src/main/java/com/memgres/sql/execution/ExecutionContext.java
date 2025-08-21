package com.memgres.sql.execution;

import com.memgres.storage.Table;
import com.memgres.types.Column;
import com.memgres.types.Row;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Execution context that maintains state during SQL statement execution.
 */
public class ExecutionContext {
    
    private Row currentRow;
    private Table currentTable;
    private String currentTableName;
    private List<Column> joinedColumns;
    private Map<String, List<Column>> tableColumns;
    private List<String> tableOrder;
    private Connection connection;
    
    // CTE support - store temporary result sets
    private Map<String, CTEResult> cteResults = new HashMap<>();
    
    public Row getCurrentRow() {
        return currentRow;
    }
    
    public void setCurrentRow(Row currentRow) {
        this.currentRow = currentRow;
    }
    
    public Table getCurrentTable() {
        return currentTable;
    }
    
    public void setCurrentTable(Table currentTable) {
        this.currentTable = currentTable;
    }
    
    public String getCurrentTableName() {
        return currentTableName;
    }
    
    public void setCurrentTableName(String currentTableName) {
        this.currentTableName = currentTableName;
    }
    
    public List<Column> getJoinedColumns() {
        return joinedColumns;
    }
    
    public void setJoinedColumns(List<Column> joinedColumns) {
        this.joinedColumns = joinedColumns;
    }
    
    public Map<String, List<Column>> getTableColumns() {
        return tableColumns;
    }
    
    public void setTableColumns(Map<String, List<Column>> tableColumns) {
        this.tableColumns = tableColumns;
    }
    
    public List<String> getTableOrder() {
        return tableOrder;
    }
    
    public void setTableOrder(List<String> tableOrder) {
        this.tableOrder = tableOrder;
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    public void setConnection(Connection connection) {
        this.connection = connection;
    }
    
    // CTE support methods
    public void addCTEResult(String cteName, List<Column> columns, List<Row> rows) {
        cteResults.put(cteName.toLowerCase(), new CTEResult(columns, rows));
    }
    
    public CTEResult getCTEResult(String cteName) {
        return cteResults.get(cteName.toLowerCase());
    }
    
    public boolean hasCTE(String cteName) {
        return cteResults.containsKey(cteName.toLowerCase());
    }
    
    public void clearCTEs() {
        cteResults.clear();
    }
    
    /**
     * Result of a Common Table Expression execution.
     */
    public static class CTEResult {
        public final List<Column> columns;
        public final List<Row> rows;
        
        public CTEResult(List<Column> columns, List<Row> rows) {
            this.columns = columns;
            this.rows = rows;
        }
    }
}