package fr.uge.teillardnajjar.chatfusion.server.logic;

import fr.uge.teillardnajjar.chatfusion.core.context.Context;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FrameVisitor;
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
}
