package org.emkor.fdb.model;

import java.time.OffsetDateTime;

public class FdbEvent {
    protected final OffsetDateTime creationDateTime;

    public FdbEvent(OffsetDateTime creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public OffsetDateTime getCreationDateTime() {
        return creationDateTime;
    }

}
