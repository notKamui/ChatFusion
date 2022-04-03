package fr.uge.teillardnajjar.chatfusion.core.model.frame;

public record FusionLinkAccept() implements Frame {
    @Override
    public void accept(FrameVisitor visitor) {
        visitor.visit(this);
    }
}
