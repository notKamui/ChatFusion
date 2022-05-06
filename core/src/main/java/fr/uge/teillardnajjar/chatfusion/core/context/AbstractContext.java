package fr.uge.teillardnajjar.chatfusion.core.context;

import fr.uge.teillardnajjar.chatfusion.core.model.frame.FrameVisitor;
import fr.uge.teillardnajjar.chatfusion.core.reader.FrameReader;
import fr.uge.teillardnajjar.chatfusion.core.reader.Readers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

public abstract class AbstractContext implements Context {

    protected final static Logger LOGGER = Logger.getLogger(AbstractContext.class.getName());
    private final static int BUFFER_SIZE = 32_768;
    protected final SelectionKey key;
    protected final SocketChannel sc;
    protected final ByteBuffer bin;
    protected final ByteBuffer bout;
    protected final ArrayDeque<ByteBuffer> queue;
    protected final ArrayDeque<ByteBuffer> filesQueue;
    private final FrameReader reader;
    protected FrameVisitor visitor;

    protected boolean connected = false;
    protected boolean closed = false;

    private final static int CONSECUTIVE_MSG_LIMIT = 3;
    private int sentMessages;

    public AbstractContext(SelectionKey key) {
        requireNonNull(key);
        this.key = key;
        this.sc = (SocketChannel) key.channel();
        this.bin = ByteBuffer.allocate(BUFFER_SIZE);
        this.bout = ByteBuffer.allocate(BUFFER_SIZE);
        this.queue = new ArrayDeque<>();
        this.filesQueue = new ArrayDeque<>();
        this.reader = new FrameReader();
        this.sentMessages = 0;
    }

    protected void setVisitor(FrameVisitor visitor) {
        requireNonNull(visitor);
        this.visitor = visitor;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
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
            System.out.println("Connection closed by " + sc.getRemoteAddress());
            closed = true;
        }
        processIn();
        updateInterestOps();
    }

    @Override
    public void updateInterestOps() {
        if (!connected) {
            key.interestOps(SelectionKey.OP_CONNECT);
            return;
        }

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
        try {
            key.interestOps(interestOps);
        } catch (CancelledKeyException e) {
            // ignore exception
        }
    }

    @Override
    public void silentlyClose() {
        try {
            sc.close();
        } catch (IOException e) {
            // ignore exception
        }
    }

    private void processQueue(ArrayDeque<ByteBuffer> queue, Runnable onFinish) {
        var buffer = queue.peek();
        assert buffer != null;
        if (!buffer.hasRemaining()) {
            queue.poll();
            return;
        }
        Readers.read(buffer, bout);
        onFinish.run();
    }

    protected void processOut() {
        while ((!queue.isEmpty() || !filesQueue.isEmpty()) && bout.hasRemaining()) {
            if (queue.isEmpty()) processQueue(filesQueue, () -> sentMessages = 0);
            else if (filesQueue.isEmpty() || sentMessages < CONSECUTIVE_MSG_LIMIT) processQueue(queue, () -> sentMessages++);
            else processQueue(filesQueue, () -> sentMessages = 0);
        }
    }

    protected void processIn() {
        if (visitor == null) throw new AssertionError();
        while (true) {
            var state = reader.process(bin);
            switch (state) {
                case ERROR:
                    LOGGER.warning("Error while reading frame");
                    silentlyClose();
                case REFILL:
                    return;
                case DONE:
                    var frame = reader.get();
                    reader.reset();
                    frame.accept(visitor);
                    break;
            }
        }
    }

    public void queuePacket(ByteBuffer buffer) {
        synchronized (queue) {
            queue.offer(buffer);
            processOut();
            updateInterestOps();
        }
    }

    public void queueFileChunk(ByteBuffer fileBuffer) {
        synchronized (filesQueue) {
            filesQueue.offer(fileBuffer);
            processOut();
            updateInterestOps();
        }
    }
}