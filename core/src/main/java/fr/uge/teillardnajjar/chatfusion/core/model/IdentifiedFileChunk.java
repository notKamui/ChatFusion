package fr.uge.teillardnajjar.chatfusion.core.model;

import java.nio.ByteBuffer;
import java.util.Objects;

public record IdentifiedFileChunk(
    Identifier identifier,
    String filename,
    int fileSize,
    int fileId,
    ByteBuffer chunk
) {
    public IdentifiedFileChunk {
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(filename);
        Objects.requireNonNull(chunk);
        if (fileSize <= 0) throw new IllegalArgumentException("fileSize must be strictly positive");
        if (fileId < 0) throw new IllegalArgumentException("fileId must be positive");
    }
}
