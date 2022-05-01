package fr.uge.teillardnajjar.chatfusion.serverold.logic;

import fr.uge.teillardnajjar.chatfusion.core.context.Context;
import fr.uge.teillardnajjar.chatfusion.core.model.part.ForwardedIdentifiedFileChunk;
import fr.uge.teillardnajjar.chatfusion.core.model.part.ForwardedIdentifiedMessage;
import fr.uge.teillardnajjar.chatfusion.core.model.part.FusionLockInfo;
import fr.uge.teillardnajjar.chatfusion.core.model.part.IdentifiedFileChunk;
import fr.uge.teillardnajjar.chatfusion.core.model.part.IdentifiedMessage;
import fr.uge.teillardnajjar.chatfusion.core.model.part.Inet;
import fr.uge.teillardnajjar.chatfusion.core.model.part.ServerInfo;
import fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ServerToServerContext extends FusionContext implements Context {
    private final static Charset ASCII = StandardCharsets.US_ASCII;
    public final ReqType reqType;
    public final FusionLockInfo fusionLockInfo;
    public int fuseCount = 0;

    public ServerToServerContext(SelectionKey key, Server server, ReqType reqType, FusionLockInfo info) {
        super(key, server);
        this.reqType = reqType;
        this.setVisitor(new ServerToServerFrameVisitor(this));
        this.fusionLockInfo = info;
    }

    public ServerToServerContext(SelectionKey key, Server server, ReqType reqType) {
        this(key, server, reqType, null);
    }

    public void queueFusionReq() {
        var info = server.fusionLockInfo().toBuffer();
        var buffer = ByteBuffer.allocate(1 + info.remaining())
            .put(OpCodes.FUSIONREQ)
            .put(info)
            .flip();
        queuePacket(buffer);
    }

    public void queueFusionEnd() {
        var buffer = ByteBuffer.allocate(1).put(OpCodes.FUSIONEND).flip();
        queuePacket(buffer);
    }

    public void queueFusionLink() {
        var sbuffer = server.info().toBuffer();
        var buffer = ByteBuffer.allocate(1 + sbuffer.remaining())
            .put(OpCodes.FUSIONLINK)
            .put(sbuffer)
            .flip();
        queuePacket(buffer);
    }

    public void queueFusionReqFwdA(String address, short port) {
        var hnameBuffer = ASCII.encode(address);
        var buffer = ByteBuffer.allocate(1 + Integer.BYTES + hnameBuffer.remaining() + Short.BYTES);
        buffer.put(OpCodes.FUSIONREQFWDA)
            .putInt(hnameBuffer.remaining())
            .put(hnameBuffer)
            .putShort(port)
            .flip();
        queuePacket(buffer);
    }

    public void queueFusionReqFwdB(FusionLockInfo info) {
        var infoBuffer = info.toBuffer();
        var buffer = ByteBuffer.allocate(1 + infoBuffer.remaining())
            .put(OpCodes.FUSIONREQFWDB)
            .put(infoBuffer)
            .flip();
        queuePacket(buffer);
    }

    public void queueFusionReqAccept() {
        var info = server.fusionLockInfo().toBuffer();
        var buffer = ByteBuffer.allocate(1 + info.remaining())
            .put(OpCodes.FUSIONREQACCEPT)
            .put(info)
            .flip();
        queuePacket(buffer);
    }

    public void queueFusion(FusionLockInfo info) {
        var infoBuffer = info.toBuffer();
        var buffer = ByteBuffer.allocate(1 + infoBuffer.remaining())
            .put(OpCodes.FUSION)
            .put(infoBuffer)
            .flip();
        queuePacket(buffer);
    }

    public void queueFusionLinkAccept() {
        var info = server.info().toBuffer();
        var buffer = ByteBuffer.allocate(1 + info.remaining())
            .put(OpCodes.FUSIONLINKACCEPT)
            .put(info)
            .flip();
        queuePacket(buffer);
        server.checkFusionFinished();
    }

    public void fusion(Inet inet) {
        server.fusion(inet.hostname(), inet.port());
    }

    public void denyFusionReq() {
        server.setFusionLock(false);
        closed = true;
    }

    public void engageFusion(FusionLockInfo info) {
        server.engageFusion(info, this);
    }

    public void fusionAccept(ServerInfo info) {
        server.checkServer(info.servername());
        server.confirmServer(info, this);
    }

    public void receiveFusionEnd() {
        fuseCount++;
        if (fuseCount >= server.siblings().size()) {
            server.setFusionLock(false);
            fuseCount = 0;
        }
    }

    public void queuePrivMsgFwd(String username, IdentifiedMessage message) {
        var unameBuffer = ASCII.encode(username);
        var msgBuffer = message.toBuffer().flip();
        var buffer = ByteBuffer.allocate(1 + Integer.BYTES + unameBuffer.remaining() + msgBuffer.remaining())
            .put(OpCodes.PRIVMSGFWD)
            .putInt(unameBuffer.remaining())
            .put(unameBuffer)
            .put(msgBuffer)
            .flip();
        queuePacket(buffer);
    }

    public void queuePrivFileFwd(String username, IdentifiedFileChunk chunk) {
        var unameBuffer = ASCII.encode(username);
        var chunkBuffer = chunk.toBuffer().flip();
        var buffer = ByteBuffer.allocate(1 + Integer.BYTES + unameBuffer.remaining() + chunkBuffer.remaining())
            .put(OpCodes.PRIVFILEFWD)
            .putInt(unameBuffer.remaining())
            .put(unameBuffer)
            .put(chunkBuffer)
            .flip();
        queuePacket(buffer);
    }

    public void queueMsgFwd(ByteBuffer message) {
        message.flip();
        var buffer = ByteBuffer.allocate(1 + message.remaining())
            .put(OpCodes.MSGFWD)
            .put(message)
            .flip();
        queuePacket(buffer);
    }

    public void broadcast(IdentifiedMessage message) {
        server.broadcast(message);
    }

    public void sendPrivMsg(ForwardedIdentifiedMessage message) {
        server.sendPrivMsg(message);
    }

    public void sendPrivFile(ForwardedIdentifiedFileChunk filechunk) {
        server.sendPrivFile(filechunk);
    }

    public void fusionForward(FusionLockInfo info) {
        server.forwardFusion(info);
    }

    private void fusionResponseAct() {
        if (/*isFusionLocked() ||*/ !checkServers(fusionLockInfo)) {
            queueFusionReqDeny();
        } else {
            acceptFusion(fusionLockInfo);
        }
        acceptFusion(fusionLockInfo);
    }

    public void doConnect() throws IOException {
        if (!sc.finishConnect()) {
            LOGGER.warning("Selector lied");
            return;
        }
        key.interestOps(SelectionKey.OP_WRITE);
        switch (reqType) {
            case FUSIONREQ -> queueFusionReq();
            case FUSIONLINK -> queueFusionLink();
            case FUSIONREQRESP -> fusionResponseAct();
        }
    }

    public enum ReqType {FUSIONREQ, FUSIONREQRESP, FUSIONLINK}
}
