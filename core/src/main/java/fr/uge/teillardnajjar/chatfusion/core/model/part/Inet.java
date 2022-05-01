package fr.uge.teillardnajjar.chatfusion.core.model.part;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.US_ASCII;

public record Inet(
    String hostname,
    short port
) implements Part {
    public Inet {
        Objects.requireNonNull(hostname);
        if (port < 0) throw new IllegalArgumentException("port must be >= 0");
    }

    public InetSocketAddress address() {
        return new InetSocketAddress(hostname, port);
    }

    @Override
    public ByteBuffer toBuffer() {
        var hnameBuffer = US_ASCII.encode(hostname);
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + hnameBuffer.remaining() + Short.BYTES);
        buffer.putInt(hnameBuffer.remaining());
        buffer.put(hnameBuffer);
        buffer.putShort(port);
        return buffer;
    }
}
