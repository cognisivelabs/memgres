package com.memgres.sql.parser;

import com.memgres.sql.PostgreSQLLexer;
import com.memgres.sql.PostgreSQLParser;
import com.memgres.sql.ast.statement.Statement;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.util.List;

/**
 * Main SQL parser that converts SQL text into AST statements.
 */
public class SqlParser {
    
    /**
     * Parse a SQL string into a list of Statement AST nodes.
     * 
     * @param sql the SQL string to parse
     * @return a list of parsed statements
     * @throws SqlParseException if parsing fails
     */
    public List<Statement> parse(String sql) throws SqlParseException {
        try {
            // Create input stream from SQL string
            CharStream input = CharStreams.fromString(sql);
            
            // Create lexer and token stream
            PostgreSQLLexer lexer = new PostgreSQLLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            
            // Create parser
            PostgreSQLParser parser = new PostgreSQLParser(tokens);
            
            // Add error listener to capture parsing errors
            parser.removeErrorListeners();
            parser.addErrorListener(new SqlErrorListener());
            
            // Parse the SQL
            PostgreSQLParser.SqlContext parseTree = parser.sql();
            
            // Convert parse tree to AST
            SqlAstBuilder astBuilder = new SqlAstBuilder();
            return astBuilder.visit(parseTree);
            
        } catch (Exception e) {
            throw new SqlParseException("Failed to parse SQL: " + sql, e);
        }
    }
    
    /**
     * Error listener for capturing ANTLR parsing errors.
     */
    private static class SqlErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, 
                               Object offendingSymbol,
                               int line, 
                               int charPositionInLine, 
                               String msg, 
                               RecognitionException e) {
            throw new RuntimeException(new SqlParseException("Syntax error at line " + line + ":" + charPositionInLine + " - " + msg));
        }
    }
}