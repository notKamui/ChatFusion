package fr.uge.teillardnajjar.chatfusion.client;

import fr.uge.teillardnajjar.chatfusion.core.model.frame.FrameVisitor;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.Fusion;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionEnd;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionLink;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionLinkAccept;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionReq;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionReqAccept;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionReqDeny;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionReqFwdA;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionReqFwdB;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.Msg;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.MsgFwd;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.MsgResp;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.PrivFile;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.PrivFileFwd;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.PrivFileResp;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.PrivMsg;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.PrivMsgFwd;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.PrivMsgResp;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.Temp;
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
        if (ctx.feedChunk(frame.identifiedFileChunk())) {
            client.logMessage(frame.identifiedFileChunk());
        }
    }

    /////////////////////////////// Invalid packets BEGIN //////////////////////////////////

    @Override
    public void visit(Temp frame) {
        throw new AssertionError();
    }

    @Override
    public void visit(Msg frame) {
        throw new AssertionError();
    }

    @Override
    public void visit(MsgFwd frame) {
        throw new AssertionError();
    }

    @Override
    public void visit(PrivMsg frame) {
        throw new AssertionError();
    }

    @Override
    public void visit(PrivMsgFwd frame) {
        throw new AssertionError();
    }

    @Override
    public void visit(PrivFile frame) {
        throw new AssertionError();
    }

    @Override
    public void visit(PrivFileFwd frame) {
        throw new AssertionError();
    }

    @Override
    public void visit(FusionReq frame) {
        throw new AssertionError();
    }

    @Override
    public void visit(FusionReqFwdA frame) {
        throw new AssertionError();
    }

    @Override
    public void visit(FusionReqDeny frame) {
        throw new AssertionError();
    }

    @Override
    public void visit(FusionReqAccept frame) {
        throw new AssertionError();
    }

    @Override
    public void visit(FusionReqFwdB frame) {
        throw new AssertionError();
    }

    @Override
    public void visit(Fusion frame) {
        throw new AssertionError();
    }

    @Override
    public void visit(FusionLink frame) {
        throw new AssertionError();
    }

    @Override
    public void visit(FusionLinkAccept frame) {
        throw new AssertionError();
    }

    @Override
    public void visit(FusionEnd frame) {
        throw new AssertionError();
    }

    /////////////////////////////// Invalid packets END //////////////////////////////////
}
