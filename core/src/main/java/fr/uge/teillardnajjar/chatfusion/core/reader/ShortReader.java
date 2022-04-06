package fr.uge.teillardnajjar.chatfusion.core.reader;

import java.nio.ByteBuffer;

public class ShortReader extends GenericReader<Short> implements Reader<Short> {
    public ShortReader() {
        super(Short.BYTES, ByteBuffer::getShort);
    }
}
