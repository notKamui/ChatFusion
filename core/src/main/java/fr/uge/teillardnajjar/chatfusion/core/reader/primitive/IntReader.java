package fr.uge.teillardnajjar.chatfusion.core.reader.primitive;

import fr.uge.teillardnajjar.chatfusion.core.reader.Reader;

import java.nio.ByteBuffer;

/**
 * {@link fr.uge.teillardnajjar.chatfusion.core.reader.Reader} implementation for int. Reads a 4 bytes integer.
 */
public class IntReader extends PrimitiveReader<Integer> implements Reader<Integer> {
    public IntReader() {
        super(Integer.BYTES, ByteBuffer::getInt);
    }
}