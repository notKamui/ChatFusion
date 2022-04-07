package fr.uge.teillardnajjar.chatfusion.server.logic;

import fr.uge.teillardnajjar.chatfusion.core.context.Context;
import fr.uge.teillardnajjar.chatfusion.core.helper.Helpers;
import fr.uge.teillardnajjar.chatfusion.core.model.parts.ForwardedIdentifiedFileChunk;
import fr.uge.teillardnajjar.chatfusion.core.model.parts.ForwardedIdentifiedMessage;
import fr.uge.teillardnajjar.chatfusion.core.model.parts.FusionLockInfo;
import fr.uge.teillardnajjar.chatfusion.core.model.parts.IdentifiedFileChunk;
import fr.uge.teillardnajjar.chatfusion.core.model.parts.IdentifiedMessage;
import fr.uge.teillardnajjar.chatfusion.core.model.parts.Identifier;
import fr.uge.teillardnajjar.chatfusion.core.model.parts.ServerInfo;
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
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Logger;

public class Server {
    private final static Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final ServerSocketChannel ssc;
    private final InetSocketAddress isa;
    private final Selector selector;
    private final String name;
    private final Thread console;
    private final Pipe<String> pipe;
    private final Map<String, Pair<ServerInfo, ServerToServerContext>> siblings;
    private final Set<ServerInfo> potentialSiblings;
    private ServerInfo info;
    private boolean fusionLocked = false;
    private ServerInfo leader = null; // is null if is the leader

