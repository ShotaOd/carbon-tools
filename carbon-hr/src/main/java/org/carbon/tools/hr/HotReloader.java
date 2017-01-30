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
    protected Set<Runnable> runners;

    public HotReloader(Package targetPackage) throws IOException {
        runtimeCompiler = new RuntimeCompiler();
        setupCompiler();
        Path packageDirectory = getPackageDirectory(targetPackage.getName().replace(".", "/"));
        runners = FileWatcherFactory.instance.factorize(packageDirectory, getFileChangedConsumer());
    }

    public void subscribe() throws IOException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(runners.size());
        runners.forEach(executorService::execute);
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
                logger.debug("class loaded\nClassLoader: {}\nClass: {}", loadClass.getClassLoader(), loadClass.getName());
                if (this.onClassCompiled != null) onClassCompiled.accept(loadClass);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    private Consumer<Path> getFileChangedConsumer() {
        return path -> {
            logger.debug("Detect change, at [{}]", path);

            String pathStr = path.toString();
            if (path.toFile().isDirectory()) {
                logger.debug("Dismiss because not file [{}]", path);
                return;
            }
            if (!path.toString().endsWith(".java")) {
                logger.debug("Dismiss because not java file [{}]", path.getFileName());
                return;
            }

            logger.debug("try compile...");
            try (BufferedReader reader = new BufferedReader(new FileReader(pathStr))) {
                String classFqnPath = resolveClassFqn(path);
                String classFqn = classFqnPath.replace("/", ".").replace(".java", "");
                if (classFqn.startsWith(".")) {
                    classFqn = classFqn.replaceFirst(".","");
                }
                logger.debug("target class: {}", classFqn);

                String source = reader.lines().collect(Collectors.joining("\n"));
                logger.debug("source: \n{}",source);

                runtimeCompiler.compile(classFqnPath, source);
            } catch (IOException ioe) {
                logger.warn("IOException occurred",  ioe);
            } catch (Exception e) {
                logger.warn("Compile Error {}",e);
            }
        };
    }

    private String resolveClassFqn(Path fullPath) {
        String fullPathStr = fullPath.toString();
        int lastIndex = fullPathStr.lastIndexOf(Std_Java_Package_Path);
        return fullPathStr.substring(lastIndex+Std_Java_Package_Path.length(), fullPathStr.length());
    }
}