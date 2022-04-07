package fr.uge.teillardnajjar.chatfusion.core.model.parts;

import java.util.Objects;

public record Inet(
    String hostname,
    short port
) {
    public Inet {
        Objects.requireNonNull(hostname);
        if (port < 0) throw new IllegalArgumentException("port must be >= 0");
    }
}
