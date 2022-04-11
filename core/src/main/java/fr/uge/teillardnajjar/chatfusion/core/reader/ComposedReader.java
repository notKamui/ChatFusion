package fr.uge.teillardnajjar.chatfusion.core.reader;

import java.nio.ByteBuffer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.DONE;
import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.ERROR;
import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.REFILL;

public class ComposedReader<T> implements Reader<T> {

    private final Supplier<? extends T> supplier;
    private final InnerReader<?>[] readers;
    private int current;

    private ProcessStatus status = REFILL;

    private ComposedReader(Supplier<? extends T> supplier, InnerReader<?>... readers) {
        this.supplier = supplier;
        this.readers = readers;
        this.current = 0;
    }

    public static <T> ComposedReader<T> with(Supplier<? extends T> supplier, InnerReader<?>... readers) {
        return new ComposedReader<>(supplier, readers);
    }

    public static <P> InnerReader<P> inner(Reader<P> reader, Consumer<P> onDone) {
        return new InnerReader<>(reader, onDone);
    }

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (status == DONE || status == ERROR) {
            throw new IllegalStateException("Reader is done or in error state");
        }

        var reader = readers[current];
        status = reader.process(buffer);
        if (status == DONE) {
            reader.onDone(reader.get());
            reader.reset();
            current++;
            if (current < readers.length) {
                status = REFILL;
            }
        }

        return status;
    }

    @Override
    public T get() {
        if (status != DONE) {
            throw new IllegalStateException("Reader is not done");
        }
        return supplier.get();
    }

    @Override
    public void reset() {
        status = REFILL;
        for (var reader : readers) {
            reader.reset();
        }
        current = 0;
    }

    public static class InnerReader<P> implements Reader<P> {
        private final Reader<P> reader;
        private final Consumer<Object> onDone;

        @SuppressWarnings("unchecked")
        InnerReader(Reader<P> reader, Consumer<P> onDone) {
            this.reader = reader;
            this.onDone = (Consumer<Object>) onDone;
        }

        @Override
        public ProcessStatus process(ByteBuffer buffer) {
            return reader.process(buffer);
        }

        @Override
        public P get() {
            return reader.get();
        }

        @Override
        public void reset() {
            reader.reset();
        }

        private void onDone(Object o) {
            onDone.accept(o);
        }
    }
}
