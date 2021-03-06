package fr.uge.teillardnajjar.chatfusion.core.model.frame;

import fr.uge.teillardnajjar.chatfusion.core.model.part.IdentifiedMessage;

public record MsgFwd(IdentifiedMessage message) implements Frame {
    @Override
    public void accept(FrameVisitor visitor) {
        visitor.visit(this);
    }
}
