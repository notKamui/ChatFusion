package fr.uge.teillardnajjar.chatfusion.core.model.parts;

import java.util.Objects;

public record ForwardedIdentifiedMessage(String username, IdentifiedMessage message) {
    public ForwardedIdentifiedMessage {
        Objects.requireNonNull(username);
        Objects.requireNonNull(message);
    }
}
