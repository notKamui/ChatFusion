package fr.uge.teillardnajjar.chatfusion.core.model.part;

import java.nio.ByteBuffer;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.US_ASCII;

public record ForwardedIdentifiedFileChunk(
    String username,
    IdentifiedFileChunk chunk
) implements Part {
    public ForwardedIdentifiedFileChunk {
        Objects.requireNonNull(username);
        Objects.requireNonNull(chunk);
    }

    @Override
    public ByteBuffer toBuffer() {
        var unameBuffer = US_ASCII.encode(username);
        var chunkBuffer = chunk.toBuffer().flip();
        return ByteBuffer.allocate(Integer.BYTES + unameBuffer.remaining() + chunkBuffer.remaining())
            .putInt(unameBuffer.remaining())
            .put(unameBuffer)
            .put(chunkBuffer);
    }
}
