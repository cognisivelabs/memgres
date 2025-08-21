package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstNode;
import com.memgres.sql.ast.AstVisitor;
import com.memgres.sql.ast.expression.Expression;

import java.util.List;
import java.util.Optional;

/**
 * Represents a JOIN clause in SQL.
 * Supports ON, USING, and NATURAL join conditions.
 */
public class JoinClause extends AstNode {
    
    public enum JoinType {
        INNER, LEFT, RIGHT, FULL_OUTER
    }
    
    public enum JoinConditionType {
        ON,        // JOIN ... ON expression
        USING,     // JOIN ... USING (column1, column2, ...)
        NATURAL    // NATURAL JOIN (no explicit condition)
    }
    
    private final JoinType joinType;
    private final TableReference table;
    private final JoinConditionType conditionType;
    private final Optional<Expression> onCondition;
    private final Optional<List<String>> usingColumns;
    
    /**
     * Constructor for ON condition joins.
     */
    public JoinClause(JoinType joinType, TableReference table, Expression onCondition) {
        this.joinType = joinType;
        this.table = table;
        this.conditionType = JoinConditionType.ON;
        this.onCondition = Optional.of(onCondition);
        this.usingColumns = Optional.empty();
    }
    
    /**
     * Constructor for USING condition joins.
     */
    public JoinClause(JoinType joinType, TableReference table, List<String> usingColumns) {
        this.joinType = joinType;
        this.table = table;
        this.conditionType = JoinConditionType.USING;
        this.onCondition = Optional.empty();
        this.usingColumns = Optional.of(usingColumns);
    }
    
    /**
     * Constructor for NATURAL joins (no explicit condition).
     */
    public JoinClause(JoinType joinType, TableReference table) {
        this.joinType = joinType;
        this.table = table;
        this.conditionType = JoinConditionType.NATURAL;
        this.onCondition = Optional.empty();
        this.usingColumns = Optional.empty();
    }
    
    /**
     * Legacy constructor for backwards compatibility.
     * @deprecated Use specific constructors for each join condition type
     */
    @Deprecated
    public JoinClause(JoinType joinType, TableReference table, Optional<Expression> onCondition) {
        this.joinType = joinType;
        this.table = table;
        this.conditionType = onCondition.isPresent() ? JoinConditionType.ON : JoinConditionType.NATURAL;
        this.onCondition = onCondition;
        this.usingColumns = Optional.empty();
    }
    
    public JoinType getJoinType() {
        return joinType;
    }
    
    public TableReference getTable() {
        return table;
    }
    
    public JoinConditionType getConditionType() {
        return conditionType;
    }
    
    public Optional<Expression> getOnCondition() {
        return onCondition;
    }
    
    public Optional<List<String>> getUsingColumns() {
        return usingColumns;
    }
    
    public boolean isNaturalJoin() {
        return conditionType == JoinConditionType.NATURAL;
    }
    
    public boolean isUsingJoin() {
        return conditionType == JoinConditionType.USING;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitJoinClause(this, context);
    }
}