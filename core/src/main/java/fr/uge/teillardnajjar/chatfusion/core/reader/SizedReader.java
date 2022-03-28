package fr.uge.teillardnajjar.chatfusion.core.reader;

import java.nio.ByteBuffer;

import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.DONE;
import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.REFILL;

abstract class SizedReader<T> implements Reader<T> {
    private final static int BUFFER_SIZE = 1024;

    private final IntReader intReader = new IntReader();
    protected State state = State.WAITING_SIZE;
    protected ByteBuffer internalBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    protected T value;

    protected void readSize(ByteBuffer buffer) {
        var status = intReader.process(buffer);
        if (status == DONE) {
            var sizeToRead = intReader.get();
            if (sizeToRead > BUFFER_SIZE || sizeToRead < 0) {
                state = State.ERROR;
                return;
            }
            internalBuffer.limit(sizeToRead);
            intReader.reset();
            state = State.WAITING_CONTENT;
        }
    }

    protected ProcessStatus internalProcess(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if (state == State.WAITING_SIZE) {
            readSize(buffer);
            if (state == State.ERROR) {
                return ProcessStatus.ERROR;
            }
        }

        if (state != State.WAITING_CONTENT) {
            return REFILL;
        }

        Readers.readCompact(buffer, internalBuffer);

        if (internalBuffer.hasRemaining()) {
            return REFILL;
        }
        state = State.DONE;
        internalBuffer.flip();
        return DONE;
    }

    @Override
    public T get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING_SIZE;
        internalBuffer.clear();
        intReader.reset();
        value = null;
    }

    protected enum State {
        DONE, WAITING_SIZE, WAITING_CONTENT, ERROR
    }
}
