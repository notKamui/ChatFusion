package fr.uge.teillardnajjar.chatfusion.core.model.part;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.US_ASCII;

public record ServerInfo(
    String servername,
    InetAddress ip,
    short port
) implements Part {
    public ServerInfo {
        Objects.requireNonNull(servername);
        if (port < 0) throw new IllegalArgumentException("port must be positive");
    }

    @Override
    public ByteBuffer toBuffer() {
        byte type;
        var ip = this.ip.getAddress();
        if (ip.length == 4) type = 0;
        else if (ip.length == 16) type = 1;
        else throw new IllegalArgumentException("ip must be 4 or 16 bytes long");
        return ByteBuffer.allocate(5 + 1 + ip.length + 2)
            .put(US_ASCII.encode(servername))
            .put(type)
            .put(ip)
            .putShort(port);
    }
}
