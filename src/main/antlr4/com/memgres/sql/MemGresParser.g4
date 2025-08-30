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
    | alterTableStatement
    | dropTableStatement
    | truncateTableStatement
    | createViewStatement
    | dropViewStatement
    | createMaterializedViewStatement
    | dropMaterializedViewStatement
    | refreshMaterializedViewStatement
    | createIndexStatement
    | dropIndexStatement
    | createSequenceStatement
    | dropSequenceStatement
    | createTriggerStatement
    | dropTriggerStatement
    | createSchemaStatement
    | dropSchemaStatement
    | createProcedureStatement
    | dropProcedureStatement
    | callStatement
    | setStatement
    | explainStatement
    ;

// SELECT statement (can be compound with UNION)
selectStatement
    : compoundSelectStatement
    ;

compoundSelectStatement
    : simpleSelectStatement (unionClause simpleSelectStatement)*
    ;

simpleSelectStatement
    : withClause? SELECT selectModifier? selectList
      (FROM fromClause)?
      whereClause?
      groupByClause?
      havingClause?
      orderByClause?
      limitClause?
    ;

unionClause
    : UNION ALL?
    | INTERSECT
    | EXCEPT
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

// Basic table reference (table name, subquery, or table function)
tableReference
    : tableName (AS? alias)?
    | LPAREN selectStatement RPAREN (AS? alias)?
    | FT_SEARCH LPAREN STRING COMMA INTEGER_LITERAL COMMA INTEGER_LITERAL RPAREN (AS? alias)?
    ;

// JOIN clause with different types and conditions
joinClause
    : NATURAL joinType JOIN tableReference       # naturalJoinClause
    | joinType JOIN tableReference joinCondition # regularJoinClause
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
    | AUTO_INCREMENT
    | IDENTITY
    ;

// ALTER TABLE statement
alterTableStatement
    : ALTER TABLE (IF EXISTS)? tableName alterTableAction
    ;

alterTableAction
    : ADD (COLUMN)? columnDefinition (BEFORE columnName | AFTER columnName)?     # addColumnAction
    | DROP (COLUMN)? (IF EXISTS)? columnName                                     # dropColumnAction
    | ALTER COLUMN columnName RENAME TO columnName                               # renameColumnAction
    | RENAME TO tableName                                                        # renameTableAction
    ;

// DROP TABLE statement
dropTableStatement
    : DROP TABLE (IF EXISTS)? tableName
    ;

// TRUNCATE TABLE statement  
truncateTableStatement
    : TRUNCATE TABLE tableName (identityOption)?
    ;

identityOption
    : CONTINUE IDENTITY     # continueIdentityOption
    | RESTART IDENTITY      # restartIdentityOption
    ;

// CREATE VIEW statement
createViewStatement
    : CREATE (OR REPLACE)? (FORCE)? VIEW (IF NOT EXISTS)? viewName (LPAREN columnNameList RPAREN)? AS selectStatement
    ;

// DROP VIEW statement
dropViewStatement
    : DROP VIEW (IF EXISTS)? viewName (restrictOrCascade)?
    ;

// CREATE MATERIALIZED VIEW statement
createMaterializedViewStatement
    : CREATE (OR REPLACE)? MATERIALIZED VIEW (IF NOT EXISTS)? viewName (LPAREN columnNameList RPAREN)? AS selectStatement
    ;

// DROP MATERIALIZED VIEW statement
dropMaterializedViewStatement
    : DROP MATERIALIZED VIEW (IF EXISTS)? viewName (restrictOrCascade)?
    ;

// REFRESH MATERIALIZED VIEW statement
refreshMaterializedViewStatement
    : REFRESH MATERIALIZED VIEW viewName
    ;

restrictOrCascade
    : RESTRICT     # restrictOption
    | CASCADE      # cascadeOption
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
    | PLUS | MINUS | MULTIPLY | DIVIDE | MODULO | EXPONENT
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

