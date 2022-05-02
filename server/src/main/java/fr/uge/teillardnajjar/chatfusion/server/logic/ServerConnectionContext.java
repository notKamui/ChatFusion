package fr.uge.teillardnajjar.chatfusion.server.logic;

import fr.uge.teillardnajjar.chatfusion.core.context.AbstractContext;
import fr.uge.teillardnajjar.chatfusion.core.context.Context;
import fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import static fr.uge.teillardnajjar.chatfusion.server.logic.ServerConnectionContext.ConnectionType.CLIENT;
import static fr.uge.teillardnajjar.chatfusion.server.logic.ServerConnectionContext.ConnectionType.SERVER;
import static fr.uge.teillardnajjar.chatfusion.server.logic.ServerConnectionContext.ConnectionType.UNKNOWN;

public class ServerConnectionContext extends AbstractContext implements Context {
    private final Server server;
    private ConnectionType type = UNKNOWN;

    public ServerConnectionContext(SelectionKey key, Server server) {
        super(key);
        this.server = server;
        setVisitor(new ServerToUnknownFrameVisitor(server, this));
    }

    void doConnect() throws IOException {
        if (!sc.finishConnect()) return;
        setConnected(true);
        updateInterestOps();
        type = SERVER;
        setVisitor(new ServerToServerFrameVisitor(server, this));
    }

    public void confirmUser(String username) {
        assert type == UNKNOWN;
        setConnected(true);
        LOGGER.info("Confirming user : " + username);
        queuePacket(ByteBuffer.allocate(1).put(OpCodes.TEMPOK).flip());
        setVisitor(new ServerToClientFrameVisitor(username, server, this));
        type = CLIENT;
    }

    public void refuseUser() {
        assert type == UNKNOWN;
        setConnected(true);
        LOGGER.info("Refusing user");
        queuePacket(ByteBuffer.allocate(1).put(OpCodes.TEMPKO).flip());
        closed = true;
    }

    @Override
    public void silentlyClose() {
        super.silentlyClose();
        if (type == CLIENT) { // cleanly disconnects the user if the connection is from a client
            var username = ((ServerToClientFrameVisitor) visitor).username();
            LOGGER.info("Disconnecting user: " + username);
            server.disconnectUser(username);
        }
    }

    // ====================== BUFFER QUEUERS ======================

    public void queueWithOpcode(ByteBuffer buffer, byte opcode) {
        if (buffer == null) buffer = ByteBuffer.allocate(0);
        buffer.flip();
        var toSend = ByteBuffer.allocate(Byte.BYTES + buffer.remaining())
            .put(opcode)
            .put(buffer)
            .flip();
        queuePacket(toSend);
    }


    // =========================== OTHERS ==========================

    public void acknowledgeServer() {
        setVisitor(new ServerToServerFrameVisitor(server, this));
        type = SERVER;
    }

    public void readyToClose() {
        closed = true;
    }

    public ConnectionType type() {
        return type;
    }

    enum ConnectionType {
        UNKNOWN,
        CLIENT,
        SERVER
    }
}
