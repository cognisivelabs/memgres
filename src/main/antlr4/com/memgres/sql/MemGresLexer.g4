lexer grammar MemGresLexer;

// Keywords (case-insensitive)
SELECT: [Ss][Ee][Ll][Ee][Cc][Tt];
FROM: [Ff][Rr][Oo][Mm];
WHERE: [Ww][Hh][Ee][Rr][Ee];
INSERT: [Ii][Nn][Ss][Ee][Rr][Tt];
INTO: [Ii][Nn][Tt][Oo];
VALUES: [Vv][Aa][Ll][Uu][Ee][Ss];
UPDATE: [Uu][Pp][Dd][Aa][Tt][Ee];
SET: [Ss][Ee][Tt];
DELETE: [Dd][Ee][Ll][Ee][Tt][Ee];
MERGE: [Mm][Ee][Rr][Gg][Ee];
MATCHED: [Mm][Aa][Tt][Cc][Hh][Ee][Dd];
CREATE: [Cc][Rr][Ee][Aa][Tt][Ee];
ALTER: [Aa][Ll][Tt][Ee][Rr];
TABLE: [Tt][Aa][Bb][Ll][Ee];
PROCEDURE: [Pp][Rr][Oo][Cc][Ee][Dd][Uu][Rr][Ee];
CALL: [Cc][Aa][Ll][Ll];
COLUMN: [Cc][Oo][Ll][Uu][Mm][Nn];
ADD: [Aa][Dd][Dd];
DROP: [Dd][Rr][Oo][Pp];
RENAME: [Rr][Ee][Nn][Aa][Mm][Ee];
TO: [Tt][Oo];
BEFORE: [Bb][Ee][Ff][Oo][Rr][Ee];
AFTER: [Aa][Ff][Tt][Ee][Rr];
INDEX: [Ii][Nn][Dd][Ee][Xx];
IF: [Ii][Ff];
SPATIAL: [Ss][Pp][Aa][Tt][Ii][Aa][Ll];
INCLUDE: [Ii][Nn][Cc][Ll][Uu][Dd][Ee];
NULLS: [Nn][Uu][Ll][Ll][Ss];
FIRST: [Ff][Ii][Rr][Ss][Tt];
LAST: [Ll][Aa][Ss][Tt];
AND: [Aa][Nn][Dd];
OR: [Oo][Rr];
NOT: [Nn][Oo][Tt];
NULL: [Nn][Uu][Ll][Ll];
IS: [Ii][Ss];
IN: [Ii][Nn];
OUT: [Oo][Uu][Tt];
INOUT: [Ii][Nn][Oo][Uu][Tt];
LIKE: [Ll][Ii][Kk][Ee];
BETWEEN: [Bb][Ee][Tt][Ww][Ee][Ee][Nn];
AS: [Aa][Ss];
ORDER: [Oo][Rr][Dd][Ee][Rr];
BY: [Bb][Yy];
GROUP: [Gg][Rr][Oo][Uu][Pp];
HAVING: [Hh][Aa][Vv][Ii][Nn][Gg];
LIMIT: [Ll][Ii][Mm][Ii][Tt];
OFFSET: [Oo][Ff][Ff][Ss][Ee][Tt];
DISTINCT: [Dd][Ii][Ss][Tt][Ii][Nn][Cc][Tt];
ALL: [Aa][Ll][Ll];
ASC: [Aa][Ss][Cc];
DESC: [Dd][Ee][Ss][Cc];
INNER: [Ii][Nn][Nn][Ee][Rr];
LEFT: [Ll][Ee][Ff][Tt];
RIGHT: [Rr][Ii][Gg][Hh][Tt];
FULL: [Ff][Uu][Ll][Ll];
CROSS: [Cc][Rr][Oo][Ss][Ss];
OUTER: [Oo][Uu][Tt][Ee][Rr];
JOIN: [Jj][Oo][Ii][Nn];
ON: [Oo][Nn];
USING: [Uu][Ss][Ii][Nn][Gg];
NATURAL: [Nn][Aa][Tt][Uu][Rr][Aa][Ll];
UNION: [Uu][Nn][Ii][Oo][Nn];
INTERSECT: [Ii][Nn][Tt][Ee][Rr][Ss][Ee][Cc][Tt];
EXCEPT: [Ee][Xx][Cc][Ee][Pp][Tt];
CASE: [Cc][Aa][Ss][Ee];
WHEN: [Ww][Hh][Ee][Nn];
THEN: [Tt][Hh][Ee][Nn];
ELSE: [Ee][Ll][Ss][Ee];
END: [Ee][Nn][Dd];
EXISTS: [Ee][Xx][Ii][Ss][Tt][Ss];
TRUE: [Tt][Rr][Uu][Ee];
FALSE: [Ff][Aa][Ll][Ss][Ee];
PRIMARY: [Pp][Rr][Ii][Mm][Aa][Rr][Yy];
KEY: [Kk][Ee][Yy];
UNIQUE: [Uu][Nn][Ii][Qq][Uu][Ee];
SEQUENCE: [Ss][Ee][Qq][Uu][Ee][Nn][Cc][Ee];
START: [Ss][Tt][Aa][Rr][Tt];
WITH: [Ww][Ii][Tt][Hh];
RECURSIVE: [Rr][Ee][Cc][Uu][Rr][Ss][Ii][Vv][Ee];
INCREMENT: [Ii][Nn][Cc][Rr][Ee][Mm][Ee][Nn][Tt];
MINVALUE: [Mm][Ii][Nn][Vv][Aa][Ll][Uu][Ee];
MAXVALUE: [Mm][Aa][Xx][Vv][Aa][Ll][Uu][Ee];
TRUNCATE: [Tt][Rr][Uu][Nn][Cc][Aa][Tt][Ee];
RESTART: [Rr][Ee][Ss][Tt][Aa][Rr][Tt];
CONTINUE: [Cc][Oo][Nn][Tt][Ii][Nn][Uu][Ee];
IDENTITY: [Ii][Dd][Ee][Nn][Tt][Ii][Tt][Yy];
AUTO_INCREMENT: [Aa][Uu][Tt][Oo]'_'[Ii][Nn][Cc][Rr][Ee][Mm][Ee][Nn][Tt];
NOMINVALUE: [Nn][Oo][Mm][Ii][Nn][Vv][Aa][Ll][Uu][Ee];
NOMAXVALUE: [Nn][Oo][Mm][Aa][Xx][Vv][Aa][Ll][Uu][Ee];
CYCLE: [Cc][Yy][Cc][Ll][Ee];
NOCYCLE: [Nn][Oo][Cc][Yy][Cc][Ll][Ee];
CACHE: [Cc][Aa][Cc][Hh][Ee];
NOCACHE: [Nn][Oo][Cc][Aa][Cc][Hh][Ee];
VALUE: [Vv][Aa][Ll][Uu][Ee];
FOR: [Ff][Oo][Rr];
CURRENT: [Cc][Uu][Rr][Rr][Ee][Nn][Tt];
NEXT: [Nn][Ee][Xx][Tt];
VIEW: [Vv][Ii][Ee][Ww];
MATERIALIZED: [Mm][Aa][Tt][Ee][Rr][Ii][Aa][Ll][Ii][Zz][Ee][Dd];
REFRESH: [Rr][Ee][Ff][Rr][Ee][Ss][Hh];
REPLACE: [Rr][Ee][Pp][Ll][Aa][Cc][Ee];
FORCE: [Ff][Oo][Rr][Cc][Ee];
RESTRICT: [Rr][Ee][Ss][Tt][Rr][Ii][Cc][Tt];
CASCADE: [Cc][Aa][Ss][Cc][Aa][Dd][Ee];
SCHEMA: [Ss][Cc][Hh][Ee][Mm][Aa];
EXPLAIN: [Ee][Xx][Pp][Ll][Aa][Ii][Nn];
TRIGGER: [Tt][Rr][Ii][Gg][Gg][Ee][Rr];
INSTEAD: [Ii][Nn][Ss][Tt][Ee][Aa][Dd];
OF: [Oo][Ff];
EACH: [Ee][Aa][Cc][Hh];
ROW: [Rr][Oo][Ww];
STATEMENT: [Ss][Tt][Aa][Tt][Ee][Mm][Ee][Nn][Tt];
QUEUE: [Qq][Uu][Ee][Uu][Ee];
NOWAIT: [Nn][Oo][Ww][Aa][Ii][Tt];

