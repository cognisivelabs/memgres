package com.memgres.benchmark;

import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Performance benchmarking framework for MemGres database.
 * Provides comprehensive benchmarking capabilities including comparison with other databases.
 */
public class BenchmarkRunner {
    private static final Logger logger = LoggerFactory.getLogger(BenchmarkRunner.class);
    
    private final ExecutorService executor;
    private final List<BenchmarkScenario> scenarios = new ArrayList<>();
    private final Map<String, DatabaseAdapter> databases = new HashMap<>();
    
    public BenchmarkRunner() {
        this(Runtime.getRuntime().availableProcessors());
    }
    
    public BenchmarkRunner(int threadCount) {
        this.executor = Executors.newFixedThreadPool(threadCount);
    }
    
    /**
     * Register a database adapter for benchmarking.
     */
    public void registerDatabase(String name, DatabaseAdapter adapter) {
        databases.put(name, adapter);
        logger.info("Registered database adapter: {}", name);
    }
    
    /**
     * Add a benchmark scenario.
     */
    public void addScenario(BenchmarkScenario scenario) {
        scenarios.add(scenario);
        logger.info("Added benchmark scenario: {}", scenario.getName());
    }
    
    /**
     * Run all benchmarks against all registered databases.
     */
    public BenchmarkReport runBenchmarks() {
        return runBenchmarks(scenarios);
    }
    
    /**
     * Run specific benchmarks against all registered databases.
     */
    public BenchmarkReport runBenchmarks(List<BenchmarkScenario> scenariosToRun) {
        logger.info("Starting benchmark suite with {} scenarios on {} databases", 
            scenariosToRun.size(), databases.size());
        
        Map<String, List<BenchmarkResult>> results = new HashMap<>();
        
        for (Map.Entry<String, DatabaseAdapter> entry : databases.entrySet()) {
            String dbName = entry.getKey();
            DatabaseAdapter adapter = entry.getValue();
            
            logger.info("Running benchmarks on database: {}", dbName);
            List<BenchmarkResult> dbResults = new ArrayList<>();
            
            try {
                adapter.initialize();
                
                for (BenchmarkScenario scenario : scenariosToRun) {
                    logger.info("Running scenario '{}' on {}", scenario.getName(), dbName);
                    BenchmarkResult result = runScenario(scenario, adapter, dbName);
                    dbResults.add(result);
                }
                
            } catch (Exception e) {
                logger.error("Error running benchmarks on {}", dbName, e);
            } finally {
                try {
                    adapter.cleanup();
                } catch (Exception e) {
                    logger.error("Error cleaning up {}", dbName, e);
                }
            }
            
            results.put(dbName, dbResults);
        }
        
        return new BenchmarkReport(results);
    }
    
