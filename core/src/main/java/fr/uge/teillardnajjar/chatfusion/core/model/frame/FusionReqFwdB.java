package fr.uge.teillardnajjar.chatfusion.core.model.frame;

import fr.uge.teillardnajjar.chatfusion.core.model.ForwardedFusionLockInfo;

public record FusionReqFwdB(ForwardedFusionLockInfo info) implements Frame {
    @Override
    public void accept(FrameVisitor visitor) {
        visitor.visit(this);
    }
}