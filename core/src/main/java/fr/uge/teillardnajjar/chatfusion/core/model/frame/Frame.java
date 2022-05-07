package fr.uge.teillardnajjar.chatfusion.core.model.frame;

public sealed interface Frame permits Fusion, FusionEnd, FusionLink, FusionLinkAccept, FusionLinkDeny, FusionReq, FusionReqAccept, FusionReqDeny, FusionReqFwdA, FusionReqFwdB, Msg, MsgFwd, MsgResp, PrivFile, PrivFileFwd, PrivFileResp, PrivMsg, PrivMsgFwd, PrivMsgResp, Temp, TempKo, TempOk {
    void accept(FrameVisitor visitor);
}
