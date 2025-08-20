package com.memgres.core;

import com.memgres.api.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.*;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Compiles and creates trigger instances from Java source code.
 * Provides a secure way to execute user-defined trigger logic.
 */
public class SourceCodeTriggerCompiler {
    private static final Logger logger = LoggerFactory.getLogger(SourceCodeTriggerCompiler.class);
    
    // Cache compiled classes to avoid recompilation
    private static final ConcurrentMap<String, Class<? extends Trigger>> compiledClasses = new ConcurrentHashMap<>();
    
    // Custom class loader for dynamic compilation
    private static final DynamicClassLoader dynamicClassLoader = new DynamicClassLoader();
    
    /**
     * Compile and create a trigger instance from Java source code.
     * 
     * @param sourceCode The Java source code implementing the Trigger interface
     * @param triggerName The name of the trigger (used for class naming)
     * @return A compiled and instantiated Trigger instance
     * @throws Exception if compilation or instantiation fails
     */
    public static Trigger compileAndCreateTrigger(String sourceCode, String triggerName) throws Exception {
        if (sourceCode == null || sourceCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Source code cannot be null or empty");
        }
        
        // Generate a unique class name based on trigger name
        String className = generateClassName(triggerName);
        String fullClassName = "com.memgres.triggers.dynamic." + className;
        
        // Check if already compiled
        Class<? extends Trigger> triggerClass = compiledClasses.get(fullClassName);
        if (triggerClass == null) {
            triggerClass = compileSourceCode(sourceCode, className, fullClassName);
            compiledClasses.put(fullClassName, triggerClass);
        }
        
        // Create and return instance
        return triggerClass.getDeclaredConstructor().newInstance();
    }
    
    /**
     * Compile Java source code into a Trigger class.
     */
    private static Class<? extends Trigger> compileSourceCode(String sourceCode, String className, String fullClassName) throws Exception {
        // Wrap the source code in a proper class structure if needed
        String wrappedSourceCode = wrapSourceCode(sourceCode, className);
        
        // Get the Java compiler
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new UnsupportedOperationException("Java compiler not available. Running with JRE instead of JDK?");
        }
        
