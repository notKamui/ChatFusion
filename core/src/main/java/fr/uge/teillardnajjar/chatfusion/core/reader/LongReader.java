package fr.uge.teillardnajjar.chatfusion.core.reader;

import java.nio.ByteBuffer;

public class LongReader extends GenericReader<Long> implements Reader<Long> {
    public LongReader() {
        super(Long.BYTES, ByteBuffer::getLong);
    }
}
