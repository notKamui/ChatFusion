package fr.uge.teillardnajjar.chatfusion.core.reader;

import fr.uge.teillardnajjar.chatfusion.core.model.IdentifiedMessage;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.DONE;
import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.ERROR;
import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.REFILL;

public class IdentifiedMessageReader implements Reader<IdentifiedMessage> {

    private final StringReader asciiReader = new StringReader(StandardCharsets.US_ASCII);
    private final StringReader utf8Reader = new StringReader(StandardCharsets.UTF_8);
    private final ByteBuffer servernameBuffer = ByteBuffer.allocate(5);
    private State state = State.WAITING_USERNAME;
    private String username;
    private String servername;
    private String message;

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
                state = State.WAITING_SERVERNAME;
                asciiReader.reset();
            }
        }

        if (state == State.WAITING_SERVERNAME) {
            Readers.readCompact(buffer, servernameBuffer);
            status = REFILL;
            if (!servernameBuffer.hasRemaining()) state = State.WAITING_MESSAGE;
        }

        if (state == State.WAITING_MESSAGE) {
            status = utf8Reader.process(buffer);
            if (status == DONE) {
                message = utf8Reader.get();
                state = State.DONE;
                utf8Reader.reset();
            }
        }

        return status;
    }

    @Override
    public IdentifiedMessage get() {
        if (state == State.DONE) {
            throw new IllegalStateException();
        }
        return new IdentifiedMessage(username, servername, message);
    }

    @Override
    public void reset() {
        state = State.WAITING_USERNAME;
        username = null;
        servername = null;
        message = null;
        asciiReader.reset();
        utf8Reader.reset();
        servernameBuffer.clear();
    }

    private enum State {DONE, WAITING_USERNAME, WAITING_SERVERNAME, WAITING_MESSAGE, ERROR}
}