// Data types
SMALLINT: [Ss][Mm][Aa][Ll][Ll][Ii][Nn][Tt];
INTEGER: [Ii][Nn][Tt][Ee][Gg][Ee][Rr];
INT: [Ii][Nn][Tt];
BIGINT: [Bb][Ii][Gg][Ii][Nn][Tt];
DECIMAL: [Dd][Ee][Cc][Ii][Mm][Aa][Ll];
NUMERIC: [Nn][Uu][Mm][Ee][Rr][Ii][Cc];
REAL: [Rr][Ee][Aa][Ll];
DOUBLE: [Dd][Oo][Uu][Bb][Ll][Ee];
PRECISION: [Pp][Rr][Ee][Cc][Ii][Ss][Ii][Oo][Nn];
VARCHAR: [Vv][Aa][Rr][Cc][Hh][Aa][Rr];
CHAR: [Cc][Hh][Aa][Rr];
TEXT: [Tt][Ee][Xx][Tt];
BOOLEAN: [Bb][Oo][Oo][Ll][Ee][Aa][Nn];
DATE: [Dd][Aa][Tt][Ee];
TIME: [Tt][Ii][Mm][Ee];
TIMESTAMP: [Tt][Ii][Mm][Ee][Ss][Tt][Aa][Mm][Pp];
TIMESTAMPTZ: [Tt][Ii][Mm][Ee][Ss][Tt][Aa][Mm][Pp][Tt][Zz];
UUID: [Uu][Uu][Ii][Dd];
JSONB: [Jj][Ss][Oo][Nn][Bb];
BYTEA: [Bb][Yy][Tt][Ee][Aa];
CLOB: [Cc][Ll][Oo][Bb];
BLOB: [Bb][Ll][Oo][Bb];
CHARACTER: [Cc][Hh][Aa][Rr][Aa][Cc][Tt][Ee][Rr];
LARGE: [Ll][Aa][Rr][Gg][Ee];
OBJECT: [Oo][Bb][Jj][Ee][Cc][Tt];
BINARY: [Bb][Ii][Nn][Aa][Rr][Yy];
VARBINARY: [Vv][Aa][Rr][Bb][Ii][Nn][Aa][Rr][Yy];
VARYING: [Vv][Aa][Rr][Yy][Ii][Nn][Gg];
INTERVAL: [Ii][Nn][Tt][Ee][Rr][Vv][Aa][Ll];

