package fr.uge.teillardnajjar.chatfusion.server.logic;

import fr.uge.teillardnajjar.chatfusion.core.context.Context;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FrameVisitor;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.Msg;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.PrivFile;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.PrivMsg;
import fr.uge.teillardnajjar.chatfusion.core.model.part.IdentifiedMessage;
import fr.uge.teillardnajjar.chatfusion.core.model.part.Identifier;
import fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes;

public class ServerToClientFrameVisitor implements FrameVisitor {

    private final String username;
    private final Server server;
    private final ServerConnectionContext ctx;

    public ServerToClientFrameVisitor(String username, Server server, ServerConnectionContext ctx) {
        this.username = username;
        this.server = server;
        this.ctx = ctx;
    }

    @Override
    public Context context() {
        return ctx;
    }

    public String username() {
        return username;
    }

    @Override
    public void visit(Msg frame) {
        var msgBuffer = new IdentifiedMessage(
            new Identifier(username, server.name()),
            frame.message()
        ).toUnflippedBuffer();
        server.broadcast(msgBuffer);
    }

    @Override
    public void visit(PrivMsg frame) {
        var msgBuffer = new IdentifiedMessage(
            new Identifier(username, server.name()),
            frame.message().message()
        ).toUnflippedBuffer();
        server.sendTo(
            frame.message().identifier(),
            msgBuffer,
            OpCodes.PRIVMSGRESP,
            OpCodes.PRIVMSGFWD
        );
    }

    @Override
    public void visit(PrivFile frame) {
        // TODO
    }
}
