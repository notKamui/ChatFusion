package fr.uge.teillardnajjar.chatfusion.core.model.parts;

import java.util.Objects;

public record IdentifiedMessage(
    Identifier identifier,
    String message
) {
    public IdentifiedMessage {
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(message);
    }

    @Override
    public String toString() {
        return "%s %s".formatted(identifier, message);
    }
}
