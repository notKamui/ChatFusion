package fr.uge.teillardnajjar.chatfusion.core.reader;

import fr.uge.teillardnajjar.chatfusion.core.model.parts.Inet;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.DONE;
import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.ERROR;

public class InetReader implements Reader<Inet> {

    private final StringReader asciiReader = new StringReader(StandardCharsets.US_ASCII);
    private final ShortReader shortReader = new ShortReader();
    private State state = State.WAITING_HOSTNAME;
    private String hostname;
    private short port;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        var status = ERROR;
        if (state == State.WAITING_HOSTNAME) {
            status = asciiReader.process(buffer);
            if (status == DONE) {
                hostname = asciiReader.get();
                state = State.WAITING_PORT;
            }
        }

        if (state == State.WAITING_PORT) {
            status = shortReader.process(buffer);
            if (status == DONE) {
                port = shortReader.get();
                if (port < 0) {
                    state = State.ERROR;
                    status = ERROR;
                } else {
                    state = State.DONE;
                }
            }
        }

        return status;
    }

    @Override
    public Inet get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return new Inet(hostname, port);
    }

    @Override
    public void reset() {
        state = State.WAITING_HOSTNAME;
        asciiReader.reset();
        shortReader.reset();
        hostname = null;
        port = 0;
    }

    private enum State {DONE, WAITING_HOSTNAME, WAITING_PORT, ERROR}
}