viewName
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
    | COUNT LPAREN (MULTIPLY | expression) RPAREN (OVER overClause)?  # countFunction
    | SUM LPAREN expression RPAREN (OVER overClause)?   # sumFunction
    | AVG LPAREN expression RPAREN (OVER overClause)?   # avgFunction
    | MIN LPAREN expression RPAREN (OVER overClause)?   # minFunction
    | MAX LPAREN expression RPAREN (OVER overClause)?   # maxFunction
    | COUNT LPAREN DISTINCT expression RPAREN          # countDistinctFunction
    | ROW_NUMBER LPAREN RPAREN OVER overClause         # rowNumberFunction
    | RANK LPAREN RPAREN OVER overClause               # rankFunction
    | DENSE_RANK LPAREN RPAREN OVER overClause         # denseRankFunction
    | PERCENT_RANK LPAREN RPAREN OVER overClause       # percentRankFunction
    | CUME_DIST LPAREN RPAREN OVER overClause          # cumeDistFunction
    | FIRST_VALUE LPAREN expression RPAREN OVER overClause # firstValueFunction
    | LAST_VALUE LPAREN expression RPAREN OVER overClause  # lastValueFunction
    | NTH_VALUE LPAREN expression COMMA expression RPAREN OVER overClause # nthValueFunction
    | LAG LPAREN expression (COMMA expression)? (COMMA expression)? RPAREN OVER overClause # lagFunction
    | LEAD LPAREN expression (COMMA expression)? (COMMA expression)? RPAREN OVER overClause # leadFunction
    | NTILE LPAREN expression RPAREN OVER overClause   # ntileFunction
    | NEXT VALUE FOR sequenceName                      # nextValueForFunction
    | CURRENT VALUE FOR sequenceName                   # currentValueForFunction
    | DATABASE LPAREN RPAREN                           # databaseFunction
    | USER LPAREN RPAREN                               # userFunction
    | CURRENT_USER LPAREN RPAREN                       # currentUserFunction
    | SESSION_USER LPAREN RPAREN                       # sessionUserFunction
    | SESSION_ID LPAREN RPAREN                         # sessionIdFunction
    | SQRT LPAREN expression RPAREN                    # sqrtFunction
    | POWER LPAREN expression COMMA expression RPAREN  # powerFunction
    | ABS LPAREN expression RPAREN                     # absFunction
    | ROUND LPAREN expression (COMMA expression)? RPAREN # roundFunction
    | RAND LPAREN RPAREN                               # randFunction
    // H2 Date/Time functions
    | CURRENT_TIMESTAMP LPAREN RPAREN                  # currentTimestampFunction
    | CURRENT_DATE LPAREN RPAREN                       # currentDateFunction
    | CURRENT_TIME LPAREN RPAREN                       # currentTimeFunction
    | DATEADD LPAREN expression COMMA expression COMMA expression RPAREN # dateAddFunction
    | DATEDIFF LPAREN expression COMMA expression COMMA expression RPAREN # dateDiffFunction
    | FORMATDATETIME LPAREN expression COMMA expression RPAREN # formatDateTimeFunction
    | PARSEDATETIME LPAREN expression COMMA expression RPAREN # parseDateTimeFunction
    // H2 System functions
    | H2VERSION LPAREN RPAREN                          # h2VersionFunction
    | DATABASE_PATH LPAREN RPAREN                      # databasePathFunction
    | MEMORY_USED LPAREN RPAREN                        # memoryUsedFunction
    | MEMORY_FREE LPAREN RPAREN                        # memoryFreeFunction
    // H2 String utility functions (non-conflicting tokens)
    | POSITION LPAREN expression COMMA expression RPAREN # positionFunction
    | ASCII LPAREN expression RPAREN                   # asciiFunction
    | HEXTORAW LPAREN expression RPAREN                # hexToRawFunction
    | RAWTOHEX LPAREN expression RPAREN                # rawToHexFunction
    // Full-Text Search functions
    | FT_INIT LPAREN RPAREN                            # ftInitFunction
    | FT_CREATE_INDEX LPAREN expression COMMA expression COMMA expression RPAREN # ftCreateIndexFunction
    | FT_DROP_INDEX LPAREN expression COMMA expression RPAREN # ftDropIndexFunction
    | FT_SEARCH LPAREN expression COMMA expression COMMA expression RPAREN # ftSearchFunction
    | FT_REINDEX LPAREN RPAREN                         # ftReindexFunction
    | FT_DROP_ALL LPAREN RPAREN                        # ftDropAllFunction
    | FT_SET_IGNORE_LIST LPAREN expression RPAREN      # ftSetIgnoreListFunction
    | FT_SET_WHITESPACE_CHARS LPAREN expression RPAREN # ftSetWhitespaceCharsFunction
    | functionName LPAREN (expressionList)? RPAREN     # genericFunction
    ;

// Window functions
overClause
    : LPAREN (PARTITION BY expressionList)? (ORDER BY orderItemList)? RPAREN
    ;

orderItemList
    : orderItem (COMMA orderItem)*
    ;

expressionList
    : expression (COMMA expression)*
    ;

columnList
    : columnName (COMMA columnName)*
    ;

