package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstNode;
import com.memgres.sql.ast.AstVisitor;

import java.util.Objects;

/**
 * Represents a UNION or UNION ALL clause between SELECT statements.
 */
public class UnionClause extends AstNode {
    
    public enum UnionType {
        UNION,      // UNION (removes duplicates)
        UNION_ALL   // UNION ALL (keeps duplicates)
    }
    
    private final UnionType unionType;
    
    public UnionClause(UnionType unionType) {
        this.unionType = unionType;
    }
    
    public UnionType getUnionType() {
        return unionType;
    }
    
    public boolean isUnionAll() {
        return unionType == UnionType.UNION_ALL;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitUnionClause(this, context);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UnionClause that = (UnionClause) obj;
        return unionType == that.unionType;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(unionType);
    }
    
    @Override
    public String toString() {
        return unionType == UnionType.UNION_ALL ? "UNION ALL" : "UNION";
    }
}