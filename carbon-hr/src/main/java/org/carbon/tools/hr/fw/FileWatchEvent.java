package org.carbon.tools.hr.fw;

import java.nio.file.Path;

/**
 * @author ubuntu 2017/01/28.
 */
public class FileWatchEvent {
    private Path absFilePath;

    public FileWatchEvent(Path absFilePath) {
        this.absFilePath = absFilePath;
    }

    public Path getAbsFilePath() {
        return absFilePath;
    }
}
