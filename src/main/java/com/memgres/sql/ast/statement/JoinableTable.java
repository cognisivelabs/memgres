package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstNode;
import com.memgres.sql.ast.AstVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents a table or set of joined tables in a FROM clause.
 * This can be either a simple table reference or a table with one or more joins.
 */
public class JoinableTable extends AstNode {
    
    private final TableReference baseTable;
    private final List<JoinClause> joins;
    
    /**
     * Constructor for a simple table reference without joins.
     */
    public JoinableTable(TableReference baseTable) {
        this.baseTable = baseTable;
        this.joins = new ArrayList<>();
    }
    
    /**
     * Constructor for a table with joins.
     */
    public JoinableTable(TableReference baseTable, List<JoinClause> joins) {
        this.baseTable = baseTable;
        this.joins = new ArrayList<>(joins);
    }
    
    public TableReference getBaseTable() {
        return baseTable;
    }
    
    public List<JoinClause> getJoins() {
        return joins;
    }
    
    public boolean hasJoins() {
        return !joins.isEmpty();
    }
    
    /**
     * Get all table references involved in this joinable table.
     * This includes the base table and all joined tables.
     */
    public List<TableReference> getAllTableReferences() {
        List<TableReference> tables = new ArrayList<>();
        tables.add(baseTable);
        for (JoinClause join : joins) {
            tables.add(join.getTable());
        }
        return tables;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitJoinableTable(this, context);
    }
}