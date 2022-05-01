package fr.uge.teillardnajjar.chatfusion.server.logic;

import fr.uge.teillardnajjar.chatfusion.core.context.Context;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FrameVisitor;

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
}
