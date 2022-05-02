package fr.uge.teillardnajjar.chatfusion.server.logic;

import fr.uge.teillardnajjar.chatfusion.core.context.Context;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FrameVisitor;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionReq;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionReqAccept;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionReqDeny;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.Temp;

public class ServerToUnknownFrameVisitor implements FrameVisitor {

    private final Server server;
    private final ServerConnectionContext ctx;

    public ServerToUnknownFrameVisitor(Server server, ServerConnectionContext ctx) {
        this.server = server;
        this.ctx = ctx;
    }

    @Override
    public Context context() {
        return ctx;
    }

    @Override
    public void visit(Temp frame) {
        var username = frame.username();
        if (server.checkLogin(username, ctx)) {
            ctx.confirmUser(username);
        } else {
            ctx.refuseUser();
        }
    }

    @Override
    public void visit(FusionReq frame) {
        server.treatFusionRequest(frame.info(), ctx);
    }

    @Override
    public void visit(FusionReqDeny frame) {
        System.out.println("Fusion request denied");
        ctx.readyToClose();
        server.unlock();
    }

    @Override
    public void visit(FusionReqAccept frame) {
        System.out.println("Fusion request accepted");
        server.acceptFusion(frame.info(), ctx);
    }
}
