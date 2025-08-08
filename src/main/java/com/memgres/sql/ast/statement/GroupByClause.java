package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstNode;
import com.memgres.sql.ast.AstVisitor;
import com.memgres.sql.ast.expression.Expression;

import java.util.List;

/**
 * Represents a GROUP BY clause in a SELECT statement.
 */
public class GroupByClause extends AstNode {
    
    private final List<Expression> groupingExpressions;
    
    public GroupByClause(List<Expression> groupingExpressions) {
        this.groupingExpressions = groupingExpressions;
    }
    
    public List<Expression> getGroupingExpressions() {
        return groupingExpressions;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitGroupByClause(this, context);
    }
}