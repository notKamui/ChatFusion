package fr.uge.teillardnajjar.chatfusion.core.reader.primitive;

import fr.uge.teillardnajjar.chatfusion.core.reader.Reader;

import java.nio.ByteBuffer;

public class Int16BReader extends PrimitiveReader<byte[]> implements Reader<byte[]> {
    public Int16BReader() {
        super(16, ByteBuffer::array);
    }
}
