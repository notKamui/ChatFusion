package fr.uge.teillardnajjar.chatfusion.core.model;

import java.util.Objects;

public record ServerInfo(
    String servername,
    Type type,
    int ipv4,
    long ipv6,
    short port
) {
    public ServerInfo {
        Objects.requireNonNull(servername);
        Objects.requireNonNull(type);
        if (port < 0) throw new IllegalArgumentException("port must be positive");
    }

    public enum Type {IPv4, IPv6}
}
