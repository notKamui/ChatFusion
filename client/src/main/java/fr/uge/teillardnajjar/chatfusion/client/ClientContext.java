package fr.uge.teillardnajjar.chatfusion.client;

import fr.uge.teillardnajjar.chatfusion.core.context.AbstractContext;
import fr.uge.teillardnajjar.chatfusion.core.context.Context;
import fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes;
import fr.uge.teillardnajjar.chatfusion.core.reader.IdentifiedMessageReader;
import fr.uge.teillardnajjar.chatfusion.core.reader.Readers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.MSGRESP;
import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.TEMPKO;
import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.TEMPOK;

final class ClientContext extends AbstractContext<ByteBuffer> implements Context {
    private final static Charset ASCII = StandardCharsets.US_ASCII;
    private final static Charset UTF8 = StandardCharsets.UTF_8;

    private final Client client;
    private final IdentifiedMessageReader identifiedMessageReader = new IdentifiedMessageReader();

    ClientContext(SelectionKey key, Client client) {
        super(key);
        Objects.requireNonNull(client);
        this.client = client;
    }

    @Override
    protected void processOut() {
        while (!queue.isEmpty() && bout.hasRemaining()) {
            var buffer = queue.peek();
            if (!buffer.hasRemaining()) {
                queue.poll();
                continue;
            }
            Readers.read(buffer, bout);
        }
    }

    @Override
    protected void processIn() {
        while (bin.hasRemaining()) {
            var opcode = bin.get();
            switch (opcode) {
                case TEMPOK -> {/*do nothing*/}
                case TEMPKO -> {
                    LOGGER.warning("Login failed");
                    closed = true;
                }
                case MSGRESP -> process(identifiedMessageReader, bin, client::logMessage);

                default -> {
                    LOGGER.severe("Unknown opcode: " + opcode);
                    closed = true;
                }
            }

        }
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

    public void doConnect() throws IOException {
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
