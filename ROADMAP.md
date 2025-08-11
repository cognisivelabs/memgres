# MemGres Roadmap

## Mission Statement

**MemGres aims to be a drop-in replacement for H2 database with full PostgreSQL JSONB support**, solving the "testing vs. production SQL compatibility gap" for modern Java applications.

---

## Current Status: **Phase 2 Complete** ✅

**Overall Progress**: 417/417 tests passing (100%)  
**H2 Compatibility**: ~40% (basic SQL operations complete)  
**PostgreSQL JSONB**: 100% (full operator and function support)  
**Testing Integration**: 100% (JUnit 5, TestNG, Spring Test)

---

## Completed Phases

### ✅ Phase 0: Foundation (Complete)
- Core database engine with thread-safe operations
- ACID transaction management with four isolation levels  
- B+ tree indexing with range query optimization
- ANTLR4-based SQL parser with AST architecture

### ✅ Phase 1: Core SQL Engine (Complete)
- **DDL**: `CREATE TABLE`, `DROP TABLE`
- **DML**: `SELECT`, `INSERT`, `UPDATE`, `DELETE`
- **Queries**: JOINs (INNER, LEFT, RIGHT, FULL OUTER), subqueries, aggregation
- **Data Types**: INTEGER, VARCHAR, TEXT, BOOLEAN, DATE, TIME, TIMESTAMP, UUID
- **Functions**: String (CONCAT, SUBSTRING, TRIM), Date/Time (NOW, EXTRACT), UUID generation

### ✅ Phase 2: JSONB & Testing Integration (Complete)
- **JSONB Support**: Full PostgreSQL compatibility with all operators (`@>`, `?`, `->`, `->>`, `#>`, `#>>`)
- **JSONB Functions**: `jsonb_agg()`, `jsonb_build_object()`, `jsonb_pretty()`, `jsonb_typeof()`
- **Array Types**: INTEGER[], TEXT[], UUID[] with PostgreSQL syntax
- **Testing Frameworks**: JUnit 5 (`@MemGres`), TestNG, Spring Test (`@DataMemGres`)
- **JDBC Interface**: Full DataSource, Connection, Statement, ResultSet implementation

---

## Phase 3: H2 Compatibility (In Progress) 🔄

**Goal**: Achieve 90%+ H2 feature compatibility for true "drop-in replacement" status.

**Current Status**: Phase 3.1 In Progress - CREATE INDEX completed, MERGE statement parsing implemented (2025-08-11)

### 🚨 Critical H2 Gaps Identified

**Missing H2 DDL Commands** (High Priority):
- ✅ `CREATE INDEX` / `DROP INDEX` - Essential for performance **[COMPLETED 2025-08-11]**
- ❌ `CREATE SEQUENCE` / `DROP SEQUENCE` - Standard H2 ID generation  
- ❌ `ALTER TABLE` (ADD COLUMN, DROP COLUMN, RENAME) - Schema evolution
- ❌ `CREATE VIEW` / `DROP VIEW` - Virtual tables
- ❌ `TRUNCATE TABLE` - Fast table clearing

**Missing H2 DML Features** (High Priority):
- 🔄 `MERGE` statement - Critical H2 upsert operation **[PARSING IMPLEMENTED 2025-08-11]**
- ❌ Window Functions - `ROW_NUMBER()`, `RANK()`, `OVER()` clause
- ❌ Common Table Expressions - `WITH` clause  
- ❌ Set Operations - `UNION`, `INTERSECT`, `EXCEPT`

**Missing H2 Functions** (Medium Priority):
- ❌ Sequence Functions - `NEXT VALUE FOR`, `CURRENT VALUE FOR`
- ❌ System Functions - `DATABASE()`, `USER()`, `SESSION_ID()`
- ❌ Math Functions - `SQRT()`, `POWER()`, `ABS()`, `ROUND()`, `RAND()`
- ❌ Advanced String Functions - `REGEXP_REPLACE()`, `SOUNDEX()`

