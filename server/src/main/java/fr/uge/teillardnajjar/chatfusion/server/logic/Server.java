package fr.uge.teillardnajjar.chatfusion.server.logic;

import fr.uge.teillardnajjar.chatfusion.core.context.Context;
import fr.uge.teillardnajjar.chatfusion.core.helper.Helpers;
import fr.uge.teillardnajjar.chatfusion.core.model.part.FusionLockInfo;
import fr.uge.teillardnajjar.chatfusion.core.model.part.Identifier;
import fr.uge.teillardnajjar.chatfusion.core.model.part.Inet;
import fr.uge.teillardnajjar.chatfusion.core.model.part.ServerInfo;
import fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes;
import fr.uge.teillardnajjar.chatfusion.core.util.Pair;
import fr.uge.teillardnajjar.chatfusion.core.util.concurrent.Pipe;
import fr.uge.teillardnajjar.chatfusion.server.command.CommandParser;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class Server {
    private final static Logger LOGGER = Logger.getLogger(Server.class.getName());
    private boolean debug = false;

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
    private ServerInfo potentialLeader = null;
    private int awaitedFusionEnd = 0;
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

        printInfo();

        while (!Thread.interrupted()) {
            if (debug) Helpers.printKeys(selector);
            try {
                selector.select(this::treatKey);
                processCommands();
            } catch (UncheckedIOException e) {
                throw e.getCause();
            }
        }
    }

    private void treatKey(SelectionKey key) {
        if (debug) Helpers.printSelectedKey(key); // for debug
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
            unlock();
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

    public void fusion(String address, short port) {
        if (isLeader()) engageFusionRequest(address, port);
        else forwardToLeader(new Inet(address, port).toBuffer(), OpCodes.FUSIONREQFWDA);
    }

    public void toggleDebug() {
        debug = !debug;
    }

    public void printInfo() {
        System.out.printf("""
                Server Info:
                    Port: %s
                    Name: %s
                    Leader: %s
                    Siblings: %d
                    Users: %d
                    Locked: %s
                """,
            isa.getPort(),
            name,
            leader == null ? "Is the leader" : leader.toString(),
            siblings.size(),
            connectedUsers.size(),
            fusionLocked
        );
    }

    //================================== GETTERS =============================================

    /**
     * Lazily returns the ServerInfo of the server.
     *
     * @return the ServerInfo of the server
     */
    public ServerInfo info() {
        if (info != null) return info;
        var ip = isa.getAddress();
        info = new ServerInfo(name, ip, (short) isa.getPort());
        return info;
    }

    /**
     * Returns the current fusion lock info of the server (itself + its siblings).
     *
     * @return the current FusionLockInfo of the server
     */
    public FusionLockInfo fusionLockInfo() {
        return new FusionLockInfo(info(), siblings.values().stream().map(Pair::first).toList());
    }

    /**
     * Gets the name of the server.
     *
     * @return the name of the server
     */
    public String name() {
        return name;
    }

    /**
     * Defines if the server is the leader of its group.
     *
     * @return true if the server is the leader, false otherwise
     */
    public boolean isLeader() {
        return leader == null;
    }

    /**
     * Returns the context of the group's leader.
     *
     * @return the context of the group's leader
     */
    public ServerConnectionContext leaderCtx() {
        Objects.requireNonNull(leader);
        return siblings.get(leader.servername()).second();
    }

    public void unlock() {
        fusionLocked = false;
    }

    //================================ PROCESSING ========================================

    /**
     * Checks if the given login is available, and if so, adds it to the list of connected users.
     *
     * @param username the login to check
     * @param ctx      the context of the client
     * @return true if the login is available, false otherwise
     */
    public boolean checkLogin(String username, ServerConnectionContext ctx) {
        if (connectedUsers.containsKey(username)) return false;
        connectedUsers.put(username, ctx);
        return true;
    }

    /**
     * Removes the given login from the list of connected users.
     *
     * @param username the login to remove
     */
    public void disconnectUser(String username) {
        connectedUsers.remove(username);
    }

    /**
     * Broadcasts a buffer to all connected users.
     *
     * @param msgBuffer the buffer to broadcast
     */
    public void broadcast(ByteBuffer msgBuffer, boolean forward) {
        connectedUsers.values().forEach(cctx -> cctx.queueWithOpcode(msgBuffer, OpCodes.MSGRESP));
        if (!forward) return;
        siblings.values().stream()
            .map(Pair::second)
            .forEach(sctx -> sctx.queueWithOpcode(msgBuffer, OpCodes.MSGFWD));
    }

    /**
     * Sends a buffer to a specified user, beit on this server or on another.
     *
     * @param to        the user to send the buffer to
     * @param buffer    the buffer to send
     * @param opcode    the opcode of the message
     * @param fwdOpcode the opcode to use in case of a forwarding
     */
    public void sendTo(Identifier to, ByteBuffer buffer, byte opcode, byte fwdOpcode) {
        if (name.equals(to.servername())) {
            var cctx = connectedUsers.get(to.username());
            if (cctx == null) return;
            cctx.queueWithOpcode(buffer, opcode);
        } else {
            forward(to, buffer, fwdOpcode);
        }

    }

    /**
     * Forwards a buffer to a specified user on another server.
     *
     * @param to        the user to send the buffer to
     * @param buffer    the buffer to send
     * @param fwdOpcode the opcode to use
     */
    private void forward(Identifier to, ByteBuffer buffer, byte fwdOpcode) {
        var dstServer = siblings.get(to.servername());
        if (dstServer == null) return;
        var sctx = dstServer.second();
        buffer.flip();
        var unameBuffer = US_ASCII.encode(to.username());
        var fwdBuffer = ByteBuffer.allocate(Integer.BYTES + unameBuffer.remaining() + buffer.remaining())
            .putInt(unameBuffer.remaining())
            .put(unameBuffer)
            .put(buffer);
        sctx.queueWithOpcode(fwdBuffer, fwdOpcode);
    }

    /**
     * Forwards a buffer to the leader of the group
     *
     * @param buffer    the buffer to send
     * @param fwdOpcode the opcode to use
     */
    private void forwardToLeader(ByteBuffer buffer, byte fwdOpcode) {
        leaderCtx().queueWithOpcode(buffer, fwdOpcode);
    }

    private void openConnectionTo(InetSocketAddress other, Consumer<ServerConnectionContext> action) {
        try {
            var sc = SocketChannel.open();
            sc.configureBlocking(false);
            var key = sc.register(selector, SelectionKey.OP_CONNECT);
            var ctx = new ServerConnectionContext(key, this);
            key.attach(ctx);
            sc.connect(other);
            action.accept(ctx);
        } catch (IOException e) {
            LOGGER.warning("Fusion : Failed to connect to " + other);
        }
    }

    /**
     * Engages the fusion protocol with the given server.
     *
     * @param address the hostname of the server to connect to
     * @param port    the port of the server to connect to
     */
    public void engageFusionRequest(String address, short port) {
        if (fusionLocked) {
            LOGGER.warning("Fusion request engaged while fusion is locked ; ignoring");
            return;
        }
        openConnectionTo(new InetSocketAddress(address, port), ctx -> {
            ctx.queueWithOpcode(fusionLockInfo().toBuffer(), OpCodes.FUSIONREQ);
            fusionLocked = true;
        });
    }

    /**
     * This method is supposed to be called after a fusion request forward to the leader (this server)
     *
     * @param info the fusion lock info of the other group
     */
    public void answerFusionRequest(FusionLockInfo info) {
        assert isLeader();
        var address = info.self().ip();
        var port = info.self().port();
        openConnectionTo(new InetSocketAddress(address, port), ctx -> treatFusionRequestAsLeader(info, ctx));
    }


    private void denyFusionRequest(ServerConnectionContext ctx) {
        ctx.queueWithOpcode(null, OpCodes.FUSIONREQDENY);
    }

    private void treatFusionRequestAsLeader(FusionLockInfo info, ServerConnectionContext ctx) {
        assert isLeader();
        if (fusionLocked) {
            denyFusionRequest(ctx);
            return;
        }
        var sibsNames = info.siblings().stream().map(ServerInfo::servername).toList();
        if (siblings.containsKey(info.self().servername()) ||
            siblings.keySet().stream().anyMatch(sibsNames::contains)
        ) { // name conflict
            denyFusionRequest(ctx);
            return;
        }
        fusionLocked = true;
        ctx.queueWithOpcode(fusionLockInfo().toBuffer(), OpCodes.FUSIONREQACCEPT);
        acceptFusion(info, ctx);
    }

    /**
     * Handles an incoming fusion request.
     *
     * @param info the fusion lock info of the opposite group
     * @param ctx  the context to which to respond to (the leader of the other group)
     */
    public void treatFusionRequest(FusionLockInfo info, ServerConnectionContext ctx) {
        if (isLeader()) treatFusionRequestAsLeader(info, ctx);
        else {
            forwardToLeader(info.toBuffer(), OpCodes.FUSIONREQFWDB);
            ctx.readyToClose();
        }
    }

    public void acceptFusion(FusionLockInfo info, ServerConnectionContext ctx) {
        System.out.println("Fusion accepted with other leader");
        siblings.values().forEach(sib -> {
            var context = sib.second();
            context.queueWithOpcode(info.toBuffer(), OpCodes.FUSION);
        });
        siblings.put(info.self().servername(), Pair.of(info.self(), ctx));
        potentialSiblings.addAll(info.siblings());
        if (siblings.isEmpty() || potentialSiblings.isEmpty()) unlock();
        awaitedFusionEnd = siblings.size();
        if (info.self().servername().compareTo(name) < 0) leader = info.self();
    }

    public void tryLink(FusionLockInfo info) {
        assert !isLeader();
        System.out.println("Received fusion packet, trying to link to other servers");
        potentialSiblings.add(info.self());
        potentialSiblings.addAll(info.siblings());
        potentialSiblings.forEach(potSib -> openConnectionTo(
            new InetSocketAddress(potSib.ip(), potSib.port()),
            ctx -> ctx.queueWithOpcode(info().toBuffer(), OpCodes.FUSIONLINK)
        ));
        potentialLeader = leader.servername().compareTo(info.self().servername()) < 0 ? leader : info.self();
    }

    public void tryAcceptLink(ServerInfo info, ServerConnectionContext ctx) {
        if (!potentialSiblings.contains(info)) { // refusing unknown servers
            ctx.queueWithOpcode(info().toBuffer(), OpCodes.FUSIONLINKDENY);
            //ctx.readyToClose();
            return;
        }
        ctx.queueWithOpcode(info().toBuffer(), OpCodes.FUSIONLINKACCEPT);
        siblings.put(info.servername(), Pair.of(info, ctx));
    }

    private void removeFromPotentialSiblings(ServerInfo info) {
        potentialSiblings.remove(info);
        if (potentialSiblings.isEmpty()) {
            System.out.println("Fusion complete");
            if (!isLeader()) leaderCtx().queueWithOpcode(info.toBuffer(), OpCodes.FUSIONEND);
            unlock();
        }
    }

    public void linkAccept(ServerInfo info, ServerConnectionContext ctx) {
        assert !isLeader();
        System.out.println("Link accepted with " + info);
        ctx.acknowledgeServer();
        removeFromPotentialSiblings(info);
        if (potentialLeader != null) leader = potentialLeader;
        siblings.put(info.servername(), Pair.of(info, ctx));
    }

    public void linkDeny(ServerInfo info) {
        assert !isLeader();
        removeFromPotentialSiblings(info);
    }

    public void endFusion() {
        awaitedFusionEnd--;
        if (awaitedFusionEnd <= 0) {
            System.out.println("Whole fusion complete ; unlocking");
            unlock();
        }
    }
}
