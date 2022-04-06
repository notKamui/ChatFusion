package fr.uge.teillardnajjar.chatfusion.server.logic;

import fr.uge.teillardnajjar.chatfusion.core.context.AbstractContext;
import fr.uge.teillardnajjar.chatfusion.core.context.Context;
import fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes;
import fr.uge.teillardnajjar.chatfusion.server.buffer.Buffers;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Objects;

public class ServerToServerContext extends AbstractContext implements Context {
    private final Server server;

    public ServerToServerContext(SelectionKey key, Server server) {
        super(key);
        Objects.requireNonNull(server);
        this.server = server;
    }

    public void queueFusionReqAccept() {
        var buffer = Buffers.getFusionReqBuffer(server);
        queuePacket(buffer);
    }

    public void queueFusionLinkAccept() {
        queuePacket(ByteBuffer.allocate(1).put(OpCodes.FUSIONLINKACCEPT).flip());
        server.checkFusionFinished();
    }
}
