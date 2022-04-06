package fr.uge.teillardnajjar.chatfusion.server.logic;

import fr.uge.teillardnajjar.chatfusion.core.context.AbstractContext;
import fr.uge.teillardnajjar.chatfusion.core.context.Context;
import fr.uge.teillardnajjar.chatfusion.core.model.parts.IdentifiedFileChunk;
import fr.uge.teillardnajjar.chatfusion.core.model.parts.IdentifiedMessage;
import fr.uge.teillardnajjar.chatfusion.core.model.parts.Identifier;
import fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ServerToClientContext extends AbstractContext implements Context {
    private final static Charset ASCII = StandardCharsets.US_ASCII;
    private final static Charset UTF8 = StandardCharsets.UTF_8;

    private final Server server;
    private String username;

    public ServerToClientContext(SelectionKey key, Server server) {
        super(key);
        Objects.requireNonNull(server);
        this.server = server;
        this.setVisitor(new ServerToClientFrameVisitor(this));
    }

    public ByteBuffer buildMessageResp(String message) {
        var unameBuffer = ASCII.encode(username);
        var snameBuffer = ASCII.encode(server.name());
        var msgBuffer = UTF8.encode(message);
        var buffer = ByteBuffer.allocate(1 + Integer.BYTES * 2 + 5 + unameBuffer.remaining() + msgBuffer.remaining());
        buffer.put(OpCodes.MSGRESP)
            .putInt(unameBuffer.remaining())
            .put(unameBuffer)
            .put(snameBuffer)
            .putInt(msgBuffer.remaining())
            .put(msgBuffer);
        return buffer;
    }

    public boolean checkLogin(String username) {
        return !server.connectedUsers().contains(username);
    }

    @Override
    public void silentlyClose() {
        super.silentlyClose();
        server.disconnectUser(username);
    }

    public void confirmUser(String username) {
        LOGGER.info("Confirming user : " + username);
        this.username = username;
        queuePacket(ByteBuffer.allocate(1).put(OpCodes.TEMPOK).flip());
        server.confirmUser(username, this);
    }

    public void refuseUser() {
        LOGGER.info("Refusing user");
        queuePacket(ByteBuffer.allocate(1).put(OpCodes.TEMPKO).flip());
        silentlyClose();
    }

    public void queueMessageResp(ByteBuffer messageResp) {
        messageResp.flip();
        queuePacket(messageResp);
    }

    public void queuePrivMsg(IdentifiedMessage message, ServerToClientContext serverToClientContext) {
        var unameBuffer = ASCII.encode(serverToClientContext.username);
        var snameBuffer = ASCII.encode(server.name());
        var msgBuffer = UTF8.encode(message.message());
        var buffer = ByteBuffer.allocate(1 + Integer.BYTES * 2 + 5 + unameBuffer.remaining() + msgBuffer.remaining());
        buffer.put(OpCodes.PRIVMSGRESP)
            .putInt(unameBuffer.remaining())
            .put(unameBuffer)
            .put(snameBuffer)
            .putInt(msgBuffer.remaining())
            .put(msgBuffer)
            .flip();
        queuePacket(buffer);
    }

    public void queuePrivFile(IdentifiedFileChunk identifiedFileChunk, ServerToClientContext serverToClientContext) {
        var unameBuffer = ASCII.encode(serverToClientContext.username);
        var snameBuffer = ASCII.encode(server.name());
        var fnameBuffer = UTF8.encode(identifiedFileChunk.filename());
        var chunkBuffer = identifiedFileChunk.chunk().flip();
        var buffer = ByteBuffer.allocate(
            1 + Integer.BYTES * 4 + Long.BYTES + 5 +
                unameBuffer.remaining() + fnameBuffer.remaining() + chunkBuffer.remaining()
        );
        buffer.put(OpCodes.PRIVFILERESP)
            .putInt(unameBuffer.remaining())
            .put(unameBuffer)
            .put(snameBuffer)
            .putInt(fnameBuffer.remaining())
            .put(fnameBuffer)
            .putLong(identifiedFileChunk.fileSize())
            .putInt(identifiedFileChunk.fileId())
            .putInt(chunkBuffer.remaining())
            .put(chunkBuffer)
            .flip();
        queuePacket(buffer);
    }

    public void broadcast(String message) {
        server.broadcast(message, this);
    }

    private boolean checkIdentity(Identifier identifier) {
        return identifier.servername().equals(server.name()) && server.connectedUsers().contains(identifier.username());
    }

    public void sendPrivMsg(IdentifiedMessage message) {
        if (checkIdentity(message.identifier())) {
            server.sendPrivMsg(message, this);
        } else {
            // TODO forward
        }
    }

    public void sendPrivFile(IdentifiedFileChunk identifiedFileChunk) {
        LOGGER.info("Sending file : " + identifiedFileChunk.filename());
        if (checkIdentity(identifiedFileChunk.identifier())) {
            server.sendPrivFile(identifiedFileChunk, this);
        } else {
            // TODO forward
        }
    }
}
