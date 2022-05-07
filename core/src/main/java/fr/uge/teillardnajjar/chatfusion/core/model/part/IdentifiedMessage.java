package fr.uge.teillardnajjar.chatfusion.core.model.part;

import java.nio.ByteBuffer;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public record IdentifiedMessage(
    Identifier identifier,
    String message
) implements Part {

    public IdentifiedMessage {
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(message);
    }

    @Override
    public String toString() {
        return "%s %s".formatted(identifier, message);
    }

    public IdentifiedMessage with(Identifier identifier) {
        return new IdentifiedMessage(identifier, message);
    }

    @Override
    public ByteBuffer toBuffer() {
        var idBuffer = identifier.toBuffer().flip();
        var msgBuffer = UTF_8.encode(message);
        return ByteBuffer.allocate(idBuffer.remaining() + Integer.BYTES + msgBuffer.remaining())
            .put(idBuffer)
            .putInt(msgBuffer.remaining())
            .put(msgBuffer);
    }
}
