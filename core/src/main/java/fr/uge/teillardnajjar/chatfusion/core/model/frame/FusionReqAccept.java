package fr.uge.teillardnajjar.chatfusion.core.model.frame;

import fr.uge.teillardnajjar.chatfusion.core.model.FusionLockInfo;

public record FusionReqAccept(FusionLockInfo info) implements Frame {
}
