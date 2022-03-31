package fr.uge.teillardnajjar.chatfusion.core.reader;

import fr.uge.teillardnajjar.chatfusion.core.model.IdentifiedFileChunk;
import fr.uge.teillardnajjar.chatfusion.core.model.Identifier;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.DONE;
import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.ERROR;
import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.REFILL;

public class IdentifiedFileChunkReader implements Reader<IdentifiedFileChunk> {

    private final IdentifierReader identifierReader = new IdentifierReader();
    private final StringReader utf8StringReader = new StringReader(StandardCharsets.UTF_8);
    private final IntReader intReader = new IntReader();
    private final ByteChunkReader chunkReader = new ByteChunkReader();
    private State state = State.WAITING_ID;
    private Identifier identifier;
    private String filename;
    private int filesize;
    private int fileid;
    private ByteBuffer chunk;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        var status = ERROR;
        if (state == State.WAITING_ID) {
            status = identifierReader.process(buffer);
            if (status == DONE) {
                identifier = identifierReader.get();
                state = State.WAITING_FILENAME;
                status = REFILL;
            }
        }

        if (state == State.WAITING_FILENAME) {
            status = utf8StringReader.process(buffer);
            if (status == DONE) {
                filename = utf8StringReader.get();
                state = State.WAITING_FILESIZE;
                status = REFILL;
            }
        }

        if (state == State.WAITING_FILESIZE) {
            status = intReader.process(buffer);
            if (status == DONE) {
                filesize = intReader.get();
                state = State.WAITING_FILEID;
                intReader.reset();
                status = REFILL;
            }
        }

        if (state == State.WAITING_FILEID) {
            status = intReader.process(buffer);
            if (status == DONE) {
                fileid = intReader.get();
                state = State.WAITING_CHUNK;
                status = REFILL;
            }
        }

        if (state == State.WAITING_CHUNK) {
            status = chunkReader.process(buffer);
            if (status == DONE) {
                chunk = chunkReader.get();
                state = State.DONE;
            }
        }

        return status;
    }

    @Override
    public IdentifiedFileChunk get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return new IdentifiedFileChunk(identifier, filename, filesize, fileid, chunk);
    }

    @Override
    public void reset() {
        state = State.WAITING_ID;
        identifier = null;
        filename = null;
        filesize = 0;
        fileid = 0;
        chunk = null;
        identifierReader.reset();
        utf8StringReader.reset();
        intReader.reset();
        chunkReader.reset();
    }

    private enum State {
        DONE,
        WAITING_ID,
        WAITING_FILENAME,
        WAITING_FILESIZE,
        WAITING_FILEID,
        WAITING_CHUNK,
        ERROR
    }
}
