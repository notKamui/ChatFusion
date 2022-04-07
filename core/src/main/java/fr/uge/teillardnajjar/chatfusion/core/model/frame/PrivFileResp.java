package fr.uge.teillardnajjar.chatfusion.core.model.frame;

import fr.uge.teillardnajjar.chatfusion.core.model.parts.IdentifiedFileChunk;

public record PrivFileResp(IdentifiedFileChunk identifiedFileChunk) implements Frame {
    @Override
    public void accept(FrameVisitor visitor) {
        visitor.visit(this);
    }
}
