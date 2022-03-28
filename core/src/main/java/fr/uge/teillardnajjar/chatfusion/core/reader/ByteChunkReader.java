package fr.uge.teillardnajjar.chatfusion.core.reader;

import java.nio.ByteBuffer;

import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.DONE;

/**
 * {@link Reader} implementation for chunks of bytes.
 * Reads an 4 bytes integer representing the size of the chunk to be read,
 * then the chunk of bytes itself, producing a {@link java.nio.ByteBuffer}
 * (in write mode).
 */
public class ByteChunkReader extends SizedReader<ByteBuffer> implements Reader<ByteBuffer> {

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        var status = internalProcess(buffer);
        if (status != DONE) return status;
        value = ByteBuffer.allocate(internalBuffer.limit());
        value.put(internalBuffer);
        return DONE;
    }
}
