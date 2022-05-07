package fr.uge.teillardnajjar.chatfusion.core.reader;

import fr.uge.teillardnajjar.chatfusion.core.reader.primitive.ByteReader;
import fr.uge.teillardnajjar.chatfusion.core.reader.primitive.Int16BReader;
import fr.uge.teillardnajjar.chatfusion.core.reader.primitive.IntReader;
import fr.uge.teillardnajjar.chatfusion.core.util.Conversions;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.function.Function;

import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.DONE;
import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.ERROR;
import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.REFILL;

public class InetAddressReader implements Reader<InetAddress> {

    private final ByteReader byteReader = new ByteReader();
    private final IntReader intReader = new IntReader();
    private final Int16BReader int16BReader = new Int16BReader();
    private State state = State.WAITING_TYPE;
    private InetAddress ip;
    private Reader<?> ipReader;
    private Function<Reader<?>, byte[]> onIpDone;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if (state == State.WAITING_TYPE) {
            var status = byteReader.process(buffer);
            if (status == DONE) {
                var type = byteReader.get();
                state = State.WAITING_IP;
                if (type == 0) {
                    ipReader = intReader;
                    onIpDone = reader -> Conversions.toByteArray((int) reader.get());
                } else if (type == 1) {
                    ipReader = int16BReader;
                    onIpDone = reader -> (byte[]) reader.get();
                } else {
                    state = State.ERROR;
                    return ERROR;
                }
            } else if (status == ERROR) {
                state = State.ERROR;
                return ERROR;
            }
        }

        if (state == State.WAITING_IP) {
            var status = ipReader.process(buffer);
            if (status == DONE) {
                try {
                    ip = InetAddress.getByAddress(onIpDone.apply(ipReader));
                    state = State.DONE;
                    return DONE;
                } catch (UnknownHostException e) {
                    state = State.ERROR;
                    return ERROR;
                }
            } else if (status == ERROR) {
                state = State.ERROR;
                return ERROR;
            }
        }

        return REFILL;
    }

    @Override
    public InetAddress get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return ip;
    }

    @Override
    public void reset() {
        state = State.WAITING_TYPE;
        ip = null;
        byteReader.reset();
        intReader.reset();
        int16BReader.reset();
    }

    private enum State {DONE, WAITING_TYPE, WAITING_IP, ERROR}
}
