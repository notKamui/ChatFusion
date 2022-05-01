package fr.uge.teillardnajjar.chatfusion.core.model.part;

import java.nio.ByteBuffer;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.US_ASCII;

public record Identifier(
    String username,
    String servername
) implements Part {
    public Identifier {
        Objects.requireNonNull(username);
        Objects.requireNonNull(servername);
    }

    @Override
    public String toString() {
        return "[%s] %s :".formatted(servername, username);
    }

    @Override
    public ByteBuffer toBuffer() {
        var unameBuffer = US_ASCII.encode(username);
        var snameBuffer = US_ASCII.encode(servername);
        return ByteBuffer.allocate(Integer.BYTES + unameBuffer.remaining() + 5)
            .putInt(unameBuffer.remaining())
            .put(unameBuffer)
            .put(snameBuffer);
    }
}
