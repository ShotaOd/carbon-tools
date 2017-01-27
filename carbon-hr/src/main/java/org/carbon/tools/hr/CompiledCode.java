package org.carbon.tools.hr;

import org.carbon.tools.hr.fo.CompiledCodeFileObject;

/**
 * @author ubuntu 2017/01/25.
 */
public class CompiledCode {
    private String className;
    private CompiledCodeFileObject compiledCodeFileObject;

    public CompiledCode(String className, CompiledCodeFileObject compiledCodeFileObject) {
        this.className = className;
        this.compiledCodeFileObject = compiledCodeFileObject;
    }

    public String getClassName() {
        return className;
    }
    public byte[] getByte() {
        return compiledCodeFileObject.getBytes();
    }
}
