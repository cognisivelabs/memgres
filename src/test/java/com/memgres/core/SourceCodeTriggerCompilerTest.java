package com.memgres.core;

import com.memgres.api.Trigger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SourceCodeTriggerCompiler functionality.
 */
public class SourceCodeTriggerCompilerTest {
    
    @Test
    void testCompileSimpleSourceCode() throws Exception {
        String sourceCode = "System.out.println(\"Trigger fired!\");";
        
        Trigger trigger = SourceCodeTriggerCompiler.compileAndCreateTrigger(sourceCode, "test_trigger");
        
        assertNotNull(trigger);
        
        // Test that fire method can be called without error
        trigger.fire(null, new Object[]{1, "old"}, new Object[]{2, "new"});
    }
    
    @Test
    void testCompileFullClassSourceCode() throws Exception {
        String sourceCode = 
            "package com.memgres.triggers.dynamic;\n" +
            "import com.memgres.api.Trigger;\n" +
            "import java.sql.Connection;\n" +
            "import java.sql.SQLException;\n" +
            "\n" +
            "public class TestTriggerFull implements Trigger {\n" +
            "    @Override\n" +
            "    public void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before, int type) throws SQLException {\n" +
            "        // Custom init logic\n" +
            "    }\n" +
            "    \n" +
            "    @Override\n" +
            "    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {\n" +
            "        if (oldRow != null && oldRow.length > 0) {\n" +
            "            System.out.println(\"Old row: \" + oldRow[0]);\n" +
            "        }\n" +
            "        if (newRow != null && newRow.length > 0) {\n" +
            "            System.out.println(\"New row: \" + newRow[0]);\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        
        Trigger trigger = SourceCodeTriggerCompiler.compileAndCreateTrigger(sourceCode, "full_trigger");
        
        assertNotNull(trigger);
        
        // Test that methods can be called
        trigger.init(null, "public", "test_trigger", "test_table", true, Trigger.INSERT);
        trigger.fire(null, new Object[]{1}, new Object[]{2});
    }
    
    @Test
    void testInvalidSourceCode() {
        String invalidSourceCode = "This is not valid Java code!";
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            SourceCodeTriggerCompiler.compileAndCreateTrigger(invalidSourceCode, "invalid_trigger");
        });
        
        assertTrue(exception.getMessage().contains("Compilation failed"));
    }
    
    @Test
    void testNullOrEmptySourceCode() {
        // Test null source code
        Exception exception1 = assertThrows(IllegalArgumentException.class, () -> {
            SourceCodeTriggerCompiler.compileAndCreateTrigger(null, "null_trigger");
        });
        assertEquals("Source code cannot be null or empty", exception1.getMessage());
        
        // Test empty source code
        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> {
            SourceCodeTriggerCompiler.compileAndCreateTrigger("", "empty_trigger");
        });
        assertEquals("Source code cannot be null or empty", exception2.getMessage());
    }
    
    @Test
    void testClassNameGeneration() throws Exception {
        // Test trigger name with special characters
        String sourceCode = "System.out.println(\"Test\");";
        
        Trigger trigger1 = SourceCodeTriggerCompiler.compileAndCreateTrigger(sourceCode, "my-trigger_name#123");
        assertNotNull(trigger1);
        
        Trigger trigger2 = SourceCodeTriggerCompiler.compileAndCreateTrigger(sourceCode, "another.trigger");
        assertNotNull(trigger2);
        
        // Verify they are different instances
        assertNotSame(trigger1, trigger2);
    }
    
    @Test
    void testCacheClearing() throws Exception {
        String sourceCode = "System.out.println(\"Cached trigger\");";
        
        // Compile first time
        Trigger trigger1 = SourceCodeTriggerCompiler.compileAndCreateTrigger(sourceCode, "cached_trigger");
        assertNotNull(trigger1);
        
        // Clear cache
        SourceCodeTriggerCompiler.clearCache();
        
        // Compile again (should work even after cache clear)
        Trigger trigger2 = SourceCodeTriggerCompiler.compileAndCreateTrigger(sourceCode, "cached_trigger_2");
        assertNotNull(trigger2);
    }
    
    @Test
    void testAccessToTriggerConstants() throws Exception {
        String sourceCode = 
            "int insertType = " + Trigger.INSERT + ";\n" +
            "int updateType = " + Trigger.UPDATE + ";\n" +
            "int deleteType = " + Trigger.DELETE + ";\n" +
            "System.out.println(\"Insert: \" + insertType + \", Update: \" + updateType + \", Delete: \" + deleteType);";
        
        Trigger trigger = SourceCodeTriggerCompiler.compileAndCreateTrigger(sourceCode, "constants_trigger");
        
        assertNotNull(trigger);
        trigger.fire(null, null, new Object[]{1, "test"});
    }
}