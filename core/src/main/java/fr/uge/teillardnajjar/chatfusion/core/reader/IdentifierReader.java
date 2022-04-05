package fr.uge.teillardnajjar.chatfusion.core.reader;

import fr.uge.teillardnajjar.chatfusion.core.model.parts.Identifier;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.DONE;
import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.ERROR;
import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.REFILL;

public class IdentifierReader implements Reader<Identifier> {

    private final StringReader asciiReader = new StringReader(StandardCharsets.US_ASCII);
    private final ByteBuffer servernameBuffer = ByteBuffer.allocate(5);
    private State state = State.WAITING_USERNAME;
    private String username;
    private String servername;

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
            }
        }

        if (state == State.WAITING_SERVERNAME) {
            Readers.readCompact(buffer, servernameBuffer);
            status = REFILL;
            if (!servernameBuffer.hasRemaining()) {
                servernameBuffer.flip();
                servername = StandardCharsets.US_ASCII.decode(servernameBuffer).toString();
                state = State.DONE;
                status = DONE;
            }
        }

        return status;
    }

    @Override
    public Identifier get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return new Identifier(username, servername);
    }

    @Override
    public void reset() {
        asciiReader.reset();
        servernameBuffer.clear();
        state = State.WAITING_USERNAME;
        username = null;
        servername = null;
    }

    private enum State {DONE, WAITING_USERNAME, WAITING_SERVERNAME, ERROR}
}
