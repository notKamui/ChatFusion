package fr.uge.teillardnajjar.chatfusion.server.logic;

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

    /////////////////////////////// Invalid packets BEGIN //////////////////////////////////

    @Override
    public void visit(TempOk frame) {

    }

    @Override
    public void visit(TempKo frame) {

    }

    @Override
    public void visit(MsgResp frame) {

    }

    @Override
    public void visit(MsgFwd frame) {

    }

    @Override
    public void visit(PrivMsgResp frame) {

    }

    @Override
    public void visit(PrivMsgFwd frame) {

    }

    @Override
    public void visit(PrivFileResp frame) {

    }

    @Override
    public void visit(PrivFileFwd frame) {

    }

    @Override
    public void visit(FusionReq frame) {

    }

    @Override
    public void visit(FusionReqFwdA frame) {

    }

    @Override
    public void visit(FusionReqDeny frame) {

    }

    @Override
    public void visit(FusionReqAccept frame) {

    }

    @Override
    public void visit(FusionReqFwdB frame) {

    }

    @Override
    public void visit(Fusion frame) {

    }

    @Override
    public void visit(FusionLink frame) {

    }

    @Override
    public void visit(FusionLinkAccept frame) {

    }

    @Override
    public void visit(FusionEnd frame) {

    }

    /////////////////////////////// Invalid packets END //////////////////////////////////
}
