package org.carbon.tools.hr.fw;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author ubuntu 2017/01/28.
 */
public class FileWatchEventQueue {
    private BlockingQueue<FileWatchEvent> queue;

    public FileWatchEventQueue() {
        this.queue = new LinkedBlockingDeque<>();
    }

    public void push(FileWatchEvent event) {
        try {
            this.queue.put(event);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pushAll(Collection<FileWatchEvent> events) {
        events.forEach(event -> {
            try {
                queue.put(event);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public FileWatchEvent waitTake() throws InterruptedException {
        return this.queue.take();
    }
}
