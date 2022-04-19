package fr.uge.teillardnajjar.chatfusion.serverold.logic;

import fr.uge.teillardnajjar.chatfusion.core.model.frame.FrameVisitor;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.Msg;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.PrivFile;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.PrivMsg;

import java.util.Objects;

public class ServerToClientFrameVisitor implements FrameVisitor {
    private final ServerToClientContext ctx;

    public ServerToClientFrameVisitor(ServerToClientContext ctx) {
        Objects.requireNonNull(ctx);
        this.ctx = ctx;
    }

    @Override
    public void visit(Msg frame) {
        ctx.broadcast(frame.message());
    }

    @Override
    public void visit(PrivMsg frame) {
        ctx.sendPrivMsg(frame.message());
    }

    @Override
    public void visit(PrivFile frame) {
        ctx.sendPrivFile(frame.identifiedFileChunk());
    }
}
