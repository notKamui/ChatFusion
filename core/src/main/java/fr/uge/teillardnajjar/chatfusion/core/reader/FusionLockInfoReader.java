package fr.uge.teillardnajjar.chatfusion.core.reader;

import fr.uge.teillardnajjar.chatfusion.core.model.parts.FusionLockInfo;
import fr.uge.teillardnajjar.chatfusion.core.model.parts.ServerInfo;

import java.nio.ByteBuffer;
import java.util.List;

import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.DONE;
import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.ERROR;

public class FusionLockInfoReader implements Reader<FusionLockInfo> {

    private final ServerInfoReader serverInfoReader = new ServerInfoReader();
    private final ServerInfoListReader serverInfoListReader = new ServerInfoListReader();
    private State state = State.WAITING_SELF;
    private ServerInfo self;
    private List<ServerInfo> siblings;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        var status = ERROR;
        if (state == State.WAITING_SELF) {
            status = serverInfoReader.process(buffer);
            if (status == DONE) {
                self = serverInfoReader.get();
                state = State.WAITING_SIB;
            }
        }

        if (state == State.WAITING_SIB) {
            status = serverInfoListReader.process(buffer);
            if (status == DONE) {
                siblings = serverInfoListReader.get();
                state = State.DONE;
            }
        }

        return status;
    }

    @Override
    public FusionLockInfo get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return new FusionLockInfo(self, siblings);
    }

    @Override
    public void reset() {
        state = State.WAITING_SELF;
        serverInfoReader.reset();
        serverInfoListReader.reset();
        self = null;
        siblings = null;
    }

    private enum State {DONE, WAITING_SELF, WAITING_SIB, ERROR}
}
