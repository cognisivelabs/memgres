package com.memgres.testing;

import java.sql.*;

/**
 * DatabaseMetaData implementation for MemGres testing.
 * 
 * <p>This provides basic database metadata for JDBC compliance.
 * Many methods return simplified or default values suitable for testing.</p>
 * 
 * @since 1.0.0
 */
public class MemGresTestDatabaseMetaData implements DatabaseMetaData {
    
    private final Connection connection;
    
    public MemGresTestDatabaseMetaData(Connection connection) {
        this.connection = connection;
    }
    
    @Override
    public boolean allProceduresAreCallable() throws SQLException {
        return false;
    }
    
    @Override
    public boolean allTablesAreSelectable() throws SQLException {
        return true;
    }
    
    @Override
    public String getURL() throws SQLException {
        return "jdbc:memgres:mem:test";
    }
    
    @Override
    public String getUserName() throws SQLException {
        return "test";
    }
    
    @Override
    public boolean isReadOnly() throws SQLException {
        return false;
    }
    
    @Override
    public boolean nullsAreSortedHigh() throws SQLException {
        return true; // PostgreSQL behavior
    }
    
    @Override
    public boolean nullsAreSortedLow() throws SQLException {
        return false;
    }
    
    @Override
    public boolean nullsAreSortedAtStart() throws SQLException {
        return false;
    }
    
    @Override
    public boolean nullsAreSortedAtEnd() throws SQLException {
        return true; // PostgreSQL behavior
    }
    
    @Override
    public String getDatabaseProductName() throws SQLException {
        return "MemGres";
    }
    
    @Override
    public String getDatabaseProductVersion() throws SQLException {
        return "1.0.0-SNAPSHOT";
    }
    
    @Override
    public String getDriverName() throws SQLException {
        return "MemGres Test Driver";
    }
    
    @Override
    public String getDriverVersion() throws SQLException {
        return "1.0.0-SNAPSHOT";
    }
    
    @Override
    public int getDriverMajorVersion() {
        return 1;
    }
    
    @Override
    public int getDriverMinorVersion() {
        return 0;
    }
    
    @Override
    public boolean usesLocalFiles() throws SQLException {
        return false; // In-memory database
    }
    
