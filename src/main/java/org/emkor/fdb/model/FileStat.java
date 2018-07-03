package org.emkor.fdb.model;

import java.time.OffsetDateTime;

public class FileStat {

    protected Long sizeBytes;
    protected OffsetDateTime access;
    protected OffsetDateTime modify;
    protected OffsetDateTime change;

    public FileStat() {
    }

    public FileStat(Long sizeBytes, OffsetDateTime access, OffsetDateTime modify, OffsetDateTime change) {
        this.sizeBytes = sizeBytes;
        this.access = access;
        this.modify = modify;
        this.change = change;
    }



}
