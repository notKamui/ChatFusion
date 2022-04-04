package fr.uge.teillardnajjar.chatfusion.core.reader;

import fr.uge.teillardnajjar.chatfusion.core.model.parts.ServerInfo;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.DONE;
import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.ERROR;

public class ServerInfoListReader implements Reader<List<ServerInfo>> {

    private final IntReader intReader = new IntReader();
    private final ServerInfoReader serverInfoReader = new ServerInfoReader();
    private final List<ServerInfo> serverInfos = new ArrayList<>();
    private State state = State.WAITING_SIZE;
    private int size = 0;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        var status = ERROR;
        if (state == State.WAITING_SIZE) {
            status = intReader.process(buffer);
            if (status == DONE) {
                size = intReader.get();
                if (size < 0) status = ERROR;
                else if (size == 0) state = State.DONE;
                else state = State.WAITING_INFO;
            }
        }

        if (state == State.WAITING_INFO) {
            status = serverInfoReader.process(buffer);
            if (status == DONE) {
                serverInfos.add(serverInfoReader.get());
                if (serverInfos.size() == size) state = State.DONE;
                else serverInfoReader.reset();
            }
        }

        return status;
    }

    @Override
    public List<ServerInfo> get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return List.copyOf(serverInfos);
    }

    @Override
    public void reset() {
        state = State.WAITING_SIZE;
        size = 0;
        serverInfos.clear();
        intReader.reset();
        serverInfoReader.reset();
    }

    private enum State {DONE, WAITING_SIZE, WAITING_INFO, ERROR}
}
