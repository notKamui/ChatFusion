package fr.uge.teillardnajjar.chatfusion.core.model.part;

import java.net.InetSocketAddress;
import java.util.Objects;

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
}
