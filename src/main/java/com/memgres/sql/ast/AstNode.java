package com.memgres.sql.ast;

/**
 * Base class for all Abstract Syntax Tree nodes in the SQL parser.
 * Represents a node in the parsed SQL statement structure.
 */
public abstract class AstNode {
    
    /**
     * Accept a visitor for traversing the AST.
     * @param visitor the visitor to accept
     * @param <T> the return type of the visitor
     * @param <C> the context type for the visitor
     * @return the result of the visitor's visit method
     * @throws Exception if the visitor throws an exception
     */
    public abstract <T, C> T accept(AstVisitor<T, C> visitor, C context) throws Exception;
    
    /**
     * Get a string representation of this AST node.
     * @return a string representation
     */
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}