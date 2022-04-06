package fr.uge.teillardnajjar.chatfusion.core.reader;

import java.nio.ByteBuffer;

/**
 * {@link Reader} implementation for int. Reads a 4 bytes integer.
 */
public class IntReader extends GenericReader<Integer> implements Reader<Integer> {
    public IntReader() {
        super(Integer.BYTES, ByteBuffer::getInt);
    }
}