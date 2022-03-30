package fr.uge.teillardnajjar.chatfusion.client;

import fr.uge.teillardnajjar.chatfusion.core.context.AbstractContext;
import fr.uge.teillardnajjar.chatfusion.core.context.Context;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

final class ClientContext extends AbstractContext<ByteBuffer> implements Context {
    ClientContext(SelectionKey key) {
        super(key);
    }

    @Override
    protected void processOut() {
        // TODO
    }

    @Override
    protected void processIn() {
        // TODO
    }

    public void doConnect() throws IOException {
        if (!sc.finishConnect()) {
            LOGGER.warning("Selector lied");
            return;
        }
        key.interestOps(SelectionKey.OP_WRITE);
    }
}