    @Override
    public boolean usesLocalFilePerTable() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        return false; // PostgreSQL-compatible (lowercase)
    }
    
    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException {
        return false;
    }
    
    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException {
        return true; // PostgreSQL behavior
    }
    
    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        return true; // PostgreSQL behavior
    }
    
    @Override
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        return false;
    }
    
    @Override
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        return false;
    }
    
    @Override
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        return true; // PostgreSQL behavior
    }
    
    @Override
    public String getIdentifierQuoteString() throws SQLException {
        return "\""; // PostgreSQL quote character
    }
    
    @Override
    public String getSQLKeywords() throws SQLException {
        return "SELECT,INSERT,UPDATE,DELETE,CREATE,DROP,TABLE,FROM,WHERE,JOIN,INNER,LEFT,RIGHT,FULL,OUTER,GROUP,BY,HAVING,ORDER,LIMIT";
    }
    
    @Override
    public String getNumericFunctions() throws SQLException {
        return "COUNT,SUM,AVG,MIN,MAX";
    }
    
    @Override
    public String getStringFunctions() throws SQLException {
        return "";
    }
    
    @Override
    public String getSystemFunctions() throws SQLException {
        return "gen_random_uuid,uuid_generate_v1,uuid_generate_v4";
    }
    
    @Override
    public String getTimeDateFunctions() throws SQLException {
        return "";
    }
    
    @Override
    public String getSearchStringEscape() throws SQLException {
        return "\\";
    }
    
    @Override
    public String getExtraNameCharacters() throws SQLException {
        return "";
    }
    
    @Override
    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        return false; // Not implemented yet
    }
    
    @Override
    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        return false; // Not implemented yet
    }
    
    @Override
    public boolean supportsColumnAliasing() throws SQLException {
        return true;
    }
    
    @Override
    public boolean nullPlusNonNullIsNull() throws SQLException {
        return true; // SQL standard behavior
    }
    
    @Override
    public boolean supportsConvert() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsConvert(int fromType, int toType) throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsTableCorrelationNames() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsDifferentTableCorrelationNames() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsExpressionsInOrderBy() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsOrderByUnrelated() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsGroupBy() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsGroupByUnrelated() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsGroupByBeyondSelect() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsLikeEscapeClause() throws SQLException {
        return false; // Not implemented yet
    }
    
    @Override
    public boolean supportsMultipleResultSets() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsMultipleTransactions() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsNonNullableColumns() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsMinimumSQLGrammar() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsCoreSQLGrammar() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsExtendedSQLGrammar() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsANSI92FullSQL() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsOuterJoins() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsFullOuterJoins() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsLimitedOuterJoins() throws SQLException {
        return true;
    }
    
    @Override
    public String getSchemaTerm() throws SQLException {
        return "schema";
    }
    
    @Override
    public String getProcedureTerm() throws SQLException {
        return "function";
    }
    
    @Override
    public String getCatalogTerm() throws SQLException {
        return "database";
    }
    
    @Override
    public boolean isCatalogAtStart() throws SQLException {
        return true;
    }
    
    @Override
    public String getCatalogSeparator() throws SQLException {
        return ".";
    }
    
    @Override
    public boolean supportsSchemasInDataManipulation() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsPositionedDelete() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsPositionedUpdate() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsSelectForUpdate() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsStoredProcedures() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsSubqueriesInComparisons() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsSubqueriesInExists() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsSubqueriesInIns() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsCorrelatedSubqueries() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsUnion() throws SQLException {
        return false; // Not implemented yet
    }
    
    @Override
    public boolean supportsUnionAll() throws SQLException {
        return false; // Not implemented yet
    }
    
    @Override
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        return true;
    }
    
    @Override
    public int getMaxBinaryLiteralLength() throws SQLException {
        return 0; // No limit
    }
    
    @Override
    public int getMaxCharLiteralLength() throws SQLException {
        return 0; // No limit
    }
    
    @Override
    public int getMaxColumnNameLength() throws SQLException {
        return 63; // PostgreSQL limit
    }
    
    @Override
    public int getMaxColumnsInGroupBy() throws SQLException {
        return 0; // No limit
    }
    
    @Override
    public int getMaxColumnsInIndex() throws SQLException {
        return 0; // No limit
    }
    
    @Override
    public int getMaxColumnsInOrderBy() throws SQLException {
        return 0; // No limit
    }
    
    @Override
    public int getMaxColumnsInSelect() throws SQLException {
        return 0; // No limit
    }
    
    @Override
    public int getMaxColumnsInTable() throws SQLException {
        return 0; // No limit
    }
    
    @Override
    public int getMaxConnections() throws SQLException {
        return 0; // No limit
    }
    
    @Override
    public int getMaxCursorNameLength() throws SQLException {
        return 0;
    }
    
    @Override
    public int getMaxIndexLength() throws SQLException {
        return 0; // No limit
    }
    
    @Override
    public int getMaxSchemaNameLength() throws SQLException {
        return 63; // PostgreSQL limit
    }
    
    @Override
    public int getMaxProcedureNameLength() throws SQLException {
        return 63; // PostgreSQL limit
    }
    
    @Override
    public int getMaxCatalogNameLength() throws SQLException {
        return 63; // PostgreSQL limit
    }
    
    @Override
    public int getMaxRowSize() throws SQLException {
        return 0; // No limit
    }
    
    @Override
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        return true;
    }
    
    @Override
    public int getMaxStatementLength() throws SQLException {
        return 0; // No limit
    }
    
    @Override
    public int getMaxStatements() throws SQLException {
        return 0; // No limit
    }
    
    @Override
    public int getMaxTableNameLength() throws SQLException {
        return 63; // PostgreSQL limit
    }
    
    @Override
    public int getMaxTablesInSelect() throws SQLException {
        return 0; // No limit
    }
    
    @Override
    public int getMaxUserNameLength() throws SQLException {
        return 63; // PostgreSQL limit
    }
    
    @Override
    public int getDefaultTransactionIsolation() throws SQLException {
        return Connection.TRANSACTION_READ_COMMITTED;
    }
    
    @Override
    public boolean supportsTransactions() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
        return level == Connection.TRANSACTION_READ_UNCOMMITTED ||
               level == Connection.TRANSACTION_READ_COMMITTED ||
               level == Connection.TRANSACTION_REPEATABLE_READ ||
               level == Connection.TRANSACTION_SERIALIZABLE;
    }
    
    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
        return false;
    }
    
    @Override
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        return false;
    }
    
    @Override
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        return false;
    }
    
    // Most metadata query methods return empty result sets for simplicity
    @Override
    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException {
        return createEmptyResultSet();
    }
    
    @Override
    public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException {
        return createEmptyResultSet();
    }
    
    @Override
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException {
        return createEmptyResultSet();
    }
    
    @Override
    public ResultSet getSchemas() throws SQLException {
        return createEmptyResultSet();
    }
    
    @Override
    public ResultSet getCatalogs() throws SQLException {
        return createEmptyResultSet();
    }
    
    @Override
    public ResultSet getTableTypes() throws SQLException {
        return createEmptyResultSet();
    }
    
    @Override
    public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        return createEmptyResultSet();
    }
    
    @Override
    public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException {
        return createEmptyResultSet();
    }
    
    @Override
    public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        return createEmptyResultSet();
    }
    
    @Override
    public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) throws SQLException {
        return createEmptyResultSet();
    }
    
    @Override
    public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException {
        return createEmptyResultSet();
    }
    
    @Override
    public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException {
        return createEmptyResultSet();
    }
    
    @Override
    public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
        return createEmptyResultSet();
    }
    
    @Override
    public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
        return createEmptyResultSet();
    }
    
    @Override
    public ResultSet getCrossReference(String parentCatalog, String parentSchema, String parentTable, String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException {
        return createEmptyResultSet();
    }
    
    @Override
    public ResultSet getTypeInfo() throws SQLException {
        return createEmptyResultSet();
    }
    
    @Override
    public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException {
        return createEmptyResultSet();
    }
    
    @Override
    public boolean supportsResultSetType(int type) throws SQLException {
        return type == ResultSet.TYPE_FORWARD_ONLY;
    }
    
    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
        return type == ResultSet.TYPE_FORWARD_ONLY && concurrency == ResultSet.CONCUR_READ_ONLY;
    }
    
    @Override
    public boolean ownUpdatesAreVisible(int type) throws SQLException {
        return false;
    }
    
    @Override
    public boolean ownDeletesAreVisible(int type) throws SQLException {
        return false;
    }
    
    @Override
    public boolean ownInsertsAreVisible(int type) throws SQLException {
        return false;
    }
    
    @Override
    public boolean othersUpdatesAreVisible(int type) throws SQLException {
        return false;
    }
    
    @Override
    public boolean othersDeletesAreVisible(int type) throws SQLException {
        return false;
    }
    
    @Override
    public boolean othersInsertsAreVisible(int type) throws SQLException {
        return false;
    }
    
    @Override
    public boolean updatesAreDetected(int type) throws SQLException {
        return false;
    }
    
    @Override
    public boolean deletesAreDetected(int type) throws SQLException {
        return false;
    }
    
    @Override
    public boolean insertsAreDetected(int type) throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsBatchUpdates() throws SQLException {
        return false;
    }
    
    @Override
    public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) throws SQLException {
        return createEmptyResultSet();
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        return connection;
    }
    
    // JDBC 3.0+ methods - mostly unsupported or return defaults
    
    @Override
    public boolean supportsSavepoints() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsNamedParameters() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsMultipleOpenResults() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsGetGeneratedKeys() throws SQLException {
        return false;
    }
    
    @Override
    public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) throws SQLException {
        return createEmptyResultSet();
    }
    
    @Override
    public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        return createEmptyResultSet();
    }
    
    @Override
    public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern) throws SQLException {
        return createEmptyResultSet();
    }
    
    @Override
    public boolean supportsResultSetHoldability(int holdability) throws SQLException {
        return holdability == ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }
    
    @Override
    public int getResultSetHoldability() throws SQLException {
        return ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }
    
    @Override
    public int getDatabaseMajorVersion() throws SQLException {
        return 1;
    }
    
    @Override
    public int getDatabaseMinorVersion() throws SQLException {
        return 0;
    }
    
    @Override
    public int getJDBCMajorVersion() throws SQLException {
        return 4;
    }
    
    @Override
    public int getJDBCMinorVersion() throws SQLException {
        return 2;
    }
    
    @Override
    public int getSQLStateType() throws SQLException {
        return DatabaseMetaData.sqlStateSQL;
    }
    
    @Override
    public boolean locatorsUpdateCopy() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsStatementPooling() throws SQLException {
        return false;
    }
    
    // JDBC 4.0+ methods
    
    @Override
    public RowIdLifetime getRowIdLifetime() throws SQLException {
        return RowIdLifetime.ROWID_UNSUPPORTED;
    }
    
    @Override
    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
        return createEmptyResultSet();
    }
    
    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
        return false;
    }
    
    @Override
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
        return false;
    }
    
    @Override
    public ResultSet getClientInfoProperties() throws SQLException {
        return createEmptyResultSet();
    }
    
    @Override
    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException {
        return createEmptyResultSet();
    }
    
    @Override
    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern) throws SQLException {
        return createEmptyResultSet();
    }
    
    // JDBC 4.1 methods
    
    @Override
    public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        return createEmptyResultSet();
    }
    
    @Override
    public boolean generatedKeyAlwaysReturned() throws SQLException {
        return false;
    }
    
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isAssignableFrom(getClass())) {
            return iface.cast(this);
        }
        throw new SQLException("Cannot unwrap to " + iface.getName());
    }
    
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(getClass());
    }
    
    private ResultSet createEmptyResultSet() {
        return new MemGresTestResultSet(null, null);
    }
}