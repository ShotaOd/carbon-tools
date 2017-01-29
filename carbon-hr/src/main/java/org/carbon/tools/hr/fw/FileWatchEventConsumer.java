package org.carbon.tools.hr.fw;

import java.nio.file.Path;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ubuntu 2017/01/28.
 */
public class FileWatchEventConsumer implements Runnable {
    private Logger logger = LoggerFactory.getLogger(FileWatchEventConsumer.class);

    private FileWatchEventQueue queue;
    private Consumer<Path> onChange;

    public FileWatchEventConsumer(FileWatchEventQueue queue, Consumer<Path> onChange) {
        this.queue = queue;
        this.onChange = onChange;
    }

    @Override
    public void run() {
        doRun();
        logger.warn("stop consuming");
    }

    protected void doRun() {
        while(true) {
            try {
                FileWatchEvent event = queue.waitTake();
                onChange.accept(event.getAbsFilePath());
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
