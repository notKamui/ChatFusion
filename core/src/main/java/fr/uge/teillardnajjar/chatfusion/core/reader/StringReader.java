package fr.uge.teillardnajjar.chatfusion.core.reader;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.DONE;

/**
 * {@link Reader} implementation for String.
 * Reads an 4 bytes integer representing the size of the string to be read,
 * then a string of characters encoded in a given charset.
 */
public class StringReader extends SizedReader<String> implements Reader<String> {

    private final Charset charset;

    /**
     * Constructs the string reader with a specified {@link java.nio.charset.Charset}.
     *
     * @param charset the charset in which the string is encoded with
     */
    public StringReader(Charset charset) {
        this.charset = charset;
    }

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        var status = internalProcess(buffer);
        if (status != DONE) return status;
        value = charset.decode(internalBuffer).toString();
        return DONE;
    }
}