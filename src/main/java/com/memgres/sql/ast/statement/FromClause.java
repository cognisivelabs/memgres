package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstNode;
import com.memgres.sql.ast.AstVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a FROM clause in a SELECT statement.
 * Can contain multiple joinable tables (for comma-separated tables) or a single joinable table with joins.
 */
public class FromClause extends AstNode {
    
    private final List<JoinableTable> joinableTables;
    
    public FromClause(List<JoinableTable> joinableTables) {
        this.joinableTables = joinableTables;
    }
    
    /**
     * Constructor for backward compatibility with simple table references.
     */
    public FromClause(List<TableReference> tableReferences, boolean isLegacyConstructor) {
        this.joinableTables = new ArrayList<>();
        for (TableReference tableRef : tableReferences) {
            this.joinableTables.add(new JoinableTable(tableRef));
        }
    }
    
    public List<JoinableTable> getJoinableTables() {
        return joinableTables;
    }
    
    /**
     * Legacy method for backward compatibility.
     * Returns all table references from all joinable tables.
     */
    @Deprecated
    public List<TableReference> getTableReferences() {
        List<TableReference> allTables = new ArrayList<>();
        for (JoinableTable joinable : joinableTables) {
            allTables.addAll(joinable.getAllTableReferences());
        }
        return allTables;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitFromClause(this, context);
    }
}