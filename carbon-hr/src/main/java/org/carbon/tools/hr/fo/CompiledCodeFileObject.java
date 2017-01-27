package org.carbon.tools.hr.fo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import javax.tools.SimpleJavaFileObject;

/**
 * @author ubuntu 2017/01/27
 */
public class CompiledCodeFileObject extends SimpleJavaFileObject {
    public CompiledCodeFileObject(String name, Kind kind) {
        super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
    }
    protected final ByteArrayOutputStream bos = new ByteArrayOutputStream();

    @Override
    public OutputStream openOutputStream() throws IOException {
        return bos;
    }

    public byte[] getBytes() {
        return bos.toByteArray();
    }
}
