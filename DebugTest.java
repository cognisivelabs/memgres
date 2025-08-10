import com.memgres.core.MemGresEngine;
import com.memgres.sql.execution.SqlExecutionEngine;
import com.memgres.sql.execution.SqlExecutionResult;
import com.memgres.testing.MemGresTestDataSource;
import com.memgres.testing.MemGresTestHelper;

public class DebugTest {
    public static void main(String[] args) throws Exception {
        // Create engine
        MemGresTestHelper helper = new MemGresTestHelper();
        MemGresTestHelper.MemGresConfig config = new MemGresTestHelper.MemGresConfig(
            "test", true, new String[]{}, false, 5000);
        MemGresEngine engine = helper.createEngine(config);
        SqlExecutionEngine sqlEngine = new SqlExecutionEngine(engine);
        
        try {
            // Test basic table creation
            System.out.println("=== Testing table creation ===");
            SqlExecutionResult result1 = sqlEngine.execute("CREATE TABLE test (id INTEGER, name TEXT)");
            System.out.println("CREATE TABLE result: " + result1.isSuccess() + " - " + result1.getMessage());
            
            // Test insert
            System.out.println("\n=== Testing insert ===");
            SqlExecutionResult result2 = sqlEngine.execute("INSERT INTO test (id, name) VALUES (1, 'John')");
            System.out.println("INSERT result: " + result2.isSuccess() + " - " + result2.getMessage() + " - affected: " + result2.getAffectedRows());
            
            // Test basic select
            System.out.println("\n=== Testing basic select ===");
            SqlExecutionResult result3 = sqlEngine.execute("SELECT id, name FROM test");
            System.out.println("SELECT result: " + result3.isSuccess() + " - " + result3.getMessage());
            System.out.println("Rows: " + result3.getRows().size());
            System.out.println("Columns: " + result3.getColumns().size());
            if (result3.getColumns().size() > 0) {
                for (int i = 0; i < result3.getColumns().size(); i++) {
                    System.out.println("  Column " + i + ": " + result3.getColumns().get(i).getName());
                }
            }
            if (result3.getRows().size() > 0) {
                System.out.println("  Row data: " + result3.getRows().get(0).getValue(0) + ", " + result3.getRows().get(0).getValue(1));
            }
            
            // Test select with where clause
            System.out.println("\n=== Testing select with where ===");
            SqlExecutionResult result4 = sqlEngine.execute("SELECT name FROM test WHERE id = 1");
            System.out.println("SELECT WHERE result: " + result4.isSuccess() + " - " + result4.getMessage());
            System.out.println("Rows: " + result4.getRows().size());
            System.out.println("Columns: " + result4.getColumns().size());
            if (result4.getColumns().size() > 0) {
                for (int i = 0; i < result4.getColumns().size(); i++) {
                    System.out.println("  Column " + i + ": " + result4.getColumns().get(i).getName());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            helper.shutdownEngine(engine);
        }
    }
}