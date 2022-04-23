package fr.uge.teillardnajjar.chatfusion.server.logic;

import fr.uge.teillardnajjar.chatfusion.core.context.Context;
import fr.uge.teillardnajjar.chatfusion.core.helper.Helpers;
import fr.uge.teillardnajjar.chatfusion.core.model.part.ServerInfo;
import fr.uge.teillardnajjar.chatfusion.core.util.Pair;
import fr.uge.teillardnajjar.chatfusion.core.util.concurrent.Pipe;
import fr.uge.teillardnajjar.chatfusion.server.command.CommandParser;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Logger;

public class Server {
    private final static Logger LOGGER = Logger.getLogger(fr.uge.teillardnajjar.chatfusion.serverold.logic.Server.class.getName());

    // SELF INFORMATIONS
    private final ServerSocketChannel ssc;
    private final InetSocketAddress isa;
    private final Selector selector;
    private final String name;
    private final Thread console;
    private final Pipe<String> pipe;
    private final Map<String, Pair<ServerInfo, ServerConnectionContext>> siblings;
    private final Map<String, ServerConnectionContext> connectedUsers;
    // FUSION HANDLING
    private final Set<ServerInfo> potentialSiblings;
    private ServerInfo info;
    // CONNECTIONS
    private ServerInfo leader = null; // is null if is the leader
    private boolean fusionLocked = false;


    public Server(int port, String name) throws IOException {
        Objects.requireNonNull(name);
        if (port < 0) throw new IllegalArgumentException("port must be positive");
        if (name.length() != 5 || name.contains("\0"))
            throw new IllegalArgumentException("name must be 5 non-zero characters long");
        this.ssc = ServerSocketChannel.open();
        this.isa = new InetSocketAddress(port);
        this.ssc.bind(isa);
        this.selector = Selector.open();
        this.name = name;
        this.connectedUsers = new HashMap<>();
        this.siblings = new HashMap<>();
        this.potentialSiblings = new HashSet<>();
        this.pipe = new Pipe<>();
        this.console = new Thread(this::consoleRun);
    }

    private void consoleRun() {
        try {
            try (var scanner = new Scanner(System.in)) {
                while (!Thread.interrupted() && scanner.hasNextLine()) {
                    var msg = scanner.nextLine();
                    sendCommand(msg);
                }
            }
            LOGGER.info("Console thread stopping");
        } catch (InterruptedException e) {
            LOGGER.info("Console thread has been interrupted");
        }
    }

    private void sendCommand(String cmd) throws InterruptedException {
        if (cmd.isBlank()) return;
        pipe.in(cmd);
        wakeup();
    }

    public void wakeup() {
        selector.wakeup();
    }

    private void processCommands() {
        while (!pipe.isEmpty()) {
            var cmd = pipe.out();
            if (cmd.isBlank()) continue;
            CommandParser.parse(cmd).ifPresentOrElse(
                command -> command.execute(this),
                () -> System.out.println("! Unknown command : " + cmd)
            );
        }
    }

    public void launch() throws IOException {
        ssc.configureBlocking(false);
        ssc.register(selector, SelectionKey.OP_ACCEPT);

        console.setDaemon(true);
        console.start();

        while (!Thread.interrupted()) {
            Helpers.printKeys(selector);
            System.out.println("Starting select");
            try {
                selector.select(this::treatKey);
                processCommands();
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
            if (key.isValid() && key.isConnectable()) {
                ((ServerConnectionContext) key.attachment()).doConnect();
            }
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
        skey.attach(new ServerConnectionContext(skey, this));
    }

    private void silentlyClose(SelectionKey key) {
        Channel sc = key.channel();
        try {
            sc.close();
        } catch (IOException e) {
            // ignore exception
        }
    }

    public void shutdown() {
        LOGGER.info("SHUTTING DOWN SOFTLY...");
        try {
            ssc.close();
        } catch (IOException e) {
            // ignore
        }
    }

    public void shutdownNow() {
        LOGGER.info("SHUTTING DOWN NOW...");
        selector.keys().forEach(this::silentlyClose);
        Thread.currentThread().interrupt();
    }

    public void printInfo() {
        System.out.printf("""
                Server Info:
                    Port: %s
                    Name: %s
                    Leader: %s
                    Siblings: %d
                    Users: %d
                """,
            isa.getPort(),
            name,
            leader == null ? "Is the leader" : leader.toString(),
            siblings.size(),
            connectedUsers.size()
        );
    }

    //================================== GETTERS =============================================

    public ServerInfo info() {
        if (info != null) return info;
        var ip = isa.getAddress();
        info = new ServerInfo(name, ip, (short) isa.getPort());
        return info;
    }


    //================================ PROCESSING ============================================

    public void fusion(String address, short port) {
        // TODO
    }
}
