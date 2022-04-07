package fr.uge.teillardnajjar.chatfusion.core.reader;

import fr.uge.teillardnajjar.chatfusion.core.model.parts.ForwardedIdentifiedFileChunk;
import fr.uge.teillardnajjar.chatfusion.core.model.parts.IdentifiedFileChunk;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.DONE;
import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.ERROR;

public class ForwardedIdentifiedFileChunkReader implements Reader<ForwardedIdentifiedFileChunk> {

    private final StringReader asciiReader = new StringReader(StandardCharsets.US_ASCII);
    private final IdentifiedFileChunkReader ifReader = new IdentifiedFileChunkReader();
    private State state = State.WAITING_USERNAME;
    private String username;
    private IdentifiedFileChunk fc;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        var status = ERROR;
        if (state == State.WAITING_USERNAME) {
            status = asciiReader.process(buffer);
            if (status == DONE) {
                username = asciiReader.get();
                state = State.WAITING_FC;
            }
        }

        if (state == State.WAITING_FC) {
            status = ifReader.process(buffer);
            if (status == DONE) {
                fc = ifReader.get();
                state = State.DONE;
            }
        }

        return status;
    }

    @Override
    public ForwardedIdentifiedFileChunk get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return new ForwardedIdentifiedFileChunk(username, fc);
    }

    @Override
    public void reset() {
        asciiReader.reset();
        ifReader.reset();
        state = State.WAITING_USERNAME;
        username = null;
        fc = null;
    }

    private enum State {DONE, WAITING_USERNAME, WAITING_FC, ERROR}
}
