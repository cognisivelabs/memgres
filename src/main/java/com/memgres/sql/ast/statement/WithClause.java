package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstNode;
import com.memgres.sql.ast.AstVisitor;

import java.util.List;
import java.util.Objects;

/**
 * Represents a WITH clause containing one or more Common Table Expressions (CTEs).
 * Supports both recursive and non-recursive CTEs.
 * 
 * Syntax: WITH [RECURSIVE] cte1, cte2, ... 
 */
public class WithClause extends AstNode {
    
    private final boolean recursive;
    private final List<CommonTableExpression> commonTableExpressions;
    
    public WithClause(boolean recursive, List<CommonTableExpression> commonTableExpressions) {
        this.recursive = recursive;
        this.commonTableExpressions = commonTableExpressions;
    }
    
    public boolean isRecursive() {
        return recursive;
    }
    
    public List<CommonTableExpression> getCommonTableExpressions() {
        return commonTableExpressions;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitWithClause(this, context);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        WithClause that = (WithClause) obj;
        return recursive == that.recursive &&
               Objects.equals(commonTableExpressions, that.commonTableExpressions);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(recursive, commonTableExpressions);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("WITH");
        if (recursive) {
            sb.append(" RECURSIVE");
        }
        sb.append(" ");
        for (int i = 0; i < commonTableExpressions.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(commonTableExpressions.get(i));
        }
        return sb.toString();
    }
}