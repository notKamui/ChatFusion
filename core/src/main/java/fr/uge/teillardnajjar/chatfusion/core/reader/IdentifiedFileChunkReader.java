package fr.uge.teillardnajjar.chatfusion.core.reader;

import fr.uge.teillardnajjar.chatfusion.core.model.parts.IdentifiedFileChunk;
import fr.uge.teillardnajjar.chatfusion.core.model.parts.Identifier;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.DONE;
import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.ERROR;

public class IdentifiedFileChunkReader implements Reader<IdentifiedFileChunk> {

    private final IdentifierReader identifierReader = new IdentifierReader();
    private final StringReader utf8StringReader = new StringReader(StandardCharsets.UTF_8);
    private final IntReader intReader = new IntReader();
    private final LongReader longReader = new LongReader();
    private final ByteChunkReader chunkReader = new ByteChunkReader();
    private State state = State.WAITING_ID;
    private Identifier identifier;
    private String filename;
    private long filesize;
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
            }
        }

        if (state == State.WAITING_FILENAME) {
            status = utf8StringReader.process(buffer);
            if (status == DONE) {
                filename = utf8StringReader.get();
                state = State.WAITING_FILESIZE;
            }
        }

        if (state == State.WAITING_FILESIZE) {
            status = longReader.process(buffer);
            if (status == DONE) {
                filesize = longReader.get();
                if (filesize < 0) {
                    state = State.ERROR;
                    status = ERROR;
                } else {
                    state = State.WAITING_FILEID;
                }
            }
        }

        if (state == State.WAITING_FILEID) {
            status = intReader.process(buffer);
            if (status == DONE) {
                fileid = intReader.get();
                if (fileid < 0) {
                    state = State.ERROR;
                    status = ERROR;
                } else {
                    state = State.WAITING_CHUNK;
                }
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
        longReader.reset();
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
