package org.emkor.fdb.model;

import java.time.OffsetDateTime;
import java.util.Objects;

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

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public OffsetDateTime getAccess() {
        return access;
    }

    public OffsetDateTime getModify() {
        return modify;
    }

    public OffsetDateTime getChange() {
        return change;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileStat fileStat = (FileStat) o;
        return Objects.equals(sizeBytes, fileStat.sizeBytes) &&
                Objects.equals(access, fileStat.access) &&
                Objects.equals(modify, fileStat.modify) &&
                Objects.equals(change, fileStat.change);
    }

    @Override
    public int hashCode() {

        return Objects.hash(sizeBytes, access, modify, change);
    }

    @Override
    public String toString() {
        return "FileStat{" +
                "sizeBytes=" + sizeBytes +
                ", access=" + access +
                ", modify=" + modify +
                ", change=" + change +
                '}';
    }
}
