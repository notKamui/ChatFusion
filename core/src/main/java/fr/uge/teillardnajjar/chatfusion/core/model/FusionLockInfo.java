package fr.uge.teillardnajjar.chatfusion.core.model;

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
