package fr.uge.teillardnajjar.chatfusion.core.reader.primitive;

import fr.uge.teillardnajjar.chatfusion.core.reader.Reader;
import fr.uge.teillardnajjar.chatfusion.core.reader.Readers;

import java.nio.ByteBuffer;
import java.util.function.Function;

abstract class PrimitiveReader<T> implements Reader<T> {
    private final ByteBuffer internalBuffer;
    private final Function<ByteBuffer, T> transform;
    private State state = State.WAITING;
    private T value;

    PrimitiveReader(int bytes, Function<ByteBuffer, T> transform) {
        this.internalBuffer = ByteBuffer.allocate(bytes);
        this.transform = transform;
    }

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
        value = transform.apply(internalBuffer);
        return ProcessStatus.DONE;
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
        state = State.WAITING;
        internalBuffer.clear();
    }

    private enum State {
        DONE, WAITING, ERROR
    }
}
