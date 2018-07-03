package org.emkor.fdb.util;

import org.emkor.fdb.model.FileStat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.function.Function;

public class ProvideFileMeta implements Function<Path, Optional<FileStat>> {

    @Override
    public Optional<FileStat> apply(Path path) {
        try {
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
            if (attributes.isRegularFile()) {
                OffsetDateTime accessTime = fromFileTime(attributes.lastAccessTime());
                OffsetDateTime modificationTime = fromFileTime(attributes.lastModifiedTime());
                OffsetDateTime creationTime = fromFileTime(attributes.creationTime());
                return Optional.of(new FileStat(attributes.size(), accessTime, modificationTime, creationTime));
            } else {
                System.out.println("File at path " + path + " is not a regular file");
                return Optional.empty();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private OffsetDateTime fromFileTime(FileTime fileTime) {
        return OffsetDateTime.ofInstant(fileTime.toInstant(), ZoneOffset.UTC);
    }
}