**Missing H2 Data Types** (Medium Priority):
- ❌ `CLOB` - Large character objects
- ❌ `BINARY`, `VARBINARY` - Binary data
- ❌ `DECFLOAT` - Decimal floating point
- ❌ `INTERVAL` - Time intervals

### Phase 3.1: Essential H2 Commands (8-10 weeks)

**Milestone**: Enable basic H2 replacement scenarios

**Week 1-2: CREATE INDEX Support** ✅ **[COMPLETED - 2025-08-11]**
```sql
CREATE [UNIQUE] [SPATIAL] INDEX [IF NOT EXISTS] idx_name ON table_name 
    (column1 [ASC|DESC] [NULLS FIRST|LAST], column2) [INCLUDE (col3, col4)];
DROP INDEX [IF EXISTS] idx_name;
```

**Implementation Tasks**:
- ✅ Research H2 CREATE INDEX syntax and behavior
- ✅ Extend ANTLR4 grammar with CREATE INDEX, DROP INDEX statements  
- ✅ Create AST nodes for index operations
- ✅ Implement index creation/deletion in storage layer
- ❌ Add index usage optimization in query execution
- ✅ Create comprehensive test suite (16 tests, 100% passing)

**H2 Compatibility Features Implemented**:
- ✅ Full H2 CREATE INDEX syntax with all options
- ✅ UNIQUE, SPATIAL, NULLS DISTINCT modifiers
- ✅ IF NOT EXISTS / IF EXISTS conditional logic
- ✅ Multi-column indexes with sort order (ASC/DESC)
- ✅ NULL ordering (NULLS FIRST/LAST)
- ✅ INCLUDE columns for covering indexes
- ✅ Automatic index name generation
- ✅ Proper error handling and validation

**Week 3-4: MERGE Statement** ✅ **[FULLY COMPLETE - 2025-08-11]**
```sql
-- Simple MERGE (H2 style)
MERGE INTO table KEY(column) VALUES(value1), (value2);

-- Advanced MERGE (Standard SQL)
MERGE INTO target USING source ON condition
WHEN MATCHED [AND condition] THEN UPDATE SET col = val | DELETE
WHEN NOT MATCHED [AND condition] THEN INSERT VALUES (val1, val2);
```

**Implementation Tasks**:
- ✅ Research H2 MERGE statement syntax and behavior
- ✅ Extend ANTLR4 grammar with MERGE statement (both simple and advanced)
- ✅ Create AST node for MERGE operations (comprehensive class hierarchy)
- ✅ Debug and fix advanced MERGE statement parsing issues
- ✅ Create comprehensive test suite for MERGE (14/14 tests passing - 100%)
- ✅ Implement MERGE execution in StatementExecutor (complete with upsert logic)
- ✅ Implement actual MERGE logic (both simple and advanced MERGE operations)

**H2 Compatibility Features Implemented**:
- ✅ Simple MERGE syntax: `MERGE INTO table KEY(columns) VALUES(...)`
- ✅ Advanced MERGE syntax: `MERGE INTO target USING source ON condition`
- ✅ Multiple WHEN clauses with conditions
- ✅ UPDATE, DELETE, and INSERT actions
- ✅ Subquery sources and table sources
- ✅ Column-level specifications for INSERT
- ✅ Complete parser integration and AST representation
- ✅ Full execution logic with upsert operations
- ✅ Complex expressions and conditional actions
- ✅ Table aliases and case insensitive syntax
- ✅ Comprehensive error handling and validation

**Week 5-6: Sequence Support**
```sql
CREATE SEQUENCE seq_name START WITH 1 INCREMENT BY 1;
SELECT NEXT VALUE FOR seq_name;
DROP SEQUENCE seq_name;
```

**Week 7-8: ALTER TABLE Operations**
```sql
ALTER TABLE table_name ADD COLUMN col_name data_type;
ALTER TABLE table_name DROP COLUMN col_name;
ALTER TABLE table_name RENAME TO new_name;
```

