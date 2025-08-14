package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstVisitor;
import com.memgres.sql.ast.expression.Expression;

import java.util.List;
import java.util.Optional;

/**
 * Represents a SELECT statement in the SQL AST.
 * Now wraps CompoundSelectStatement to support UNION operations while maintaining backward compatibility.
 */
public class SelectStatement extends Statement {
    
    private final CompoundSelectStatement compoundSelectStatement;
    
    // Constructor for simple SELECT statements (backward compatibility)
    public SelectStatement(Optional<WithClause> withClause,
                          boolean distinct,
                          List<SelectItem> selectItems,
                          Optional<FromClause> fromClause,
                          Optional<WhereClause> whereClause,
                          Optional<GroupByClause> groupByClause,
                          Optional<HavingClause> havingClause,
                          Optional<OrderByClause> orderByClause,
                          Optional<LimitClause> limitClause) {
        // Create a simple compound SELECT statement with no UNION operations
        SimpleSelectStatement simpleSelect = new SimpleSelectStatement(
            withClause, distinct, selectItems, fromClause, whereClause,
            groupByClause, havingClause, orderByClause, limitClause
        );
        this.compoundSelectStatement = new CompoundSelectStatement(
            List.of(simpleSelect), List.of()
        );
    }
    
    // Constructor for compound SELECT statements (with UNION operations)
    public SelectStatement(CompoundSelectStatement compoundSelectStatement) {
        this.compoundSelectStatement = compoundSelectStatement;
    }
    
    // Delegate to the first (and possibly only) simple SELECT statement for backward compatibility
    public Optional<WithClause> getWithClause() {
        return compoundSelectStatement.getFirstSelectStatement().getWithClause();
    }
    
    public boolean isDistinct() {
        return compoundSelectStatement.getFirstSelectStatement().isDistinct();
    }
    
    public List<SelectItem> getSelectItems() {
        return compoundSelectStatement.getFirstSelectStatement().getSelectItems();
    }
    
    public Optional<FromClause> getFromClause() {
        return compoundSelectStatement.getFirstSelectStatement().getFromClause();
    }
    
    public Optional<WhereClause> getWhereClause() {
        return compoundSelectStatement.getFirstSelectStatement().getWhereClause();
    }
    
    public Optional<GroupByClause> getGroupByClause() {
        return compoundSelectStatement.getFirstSelectStatement().getGroupByClause();
    }
    
    public Optional<HavingClause> getHavingClause() {
        return compoundSelectStatement.getFirstSelectStatement().getHavingClause();
    }
    
    public Optional<OrderByClause> getOrderByClause() {
        return compoundSelectStatement.getFirstSelectStatement().getOrderByClause();
    }
    
    public Optional<LimitClause> getLimitClause() {
        return compoundSelectStatement.getFirstSelectStatement().getLimitClause();
    }
    
    // New methods for compound SELECT functionality
    public CompoundSelectStatement getCompoundSelectStatement() {
        return compoundSelectStatement;
    }
    
    public boolean isCompound() {
        return !compoundSelectStatement.isSimple();
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitSelectStatement(this, context);
    }
}