package org.emkor.fdb.model;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Objects;


public class InotifyEvent extends FdbEvent {
    private final Path filePath;

    private final InotifyEventType eventType;

    public InotifyEvent(OffsetDateTime creationDateTime, Path filePath, InotifyEventType eventType) {
        super(creationDateTime);
        this.filePath = filePath;
        this.eventType = eventType;
    }

    public Path getFilePath() {
        return filePath;
    }

    public InotifyEventType getEventType() {
        return eventType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InotifyEvent that = (InotifyEvent) o;
        return Objects.equals(filePath, that.filePath) &&
                eventType == that.eventType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath, eventType);
    }

    @Override
    public String toString() {
        return "InotifyEvent{" +
                "creationDateTime=" + creationDateTime +
                ", eventType=" + eventType +
                ", filePath=" + filePath +
                '}';
    }
}
