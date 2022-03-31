package fr.uge.teillardnajjar.chatfusion.core.reader;

import fr.uge.teillardnajjar.chatfusion.core.model.ServerInfo;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.ERROR;
import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.REFILL;

public class ServerInfoReader implements Reader<ServerInfo> {

    private final ByteBuffer internalBuffer = ByteBuffer.allocate(16);
    private State state = State.WAITING_SERVERNAME;
    private String servername;
    private ServerInfo.Type type;
    private int ipv4;
    private long ipv6;
    private short port;

    public ServerInfoReader() {
        internalBuffer.limit(5);
    }

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        var status = ERROR;
        if (state == State.WAITING_SERVERNAME) {
            Readers.readCompact(buffer, internalBuffer);
            status = REFILL;
            if (!internalBuffer.hasRemaining()) {
                internalBuffer.flip();
                servername = StandardCharsets.US_ASCII.decode(internalBuffer).toString();
                state = State.WAITING_TYPE;
                internalBuffer.clear();
                internalBuffer.limit(1);
            }
        }

        if (state == State.WAITING_TYPE) {
            Readers.readCompact(buffer, internalBuffer);
            status = REFILL;
            if (!internalBuffer.hasRemaining()) {
                internalBuffer.flip();
                type = ServerInfo.Type.values()[internalBuffer.get()];
                internalBuffer.clear();
                if (type == ServerInfo.Type.IPv4) {
                    state = State.WAITING_IPv4;
                    internalBuffer.limit(4);
                } else if (type == ServerInfo.Type.IPv6) {
                    state = State.WAITING_IPv6;
                    internalBuffer.limit(16);
                } else {
                    throw new IllegalStateException();
                }
            }
        }

        if (state == State.WAITING_IPv4) {
            Readers.readCompact(buffer, internalBuffer);
            status = REFILL;
            if (!internalBuffer.hasRemaining()) {
                internalBuffer.flip();
                ipv4 = internalBuffer.getInt();
                internalBuffer.clear();
                state = State.WAITING_PORT;
                internalBuffer.limit(2);
            }
        }

        if (state == State.WAITING_IPv6) {
            Readers.readCompact(buffer, internalBuffer);
            status = REFILL;
            if (!internalBuffer.hasRemaining()) {
                internalBuffer.flip();
                ipv6 = internalBuffer.getLong();
                internalBuffer.clear();
                state = State.WAITING_PORT;
                internalBuffer.limit(2);
            }
        }

        if (state == State.WAITING_PORT) {
            Readers.readCompact(buffer, internalBuffer);
            status = REFILL;
            if (!internalBuffer.hasRemaining()) {
                internalBuffer.flip();
                port = internalBuffer.getShort();
                internalBuffer.clear();
                state = State.DONE;
            }
        }

        return status;
    }

    @Override
    public ServerInfo get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return new ServerInfo(servername, type, ipv4, ipv6, port);
    }

    @Override
    public void reset() {
        internalBuffer.clear();
        internalBuffer.limit(5);
        state = State.WAITING_SERVERNAME;
        servername = null;
        type = null;
        ipv4 = 0;
        ipv6 = 0;
        port = 0;
    }

    private enum State {DONE, WAITING_SERVERNAME, WAITING_TYPE, WAITING_IPv4, WAITING_IPv6, WAITING_PORT, ERROR}
}
