package fr.uge.teillardnajjar.chatfusion.server.logic;

import fr.uge.teillardnajjar.chatfusion.core.context.AbstractContext;
import fr.uge.teillardnajjar.chatfusion.core.context.Context;
import fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes;

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

    void doConnect() {
        // TODO
    }

    public void confirmUser(String username) {
        assert type == UNKNOWN;
        LOGGER.info("Confirming user : " + username);
        queuePacket(ByteBuffer.allocate(1).put(OpCodes.TEMPOK).flip());
        setVisitor(new ServerToClientFrameVisitor(username, server, this));
        type = CLIENT;
    }

    public void refuseUser() {
        assert type == UNKNOWN;
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

    private void queueWithOpcode(ByteBuffer buffer, byte opcode) {
        buffer.flip();
        var toSend = ByteBuffer.allocate(1 + buffer.remaining())
            .put(opcode)
            .put(buffer)
            .flip();
        queuePacket(toSend);
    }

    public void queueMessageResp(ByteBuffer msgBuffer) {
        assert type == CLIENT;
        queueWithOpcode(msgBuffer, OpCodes.MSGRESP);
    }

    public void queueMsgFwd(ByteBuffer message) {
        assert type == SERVER;
        queueWithOpcode(message, OpCodes.MSGFWD);
    }

    // =========================== OTHERS ==========================

    enum ConnectionType {
        UNKNOWN,
        CLIENT,
        SERVER
    }
}
