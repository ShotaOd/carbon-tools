package org.carbon.tools.hr.fw;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ubuntu 2017/01/25.
 */
public class FileWatcherFactory {
    private Logger logger = LoggerFactory.getLogger(FileWatcherFactory.class);

    private FileWatcherFactory() {}
    public static FileWatcherFactory instance = new FileWatcherFactory();

    public Set<FileWatcher> factorize(Path basePath) throws IOException {
        Map<Path, IOException> ioErrors = new HashMap<>();
        Set<FileWatcher> fileWatchers = factorize(basePath, ioErrors).collect(Collectors.toSet());
        if (!ioErrors.isEmpty()) {
            String failAccessList = ioErrors.entrySet().stream()
                    .map(entry -> "path: " + entry.getKey() + "\nreason:" + entry.getValue().getMessage())
                    .collect(Collectors.joining("\n"));
            logger.warn("below directories cannot access\n"+failAccessList);
        }
        String watchList = fileWatchers.stream().map(watcher -> "file:" + watcher.getTarget()).collect(Collectors.joining("\n"));
        logger.info("watching below\n"+watchList);
        return fileWatchers;
    }

    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "ConstantConditions"})
    private Stream<FileWatcher> factorize(Path basePath, Map<Path, IOException> ioErrors) throws IOException {
        return Files.walk(basePath, Integer.MAX_VALUE)
            .flatMap(path -> {
                if (Files.isRegularFile(path)) {
                    return Stream.empty();
                }
                try {
                    File dir = path.toFile();
                    if (dir.list().length > 0) {
                        return Stream.of(new FileWatcher(path));
                    }
                    return factorize(path, ioErrors);
                } catch (IOException e) {
                    ioErrors.put(path, e);
                    return Stream.empty();
                }
            });
    }
}
