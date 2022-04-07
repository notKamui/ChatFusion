package fr.uge.teillardnajjar.chatfusion.core.reader;

import fr.uge.teillardnajjar.chatfusion.core.model.parts.ForwardedIdentifiedMessage;
import fr.uge.teillardnajjar.chatfusion.core.model.parts.IdentifiedMessage;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.DONE;
import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.ERROR;

public class ForwardedIdentifiedMessageReader implements Reader<ForwardedIdentifiedMessage> {

    private final StringReader asciiReader = new StringReader(StandardCharsets.US_ASCII);
    private final IdentifiedMessageReader imReader = new IdentifiedMessageReader();
    private State state = State.WAITING_USERNAME;
    private String username;
    private IdentifiedMessage im;

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
                state = State.WAITING_IM;
            }
        }

        if (state == State.WAITING_IM) {
            status = imReader.process(buffer);
            if (status == DONE) {
                im = imReader.get();
                state = State.DONE;
            }
        }

        return status;
    }

    @Override
    public ForwardedIdentifiedMessage get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return new ForwardedIdentifiedMessage(username, im);
    }

    @Override
    public void reset() {
        asciiReader.reset();
        imReader.reset();
        state = State.WAITING_USERNAME;
        username = null;
        im = null;
    }

    private enum State {DONE, WAITING_USERNAME, WAITING_IM, ERROR}
}
