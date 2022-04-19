package fr.uge.teillardnajjar.chatfusion.serverold.logic;

import fr.uge.teillardnajjar.chatfusion.core.context.Context;
import fr.uge.teillardnajjar.chatfusion.core.model.part.FusionLockInfo;
import fr.uge.teillardnajjar.chatfusion.core.model.part.ServerInfo;
import fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class GenericContext extends FusionContext implements Context {

    public GenericContext(SelectionKey key, Server server) {
        super(key, server);
        this.setVisitor(new GenericFrameVisitor(this));
    }

    public boolean checkLogin(String username) {
        return !server.connectedUsers().contains(username);
    }

    public void confirmUser(String username) {
        LOGGER.info("Confirming user : " + username);
        var newCtx = new ServerToClientContext(key, server, username);
        key.attach(newCtx);
        server.confirmUser(username, newCtx);
        newCtx.queuePacket(ByteBuffer.allocate(1).put(OpCodes.TEMPOK).flip());
        try {
            newCtx.doWrite();
        } catch (IOException e) {
            silentlyClose();
        }
    }

    public void refuseUser() {
        LOGGER.info("Refusing user");
        queuePacket(ByteBuffer.allocate(1).put(OpCodes.TEMPKO).flip());
        closed = true;
        server.wakeup();
    }

    public boolean serverIsLeader() {
        return server.isLeader();
    }

    public boolean checkLinkServer(ServerInfo info) {
        return server.checkLinkServer(info);
    }

    public void refuseLink() {
        closed = true;
    }

    public void acceptLink(ServerInfo info) {
        var newCtx = new ServerToServerContext(key, server, null);
        key.attach(newCtx);
        server.confirmServer(info, newCtx);
        newCtx.queueFusionLinkAccept();
    }

    public void forwardFusionReq(FusionLockInfo info) {
        server.forwardFusionReq(info);
        //closed = true;
    }

    public void abortFusion() {
        server.setFusionLock(false);
        closed = true;
    }

    public void engageFusion(FusionLockInfo info) {
        var newCtx = new ServerToServerContext(key, server, null);
        key.attach(newCtx);
        server.confirmServer(info, newCtx);
        newCtx.engageFusion(info);
    }
}
