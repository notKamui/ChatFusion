package fr.uge.teillardnajjar.chatfusion.core.reader;

import java.nio.ByteBuffer;

/**
 * Reader helper functions
 */
class Readers {

    private Readers() {
        throw new AssertionError("No instance");
    }

    /**
     * Reads the buffer as much as possible by transposing
     * the temporary buffer to the main one, and compacts the buffer.
     *
     * @param buffer the main buffer to read in
     * @param tmp    the temporary buffer
     */
    static void readCompact(ByteBuffer buffer, ByteBuffer tmp) {
        buffer.flip();
        try {
            if (buffer.remaining() <= tmp.remaining()) {
                tmp.put(buffer);
            } else {
                var oldLimit = buffer.limit();
                buffer.limit(tmp.remaining());
                tmp.put(buffer);
                buffer.limit(oldLimit);
            }
        } finally {
            buffer.compact();
        }
    }
}
