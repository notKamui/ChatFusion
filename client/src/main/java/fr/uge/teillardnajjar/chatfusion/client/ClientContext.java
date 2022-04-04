package fr.uge.teillardnajjar.chatfusion.client;

import fr.uge.teillardnajjar.chatfusion.core.context.AbstractContext;
import fr.uge.teillardnajjar.chatfusion.core.context.Context;
import fr.uge.teillardnajjar.chatfusion.core.model.parts.IdentifiedFileChunk;
import fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

final class ClientContext extends AbstractContext implements Context {
    private final static Charset ASCII = StandardCharsets.US_ASCII;
    private final static Charset UTF8 = StandardCharsets.UTF_8;

    private final Client client;
    private final FilesManager filesManager;

    ClientContext(SelectionKey key, Client client) {
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

    void queuePublicMessage(String msg) {
        var encoded = UTF8.encode(msg);
        var length = encoded.remaining();
        var buffer = ByteBuffer.allocate(1 + Integer.BYTES + length);
        buffer.put(OpCodes.MSG)
            .putInt(length)
            .put(encoded);
        queuePacket(buffer);
    }

    void feedChunk(IdentifiedFileChunk chunk) {
        filesManager.feedChunk(chunk);
    }

    void doConnect() throws IOException {
        if (!sc.finishConnect()) {
            LOGGER.warning("Selector lied");
            return;
        }
        key.interestOps(SelectionKey.OP_WRITE);
        queueLogin();
    }

    private void queuePacket(ByteBuffer buffer) {
        queue.offer(buffer);
        processOut();
        updateInterestOps();
    }
}
