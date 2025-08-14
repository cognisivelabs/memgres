package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstVisitor;

import java.util.List;
import java.util.Objects;

/**
 * Represents a compound SELECT statement that can contain multiple SELECT statements
 * connected by UNION or UNION ALL operations.
 */
public class CompoundSelectStatement extends Statement {
    
    private final List<SimpleSelectStatement> selectStatements;
    private final List<UnionClause> unionClauses;
    
    public CompoundSelectStatement(List<SimpleSelectStatement> selectStatements, List<UnionClause> unionClauses) {
        this.selectStatements = selectStatements;
        this.unionClauses = unionClauses;
        
        if (selectStatements.size() != unionClauses.size() + 1) {
            throw new IllegalArgumentException("Number of SELECT statements must be one more than number of UNION clauses");
        }
    }
    
    public List<SimpleSelectStatement> getSelectStatements() {
        return selectStatements;
    }
    
    public List<UnionClause> getUnionClauses() {
        return unionClauses;
    }
    
    public boolean isSimple() {
        return unionClauses.isEmpty();
    }
    
    public SimpleSelectStatement getFirstSelectStatement() {
        return selectStatements.get(0);
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitCompoundSelectStatement(this, context);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CompoundSelectStatement that = (CompoundSelectStatement) obj;
        return Objects.equals(selectStatements, that.selectStatements) &&
               Objects.equals(unionClauses, that.unionClauses);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(selectStatements, unionClauses);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selectStatements.size(); i++) {
            if (i > 0) {
                sb.append(" ").append(unionClauses.get(i - 1)).append(" ");
            }
            sb.append(selectStatements.get(i));
        }
        return sb.toString();
    }
}