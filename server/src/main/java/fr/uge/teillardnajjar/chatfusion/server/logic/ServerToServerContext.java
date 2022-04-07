package fr.uge.teillardnajjar.chatfusion.server.logic;

import fr.uge.teillardnajjar.chatfusion.core.context.AbstractContext;
import fr.uge.teillardnajjar.chatfusion.core.context.Context;
import fr.uge.teillardnajjar.chatfusion.core.model.parts.FusionLockInfo;
import fr.uge.teillardnajjar.chatfusion.core.model.parts.Inet;
import fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ServerToServerContext extends AbstractContext implements Context {
    private final static Charset ASCII = StandardCharsets.US_ASCII;
    public final ReqType reqType;

    public ServerToServerContext(SelectionKey key, Server server, ReqType reqType) {
        super(key);
        Objects.requireNonNull(server);
        this.server = server;
        this.reqType = reqType;
        this.setVisitor(new ServerToServerFrameVisitor(this));
    }

    private final Server server;

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
        queuePacket(ByteBuffer.allocate(1).put(OpCodes.FUSIONLINKACCEPT).flip());
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

    public void doConnect() throws IOException {
        if (!sc.finishConnect()) {
            LOGGER.warning("Selector lied");
            return;
        }
        key.interestOps(SelectionKey.OP_WRITE);
        if (reqType == ReqType.FUSIONREQ) {
            queueFusionReq();
        } else {
            queueFusionLink();
        }
    }

    public enum ReqType {FUSIONREQ, FUSIONLINK;}
}
