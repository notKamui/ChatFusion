package fr.uge.teillardnajjar.chatfusion.client;

import fr.uge.teillardnajjar.chatfusion.client.command.PrivateFileCommand;
import fr.uge.teillardnajjar.chatfusion.client.command.PrivateMessageCommand;
import fr.uge.teillardnajjar.chatfusion.client.command.PublicMessageCommand;
import fr.uge.teillardnajjar.chatfusion.core.context.AbstractContext;
import fr.uge.teillardnajjar.chatfusion.core.context.Context;
import fr.uge.teillardnajjar.chatfusion.core.model.parts.IdentifiedFileChunk;
import fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class ClientContext extends AbstractContext implements Context {
    private final static Charset ASCII = StandardCharsets.US_ASCII;
    private final static Charset UTF8 = StandardCharsets.UTF_8;

    private final Client client;
    private final FilesManager filesManager;

    public ClientContext(SelectionKey key, Client client) {
        super(key);
        Objects.requireNonNull(client);
        this.client = client;
        this.setVisitor(new ClientFrameVisitor(client, this));
        this.filesManager = new FilesManager(client);
    }

    private void queueLogin() {
        var login = client.login();
        var length = login.length();
        var buffer = ByteBuffer.allocate(1 + Integer.BYTES + length);
        buffer.put(OpCodes.TEMP)
            .putInt(length)
            .put(ASCII.encode(login));
        queuePacket(buffer);
    }

    public void queuePublicMessage(PublicMessageCommand cmd) {
        var encoded = UTF8.encode(cmd.message());
        var length = encoded.remaining();
        var buffer = ByteBuffer.allocate(1 + Integer.BYTES + length);
        buffer.put(OpCodes.MSG)
            .putInt(length)
            .put(encoded);
        queuePacket(buffer);
    }

    public void queuePrivateMessage(PrivateMessageCommand cmd) {
        var username = ASCII.encode(cmd.targetUsername());
        var servername = ASCII.encode(cmd.targetServername());
        var msg = UTF8.encode(cmd.message());
        var buffer = ByteBuffer.allocate(1 + Integer.BYTES * 2 + username.remaining() + 5 + msg.remaining());
        buffer.put(OpCodes.PRIVMSG)
            .putInt(username.remaining())
            .put(username)
            .put(servername)
            .putInt(msg.remaining())
            .put(msg);
        queuePacket(buffer);
    }

    public void queuePrivateFile(PrivateFileCommand cmd) {
        try {
            FileSender.from(cmd).sendAsync(this);
        } catch (IOException | UncheckedIOException e) {
            LOGGER.warning("Error while sending file");
        }
    }

    public void feedChunk(IdentifiedFileChunk chunk) {
        filesManager.feedChunk(chunk);
    }

    public void doConnect() throws IOException {
        if (!sc.finishConnect()) {
            LOGGER.warning("Selector lied");
            return;
        }
        key.interestOps(SelectionKey.OP_WRITE);
        queueLogin();
    }

    void queuePacket(ByteBuffer buffer) {
        synchronized (client) {
            queue.offer(buffer);
            processOut();
            updateInterestOps();
        }
    }
}
