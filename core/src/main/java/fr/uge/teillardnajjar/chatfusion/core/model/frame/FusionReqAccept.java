package fr.uge.teillardnajjar.chatfusion.core.model.frame;

import fr.uge.teillardnajjar.chatfusion.core.model.part.FusionLockInfo;

public record FusionReqAccept(FusionLockInfo info) implements Frame {
    @Override
    public void accept(FrameVisitor visitor) {
        visitor.visit(this);
    }
}
