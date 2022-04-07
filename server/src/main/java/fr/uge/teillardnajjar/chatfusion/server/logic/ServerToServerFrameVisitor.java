package fr.uge.teillardnajjar.chatfusion.server.logic;

import fr.uge.teillardnajjar.chatfusion.core.model.frame.FrameVisitor;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.Fusion;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionReqAccept;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionReqDeny;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionReqFwdA;

public class ServerToServerFrameVisitor implements FrameVisitor {
    private final ServerToServerContext ctx;

    public ServerToServerFrameVisitor(ServerToServerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void visit(FusionReqFwdA frame) {
        ctx.fusion(frame.inet());
    }

    @Override
    public void visit(FusionReqDeny frame) {
        ctx.denyFusionReq();
    }

    @Override
    public void visit(FusionReqAccept frame) {
        ctx.engageFusion(frame.info());
    }

    @Override
    public void visit(Fusion frame) {
        ctx.engageFusion(frame.info());
    }
}
