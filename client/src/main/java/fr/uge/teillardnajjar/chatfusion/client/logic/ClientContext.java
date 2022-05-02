package fr.uge.teillardnajjar.chatfusion.client.logic;

import fr.uge.teillardnajjar.chatfusion.client.command.PrivateFileCommand;
import fr.uge.teillardnajjar.chatfusion.client.command.PrivateMessageCommand;
import fr.uge.teillardnajjar.chatfusion.client.command.PublicMessageCommand;
import fr.uge.teillardnajjar.chatfusion.client.file.FileSender;
import fr.uge.teillardnajjar.chatfusion.client.file.FilesManager;
import fr.uge.teillardnajjar.chatfusion.core.context.AbstractContext;
import fr.uge.teillardnajjar.chatfusion.core.context.Context;
import fr.uge.teillardnajjar.chatfusion.core.model.part.IdentifiedFileChunk;
import fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class ClientContext extends AbstractContext implements Context {
    private final static Charset ASCII = StandardCharsets.US_ASCII;
    private final static Charset UTF8 = StandardCharsets.UTF_8;

    private final Client client;
    private final FilesManager filesManager;
    private final Set<FileSender> fileSenders = new HashSet<>();

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
            .put(ASCII.encode(login))
            .flip();
        queuePacket(buffer);
    }

    public void queuePublicMessage(PublicMessageCommand cmd) {
        var encoded = UTF8.encode(cmd.message());
        var length = encoded.remaining();
        var buffer = ByteBuffer.allocate(1 + Integer.BYTES + length);
        buffer.put(OpCodes.MSG)
            .putInt(length)
            .put(encoded)
            .flip();
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
            .put(msg)
            .flip();
        queuePacket(buffer);
    }

    public void queuePrivateFile(PrivateFileCommand cmd) {
        try {
            var sender = FileSender.from(cmd);
            fileSenders.add(sender);
            sender.sendAsync(this);
        } catch (IOException | UncheckedIOException e) {
            LOGGER.warning("Error while sending file");
        }
    }

    public void finishSender(FileSender sender) {
        fileSenders.remove(sender);
        client.wakeup();
    }

    public void exit() {
        silentlyClose();
        fileSenders.forEach(sender -> {
            sender.cancel();
            finishSender(sender);
        });
        Thread.currentThread().interrupt();
    }

    public void feedChunk(IdentifiedFileChunk chunk) {
        filesManager.feedChunk(chunk);
    }

    public void doConnect() throws IOException {
        if (!sc.finishConnect()) {
            LOGGER.warning("Selector lied");
            return;
        }
        setConnected(true);
        queueLogin();
        updateInterestOps();
    }
}
