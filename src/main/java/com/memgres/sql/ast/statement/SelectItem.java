package com.memgres.sql.ast.statement;

import com.memgres.sql.ast.AstNode;
import com.memgres.sql.ast.AstVisitor;
import com.memgres.sql.ast.expression.Expression;

import java.util.Optional;

/**
 * Represents a single item in the SELECT list (e.g., column, expression, or *).
 */
public class SelectItem extends AstNode {
    
    private final Expression expression;
    private final Optional<String> alias;
    private final boolean isWildcard;
    
    /**
     * Constructor for regular select items (column, expression).
     */
    public SelectItem(Expression expression, Optional<String> alias) {
        this.expression = expression;
        this.alias = alias;
        this.isWildcard = false;
    }
    
    /**
     * Constructor for wildcard select item (*).
     */
    public SelectItem() {
        this.expression = null;
        this.alias = Optional.empty();
        this.isWildcard = true;
    }
    
    public Expression getExpression() {
        return expression;
    }
    
    public Optional<String> getAlias() {
        return alias;
    }
    
    public boolean isWildcard() {
        return isWildcard;
    }
    
    @Override
    public <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception {
        return visitor.visitSelectItem(this, context);
    }
}