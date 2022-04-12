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

        reader.reset();
        var msg = new IdentifiedMessage(new Identifier("kamui", "ABCDE"), "Hello world!");
        var packet = msg.toUnflippedBuffer().flip();
        buffer = ByteBuffer.allocate(1 + packet.remaining())
            .put(OpCodes.MSGRESP)
            .put(packet);
        while (reader.process(buffer) != Reader.ProcessStatus.DONE) ;
        var res = reader.get();
        Assertions.assertTrue(res instanceof MsgResp);
        Assertions.assertEquals(msg, ((MsgResp) res).message());
    }
}
