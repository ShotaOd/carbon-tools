# carbon-tools
## Hot Reloader

### Sample Usage
```java
package org.carbon.tools.hr.test;

import org.carbon.tools.hr.HotReloader;
import org.carbon.tools.hr.test.sample.PackageTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ubuntu 2017/01/27.
 */
public class TestRunner {
    private static Logger logger = LoggerFactory.getLogger(TestRunner.class);
    public static void main(String[] args) throws Exception{
        HotReloader hotReloader = new HotReloader(PackageTarget.class.getPackage());
        hotReloader.setOnClassCompiled(loadCLass -> {
            try {
                Object o = loadCLass.newInstance();
                logger.info("{}", o);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        hotReloader.subscribe();
    }
}

```
