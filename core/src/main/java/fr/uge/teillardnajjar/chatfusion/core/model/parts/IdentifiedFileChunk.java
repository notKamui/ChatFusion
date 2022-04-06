package fr.uge.teillardnajjar.chatfusion.core.model.parts;

import java.nio.ByteBuffer;
import java.util.Objects;

public record IdentifiedFileChunk(
    Identifier identifier,
    String filename,
    long fileSize,
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

    @Override
    public String toString() {
        return "%s file %s (size %dB)".formatted(identifier, filename, fileSize);
    }
}
