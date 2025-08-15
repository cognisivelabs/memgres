package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstNode;
import com.memgres.sql.ast.AstVisitor;

import java.util.Objects;

/**
 * Represents a set operation clause between SELECT statements.
 * Supports UNION, UNION ALL, INTERSECT, and EXCEPT operations.
 */
public class UnionClause extends AstNode {
    
    public enum UnionType {
        UNION,      // UNION (removes duplicates)
        UNION_ALL,  // UNION ALL (keeps duplicates)
        INTERSECT,  // INTERSECT (returns common rows, removes duplicates)
        EXCEPT      // EXCEPT (returns rows from first set not in second, removes duplicates)
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
    
    public boolean isIntersect() {
        return unionType == UnionType.INTERSECT;
    }
    
    public boolean isExcept() {
        return unionType == UnionType.EXCEPT;
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
        switch (unionType) {
            case UNION_ALL:
                return "UNION ALL";
            case INTERSECT:
                return "INTERSECT";
            case EXCEPT:
                return "EXCEPT";
            default:
                return "UNION";
        }
    }
}