package fr.uge.teillardnajjar.chatfusion.server.logic;

import fr.uge.teillardnajjar.chatfusion.core.model.frame.FrameVisitor;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.Msg;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.PrivFile;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.PrivMsg;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.Temp;

import java.util.Objects;

public class ServerToClientFrameVisitor implements FrameVisitor {
    private final ServerToClientContext ctx;

    public ServerToClientFrameVisitor(ServerToClientContext ctx) {
        Objects.requireNonNull(ctx);
        this.ctx = ctx;
    }


    @Override
    public void visit(Temp frame) {
        var username = frame.username();
        if (ctx.checkLogin(username)) {
            ctx.confirmUser(username);
        } else {
            ctx.refuseUser();
        }
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
