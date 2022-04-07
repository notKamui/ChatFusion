package fr.uge.teillardnajjar.chatfusion.server.logic;

import fr.uge.teillardnajjar.chatfusion.core.model.frame.FrameVisitor;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionLink;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionReq;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.Temp;

public class GenericFrameVisitor implements FrameVisitor {
    private final GenericContext ctx;

    public GenericFrameVisitor(GenericContext ctx) {
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
    public void visit(FusionReq frame) {
        if (!ctx.serverIsLeader()) {
            ctx.forwardFusionReq(frame.info());
        } else if (ctx.isFusionLocked() || !ctx.checkServers(frame.info())) {
            ctx.queueFusionReqDeny();
        } else {
            ctx.acceptFusion(frame.info());
        }
    }

    @Override
    public void visit(FusionLink frame) {
        var info = frame.info();
        if (ctx.checkLinkServer(info)) {
            ctx.acceptLink(info);
        } else {
            ctx.refuseLink();
        }
    }
}
