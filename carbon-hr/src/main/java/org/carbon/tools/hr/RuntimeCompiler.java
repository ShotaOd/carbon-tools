package org.carbon.tools.hr;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.carbon.tools.hr.fo.SourceCodeFileObject;
import static javax.tools.JavaCompiler.CompilationTask;

/**
 * @author ubuntu 2017/01/25.
 */
public class RuntimeCompiler {
    final private JavaCompiler compiler;
    final private ClassFileManager fileManager;

    private Consumer<CompiledCode> handleCompile;

    public RuntimeCompiler() {
        compiler = ToolProvider.getSystemJavaCompiler();

        StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());
        fileManager = new ClassFileManager<>(standardFileManager);
    }

    public void setHandleCompile(Consumer<CompiledCode> handleCompile) {
        this.handleCompile = handleCompile;
    }

    public boolean compile(String fqnPath, String source) {
        SourceCodeFileObject fileObject = new SourceCodeFileObject(fqnPath, source);
        List<String> options = Arrays.asList(
                "-classpath", System.getProperty("java.class.path")
        );
        CompilationTask task = compiler.getTask(
            null,
            fileManager,
            null,
            options,
            null,
            Collections.singletonList(fileObject));

        Boolean compileResult = task.call();

        this.handleCompile.accept(fileManager.getCompileCode());

        return compileResult;
    }
}
