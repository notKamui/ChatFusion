package fr.uge.teillardnajjar.chatfusion.core.reader.primitive;

import fr.uge.teillardnajjar.chatfusion.core.reader.Reader;

import java.nio.ByteBuffer;

public class LongReader extends PrimitiveReader<Long> implements Reader<Long> {
    public LongReader() {
        super(Long.BYTES, ByteBuffer::getLong);
    }
}
