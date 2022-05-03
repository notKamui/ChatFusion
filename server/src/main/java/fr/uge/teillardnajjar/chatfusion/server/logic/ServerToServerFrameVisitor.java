package fr.uge.teillardnajjar.chatfusion.server.logic;

import fr.uge.teillardnajjar.chatfusion.core.context.Context;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FrameVisitor;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.Fusion;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionEnd;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionLinkAccept;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionLinkDeny;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionReqAccept;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionReqDeny;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionReqFwdA;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionReqFwdB;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.MsgFwd;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.PrivFileFwd;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.PrivMsgFwd;
import fr.uge.teillardnajjar.chatfusion.core.model.part.Identifier;
import fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes;

public class ServerToServerFrameVisitor implements FrameVisitor {
    private final Server server;
    private final ServerConnectionContext ctx;

    public ServerToServerFrameVisitor(Server server, ServerConnectionContext ctx) {
        this.server = server;
        this.ctx = ctx;
    }

    @Override
    public Context context() {
        return ctx;
    }

    @Override
    public void visit(MsgFwd frame) {
        server.broadcast(frame.message().toBuffer(), false);
    }

    @Override
    public void visit(PrivMsgFwd frame) {
        var msg = frame.message();
        server.sendTo(
            new Identifier(msg.username(), server.name()),
            msg.message().toBuffer(),
            OpCodes.PRIVMSGRESP,
            (byte) 0
        );
    }

    @Override
    public void visit(PrivFileFwd frame) {
        var chunk = frame.filechunk();
        server.sendTo(
            new Identifier(chunk.username(), server.name()),
            chunk.chunk().toBuffer(),
            OpCodes.PRIVFILERESP,
            (byte) 0
        );
    }

    @Override
    public void visit(FusionReqFwdA frame) {
        var inet = frame.inet();
        server.engageFusionRequest(inet.hostname(), inet.port());
    }

    @Override
    public void visit(FusionReqFwdB frame) {
        server.answerFusionRequest(frame.info());
    }

    @Override
    public void visit(FusionReqDeny frame) {
        System.out.println("Fusion request denied");
        ctx.readyToClose();
        server.unlock();
    }

    @Override
    public void visit(FusionReqAccept frame) {
        if (!server.isLeader()) ctx.silentlyClose();
        System.out.println("Fusion request accepted");
        server.acceptFusion(frame.info(), ctx);
    }

    @Override
    public void visit(Fusion frame) {
        if (server.isLeader()) ctx.silentlyClose();
        server.tryLink(frame.info());
    }

    @Override
    public void visit(FusionLinkAccept frame) {
        server.linkAccept(frame.info(), ctx);
    }

    @Override
    public void visit(FusionLinkDeny frame) {
        server.linkDeny(frame.info());
    }

    @Override
    public void visit(FusionEnd frame) {
        server.endFusion();
    }
}
