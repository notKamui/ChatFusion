package fr.uge.teillardnajjar.chatfusion.core.model.parts;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public record ServerInfo(
    String servername,
    byte[] ip,
    short port
) {
    public ServerInfo {
        Objects.requireNonNull(servername);
        if (port < 0) throw new IllegalArgumentException("port must be positive");
    }

    public ByteBuffer toBuffer() {
        byte type;
        if (ip.length == 4) type = 0;
        else if (ip.length == 16) type = 1;
        else throw new IllegalArgumentException("ip must be 4 or 16 bytes long");
        return ByteBuffer.allocate(5 + 1 + ip.length + 2)
            .put(StandardCharsets.US_ASCII.encode(servername))
            .put(type)
            .put(ip)
            .putShort(port)
            .flip();
    }

    public enum Type {IPv4, IPv6}
}
