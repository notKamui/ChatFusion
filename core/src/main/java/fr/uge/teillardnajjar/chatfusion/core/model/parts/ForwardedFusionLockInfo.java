package fr.uge.teillardnajjar.chatfusion.core.model.parts;

import java.util.Objects;

public record ForwardedFusionLockInfo(
    ServerInfo leader,
    FusionLockInfo fusionLockInfo
) {
    public ForwardedFusionLockInfo {
        Objects.requireNonNull(leader);
        Objects.requireNonNull(fusionLockInfo);
    }
}
