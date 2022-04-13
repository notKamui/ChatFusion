package fr.uge.teillardnajjar.chatfusion.core.reader;

import fr.uge.teillardnajjar.chatfusion.core.model.frame.MsgResp;
import fr.uge.teillardnajjar.chatfusion.core.model.frame.TempOk;
import fr.uge.teillardnajjar.chatfusion.core.model.part.IdentifiedMessage;
import fr.uge.teillardnajjar.chatfusion.core.model.part.Identifier;
import fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

public class FrameReaderTest {

    @Test
    public void test() {
        var reader = new FrameReader();
        var tempok = new TempOk();
        var buffer = ByteBuffer.allocate(1).put(OpCodes.TEMPOK);
        while (reader.process(buffer) != Reader.ProcessStatus.DONE) ;
        Assertions.assertEquals(tempok, reader.get());

        var msg = new IdentifiedMessage(new Identifier("kamui", "ABCDE"), "Hello world!");
        var packet = msg.toUnflippedBuffer().flip();
        var msg2 = new IdentifiedMessage(new Identifier("kamui", "ABCDE"), "No no");
        var packet2 = msg2.toUnflippedBuffer().flip();
        buffer = ByteBuffer.allocate(1 + packet.remaining() + 1 + packet2.remaining())
            .put(OpCodes.MSGRESP)
            .put(packet)
            .put(OpCodes.MSGRESP)
            .put(packet2);
        reader.reset();
        while (reader.process(buffer) != Reader.ProcessStatus.DONE) ;
        var res = reader.get();
        Assertions.assertTrue(res instanceof MsgResp);
        Assertions.assertEquals(msg, ((MsgResp) res).message());

        reader.reset();
        while (reader.process(buffer) != Reader.ProcessStatus.DONE) ;
        var res2 = reader.get();
        Assertions.assertTrue(res2 instanceof MsgResp);
        Assertions.assertEquals(msg2, ((MsgResp) res2).message());
    }
}
