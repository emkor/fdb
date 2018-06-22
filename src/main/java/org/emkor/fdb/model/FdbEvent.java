package org.emkor.fdb.model;

import java.time.OffsetDateTime;

public class FdbEvent {
    protected OffsetDateTime creationDateTime;

    protected FdbEvent(OffsetDateTime creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    protected FdbEvent() {
    }

    public OffsetDateTime getCreationDateTime() {
        return creationDateTime;
    }

}
