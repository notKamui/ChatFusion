package fr.uge.teillardnajjar.chatfusion.core.model.frame;

public interface FrameVisitor {
    void visit(Temp frame);

    void visit(TempOk frame);

    void visit(TempKo frame);

    void visit(Msg frame);

    void visit(MsgResp frame);

    void visit(MsgFwd frame);

    void visit(PrivMsg frame);

    void visit(PrivMsgResp frame);

    void visit(PrivMsgFwd frame);

    void visit(PrivFile frame);

    void visit(PrivFileResp frame);

    void visit(PrivFileFwd frame);

    void visit(FusionReq frame);

    void visit(FusionReqFwdA frame);

    void visit(FusionReqDeny frame);

    void visit(FusionReqAccept frame);

    void visit(FusionReqFwdB frame);

    void visit(Fusion frame);

    void visit(FusionLink frame);

    void visit(FusionLinkAccept frame);

    void visit(FusionEnd frame);
}
