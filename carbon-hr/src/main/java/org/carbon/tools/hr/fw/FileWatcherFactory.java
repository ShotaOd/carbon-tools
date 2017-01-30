package org.carbon.tools.hr.fw;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ubuntu 2017/01/25.
 */
public class FileWatcherFactory {
    private Logger logger = LoggerFactory.getLogger(FileWatcherFactory.class);

    private FileWatchEventQueue queue;

    private FileWatcherFactory() {
        this.queue = new FileWatchEventQueue();
    }

    public static FileWatcherFactory instance = new FileWatcherFactory();

    public Set<Runnable> factorize(Path basePath, Consumer<Path> fileChangedConsumer) throws IOException {
        Map<Path, IOException> ioErrors = new HashMap<>();

        Set<FileWatchRunner> fileWatchRunners = factorize(basePath, ioErrors).collect(Collectors.toSet());

        if (!ioErrors.isEmpty()) {
            String failAccessList = ioErrors.entrySet().stream()
                    .map(entry -> "path: " + entry.getKey() + "\nreason:" + entry.getValue().getMessage())
                    .collect(Collectors.joining("\n"));
            logger.warn("below directories cannot access\n"+failAccessList);
        }

        String watchList = fileWatchRunners.stream().map(watcher -> "file:" + watcher.getTarget()).collect(Collectors.joining("\n"));
        logger.info("watching below\n"+watchList);

        Runnable eventConsumer = new FileWatchEventConsumer(this.queue, fileChangedConsumer);
        Set<Runnable> runnables = fileWatchRunners.stream().map(watcher -> (Runnable) watcher).collect(Collectors.toSet());
        runnables.add(eventConsumer);
        return runnables;
    }

    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "ConstantConditions"})
    private Stream<FileWatchRunner> factorize(Path basePath, Map<Path, IOException> ioErrors) throws IOException {
        return Files.walk(basePath, Integer.MAX_VALUE)
            .flatMap(path -> {
                if (Files.isRegularFile(path)) {
                    return Stream.empty();
                }
                try {
                    File dir = path.toFile();
                    if (dir.list().length > 0) {
                        WatchService watchService = FileSystems.getDefault().newWatchService();
                        return Stream.of(new FileWatchRunner(path, this.queue, watchService));
                    }
                    return factorize(path, ioErrors);
                } catch (IOException e) {
                    ioErrors.put(path, e);
                    return Stream.empty();
                }
            });
    }
}
