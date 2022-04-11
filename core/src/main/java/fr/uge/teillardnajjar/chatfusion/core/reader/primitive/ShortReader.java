package fr.uge.teillardnajjar.chatfusion.core.reader.primitive;

import fr.uge.teillardnajjar.chatfusion.core.reader.Reader;

import java.nio.ByteBuffer;

public class ShortReader extends PrimitiveReader<Short> implements Reader<Short> {
    public ShortReader() {
        super(Short.BYTES, ByteBuffer::getShort);
    }
}
