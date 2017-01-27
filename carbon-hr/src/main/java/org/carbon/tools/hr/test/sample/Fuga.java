package org.carbon.tools.hr.test.sample;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author ubuntu 2017/01/27.
 */
public class Fuga {
    private LocalDateTime dateTime = LocalDateTime.now();

    @Override
    public String toString() {
        return "fugafugafuga"+dateTime.format(DateTimeFormatter.ISO_DATE_TIME);
    }
}
