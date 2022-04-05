package fr.uge.teillardnajjar.chatfusion.server.logic;

import fr.uge.teillardnajjar.chatfusion.core.context.AbstractContext;
import fr.uge.teillardnajjar.chatfusion.core.context.Context;

import java.nio.channels.SelectionKey;
import java.util.Objects;

public class ServerToServerContext extends AbstractContext implements Context {
    private final Server server;

    public ServerToServerContext(SelectionKey key, Server server) {
        super(key);
        Objects.requireNonNull(server);
        this.server = server;
    }
}