columnNameList
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
    | CLOB (LPAREN INTEGER_LITERAL RPAREN)?            # clobType
    | CHARACTER LARGE OBJECT (LPAREN INTEGER_LITERAL RPAREN)? # characterLargeObjectType
    | BLOB (LPAREN INTEGER_LITERAL RPAREN)?            # blobType
    | BINARY LARGE OBJECT (LPAREN INTEGER_LITERAL RPAREN)? # binaryLargeObjectType
    | BINARY (LPAREN INTEGER_LITERAL RPAREN)?          # binaryType
    | VARBINARY (LPAREN INTEGER_LITERAL RPAREN)?       # varbinaryType
    | BINARY VARYING (LPAREN INTEGER_LITERAL RPAREN)?  # binaryVaryingType
    | INTERVAL                                          # intervalType
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

// CREATE SEQUENCE statement
createSequenceStatement
    : CREATE SEQUENCE (IF NOT EXISTS)? sequenceName (AS dataType)? sequenceOption*
    ;

// DROP SEQUENCE statement
dropSequenceStatement
    : DROP SEQUENCE (IF EXISTS)? sequenceName
    ;

sequenceName
    : identifier
    ;

sequenceOption
    : START WITH signedIntegerLiteral
    | INCREMENT BY signedIntegerLiteral
    | MINVALUE signedIntegerLiteral
    | MAXVALUE signedIntegerLiteral
    | NOMINVALUE
    | NOMAXVALUE
    | CYCLE
    | NOCYCLE
    | CACHE signedIntegerLiteral
    | NOCACHE
    ;

// WITH clause (Common Table Expressions)
withClause
    : WITH RECURSIVE? commonTableExpression (COMMA commonTableExpression)*
    ;

commonTableExpression
    : identifier (LPAREN columnNameList RPAREN)? AS LPAREN selectStatement RPAREN
    ;

// Signed integer literal
signedIntegerLiteral
    : (PLUS | MINUS)? INTEGER_LITERAL
    ;

// CREATE TRIGGER statement
createTriggerStatement
    : CREATE TRIGGER (IF NOT EXISTS)? triggerName
      triggerTiming
      triggerEvent (COMMA triggerEvent)*
      ON tableName
      (FOR EACH (ROW | STATEMENT))?
      (QUEUE INTEGER_LITERAL)?
      NOWAIT?
      triggerImplementation
    ;

// DROP TRIGGER statement
dropTriggerStatement
    : DROP TRIGGER (IF EXISTS)? triggerName
    ;

triggerName
    : identifier
    ;

triggerTiming
    : BEFORE | AFTER | (INSTEAD OF)
    ;

triggerEvent
    : INSERT | UPDATE | DELETE | SELECT
    ;

triggerImplementation
    : CALL STRING                           # callTriggerImplementation
    | AS STRING                             # sourceTriggerImplementation
    ;

// CREATE PROCEDURE statement
createProcedureStatement
    : CREATE PROCEDURE procedureName (LPAREN procedureParameterList? RPAREN)? AS javaClassName
    ;

// DROP PROCEDURE statement
dropProcedureStatement
    : DROP PROCEDURE (IF EXISTS)? procedureName
    ;

// CALL statement
callStatement
    : CALL procedureName LPAREN callParameterList? RPAREN
    ;

// Procedure parameter definition
procedureParameterList
    : procedureParameter (COMMA procedureParameter)*
    ;

procedureParameter
    : parameterDirection? parameterName dataType
    ;

parameterDirection
    : IN
    | OUT
    | INOUT
    ;

parameterName
    : identifier
    ;

procedureName
    : identifier (DOT identifier)*
    ;

javaClassName
    : STRING
    ;

// Call parameter list (values for CALL statement)
callParameterList
    : callParameter (COMMA callParameter)*
    ;

callParameter
    : expression
    | JSONB_EXISTS  // Parameter placeholder (reusing ? token)
    ;

// CREATE SCHEMA statement
createSchemaStatement
    : CREATE SCHEMA (IF NOT EXISTS)? schemaName
    ;

// DROP SCHEMA statement
dropSchemaStatement
    : DROP SCHEMA (IF EXISTS)? schemaName (CASCADE | RESTRICT)?
    ;

// SET statement for configuration
setStatement
    : SET configurationKey EQ configurationValue
    | SET configurationKey TO configurationValue
    ;

// EXPLAIN statement for query plans
explainStatement
    : EXPLAIN selectStatement
    | EXPLAIN insertStatement
    | EXPLAIN updateStatement
    | EXPLAIN deleteStatement
    ;

schemaName
    : identifier
    ;

configurationKey
    : identifier (DOT identifier)*
    ;

configurationValue
    : literal
    | identifier
    ;

// Function names (includes identifiers and reserved keywords that can be function names)
functionName
    : identifier
    | LEFT          // H2 string function LEFT(string, length)
    | RIGHT         // H2 string function RIGHT(string, length)  
    | CHAR          // H2 string function CHAR(ascii_code)
    ;

// Identifiers (case-insensitive)
identifier
    : IDENTIFIER
    | QUOTED_IDENTIFIER
    ;