package fr.uge.teillardnajjar.chatfusion.core.model.part;

import java.nio.ByteBuffer;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.US_ASCII;

public record ForwardedIdentifiedMessage(
    String username,
    IdentifiedMessage message
) implements Part {
    public ForwardedIdentifiedMessage {
        Objects.requireNonNull(username);
        Objects.requireNonNull(message);
    }

    @Override
    public ByteBuffer toBuffer() {
        var unameBuffer = US_ASCII.encode(username);
        var msgBuffer = message.toBuffer().flip();
        return ByteBuffer.allocate(Integer.BYTES + unameBuffer.remaining() + msgBuffer.remaining())
            .putInt(unameBuffer.remaining())
            .put(unameBuffer)
            .put(msgBuffer);
    }
}
