package fr.uge.teillardnajjar.chatfusion.core.model.part;

import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServerInfoTest {

    @Test
    public void testEquals() {
        var address1 = new InetSocketAddress("localhost", 8080);
        var serverInfo1 = new ServerInfo("KAMUI", address1.getAddress(), (short) address1.getPort());

        var address2 = new InetSocketAddress("127.0.0.1", 8080);
        var serverInfo2 = new ServerInfo("KAMUI", address2.getAddress(), (short) address2.getPort());
        assertEquals(serverInfo1, serverInfo2);
    }
}
