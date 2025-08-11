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
CREATE: [Cc][Rr][Ee][Aa][Tt][Ee];
TABLE: [Tt][Aa][Bb][Ll][Ee];
DROP: [Dd][Rr][Oo][Pp];
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
POWER: '^';
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