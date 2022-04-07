package fr.uge.teillardnajjar.chatfusion.core.model.frame;

import fr.uge.teillardnajjar.chatfusion.core.model.parts.ForwardedIdentifiedFileChunk;

public record PrivFileFwd(ForwardedIdentifiedFileChunk filechunk) implements Frame {
    @Override
    public void accept(FrameVisitor visitor) {
        visitor.visit(this);
    }
}
