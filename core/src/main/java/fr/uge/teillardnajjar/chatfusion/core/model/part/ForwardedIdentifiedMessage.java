package fr.uge.teillardnajjar.chatfusion.core.model.part;

import java.util.Objects;

public record ForwardedIdentifiedMessage(
    String username,
    IdentifiedMessage message
) implements Part {
    public ForwardedIdentifiedMessage {
        Objects.requireNonNull(username);
        Objects.requireNonNull(message);
    }
}
