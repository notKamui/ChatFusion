package fr.uge.teillardnajjar.chatfusion.server.logic;

import fr.uge.teillardnajjar.chatfusion.core.context.Context;
import fr.uge.teillardnajjar.chatfusion.core.helper.Helpers;
import fr.uge.teillardnajjar.chatfusion.core.model.parts.IdentifiedFileChunk;
import fr.uge.teillardnajjar.chatfusion.core.model.parts.IdentifiedMessage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

public class Server {
    private final static Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final ServerSocketChannel ssc;
    private final Selector selector;
    private final String name;

    private final Map<String, ServerToClientContext> connectedUsers;

    public Server(int port, String name) throws IOException {
        Objects.requireNonNull(name);
        if (port < 0) throw new IllegalArgumentException("port must be positive");
        if (name.length() != 5 || name.contains("\0"))
            throw new IllegalArgumentException("name must be 5 non-zero characters long");
        this.ssc = ServerSocketChannel.open();
        this.ssc.bind(new InetSocketAddress(port));
        this.selector = Selector.open();
        this.name = name;
        this.connectedUsers = new HashMap<>();
    }

    public void launch() throws IOException {
        ssc.configureBlocking(false);
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        while (!Thread.interrupted()) {
            Helpers.printKeys(selector);
            System.out.println("Starting select");
            try {
                selector.select(this::treatKey);
            } catch (UncheckedIOException e) {
                throw e.getCause();
            }
            System.out.println("Select finished");
        }
    }

    private void treatKey(SelectionKey key) {
        Helpers.printSelectedKey(key); // for debug
        try {
            if (key.isValid() && key.isAcceptable()) {
                doAccept();
            }
        } catch (IOException ioe) {
            // lambda call in select requires to tunnel IOException
            throw new UncheckedIOException(ioe);
        }
        try {
            if (key.isValid() && key.isWritable()) {
                ((Context) key.attachment()).doWrite();
            }
            if (key.isValid() && key.isReadable()) {
                ((Context) key.attachment()).doRead();
            }
        } catch (IOException e) {
            LOGGER.info("Connection closed with client due to IOException");
            silentlyClose(key);
        }
    }

    private void doAccept() throws IOException {
        var client = ssc.accept();
        if (client == null) {
            LOGGER.warning("accept() returned null");
            return;
        }
        client.configureBlocking(false);
        var skey = client.register(selector, SelectionKey.OP_READ);
        skey.attach(new ServerToClientContext(skey, this));
    }

    private void silentlyClose(SelectionKey key) {
        Channel sc = key.channel();
        try {
            sc.close();
        } catch (IOException e) {
            // ignore exception
        }
    }

    public Set<String> connectedUsers() {
        return connectedUsers.keySet();
    }

    public void confirmUser(String username, ServerToClientContext ctx) {
        connectedUsers.put(username, ctx);
    }

    public void disconnectUser(String username) {
        connectedUsers.remove(username);
    }

    public void broadcast(String message, ServerToClientContext ctx) {
        var messageResp = ctx.buildMessageResp(message);
        selector.keys().stream()
            .filter(SelectionKey::isValid)
            .filter(k -> k.attachment() instanceof ServerToClientContext)
            .forEach(k -> {
                var octx = (ServerToClientContext) k.attachment();
                if (ctx == octx) return;
                octx.queueMessageResp(messageResp);
            });
        // TODO forward
    }

    public void sendPrivMsg(
        IdentifiedMessage message,
        ServerToClientContext serverToClientContext
    ) {
        connectedUsers.get(message.identifier().username())
            .queuePrivMsg(message, serverToClientContext);
    }

    public void sendPrivFile(
        IdentifiedFileChunk identifiedFileChunk,
        ServerToClientContext serverToClientContext
    ) {
        connectedUsers.get(identifiedFileChunk.identifier().username())
            .queuePrivFile(identifiedFileChunk, serverToClientContext);
    }

    public String name() {
        return name;
    }
}