**Week 9-10: TRUNCATE TABLE**
```sql
TRUNCATE TABLE table_name [RESTART IDENTITY];
```

### Phase 3.2: Advanced H2 Features (10-12 weeks)

**Milestone**: Support complex H2 applications

**Week 1-4: Window Functions**
```sql
SELECT ROW_NUMBER() OVER (PARTITION BY dept ORDER BY salary),
       RANK() OVER (ORDER BY salary DESC)
FROM employees;
```

**Week 5-8: Common Table Expressions**
```sql
WITH RECURSIVE cte AS (
    SELECT id, parent_id, 1 as level FROM categories WHERE parent_id IS NULL
    UNION ALL
    SELECT c.id, c.parent_id, cte.level + 1
    FROM categories c JOIN cte ON c.parent_id = cte.id
)
SELECT * FROM cte;
```

**Week 9-12: System & Math Functions**
```sql
SELECT DATABASE(), USER(), SESSION_ID();
SELECT SQRT(25), POWER(2,3), ABS(-5), ROUND(3.14159, 2);
```

### Phase 3.3: H2 Advanced Features (8-10 weeks)

**Milestone**: Full H2 compatibility

- **Views**: `CREATE VIEW`, updatable views
- **Set Operations**: `UNION ALL`, `INTERSECT`, `EXCEPT`  
- **Advanced Data Types**: `CLOB`, `BINARY`, `INTERVAL`
- **Triggers**: Basic `BEFORE`/`AFTER` trigger support

---

## Phase 4: Performance & Production (12-16 weeks)

**Goal**: Match H2 performance benchmarks and production readiness.

### 4.1: Query Optimization
- Cost-based query planner
- Statistics collection for better join ordering
- Index selection optimization

### 4.2: Advanced Indexing  
- Composite indexes (multi-column)
- Partial indexes with WHERE conditions
- Expression indexes
- Index-only scans

### 4.3: Production Features
- Connection pooling
- Enhanced error handling and logging
- Memory usage optimization
- Performance monitoring and metrics

---

## Success Criteria

### H2 Compatibility Targets
- **Phase 3.1**: 70% H2 compatibility (essential commands)
- **Phase 3.2**: 85% H2 compatibility (advanced features) 
- **Phase 3.3**: 95% H2 compatibility (full feature set)

### Performance Targets (vs H2)
- **Startup time**: < 100ms (match H2)
- **Simple queries**: < 1ms (match H2)
- **Complex queries**: Within 2x of H2 performance
- **Memory usage**: Within 1.5x of H2 for equivalent datasets

### Testing Targets
- **Test coverage**: Maintain 95%+ code coverage
- **Integration tests**: 100% passing rate
- **H2 compatibility tests**: Run H2's own test suite against MemGres

---

## Development Process

### Implementation Strategy
1. **Research**: Analyze H2 source code and documentation for feature specifications
2. **Design**: Create detailed technical design documents  
3. **Grammar**: Extend ANTLR4 parser with new SQL constructs
4. **Implementation**: Add AST nodes and execution logic
5. **Testing**: Comprehensive test coverage including H2 compatibility tests
6. **Performance**: Benchmark against H2 and optimize
7. **Documentation**: Update user guides and API documentation

### Quality Gates
- All existing tests must continue passing
- New features require 95%+ test coverage
- Performance must not regress by more than 10%
- Code review and approval required for all changes

---

## Timeline Summary

- **Phase 3.1** (Essential H2): 8-10 weeks → **70% H2 compatibility**
- **Phase 3.2** (Advanced H2): 10-12 weeks → **85% H2 compatibility**  
- **Phase 3.3** (Full H2): 8-10 weeks → **95% H2 compatibility**
- **Phase 4** (Production): 12-16 weeks → **Production ready**

**Total estimated timeline**: **38-48 weeks** for complete H2 compatibility

---

**Last Updated**: 2025-08-11  
**Current Branch**: `feature/phase3.1-merge-statement`  
**Current Task**: MERGE statement implementation - **FULLY COMPLETE (14/14 tests passing - 100%)**