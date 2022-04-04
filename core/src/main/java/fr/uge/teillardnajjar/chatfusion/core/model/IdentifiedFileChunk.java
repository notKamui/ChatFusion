package fr.uge.teillardnajjar.chatfusion.core.model;

import java.nio.ByteBuffer;
import java.util.Objects;

public record IdentifiedFileChunk(
    Identifier identifier,
    String filename,
    int fileSize,
    int fileId,
    ByteBuffer chunk
) implements Comparable<IdentifiedFileChunk> {
    public IdentifiedFileChunk {
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(filename);
        Objects.requireNonNull(chunk);
        if (fileSize <= 0) throw new IllegalArgumentException("fileSize must be strictly positive");
        if (fileId < 0) throw new IllegalArgumentException("fileId must be positive");
    }

    @Override
    public String toString() {
        return "%s file %s (size %dB)".formatted(identifier, filename, fileSize);
    }

    @Override
    public int compareTo(IdentifiedFileChunk o) {
        return this.fileId - o.fileId;
    }
}
