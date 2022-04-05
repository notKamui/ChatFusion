package fr.uge.teillardnajjar.chatfusion.core.context;

import fr.uge.teillardnajjar.chatfusion.core.model.frame.FrameVisitor;
import fr.uge.teillardnajjar.chatfusion.core.reader.FrameReader;
import fr.uge.teillardnajjar.chatfusion.core.reader.Reader;
import fr.uge.teillardnajjar.chatfusion.core.reader.Readers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Logger;

public abstract class AbstractContext implements Context {

    protected final static Logger LOGGER = Logger.getLogger(AbstractContext.class.getName());
    private final static int BUFFER_SIZE = 32_768;
    protected final SelectionKey key;
    protected final SocketChannel sc;
    protected final ByteBuffer bin;
    protected final ByteBuffer bout;
    protected final ArrayDeque<ByteBuffer> queue;
    private final FrameReader reader;
    private FrameVisitor visitor;

    protected boolean closed = false;

    public AbstractContext(SelectionKey key) {
        Objects.requireNonNull(key);
        this.key = key;
        this.sc = (SocketChannel) key.channel();
        this.bin = ByteBuffer.allocate(BUFFER_SIZE);
        this.bout = ByteBuffer.allocate(BUFFER_SIZE);
        this.queue = new ArrayDeque<>();
        this.reader = new FrameReader();
    }

    protected void setVisitor(FrameVisitor visitor) {
        Objects.requireNonNull(visitor);
        this.visitor = visitor;
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

    protected void processIn() {
        if (visitor == null) throw new AssertionError();
        process(reader, bin, frame -> frame.accept(visitor));
    }

    protected <E> void process(
        Reader<E> reader,
        ByteBuffer buffer,
        Consumer<E> onSuccess
    ) {
        while (buffer.hasRemaining()) {
            var state = reader.process(buffer);
            switch (state) {
                case ERROR:
                    LOGGER.warning("Error while reading frame");
                    silentlyClose();
                case REFILL:
                    return;
                case DONE:
                    var frame = reader.get();
                    onSuccess.accept(frame);
                    reader.reset();
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
}
