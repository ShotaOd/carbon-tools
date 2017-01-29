package org.carbon.tools.hr.fw;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.stream.Collectors;

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

            List<FileWatchEvent> watchEvents = watchKey.pollEvents().stream().map(event -> {
                WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                Path fileName = pathEvent.context();
                Path absFilePath = target.resolve(fileName);
                return new FileWatchEvent(absFilePath);
            }).collect(Collectors.toList());
            queue.pushAll(watchEvents);

            boolean reset = watchKey.reset();
            if (!reset) {
                logger.error("cannot reset watch key");
                break;
            }
        }
    }
}