// UUID generation functions
GEN_RANDOM_UUID: [Gg][Ee][Nn]'_'[Rr][Aa][Nn][Dd][Oo][Mm]'_'[Uu][Uu][Ii][Dd];
UUID_GENERATE_V1: [Uu][Uu][Ii][Dd]'_'[Gg][Ee][Nn][Ee][Rr][Aa][Tt][Ee]'_'[Vv][1];
UUID_GENERATE_V4: [Uu][Uu][Ii][Dd]'_'[Gg][Ee][Nn][Ee][Rr][Aa][Tt][Ee]'_'[Vv][4];

// Aggregate functions
COUNT: [Cc][Oo][Uu][Nn][Tt];
SUM: [Ss][Uu][Mm];
AVG: [Aa][Vv][Gg];
MIN: [Mm][Ii][Nn];
MAX: [Mm][Aa][Xx];

// System functions
DATABASE: [Dd][Aa][Tt][Aa][Bb][Aa][Ss][Ee];
USER: [Uu][Ss][Ee][Rr];
CURRENT_USER: [Cc][Uu][Rr][Rr][Ee][Nn][Tt]'_'[Uu][Ss][Ee][Rr];
SESSION_USER: [Ss][Ee][Ss][Ss][Ii][Oo][Nn]'_'[Uu][Ss][Ee][Rr];
SESSION_ID: [Ss][Ee][Ss][Ss][Ii][Oo][Nn]'_'[Ii][Dd];

