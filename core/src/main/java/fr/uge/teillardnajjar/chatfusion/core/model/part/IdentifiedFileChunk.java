package fr.uge.teillardnajjar.chatfusion.core.model.part;

import java.nio.ByteBuffer;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public record IdentifiedFileChunk(
    Identifier identifier,
    String filename,
    long fileSize,
    int fileId,
    ByteBuffer chunk
) implements Part {

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

    public IdentifiedFileChunk with(Identifier identifier) {
        return new IdentifiedFileChunk(identifier, filename, fileSize, fileId, chunk);
    }

    @Override
    public ByteBuffer toBuffer() {
        var idBuffer = identifier.toBuffer().flip();
        var fnameBuffer = UTF_8.encode(filename);
        var chunkBuffer = chunk.flip();
        return ByteBuffer.allocate(idBuffer.remaining() + Integer.BYTES * 3 + Long.BYTES + fnameBuffer.remaining() + chunkBuffer.remaining())
            .put(idBuffer)
            .putInt(fnameBuffer.remaining())
            .put(fnameBuffer)
            .putLong(fileSize)
            .putInt(fileId)
            .putInt(chunkBuffer.remaining())
            .put(chunkBuffer);
    }
}
