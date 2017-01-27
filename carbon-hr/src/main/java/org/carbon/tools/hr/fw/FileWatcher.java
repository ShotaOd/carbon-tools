package org.carbon.tools.hr.fw;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ubuntu 2017/01/25.
 */
public class FileWatcher implements Runnable {

    private Logger logger = LoggerFactory.getLogger(FileWatcher.class);

    private static final Object lock = new Object();

    private long interval = 1000L;
    private WatchEvent.Kind[] kinds = new WatchEvent.Kind[]{StandardWatchEventKinds.OVERFLOW, StandardWatchEventKinds.ENTRY_MODIFY};
    private Path target;
    private WatchKey watchKey;
    private Consumer<Path> fileDelegate;

    public FileWatcher(Path targetDir) {
        this.target = targetDir;
    }

    public Path getTarget() {
        return target;
    }

    public void setUp() throws IOException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        watchKey = target.register(watchService, this.kinds);
    }

    public void setFileDelegate(Consumer<Path> fileDelegate) {
        this.fileDelegate = fileDelegate;
    }

    @Override
    public void run() {
        if (watchKey == null) throw new IllegalStateException("should call setUp()");
        doRun();
    }

    public void doRun() {
        while (true) {
            if (!watchKey.reset()) return;
            watch();
        }
    }

    @SuppressWarnings("unchecked")
    public void watch() {
        try {
            logger.debug("start watch per {}mills", interval);
            while (watchKey.isValid()) {
                synchronized (lock) {
                    Thread.sleep(interval);
                }
                List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
                watchEvents.forEach(event -> {
                    WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                    Path fileName = pathEvent.context();
                    Path fullFilePath = target.resolve(fileName);
                    this.fileDelegate.accept(fullFilePath);
                });
            }
        } catch (Throwable e) {
            logger.warn("exception is occurred", e);
        } finally {
            logger.warn("File watcher become disabled");
        }
    }
}
