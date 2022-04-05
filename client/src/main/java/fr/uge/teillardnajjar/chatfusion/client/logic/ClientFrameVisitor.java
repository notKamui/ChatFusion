package fr.uge.teillardnajjar.chatfusion.client.logic;

import fr.uge.teillardnajjar.chatfusion.core.model.frame.FrameVisitor;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.MsgResp;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.PrivFileResp;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.PrivMsgResp;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.TempKo;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.TempOk;

import java.util.Objects;

final class ClientFrameVisitor implements FrameVisitor {
    private final Client client;
    private final ClientContext ctx;

    ClientFrameVisitor(Client client, ClientContext ctx) {
        Objects.requireNonNull(client);
        Objects.requireNonNull(ctx);
        this.client = client;
        this.ctx = ctx;
    }

    @Override
    public void visit(TempOk frame) {
        client.logMessage("Welcome to the chat, %s!".formatted(client.login()));
    }

    @Override
    public void visit(TempKo frame) {
        client.logMessage("Connection has been refused by the server...");
        ctx.silentlyClose();
    }

    @Override
    public void visit(MsgResp frame) {
        client.logMessage(frame.message(), false);
    }

    @Override
    public void visit(PrivMsgResp frame) {
        client.logMessage(frame.message(), true);
    }

    @Override
    public void visit(PrivFileResp frame) {
        ctx.feedChunk(frame.identifiedFileChunk());
    }
}
