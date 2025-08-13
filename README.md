# MemGres

[![Build Status](https://github.com/memgres/memgres-core/workflows/CI/badge.svg)](https://github.com/memgres/memgres-core/actions)
[![Test Coverage](https://img.shields.io/badge/coverage-100%25-brightgreen.svg)](https://github.com/memgres/memgres-core)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

**MemGres** is a lightweight, in-memory database for Java applications. It provides **H2-compatible SQL** with **PostgreSQL JSONB extensions** for modern testing scenarios.

## Features

- **Pure Java**: Requires Java 17+, small footprint (~5MB jar), zero external dependencies
- **H2-compatible SQL**: Standard DDL/DML operations, joins, subqueries, aggregation, views  
- **PostgreSQL JSONB**: Full JSON operators (`@>`, `?`, `->`, `->>`) for modern applications
- **Advanced DDL**: CREATE INDEX, MERGE statements, SEQUENCE support, CREATE VIEW / DROP VIEW, TRUNCATE TABLE, ALTER TABLE
- **Testing-focused**: `@MemGres` annotations for JUnit 5, TestNG, and Spring Test
- **High performance**: < 100ms startup, < 1ms simple queries, thread-safe operations
- **ACID transactions**: Four isolation levels with automatic rollback for testing

## Installation

### Maven
```xml
<dependency>
    <groupId>com.memgres</groupId>
    <artifactId>memgres-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

### Gradle
```gradle
testImplementation 'com.memgres:memgres-core:1.0.0-SNAPSHOT'
```

## Quick Start

```java
@Test
@MemGres
void testWithMemGres(SqlExecutionEngine sql) {
    sql.execute("CREATE TABLE users (id INTEGER, profile JSONB)");
    sql.execute("INSERT INTO users VALUES (1, '{\"name\": \"Alice\", \"age\": 30}')");
    
    // Create a view with JSONB operations
    sql.execute("CREATE VIEW adult_users AS SELECT id, profile->>'name' as name FROM users WHERE profile @> '{\"age\": 30}'");
    
    var result = sql.execute("SELECT name FROM adult_users");
    assertEquals("Alice", result.getRows().get(0).getValue(0));
}
```

## Use Cases

**Replace H2 when you need JSONB**: Perfect for testing PostgreSQL applications that use JSON operations. Eliminates the need for separate test and production SQL schemas.

## Documentation

- [User Guide](docs/USER_GUIDE.md) - Complete usage documentation
- [API Reference](docs/API_REFERENCE.md) - Detailed API documentation  
- [Migration Guide](docs/MIGRATION.md) - Migrating from H2 to MemGres

## Support

- [Issues](https://github.com/memgres/memgres-core/issues) - Bug reports and feature requests
- [Discussions](https://github.com/memgres/memgres-core/discussions) - Questions and community support

## Status

**Current**: Phase 3.1 Complete (500+ tests passing - 100% success rate)
- ✅ H2-compatible SQL operations (DDL, DML, joins, subqueries, aggregation)  
- ✅ PostgreSQL JSONB with all operators and functions
- ✅ Testing framework integration (JUnit 5, TestNG, Spring Test)
- ✅ Essential H2 DDL commands (CREATE INDEX, MERGE statements, SEQUENCE support)
- ✅ Advanced H2 features (CREATE VIEW / DROP VIEW, TRUNCATE TABLE, ALTER TABLE)
- ✅ **NEW**: H2 System & Math Functions (DATABASE(), USER(), SQRT(), POWER(), ABS(), ROUND(), RAND())

**Next**: Phase 3.2 - Advanced H2 features (Window functions, CTEs, Set operations)

## License

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for details.