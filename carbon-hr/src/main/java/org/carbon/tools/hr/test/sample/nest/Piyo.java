package org.carbon.tools.hr.test.sample.nest;

import org.carbon.tools.hr.test.sample.Fuga;
import org.carbon.tools.hr.test.sample.Hoge;

/**
 * @author ubuntu 2017/01/27.
 */
public class Piyo {
    private Hoge hoge = new Hoge();
    private Fuga fuga = new Fuga();


    @Override
    public String toString() {
        return "Piyo{" +
                "hoge=" + hoge +
                ", fuga=" + fuga +
                '}';
    }
}
