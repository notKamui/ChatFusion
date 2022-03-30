package fr.uge.teillardnajjar.chatfusion.core.model;

import java.util.Objects;

public record ServerInfo(
    String servername,
    Type type,
    String address,
    int port
) {
    public ServerInfo {
        Objects.requireNonNull(servername);
        Objects.requireNonNull(type);
        Objects.requireNonNull(address);
        if (port < 0) throw new IllegalArgumentException("port must be positive");
    }

    public enum Type {IPv4, IPv6}
}
