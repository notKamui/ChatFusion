package fr.uge.teillardnajjar.chatfusion.core.model.parts;

import java.util.List;
import java.util.Objects;

public record FusionLockInfo(
    ServerInfo self,
    List<ServerInfo> siblings
) {
    public FusionLockInfo {
        Objects.requireNonNull(self);
        Objects.requireNonNull(siblings);
    }
}
