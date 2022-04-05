package fr.uge.teillardnajjar.chatfusion.core.reader;

import fr.uge.teillardnajjar.chatfusion.core.model.frame.Frame;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.Fusion;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionEnd;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionLink;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionLinkAccept;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionReq;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionReqAccept;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionReqDeny;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionReqFwdA;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionReqFwdB;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.Msg;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.MsgFwd;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.MsgResp;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.PrivFile;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.PrivFileFwd;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.PrivFileResp;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.PrivMsg;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.PrivMsgFwd;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.PrivMsgResp;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.Temp;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.TempKo;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.TempOk;
import fr.uge.teillardnajjar.chatfusion.core.model.parts.ForwardedFusionLockInfo;
import fr.uge.teillardnajjar.chatfusion.core.model.parts.FusionLockInfo;
import fr.uge.teillardnajjar.chatfusion.core.model.parts.IdentifiedFileChunk;
import fr.uge.teillardnajjar.chatfusion.core.model.parts.IdentifiedMessage;
import fr.uge.teillardnajjar.chatfusion.core.util.Pair;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.FUSION;
import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.FUSIONEND;
import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.FUSIONLINK;
import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.FUSIONLINKACCEPT;
import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.FUSIONREQ;
import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.FUSIONREQACCEPT;
import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.FUSIONREQDENY;
import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.FUSIONREQFWDA;
import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.FUSIONREQFWDB;
import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.MSG;
import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.MSGFWD;
import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.MSGRESP;
import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.PRIVFILE;
import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.PRIVFILEFWD;
import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.PRIVFILERESP;
import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.PRIVMSG;
import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.PRIVMSGFWD;
import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.PRIVMSGRESP;
import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.TEMP;
import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.TEMPKO;
import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.TEMPOK;
import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.DONE;
import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.ERROR;
import static fr.uge.teillardnajjar.chatfusion.core.reader.Reader.ProcessStatus.REFILL;

public class FrameReader implements Reader<Frame> {
    private final static Logger LOGGER = Logger.getLogger(FrameReader.class.getName());

    private final StringReader asciiReader = new StringReader(StandardCharsets.US_ASCII);
    private final StringReader utf8Reader = new StringReader(StandardCharsets.UTF_8);
    private final IdentifiedMessageReader imReader = new IdentifiedMessageReader();
    private final IdentifiedFileChunkReader ifReader = new IdentifiedFileChunkReader();
    private final FusionLockInfoReader fliReader = new FusionLockInfoReader();
    private final ForwardedFusionLockInfoReader ffliReader = new ForwardedFusionLockInfoReader();
    private final ServerInfoReader siReader = new ServerInfoReader();
    private final ByteBuffer opcodeBuffer = ByteBuffer.allocate(1);
    private State state = State.WAITING_OPCODE;
    private boolean popped = false;
    private byte opcode;
    private Frame value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        var status = ERROR;
        if (state == State.WAITING_OPCODE) {
            Readers.readCompact(buffer, opcodeBuffer);
            status = REFILL;
            if (!opcodeBuffer.hasRemaining()) {
                opcodeBuffer.flip();
                opcode = opcodeBuffer.get();
                LOGGER.info("RECEIVED OPCODE %s".formatted(Integer.toHexString(opcode)));
                state = State.WAITING_PAYLOAD;
            }
        }

        if (state == State.WAITING_PAYLOAD) {
            status = processOpcode(buffer);
        }

