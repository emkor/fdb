package org.emkor.fdb;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class FileWatcherVerticle extends AbstractVerticle {
    private WatchService watcher = null;
    private Map<WatchKey, Path> watchKeys = new HashMap<>();
    private List<String> watchDirs = Collections.singletonList("/home/mat/Downloads/img_size");

    @Override
    public void start() {

        try {
            watcher = FileSystems.getDefault().newWatchService();
            for (String dir : watchDirs) {
                Path dirPath = Paths.get(dir);
                WatchKey dirKey = dirPath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
                watchKeys.put(dirKey, dirPath);
            }

            vertx.setPeriodic(500, timerId -> {
                try {
                    processEvents();
                } catch (Exception exc) {
                    exc.printStackTrace();
                }
            });
        } catch (ClosedWatchServiceException | IOException exc) {
            exc.printStackTrace();
        }
    }

    private void processEvents() throws Exception {
        for (; ; ) {
            WatchKey key;
            key = watcher.poll();
            // watch key is null if no queued key is available (within the specified timeframe if a timeout was specified on the poll() request)
            if (key == null) break;


            Path dir = watchKeys.get(key);
            if (dir == null) {
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent<Path> watchEvent = (WatchEvent<Path>) event;
                WatchEvent.Kind<Path> kind = watchEvent.kind();

                if (kind.name().equals(StandardWatchEventKinds.OVERFLOW.name())) {
                    continue;
                }

                //The filename is the context of the event.
                Path filename = watchEvent.context();
                JsonObject watchMsg = new JsonObject();
                // Add filename to message
                watchMsg.put("filename", filename.toString());
                // Add file location (dir) to message
                watchMsg.put("location", dir.toString());
                // Add type of event (create, modify, delete)
                watchMsg.put("type", kind.name());
                System.out.println("Publishing message: " + watchMsg.encode());
                // publish on eventbus
                vertx.eventBus().publish("app.filechanges", watchMsg);
            }

            //Reset the key -- this step is critical if you want to receive
            //further watch events. If the key is no longer valid, the directory
            //is inaccessible so exit the loop.
            boolean valid = key.reset();
            if (!valid) {
                watchKeys.remove(key);
                // Exit if no keys remain
                if (watchKeys.isEmpty()) {
                    break;
                }
            }
        }
    }

}
