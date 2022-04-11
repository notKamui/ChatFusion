package fr.uge.teillardnajjar.chatfusion.core.model.part;

import java.util.Objects;

public record ForwardedIdentifiedFileChunk(
    String username,
    IdentifiedFileChunk chunk
) implements Part {
    public ForwardedIdentifiedFileChunk {
        Objects.requireNonNull(username);
        Objects.requireNonNull(chunk);
    }
}
