# MemGres - In-Memory PostgreSQL-Like Database

## Project Overview

MemGres is an in-memory database system designed to be PostgreSQL-compatible but optimized for testing environments. It provides fast, lightweight database operations while maintaining ACID properties and supporting key PostgreSQL features.

## Architecture Overview

### Core Components

1. **Storage Engine**
   - ConcurrentHashMap-based table storage for thread-safe operations
   - B+ tree indexing system for efficient queries
   - Memory-optimized data structures

2. **Data Types System**
   - Native UUID support with generation functions
   - JSONB implementation with binary storage
   - Standard SQL data types (INTEGER, VARCHAR, BOOLEAN, etc.)

3. **Query Engine**
   - SQL parser supporting PostgreSQL syntax subset
   - Query planner and optimizer
   - Execution engine with operator support

4. **Transaction Management**
   - ACID compliance with isolation levels
   - Multi-version concurrency control (MVCC)
   - Deadlock detection and resolution

## Technical Specifications

### Storage Strategy
- **Tables**: ConcurrentHashMap&lt;String, Table&gt; for schema storage
- **Rows**: ArrayList&lt;Row&gt; with efficient memory layout
- **Indexes**: B+ tree implementation with configurable node sizes
- **Memory Management**: Weak references for cache eviction

### JSONB Implementation
- Binary JSON storage format for efficient operations
- Path indexing for fast key lookups
- PostgreSQL operators support:
  - `@>` (contains)
  - `?` (key exists)
  - `->` (get JSON object field)
  - `->>` (get JSON object field as text)
  - `#>` (get JSON object at path)
  - `#>>` (get JSON object at path as text)

### UUID Support
- Native UUID data type with validation
- Generation functions:
  - `gen_random_uuid()`: Cryptographically secure random UUID
  - `uuid_generate_v1()`: Time-based UUID
  - `uuid_generate_v4()`: Random UUID
- Efficient UUID indexing and comparison

## Implementation Phases

### Phase 1: Foundation (Core Infrastructure)
1. **Project Structure Setup**
   - Maven configuration with proper dependencies
   - Package organization and module structure
   - CI/CD pipeline setup

2. **Core Data Structures**
   - Table and Row abstractions
   - Data type system implementation
   - Memory management utilities

3. **Storage Engine**
   - ConcurrentHashMap-based table storage
   - Basic CRUD operations
   - Thread-safety mechanisms

### Phase 2: Indexing and Query Processing
1. **B+ Tree Implementation**
   - Generic B+ tree with configurable parameters
   - Index management and maintenance
   - Range query support

2. **SQL Parser**
   - ANTLR-based SQL grammar
   - AST generation and validation
   - Query compilation pipeline

3. **Query Execution**
   - Operator implementation (SELECT, INSERT, UPDATE, DELETE)
   - Join algorithms (nested loop, hash join)
   - Aggregation functions

### Phase 3: Advanced Features
1. **JSONB Implementation**
   - Binary JSON storage format
   - Path indexing system
   - Operator implementations

2. **UUID Support**
   - UUID data type and validation
   - Generation function implementations
   - Index optimization for UUIDs

3. **Transaction Management**
   - ACID transaction support
   - Isolation level implementation
   - Concurrent access control

### Phase 4: Testing Integration and Deployment
1. **Testing Framework Integration**
   - JUnit 5 integration
   - TestNG compatibility
   - Spring Test support
   - Custom annotations for database setup

2. **Performance Optimization**
   - Query optimization strategies
   - Memory usage optimization
   - Benchmark suite implementation

3. **Maven Repository Deployment**
   - Artifact configuration
   - Documentation generation
   - Release management

## Project Structure

```
inmemopg/
├── docs/
│   ├── PROJECT_PLAN.md
│   ├── API_REFERENCE.md
│   └── DEVELOPER_GUIDE.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── inmemopg/
│   │   │           ├── core/          # Core database engine
│   │   │           ├── storage/       # Storage and indexing
│   │   │           ├── sql/           # SQL parsing and execution
│   │   │           ├── types/         # Data types implementation
│   │   │           ├── transaction/   # Transaction management
│   │   │           └── testing/       # Testing framework integration
│   │   └── resources/
│   │       ├── sql-grammar/
│   │       └── config/
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── inmemopg/
│       │           ├── integration/
│       │           ├── performance/
│       │           └── unit/
│       └── resources/
├── pom.xml
├── README.md
└── LICENSE
```

## Dependencies

### Core Dependencies
- **ANTLR4**: SQL parsing and grammar processing
- **Jackson**: JSON processing for JSONB support
- **SLF4J + Logback**: Logging framework
- **Caffeine**: High-performance caching

### Testing Dependencies
- **JUnit 5**: Primary testing framework
- **TestNG**: Alternative testing support
- **Mockito**: Mocking framework
- **Testcontainers**: Integration testing (PostgreSQL compatibility)

### Build Dependencies
- **Maven**: Build and dependency management
- **SpotBugs**: Static analysis
- **Checkstyle**: Code style enforcement
- **JaCoCo**: Code coverage analysis

## Performance Goals

- **Startup Time**: &lt; 100ms for database initialization
- **Query Latency**: &lt; 1ms for simple queries, &lt; 10ms for complex joins
- **Memory Usage**: &lt; 50MB baseline, efficient scaling with data size
- **Throughput**: &gt; 10K simple operations/second on standard hardware

## Deployment Strategy

### Maven Central Deployment
- Group ID: `com.inmemopg`
- Artifact ID: `inmemopg-core`
- Versioning: Semantic versioning (MAJOR.MINOR.PATCH)
- Release cycle: Monthly minor releases, patches as needed

### Testing Integration
```xml
<dependency>
    <groupId>com.inmemopg</groupId>
    <artifactId>inmemopg-core</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
```

### Usage Example
```java
@Test
@InMemoPG
public void testUserRepository() {
    // Automatic in-memory database setup
    UserRepository repository = new UserRepository(dataSource);
    // Test implementation
}
```

## Success Metrics

1. **Compatibility**: 90% compatibility with PostgreSQL for testing use cases
2. **Performance**: 10x faster than H2 for typical test scenarios
3. **Adoption**: Integration with major Java testing frameworks
4. **Reliability**: Zero data corruption, proper transaction isolation

## Risk Assessment

### Technical Risks
- Memory management complexity
- SQL compatibility edge cases
- Performance optimization challenges

### Mitigation Strategies
- Comprehensive test suite with PostgreSQL comparison
- Performance benchmarking against H2 and embedded PostgreSQL
- Memory profiling and optimization tools

## Timeline

- **Phase 1**: 4-6 weeks (Foundation)
- **Phase 2**: 6-8 weeks (Indexing and Query Processing)
- **Phase 3**: 4-6 weeks (Advanced Features)
- **Phase 4**: 2-4 weeks (Testing Integration and Deployment)

**Total Estimated Time**: 16-24 weeks

## Next Steps

1. Set up Maven project structure
2. Implement core data structures and storage engine
3. Create B+ tree indexing system
4. Develop SQL parser and basic query execution
5. Add JSONB and UUID support
6. Implement transaction management
7. Create testing framework integration
8. Deploy to Maven Central
