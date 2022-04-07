package fr.uge.teillardnajjar.chatfusion.core.model.frame;

public interface FrameVisitor {
    default void visit(Temp frame) {
        throw new UnsupportedOperationException();
    }

    default void visit(TempOk frame) {
        throw new UnsupportedOperationException();
    }

    default void visit(TempKo frame) {
        throw new UnsupportedOperationException();
    }

    default void visit(Msg frame) {
        throw new UnsupportedOperationException();
    }

    default void visit(MsgResp frame) {
        throw new UnsupportedOperationException();
    }

    default void visit(MsgFwd frame) {
        throw new UnsupportedOperationException();
    }

    default void visit(PrivMsg frame) {
        throw new UnsupportedOperationException();
    }

    default void visit(PrivMsgResp frame) {
        throw new UnsupportedOperationException();
    }

    default void visit(PrivMsgFwd frame) {
        throw new UnsupportedOperationException();
    }

    default void visit(PrivFile frame) {
        throw new UnsupportedOperationException();
    }

    default void visit(PrivFileResp frame) {
        throw new UnsupportedOperationException();
    }

    default void visit(PrivFileFwd frame) {
        throw new UnsupportedOperationException();
    }

    default void visit(FusionReq frame) {
        throw new UnsupportedOperationException();
    }

    default void visit(FusionReqFwdA frame) {
        throw new UnsupportedOperationException();
    }

    default void visit(FusionReqDeny frame) {
        throw new UnsupportedOperationException();
    }

    default void visit(FusionReqAccept frame) {
        throw new UnsupportedOperationException();
    }

    default void visit(FusionReqFwdB frame) {
        throw new UnsupportedOperationException();
    }

    default void visit(Fusion frame) {
        throw new UnsupportedOperationException();
    }

    default void visit(FusionLink frame) {
        throw new UnsupportedOperationException();
    }

    default void visit(FusionLinkAccept frame) {
        throw new UnsupportedOperationException();
    }

    default void visit(FusionEnd frame) {
        throw new UnsupportedOperationException();
    }
}
