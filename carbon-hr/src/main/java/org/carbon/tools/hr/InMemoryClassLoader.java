package org.carbon.tools.hr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ubuntu 2017/01/25.
 */
public class InMemoryClassLoader extends ClassLoader{
    private Logger logger = LoggerFactory.getLogger(InMemoryClassLoader.class);
    private CompiledCode compiledCode;

    public InMemoryClassLoader(CompiledCode compiledCode) {
        this.compiledCode = compiledCode;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (!compiledCode.getClassName().equals(name)) {
            return super.loadClass(name);
        }

        byte[] byteCode = compiledCode.getByte();
        return defineClass(name, byteCode, 0, byteCode.length);
    }
}
