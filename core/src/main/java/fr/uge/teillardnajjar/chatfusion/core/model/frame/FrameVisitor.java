package fr.uge.teillardnajjar.chatfusion.core.model.frame;

import fr.uge.teillardnajjar.chatfusion.core.context.Context;

public interface FrameVisitor {

    Context context();

    default void visit(Temp frame) {
        context().silentlyClose();
    }

    default void visit(TempOk frame) {
        context().silentlyClose();
    }

    default void visit(TempKo frame) {
        context().silentlyClose();
    }

    default void visit(Msg frame) {
        context().silentlyClose();
    }

    default void visit(MsgResp frame) {
        context().silentlyClose();
    }

    default void visit(MsgFwd frame) {
        context().silentlyClose();
    }

    default void visit(PrivMsg frame) {
        context().silentlyClose();
    }

    default void visit(PrivMsgResp frame) {
        context().silentlyClose();
    }

    default void visit(PrivMsgFwd frame) {
        context().silentlyClose();
    }

    default void visit(PrivFile frame) {
        context().silentlyClose();
    }

    default void visit(PrivFileResp frame) {
        context().silentlyClose();
    }

    default void visit(PrivFileFwd frame) {
        context().silentlyClose();
    }

    default void visit(FusionReq frame) {
        context().silentlyClose();
    }

    default void visit(FusionReqFwdA frame) {
        context().silentlyClose();
    }

    default void visit(FusionReqDeny frame) {
        context().silentlyClose();
    }

    default void visit(FusionReqAccept frame) {
        context().silentlyClose();
    }

    default void visit(FusionReqFwdB frame) {
        context().silentlyClose();
    }

    default void visit(Fusion frame) {
        context().silentlyClose();
    }

    default void visit(FusionLink frame) {
        context().silentlyClose();
    }

    default void visit(FusionLinkAccept frame) {
        context().silentlyClose();
    }

    default void visit(FusionEnd frame) {
        context().silentlyClose();
    }
}
