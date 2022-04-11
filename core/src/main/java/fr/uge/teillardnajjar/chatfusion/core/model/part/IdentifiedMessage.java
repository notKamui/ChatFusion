package fr.uge.teillardnajjar.chatfusion.core.model.part;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public record IdentifiedMessage(
    Identifier identifier,
    String message
) implements Part {
    private final static Charset ASCII = StandardCharsets.US_ASCII;
    private final static Charset UTF8 = StandardCharsets.UTF_8;

    public IdentifiedMessage {
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(message);
    }

    public ByteBuffer toUnflippedBuffer() {
        var unameBuffer = ASCII.encode(identifier.username());
        var snameBuffer = ASCII.encode(identifier.servername());
        var msgBuffer = UTF8.encode(message);
        var buffer = ByteBuffer.allocate(Integer.BYTES * 2 + 5 + unameBuffer.remaining() + msgBuffer.remaining());
        buffer
            .putInt(unameBuffer.remaining())
            .put(unameBuffer)
            .put(snameBuffer)
            .putInt(msgBuffer.remaining())
            .put(msgBuffer);
        return buffer;
    }

    @Override
    public String toString() {
        return "%s %s".formatted(identifier, message);
    }
}
