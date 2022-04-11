package fr.uge.teillardnajjar.chatfusion.core.reader.list;

import fr.uge.teillardnajjar.chatfusion.core.reader.Reader;
import fr.uge.teillardnajjar.chatfusion.core.reader.primitive.IntReader;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.DONE;
import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.ERROR;
import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.REFILL;

abstract class ListReader<T> implements Reader<List<T>> {
    private final IntReader intReader = new IntReader();
    private final Reader<T> reader;

    private List<T> list;
    private int size;
    private State state = State.WAITING_SIZE;

    ListReader(Reader<T> reader) {
        this.reader = reader;
    }

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException("Cannot process a reader that is done or in error");
        }

        if (state == State.WAITING_SIZE) {
            var status = intReader.process(buffer);
            if (status == DONE) {
                size = intReader.get();
                if (size < 0) {
                    state = State.ERROR;
                    return ERROR;
                } else if (size == 0) {
                    state = State.DONE;
                    return DONE;
                }
                list = new ArrayList<>();
                state = State.WAITING_CONTENT;
            } else if (status == ERROR) {
                state = State.ERROR;
                return ERROR;
            }
        }

        if (state == State.WAITING_CONTENT) {
            var status = reader.process(buffer);
            if (status == DONE) {
                list.add(reader.get());
                if (list.size() == size) {
                    state = State.DONE;
                    return DONE;
                } else {
                    reader.reset();
                }
            } else if (status == ERROR) {
                state = State.ERROR;
                return ERROR;
            }
        }

        return REFILL;
    }

    @Override
    public List<T> get() {
        if (state != State.DONE) {
            throw new IllegalStateException("Cannot get a reader that is not done");
        }
        return list;
    }

    @Override
    public void reset() {
        state = State.WAITING_SIZE;
        list = null;
        size = 0;
        reader.reset();
        intReader.reset();
    }

    private enum State {DONE, WAITING_SIZE, WAITING_CONTENT, ERROR}
}