// Math functions
SQRT: [Ss][Qq][Rr][Tt];
POWER: [Pp][Oo][Ww][Ee][Rr];
ABS: [Aa][Bb][Ss];
ROUND: [Rr][Oo][Uu][Nn][Dd];
RAND: [Rr][Aa][Nn][Dd];
// H2 Date/Time functions
CURRENT_TIMESTAMP: [Cc][Uu][Rr][Rr][Ee][Nn][Tt]'_'[Tt][Ii][Mm][Ee][Ss][Tt][Aa][Mm][Pp];
CURRENT_DATE: [Cc][Uu][Rr][Rr][Ee][Nn][Tt]'_'[Dd][Aa][Tt][Ee];
CURRENT_TIME: [Cc][Uu][Rr][Rr][Ee][Nn][Tt]'_'[Tt][Ii][Mm][Ee];
DATEADD: [Dd][Aa][Tt][Ee][Aa][Dd][Dd];
DATEDIFF: [Dd][Aa][Tt][Ee][Dd][Ii][Ff][Ff];
FORMATDATETIME: [Ff][Oo][Rr][Mm][Aa][Tt][Dd][Aa][Tt][Ee][Tt][Ii][Mm][Ee];
PARSEDATETIME: [Pp][Aa][Rr][Ss][Ee][Dd][Aa][Tt][Ee][Tt][Ii][Mm][Ee];
// H2 System functions
H2VERSION: [Hh][2][Vv][Ee][Rr][Ss][Ii][Oo][Nn];
DATABASE_PATH: [Dd][Aa][Tt][Aa][Bb][Aa][Ss][Ee]'_'[Pp][Aa][Tt][Hh];
MEMORY_USED: [Mm][Ee][Mm][Oo][Rr][Yy]'_'[Uu][Ss][Ee][Dd];
MEMORY_FREE: [Mm][Ee][Mm][Oo][Rr][Yy]'_'[Ff][Rr][Ee][Ee];
// H2 String utility functions (non-conflicting)
POSITION: [Pp][Oo][Ss][Ii][Tt][Ii][Oo][Nn];
ASCII: [Aa][Ss][Cc][Ii][Ii];
HEXTORAW: [Hh][Ee][Xx][Tt][Oo][Rr][Aa][Ww];
RAWTOHEX: [Rr][Aa][Ww][Tt][Oo][Hh][Ee][Xx];

// Window functions
ROW_NUMBER: [Rr][Oo][Ww]'_'[Nn][Uu][Mm][Bb][Ee][Rr];
RANK: [Rr][Aa][Nn][Kk];
DENSE_RANK: [Dd][Ee][Nn][Ss][Ee]'_'[Rr][Aa][Nn][Kk];
PERCENT_RANK: [Pp][Ee][Rr][Cc][Ee][Nn][Tt]'_'[Rr][Aa][Nn][Kk];
CUME_DIST: [Cc][Uu][Mm][Ee]'_'[Dd][Ii][Ss][Tt];
OVER: [Oo][Vv][Ee][Rr];
PARTITION: [Pp][Aa][Rr][Tt][Ii][Tt][Ii][Oo][Nn];
WINDOW: [Ww][Ii][Nn][Dd][Oo][Ww];

// Operators
EQ: '=';
NE: '!=' | '<>';
LT: '<';
LE: '<=';
GT: '>';
GE: '>=';
PLUS: '+';
MINUS: '-';
MULTIPLY: '*';
DIVIDE: '/';
MODULO: '%';
EXPONENT: '^';
CONCAT: '||';
JSONB_CONTAINS: '@>';
JSONB_CONTAINED: '<@';
JSONB_EXISTS: '?';
JSONB_EXTRACT: '->';
JSONB_EXTRACT_TEXT: '->>';
JSONB_PATH_EXTRACT: '#>';
JSONB_PATH_EXTRACT_TEXT: '#>>';

// Delimiters
LPAREN: '(';
RPAREN: ')';
LBRACKET: '[';
RBRACKET: ']';
LBRACE: '{';
RBRACE: '}';
COMMA: ',';
SEMICOLON: ';';
DOT: '.';
COLON: ':';

// Literals
STRING: '\'' ( ESC | ~'\'' )* '\'';
IDENTIFIER: [a-zA-Z_][a-zA-Z0-9_]*;
QUOTED_IDENTIFIER: '"' ( ~'"' | '""' )* '"';
INTEGER_LITERAL: [0-9]+;
DECIMAL_LITERAL: [0-9]+ '.' [0-9]* | '.' [0-9]+;
SCIENTIFIC_LITERAL: ([0-9]+ ('.' [0-9]*)? | '.' [0-9]+) [eE] [+-]? [0-9]+;

// Escape sequences
fragment ESC: '\\' [\\'];

// Whitespace and comments
WS: [ \t\r\n]+ -> skip;
LINE_COMMENT: '--' ~[\r\n]* -> skip;
BLOCK_COMMENT: '/*' .*? '*/' -> skip;