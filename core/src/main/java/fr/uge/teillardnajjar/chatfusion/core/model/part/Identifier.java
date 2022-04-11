package fr.uge.teillardnajjar.chatfusion.core.model.part;

import java.util.Objects;

public record Identifier(
    String username,
    String servername
) implements Part {
    public Identifier {
        Objects.requireNonNull(username);
        Objects.requireNonNull(servername);
    }

    @Override
    public String toString() {
        return "[%s] %s :".formatted(servername, username);
    }
}
