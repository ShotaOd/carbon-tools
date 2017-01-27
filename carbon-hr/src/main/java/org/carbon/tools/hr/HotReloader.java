package org.carbon.tools.hr;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.carbon.tools.hr.fw.FileWatcher;
import org.carbon.tools.hr.fw.FileWatcherFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ubuntu 2017/01/27
 */
public class HotReloader {
    private Logger logger = LoggerFactory.getLogger(HotReloader.class);

    final private String Std_Java_Package_Path = "/src/main/java";
    final private String Current_Directory_Property = "user.dir";

    private Consumer<Class> onClassCompiled;

    protected RuntimeCompiler runtimeCompiler;
    protected Set<FileWatcher> fileWatchers;

    public HotReloader(Package targetPackage) throws IOException {
        runtimeCompiler = new RuntimeCompiler();
        setupCompiler();
        Path packageDirectory = getPackageDirectory(targetPackage.getName().replace(".", "/"));
        fileWatchers = FileWatcherFactory.instance.factorize(packageDirectory);
    }

    public void subscribe() throws IOException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(fileWatchers.size());
        for (FileWatcher watcher : fileWatchers) {
            executorService.submit(setupWatcher(watcher));
        }
    }

    public void setOnClassCompiled(Consumer<Class> onClassCompiled) {
        this.onClassCompiled = onClassCompiled;
    }

    private Path getPackageDirectory(String targetPackage) {
        String rootDir = System.getProperty(Current_Directory_Property);
        return Paths.get(rootDir, Std_Java_Package_Path, targetPackage);
    }

    private void setupCompiler() {
        this.runtimeCompiler.setHandleCompile(compiledCode -> {
            InMemoryClassLoader classLoader = new InMemoryClassLoader(compiledCode);
            try {
                Class<?> loadClass = classLoader.loadClass(compiledCode.getClassName());
                logger.debug("class loaded\nClassLoader: {}\nClass: {}",loadClass.getClassLoader(), loadClass);
                try {
                    Object o = loadClass.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                if (this.onClassCompiled != null) onClassCompiled.accept(loadClass);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    private FileWatcher setupWatcher(FileWatcher watcher) throws IOException, InterruptedException {
        watcher.setUp();
        watcher.setFileDelegate(path -> {
            logger.debug("detect change, file://" + path);
            String pathStr = path.toString();
            if (path.toFile().isDirectory()) return;
            if (!path.toString().endsWith(".java")) {
                logger.debug("dismiss because ["+path.getFileName()+"] is not java file");
                return;
            }
            logger.debug("try compile...");
            try (BufferedReader reader = new BufferedReader(new FileReader(pathStr))) {
                String classFqnPath = resolveClassFqn(path);
                String classFqn = classFqnPath.replace("/", ".").replace(".java", "");
                if (classFqn.startsWith(".")) {
                    classFqn = classFqn.replaceFirst(".","");
                }
                String source = reader.lines().collect(Collectors.joining("\n"));
                logger.debug("target file: {}",classFqnPath);
                logger.debug("source: \n{}",source);
                runtimeCompiler.compile(classFqnPath, source);
            } catch (IOException ignore) {
            } catch (Exception e) {
                logger.warn("",e);
            }
        });
        return watcher;
    }

    private String resolveClassFqn(Path fullPath) {
        String fullPathStr = fullPath.toString();
        int lastIndex = fullPathStr.lastIndexOf(Std_Java_Package_Path);
        return fullPathStr.substring(lastIndex+Std_Java_Package_Path.length(), fullPathStr.length());
    }
}