    private final Map<String, ServerToClientContext> connectedUsers;

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
        if (!pipe.isEmpty()) return;
        pipe.in(cmd);
        wakeup();
    }

    private void processCommands() {
        if (pipe.isEmpty()) return;
        var cmd = pipe.out();
        if (cmd.isEmpty()) return;
        CommandParser.parse(cmd).ifPresentOrElse(
            command -> command.execute(this),
            () -> System.out.println("! Unknown command : " + cmd)
        );
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
                ((ServerToServerContext) key.attachment()).doConnect();
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
        skey.attach(new GenericContext(skey, this));
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

    public void broadcast(String message, ServerToClientContext ctx, boolean forward) {
        var messageResp = ctx.buildMessageResp(message);
        selector.keys().stream()
            .filter(SelectionKey::isValid)
            .filter(k -> k.attachment() instanceof ServerToClientContext)
            .forEach(k -> {
                var octx = (ServerToClientContext) k.attachment();
                if (ctx == octx) return;
                octx.queueMessageResp(messageResp);
            });
        if (forward) {
            siblings.values().stream()
                .map(Pair::second)
                .forEach(dctx -> dctx.queueMsgFwd(messageResp));
        }
    }

    public void broadcast(IdentifiedMessage message) {
        var messageResp = message.toUnflippedBuffer();
        selector.keys().stream()
            .filter(SelectionKey::isValid)
            .filter(k -> k.attachment() instanceof ServerToClientContext)
            .forEach(k -> {
                var octx = (ServerToClientContext) k.attachment();
                octx.queueMessageResp(messageResp);
            });
    }

    public void broadcast(FusionLockInfo info) {
        siblings.values()
            .stream().map(Pair::second)
            .forEach(ctx -> ctx.queueFusion(info));
    }

    public void sendPrivMsg(
        IdentifiedMessage message,
        ServerToClientContext serverToClientContext
    ) {
        var dest = connectedUsers.get(message.identifier().username());
        if (dest == null) return;
        dest.queuePrivMsg(message, new Identifier(serverToClientContext.username(), name));
    }

    public void sendPrivFile(
        IdentifiedFileChunk identifiedFileChunk,
        ServerToClientContext serverToClientContext
    ) {
        var dest = connectedUsers.get(identifiedFileChunk.identifier().username());
        if (dest == null) return;
        dest.queuePrivFile(identifiedFileChunk, new Identifier(serverToClientContext.username(), name));
    }

    public String name() {
        return name;
    }

    public void wakeup() {
        selector.wakeup();
    }

    public boolean isFusionLocked() {
        return fusionLocked;
    }

    public void setFusionLock(boolean locked) {
        this.fusionLocked = locked;
    }

    public boolean checkServer(String servername) {
        return !servername.equals(name) && !siblings.containsKey(servername);
    }

    public void engageFusion(FusionLockInfo info, ServerToServerContext otherLeader) {
        confirmServer(info, otherLeader);
        System.out.println("Engaging fusion with " + potentialSiblings.size() + " potential siblings");
        potentialSiblings.forEach(this::link);
    }

    public void confirmServer(FusionLockInfo info, ServerToServerContext otherLeaderCtx) {
        var otherLeader = info.self();
        var toCompare = leader == null ? name : leader.servername();
        if (otherLeader.servername().compareTo(toCompare) < 0) {
            leader = otherLeader;
        }
        if (!info.self().servername().equals(name)) {
            siblings.put(info.self().servername(), Pair.of(otherLeader, otherLeaderCtx));
        }
        potentialSiblings.addAll(info.siblings().stream().filter(s -> !s.servername().equals(name)).toList());
        setFusionLock(true);
    }

    public void confirmServer(ServerInfo info, ServerToServerContext serverCtx) {
        siblings.put(info.servername(), Pair.of(info, serverCtx));
    }

    public InetSocketAddress address() {
        return isa;
    }

    public List<ServerInfo> siblings() {
        return siblings.values().stream().map(Pair::first).toList();
    }

    public ServerInfo info() {
        if (info != null) return info;
        var ip = address().getAddress();
        info = new ServerInfo(name, ip, (short) isa.getPort());
        return info;
    }

    public FusionLockInfo fusionLockInfo() {
        return new FusionLockInfo(info(), siblings());
    }

    public boolean isLeader() {
        return leader == null;
    }

    public boolean checkLinkServer(ServerInfo info) {
        return potentialSiblings.remove(info);
    }

    public void checkFusionFinished() {
        if (potentialSiblings.isEmpty()) {
            leader = siblings.entrySet().stream()
                .filter(e -> e.getValue().first() != leader)
                .min(Map.Entry.comparingByKey())
                .map(e -> e.getValue().first())
                .orElse(null);
            if (leader != null) {
                siblings.get(leader.servername()).second().queueFusionEnd();
            }
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
        try {
            if (leader != null) { // if is NOT leader
                forwardFusion(address, port);
                return;
            }
            var sc = SocketChannel.open();
            sc.configureBlocking(false);
            var key = sc.register(selector, SelectionKey.OP_CONNECT);
            var ctx = new ServerToServerContext(key, this, ServerToServerContext.ReqType.FUSIONREQ);
            key.attach(ctx);
            var otherAddress = new InetSocketAddress(address, port);
            sc.connect(otherAddress);
            setFusionLock(true);
        } catch (IOException e) {
            LOGGER.info("Failed to fuse");
        }
    }

    private void forwardFusion(String address, short port) {
        var leaderCtx = siblings.get(leader.servername()).second();
        leaderCtx.queueFusionReqFwdA(address, port);
    }

    public void forwardFusion(FusionLockInfo info) {
        try {
            var sc = SocketChannel.open();
            sc.configureBlocking(false);
            var key = sc.register(selector, SelectionKey.OP_CONNECT);
            var ctx = new ServerToServerContext(key, this, ServerToServerContext.ReqType.FUSIONREQRESP, info);
            key.attach(ctx);
            var otherAddress = new InetSocketAddress(info.self().ip(), info.self().port());
            sc.connect(otherAddress);
        } catch (IOException e) {
            LOGGER.info("Failed to forward");
        }
    }

    private void link(ServerInfo serverInfo) {
        try {
            var sc = SocketChannel.open();
            sc.configureBlocking(false);
            var key = sc.register(selector, SelectionKey.OP_CONNECT);
            var ctx = new ServerToServerContext(key, this, ServerToServerContext.ReqType.FUSIONLINK);
            key.attach(ctx);
            var hname = serverInfo.ip();
            var otherAddress = new InetSocketAddress(hname, serverInfo.port());
            sc.connect(otherAddress);
        } catch (IOException e) {
            LOGGER.info("Failed to link");
        }
    }

    public void forwardFusionReq(FusionLockInfo info) {
        siblings.get(leader.servername()).second().queueFusionReqFwdB(info);
    }

    public void forward(IdentifiedMessage message, ServerToClientContext ctx) {
        var dest = siblings.get(message.identifier().servername());
        if (dest == null) return;
        var fwd = new IdentifiedMessage(
            new Identifier(ctx.username(), name),
            message.message()
        );
        dest.second().queuePrivMsgFwd(message.identifier().username(), fwd);
    }

    public void forward(IdentifiedFileChunk filechunk, ServerToClientContext ctx) {
        var dest = siblings.get(filechunk.identifier().servername());
        if (dest == null) return;
        var fwd = new IdentifiedFileChunk(
            new Identifier(ctx.username(), name),
            filechunk.filename(),
            filechunk.fileSize(),
            filechunk.fileId(),
            filechunk.chunk()
        );
        dest.second().queuePrivFileFwd(filechunk.identifier().username(), fwd);
    }

    public void sendPrivMsg(ForwardedIdentifiedMessage message) {
        var dest = connectedUsers.get(message.username());
        if (dest == null) return;
        dest.queuePrivMsg(message.message(), message.message().identifier());
    }

    public void sendPrivFile(ForwardedIdentifiedFileChunk filechunk) {
        var dest = connectedUsers.get(filechunk.username());
        if (dest == null) return;
        dest.queuePrivFile(filechunk.chunk(), filechunk.chunk().identifier());
    }

    public void printInfo() {
        System.out.println("Server Info: ");
        System.out.println("\tHostname: " + address().getAddress());
        System.out.println("\tPort: " + address().getPort());
        System.out.println("\tName: " + name);
        System.out.println("\tLeader: " + (leader == null ? "No leader" : leader));
        System.out.println("\tSiblings: " + siblings.size());
        System.out.println("\tUsers: " + connectedUsers.size());
    }
}
