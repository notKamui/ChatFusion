package fr.uge.teillardnajjar.chatfusion.core.reader;

import fr.uge.teillardnajjar.chatfusion.core.model.parts.IdentifiedMessage;
import fr.uge.teillardnajjar.chatfusion.core.model.parts.Identifier;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.DONE;
import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.ERROR;

public class IdentifiedMessageReader implements Reader<IdentifiedMessage> {

    private final IdentifierReader identifierReader = new IdentifierReader();
    private final StringReader utf8Reader = new StringReader(StandardCharsets.UTF_8);
    private State state = State.WAITING_ID;
    private Identifier identifier;
    private String message;

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
                state = State.WAITING_MESSAGE;
            }
        }

        System.out.println("ID state = " + state);

        if (state == State.WAITING_MESSAGE) {
            status = utf8Reader.process(buffer);
            if (status == DONE) {
                message = utf8Reader.get();
                state = State.DONE;
            }
        }

        System.out.println("MESSAGE state = " + state);

        return status;
    }

    @Override
    public IdentifiedMessage get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return new IdentifiedMessage(identifier, message);
    }

    @Override
    public void reset() {
        state = State.WAITING_ID;
        identifierReader.reset();
        utf8Reader.reset();
        identifier = null;
        message = null;
    }

    private enum State {DONE, WAITING_ID, WAITING_MESSAGE, ERROR}
}
