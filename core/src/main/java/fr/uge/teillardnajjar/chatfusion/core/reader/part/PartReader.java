package fr.uge.teillardnajjar.chatfusion.core.reader.part;

import fr.uge.teillardnajjar.chatfusion.core.model.part.Part;
import fr.uge.teillardnajjar.chatfusion.core.reader.Reader;

import java.nio.ByteBuffer;

abstract class PartReader<T extends Part> implements Reader<T> {
    private Reader<T> inner;

    private Reader<T> inner() {
        if (inner == null) {
            inner = provide();
        }
        return inner;
    }

    protected abstract Reader<T> provide();

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        return inner().process(buffer);
    }

    @Override
    public T get() {
        return inner().get();
    }

    @Override
    public void reset() {
        inner().reset();
    }
}
