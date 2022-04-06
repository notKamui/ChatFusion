package fr.uge.teillardnajjar.chatfusion.core.model.parts;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public record ServerInfo(
    String servername,
    Type type,
    int ipv4,
    byte[] ipv6,
    short port
) {
    public ServerInfo {
        Objects.requireNonNull(servername);
        Objects.requireNonNull(type);
        if (port < 0) throw new IllegalArgumentException("port must be positive");
    }

    public ByteBuffer toBuffer() {
        var addressSize = type == Type.IPv4 ? 4 : 16;
        var buffer = ByteBuffer.allocate(5 + 1 + addressSize + 2)
            .put(StandardCharsets.US_ASCII.encode(servername))
            .put((byte) type.ordinal());
        if (type == Type.IPv4) {
            buffer.putInt(ipv4);
        } else {
            buffer.put(ipv6);
        }
        buffer.putShort(port).flip();
        return buffer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerInfo that = (ServerInfo) o;

        if (ipv4 != that.ipv4) return false;
        if (port != that.port) return false;
        if (!servername.equals(that.servername)) return false;
        if (type != that.type) return false;
        return Arrays.equals(ipv6, that.ipv6);
    }

    @Override
    public int hashCode() {
        int result = servername.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + ipv4;
        result = 31 * result + Arrays.hashCode(ipv6);
        result = 31 * result + (int) port;
        return result;
    }

    public enum Type {IPv4, IPv6}
}
