package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstVisitor;
import com.memgres.sql.ast.expression.Expression;

import java.util.List;
import java.util.Optional;

/**
 * Represents a SELECT statement in the SQL AST.
 */
public class SelectStatement extends Statement {
    
    private final boolean distinct;
    private final List<SelectItem> selectItems;
    private final Optional<FromClause> fromClause;
    private final Optional<WhereClause> whereClause;
    private final Optional<GroupByClause> groupByClause;
    private final Optional<HavingClause> havingClause;
    private final Optional<OrderByClause> orderByClause;
    private final Optional<LimitClause> limitClause;
    
    public SelectStatement(boolean distinct,
                          List<SelectItem> selectItems,
                          Optional<FromClause> fromClause,
                          Optional<WhereClause> whereClause,
                          Optional<GroupByClause> groupByClause,
                          Optional<HavingClause> havingClause,
                          Optional<OrderByClause> orderByClause,
                          Optional<LimitClause> limitClause) {
        this.distinct = distinct;
        this.selectItems = selectItems;
        this.fromClause = fromClause;
        this.whereClause = whereClause;
        this.groupByClause = groupByClause;
        this.havingClause = havingClause;
        this.orderByClause = orderByClause;
        this.limitClause = limitClause;
    }
    
    public boolean isDistinct() {
        return distinct;
    }
    
    public List<SelectItem> getSelectItems() {
        return selectItems;
    }
    
    public Optional<FromClause> getFromClause() {
        return fromClause;
    }
    
    public Optional<WhereClause> getWhereClause() {
        return whereClause;
    }
    
    public Optional<GroupByClause> getGroupByClause() {
        return groupByClause;
    }
    
    public Optional<HavingClause> getHavingClause() {
        return havingClause;
    }
    
    public Optional<OrderByClause> getOrderByClause() {
        return orderByClause;
    }
    
    public Optional<LimitClause> getLimitClause() {
        return limitClause;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitSelectStatement(this, context);
    }
}