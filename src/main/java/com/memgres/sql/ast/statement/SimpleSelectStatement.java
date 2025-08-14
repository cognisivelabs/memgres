package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstVisitor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a simple (non-compound) SELECT statement.
 * This is the basic SELECT statement without UNION operations.
 */
public class SimpleSelectStatement extends Statement {
    
    private final Optional<WithClause> withClause;
    private final boolean distinct;
    private final List<SelectItem> selectItems;
    private final Optional<FromClause> fromClause;
    private final Optional<WhereClause> whereClause;
    private final Optional<GroupByClause> groupByClause;
    private final Optional<HavingClause> havingClause;
    private final Optional<OrderByClause> orderByClause;
    private final Optional<LimitClause> limitClause;
    
    public SimpleSelectStatement(Optional<WithClause> withClause,
                                boolean distinct,
                                List<SelectItem> selectItems,
                                Optional<FromClause> fromClause,
                                Optional<WhereClause> whereClause,
                                Optional<GroupByClause> groupByClause,
                                Optional<HavingClause> havingClause,
                                Optional<OrderByClause> orderByClause,
                                Optional<LimitClause> limitClause) {
        this.withClause = withClause;
        this.distinct = distinct;
        this.selectItems = selectItems;
        this.fromClause = fromClause;
        this.whereClause = whereClause;
        this.groupByClause = groupByClause;
        this.havingClause = havingClause;
        this.orderByClause = orderByClause;
        this.limitClause = limitClause;
    }
    
    public Optional<WithClause> getWithClause() {
        return withClause;
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
        return visitor.visitSimpleSelectStatement(this, context);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SimpleSelectStatement that = (SimpleSelectStatement) obj;
        return distinct == that.distinct &&
               Objects.equals(withClause, that.withClause) &&
               Objects.equals(selectItems, that.selectItems) &&
               Objects.equals(fromClause, that.fromClause) &&
               Objects.equals(whereClause, that.whereClause) &&
               Objects.equals(groupByClause, that.groupByClause) &&
               Objects.equals(havingClause, that.havingClause) &&
               Objects.equals(orderByClause, that.orderByClause) &&
               Objects.equals(limitClause, that.limitClause);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(withClause, distinct, selectItems, fromClause, whereClause, 
                          groupByClause, havingClause, orderByClause, limitClause);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        if (withClause.isPresent()) {
            sb.append(withClause.get()).append(" ");
        }
        
        sb.append("SELECT ");
        if (distinct) {
            sb.append("DISTINCT ");
        }
        
        for (int i = 0; i < selectItems.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(selectItems.get(i));
        }
        
        if (fromClause.isPresent()) {
            sb.append(" FROM ").append(fromClause.get());
        }
        
        if (whereClause.isPresent()) {
            sb.append(" ").append(whereClause.get());
        }
        
        if (groupByClause.isPresent()) {
            sb.append(" ").append(groupByClause.get());
        }
        
        if (havingClause.isPresent()) {
            sb.append(" ").append(havingClause.get());
        }
        
        if (orderByClause.isPresent()) {
            sb.append(" ").append(orderByClause.get());
        }
        
        if (limitClause.isPresent()) {
            sb.append(" ").append(limitClause.get());
        }
        
        return sb.toString();
    }
}