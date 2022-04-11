package fr.uge.teillardnajjar.chatfusion.server.logic;

import fr.uge.teillardnajjar.chatfusion.core.context.AbstractContext;
import fr.uge.teillardnajjar.chatfusion.core.context.Context;
import fr.uge.teillardnajjar.chatfusion.core.model.part.FusionLockInfo;
import fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public abstract class FusionContext extends AbstractContext implements Context {
    protected final Server server;

    protected FusionContext(SelectionKey key, Server server) {
        super(key);
        this.server = server;
    }

    protected boolean isFusionLocked() {
        return server.isFusionLocked();
    }

    protected boolean checkServers(FusionLockInfo info) {
        return server.checkServer(info.self().servername()) &&
            info.siblings().stream().allMatch(s -> server.checkServer(s.servername()));
    }

    protected void queueFusionReqDeny() {
        System.out.println(">>> Fusion denied");
        queuePacket(ByteBuffer.allocate(1).put(OpCodes.FUSIONREQDENY).flip());
        closed = true;
        server.wakeup();
    }

    protected void acceptFusion(FusionLockInfo info) {
        var newCtx = new ServerToServerContext(key, server, null);
        key.attach(newCtx);
        server.confirmServer(info, newCtx);
        newCtx.queueFusionReqAccept();
        server.broadcast(info);
        try {
            newCtx.doWrite();
        } catch (IOException e) {
            silentlyClose();
        }
    }
}