        // Create diagnostic collector
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        
        // Create file manager
        StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnostics, null, null);
        InMemoryJavaFileManager fileManager = new InMemoryJavaFileManager(standardFileManager, dynamicClassLoader);
        
        // Create source file object
        JavaFileObject sourceFile = new InMemoryJavaFileObject(className, wrappedSourceCode);
        
        // Compile
        JavaCompiler.CompilationTask task = compiler.getTask(
            null,
            fileManager,
            diagnostics,
            Arrays.asList("-cp", System.getProperty("java.class.path")),
            null,
            Collections.singletonList(sourceFile)
        );
        
        boolean success = task.call();
        
        if (!success) {
            StringBuilder errorMsg = new StringBuilder("Compilation failed:\n");
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                errorMsg.append(diagnostic.toString()).append("\n");
            }
            throw new RuntimeException(errorMsg.toString());
        }
        
        // Load the compiled class
        Class<?> compiledClass = dynamicClassLoader.loadClass(fullClassName);
        
        // Verify it implements Trigger interface
        if (!Trigger.class.isAssignableFrom(compiledClass)) {
            throw new IllegalArgumentException("Compiled class does not implement Trigger interface");
        }
        
        @SuppressWarnings("unchecked")
        Class<? extends Trigger> triggerClass = (Class<? extends Trigger>) compiledClass;
        
        logger.info("Successfully compiled trigger class: {}", fullClassName);
        return triggerClass;
    }
    
    /**
     * Wrap user source code in a proper class structure if it's not already a complete class.
     */
    private static String wrapSourceCode(String sourceCode, String className) {
        // Check if source code already contains a class definition
        if (sourceCode.contains("class ") && sourceCode.contains("implements Trigger")) {
            // For full class definitions, replace the class name to match our expected naming
            return sourceCode.replaceAll("class\\s+\\w+", "class " + className);
        }
        
        // If it's just method implementations, wrap it in a class
        return String.format(
            "package com.memgres.triggers.dynamic;\n" +
            "import com.memgres.api.Trigger;\n" +
            "import java.sql.Connection;\n" +
            "import java.sql.SQLException;\n" +
            "\n" +
            "public class %s implements Trigger {\n" +
            "    @Override\n" +
            "    public void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before, int type) throws SQLException {\n" +
            "        // Default empty implementation\n" +
            "    }\n" +
            "    \n" +
            "    @Override\n" +
            "    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {\n" +
            "        %s\n" +
            "    }\n" +
            "}\n",
            className,
            sourceCode
        );
    }
    
    /**
     * Generate a valid Java class name from trigger name.
     */
    private static String generateClassName(String triggerName) {
        if (triggerName == null) {
            triggerName = "DynamicTrigger";
        }
        
        // Convert to valid Java identifier
        StringBuilder className = new StringBuilder();
        boolean capitalize = true;
        
        for (char c : triggerName.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                className.append(capitalize ? Character.toUpperCase(c) : c);
                capitalize = false;
            } else {
                capitalize = true;
            }
        }
        
        // Ensure it starts with a letter
        if (className.length() == 0 || !Character.isLetter(className.charAt(0))) {
            className.insert(0, "Trigger");
        }
        
        // Add timestamp to make it unique
        className.append("_").append(System.currentTimeMillis());
        
        return className.toString();
    }
    
    /**
     * Clear the compiled class cache (useful for testing or memory management).
     */
    public static void clearCache() {
        compiledClasses.clear();
        logger.debug("Cleared compiled trigger class cache");
    }
    
    /**
     * Custom class loader for dynamically compiled classes.
     */
    private static class DynamicClassLoader extends ClassLoader {
        private final ConcurrentMap<String, byte[]> classBytes = new ConcurrentHashMap<>();
        
        public DynamicClassLoader() {
            super(SourceCodeTriggerCompiler.class.getClassLoader());
        }
        
        public void addClassBytes(String className, byte[] bytes) {
            classBytes.put(className, bytes);
        }
        
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] bytes = classBytes.get(name);
            if (bytes != null) {
                return defineClass(name, bytes, 0, bytes.length);
            }
            return super.findClass(name);
        }
    }
    
    /**
     * In-memory Java file object for compilation.
     */
    private static class InMemoryJavaFileObject extends SimpleJavaFileObject {
        private final String content;
        
        public InMemoryJavaFileObject(String className, String content) {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.content = content;
        }
        
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return content;
        }
    }
    
    /**
     * In-memory file manager for compilation.
     */
    private static class InMemoryJavaFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
        private final DynamicClassLoader classLoader;
        
        public InMemoryJavaFileManager(StandardJavaFileManager fileManager, DynamicClassLoader classLoader) {
            super(fileManager);
            this.classLoader = classLoader;
        }
        
        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            if (kind == JavaFileObject.Kind.CLASS) {
                return new InMemoryClassFileObject(className, classLoader);
            }
            return super.getJavaFileForOutput(location, className, kind, sibling);
        }
    }
    
    /**
     * In-memory class file object for storing compiled bytecode.
     */
    private static class InMemoryClassFileObject extends SimpleJavaFileObject {
        private final String className;
        private final DynamicClassLoader classLoader;
        private final StringWriter writer = new StringWriter();
        
        public InMemoryClassFileObject(String className, DynamicClassLoader classLoader) {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.CLASS.extension), Kind.CLASS);
            this.className = className;
            this.classLoader = classLoader;
        }
        
        @Override
        public java.io.OutputStream openOutputStream() throws IOException {
            return new java.io.ByteArrayOutputStream() {
                @Override
                public void close() throws IOException {
                    super.close();
                    classLoader.addClassBytes(className, toByteArray());
                }
            };
        }
    }
}