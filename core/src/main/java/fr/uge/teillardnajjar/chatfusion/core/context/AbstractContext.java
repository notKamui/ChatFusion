package fr.uge.teillardnajjar.chatfusion.core.context;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.logging.Logger;

public abstract class AbstractContext<T> implements Context {

    protected final static Logger LOGGER = Logger.getLogger(AbstractContext.class.getName());
    private final static int BUFFER_SIZE = 32_768;
    protected final SelectionKey key;
    protected final SocketChannel sc;
    protected final ByteBuffer bin;
    protected final ByteBuffer bout;
    private final ArrayDeque<T> queue;

    private boolean closed = false;

    public AbstractContext(SelectionKey key) {
        Objects.requireNonNull(key);

        this.key = key;
        this.sc = (SocketChannel) key.channel();
        this.bin = ByteBuffer.allocate(BUFFER_SIZE);
        this.bout = ByteBuffer.allocate(BUFFER_SIZE);
        this.queue = new ArrayDeque<>();
    }

    @Override
    public void doWrite() throws IOException {
        bout.flip();
        sc.write(bout);
        bout.compact();
        processOut();
        updateInterestOps();
    }

    @Override
    public void doRead() throws IOException {
        if (sc.read(bin) == -1) {
            LOGGER.info("Connection closed by " + sc.getRemoteAddress());
            closed = true;
        }
        processIn();
        updateInterestOps();
    }

    @Override
    public void updateInterestOps() {
        int interestOps = 0;
        if (!closed && bin.hasRemaining()) {
            interestOps |= SelectionKey.OP_READ;
        }
        if (bout.position() != 0) {
            interestOps |= SelectionKey.OP_WRITE;
        }

        if (interestOps == 0) {
            silentlyClose();
            return;
        }
        key.interestOps(interestOps);
    }

    @Override
    public void silentlyClose() {
        try {
            sc.close();
        } catch (IOException e) {
            // ignore exception
        }
    }

    protected abstract void processOut();

    protected abstract void processIn();
}
