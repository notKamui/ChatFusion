package fr.uge.teillardnajjar.chatfusion.core.reader;

import fr.uge.teillardnajjar.chatfusion.core.model.parts.ServerInfo;
import fr.uge.teillardnajjar.chatfusion.core.util.Conversions;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.DONE;
import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.ERROR;
import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.REFILL;

public class ServerInfoReader implements Reader<ServerInfo> {
    private final static Charset ASCII = StandardCharsets.US_ASCII;
    private final IntReader intReader = new IntReader();
    private final Int16BReader int16BReader = new Int16BReader();
    private final ShortReader shortReader = new ShortReader();
    private final ByteBuffer typeBuffer = ByteBuffer.allocate(1);
    private final ByteBuffer servernameBuffer = ByteBuffer.allocate(5);
    private State state = State.WAITING_SERVERNAME;
    private String servername;
    private InetAddress ip;
    private short port;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        var status = ERROR;
        if (state == State.WAITING_SERVERNAME) {
            Readers.readCompact(buffer, servernameBuffer);
            status = REFILL;
            if (!servernameBuffer.hasRemaining()) {
                servernameBuffer.flip();
                servername = ASCII.decode(servernameBuffer).toString();
                if (servername.contains("\0")) {
                    state = State.ERROR;
                    status = ERROR;
                } else {
                    state = State.WAITING_TYPE;
                }
            }
        }

        if (state == State.WAITING_TYPE) {
            Readers.readCompact(buffer, typeBuffer);
            status = REFILL;
            if (!typeBuffer.hasRemaining()) {
                typeBuffer.flip();
                var type = ServerInfo.Type.values()[typeBuffer.get()];
                typeBuffer.clear();
                if (type == ServerInfo.Type.IPv4) {
                    state = State.WAITING_IPv4;
                } else if (type == ServerInfo.Type.IPv6) {
                    state = State.WAITING_IPv6;
                } else {
                    state = State.ERROR;
                    status = ERROR;
                }
            }
        }

        if (state == State.WAITING_IPv4) {
            status = intReader.process(buffer);
            if (status == DONE) {
                try {
                    ip = InetAddress.getByAddress(Conversions.toByteArray(intReader.get()));
                    state = State.WAITING_PORT;
                } catch (UnknownHostException e) {
                    status = ERROR;
                    state = State.ERROR;
                }
            }
        }

        if (state == State.WAITING_IPv6) {
            status = int16BReader.process(buffer);
            if (status == DONE) {
                try {
                    ip = InetAddress.getByAddress(int16BReader.get());
                    state = State.WAITING_PORT;
                } catch (UnknownHostException e) {
                    status = ERROR;
                    state = State.ERROR;
                }
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
    public ServerInfo get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return new ServerInfo(servername, ip, port);
    }

    @Override
    public void reset() {
        state = State.WAITING_SERVERNAME;
        servername = null;
        ip = null;
        port = 0;
        intReader.reset();
        int16BReader.reset();
        shortReader.reset();
        typeBuffer.clear();
        servernameBuffer.clear();
    }

    private enum State {DONE, WAITING_SERVERNAME, WAITING_TYPE, WAITING_IPv4, WAITING_IPv6, WAITING_PORT, ERROR}
}
