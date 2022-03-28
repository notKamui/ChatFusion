package fr.uge.teillardnajjar.chatfusion.core.reader;

import java.nio.ByteBuffer;

/**
 * {@link Reader} implementation for int. Reads a 4 bytes integer.
 */
public class IntReader implements Reader<Integer> {

    private final ByteBuffer internalBuffer = ByteBuffer.allocate(Integer.BYTES); // write-mode
    private State state = State.WAITING;
    private int value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }
        Readers.readCompact(buffer, internalBuffer);
        if (internalBuffer.hasRemaining()) {
            return ProcessStatus.REFILL;
        }
        state = State.DONE;
        internalBuffer.flip();
        value = internalBuffer.getInt();
        return ProcessStatus.DONE;
    }

    @Override
    public Integer get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING;
        internalBuffer.clear();
    }

    private enum State {
        DONE, WAITING, ERROR
    }
}