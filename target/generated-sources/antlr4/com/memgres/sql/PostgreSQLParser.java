// Generated from com/memgres/sql/PostgreSQLParser.g4 by ANTLR 4.13.1
package com.memgres.sql;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class PostgreSQLParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		SELECT=1, FROM=2, WHERE=3, INSERT=4, INTO=5, VALUES=6, UPDATE=7, SET=8, 
		DELETE=9, CREATE=10, TABLE=11, DROP=12, AND=13, OR=14, NOT=15, NULL=16, 
		IS=17, IN=18, LIKE=19, BETWEEN=20, AS=21, ORDER=22, BY=23, GROUP=24, HAVING=25, 
		LIMIT=26, OFFSET=27, DISTINCT=28, ALL=29, ASC=30, DESC=31, INNER=32, LEFT=33, 
		RIGHT=34, FULL=35, CROSS=36, OUTER=37, JOIN=38, ON=39, USING=40, UNION=41, 
		INTERSECT=42, EXCEPT=43, CASE=44, WHEN=45, THEN=46, ELSE=47, END=48, EXISTS=49, 
		TRUE=50, FALSE=51, PRIMARY=52, KEY=53, UNIQUE=54, SMALLINT=55, INTEGER=56, 
		INT=57, BIGINT=58, DECIMAL=59, NUMERIC=60, REAL=61, DOUBLE=62, PRECISION=63, 
		VARCHAR=64, CHAR=65, TEXT=66, BOOLEAN=67, DATE=68, TIME=69, TIMESTAMP=70, 
		TIMESTAMPTZ=71, UUID=72, JSONB=73, BYTEA=74, GEN_RANDOM_UUID=75, UUID_GENERATE_V1=76, 
		UUID_GENERATE_V4=77, COUNT=78, SUM=79, AVG=80, MIN=81, MAX=82, EQ=83, 
		NE=84, LT=85, LE=86, GT=87, GE=88, PLUS=89, MINUS=90, MULTIPLY=91, DIVIDE=92, 
		MODULO=93, POWER=94, CONCAT=95, JSONB_CONTAINS=96, JSONB_CONTAINED=97, 
		JSONB_EXISTS=98, JSONB_EXTRACT=99, JSONB_EXTRACT_TEXT=100, JSONB_PATH_EXTRACT=101, 
		JSONB_PATH_EXTRACT_TEXT=102, LPAREN=103, RPAREN=104, LBRACKET=105, RBRACKET=106, 
		LBRACE=107, RBRACE=108, COMMA=109, SEMICOLON=110, DOT=111, COLON=112, 
		STRING=113, IDENTIFIER=114, QUOTED_IDENTIFIER=115, INTEGER_LITERAL=116, 
		DECIMAL_LITERAL=117, SCIENTIFIC_LITERAL=118, WS=119, LINE_COMMENT=120, 
		BLOCK_COMMENT=121;
	public static final int
		RULE_sql = 0, RULE_statement = 1, RULE_selectStatement = 2, RULE_selectModifier = 3, 
		RULE_selectList = 4, RULE_selectItem = 5, RULE_fromClause = 6, RULE_joinableTable = 7, 
		RULE_tableReference = 8, RULE_joinClause = 9, RULE_joinType = 10, RULE_joinCondition = 11, 
		RULE_whereClause = 12, RULE_groupByClause = 13, RULE_havingClause = 14, 
		RULE_orderByClause = 15, RULE_orderItem = 16, RULE_limitClause = 17, RULE_insertStatement = 18, 
		RULE_valuesClause = 19, RULE_updateStatement = 20, RULE_updateItem = 21, 
		RULE_deleteStatement = 22, RULE_createTableStatement = 23, RULE_columnDefinition = 24, 
		RULE_columnConstraint = 25, RULE_dropTableStatement = 26, RULE_expression = 27, 
		RULE_whenClause = 28, RULE_binaryOperator = 29, RULE_literal = 30, RULE_columnReference = 31, 
		RULE_tableName = 32, RULE_columnName = 33, RULE_alias = 34, RULE_functionCall = 35, 
		RULE_expressionList = 36, RULE_columnList = 37, RULE_dataType = 38, RULE_identifier = 39;
	private static String[] makeRuleNames() {
		return new String[] {
			"sql", "statement", "selectStatement", "selectModifier", "selectList", 
			"selectItem", "fromClause", "joinableTable", "tableReference", "joinClause", 
			"joinType", "joinCondition", "whereClause", "groupByClause", "havingClause", 
			"orderByClause", "orderItem", "limitClause", "insertStatement", "valuesClause", 
			"updateStatement", "updateItem", "deleteStatement", "createTableStatement", 
			"columnDefinition", "columnConstraint", "dropTableStatement", "expression", 
			"whenClause", "binaryOperator", "literal", "columnReference", "tableName", 
			"columnName", "alias", "functionCall", "expressionList", "columnList", 
			"dataType", "identifier"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, "'='", 
			null, "'<'", "'<='", "'>'", "'>='", "'+'", "'-'", "'*'", "'/'", "'%'", 
			"'^'", "'||'", "'@>'", "'<@'", "'?'", "'->'", "'->>'", "'#>'", "'#>>'", 
			"'('", "')'", "'['", "']'", "'{'", "'}'", "','", "';'", "'.'", "':'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "SELECT", "FROM", "WHERE", "INSERT", "INTO", "VALUES", "UPDATE", 
			"SET", "DELETE", "CREATE", "TABLE", "DROP", "AND", "OR", "NOT", "NULL", 
			"IS", "IN", "LIKE", "BETWEEN", "AS", "ORDER", "BY", "GROUP", "HAVING", 
			"LIMIT", "OFFSET", "DISTINCT", "ALL", "ASC", "DESC", "INNER", "LEFT", 
			"RIGHT", "FULL", "CROSS", "OUTER", "JOIN", "ON", "USING", "UNION", "INTERSECT", 
			"EXCEPT", "CASE", "WHEN", "THEN", "ELSE", "END", "EXISTS", "TRUE", "FALSE", 
			"PRIMARY", "KEY", "UNIQUE", "SMALLINT", "INTEGER", "INT", "BIGINT", "DECIMAL", 
			"NUMERIC", "REAL", "DOUBLE", "PRECISION", "VARCHAR", "CHAR", "TEXT", 
			"BOOLEAN", "DATE", "TIME", "TIMESTAMP", "TIMESTAMPTZ", "UUID", "JSONB", 
			"BYTEA", "GEN_RANDOM_UUID", "UUID_GENERATE_V1", "UUID_GENERATE_V4", "COUNT", 
			"SUM", "AVG", "MIN", "MAX", "EQ", "NE", "LT", "LE", "GT", "GE", "PLUS", 
			"MINUS", "MULTIPLY", "DIVIDE", "MODULO", "POWER", "CONCAT", "JSONB_CONTAINS", 
			"JSONB_CONTAINED", "JSONB_EXISTS", "JSONB_EXTRACT", "JSONB_EXTRACT_TEXT", 
			"JSONB_PATH_EXTRACT", "JSONB_PATH_EXTRACT_TEXT", "LPAREN", "RPAREN", 
			"LBRACKET", "RBRACKET", "LBRACE", "RBRACE", "COMMA", "SEMICOLON", "DOT", 
			"COLON", "STRING", "IDENTIFIER", "QUOTED_IDENTIFIER", "INTEGER_LITERAL", 
			"DECIMAL_LITERAL", "SCIENTIFIC_LITERAL", "WS", "LINE_COMMENT", "BLOCK_COMMENT"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "PostgreSQLParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public PostgreSQLParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SqlContext extends ParserRuleContext {
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public TerminalNode EOF() { return getToken(PostgreSQLParser.EOF, 0); }
		public List<TerminalNode> SEMICOLON() { return getTokens(PostgreSQLParser.SEMICOLON); }
		public TerminalNode SEMICOLON(int i) {
			return getToken(PostgreSQLParser.SEMICOLON, i);
		}
		public SqlContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sql; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterSql(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitSql(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitSql(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SqlContext sql() throws RecognitionException {
		SqlContext _localctx = new SqlContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_sql);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(80);
			statement();
			setState(85);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(81);
					match(SEMICOLON);
					setState(82);
					statement();
					}
					} 
				}
				setState(87);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			}
			setState(89);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMICOLON) {
				{
				setState(88);
				match(SEMICOLON);
				}
			}

			setState(91);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StatementContext extends ParserRuleContext {
		public SelectStatementContext selectStatement() {
			return getRuleContext(SelectStatementContext.class,0);
		}
		public InsertStatementContext insertStatement() {
			return getRuleContext(InsertStatementContext.class,0);
		}
		public UpdateStatementContext updateStatement() {
			return getRuleContext(UpdateStatementContext.class,0);
		}
		public DeleteStatementContext deleteStatement() {
			return getRuleContext(DeleteStatementContext.class,0);
		}
		public CreateTableStatementContext createTableStatement() {
			return getRuleContext(CreateTableStatementContext.class,0);
		}
		public DropTableStatementContext dropTableStatement() {
			return getRuleContext(DropTableStatementContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_statement);
		try {
			setState(99);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SELECT:
				enterOuterAlt(_localctx, 1);
				{
				setState(93);
				selectStatement();
				}
				break;
			case INSERT:
				enterOuterAlt(_localctx, 2);
				{
				setState(94);
				insertStatement();
				}
				break;
			case UPDATE:
				enterOuterAlt(_localctx, 3);
				{
				setState(95);
				updateStatement();
				}
				break;
			case DELETE:
				enterOuterAlt(_localctx, 4);
				{
				setState(96);
				deleteStatement();
				}
				break;
			case CREATE:
				enterOuterAlt(_localctx, 5);
				{
				setState(97);
				createTableStatement();
				}
				break;
			case DROP:
				enterOuterAlt(_localctx, 6);
				{
				setState(98);
				dropTableStatement();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SelectStatementContext extends ParserRuleContext {
		public TerminalNode SELECT() { return getToken(PostgreSQLParser.SELECT, 0); }
		public SelectListContext selectList() {
			return getRuleContext(SelectListContext.class,0);
		}
		public SelectModifierContext selectModifier() {
			return getRuleContext(SelectModifierContext.class,0);
		}
		public TerminalNode FROM() { return getToken(PostgreSQLParser.FROM, 0); }
		public FromClauseContext fromClause() {
			return getRuleContext(FromClauseContext.class,0);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public GroupByClauseContext groupByClause() {
			return getRuleContext(GroupByClauseContext.class,0);
		}
		public HavingClauseContext havingClause() {
			return getRuleContext(HavingClauseContext.class,0);
		}
		public OrderByClauseContext orderByClause() {
			return getRuleContext(OrderByClauseContext.class,0);
		}
		public LimitClauseContext limitClause() {
			return getRuleContext(LimitClauseContext.class,0);
		}
		public SelectStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterSelectStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitSelectStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitSelectStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectStatementContext selectStatement() throws RecognitionException {
		SelectStatementContext _localctx = new SelectStatementContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_selectStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(101);
			match(SELECT);
			setState(103);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DISTINCT || _la==ALL) {
				{
				setState(102);
				selectModifier();
				}
			}

			setState(105);
			selectList();
			setState(108);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FROM) {
				{
				setState(106);
				match(FROM);
				setState(107);
				fromClause();
				}
			}

			setState(111);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(110);
				whereClause();
				}
			}

			setState(114);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==GROUP) {
				{
				setState(113);
				groupByClause();
				}
			}

			setState(117);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==HAVING) {
				{
				setState(116);
				havingClause();
				}
			}

			setState(120);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ORDER) {
				{
				setState(119);
				orderByClause();
				}
			}

			setState(123);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LIMIT) {
				{
				setState(122);
				limitClause();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SelectModifierContext extends ParserRuleContext {
		public TerminalNode DISTINCT() { return getToken(PostgreSQLParser.DISTINCT, 0); }
		public TerminalNode ALL() { return getToken(PostgreSQLParser.ALL, 0); }
		public SelectModifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectModifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterSelectModifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitSelectModifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitSelectModifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectModifierContext selectModifier() throws RecognitionException {
		SelectModifierContext _localctx = new SelectModifierContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_selectModifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(125);
			_la = _input.LA(1);
			if ( !(_la==DISTINCT || _la==ALL) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SelectListContext extends ParserRuleContext {
		public List<SelectItemContext> selectItem() {
			return getRuleContexts(SelectItemContext.class);
		}
		public SelectItemContext selectItem(int i) {
			return getRuleContext(SelectItemContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(PostgreSQLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(PostgreSQLParser.COMMA, i);
		}
		public TerminalNode MULTIPLY() { return getToken(PostgreSQLParser.MULTIPLY, 0); }
		public SelectListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterSelectList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitSelectList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitSelectList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectListContext selectList() throws RecognitionException {
		SelectListContext _localctx = new SelectListContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_selectList);
		int _la;
		try {
			setState(136);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT:
			case NULL:
			case CASE:
			case EXISTS:
			case TRUE:
			case FALSE:
			case GEN_RANDOM_UUID:
			case UUID_GENERATE_V1:
			case UUID_GENERATE_V4:
			case COUNT:
			case SUM:
			case AVG:
			case MIN:
			case MAX:
			case LPAREN:
			case STRING:
			case IDENTIFIER:
			case QUOTED_IDENTIFIER:
			case INTEGER_LITERAL:
			case DECIMAL_LITERAL:
			case SCIENTIFIC_LITERAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(127);
				selectItem();
				setState(132);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(128);
					match(COMMA);
					setState(129);
					selectItem();
					}
					}
					setState(134);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case MULTIPLY:
				enterOuterAlt(_localctx, 2);
				{
				setState(135);
				match(MULTIPLY);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SelectItemContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public AliasContext alias() {
			return getRuleContext(AliasContext.class,0);
		}
		public TerminalNode AS() { return getToken(PostgreSQLParser.AS, 0); }
		public SelectItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterSelectItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitSelectItem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitSelectItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectItemContext selectItem() throws RecognitionException {
		SelectItemContext _localctx = new SelectItemContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_selectItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(138);
			expression(0);
			setState(143);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AS || _la==IDENTIFIER || _la==QUOTED_IDENTIFIER) {
				{
				setState(140);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS) {
					{
					setState(139);
					match(AS);
					}
				}

				setState(142);
				alias();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FromClauseContext extends ParserRuleContext {
		public List<JoinableTableContext> joinableTable() {
			return getRuleContexts(JoinableTableContext.class);
		}
		public JoinableTableContext joinableTable(int i) {
			return getRuleContext(JoinableTableContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(PostgreSQLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(PostgreSQLParser.COMMA, i);
		}
		public FromClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fromClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterFromClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitFromClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitFromClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FromClauseContext fromClause() throws RecognitionException {
		FromClauseContext _localctx = new FromClauseContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_fromClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(145);
			joinableTable();
			setState(150);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(146);
				match(COMMA);
				setState(147);
				joinableTable();
				}
				}
				setState(152);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class JoinableTableContext extends ParserRuleContext {
		public TableReferenceContext tableReference() {
			return getRuleContext(TableReferenceContext.class,0);
		}
		public List<JoinClauseContext> joinClause() {
			return getRuleContexts(JoinClauseContext.class);
		}
		public JoinClauseContext joinClause(int i) {
			return getRuleContext(JoinClauseContext.class,i);
		}
		public JoinableTableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinableTable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterJoinableTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitJoinableTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitJoinableTable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JoinableTableContext joinableTable() throws RecognitionException {
		JoinableTableContext _localctx = new JoinableTableContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_joinableTable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(153);
			tableReference();
			setState(157);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 408021893120L) != 0)) {
				{
				{
				setState(154);
				joinClause();
				}
				}
				setState(159);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TableReferenceContext extends ParserRuleContext {
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public AliasContext alias() {
			return getRuleContext(AliasContext.class,0);
		}
		public TerminalNode AS() { return getToken(PostgreSQLParser.AS, 0); }
		public TerminalNode LPAREN() { return getToken(PostgreSQLParser.LPAREN, 0); }
		public SelectStatementContext selectStatement() {
			return getRuleContext(SelectStatementContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(PostgreSQLParser.RPAREN, 0); }
		public TableReferenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableReference; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterTableReference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitTableReference(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitTableReference(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableReferenceContext tableReference() throws RecognitionException {
		TableReferenceContext _localctx = new TableReferenceContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_tableReference);
		int _la;
		try {
			setState(176);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
			case QUOTED_IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(160);
				tableName();
				setState(165);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS || _la==IDENTIFIER || _la==QUOTED_IDENTIFIER) {
					{
					setState(162);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==AS) {
						{
						setState(161);
						match(AS);
						}
					}

					setState(164);
					alias();
					}
				}

				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 2);
				{
				setState(167);
				match(LPAREN);
				setState(168);
				selectStatement();
				setState(169);
				match(RPAREN);
				setState(174);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS || _la==IDENTIFIER || _la==QUOTED_IDENTIFIER) {
					{
					setState(171);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==AS) {
						{
						setState(170);
						match(AS);
						}
					}

					setState(173);
					alias();
					}
				}

				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class JoinClauseContext extends ParserRuleContext {
		public JoinTypeContext joinType() {
			return getRuleContext(JoinTypeContext.class,0);
		}
		public TerminalNode JOIN() { return getToken(PostgreSQLParser.JOIN, 0); }
		public TableReferenceContext tableReference() {
			return getRuleContext(TableReferenceContext.class,0);
		}
		public JoinConditionContext joinCondition() {
			return getRuleContext(JoinConditionContext.class,0);
		}
		public JoinClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterJoinClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitJoinClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitJoinClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JoinClauseContext joinClause() throws RecognitionException {
		JoinClauseContext _localctx = new JoinClauseContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_joinClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(178);
			joinType();
			setState(179);
			match(JOIN);
			setState(180);
			tableReference();
			setState(181);
			joinCondition();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class JoinTypeContext extends ParserRuleContext {
		public JoinTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinType; }
	 
		public JoinTypeContext() { }
		public void copyFrom(JoinTypeContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class RightJoinContext extends JoinTypeContext {
		public TerminalNode RIGHT() { return getToken(PostgreSQLParser.RIGHT, 0); }
		public TerminalNode OUTER() { return getToken(PostgreSQLParser.OUTER, 0); }
		public RightJoinContext(JoinTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterRightJoin(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitRightJoin(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitRightJoin(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class InnerJoinContext extends JoinTypeContext {
		public TerminalNode INNER() { return getToken(PostgreSQLParser.INNER, 0); }
		public InnerJoinContext(JoinTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterInnerJoin(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitInnerJoin(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitInnerJoin(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LeftJoinContext extends JoinTypeContext {
		public TerminalNode LEFT() { return getToken(PostgreSQLParser.LEFT, 0); }
		public TerminalNode OUTER() { return getToken(PostgreSQLParser.OUTER, 0); }
		public LeftJoinContext(JoinTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterLeftJoin(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitLeftJoin(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitLeftJoin(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FullOuterJoinContext extends JoinTypeContext {
		public TerminalNode FULL() { return getToken(PostgreSQLParser.FULL, 0); }
		public TerminalNode OUTER() { return getToken(PostgreSQLParser.OUTER, 0); }
		public FullOuterJoinContext(JoinTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterFullOuterJoin(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitFullOuterJoin(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitFullOuterJoin(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CrossJoinContext extends JoinTypeContext {
		public TerminalNode CROSS() { return getToken(PostgreSQLParser.CROSS, 0); }
		public CrossJoinContext(JoinTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterCrossJoin(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitCrossJoin(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitCrossJoin(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JoinTypeContext joinType() throws RecognitionException {
		JoinTypeContext _localctx = new JoinTypeContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_joinType);
		int _la;
		try {
			setState(199);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INNER:
			case JOIN:
				_localctx = new InnerJoinContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(184);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==INNER) {
					{
					setState(183);
					match(INNER);
					}
				}

				}
				break;
			case LEFT:
				_localctx = new LeftJoinContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(186);
				match(LEFT);
				setState(188);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OUTER) {
					{
					setState(187);
					match(OUTER);
					}
				}

				}
				break;
			case RIGHT:
				_localctx = new RightJoinContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(190);
				match(RIGHT);
				setState(192);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OUTER) {
					{
					setState(191);
					match(OUTER);
					}
				}

				}
				break;
			case FULL:
				_localctx = new FullOuterJoinContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(194);
				match(FULL);
				setState(196);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OUTER) {
					{
					setState(195);
					match(OUTER);
					}
				}

				}
				break;
			case CROSS:
				_localctx = new CrossJoinContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(198);
				match(CROSS);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class JoinConditionContext extends ParserRuleContext {
		public JoinConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinCondition; }
	 
		public JoinConditionContext() { }
		public void copyFrom(JoinConditionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NaturalJoinConditionContext extends JoinConditionContext {
		public NaturalJoinConditionContext(JoinConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterNaturalJoinCondition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitNaturalJoinCondition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitNaturalJoinCondition(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class UsingJoinConditionContext extends JoinConditionContext {
		public TerminalNode USING() { return getToken(PostgreSQLParser.USING, 0); }
		public TerminalNode LPAREN() { return getToken(PostgreSQLParser.LPAREN, 0); }
		public ColumnListContext columnList() {
			return getRuleContext(ColumnListContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(PostgreSQLParser.RPAREN, 0); }
		public UsingJoinConditionContext(JoinConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterUsingJoinCondition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitUsingJoinCondition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitUsingJoinCondition(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class OnJoinConditionContext extends JoinConditionContext {
		public TerminalNode ON() { return getToken(PostgreSQLParser.ON, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public OnJoinConditionContext(JoinConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterOnJoinCondition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitOnJoinCondition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitOnJoinCondition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JoinConditionContext joinCondition() throws RecognitionException {
		JoinConditionContext _localctx = new JoinConditionContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_joinCondition);
		try {
			setState(209);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ON:
				_localctx = new OnJoinConditionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(201);
				match(ON);
				setState(202);
				expression(0);
				}
				break;
			case USING:
				_localctx = new UsingJoinConditionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(203);
				match(USING);
				setState(204);
				match(LPAREN);
				setState(205);
				columnList();
				setState(206);
				match(RPAREN);
				}
				break;
			case EOF:
			case WHERE:
			case ORDER:
			case GROUP:
			case HAVING:
			case LIMIT:
			case INNER:
			case LEFT:
			case RIGHT:
			case FULL:
			case CROSS:
			case JOIN:
			case RPAREN:
			case COMMA:
			case SEMICOLON:
				_localctx = new NaturalJoinConditionContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class WhereClauseContext extends ParserRuleContext {
		public TerminalNode WHERE() { return getToken(PostgreSQLParser.WHERE, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public WhereClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whereClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterWhereClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitWhereClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitWhereClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhereClauseContext whereClause() throws RecognitionException {
		WhereClauseContext _localctx = new WhereClauseContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_whereClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(211);
			match(WHERE);
			setState(212);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class GroupByClauseContext extends ParserRuleContext {
		public TerminalNode GROUP() { return getToken(PostgreSQLParser.GROUP, 0); }
		public TerminalNode BY() { return getToken(PostgreSQLParser.BY, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(PostgreSQLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(PostgreSQLParser.COMMA, i);
		}
		public GroupByClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupByClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterGroupByClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitGroupByClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitGroupByClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GroupByClauseContext groupByClause() throws RecognitionException {
		GroupByClauseContext _localctx = new GroupByClauseContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_groupByClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(214);
			match(GROUP);
			setState(215);
			match(BY);
			setState(216);
			expression(0);
			setState(221);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(217);
				match(COMMA);
				setState(218);
				expression(0);
				}
				}
				setState(223);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class HavingClauseContext extends ParserRuleContext {
		public TerminalNode HAVING() { return getToken(PostgreSQLParser.HAVING, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public HavingClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_havingClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterHavingClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitHavingClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitHavingClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HavingClauseContext havingClause() throws RecognitionException {
		HavingClauseContext _localctx = new HavingClauseContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_havingClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(224);
			match(HAVING);
			setState(225);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OrderByClauseContext extends ParserRuleContext {
		public TerminalNode ORDER() { return getToken(PostgreSQLParser.ORDER, 0); }
		public TerminalNode BY() { return getToken(PostgreSQLParser.BY, 0); }
		public List<OrderItemContext> orderItem() {
			return getRuleContexts(OrderItemContext.class);
		}
		public OrderItemContext orderItem(int i) {
			return getRuleContext(OrderItemContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(PostgreSQLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(PostgreSQLParser.COMMA, i);
		}
		public OrderByClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderByClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterOrderByClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitOrderByClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitOrderByClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrderByClauseContext orderByClause() throws RecognitionException {
		OrderByClauseContext _localctx = new OrderByClauseContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_orderByClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(227);
			match(ORDER);
			setState(228);
			match(BY);
			setState(229);
			orderItem();
			setState(234);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(230);
				match(COMMA);
				setState(231);
				orderItem();
				}
				}
				setState(236);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OrderItemContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode ASC() { return getToken(PostgreSQLParser.ASC, 0); }
		public TerminalNode DESC() { return getToken(PostgreSQLParser.DESC, 0); }
		public OrderItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterOrderItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitOrderItem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitOrderItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrderItemContext orderItem() throws RecognitionException {
		OrderItemContext _localctx = new OrderItemContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_orderItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(237);
			expression(0);
			setState(239);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASC || _la==DESC) {
				{
				setState(238);
				_la = _input.LA(1);
				if ( !(_la==ASC || _la==DESC) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LimitClauseContext extends ParserRuleContext {
		public TerminalNode LIMIT() { return getToken(PostgreSQLParser.LIMIT, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode OFFSET() { return getToken(PostgreSQLParser.OFFSET, 0); }
		public LimitClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_limitClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterLimitClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitLimitClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitLimitClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LimitClauseContext limitClause() throws RecognitionException {
		LimitClauseContext _localctx = new LimitClauseContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_limitClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(241);
			match(LIMIT);
			setState(242);
			expression(0);
			setState(245);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==OFFSET) {
				{
				setState(243);
				match(OFFSET);
				setState(244);
				expression(0);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class InsertStatementContext extends ParserRuleContext {
		public TerminalNode INSERT() { return getToken(PostgreSQLParser.INSERT, 0); }
		public TerminalNode INTO() { return getToken(PostgreSQLParser.INTO, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public TerminalNode VALUES() { return getToken(PostgreSQLParser.VALUES, 0); }
		public List<ValuesClauseContext> valuesClause() {
			return getRuleContexts(ValuesClauseContext.class);
		}
		public ValuesClauseContext valuesClause(int i) {
			return getRuleContext(ValuesClauseContext.class,i);
		}
		public SelectStatementContext selectStatement() {
			return getRuleContext(SelectStatementContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(PostgreSQLParser.LPAREN, 0); }
		public ColumnListContext columnList() {
			return getRuleContext(ColumnListContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(PostgreSQLParser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(PostgreSQLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(PostgreSQLParser.COMMA, i);
		}
		public InsertStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_insertStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterInsertStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitInsertStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitInsertStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InsertStatementContext insertStatement() throws RecognitionException {
		InsertStatementContext _localctx = new InsertStatementContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_insertStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(247);
			match(INSERT);
			setState(248);
			match(INTO);
			setState(249);
			tableName();
			setState(254);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(250);
				match(LPAREN);
				setState(251);
				columnList();
				setState(252);
				match(RPAREN);
				}
			}

			setState(266);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case VALUES:
				{
				setState(256);
				match(VALUES);
				setState(257);
				valuesClause();
				setState(262);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(258);
					match(COMMA);
					setState(259);
					valuesClause();
					}
					}
					setState(264);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case SELECT:
				{
				setState(265);
				selectStatement();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ValuesClauseContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(PostgreSQLParser.LPAREN, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(PostgreSQLParser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(PostgreSQLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(PostgreSQLParser.COMMA, i);
		}
		public ValuesClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_valuesClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterValuesClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitValuesClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitValuesClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValuesClauseContext valuesClause() throws RecognitionException {
		ValuesClauseContext _localctx = new ValuesClauseContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_valuesClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(268);
			match(LPAREN);
			setState(269);
			expression(0);
			setState(274);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(270);
				match(COMMA);
				setState(271);
				expression(0);
				}
				}
				setState(276);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(277);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UpdateStatementContext extends ParserRuleContext {
		public TerminalNode UPDATE() { return getToken(PostgreSQLParser.UPDATE, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public TerminalNode SET() { return getToken(PostgreSQLParser.SET, 0); }
		public List<UpdateItemContext> updateItem() {
			return getRuleContexts(UpdateItemContext.class);
		}
		public UpdateItemContext updateItem(int i) {
			return getRuleContext(UpdateItemContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(PostgreSQLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(PostgreSQLParser.COMMA, i);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public UpdateStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_updateStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterUpdateStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitUpdateStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitUpdateStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UpdateStatementContext updateStatement() throws RecognitionException {
		UpdateStatementContext _localctx = new UpdateStatementContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_updateStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(279);
			match(UPDATE);
			setState(280);
			tableName();
			setState(281);
			match(SET);
			setState(282);
			updateItem();
			setState(287);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(283);
				match(COMMA);
				setState(284);
				updateItem();
				}
				}
				setState(289);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(291);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(290);
				whereClause();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UpdateItemContext extends ParserRuleContext {
		public ColumnNameContext columnName() {
			return getRuleContext(ColumnNameContext.class,0);
		}
		public TerminalNode EQ() { return getToken(PostgreSQLParser.EQ, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public UpdateItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_updateItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterUpdateItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitUpdateItem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitUpdateItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UpdateItemContext updateItem() throws RecognitionException {
		UpdateItemContext _localctx = new UpdateItemContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_updateItem);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(293);
			columnName();
			setState(294);
			match(EQ);
			setState(295);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DeleteStatementContext extends ParserRuleContext {
		public TerminalNode DELETE() { return getToken(PostgreSQLParser.DELETE, 0); }
		public TerminalNode FROM() { return getToken(PostgreSQLParser.FROM, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public DeleteStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_deleteStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterDeleteStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitDeleteStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitDeleteStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DeleteStatementContext deleteStatement() throws RecognitionException {
		DeleteStatementContext _localctx = new DeleteStatementContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_deleteStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(297);
			match(DELETE);
			setState(298);
			match(FROM);
			setState(299);
			tableName();
			setState(301);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(300);
				whereClause();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CreateTableStatementContext extends ParserRuleContext {
		public TerminalNode CREATE() { return getToken(PostgreSQLParser.CREATE, 0); }
		public TerminalNode TABLE() { return getToken(PostgreSQLParser.TABLE, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(PostgreSQLParser.LPAREN, 0); }
		public List<ColumnDefinitionContext> columnDefinition() {
			return getRuleContexts(ColumnDefinitionContext.class);
		}
		public ColumnDefinitionContext columnDefinition(int i) {
			return getRuleContext(ColumnDefinitionContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(PostgreSQLParser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(PostgreSQLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(PostgreSQLParser.COMMA, i);
		}
		public CreateTableStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createTableStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterCreateTableStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitCreateTableStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitCreateTableStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreateTableStatementContext createTableStatement() throws RecognitionException {
		CreateTableStatementContext _localctx = new CreateTableStatementContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_createTableStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(303);
			match(CREATE);
			setState(304);
			match(TABLE);
			setState(305);
			tableName();
			setState(306);
			match(LPAREN);
			setState(307);
			columnDefinition();
			setState(312);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(308);
				match(COMMA);
				setState(309);
				columnDefinition();
				}
				}
				setState(314);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(315);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ColumnDefinitionContext extends ParserRuleContext {
		public ColumnNameContext columnName() {
			return getRuleContext(ColumnNameContext.class,0);
		}
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
		public List<ColumnConstraintContext> columnConstraint() {
			return getRuleContexts(ColumnConstraintContext.class);
		}
		public ColumnConstraintContext columnConstraint(int i) {
			return getRuleContext(ColumnConstraintContext.class,i);
		}
		public ColumnDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterColumnDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitColumnDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitColumnDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnDefinitionContext columnDefinition() throws RecognitionException {
		ColumnDefinitionContext _localctx = new ColumnDefinitionContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_columnDefinition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(317);
			columnName();
			setState(318);
			dataType();
			setState(322);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 22517998136950784L) != 0)) {
				{
				{
				setState(319);
				columnConstraint();
				}
				}
				setState(324);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ColumnConstraintContext extends ParserRuleContext {
		public TerminalNode NOT() { return getToken(PostgreSQLParser.NOT, 0); }
		public TerminalNode NULL() { return getToken(PostgreSQLParser.NULL, 0); }
		public TerminalNode PRIMARY() { return getToken(PostgreSQLParser.PRIMARY, 0); }
		public TerminalNode KEY() { return getToken(PostgreSQLParser.KEY, 0); }
		public TerminalNode UNIQUE() { return getToken(PostgreSQLParser.UNIQUE, 0); }
		public ColumnConstraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnConstraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterColumnConstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitColumnConstraint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitColumnConstraint(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnConstraintContext columnConstraint() throws RecognitionException {
		ColumnConstraintContext _localctx = new ColumnConstraintContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_columnConstraint);
		try {
			setState(331);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT:
				enterOuterAlt(_localctx, 1);
				{
				setState(325);
				match(NOT);
				setState(326);
				match(NULL);
				}
				break;
			case NULL:
				enterOuterAlt(_localctx, 2);
				{
				setState(327);
				match(NULL);
				}
				break;
			case PRIMARY:
				enterOuterAlt(_localctx, 3);
				{
				setState(328);
				match(PRIMARY);
				setState(329);
				match(KEY);
				}
				break;
			case UNIQUE:
				enterOuterAlt(_localctx, 4);
				{
				setState(330);
				match(UNIQUE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DropTableStatementContext extends ParserRuleContext {
		public TerminalNode DROP() { return getToken(PostgreSQLParser.DROP, 0); }
		public TerminalNode TABLE() { return getToken(PostgreSQLParser.TABLE, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public DropTableStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dropTableStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterDropTableStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitDropTableStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitDropTableStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DropTableStatementContext dropTableStatement() throws RecognitionException {
		DropTableStatementContext _localctx = new DropTableStatementContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_dropTableStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(333);
			match(DROP);
			setState(334);
			match(TABLE);
			setState(335);
			tableName();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionContext extends ParserRuleContext {
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
	 
		public ExpressionContext() { }
		public void copyFrom(ExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class InSubqueryExpressionContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode IN() { return getToken(PostgreSQLParser.IN, 0); }
		public TerminalNode LPAREN() { return getToken(PostgreSQLParser.LPAREN, 0); }
		public SelectStatementContext selectStatement() {
			return getRuleContext(SelectStatementContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(PostgreSQLParser.RPAREN, 0); }
		public TerminalNode NOT() { return getToken(PostgreSQLParser.NOT, 0); }
		public InSubqueryExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterInSubqueryExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitInSubqueryExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitInSubqueryExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ColumnReferenceExpressionContext extends ExpressionContext {
		public ColumnReferenceContext columnReference() {
			return getRuleContext(ColumnReferenceContext.class,0);
		}
		public ColumnReferenceExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterColumnReferenceExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitColumnReferenceExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitColumnReferenceExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NotExpressionContext extends ExpressionContext {
		public TerminalNode NOT() { return getToken(PostgreSQLParser.NOT, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public NotExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterNotExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitNotExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitNotExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SubqueryExpressionContext extends ExpressionContext {
		public TerminalNode LPAREN() { return getToken(PostgreSQLParser.LPAREN, 0); }
		public SelectStatementContext selectStatement() {
			return getRuleContext(SelectStatementContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(PostgreSQLParser.RPAREN, 0); }
		public SubqueryExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterSubqueryExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitSubqueryExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitSubqueryExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BinaryExpressionContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public BinaryOperatorContext binaryOperator() {
			return getRuleContext(BinaryOperatorContext.class,0);
		}
		public BinaryExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterBinaryExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitBinaryExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitBinaryExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BetweenExpressionContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode BETWEEN() { return getToken(PostgreSQLParser.BETWEEN, 0); }
		public TerminalNode AND() { return getToken(PostgreSQLParser.AND, 0); }
		public TerminalNode NOT() { return getToken(PostgreSQLParser.NOT, 0); }
		public BetweenExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterBetweenExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitBetweenExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitBetweenExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class InExpressionContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode IN() { return getToken(PostgreSQLParser.IN, 0); }
		public TerminalNode LPAREN() { return getToken(PostgreSQLParser.LPAREN, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(PostgreSQLParser.RPAREN, 0); }
		public TerminalNode NOT() { return getToken(PostgreSQLParser.NOT, 0); }
		public InExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterInExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitInExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitInExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ParenthesizedExpressionContext extends ExpressionContext {
		public TerminalNode LPAREN() { return getToken(PostgreSQLParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(PostgreSQLParser.RPAREN, 0); }
		public ParenthesizedExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterParenthesizedExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitParenthesizedExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitParenthesizedExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExistsExpressionContext extends ExpressionContext {
		public TerminalNode EXISTS() { return getToken(PostgreSQLParser.EXISTS, 0); }
		public TerminalNode LPAREN() { return getToken(PostgreSQLParser.LPAREN, 0); }
		public SelectStatementContext selectStatement() {
			return getRuleContext(SelectStatementContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(PostgreSQLParser.RPAREN, 0); }
		public ExistsExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterExistsExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitExistsExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitExistsExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CaseExpressionContext extends ExpressionContext {
		public TerminalNode CASE() { return getToken(PostgreSQLParser.CASE, 0); }
		public TerminalNode END() { return getToken(PostgreSQLParser.END, 0); }
		public List<WhenClauseContext> whenClause() {
			return getRuleContexts(WhenClauseContext.class);
		}
		public WhenClauseContext whenClause(int i) {
			return getRuleContext(WhenClauseContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(PostgreSQLParser.ELSE, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public CaseExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterCaseExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitCaseExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitCaseExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FunctionCallExpressionContext extends ExpressionContext {
		public FunctionCallContext functionCall() {
			return getRuleContext(FunctionCallContext.class,0);
		}
		public FunctionCallExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterFunctionCallExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitFunctionCallExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitFunctionCallExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LikeExpressionContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode LIKE() { return getToken(PostgreSQLParser.LIKE, 0); }
		public TerminalNode NOT() { return getToken(PostgreSQLParser.NOT, 0); }
		public LikeExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterLikeExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitLikeExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitLikeExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LiteralExpressionContext extends ExpressionContext {
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public LiteralExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterLiteralExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitLiteralExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitLiteralExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class IsNullExpressionContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode IS() { return getToken(PostgreSQLParser.IS, 0); }
		public TerminalNode NULL() { return getToken(PostgreSQLParser.NULL, 0); }
		public TerminalNode NOT() { return getToken(PostgreSQLParser.NOT, 0); }
		public IsNullExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterIsNullExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitIsNullExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitIsNullExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		return expression(0);
	}

	private ExpressionContext expression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExpressionContext _localctx = new ExpressionContext(_ctx, _parentState);
		ExpressionContext _prevctx = _localctx;
		int _startState = 54;
		enterRecursionRule(_localctx, 54, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(368);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,43,_ctx) ) {
			case 1:
				{
				_localctx = new LiteralExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(338);
				literal();
				}
				break;
			case 2:
				{
				_localctx = new ColumnReferenceExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(339);
				columnReference();
				}
				break;
			case 3:
				{
				_localctx = new FunctionCallExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(340);
				functionCall();
				}
				break;
			case 4:
				{
				_localctx = new ParenthesizedExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(341);
				match(LPAREN);
				setState(342);
				expression(0);
				setState(343);
				match(RPAREN);
				}
				break;
			case 5:
				{
				_localctx = new SubqueryExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(345);
				match(LPAREN);
				setState(346);
				selectStatement();
				setState(347);
				match(RPAREN);
				}
				break;
			case 6:
				{
				_localctx = new NotExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(349);
				match(NOT);
				setState(350);
				expression(8);
				}
				break;
			case 7:
				{
				_localctx = new CaseExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(351);
				match(CASE);
				setState(353); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(352);
					whenClause();
					}
					}
					setState(355); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==WHEN );
				setState(359);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ELSE) {
					{
					setState(357);
					match(ELSE);
					setState(358);
					expression(0);
					}
				}

				setState(361);
				match(END);
				}
				break;
			case 8:
				{
				_localctx = new ExistsExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(363);
				match(EXISTS);
				setState(364);
				match(LPAREN);
				setState(365);
				selectStatement();
				setState(366);
				match(RPAREN);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(415);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,50,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(413);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,49,_ctx) ) {
					case 1:
						{
						_localctx = new BinaryExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(370);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(371);
						binaryOperator();
						setState(372);
						expression(10);
						}
						break;
					case 2:
						{
						_localctx = new LikeExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(374);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(376);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==NOT) {
							{
							setState(375);
							match(NOT);
							}
						}

						setState(378);
						match(LIKE);
						setState(379);
						expression(5);
						}
						break;
					case 3:
						{
						_localctx = new BetweenExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(380);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(382);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==NOT) {
							{
							setState(381);
							match(NOT);
							}
						}

						setState(384);
						match(BETWEEN);
						setState(385);
						expression(0);
						setState(386);
						match(AND);
						setState(387);
						expression(4);
						}
						break;
					case 4:
						{
						_localctx = new IsNullExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(389);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(390);
						match(IS);
						setState(392);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==NOT) {
							{
							setState(391);
							match(NOT);
							}
						}

						setState(394);
						match(NULL);
						}
						break;
					case 5:
						{
						_localctx = new InExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(395);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(397);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==NOT) {
							{
							setState(396);
							match(NOT);
							}
						}

						setState(399);
						match(IN);
						setState(400);
						match(LPAREN);
						setState(401);
						expressionList();
						setState(402);
						match(RPAREN);
						}
						break;
					case 6:
						{
						_localctx = new InSubqueryExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(404);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(406);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==NOT) {
							{
							setState(405);
							match(NOT);
							}
						}

						setState(408);
						match(IN);
						setState(409);
						match(LPAREN);
						setState(410);
						selectStatement();
						setState(411);
						match(RPAREN);
						}
						break;
					}
					} 
				}
				setState(417);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,50,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class WhenClauseContext extends ParserRuleContext {
		public TerminalNode WHEN() { return getToken(PostgreSQLParser.WHEN, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode THEN() { return getToken(PostgreSQLParser.THEN, 0); }
		public WhenClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whenClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterWhenClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitWhenClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitWhenClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhenClauseContext whenClause() throws RecognitionException {
		WhenClauseContext _localctx = new WhenClauseContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_whenClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(418);
			match(WHEN);
			setState(419);
			expression(0);
			setState(420);
			match(THEN);
			setState(421);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BinaryOperatorContext extends ParserRuleContext {
		public TerminalNode EQ() { return getToken(PostgreSQLParser.EQ, 0); }
		public TerminalNode NE() { return getToken(PostgreSQLParser.NE, 0); }
		public TerminalNode LT() { return getToken(PostgreSQLParser.LT, 0); }
		public TerminalNode LE() { return getToken(PostgreSQLParser.LE, 0); }
		public TerminalNode GT() { return getToken(PostgreSQLParser.GT, 0); }
		public TerminalNode GE() { return getToken(PostgreSQLParser.GE, 0); }
		public TerminalNode PLUS() { return getToken(PostgreSQLParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(PostgreSQLParser.MINUS, 0); }
		public TerminalNode MULTIPLY() { return getToken(PostgreSQLParser.MULTIPLY, 0); }
		public TerminalNode DIVIDE() { return getToken(PostgreSQLParser.DIVIDE, 0); }
		public TerminalNode MODULO() { return getToken(PostgreSQLParser.MODULO, 0); }
		public TerminalNode POWER() { return getToken(PostgreSQLParser.POWER, 0); }
		public TerminalNode AND() { return getToken(PostgreSQLParser.AND, 0); }
		public TerminalNode OR() { return getToken(PostgreSQLParser.OR, 0); }
		public TerminalNode CONCAT() { return getToken(PostgreSQLParser.CONCAT, 0); }
		public TerminalNode JSONB_CONTAINS() { return getToken(PostgreSQLParser.JSONB_CONTAINS, 0); }
		public TerminalNode JSONB_CONTAINED() { return getToken(PostgreSQLParser.JSONB_CONTAINED, 0); }
		public TerminalNode JSONB_EXISTS() { return getToken(PostgreSQLParser.JSONB_EXISTS, 0); }
		public TerminalNode JSONB_EXTRACT() { return getToken(PostgreSQLParser.JSONB_EXTRACT, 0); }
		public TerminalNode JSONB_EXTRACT_TEXT() { return getToken(PostgreSQLParser.JSONB_EXTRACT_TEXT, 0); }
		public TerminalNode JSONB_PATH_EXTRACT() { return getToken(PostgreSQLParser.JSONB_PATH_EXTRACT, 0); }
		public TerminalNode JSONB_PATH_EXTRACT_TEXT() { return getToken(PostgreSQLParser.JSONB_PATH_EXTRACT_TEXT, 0); }
		public BinaryOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_binaryOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterBinaryOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitBinaryOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitBinaryOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BinaryOperatorContext binaryOperator() throws RecognitionException {
		BinaryOperatorContext _localctx = new BinaryOperatorContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_binaryOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(423);
			_la = _input.LA(1);
			if ( !(_la==AND || _la==OR || ((((_la - 83)) & ~0x3f) == 0 && ((1L << (_la - 83)) & 1048575L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LiteralContext extends ParserRuleContext {
		public LiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literal; }
	 
		public LiteralContext() { }
		public void copyFrom(LiteralContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DecimalLiteralContext extends LiteralContext {
		public TerminalNode DECIMAL_LITERAL() { return getToken(PostgreSQLParser.DECIMAL_LITERAL, 0); }
		public DecimalLiteralContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterDecimalLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitDecimalLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitDecimalLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NullLiteralContext extends LiteralContext {
		public TerminalNode NULL() { return getToken(PostgreSQLParser.NULL, 0); }
		public NullLiteralContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterNullLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitNullLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitNullLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringLiteralContext extends LiteralContext {
		public TerminalNode STRING() { return getToken(PostgreSQLParser.STRING, 0); }
		public StringLiteralContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterStringLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitStringLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitStringLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ScientificLiteralContext extends LiteralContext {
		public TerminalNode SCIENTIFIC_LITERAL() { return getToken(PostgreSQLParser.SCIENTIFIC_LITERAL, 0); }
		public ScientificLiteralContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterScientificLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitScientificLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitScientificLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class IntegerLiteralContext extends LiteralContext {
		public TerminalNode INTEGER_LITERAL() { return getToken(PostgreSQLParser.INTEGER_LITERAL, 0); }
		public IntegerLiteralContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterIntegerLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitIntegerLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitIntegerLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BooleanLiteralContext extends LiteralContext {
		public TerminalNode TRUE() { return getToken(PostgreSQLParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(PostgreSQLParser.FALSE, 0); }
		public BooleanLiteralContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterBooleanLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitBooleanLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitBooleanLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_literal);
		try {
			setState(432);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STRING:
				_localctx = new StringLiteralContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(425);
				match(STRING);
				}
				break;
			case INTEGER_LITERAL:
				_localctx = new IntegerLiteralContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(426);
				match(INTEGER_LITERAL);
				}
				break;
			case DECIMAL_LITERAL:
				_localctx = new DecimalLiteralContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(427);
				match(DECIMAL_LITERAL);
				}
				break;
			case SCIENTIFIC_LITERAL:
				_localctx = new ScientificLiteralContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(428);
				match(SCIENTIFIC_LITERAL);
				}
				break;
			case TRUE:
				_localctx = new BooleanLiteralContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(429);
				match(TRUE);
				}
				break;
			case FALSE:
				_localctx = new BooleanLiteralContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(430);
				match(FALSE);
				}
				break;
			case NULL:
				_localctx = new NullLiteralContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(431);
				match(NULL);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ColumnReferenceContext extends ParserRuleContext {
		public ColumnNameContext columnName() {
			return getRuleContext(ColumnNameContext.class,0);
		}
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(PostgreSQLParser.DOT, 0); }
		public ColumnReferenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnReference; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterColumnReference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitColumnReference(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitColumnReference(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnReferenceContext columnReference() throws RecognitionException {
		ColumnReferenceContext _localctx = new ColumnReferenceContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_columnReference);
		try {
			setState(439);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,52,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(434);
				columnName();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(435);
				tableName();
				setState(436);
				match(DOT);
				setState(437);
				columnName();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TableNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TableNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterTableName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitTableName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitTableName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableNameContext tableName() throws RecognitionException {
		TableNameContext _localctx = new TableNameContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_tableName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(441);
			identifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ColumnNameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ColumnNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterColumnName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitColumnName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitColumnName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnNameContext columnName() throws RecognitionException {
		ColumnNameContext _localctx = new ColumnNameContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_columnName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(443);
			identifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AliasContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public AliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterAlias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitAlias(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitAlias(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AliasContext alias() throws RecognitionException {
		AliasContext _localctx = new AliasContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_alias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(445);
			identifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionCallContext extends ParserRuleContext {
		public FunctionCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionCall; }
	 
		public FunctionCallContext() { }
		public void copyFrom(FunctionCallContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CountFunctionContext extends FunctionCallContext {
		public TerminalNode COUNT() { return getToken(PostgreSQLParser.COUNT, 0); }
		public TerminalNode LPAREN() { return getToken(PostgreSQLParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(PostgreSQLParser.RPAREN, 0); }
		public TerminalNode MULTIPLY() { return getToken(PostgreSQLParser.MULTIPLY, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public CountFunctionContext(FunctionCallContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterCountFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitCountFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitCountFunction(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CountDistinctFunctionContext extends FunctionCallContext {
		public TerminalNode COUNT() { return getToken(PostgreSQLParser.COUNT, 0); }
		public TerminalNode LPAREN() { return getToken(PostgreSQLParser.LPAREN, 0); }
		public TerminalNode DISTINCT() { return getToken(PostgreSQLParser.DISTINCT, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(PostgreSQLParser.RPAREN, 0); }
		public CountDistinctFunctionContext(FunctionCallContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterCountDistinctFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitCountDistinctFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitCountDistinctFunction(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class GenRandomUuidFunctionContext extends FunctionCallContext {
		public TerminalNode GEN_RANDOM_UUID() { return getToken(PostgreSQLParser.GEN_RANDOM_UUID, 0); }
		public TerminalNode LPAREN() { return getToken(PostgreSQLParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(PostgreSQLParser.RPAREN, 0); }
		public GenRandomUuidFunctionContext(FunctionCallContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterGenRandomUuidFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitGenRandomUuidFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitGenRandomUuidFunction(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SumFunctionContext extends FunctionCallContext {
		public TerminalNode SUM() { return getToken(PostgreSQLParser.SUM, 0); }
		public TerminalNode LPAREN() { return getToken(PostgreSQLParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(PostgreSQLParser.RPAREN, 0); }
		public SumFunctionContext(FunctionCallContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterSumFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitSumFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitSumFunction(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class UuidGenerateV4FunctionContext extends FunctionCallContext {
		public TerminalNode UUID_GENERATE_V4() { return getToken(PostgreSQLParser.UUID_GENERATE_V4, 0); }
		public TerminalNode LPAREN() { return getToken(PostgreSQLParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(PostgreSQLParser.RPAREN, 0); }
		public UuidGenerateV4FunctionContext(FunctionCallContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterUuidGenerateV4Function(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitUuidGenerateV4Function(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitUuidGenerateV4Function(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MinFunctionContext extends FunctionCallContext {
		public TerminalNode MIN() { return getToken(PostgreSQLParser.MIN, 0); }
		public TerminalNode LPAREN() { return getToken(PostgreSQLParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(PostgreSQLParser.RPAREN, 0); }
		public MinFunctionContext(FunctionCallContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterMinFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitMinFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitMinFunction(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class UuidGenerateV1FunctionContext extends FunctionCallContext {
		public TerminalNode UUID_GENERATE_V1() { return getToken(PostgreSQLParser.UUID_GENERATE_V1, 0); }
		public TerminalNode LPAREN() { return getToken(PostgreSQLParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(PostgreSQLParser.RPAREN, 0); }
		public UuidGenerateV1FunctionContext(FunctionCallContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterUuidGenerateV1Function(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitUuidGenerateV1Function(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitUuidGenerateV1Function(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class GenericFunctionContext extends FunctionCallContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(PostgreSQLParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(PostgreSQLParser.RPAREN, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public GenericFunctionContext(FunctionCallContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterGenericFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitGenericFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitGenericFunction(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MaxFunctionContext extends FunctionCallContext {
		public TerminalNode MAX() { return getToken(PostgreSQLParser.MAX, 0); }
		public TerminalNode LPAREN() { return getToken(PostgreSQLParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(PostgreSQLParser.RPAREN, 0); }
		public MaxFunctionContext(FunctionCallContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterMaxFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitMaxFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitMaxFunction(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AvgFunctionContext extends FunctionCallContext {
		public TerminalNode AVG() { return getToken(PostgreSQLParser.AVG, 0); }
		public TerminalNode LPAREN() { return getToken(PostgreSQLParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(PostgreSQLParser.RPAREN, 0); }
		public AvgFunctionContext(FunctionCallContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterAvgFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitAvgFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitAvgFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionCallContext functionCall() throws RecognitionException {
		FunctionCallContext _localctx = new FunctionCallContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_functionCall);
		int _la;
		try {
			setState(496);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,55,_ctx) ) {
			case 1:
				_localctx = new GenRandomUuidFunctionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(447);
				match(GEN_RANDOM_UUID);
				setState(448);
				match(LPAREN);
				setState(449);
				match(RPAREN);
				}
				break;
			case 2:
				_localctx = new UuidGenerateV1FunctionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(450);
				match(UUID_GENERATE_V1);
				setState(451);
				match(LPAREN);
				setState(452);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new UuidGenerateV4FunctionContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(453);
				match(UUID_GENERATE_V4);
				setState(454);
				match(LPAREN);
				setState(455);
				match(RPAREN);
				}
				break;
			case 4:
				_localctx = new CountFunctionContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(456);
				match(COUNT);
				setState(457);
				match(LPAREN);
				setState(460);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case MULTIPLY:
					{
					setState(458);
					match(MULTIPLY);
					}
					break;
				case NOT:
				case NULL:
				case CASE:
				case EXISTS:
				case TRUE:
				case FALSE:
				case GEN_RANDOM_UUID:
				case UUID_GENERATE_V1:
				case UUID_GENERATE_V4:
				case COUNT:
				case SUM:
				case AVG:
				case MIN:
				case MAX:
				case LPAREN:
				case STRING:
				case IDENTIFIER:
				case QUOTED_IDENTIFIER:
				case INTEGER_LITERAL:
				case DECIMAL_LITERAL:
				case SCIENTIFIC_LITERAL:
					{
					setState(459);
					expression(0);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(462);
				match(RPAREN);
				}
				break;
			case 5:
				_localctx = new SumFunctionContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(463);
				match(SUM);
				setState(464);
				match(LPAREN);
				setState(465);
				expression(0);
				setState(466);
				match(RPAREN);
				}
				break;
			case 6:
				_localctx = new AvgFunctionContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(468);
				match(AVG);
				setState(469);
				match(LPAREN);
				setState(470);
				expression(0);
				setState(471);
				match(RPAREN);
				}
				break;
			case 7:
				_localctx = new MinFunctionContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(473);
				match(MIN);
				setState(474);
				match(LPAREN);
				setState(475);
				expression(0);
				setState(476);
				match(RPAREN);
				}
				break;
			case 8:
				_localctx = new MaxFunctionContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(478);
				match(MAX);
				setState(479);
				match(LPAREN);
				setState(480);
				expression(0);
				setState(481);
				match(RPAREN);
				}
				break;
			case 9:
				_localctx = new CountDistinctFunctionContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(483);
				match(COUNT);
				setState(484);
				match(LPAREN);
				setState(485);
				match(DISTINCT);
				setState(486);
				expression(0);
				setState(487);
				match(RPAREN);
				}
				break;
			case 10:
				_localctx = new GenericFunctionContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(489);
				identifier();
				setState(490);
				match(LPAREN);
				setState(492);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3958241860091904L) != 0) || ((((_la - 75)) & ~0x3f) == 0 && ((1L << (_la - 75)) & 17317576573183L) != 0)) {
					{
					setState(491);
					expressionList();
					}
				}

				setState(494);
				match(RPAREN);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionListContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(PostgreSQLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(PostgreSQLParser.COMMA, i);
		}
		public ExpressionListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expressionList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterExpressionList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitExpressionList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitExpressionList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionListContext expressionList() throws RecognitionException {
		ExpressionListContext _localctx = new ExpressionListContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_expressionList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(498);
			expression(0);
			setState(503);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(499);
				match(COMMA);
				setState(500);
				expression(0);
				}
				}
				setState(505);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ColumnListContext extends ParserRuleContext {
		public List<ColumnNameContext> columnName() {
			return getRuleContexts(ColumnNameContext.class);
		}
		public ColumnNameContext columnName(int i) {
			return getRuleContext(ColumnNameContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(PostgreSQLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(PostgreSQLParser.COMMA, i);
		}
		public ColumnListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterColumnList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitColumnList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitColumnList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnListContext columnList() throws RecognitionException {
		ColumnListContext _localctx = new ColumnListContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_columnList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(506);
			columnName();
			setState(511);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(507);
				match(COMMA);
				setState(508);
				columnName();
				}
				}
				setState(513);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DataTypeContext extends ParserRuleContext {
		public DataTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dataType; }
	 
		public DataTypeContext() { }
		public void copyFrom(DataTypeContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CharTypeContext extends DataTypeContext {
		public TerminalNode CHAR() { return getToken(PostgreSQLParser.CHAR, 0); }
		public TerminalNode LPAREN() { return getToken(PostgreSQLParser.LPAREN, 0); }
		public TerminalNode INTEGER_LITERAL() { return getToken(PostgreSQLParser.INTEGER_LITERAL, 0); }
		public TerminalNode RPAREN() { return getToken(PostgreSQLParser.RPAREN, 0); }
		public CharTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterCharType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitCharType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitCharType(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DecimalTypeContext extends DataTypeContext {
		public TerminalNode DECIMAL() { return getToken(PostgreSQLParser.DECIMAL, 0); }
		public TerminalNode LPAREN() { return getToken(PostgreSQLParser.LPAREN, 0); }
		public List<TerminalNode> INTEGER_LITERAL() { return getTokens(PostgreSQLParser.INTEGER_LITERAL); }
		public TerminalNode INTEGER_LITERAL(int i) {
			return getToken(PostgreSQLParser.INTEGER_LITERAL, i);
		}
		public TerminalNode RPAREN() { return getToken(PostgreSQLParser.RPAREN, 0); }
		public TerminalNode COMMA() { return getToken(PostgreSQLParser.COMMA, 0); }
		public DecimalTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterDecimalType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitDecimalType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitDecimalType(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class RealTypeContext extends DataTypeContext {
		public TerminalNode REAL() { return getToken(PostgreSQLParser.REAL, 0); }
		public RealTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterRealType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitRealType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitRealType(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BooleanTypeContext extends DataTypeContext {
		public TerminalNode BOOLEAN() { return getToken(PostgreSQLParser.BOOLEAN, 0); }
		public BooleanTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterBooleanType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitBooleanType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitBooleanType(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class IntegerTypeContext extends DataTypeContext {
		public TerminalNode INTEGER() { return getToken(PostgreSQLParser.INTEGER, 0); }
		public TerminalNode INT() { return getToken(PostgreSQLParser.INT, 0); }
		public IntegerTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterIntegerType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitIntegerType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitIntegerType(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class JsonbTypeContext extends DataTypeContext {
		public TerminalNode JSONB() { return getToken(PostgreSQLParser.JSONB, 0); }
		public JsonbTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterJsonbType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitJsonbType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitJsonbType(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DoublePrecisionTypeContext extends DataTypeContext {
		public TerminalNode DOUBLE() { return getToken(PostgreSQLParser.DOUBLE, 0); }
		public TerminalNode PRECISION() { return getToken(PostgreSQLParser.PRECISION, 0); }
		public DoublePrecisionTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterDoublePrecisionType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitDoublePrecisionType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitDoublePrecisionType(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeTypeContext extends DataTypeContext {
		public TerminalNode TIME() { return getToken(PostgreSQLParser.TIME, 0); }
		public TimeTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterTimeType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitTimeType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitTimeType(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TextTypeContext extends DataTypeContext {
		public TerminalNode TEXT() { return getToken(PostgreSQLParser.TEXT, 0); }
		public TextTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterTextType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitTextType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitTextType(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class UuidTypeContext extends DataTypeContext {
		public TerminalNode UUID() { return getToken(PostgreSQLParser.UUID, 0); }
		public UuidTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterUuidType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitUuidType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitUuidType(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SmallintTypeContext extends DataTypeContext {
		public TerminalNode SMALLINT() { return getToken(PostgreSQLParser.SMALLINT, 0); }
		public SmallintTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterSmallintType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitSmallintType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitSmallintType(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateTypeContext extends DataTypeContext {
		public TerminalNode DATE() { return getToken(PostgreSQLParser.DATE, 0); }
		public DateTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterDateType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitDateType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitDateType(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimestamptzTypeContext extends DataTypeContext {
		public TerminalNode TIMESTAMPTZ() { return getToken(PostgreSQLParser.TIMESTAMPTZ, 0); }
		public TimestamptzTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterTimestamptzType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitTimestamptzType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitTimestamptzType(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ByteaTypeContext extends DataTypeContext {
		public TerminalNode BYTEA() { return getToken(PostgreSQLParser.BYTEA, 0); }
		public ByteaTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterByteaType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitByteaType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitByteaType(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NumericTypeContext extends DataTypeContext {
		public TerminalNode NUMERIC() { return getToken(PostgreSQLParser.NUMERIC, 0); }
		public TerminalNode LPAREN() { return getToken(PostgreSQLParser.LPAREN, 0); }
		public List<TerminalNode> INTEGER_LITERAL() { return getTokens(PostgreSQLParser.INTEGER_LITERAL); }
		public TerminalNode INTEGER_LITERAL(int i) {
			return getToken(PostgreSQLParser.INTEGER_LITERAL, i);
		}
		public TerminalNode RPAREN() { return getToken(PostgreSQLParser.RPAREN, 0); }
		public TerminalNode COMMA() { return getToken(PostgreSQLParser.COMMA, 0); }
		public NumericTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterNumericType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitNumericType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitNumericType(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimestampTypeContext extends DataTypeContext {
		public TerminalNode TIMESTAMP() { return getToken(PostgreSQLParser.TIMESTAMP, 0); }
		public TimestampTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterTimestampType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitTimestampType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitTimestampType(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BigintTypeContext extends DataTypeContext {
		public TerminalNode BIGINT() { return getToken(PostgreSQLParser.BIGINT, 0); }
		public BigintTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterBigintType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitBigintType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitBigintType(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class VarcharTypeContext extends DataTypeContext {
		public TerminalNode VARCHAR() { return getToken(PostgreSQLParser.VARCHAR, 0); }
		public TerminalNode LPAREN() { return getToken(PostgreSQLParser.LPAREN, 0); }
		public TerminalNode INTEGER_LITERAL() { return getToken(PostgreSQLParser.INTEGER_LITERAL, 0); }
		public TerminalNode RPAREN() { return getToken(PostgreSQLParser.RPAREN, 0); }
		public VarcharTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterVarcharType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitVarcharType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitVarcharType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DataTypeContext dataType() throws RecognitionException {
		DataTypeContext _localctx = new DataTypeContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_dataType);
		int _la;
		try {
			setState(561);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SMALLINT:
				_localctx = new SmallintTypeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(514);
				match(SMALLINT);
				}
				break;
			case INTEGER:
			case INT:
				_localctx = new IntegerTypeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(515);
				_la = _input.LA(1);
				if ( !(_la==INTEGER || _la==INT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case BIGINT:
				_localctx = new BigintTypeContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(516);
				match(BIGINT);
				}
				break;
			case DECIMAL:
				_localctx = new DecimalTypeContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(517);
				match(DECIMAL);
				setState(525);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LPAREN) {
					{
					setState(518);
					match(LPAREN);
					setState(519);
					match(INTEGER_LITERAL);
					setState(522);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==COMMA) {
						{
						setState(520);
						match(COMMA);
						setState(521);
						match(INTEGER_LITERAL);
						}
					}

					setState(524);
					match(RPAREN);
					}
				}

				}
				break;
			case NUMERIC:
				_localctx = new NumericTypeContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(527);
				match(NUMERIC);
				setState(535);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LPAREN) {
					{
					setState(528);
					match(LPAREN);
					setState(529);
					match(INTEGER_LITERAL);
					setState(532);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==COMMA) {
						{
						setState(530);
						match(COMMA);
						setState(531);
						match(INTEGER_LITERAL);
						}
					}

					setState(534);
					match(RPAREN);
					}
				}

				}
				break;
			case REAL:
				_localctx = new RealTypeContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(537);
				match(REAL);
				}
				break;
			case DOUBLE:
				_localctx = new DoublePrecisionTypeContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(538);
				match(DOUBLE);
				setState(539);
				match(PRECISION);
				}
				break;
			case VARCHAR:
				_localctx = new VarcharTypeContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(540);
				match(VARCHAR);
				setState(544);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LPAREN) {
					{
					setState(541);
					match(LPAREN);
					setState(542);
					match(INTEGER_LITERAL);
					setState(543);
					match(RPAREN);
					}
				}

				}
				break;
			case CHAR:
				_localctx = new CharTypeContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(546);
				match(CHAR);
				setState(550);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LPAREN) {
					{
					setState(547);
					match(LPAREN);
					setState(548);
					match(INTEGER_LITERAL);
					setState(549);
					match(RPAREN);
					}
				}

				}
				break;
			case TEXT:
				_localctx = new TextTypeContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(552);
				match(TEXT);
				}
				break;
			case BOOLEAN:
				_localctx = new BooleanTypeContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(553);
				match(BOOLEAN);
				}
				break;
			case DATE:
				_localctx = new DateTypeContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(554);
				match(DATE);
				}
				break;
			case TIME:
				_localctx = new TimeTypeContext(_localctx);
				enterOuterAlt(_localctx, 13);
				{
				setState(555);
				match(TIME);
				}
				break;
			case TIMESTAMP:
				_localctx = new TimestampTypeContext(_localctx);
				enterOuterAlt(_localctx, 14);
				{
				setState(556);
				match(TIMESTAMP);
				}
				break;
			case TIMESTAMPTZ:
				_localctx = new TimestamptzTypeContext(_localctx);
				enterOuterAlt(_localctx, 15);
				{
				setState(557);
				match(TIMESTAMPTZ);
				}
				break;
			case UUID:
				_localctx = new UuidTypeContext(_localctx);
				enterOuterAlt(_localctx, 16);
				{
				setState(558);
				match(UUID);
				}
				break;
			case JSONB:
				_localctx = new JsonbTypeContext(_localctx);
				enterOuterAlt(_localctx, 17);
				{
				setState(559);
				match(JSONB);
				}
				break;
			case BYTEA:
				_localctx = new ByteaTypeContext(_localctx);
				enterOuterAlt(_localctx, 18);
				{
				setState(560);
				match(BYTEA);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IdentifierContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(PostgreSQLParser.IDENTIFIER, 0); }
		public TerminalNode QUOTED_IDENTIFIER() { return getToken(PostgreSQLParser.QUOTED_IDENTIFIER, 0); }
		public IdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).enterIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PostgreSQLParserListener ) ((PostgreSQLParserListener)listener).exitIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PostgreSQLParserVisitor ) return ((PostgreSQLParserVisitor<? extends T>)visitor).visitIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierContext identifier() throws RecognitionException {
		IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_identifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(563);
			_la = _input.LA(1);
			if ( !(_la==IDENTIFIER || _la==QUOTED_IDENTIFIER) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 27:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 9);
		case 1:
			return precpred(_ctx, 4);
		case 2:
			return precpred(_ctx, 3);
		case 3:
			return precpred(_ctx, 7);
		case 4:
			return precpred(_ctx, 6);
		case 5:
			return precpred(_ctx, 5);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001y\u0236\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"+
		"\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e"+
		"\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0002\"\u0007\"\u0002"+
		"#\u0007#\u0002$\u0007$\u0002%\u0007%\u0002&\u0007&\u0002\'\u0007\'\u0001"+
		"\u0000\u0001\u0000\u0001\u0000\u0005\u0000T\b\u0000\n\u0000\f\u0000W\t"+
		"\u0000\u0001\u0000\u0003\u0000Z\b\u0000\u0001\u0000\u0001\u0000\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003"+
		"\u0001d\b\u0001\u0001\u0002\u0001\u0002\u0003\u0002h\b\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0003\u0002m\b\u0002\u0001\u0002\u0003\u0002"+
		"p\b\u0002\u0001\u0002\u0003\u0002s\b\u0002\u0001\u0002\u0003\u0002v\b"+
		"\u0002\u0001\u0002\u0003\u0002y\b\u0002\u0001\u0002\u0003\u0002|\b\u0002"+
		"\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0005\u0004"+
		"\u0083\b\u0004\n\u0004\f\u0004\u0086\t\u0004\u0001\u0004\u0003\u0004\u0089"+
		"\b\u0004\u0001\u0005\u0001\u0005\u0003\u0005\u008d\b\u0005\u0001\u0005"+
		"\u0003\u0005\u0090\b\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0005\u0006"+
		"\u0095\b\u0006\n\u0006\f\u0006\u0098\t\u0006\u0001\u0007\u0001\u0007\u0005"+
		"\u0007\u009c\b\u0007\n\u0007\f\u0007\u009f\t\u0007\u0001\b\u0001\b\u0003"+
		"\b\u00a3\b\b\u0001\b\u0003\b\u00a6\b\b\u0001\b\u0001\b\u0001\b\u0001\b"+
		"\u0003\b\u00ac\b\b\u0001\b\u0003\b\u00af\b\b\u0003\b\u00b1\b\b\u0001\t"+
		"\u0001\t\u0001\t\u0001\t\u0001\t\u0001\n\u0003\n\u00b9\b\n\u0001\n\u0001"+
		"\n\u0003\n\u00bd\b\n\u0001\n\u0001\n\u0003\n\u00c1\b\n\u0001\n\u0001\n"+
		"\u0003\n\u00c5\b\n\u0001\n\u0003\n\u00c8\b\n\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0003"+
		"\u000b\u00d2\b\u000b\u0001\f\u0001\f\u0001\f\u0001\r\u0001\r\u0001\r\u0001"+
		"\r\u0001\r\u0005\r\u00dc\b\r\n\r\f\r\u00df\t\r\u0001\u000e\u0001\u000e"+
		"\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f"+
		"\u0005\u000f\u00e9\b\u000f\n\u000f\f\u000f\u00ec\t\u000f\u0001\u0010\u0001"+
		"\u0010\u0003\u0010\u00f0\b\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0001"+
		"\u0011\u0003\u0011\u00f6\b\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001"+
		"\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u00ff\b\u0012\u0001"+
		"\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0005\u0012\u0105\b\u0012\n"+
		"\u0012\f\u0012\u0108\t\u0012\u0001\u0012\u0003\u0012\u010b\b\u0012\u0001"+
		"\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0005\u0013\u0111\b\u0013\n"+
		"\u0013\f\u0013\u0114\t\u0013\u0001\u0013\u0001\u0013\u0001\u0014\u0001"+
		"\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0005\u0014\u011e"+
		"\b\u0014\n\u0014\f\u0014\u0121\t\u0014\u0001\u0014\u0003\u0014\u0124\b"+
		"\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0003\u0016\u012e\b\u0016\u0001\u0017\u0001"+
		"\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0005"+
		"\u0017\u0137\b\u0017\n\u0017\f\u0017\u013a\t\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0018\u0001\u0018\u0001\u0018\u0005\u0018\u0141\b\u0018\n\u0018"+
		"\f\u0018\u0144\t\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019"+
		"\u0001\u0019\u0001\u0019\u0003\u0019\u014c\b\u0019\u0001\u001a\u0001\u001a"+
		"\u0001\u001a\u0001\u001a\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0004\u001b\u0162\b\u001b\u000b\u001b\f\u001b\u0163\u0001\u001b\u0001"+
		"\u001b\u0003\u001b\u0168\b\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001"+
		"\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0003\u001b\u0171\b\u001b\u0001"+
		"\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0003"+
		"\u001b\u0179\b\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0003"+
		"\u001b\u017f\b\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001"+
		"\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0003\u001b\u0189\b\u001b\u0001"+
		"\u001b\u0001\u001b\u0001\u001b\u0003\u001b\u018e\b\u001b\u0001\u001b\u0001"+
		"\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0003"+
		"\u001b\u0197\b\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001"+
		"\u001b\u0005\u001b\u019e\b\u001b\n\u001b\f\u001b\u01a1\t\u001b\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001d\u0001\u001d"+
		"\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e"+
		"\u0001\u001e\u0003\u001e\u01b1\b\u001e\u0001\u001f\u0001\u001f\u0001\u001f"+
		"\u0001\u001f\u0001\u001f\u0003\u001f\u01b8\b\u001f\u0001 \u0001 \u0001"+
		"!\u0001!\u0001\"\u0001\"\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001"+
		"#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0003#\u01cd\b#\u0001#\u0001"+
		"#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001"+
		"#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001"+
		"#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0003#\u01ed"+
		"\b#\u0001#\u0001#\u0003#\u01f1\b#\u0001$\u0001$\u0001$\u0005$\u01f6\b"+
		"$\n$\f$\u01f9\t$\u0001%\u0001%\u0001%\u0005%\u01fe\b%\n%\f%\u0201\t%\u0001"+
		"&\u0001&\u0001&\u0001&\u0001&\u0001&\u0001&\u0001&\u0003&\u020b\b&\u0001"+
		"&\u0003&\u020e\b&\u0001&\u0001&\u0001&\u0001&\u0001&\u0003&\u0215\b&\u0001"+
		"&\u0003&\u0218\b&\u0001&\u0001&\u0001&\u0001&\u0001&\u0001&\u0001&\u0003"+
		"&\u0221\b&\u0001&\u0001&\u0001&\u0001&\u0003&\u0227\b&\u0001&\u0001&\u0001"+
		"&\u0001&\u0001&\u0001&\u0001&\u0001&\u0001&\u0003&\u0232\b&\u0001\'\u0001"+
		"\'\u0001\'\u0000\u00016(\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012"+
		"\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@BDFHJLN\u0000\u0005"+
		"\u0001\u0000\u001c\u001d\u0001\u0000\u001e\u001f\u0002\u0000\r\u000eS"+
		"f\u0001\u000089\u0001\u0000rs\u027f\u0000P\u0001\u0000\u0000\u0000\u0002"+
		"c\u0001\u0000\u0000\u0000\u0004e\u0001\u0000\u0000\u0000\u0006}\u0001"+
		"\u0000\u0000\u0000\b\u0088\u0001\u0000\u0000\u0000\n\u008a\u0001\u0000"+
		"\u0000\u0000\f\u0091\u0001\u0000\u0000\u0000\u000e\u0099\u0001\u0000\u0000"+
		"\u0000\u0010\u00b0\u0001\u0000\u0000\u0000\u0012\u00b2\u0001\u0000\u0000"+
		"\u0000\u0014\u00c7\u0001\u0000\u0000\u0000\u0016\u00d1\u0001\u0000\u0000"+
		"\u0000\u0018\u00d3\u0001\u0000\u0000\u0000\u001a\u00d6\u0001\u0000\u0000"+
		"\u0000\u001c\u00e0\u0001\u0000\u0000\u0000\u001e\u00e3\u0001\u0000\u0000"+
		"\u0000 \u00ed\u0001\u0000\u0000\u0000\"\u00f1\u0001\u0000\u0000\u0000"+
		"$\u00f7\u0001\u0000\u0000\u0000&\u010c\u0001\u0000\u0000\u0000(\u0117"+
		"\u0001\u0000\u0000\u0000*\u0125\u0001\u0000\u0000\u0000,\u0129\u0001\u0000"+
		"\u0000\u0000.\u012f\u0001\u0000\u0000\u00000\u013d\u0001\u0000\u0000\u0000"+
		"2\u014b\u0001\u0000\u0000\u00004\u014d\u0001\u0000\u0000\u00006\u0170"+
		"\u0001\u0000\u0000\u00008\u01a2\u0001\u0000\u0000\u0000:\u01a7\u0001\u0000"+
		"\u0000\u0000<\u01b0\u0001\u0000\u0000\u0000>\u01b7\u0001\u0000\u0000\u0000"+
		"@\u01b9\u0001\u0000\u0000\u0000B\u01bb\u0001\u0000\u0000\u0000D\u01bd"+
		"\u0001\u0000\u0000\u0000F\u01f0\u0001\u0000\u0000\u0000H\u01f2\u0001\u0000"+
		"\u0000\u0000J\u01fa\u0001\u0000\u0000\u0000L\u0231\u0001\u0000\u0000\u0000"+
		"N\u0233\u0001\u0000\u0000\u0000PU\u0003\u0002\u0001\u0000QR\u0005n\u0000"+
		"\u0000RT\u0003\u0002\u0001\u0000SQ\u0001\u0000\u0000\u0000TW\u0001\u0000"+
		"\u0000\u0000US\u0001\u0000\u0000\u0000UV\u0001\u0000\u0000\u0000VY\u0001"+
		"\u0000\u0000\u0000WU\u0001\u0000\u0000\u0000XZ\u0005n\u0000\u0000YX\u0001"+
		"\u0000\u0000\u0000YZ\u0001\u0000\u0000\u0000Z[\u0001\u0000\u0000\u0000"+
		"[\\\u0005\u0000\u0000\u0001\\\u0001\u0001\u0000\u0000\u0000]d\u0003\u0004"+
		"\u0002\u0000^d\u0003$\u0012\u0000_d\u0003(\u0014\u0000`d\u0003,\u0016"+
		"\u0000ad\u0003.\u0017\u0000bd\u00034\u001a\u0000c]\u0001\u0000\u0000\u0000"+
		"c^\u0001\u0000\u0000\u0000c_\u0001\u0000\u0000\u0000c`\u0001\u0000\u0000"+
		"\u0000ca\u0001\u0000\u0000\u0000cb\u0001\u0000\u0000\u0000d\u0003\u0001"+
		"\u0000\u0000\u0000eg\u0005\u0001\u0000\u0000fh\u0003\u0006\u0003\u0000"+
		"gf\u0001\u0000\u0000\u0000gh\u0001\u0000\u0000\u0000hi\u0001\u0000\u0000"+
		"\u0000il\u0003\b\u0004\u0000jk\u0005\u0002\u0000\u0000km\u0003\f\u0006"+
		"\u0000lj\u0001\u0000\u0000\u0000lm\u0001\u0000\u0000\u0000mo\u0001\u0000"+
		"\u0000\u0000np\u0003\u0018\f\u0000on\u0001\u0000\u0000\u0000op\u0001\u0000"+
		"\u0000\u0000pr\u0001\u0000\u0000\u0000qs\u0003\u001a\r\u0000rq\u0001\u0000"+
		"\u0000\u0000rs\u0001\u0000\u0000\u0000su\u0001\u0000\u0000\u0000tv\u0003"+
		"\u001c\u000e\u0000ut\u0001\u0000\u0000\u0000uv\u0001\u0000\u0000\u0000"+
		"vx\u0001\u0000\u0000\u0000wy\u0003\u001e\u000f\u0000xw\u0001\u0000\u0000"+
		"\u0000xy\u0001\u0000\u0000\u0000y{\u0001\u0000\u0000\u0000z|\u0003\"\u0011"+
		"\u0000{z\u0001\u0000\u0000\u0000{|\u0001\u0000\u0000\u0000|\u0005\u0001"+
		"\u0000\u0000\u0000}~\u0007\u0000\u0000\u0000~\u0007\u0001\u0000\u0000"+
		"\u0000\u007f\u0084\u0003\n\u0005\u0000\u0080\u0081\u0005m\u0000\u0000"+
		"\u0081\u0083\u0003\n\u0005\u0000\u0082\u0080\u0001\u0000\u0000\u0000\u0083"+
		"\u0086\u0001\u0000\u0000\u0000\u0084\u0082\u0001\u0000\u0000\u0000\u0084"+
		"\u0085\u0001\u0000\u0000\u0000\u0085\u0089\u0001\u0000\u0000\u0000\u0086"+
		"\u0084\u0001\u0000\u0000\u0000\u0087\u0089\u0005[\u0000\u0000\u0088\u007f"+
		"\u0001\u0000\u0000\u0000\u0088\u0087\u0001\u0000\u0000\u0000\u0089\t\u0001"+
		"\u0000\u0000\u0000\u008a\u008f\u00036\u001b\u0000\u008b\u008d\u0005\u0015"+
		"\u0000\u0000\u008c\u008b\u0001\u0000\u0000\u0000\u008c\u008d\u0001\u0000"+
		"\u0000\u0000\u008d\u008e\u0001\u0000\u0000\u0000\u008e\u0090\u0003D\""+
		"\u0000\u008f\u008c\u0001\u0000\u0000\u0000\u008f\u0090\u0001\u0000\u0000"+
		"\u0000\u0090\u000b\u0001\u0000\u0000\u0000\u0091\u0096\u0003\u000e\u0007"+
		"\u0000\u0092\u0093\u0005m\u0000\u0000\u0093\u0095\u0003\u000e\u0007\u0000"+
		"\u0094\u0092\u0001\u0000\u0000\u0000\u0095\u0098\u0001\u0000\u0000\u0000"+
		"\u0096\u0094\u0001\u0000\u0000\u0000\u0096\u0097\u0001\u0000\u0000\u0000"+
		"\u0097\r\u0001\u0000\u0000\u0000\u0098\u0096\u0001\u0000\u0000\u0000\u0099"+
		"\u009d\u0003\u0010\b\u0000\u009a\u009c\u0003\u0012\t\u0000\u009b\u009a"+
		"\u0001\u0000\u0000\u0000\u009c\u009f\u0001\u0000\u0000\u0000\u009d\u009b"+
		"\u0001\u0000\u0000\u0000\u009d\u009e\u0001\u0000\u0000\u0000\u009e\u000f"+
		"\u0001\u0000\u0000\u0000\u009f\u009d\u0001\u0000\u0000\u0000\u00a0\u00a5"+
		"\u0003@ \u0000\u00a1\u00a3\u0005\u0015\u0000\u0000\u00a2\u00a1\u0001\u0000"+
		"\u0000\u0000\u00a2\u00a3\u0001\u0000\u0000\u0000\u00a3\u00a4\u0001\u0000"+
		"\u0000\u0000\u00a4\u00a6\u0003D\"\u0000\u00a5\u00a2\u0001\u0000\u0000"+
		"\u0000\u00a5\u00a6\u0001\u0000\u0000\u0000\u00a6\u00b1\u0001\u0000\u0000"+
		"\u0000\u00a7\u00a8\u0005g\u0000\u0000\u00a8\u00a9\u0003\u0004\u0002\u0000"+
		"\u00a9\u00ae\u0005h\u0000\u0000\u00aa\u00ac\u0005\u0015\u0000\u0000\u00ab"+
		"\u00aa\u0001\u0000\u0000\u0000\u00ab\u00ac\u0001\u0000\u0000\u0000\u00ac"+
		"\u00ad\u0001\u0000\u0000\u0000\u00ad\u00af\u0003D\"\u0000\u00ae\u00ab"+
		"\u0001\u0000\u0000\u0000\u00ae\u00af\u0001\u0000\u0000\u0000\u00af\u00b1"+
		"\u0001\u0000\u0000\u0000\u00b0\u00a0\u0001\u0000\u0000\u0000\u00b0\u00a7"+
		"\u0001\u0000\u0000\u0000\u00b1\u0011\u0001\u0000\u0000\u0000\u00b2\u00b3"+
		"\u0003\u0014\n\u0000\u00b3\u00b4\u0005&\u0000\u0000\u00b4\u00b5\u0003"+
		"\u0010\b\u0000\u00b5\u00b6\u0003\u0016\u000b\u0000\u00b6\u0013\u0001\u0000"+
		"\u0000\u0000\u00b7\u00b9\u0005 \u0000\u0000\u00b8\u00b7\u0001\u0000\u0000"+
		"\u0000\u00b8\u00b9\u0001\u0000\u0000\u0000\u00b9\u00c8\u0001\u0000\u0000"+
		"\u0000\u00ba\u00bc\u0005!\u0000\u0000\u00bb\u00bd\u0005%\u0000\u0000\u00bc"+
		"\u00bb\u0001\u0000\u0000\u0000\u00bc\u00bd\u0001\u0000\u0000\u0000\u00bd"+
		"\u00c8\u0001\u0000\u0000\u0000\u00be\u00c0\u0005\"\u0000\u0000\u00bf\u00c1"+
		"\u0005%\u0000\u0000\u00c0\u00bf\u0001\u0000\u0000\u0000\u00c0\u00c1\u0001"+
		"\u0000\u0000\u0000\u00c1\u00c8\u0001\u0000\u0000\u0000\u00c2\u00c4\u0005"+
		"#\u0000\u0000\u00c3\u00c5\u0005%\u0000\u0000\u00c4\u00c3\u0001\u0000\u0000"+
		"\u0000\u00c4\u00c5\u0001\u0000\u0000\u0000\u00c5\u00c8\u0001\u0000\u0000"+
		"\u0000\u00c6\u00c8\u0005$\u0000\u0000\u00c7\u00b8\u0001\u0000\u0000\u0000"+
		"\u00c7\u00ba\u0001\u0000\u0000\u0000\u00c7\u00be\u0001\u0000\u0000\u0000"+
		"\u00c7\u00c2\u0001\u0000\u0000\u0000\u00c7\u00c6\u0001\u0000\u0000\u0000"+
		"\u00c8\u0015\u0001\u0000\u0000\u0000\u00c9\u00ca\u0005\'\u0000\u0000\u00ca"+
		"\u00d2\u00036\u001b\u0000\u00cb\u00cc\u0005(\u0000\u0000\u00cc\u00cd\u0005"+
		"g\u0000\u0000\u00cd\u00ce\u0003J%\u0000\u00ce\u00cf\u0005h\u0000\u0000"+
		"\u00cf\u00d2\u0001\u0000\u0000\u0000\u00d0\u00d2\u0001\u0000\u0000\u0000"+
		"\u00d1\u00c9\u0001\u0000\u0000\u0000\u00d1\u00cb\u0001\u0000\u0000\u0000"+
		"\u00d1\u00d0\u0001\u0000\u0000\u0000\u00d2\u0017\u0001\u0000\u0000\u0000"+
		"\u00d3\u00d4\u0005\u0003\u0000\u0000\u00d4\u00d5\u00036\u001b\u0000\u00d5"+
		"\u0019\u0001\u0000\u0000\u0000\u00d6\u00d7\u0005\u0018\u0000\u0000\u00d7"+
		"\u00d8\u0005\u0017\u0000\u0000\u00d8\u00dd\u00036\u001b\u0000\u00d9\u00da"+
		"\u0005m\u0000\u0000\u00da\u00dc\u00036\u001b\u0000\u00db\u00d9\u0001\u0000"+
		"\u0000\u0000\u00dc\u00df\u0001\u0000\u0000\u0000\u00dd\u00db\u0001\u0000"+
		"\u0000\u0000\u00dd\u00de\u0001\u0000\u0000\u0000\u00de\u001b\u0001\u0000"+
		"\u0000\u0000\u00df\u00dd\u0001\u0000\u0000\u0000\u00e0\u00e1\u0005\u0019"+
		"\u0000\u0000\u00e1\u00e2\u00036\u001b\u0000\u00e2\u001d\u0001\u0000\u0000"+
		"\u0000\u00e3\u00e4\u0005\u0016\u0000\u0000\u00e4\u00e5\u0005\u0017\u0000"+
		"\u0000\u00e5\u00ea\u0003 \u0010\u0000\u00e6\u00e7\u0005m\u0000\u0000\u00e7"+
		"\u00e9\u0003 \u0010\u0000\u00e8\u00e6\u0001\u0000\u0000\u0000\u00e9\u00ec"+
		"\u0001\u0000\u0000\u0000\u00ea\u00e8\u0001\u0000\u0000\u0000\u00ea\u00eb"+
		"\u0001\u0000\u0000\u0000\u00eb\u001f\u0001\u0000\u0000\u0000\u00ec\u00ea"+
		"\u0001\u0000\u0000\u0000\u00ed\u00ef\u00036\u001b\u0000\u00ee\u00f0\u0007"+
		"\u0001\u0000\u0000\u00ef\u00ee\u0001\u0000\u0000\u0000\u00ef\u00f0\u0001"+
		"\u0000\u0000\u0000\u00f0!\u0001\u0000\u0000\u0000\u00f1\u00f2\u0005\u001a"+
		"\u0000\u0000\u00f2\u00f5\u00036\u001b\u0000\u00f3\u00f4\u0005\u001b\u0000"+
		"\u0000\u00f4\u00f6\u00036\u001b\u0000\u00f5\u00f3\u0001\u0000\u0000\u0000"+
		"\u00f5\u00f6\u0001\u0000\u0000\u0000\u00f6#\u0001\u0000\u0000\u0000\u00f7"+
		"\u00f8\u0005\u0004\u0000\u0000\u00f8\u00f9\u0005\u0005\u0000\u0000\u00f9"+
		"\u00fe\u0003@ \u0000\u00fa\u00fb\u0005g\u0000\u0000\u00fb\u00fc\u0003"+
		"J%\u0000\u00fc\u00fd\u0005h\u0000\u0000\u00fd\u00ff\u0001\u0000\u0000"+
		"\u0000\u00fe\u00fa\u0001\u0000\u0000\u0000\u00fe\u00ff\u0001\u0000\u0000"+
		"\u0000\u00ff\u010a\u0001\u0000\u0000\u0000\u0100\u0101\u0005\u0006\u0000"+
		"\u0000\u0101\u0106\u0003&\u0013\u0000\u0102\u0103\u0005m\u0000\u0000\u0103"+
		"\u0105\u0003&\u0013\u0000\u0104\u0102\u0001\u0000\u0000\u0000\u0105\u0108"+
		"\u0001\u0000\u0000\u0000\u0106\u0104\u0001\u0000\u0000\u0000\u0106\u0107"+
		"\u0001\u0000\u0000\u0000\u0107\u010b\u0001\u0000\u0000\u0000\u0108\u0106"+
		"\u0001\u0000\u0000\u0000\u0109\u010b\u0003\u0004\u0002\u0000\u010a\u0100"+
		"\u0001\u0000\u0000\u0000\u010a\u0109\u0001\u0000\u0000\u0000\u010b%\u0001"+
		"\u0000\u0000\u0000\u010c\u010d\u0005g\u0000\u0000\u010d\u0112\u00036\u001b"+
		"\u0000\u010e\u010f\u0005m\u0000\u0000\u010f\u0111\u00036\u001b\u0000\u0110"+
		"\u010e\u0001\u0000\u0000\u0000\u0111\u0114\u0001\u0000\u0000\u0000\u0112"+
		"\u0110\u0001\u0000\u0000\u0000\u0112\u0113\u0001\u0000\u0000\u0000\u0113"+
		"\u0115\u0001\u0000\u0000\u0000\u0114\u0112\u0001\u0000\u0000\u0000\u0115"+
		"\u0116\u0005h\u0000\u0000\u0116\'\u0001\u0000\u0000\u0000\u0117\u0118"+
		"\u0005\u0007\u0000\u0000\u0118\u0119\u0003@ \u0000\u0119\u011a\u0005\b"+
		"\u0000\u0000\u011a\u011f\u0003*\u0015\u0000\u011b\u011c\u0005m\u0000\u0000"+
		"\u011c\u011e\u0003*\u0015\u0000\u011d\u011b\u0001\u0000\u0000\u0000\u011e"+
		"\u0121\u0001\u0000\u0000\u0000\u011f\u011d\u0001\u0000\u0000\u0000\u011f"+
		"\u0120\u0001\u0000\u0000\u0000\u0120\u0123\u0001\u0000\u0000\u0000\u0121"+
		"\u011f\u0001\u0000\u0000\u0000\u0122\u0124\u0003\u0018\f\u0000\u0123\u0122"+
		"\u0001\u0000\u0000\u0000\u0123\u0124\u0001\u0000\u0000\u0000\u0124)\u0001"+
		"\u0000\u0000\u0000\u0125\u0126\u0003B!\u0000\u0126\u0127\u0005S\u0000"+
		"\u0000\u0127\u0128\u00036\u001b\u0000\u0128+\u0001\u0000\u0000\u0000\u0129"+
		"\u012a\u0005\t\u0000\u0000\u012a\u012b\u0005\u0002\u0000\u0000\u012b\u012d"+
		"\u0003@ \u0000\u012c\u012e\u0003\u0018\f\u0000\u012d\u012c\u0001\u0000"+
		"\u0000\u0000\u012d\u012e\u0001\u0000\u0000\u0000\u012e-\u0001\u0000\u0000"+
		"\u0000\u012f\u0130\u0005\n\u0000\u0000\u0130\u0131\u0005\u000b\u0000\u0000"+
		"\u0131\u0132\u0003@ \u0000\u0132\u0133\u0005g\u0000\u0000\u0133\u0138"+
		"\u00030\u0018\u0000\u0134\u0135\u0005m\u0000\u0000\u0135\u0137\u00030"+
		"\u0018\u0000\u0136\u0134\u0001\u0000\u0000\u0000\u0137\u013a\u0001\u0000"+
		"\u0000\u0000\u0138\u0136\u0001\u0000\u0000\u0000\u0138\u0139\u0001\u0000"+
		"\u0000\u0000\u0139\u013b\u0001\u0000\u0000\u0000\u013a\u0138\u0001\u0000"+
		"\u0000\u0000\u013b\u013c\u0005h\u0000\u0000\u013c/\u0001\u0000\u0000\u0000"+
		"\u013d\u013e\u0003B!\u0000\u013e\u0142\u0003L&\u0000\u013f\u0141\u0003"+
		"2\u0019\u0000\u0140\u013f\u0001\u0000\u0000\u0000\u0141\u0144\u0001\u0000"+
		"\u0000\u0000\u0142\u0140\u0001\u0000\u0000\u0000\u0142\u0143\u0001\u0000"+
		"\u0000\u0000\u01431\u0001\u0000\u0000\u0000\u0144\u0142\u0001\u0000\u0000"+
		"\u0000\u0145\u0146\u0005\u000f\u0000\u0000\u0146\u014c\u0005\u0010\u0000"+
		"\u0000\u0147\u014c\u0005\u0010\u0000\u0000\u0148\u0149\u00054\u0000\u0000"+
		"\u0149\u014c\u00055\u0000\u0000\u014a\u014c\u00056\u0000\u0000\u014b\u0145"+
		"\u0001\u0000\u0000\u0000\u014b\u0147\u0001\u0000\u0000\u0000\u014b\u0148"+
		"\u0001\u0000\u0000\u0000\u014b\u014a\u0001\u0000\u0000\u0000\u014c3\u0001"+
		"\u0000\u0000\u0000\u014d\u014e\u0005\f\u0000\u0000\u014e\u014f\u0005\u000b"+
		"\u0000\u0000\u014f\u0150\u0003@ \u0000\u01505\u0001\u0000\u0000\u0000"+
		"\u0151\u0152\u0006\u001b\uffff\uffff\u0000\u0152\u0171\u0003<\u001e\u0000"+
		"\u0153\u0171\u0003>\u001f\u0000\u0154\u0171\u0003F#\u0000\u0155\u0156"+
		"\u0005g\u0000\u0000\u0156\u0157\u00036\u001b\u0000\u0157\u0158\u0005h"+
		"\u0000\u0000\u0158\u0171\u0001\u0000\u0000\u0000\u0159\u015a\u0005g\u0000"+
		"\u0000\u015a\u015b\u0003\u0004\u0002\u0000\u015b\u015c\u0005h\u0000\u0000"+
		"\u015c\u0171\u0001\u0000\u0000\u0000\u015d\u015e\u0005\u000f\u0000\u0000"+
		"\u015e\u0171\u00036\u001b\b\u015f\u0161\u0005,\u0000\u0000\u0160\u0162"+
		"\u00038\u001c\u0000\u0161\u0160\u0001\u0000\u0000\u0000\u0162\u0163\u0001"+
		"\u0000\u0000\u0000\u0163\u0161\u0001\u0000\u0000\u0000\u0163\u0164\u0001"+
		"\u0000\u0000\u0000\u0164\u0167\u0001\u0000\u0000\u0000\u0165\u0166\u0005"+
		"/\u0000\u0000\u0166\u0168\u00036\u001b\u0000\u0167\u0165\u0001\u0000\u0000"+
		"\u0000\u0167\u0168\u0001\u0000\u0000\u0000\u0168\u0169\u0001\u0000\u0000"+
		"\u0000\u0169\u016a\u00050\u0000\u0000\u016a\u0171\u0001\u0000\u0000\u0000"+
		"\u016b\u016c\u00051\u0000\u0000\u016c\u016d\u0005g\u0000\u0000\u016d\u016e"+
		"\u0003\u0004\u0002\u0000\u016e\u016f\u0005h\u0000\u0000\u016f\u0171\u0001"+
		"\u0000\u0000\u0000\u0170\u0151\u0001\u0000\u0000\u0000\u0170\u0153\u0001"+
		"\u0000\u0000\u0000\u0170\u0154\u0001\u0000\u0000\u0000\u0170\u0155\u0001"+
		"\u0000\u0000\u0000\u0170\u0159\u0001\u0000\u0000\u0000\u0170\u015d\u0001"+
		"\u0000\u0000\u0000\u0170\u015f\u0001\u0000\u0000\u0000\u0170\u016b\u0001"+
		"\u0000\u0000\u0000\u0171\u019f\u0001\u0000\u0000\u0000\u0172\u0173\n\t"+
		"\u0000\u0000\u0173\u0174\u0003:\u001d\u0000\u0174\u0175\u00036\u001b\n"+
		"\u0175\u019e\u0001\u0000\u0000\u0000\u0176\u0178\n\u0004\u0000\u0000\u0177"+
		"\u0179\u0005\u000f\u0000\u0000\u0178\u0177\u0001\u0000\u0000\u0000\u0178"+
		"\u0179\u0001\u0000\u0000\u0000\u0179\u017a\u0001\u0000\u0000\u0000\u017a"+
		"\u017b\u0005\u0013\u0000\u0000\u017b\u019e\u00036\u001b\u0005\u017c\u017e"+
		"\n\u0003\u0000\u0000\u017d\u017f\u0005\u000f\u0000\u0000\u017e\u017d\u0001"+
		"\u0000\u0000\u0000\u017e\u017f\u0001\u0000\u0000\u0000\u017f\u0180\u0001"+
		"\u0000\u0000\u0000\u0180\u0181\u0005\u0014\u0000\u0000\u0181\u0182\u0003"+
		"6\u001b\u0000\u0182\u0183\u0005\r\u0000\u0000\u0183\u0184\u00036\u001b"+
		"\u0004\u0184\u019e\u0001\u0000\u0000\u0000\u0185\u0186\n\u0007\u0000\u0000"+
		"\u0186\u0188\u0005\u0011\u0000\u0000\u0187\u0189\u0005\u000f\u0000\u0000"+
		"\u0188\u0187\u0001\u0000\u0000\u0000\u0188\u0189\u0001\u0000\u0000\u0000"+
		"\u0189\u018a\u0001\u0000\u0000\u0000\u018a\u019e\u0005\u0010\u0000\u0000"+
		"\u018b\u018d\n\u0006\u0000\u0000\u018c\u018e\u0005\u000f\u0000\u0000\u018d"+
		"\u018c\u0001\u0000\u0000\u0000\u018d\u018e\u0001\u0000\u0000\u0000\u018e"+
		"\u018f\u0001\u0000\u0000\u0000\u018f\u0190\u0005\u0012\u0000\u0000\u0190"+
		"\u0191\u0005g\u0000\u0000\u0191\u0192\u0003H$\u0000\u0192\u0193\u0005"+
		"h\u0000\u0000\u0193\u019e\u0001\u0000\u0000\u0000\u0194\u0196\n\u0005"+
		"\u0000\u0000\u0195\u0197\u0005\u000f\u0000\u0000\u0196\u0195\u0001\u0000"+
		"\u0000\u0000\u0196\u0197\u0001\u0000\u0000\u0000\u0197\u0198\u0001\u0000"+
		"\u0000\u0000\u0198\u0199\u0005\u0012\u0000\u0000\u0199\u019a\u0005g\u0000"+
		"\u0000\u019a\u019b\u0003\u0004\u0002\u0000\u019b\u019c\u0005h\u0000\u0000"+
		"\u019c\u019e\u0001\u0000\u0000\u0000\u019d\u0172\u0001\u0000\u0000\u0000"+
		"\u019d\u0176\u0001\u0000\u0000\u0000\u019d\u017c\u0001\u0000\u0000\u0000"+
		"\u019d\u0185\u0001\u0000\u0000\u0000\u019d\u018b\u0001\u0000\u0000\u0000"+
		"\u019d\u0194\u0001\u0000\u0000\u0000\u019e\u01a1\u0001\u0000\u0000\u0000"+
		"\u019f\u019d\u0001\u0000\u0000\u0000\u019f\u01a0\u0001\u0000\u0000\u0000"+
		"\u01a07\u0001\u0000\u0000\u0000\u01a1\u019f\u0001\u0000\u0000\u0000\u01a2"+
		"\u01a3\u0005-\u0000\u0000\u01a3\u01a4\u00036\u001b\u0000\u01a4\u01a5\u0005"+
		".\u0000\u0000\u01a5\u01a6\u00036\u001b\u0000\u01a69\u0001\u0000\u0000"+
		"\u0000\u01a7\u01a8\u0007\u0002\u0000\u0000\u01a8;\u0001\u0000\u0000\u0000"+
		"\u01a9\u01b1\u0005q\u0000\u0000\u01aa\u01b1\u0005t\u0000\u0000\u01ab\u01b1"+
		"\u0005u\u0000\u0000\u01ac\u01b1\u0005v\u0000\u0000\u01ad\u01b1\u00052"+
		"\u0000\u0000\u01ae\u01b1\u00053\u0000\u0000\u01af\u01b1\u0005\u0010\u0000"+
		"\u0000\u01b0\u01a9\u0001\u0000\u0000\u0000\u01b0\u01aa\u0001\u0000\u0000"+
		"\u0000\u01b0\u01ab\u0001\u0000\u0000\u0000\u01b0\u01ac\u0001\u0000\u0000"+
		"\u0000\u01b0\u01ad\u0001\u0000\u0000\u0000\u01b0\u01ae\u0001\u0000\u0000"+
		"\u0000\u01b0\u01af\u0001\u0000\u0000\u0000\u01b1=\u0001\u0000\u0000\u0000"+
		"\u01b2\u01b8\u0003B!\u0000\u01b3\u01b4\u0003@ \u0000\u01b4\u01b5\u0005"+
		"o\u0000\u0000\u01b5\u01b6\u0003B!\u0000\u01b6\u01b8\u0001\u0000\u0000"+
		"\u0000\u01b7\u01b2\u0001\u0000\u0000\u0000\u01b7\u01b3\u0001\u0000\u0000"+
		"\u0000\u01b8?\u0001\u0000\u0000\u0000\u01b9\u01ba\u0003N\'\u0000\u01ba"+
		"A\u0001\u0000\u0000\u0000\u01bb\u01bc\u0003N\'\u0000\u01bcC\u0001\u0000"+
		"\u0000\u0000\u01bd\u01be\u0003N\'\u0000\u01beE\u0001\u0000\u0000\u0000"+
		"\u01bf\u01c0\u0005K\u0000\u0000\u01c0\u01c1\u0005g\u0000\u0000\u01c1\u01f1"+
		"\u0005h\u0000\u0000\u01c2\u01c3\u0005L\u0000\u0000\u01c3\u01c4\u0005g"+
		"\u0000\u0000\u01c4\u01f1\u0005h\u0000\u0000\u01c5\u01c6\u0005M\u0000\u0000"+
		"\u01c6\u01c7\u0005g\u0000\u0000\u01c7\u01f1\u0005h\u0000\u0000\u01c8\u01c9"+
		"\u0005N\u0000\u0000\u01c9\u01cc\u0005g\u0000\u0000\u01ca\u01cd\u0005["+
		"\u0000\u0000\u01cb\u01cd\u00036\u001b\u0000\u01cc\u01ca\u0001\u0000\u0000"+
		"\u0000\u01cc\u01cb\u0001\u0000\u0000\u0000\u01cd\u01ce\u0001\u0000\u0000"+
		"\u0000\u01ce\u01f1\u0005h\u0000\u0000\u01cf\u01d0\u0005O\u0000\u0000\u01d0"+
		"\u01d1\u0005g\u0000\u0000\u01d1\u01d2\u00036\u001b\u0000\u01d2\u01d3\u0005"+
		"h\u0000\u0000\u01d3\u01f1\u0001\u0000\u0000\u0000\u01d4\u01d5\u0005P\u0000"+
		"\u0000\u01d5\u01d6\u0005g\u0000\u0000\u01d6\u01d7\u00036\u001b\u0000\u01d7"+
		"\u01d8\u0005h\u0000\u0000\u01d8\u01f1\u0001\u0000\u0000\u0000\u01d9\u01da"+
		"\u0005Q\u0000\u0000\u01da\u01db\u0005g\u0000\u0000\u01db\u01dc\u00036"+
		"\u001b\u0000\u01dc\u01dd\u0005h\u0000\u0000\u01dd\u01f1\u0001\u0000\u0000"+
		"\u0000\u01de\u01df\u0005R\u0000\u0000\u01df\u01e0\u0005g\u0000\u0000\u01e0"+
		"\u01e1\u00036\u001b\u0000\u01e1\u01e2\u0005h\u0000\u0000\u01e2\u01f1\u0001"+
		"\u0000\u0000\u0000\u01e3\u01e4\u0005N\u0000\u0000\u01e4\u01e5\u0005g\u0000"+
		"\u0000\u01e5\u01e6\u0005\u001c\u0000\u0000\u01e6\u01e7\u00036\u001b\u0000"+
		"\u01e7\u01e8\u0005h\u0000\u0000\u01e8\u01f1\u0001\u0000\u0000\u0000\u01e9"+
		"\u01ea\u0003N\'\u0000\u01ea\u01ec\u0005g\u0000\u0000\u01eb\u01ed\u0003"+
		"H$\u0000\u01ec\u01eb\u0001\u0000\u0000\u0000\u01ec\u01ed\u0001\u0000\u0000"+
		"\u0000\u01ed\u01ee\u0001\u0000\u0000\u0000\u01ee\u01ef\u0005h\u0000\u0000"+
		"\u01ef\u01f1\u0001\u0000\u0000\u0000\u01f0\u01bf\u0001\u0000\u0000\u0000"+
		"\u01f0\u01c2\u0001\u0000\u0000\u0000\u01f0\u01c5\u0001\u0000\u0000\u0000"+
		"\u01f0\u01c8\u0001\u0000\u0000\u0000\u01f0\u01cf\u0001\u0000\u0000\u0000"+
		"\u01f0\u01d4\u0001\u0000\u0000\u0000\u01f0\u01d9\u0001\u0000\u0000\u0000"+
		"\u01f0\u01de\u0001\u0000\u0000\u0000\u01f0\u01e3\u0001\u0000\u0000\u0000"+
		"\u01f0\u01e9\u0001\u0000\u0000\u0000\u01f1G\u0001\u0000\u0000\u0000\u01f2"+
		"\u01f7\u00036\u001b\u0000\u01f3\u01f4\u0005m\u0000\u0000\u01f4\u01f6\u0003"+
		"6\u001b\u0000\u01f5\u01f3\u0001\u0000\u0000\u0000\u01f6\u01f9\u0001\u0000"+
		"\u0000\u0000\u01f7\u01f5\u0001\u0000\u0000\u0000\u01f7\u01f8\u0001\u0000"+
		"\u0000\u0000\u01f8I\u0001\u0000\u0000\u0000\u01f9\u01f7\u0001\u0000\u0000"+
		"\u0000\u01fa\u01ff\u0003B!\u0000\u01fb\u01fc\u0005m\u0000\u0000\u01fc"+
		"\u01fe\u0003B!\u0000\u01fd\u01fb\u0001\u0000\u0000\u0000\u01fe\u0201\u0001"+
		"\u0000\u0000\u0000\u01ff\u01fd\u0001\u0000\u0000\u0000\u01ff\u0200\u0001"+
		"\u0000\u0000\u0000\u0200K\u0001\u0000\u0000\u0000\u0201\u01ff\u0001\u0000"+
		"\u0000\u0000\u0202\u0232\u00057\u0000\u0000\u0203\u0232\u0007\u0003\u0000"+
		"\u0000\u0204\u0232\u0005:\u0000\u0000\u0205\u020d\u0005;\u0000\u0000\u0206"+
		"\u0207\u0005g\u0000\u0000\u0207\u020a\u0005t\u0000\u0000\u0208\u0209\u0005"+
		"m\u0000\u0000\u0209\u020b\u0005t\u0000\u0000\u020a\u0208\u0001\u0000\u0000"+
		"\u0000\u020a\u020b\u0001\u0000\u0000\u0000\u020b\u020c\u0001\u0000\u0000"+
		"\u0000\u020c\u020e\u0005h\u0000\u0000\u020d\u0206\u0001\u0000\u0000\u0000"+
		"\u020d\u020e\u0001\u0000\u0000\u0000\u020e\u0232\u0001\u0000\u0000\u0000"+
		"\u020f\u0217\u0005<\u0000\u0000\u0210\u0211\u0005g\u0000\u0000\u0211\u0214"+
		"\u0005t\u0000\u0000\u0212\u0213\u0005m\u0000\u0000\u0213\u0215\u0005t"+
		"\u0000\u0000\u0214\u0212\u0001\u0000\u0000\u0000\u0214\u0215\u0001\u0000"+
		"\u0000\u0000\u0215\u0216\u0001\u0000\u0000\u0000\u0216\u0218\u0005h\u0000"+
		"\u0000\u0217\u0210\u0001\u0000\u0000\u0000\u0217\u0218\u0001\u0000\u0000"+
		"\u0000\u0218\u0232\u0001\u0000\u0000\u0000\u0219\u0232\u0005=\u0000\u0000"+
		"\u021a\u021b\u0005>\u0000\u0000\u021b\u0232\u0005?\u0000\u0000\u021c\u0220"+
		"\u0005@\u0000\u0000\u021d\u021e\u0005g\u0000\u0000\u021e\u021f\u0005t"+
		"\u0000\u0000\u021f\u0221\u0005h\u0000\u0000\u0220\u021d\u0001\u0000\u0000"+
		"\u0000\u0220\u0221\u0001\u0000\u0000\u0000\u0221\u0232\u0001\u0000\u0000"+
		"\u0000\u0222\u0226\u0005A\u0000\u0000\u0223\u0224\u0005g\u0000\u0000\u0224"+
		"\u0225\u0005t\u0000\u0000\u0225\u0227\u0005h\u0000\u0000\u0226\u0223\u0001"+
		"\u0000\u0000\u0000\u0226\u0227\u0001\u0000\u0000\u0000\u0227\u0232\u0001"+
		"\u0000\u0000\u0000\u0228\u0232\u0005B\u0000\u0000\u0229\u0232\u0005C\u0000"+
		"\u0000\u022a\u0232\u0005D\u0000\u0000\u022b\u0232\u0005E\u0000\u0000\u022c"+
		"\u0232\u0005F\u0000\u0000\u022d\u0232\u0005G\u0000\u0000\u022e\u0232\u0005"+
		"H\u0000\u0000\u022f\u0232\u0005I\u0000\u0000\u0230\u0232\u0005J\u0000"+
		"\u0000\u0231\u0202\u0001\u0000\u0000\u0000\u0231\u0203\u0001\u0000\u0000"+
		"\u0000\u0231\u0204\u0001\u0000\u0000\u0000\u0231\u0205\u0001\u0000\u0000"+
		"\u0000\u0231\u020f\u0001\u0000\u0000\u0000\u0231\u0219\u0001\u0000\u0000"+
		"\u0000\u0231\u021a\u0001\u0000\u0000\u0000\u0231\u021c\u0001\u0000\u0000"+
		"\u0000\u0231\u0222\u0001\u0000\u0000\u0000\u0231\u0228\u0001\u0000\u0000"+
		"\u0000\u0231\u0229\u0001\u0000\u0000\u0000\u0231\u022a\u0001\u0000\u0000"+
		"\u0000\u0231\u022b\u0001\u0000\u0000\u0000\u0231\u022c\u0001\u0000\u0000"+
		"\u0000\u0231\u022d\u0001\u0000\u0000\u0000\u0231\u022e\u0001\u0000\u0000"+
		"\u0000\u0231\u022f\u0001\u0000\u0000\u0000\u0231\u0230\u0001\u0000\u0000"+
		"\u0000\u0232M\u0001\u0000\u0000\u0000\u0233\u0234\u0007\u0004\u0000\u0000"+
		"\u0234O\u0001\u0000\u0000\u0000AUYcglorux{\u0084\u0088\u008c\u008f\u0096"+
		"\u009d\u00a2\u00a5\u00ab\u00ae\u00b0\u00b8\u00bc\u00c0\u00c4\u00c7\u00d1"+
		"\u00dd\u00ea\u00ef\u00f5\u00fe\u0106\u010a\u0112\u011f\u0123\u012d\u0138"+
		"\u0142\u014b\u0163\u0167\u0170\u0178\u017e\u0188\u018d\u0196\u019d\u019f"+
		"\u01b0\u01b7\u01cc\u01ec\u01f0\u01f7\u01ff\u020a\u020d\u0214\u0217\u0220"+
		"\u0226\u0231";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}