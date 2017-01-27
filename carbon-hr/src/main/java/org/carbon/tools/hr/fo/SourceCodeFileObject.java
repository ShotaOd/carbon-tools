package org.carbon.tools.hr.fo;

import java.io.IOException;
import java.net.URI;
import javax.tools.SimpleJavaFileObject;

/**
 * @author ubuntu 2017/01/25.
 */
public class SourceCodeFileObject extends SimpleJavaFileObject {
    private String code;

    public SourceCodeFileObject(String fqnPath, String code) {
        super(URI.create("string://"+fqnPath), Kind.SOURCE);
        this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return code;
    }
}
