package fr.uge.teillardnajjar.chatfusion.core.model;

import java.util.Objects;

public record IdentifiedMessage(
    String username,
    String servername,
    String message
) {
    public IdentifiedMessage {
        Objects.requireNonNull(username);
        Objects.requireNonNull(servername);
        Objects.requireNonNull(message);
    }
}
