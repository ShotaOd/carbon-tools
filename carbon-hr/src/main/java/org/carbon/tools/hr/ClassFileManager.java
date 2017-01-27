package org.carbon.tools.hr;

import java.io.IOException;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

import org.carbon.tools.hr.fo.CompiledCodeFileObject;

/**
 * @author ubuntu 2017/01/25.
 */
public class ClassFileManager<T extends JavaFileManager> extends ForwardingJavaFileManager<JavaFileManager> {

    public ClassFileManager(T fileManager) {
        super(fileManager);
    }

    private CompiledCode compiledCode;

    public CompiledCode getCompileCode() {
        return compiledCode;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        CompiledCodeFileObject compiledCodeFileObject = new CompiledCodeFileObject(className, kind);
        this.compiledCode = new CompiledCode(className, compiledCodeFileObject);
        return compiledCodeFileObject;
    }
}