        return status;
    }

    private <T> Pair<Frame, ProcessStatus> processPayload(
        ByteBuffer buffer,
        Reader<T> reader,
        Function<T, Frame> generator
    ) {
        if (reader == null) return Pair.of(generator.apply(null), DONE);
        var status = reader.process(buffer);
        if (status == DONE) {
            return Pair.of(generator.apply(reader.get()), DONE);
        }
        return Pair.of(null, status);
    }

    private <T> Pair<Frame, ProcessStatus> processPayloadWithPop(
        ByteBuffer buffer,
        Reader<T> reader,
        Function<T, Frame> generator
    ) {
        if (!popped) {
            popped = true;
            buffer.get();
        }
        return processPayload(buffer, reader, generator);
    }

    private Pair<Frame, ProcessStatus> processNullPayload(
        ByteBuffer buffer,
        Supplier<Frame> generator
    ) {
        return processPayload(buffer, null, __ -> generator.get());
    }

    private Pair<Frame, ProcessStatus> processIMPayload(
        ByteBuffer buffer,
        Function<IdentifiedMessage, Frame> generator
    ) {
        return processPayload(buffer, imReader, generator);
    }

    private Pair<Frame, ProcessStatus> processIFPayload(
        ByteBuffer buffer,
        Function<IdentifiedFileChunk, Frame> generator
    ) {
        return processPayload(buffer, ifReader, generator);
    }

    private Pair<Frame, ProcessStatus> processFLIPayload(
        ByteBuffer buffer,
        Function<FusionLockInfo, Frame> generator
    ) {
        return processPayload(buffer, fliReader, generator);
    }

    private Pair<Frame, ProcessStatus> processFFLIPayload(
        ByteBuffer buffer,
        Function<ForwardedFusionLockInfo, Frame> generator
    ) {
        return processPayload(buffer, ffliReader, generator);
    }

    private ProcessStatus processOpcode(ByteBuffer buffer) {
        Pair<Frame, ProcessStatus> proc = switch (opcode) {
            case TEMP -> processPayload(buffer, asciiReader, Temp::new);
            case TEMPOK -> processNullPayload(buffer, TempOk::new);
            case TEMPKO -> processNullPayload(buffer, TempKo::new);

            case MSG -> processPayload(buffer, utf8Reader, Msg::new);
            case MSGRESP -> processIMPayload(buffer, MsgResp::new);
            case MSGFWD -> processPayloadWithPop(buffer, imReader, MsgFwd::new);

            case PRIVMSG -> processIMPayload(buffer, PrivMsg::new);
            case PRIVMSGRESP -> processIMPayload(buffer, PrivMsgResp::new);
            case PRIVMSGFWD -> processPayloadWithPop(buffer, imReader, PrivMsgFwd::new);

            case PRIVFILE -> processIFPayload(buffer, PrivFile::new);
            case PRIVFILERESP -> processIFPayload(buffer, PrivFileResp::new);
            case PRIVFILEFWD -> processPayloadWithPop(buffer, ifReader, PrivFileFwd::new);

            case FUSIONREQ -> processFLIPayload(buffer, FusionReq::new);
            case FUSIONREQFWDA -> processFFLIPayload(buffer, FusionReqFwdA::new);
            case FUSIONREQDENY -> processNullPayload(buffer, FusionReqDeny::new);
            case FUSIONREQACCEPT -> processFLIPayload(buffer, FusionReqAccept::new);
            case FUSIONREQFWDB -> processFFLIPayload(buffer, FusionReqFwdB::new);

            case FUSION -> processPayloadWithPop(buffer, fliReader, Fusion::new);
            case FUSIONLINK -> processPayload(buffer, siReader, FusionLink::new);
            case FUSIONLINKACCEPT -> processNullPayload(buffer, FusionLinkAccept::new);
            case FUSIONEND -> processNullPayload(buffer, FusionEnd::new);

            default -> Pair.of(null, ERROR);
        };
        if (proc.second() == DONE) {
            value = proc.first();
            state = State.DONE;
            return DONE;
        } else return proc.second();
    }

    @Override
    public Frame get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return value;
    }

    @Override
    public void reset() {
        value = null;
        state = State.WAITING_OPCODE;
        popped = false;
        opcode = 0;
        asciiReader.reset();
        utf8Reader.reset();
        imReader.reset();
        ifReader.reset();
        fliReader.reset();
        ffliReader.reset();
        siReader.reset();
    }

    private enum State {DONE, WAITING_OPCODE, WAITING_PAYLOAD, ERROR}
}
