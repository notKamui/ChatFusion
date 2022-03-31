package fr.uge.teillardnajjar.chatfusion.core.model;

import java.util.Objects;

public record Identifier(
    String username,
    String servername
) {
    public Identifier {
        Objects.requireNonNull(username);
        Objects.requireNonNull(servername);
    }
}
