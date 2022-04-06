package fr.uge.teillardnajjar.chatfusion.core.reader;

import java.nio.ByteBuffer;

public class Int16BReader extends GenericReader<byte[]> implements Reader<byte[]> {
    public Int16BReader() {
        super(16, ByteBuffer::array);
    }
}
