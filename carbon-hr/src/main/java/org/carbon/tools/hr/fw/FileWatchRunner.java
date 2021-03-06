package org.carbon.tools.hr.fw;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ubuntu 2017/01/25.
 */
public class FileWatchRunner implements Runnable {

    private Logger logger = LoggerFactory.getLogger(FileWatchRunner.class);

    private WatchEvent.Kind[] kinds = new WatchEvent.Kind[]{StandardWatchEventKinds.OVERFLOW, StandardWatchEventKinds.ENTRY_MODIFY};

    private Path target;
    private FileWatchEventQueue queue;
    private WatchService watchService;

    public FileWatchRunner(Path target, FileWatchEventQueue queue, WatchService watchService) {
        this.target = target;
        this.queue = queue;
        this.watchService = watchService;
    }

    public Path getTarget() {
        return target;
    }

    @Override
    public void run() {
        try {
            preRun();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        doRun();
    }

    protected void preRun() throws IOException {
        target.register(watchService, kinds);
    }

    @SuppressWarnings("unchecked")
    protected void doRun() {
        while (true) {
            WatchKey watchKey;
            try {
                watchKey = watchService.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.error("stop thread");
                break;
            }

            watchKey.pollEvents().stream()
                .map(event -> {
                    WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                    Path fileName = pathEvent.context();
                    Path absFilePath = target.resolve(fileName);
                    return new FileWatchEvent(absFilePath);
                })
                .forEach(watchEvent -> {
                    queue.push(watchEvent);
                });

            boolean reset = watchKey.reset();
            if (!reset) {
                logger.error("cannot reset watch key");
                break;
            }
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof FileWatchRunner)) return false;
        FileWatchRunner that = (FileWatchRunner) object;
        return target.equals(that.target);
    }

    @Override
    public int hashCode() {
        return target.hashCode();
    }
}
