package fr.uge.teillardnajjar.chatfusion.core.reader;

import fr.uge.teillardnajjar.chatfusion.core.model.frame.Frame;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.Fusion;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionEnd;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionLink;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionLinkAccept;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.FusionLinkDeny;
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
import fr.uge.teillardnajjar.chatfusion.core.model.part.FusionLockInfo;
import fr.uge.teillardnajjar.chatfusion.core.model.part.IdentifiedFileChunk;
import fr.uge.teillardnajjar.chatfusion.core.model.part.IdentifiedMessage;
import fr.uge.teillardnajjar.chatfusion.core.model.part.ServerInfo;
import fr.uge.teillardnajjar.chatfusion.core.reader.part.ForwardedIdentifiedFileChunkReader;
import fr.uge.teillardnajjar.chatfusion.core.reader.part.ForwardedIdentifiedMessageReader;
import fr.uge.teillardnajjar.chatfusion.core.reader.part.FusionLockInfoReader;
import fr.uge.teillardnajjar.chatfusion.core.reader.part.IdentifiedFileChunkReader;
import fr.uge.teillardnajjar.chatfusion.core.reader.part.IdentifiedMessageReader;
import fr.uge.teillardnajjar.chatfusion.core.reader.part.InetReader;
import fr.uge.teillardnajjar.chatfusion.core.reader.part.ServerInfoReader;
import fr.uge.teillardnajjar.chatfusion.core.reader.primitive.ByteReader;
import fr.uge.teillardnajjar.chatfusion.core.reader.sized.StringReader;
import fr.uge.teillardnajjar.chatfusion.core.util.Pair;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.util.function.Supplier;

import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.FUSION;
import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.FUSIONEND;
import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.FUSIONLINK;
import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.FUSIONLINKACCEPT;
import static fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes.FUSIONLINKDENY;
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

public class FrameReader implements Reader<Frame> {

    private final ByteReader byteReader = new ByteReader();
    private final StringReader asciiReader = new StringReader(StandardCharsets.US_ASCII);
    private final StringReader utf8Reader = new StringReader(StandardCharsets.UTF_8);
    private final IdentifiedMessageReader imReader = new IdentifiedMessageReader();
    private final ForwardedIdentifiedMessageReader fimReader = new ForwardedIdentifiedMessageReader();
    private final ForwardedIdentifiedFileChunkReader fifReader = new ForwardedIdentifiedFileChunkReader();
    private final IdentifiedFileChunkReader ifReader = new IdentifiedFileChunkReader();
    private final FusionLockInfoReader fliReader = new FusionLockInfoReader();
    private final InetReader inetReader = new InetReader();
    private final ServerInfoReader siReader = new ServerInfoReader();
    private State state = State.WAITING_OPCODE;
    private byte opcode;
    private Frame value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        var status = ERROR;
        if (state == State.WAITING_OPCODE) {
            status = byteReader.process(buffer);
            if (status == DONE) {
                opcode = byteReader.get();
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

    private Pair<Frame, ProcessStatus> processSIPayload(
        ByteBuffer buffer,
        Function<ServerInfo, Frame> generator
    ) {
        return processPayload(buffer, siReader, generator);
    }

    private ProcessStatus processOpcode(ByteBuffer buffer) {
        Pair<Frame, ProcessStatus> proc = switch (opcode) {
            case TEMP -> processPayload(buffer, asciiReader, Temp::new);
            case TEMPOK -> processNullPayload(buffer, TempOk::new);
            case TEMPKO -> processNullPayload(buffer, TempKo::new);

            case MSG -> processPayload(buffer, utf8Reader, Msg::new);
            case MSGRESP -> processIMPayload(buffer, MsgResp::new);
            case MSGFWD -> processIMPayload(buffer, MsgFwd::new);

            case PRIVMSG -> processIMPayload(buffer, PrivMsg::new);
            case PRIVMSGRESP -> processIMPayload(buffer, PrivMsgResp::new);
            case PRIVMSGFWD -> processPayload(buffer, fimReader, PrivMsgFwd::new);

            case PRIVFILE -> processIFPayload(buffer, PrivFile::new);
            case PRIVFILERESP -> processIFPayload(buffer, PrivFileResp::new);
            case PRIVFILEFWD -> processPayload(buffer, fifReader, PrivFileFwd::new);

            case FUSIONREQ -> processFLIPayload(buffer, FusionReq::new);
            case FUSIONREQFWDA -> processPayload(buffer, inetReader, FusionReqFwdA::new);
            case FUSIONREQDENY -> processNullPayload(buffer, FusionReqDeny::new);
            case FUSIONREQACCEPT -> processFLIPayload(buffer, FusionReqAccept::new);
            case FUSIONREQFWDB -> processFLIPayload(buffer, FusionReqFwdB::new);

            case FUSION -> processPayload(buffer, fliReader, Fusion::new);
            case FUSIONLINK -> processSIPayload(buffer, FusionLink::new);
            case FUSIONLINKACCEPT -> processSIPayload(buffer, FusionLinkAccept::new);
            case FUSIONLINKDENY -> processNullPayload(buffer, FusionLinkDeny::new);
            case FUSIONEND -> processSIPayload(buffer, FusionEnd::new);

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
        opcode = 0;
        byteReader.reset();
        asciiReader.reset();
        utf8Reader.reset();
        imReader.reset();
        ifReader.reset();
        fimReader.reset();
        fifReader.reset();
        fliReader.reset();
        siReader.reset();
    }

    private enum State {DONE, WAITING_OPCODE, WAITING_PAYLOAD, ERROR}
}
