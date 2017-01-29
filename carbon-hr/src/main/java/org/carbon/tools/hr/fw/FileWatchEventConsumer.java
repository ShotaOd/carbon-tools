package org.carbon.tools.hr.fw;

import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * @author ubuntu 2017/01/28.
 */
public class FileWatchEventConsumer implements Runnable {
    private FileWatchEventQueue queue;
    private Consumer<Path> onChange;

    public FileWatchEventConsumer(FileWatchEventQueue queue, Consumer<Path> onChange) {
        this.queue = queue;
        this.onChange = onChange;
    }

    @Override
    public void run() {
        while(true) {
            FileWatchEvent event = queue.waitTake();
            if (event != null) {
                onChange.accept(event.getAbsFilePath());
            }
        }
    }
}
