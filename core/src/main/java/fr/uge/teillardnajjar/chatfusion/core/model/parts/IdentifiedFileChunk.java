package fr.uge.teillardnajjar.chatfusion.core.model.parts;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public record IdentifiedFileChunk(
    Identifier identifier,
    String filename,
    long fileSize,
    int fileId,
    ByteBuffer chunk
) {
    private final static Charset ASCII = StandardCharsets.US_ASCII;
    private static final Charset UTF8 = StandardCharsets.UTF_8;

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

    public ByteBuffer toUnflippedBuffer() {
        var unameBuffer = ASCII.encode(identifier.username());
        var snameBuffer = ASCII.encode(identifier.servername());
        var fnameBuffer = UTF8.encode(filename);
        var chunkBuffer = chunk.flip();
        var buffer = ByteBuffer.allocate(Integer.BYTES * 4 + 5 + Long.BYTES + unameBuffer.remaining() + snameBuffer.remaining() + fnameBuffer.remaining() + chunkBuffer.remaining());
        buffer
            .putInt(unameBuffer.remaining())
            .put(unameBuffer)
            .put(snameBuffer)
            .putInt(fnameBuffer.remaining())
            .put(fnameBuffer)
            .putLong(fileSize)
            .putInt(fileId)
            .putInt(chunkBuffer.remaining())
            .put(chunkBuffer);
        return buffer;
    }
}
