package fr.uge.teillardnajjar.chatfusion.core.reader.primitive;

import fr.uge.teillardnajjar.chatfusion.core.reader.Reader;

import java.nio.ByteBuffer;

public class ByteReader extends PrimitiveReader<Byte> implements Reader<Byte> {
    public ByteReader() {
        super(Byte.BYTES, ByteBuffer::get);
    }
}
