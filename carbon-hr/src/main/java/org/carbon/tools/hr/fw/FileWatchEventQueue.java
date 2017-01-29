package org.carbon.tools.hr.fw;

import java.util.Collection;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author ubuntu 2017/01/28.
 */
public class FileWatchEventQueue {
    private BlockingDeque<FileWatchEvent> queue;

    public FileWatchEventQueue() {
        this.queue = new LinkedBlockingDeque<>();
    }

    public void push(FileWatchEvent event) {
        this.queue.add(event);
    }

    public void pushAll(Collection<FileWatchEvent> events) {
        events.forEach(event -> {
            try {
                queue.putLast(event);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public FileWatchEvent take() {
        try {
            return this.queue.takeFirst();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
