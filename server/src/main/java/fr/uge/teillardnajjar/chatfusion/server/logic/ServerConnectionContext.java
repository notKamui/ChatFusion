package fr.uge.teillardnajjar.chatfusion.server.logic;

import fr.uge.teillardnajjar.chatfusion.core.context.AbstractContext;
import fr.uge.teillardnajjar.chatfusion.core.context.Context;

import java.nio.channels.SelectionKey;

public class ServerConnectionContext extends AbstractContext implements Context {
    private final Server server;

    public ServerConnectionContext(SelectionKey key, Server server) {
        super(key);
        this.server = server;
    }

    void doConnect() {
        // TODO
    }
}
