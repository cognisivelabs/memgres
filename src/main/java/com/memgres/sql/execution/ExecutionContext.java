package com.memgres.sql.execution;

import com.memgres.storage.Table;
import com.memgres.types.Column;
import com.memgres.types.Row;

import java.util.List;

/**
 * Execution context that maintains state during SQL statement execution.
 */
public class ExecutionContext {
    
    private Row currentRow;
    private Table currentTable;
    private List<Column> joinedColumns;
    
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
    
    public List<Column> getJoinedColumns() {
        return joinedColumns;
    }
    
    public void setJoinedColumns(List<Column> joinedColumns) {
        this.joinedColumns = joinedColumns;
    }
}