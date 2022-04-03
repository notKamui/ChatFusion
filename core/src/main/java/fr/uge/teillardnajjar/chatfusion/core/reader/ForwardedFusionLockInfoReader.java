package fr.uge.teillardnajjar.chatfusion.core.reader;

import fr.uge.teillardnajjar.chatfusion.core.model.ForwardedFusionLockInfo;
import fr.uge.teillardnajjar.chatfusion.core.model.FusionLockInfo;
import fr.uge.teillardnajjar.chatfusion.core.model.ServerInfo;
import fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes;

import java.nio.ByteBuffer;

import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.DONE;
import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.ERROR;
import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.REFILL;

public class ForwardedFusionLockInfoReader implements Reader<ForwardedFusionLockInfo> {

    private final ServerInfoReader serverInfoReader = new ServerInfoReader();
    private final FusionLockInfoReader fusionLockInfoReader = new FusionLockInfoReader();
    private final ByteBuffer popper = ByteBuffer.allocate(1);
    private State state = State.WAITING_LEADER;
    private ServerInfo leader;
    private FusionLockInfo info;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        var status = ERROR;
        if (state == State.WAITING_LEADER) {
            status = serverInfoReader.process(buffer);
            if (status == DONE) {
                leader = serverInfoReader.get();
                state = State.WAITING_POP;
            }
        }

        if (state == State.WAITING_POP) {
            Readers.readCompact(buffer, popper);
            status = REFILL;
            if (!popper.hasRemaining()) {
                popper.flip();
                if (popper.get() != OpCodes.FUSIONREQ) {
                    state = State.ERROR;
                    status = ERROR;
                } else {
                    state = State.WAITING_INFO;
                }
            }
        }

        if (state == State.WAITING_INFO) {
            status = fusionLockInfoReader.process(buffer);
            if (status == DONE) {
                info = fusionLockInfoReader.get();
                state = State.DONE;
            }
        }

        return status;
    }

    @Override
    public ForwardedFusionLockInfo get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return new ForwardedFusionLockInfo(leader, info);
    }

    @Override
    public void reset() {
        state = State.WAITING_LEADER;
        serverInfoReader.reset();
        fusionLockInfoReader.reset();
        leader = null;
        info = null;
    }

    private enum State {DONE, WAITING_LEADER, WAITING_POP, WAITING_INFO, ERROR}
}
