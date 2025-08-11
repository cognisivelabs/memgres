parser grammar MemGresParser;

options {
    tokenVocab = MemGresLexer;
}

// Main entry point
sql: statement (SEMICOLON statement)* SEMICOLON? EOF;

statement
    : selectStatement
    | insertStatement
    | updateStatement
    | deleteStatement
    | mergeStatement
    | createTableStatement
    | dropTableStatement
    | createIndexStatement
    | dropIndexStatement
    ;

// SELECT statement
selectStatement
    : SELECT selectModifier? selectList
      (FROM fromClause)?
      whereClause?
      groupByClause?
      havingClause?
      orderByClause?
      limitClause?
    ;

selectModifier
    : DISTINCT
    | ALL
    ;

selectList
    : selectItem (COMMA selectItem)*
    | MULTIPLY
    ;

selectItem
    : expression (AS? alias)?
    ;

// FROM clause - can contain multiple joinable tables separated by commas
fromClause
    : joinableTable (COMMA joinableTable)*
    ;

// A joinable table is a base table optionally followed by one or more joins
joinableTable
    : tableReference joinClause*
    ;

// Basic table reference (table name or subquery)
tableReference
    : tableName (AS? alias)?
    | LPAREN selectStatement RPAREN (AS? alias)?
    ;

// JOIN clause with different types and conditions
joinClause
    : joinType JOIN tableReference joinCondition
    ;

// JOIN types
joinType
    : INNER?                    # innerJoin
    | LEFT OUTER?               # leftJoin
    | RIGHT OUTER?              # rightJoin
    | FULL OUTER?               # fullOuterJoin
    | CROSS                     # crossJoin
    ;

// JOIN conditions (ON or USING)
joinCondition
    : ON expression             # onJoinCondition
    | USING LPAREN columnList RPAREN  # usingJoinCondition
    |                           # naturalJoinCondition
    ;

// WHERE clause
whereClause
    : WHERE expression
    ;

// GROUP BY clause
groupByClause
    : GROUP BY expression (COMMA expression)*
    ;

// HAVING clause
havingClause
    : HAVING expression
    ;

// ORDER BY clause
orderByClause
    : ORDER BY orderItem (COMMA orderItem)*
    ;

orderItem
    : expression (ASC | DESC)?
    ;

// LIMIT clause
limitClause
    : LIMIT expression (OFFSET expression)?
    ;

// INSERT statement
insertStatement
    : INSERT INTO tableName (LPAREN columnList RPAREN)? 
      (VALUES valuesClause (COMMA valuesClause)* | selectStatement)
    ;

valuesClause
    : LPAREN expression (COMMA expression)* RPAREN
    ;

// UPDATE statement
updateStatement
    : UPDATE tableName SET updateItem (COMMA updateItem)* whereClause?
    ;

updateItem
    : columnName EQ expression
    ;

// DELETE statement
deleteStatement
    : DELETE FROM tableName whereClause?
    ;

// CREATE TABLE statement
createTableStatement
    : CREATE TABLE tableName LPAREN columnDefinition (COMMA columnDefinition)* RPAREN
    ;

columnDefinition
    : columnName dataType columnConstraint*
    ;

columnConstraint
    : NOT NULL
    | NULL
    | PRIMARY KEY
    | UNIQUE
    ;

// DROP TABLE statement
dropTableStatement
    : DROP TABLE tableName
    ;

// CREATE INDEX statement
createIndexStatement
    : CREATE (UNIQUE (NULLS DISTINCT)? | SPATIAL)? INDEX (IF NOT EXISTS)? indexName? ON tableName LPAREN indexColumnList RPAREN (INCLUDE LPAREN indexColumnList RPAREN)?
    ;

// DROP INDEX statement
dropIndexStatement
    : DROP INDEX (IF EXISTS)? indexName
    ;

indexName
    : identifier
    ;

indexColumnList
    : indexColumn (COMMA indexColumn)*
    ;

indexColumn
    : columnName (ASC | DESC)? (NULLS (FIRST | LAST))?
    ;

// Expressions
expression
    : literal                                           # literalExpression
    | columnReference                                   # columnReferenceExpression
    | functionCall                                      # functionCallExpression
    | LPAREN expression RPAREN                         # parenthesizedExpression
    | LPAREN selectStatement RPAREN                    # subqueryExpression
    | expression binaryOperator expression             # binaryExpression
    | NOT expression                                    # notExpression
    | expression IS (NOT)? NULL                        # isNullExpression
    | expression (NOT)? IN LPAREN expressionList RPAREN # inExpression
    | expression (NOT)? IN LPAREN selectStatement RPAREN # inSubqueryExpression
    | expression (NOT)? LIKE expression                # likeExpression
    | expression (NOT)? BETWEEN expression AND expression # betweenExpression
    | CASE whenClause+ (ELSE expression)? END          # caseExpression
    | EXISTS LPAREN selectStatement RPAREN             # existsExpression
    ;

