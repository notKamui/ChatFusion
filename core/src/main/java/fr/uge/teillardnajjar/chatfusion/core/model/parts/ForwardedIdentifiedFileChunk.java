package fr.uge.teillardnajjar.chatfusion.core.model.parts;

import java.util.Objects;

public record ForwardedIdentifiedFileChunk(
    String username,
    IdentifiedFileChunk chunk
) {
    public ForwardedIdentifiedFileChunk {
        Objects.requireNonNull(username);
        Objects.requireNonNull(chunk);
    }
}
