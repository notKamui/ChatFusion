package fr.uge.teillardnajjar.chatfusion.core.model;

import java.util.Objects;

public record IdentifiedMessage(
    Identifier identifier,
    String message
) {
    public IdentifiedMessage {
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(message);
    }
}
