package org.emkor.fdb.verticle;

import java.io.IOException;
import java.nio.file.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import org.emkor.fdb.util.ModelSerializer;
import org.emkor.fdb.model.InotifyEvent;
import org.emkor.fdb.model.InotifyEventType;
import org.emkor.fdb.model.QueueAddress;
import org.msgpack.jackson.dataformat.MessagePackFactory;


public class FileWatcher extends AbstractVerticle {
    private static final Map<WatchEvent.Kind<Path>, InotifyEventType> inotifyEventToFdbEventTypeMap = new HashMap<>();

    static {
        inotifyEventToFdbEventTypeMap.put(StandardWatchEventKinds.ENTRY_CREATE, InotifyEventType.CREATE);
        inotifyEventToFdbEventTypeMap.put(StandardWatchEventKinds.ENTRY_MODIFY, InotifyEventType.UPDATE);
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
            WatchKey dirKey = dirPath.register(watcher,
                    StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
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

            List<InotifyEvent> unfilteredEvents = collectInotifyEvents(watchKey, dir);
            for (InotifyEvent e : selectLastEventPerFile(unfilteredEvents)) {
                vertx.eventBus().publish(QueueAddress.inotify, codec.serialize(e));
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

    private List<InotifyEvent> collectInotifyEvents(WatchKey watchKey, Path dir) {
        List<InotifyEvent> unfilteredEvents = new ArrayList<>();
        for (WatchEvent<?> event : watchKey.pollEvents()) {
            WatchEvent<Path> watchEvent = (WatchEvent<Path>) event;
            WatchEvent.Kind<Path> kind = watchEvent.kind();
            if (kind.name().equals(StandardWatchEventKinds.OVERFLOW.name())) {
                continue;
            }
            unfilteredEvents.add(buildEvent(dir, watchEvent, kind));
        }
        return unfilteredEvents;
    }

    private List<InotifyEvent> selectLastEventPerFile(List<InotifyEvent> events) {
        Map<Path, List<InotifyEvent>> fileToEventsMap = events.stream().collect(Collectors.groupingBy(InotifyEvent::getFilePath));
        List<InotifyEvent> filteredEvents = new ArrayList<>();
        for (Map.Entry<Path, List<InotifyEvent>> entry : fileToEventsMap.entrySet()) {
            InotifyEvent lastEvent = entry.getValue().get(entry.getValue().size() - 1);
            filteredEvents.add(lastEvent);
        }
        return filteredEvents;
    }

    private InotifyEvent buildEvent(Path directory, WatchEvent<Path> watchEvent, WatchEvent.Kind<Path> kind) {
        Path fullPath = Paths.get(directory.toString(), watchEvent.context().toString());
        InotifyEventType eventType = inotifyEventToFdbEventTypeMap.get(kind);
        return new InotifyEvent(OffsetDateTime.now(ZoneOffset.UTC), fullPath, eventType);
    }

}
