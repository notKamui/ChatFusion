package fr.uge.teillardnajjar.chatfusion.core.reader.part;

import fr.uge.teillardnajjar.chatfusion.core.model.part.FusionLockInfo;
import fr.uge.teillardnajjar.chatfusion.core.model.part.ServerInfo;
import fr.uge.teillardnajjar.chatfusion.core.reader.Reader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.List;

public class FusionLockInfoReaderTest {

    @Test
    public void test() {
        var address = new InetSocketAddress("localhost", 8080);
        var si = new ServerInfo("aaaaa", address.getAddress(), (short) address.getPort());
        var l = new FusionLockInfo(si, List.of(si, si, si));

        var reader = new FusionLockInfoReader();
        var packet = l.toBuffer();

        while (reader.process(packet) != Reader.ProcessStatus.DONE) ;
        Assertions.assertEquals(l, reader.get());

        reader.reset();
        packet = l.toBuffer();
        while (reader.process(packet) != Reader.ProcessStatus.DONE) ;
        Assertions.assertEquals(l, reader.get());
    }
}
