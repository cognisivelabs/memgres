package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstNode;
import com.memgres.sql.ast.AstVisitor;
import com.memgres.sql.ast.expression.Expression;

import java.util.Optional;

/**
 * Represents a JOIN clause in SQL.
 */
public class JoinClause extends AstNode {
    
    public enum JoinType {
        INNER, LEFT, RIGHT, FULL_OUTER
    }
    
    private final JoinType joinType;
    private final TableReference table;
    private final Optional<Expression> onCondition;
    
    public JoinClause(JoinType joinType, TableReference table, Optional<Expression> onCondition) {
        this.joinType = joinType;
        this.table = table;
        this.onCondition = onCondition;
    }
    
    public JoinType getJoinType() {
        return joinType;
    }
    
    public TableReference getTable() {
        return table;
    }
    
    public Optional<Expression> getOnCondition() {
        return onCondition;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitJoinClause(this, context);
    }
}