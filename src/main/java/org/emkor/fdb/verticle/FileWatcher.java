package org.emkor.fdb.verticle;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import org.emkor.fdb.codec.ModelSerializer;
import org.emkor.fdb.model.InotifyEvent;
import org.emkor.fdb.model.InotifyEventType;
import org.emkor.fdb.model.QueueAddress;
import org.msgpack.jackson.dataformat.MessagePackFactory;


public class FileWatcher extends AbstractVerticle {
    private static final Map<WatchEvent.Kind<Path>, InotifyEventType> watchEventToEventMap = new HashMap<>();

    static {
        watchEventToEventMap.put(StandardWatchEventKinds.ENTRY_CREATE, InotifyEventType.CREATE);
        watchEventToEventMap.put(StandardWatchEventKinds.ENTRY_DELETE, InotifyEventType.DELETE);
        watchEventToEventMap.put(StandardWatchEventKinds.ENTRY_MODIFY, InotifyEventType.UPDATE);
    }

    private ModelSerializer<InotifyEvent> codec = null;
    private WatchService watcher = null;
    private Map<WatchKey, Path> watchKeys = new HashMap<>();
    private List<String> watchDirs = new ArrayList<>();

    @Override
    public void start() {
        watchDirs = readWatchedDirsConfig();
        codec = new ModelSerializer<>(new ObjectMapper(new MessagePackFactory())
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule()), InotifyEvent.class);
        try {
            watcher = FileSystems.getDefault().newWatchService();
            registerWatchers();
            pollInotifyPeriodically();
        } catch (ClosedWatchServiceException | IOException exc) {
            exc.printStackTrace();
        }
    }

    private List<String> readWatchedDirsConfig() {
        JsonObject config = config();
        return (List<String>) config.getJsonArray("watched_directories").getList();
    }

    private void pollInotifyPeriodically() {
        vertx.setPeriodic(500, timerId -> {
            try {
                handleInotifyEvents();
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        });
    }

    private void registerWatchers() throws IOException {
        for (String dir : watchDirs) {
            Path dirPath = Paths.get(dir);
            WatchKey dirKey = dirPath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            watchKeys.put(dirKey, dirPath);
        }
    }

    private void handleInotifyEvents() throws Exception {
        for (; ; ) {
            WatchKey watchKey;
            watchKey = watcher.poll();
            if (watchKey == null) break;

            Path dir = watchKeys.get(watchKey);
            if (dir == null) {
                continue;
            }

            for (WatchEvent<?> event : watchKey.pollEvents()) {
                WatchEvent<Path> watchEvent = (WatchEvent<Path>) event;
                WatchEvent.Kind<Path> kind = watchEvent.kind();

                if (kind.name().equals(StandardWatchEventKinds.OVERFLOW.name())) {
                    continue;
                }
                InotifyEvent fdbEvent = buildEvent(dir, watchEvent, kind);
                vertx.eventBus().publish(QueueAddress.inotify, codec.serialize(fdbEvent));
            }

            //Reset the watchKey -- this step is critical if you want to receive
            //further watch events. If the watchKey is no longer valid, the directory
            //is inaccessible so exit the loop.
            boolean valid = watchKey.reset();
            if (!valid) {
                watchKeys.remove(watchKey);
                if (watchKeys.isEmpty()) {
                    break;
                }
            }
        }
    }

    private InotifyEvent buildEvent(Path directory, WatchEvent<Path> watchEvent, WatchEvent.Kind<Path> kind) {
        Path fullPath = Paths.get(directory.toString(), watchEvent.context().toString());
        InotifyEventType eventType = watchEventToEventMap.get(kind);
        try {
            BasicFileAttributes attr = Files.readAttributes(fullPath, BasicFileAttributes.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new InotifyEvent(OffsetDateTime.now(ZoneOffset.UTC), fullPath, eventType);
    }

}