    /**
     * Run a single benchmark scenario.
     */
    private BenchmarkResult runScenario(BenchmarkScenario scenario, DatabaseAdapter adapter, String dbName) {
        long startTime = System.currentTimeMillis();
        List<Long> operationTimes = new ArrayList<>();
        AtomicLong totalOperations = new AtomicLong();
        AtomicLong failedOperations = new AtomicLong();
        
        try {
            // Setup phase
            scenario.setup(adapter);
            
            // Warmup phase
            if (scenario.getWarmupIterations() > 0) {
                logger.debug("Running {} warmup iterations", scenario.getWarmupIterations());
                for (int i = 0; i < scenario.getWarmupIterations(); i++) {
                    scenario.execute(adapter);
                }
            }
            
            // Benchmark phase
            int iterations = scenario.getIterations();
            int concurrency = scenario.getConcurrency();
            
            if (concurrency > 1) {
                // Concurrent execution
                CountDownLatch latch = new CountDownLatch(concurrency);
                List<Future<List<Long>>> futures = new ArrayList<>();
                
                for (int t = 0; t < concurrency; t++) {
                    futures.add(executor.submit(() -> {
                        List<Long> threadTimes = new ArrayList<>();
                        try {
                            for (int i = 0; i < iterations / concurrency; i++) {
                                long opStart = System.nanoTime();
                                totalOperations.incrementAndGet();
                                try {
                                    scenario.execute(adapter);
                                } catch (Exception e) {
                                    failedOperations.incrementAndGet();
                                    // Don't throw - continue with other operations
                                }
                                threadTimes.add(System.nanoTime() - opStart);
                            }
                        } finally {
                            latch.countDown();
                        }
                        return threadTimes;
                    }));
                }
                
                // Wait for completion
                latch.await(scenario.getTimeoutSeconds(), TimeUnit.SECONDS);
                
                // Collect results
                for (Future<List<Long>> future : futures) {
                    try {
                        operationTimes.addAll(future.get());
                    } catch (Exception e) {
                        logger.error("Error in concurrent execution", e);
                    }
                }
                
            } else {
                // Sequential execution
                for (int i = 0; i < iterations; i++) {
                    long opStart = System.nanoTime();
                    totalOperations.incrementAndGet();
                    try {
                        scenario.execute(adapter);
                    } catch (Exception e) {
                        failedOperations.incrementAndGet();
                        logger.debug("Operation failed: {}", e.getMessage());
                    }
                    operationTimes.add(System.nanoTime() - opStart);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error running scenario '{}' on {}", scenario.getName(), dbName, e);
        } finally {
            try {
                scenario.cleanup(adapter);
            } catch (Exception e) {
                logger.error("Error cleaning up scenario", e);
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        return new BenchmarkResult(
            scenario.getName(),
            dbName,
            totalOperations.get(),
            failedOperations.get(),
            endTime - startTime,
            operationTimes
        );
    }
    
    /**
     * Shutdown the benchmark runner.
     */
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Interface for database adapters.
     */
    public interface DatabaseAdapter {
        void initialize() throws Exception;
        void cleanup() throws Exception;
        void executeSQL(String sql) throws Exception;
        ResultSet executeQuery(String sql) throws Exception;
        Connection getConnection() throws Exception;
    }
    
    /**
     * MemGres database adapter.
     */
    public static class MemGresAdapter implements DatabaseAdapter {
        private MemGresEngine engine;
        private SqlExecutionEngine sqlEngine;
        
        public MemGresAdapter() {
            // Initialize in initialize() method to ensure proper lifecycle
        }
        
        @Override
        public void initialize() {
            this.engine = new MemGresEngine();
            engine.initialize();
            this.sqlEngine = new SqlExecutionEngine(engine);
        }
        
        @Override
        public void cleanup() {
            if (engine != null) {
                engine.shutdown();
            }
        }
        
        @Override
        public void executeSQL(String sql) throws SqlExecutionException {
            sqlEngine.execute(sql);
        }
        
        @Override
        public ResultSet executeQuery(String sql) throws Exception {
            // MemGres doesn't use JDBC ResultSet, so this returns null
            // Use executeSQL for MemGres operations
            sqlEngine.execute(sql);
            return null;
        }
        
        @Override
        public Connection getConnection() {
            // MemGres doesn't use JDBC connections
            return null;
        }
    }
    
    /**
     * Benchmark scenario definition.
     */
    public static abstract class BenchmarkScenario {
        private final String name;
        private int iterations = 1000;
        private int warmupIterations = 100;
        private int concurrency = 1;
        private int timeoutSeconds = 300;
        
        public BenchmarkScenario(String name) {
            this.name = name;
        }
        
        public abstract void setup(DatabaseAdapter adapter) throws Exception;
        public abstract void execute(DatabaseAdapter adapter) throws Exception;
        public abstract void cleanup(DatabaseAdapter adapter) throws Exception;
        
        public String getName() { return name; }
        public int getIterations() { return iterations; }
        public int getWarmupIterations() { return warmupIterations; }
        public int getConcurrency() { return concurrency; }
        public int getTimeoutSeconds() { return timeoutSeconds; }
        
        public BenchmarkScenario withIterations(int iterations) {
            this.iterations = iterations;
            return this;
        }
        
        public BenchmarkScenario withWarmup(int warmupIterations) {
            this.warmupIterations = warmupIterations;
            return this;
        }
        
        public BenchmarkScenario withConcurrency(int concurrency) {
            this.concurrency = concurrency;
            return this;
        }
        
        public BenchmarkScenario withTimeout(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
            return this;
        }
    }
    
    /**
     * Pre-defined benchmark scenarios.
     */
    public static class Scenarios {
        
        /**
         * Simple INSERT benchmark.
         */
        public static BenchmarkScenario simpleInsert() {
            return new BenchmarkScenario("Simple INSERT") {
                private final Random random = new Random();
                private final AtomicLong counter = new AtomicLong();
                
                @Override
                public void setup(DatabaseAdapter adapter) throws Exception {
                    adapter.executeSQL("CREATE TABLE users (id INTEGER, name VARCHAR)");
                }
                
                @Override
                public void execute(DatabaseAdapter adapter) throws Exception {
                    long id = counter.incrementAndGet();
                    adapter.executeSQL("INSERT INTO users VALUES (" + id + ", 'value_" + id + "')");
                }
                
                @Override
                public void cleanup(DatabaseAdapter adapter) throws Exception {
                    try {
                        adapter.executeSQL("DROP TABLE users");
                    } catch (Exception e) {
                        // Ignore cleanup errors
                    }
                }
            };
        }
        
        /**
         * Simple SELECT benchmark.
         */
        public static BenchmarkScenario simpleSelect() {
            return new BenchmarkScenario("Simple SELECT") {
                private final Random random = new Random();
                
                @Override
                public void setup(DatabaseAdapter adapter) throws Exception {
                    adapter.executeSQL("CREATE TABLE test_data (id INTEGER, name VARCHAR)");
                    for (int i = 0; i < 100; i++) { // Reduced data size for faster setup
                        adapter.executeSQL("INSERT INTO test_data VALUES (" + i + ", 'value_" + i + "')");
                    }
                }
                
                @Override
                public void execute(DatabaseAdapter adapter) throws Exception {
                    int id = random.nextInt(100);
                    adapter.executeSQL("SELECT name FROM test_data WHERE id = " + id);
                }
                
                @Override
                public void cleanup(DatabaseAdapter adapter) throws Exception {
                    try {
                        adapter.executeSQL("DROP TABLE test_data");
                    } catch (Exception e) {
                        // Ignore cleanup errors
                    }
                }
            };
        }
        
        /**
         * JOIN benchmark.
         */
        public static BenchmarkScenario joinQuery() {
            return new BenchmarkScenario("JOIN Query") {
                @Override
                public void setup(DatabaseAdapter adapter) throws Exception {
                    adapter.executeSQL("CREATE TABLE orders (id INTEGER, customer_id INTEGER, amount DECIMAL)");
                    adapter.executeSQL("CREATE TABLE customers (id INTEGER, name VARCHAR)");
                    
                    for (int i = 1; i <= 1000; i++) {
                        adapter.executeSQL("INSERT INTO customers VALUES (" + i + ", 'Customer_" + i + "')");
                    }
                    
                    for (int i = 1; i <= 5000; i++) {
                        int customerId = (i % 1000) + 1;
                        adapter.executeSQL("INSERT INTO orders VALUES (" + i + ", " + customerId + ", " + (i * 10.5) + ")");
                    }
                }
                
                @Override
                public void execute(DatabaseAdapter adapter) throws Exception {
                    adapter.executeSQL("SELECT c.name, SUM(o.amount) FROM customers c " +
                        "INNER JOIN orders o ON c.id = o.customer_id " +
                        "GROUP BY c.name LIMIT 10");
                }
                
                @Override
                public void cleanup(DatabaseAdapter adapter) throws Exception {
                    try {
                        adapter.executeSQL("DROP TABLE orders");
                        adapter.executeSQL("DROP TABLE customers");
                    } catch (Exception e) {
                        // Ignore cleanup errors
                    }
                }
            };
        }
        
        /**
         * Complex aggregation benchmark.
         */
        public static BenchmarkScenario complexAggregation() {
            return new BenchmarkScenario("Complex Aggregation") {
                @Override
                public void setup(DatabaseAdapter adapter) throws Exception {
                    adapter.executeSQL("CREATE TABLE sales (" +
                        "id INTEGER, " +
                        "product_id INTEGER, " +
                        "category VARCHAR, " +
                        "amount DECIMAL, " +
                        "sale_date DATE)");
                    
                    for (int i = 1; i <= 1000; i++) { // Reduced from 10000 to 1000 for faster setup
                        int productId = i % 100;
                        String category = "Category_" + (productId % 10);
                        adapter.executeSQL(String.format(
                            "INSERT INTO sales VALUES (%d, %d, '%s', %.2f, '2024-01-01')",
                            i, productId, category, i * 15.75
                        ));
                    }
                }
                
                @Override
                public void execute(DatabaseAdapter adapter) throws Exception {
                    adapter.executeSQL(
                        "SELECT category, COUNT(*) as count, " +
                        "SUM(amount) as total, AVG(amount) as average, " +
                        "MIN(amount) as minimum, MAX(amount) as maximum " +
                        "FROM sales " +
                        "GROUP BY category " +
                        "HAVING COUNT(*) > 100 " +
                        "ORDER BY total DESC"
                    );
                }
                
                @Override
                public void cleanup(DatabaseAdapter adapter) throws Exception {
                    try {
                        adapter.executeSQL("DROP TABLE sales");
                    } catch (Exception e) {
                        // Ignore cleanup errors
                    }
                }
            };
        }
    }
}