whenClause
    : WHEN expression THEN expression
    ;

binaryOperator
    : EQ | NE | LT | LE | GT | GE
    | PLUS | MINUS | MULTIPLY | DIVIDE | MODULO | POWER
    | AND | OR
    | CONCAT
    | JSONB_CONTAINS | JSONB_CONTAINED | JSONB_EXISTS
    | JSONB_EXTRACT | JSONB_EXTRACT_TEXT
    | JSONB_PATH_EXTRACT | JSONB_PATH_EXTRACT_TEXT
    ;

// Literals
literal
    : STRING                    # stringLiteral
    | INTEGER_LITERAL           # integerLiteral
    | DECIMAL_LITERAL           # decimalLiteral
    | SCIENTIFIC_LITERAL        # scientificLiteral
    | TRUE                      # booleanLiteral
    | FALSE                     # booleanLiteral
    | NULL                      # nullLiteral
    ;

// Column and table references
columnReference
    : columnName
    | tableName DOT columnName
    ;

tableName
    : identifier
    ;

columnName
    : identifier
    ;

alias
    : identifier
    ;

// Function calls
functionCall
    : GEN_RANDOM_UUID LPAREN RPAREN                    # genRandomUuidFunction
    | UUID_GENERATE_V1 LPAREN RPAREN                   # uuidGenerateV1Function
    | UUID_GENERATE_V4 LPAREN RPAREN                   # uuidGenerateV4Function
    | COUNT LPAREN (MULTIPLY | expression) RPAREN      # countFunction
    | SUM LPAREN expression RPAREN                     # sumFunction
    | AVG LPAREN expression RPAREN                     # avgFunction
    | MIN LPAREN expression RPAREN                     # minFunction
    | MAX LPAREN expression RPAREN                     # maxFunction
    | COUNT LPAREN DISTINCT expression RPAREN          # countDistinctFunction
    | identifier LPAREN (expressionList)? RPAREN       # genericFunction
    ;

expressionList
    : expression (COMMA expression)*
    ;

columnList
    : columnName (COMMA columnName)*
    ;

// Data types
dataType
    : SMALLINT                                          # smallintType
    | (INTEGER | INT)                                   # integerType
    | BIGINT                                            # bigintType
    | DECIMAL (LPAREN INTEGER_LITERAL (COMMA INTEGER_LITERAL)? RPAREN)? # decimalType
    | NUMERIC (LPAREN INTEGER_LITERAL (COMMA INTEGER_LITERAL)? RPAREN)? # numericType
    | REAL                                              # realType
    | DOUBLE PRECISION                                  # doublePrecisionType
    | VARCHAR (LPAREN INTEGER_LITERAL RPAREN)?         # varcharType
    | CHAR (LPAREN INTEGER_LITERAL RPAREN)?            # charType
    | TEXT                                              # textType
    | BOOLEAN                                           # booleanType
    | DATE                                              # dateType
    | TIME                                              # timeType
    | TIMESTAMP                                         # timestampType
    | TIMESTAMPTZ                                       # timestamptzType
    | UUID                                              # uuidType
    | JSONB                                             # jsonbType
    | BYTEA                                             # byteaType
    ;

// MERGE statement - supports both simple and advanced H2 MERGE syntax
mergeStatement
    : simpleMergeStatement
    | advancedMergeStatement
    ;

// Simple MERGE: MERGE INTO table KEY(columns) VALUES(values)
simpleMergeStatement
    : MERGE INTO tableName KEY LPAREN keyColumnList RPAREN VALUES valuesClause (COMMA valuesClause)*
    ;

// Advanced MERGE: MERGE INTO target USING source ON condition WHEN...
advancedMergeStatement
    : MERGE INTO tableName (AS? alias)? 
      USING mergeSource (AS? alias)?
      ON expression
      mergeWhenClause+
    ;

mergeSource
    : tableName                           # tableSource
    | LPAREN selectStatement RPAREN       # subquerySource
    ;

mergeWhenClause
    : WHEN MATCHED (AND expression)? THEN mergeAction
    | WHEN NOT MATCHED (AND expression)? THEN mergeInsertAction
    ;

mergeAction
    : UPDATE SET updateItem (COMMA updateItem)*
    | DELETE
    ;

mergeInsertAction
    : INSERT (LPAREN columnList RPAREN)? VALUES valuesClause
    ;

keyColumnList
    : identifier (COMMA identifier)*
    ;

// Identifiers (case-insensitive)
identifier
    : IDENTIFIER
    | QUOTED_IDENTIFIER
    ;