package fr.uge.teillardnajjar.chatfusion.core.reader.part;

import fr.uge.teillardnajjar.chatfusion.core.model.part.IdentifiedMessage;
import fr.uge.teillardnajjar.chatfusion.core.model.part.Identifier;
import fr.uge.teillardnajjar.chatfusion.core.reader.Reader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IdentifiedMessageReaderTest {

    @Test
    public void test() {
        var reader = new IdentifiedMessageReader();
        var msg = new IdentifiedMessage(new Identifier("kamui", "ABCDE"), "Hello world!");
        var packet = msg.toUnflippedBuffer();
        while (reader.process(packet) != Reader.ProcessStatus.DONE) ;
        Assertions.assertEquals(msg, reader.get());

        reader.reset();
        packet = msg.toUnflippedBuffer();
        while (reader.process(packet) != Reader.ProcessStatus.DONE) ;
        Assertions.assertEquals(msg, reader.get());
    }
}
