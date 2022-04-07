package fr.uge.teillardnajjar.chatfusion.client.logic;

import fr.uge.teillardnajjar.chatfusion.client.command.CommandParser;
import fr.uge.teillardnajjar.chatfusion.core.model.parts.IdentifiedFileChunk;
import fr.uge.teillardnajjar.chatfusion.core.model.parts.IdentifiedMessage;
import fr.uge.teillardnajjar.chatfusion.core.util.concurrent.Pipe;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Scanner;
import java.util.logging.Logger;

public class Client {
    private final static Logger LOGGER = Logger.getLogger(Client.class.getName());

    private final SocketChannel sc;
    private final InetSocketAddress serverAddress;
    private final Selector selector;
    private final Thread console;
    private final Pipe<String> pipe;
    private final Path downloadFolder;
    private final String login;

    private ClientContext context;

    public Client(
        InetSocketAddress serverAddress,
        Path downloadFolder,
        String login
    ) throws IOException {
        Objects.requireNonNull(serverAddress);
        Objects.requireNonNull(downloadFolder);
        Objects.requireNonNull(login);

        this.sc = SocketChannel.open();
        this.serverAddress = serverAddress;
        this.selector = Selector.open();
        this.console = new Thread(this::consoleRun);
        this.pipe = new Pipe<>();
        this.downloadFolder = downloadFolder;
        this.login = login;
    }

    /**
     * Launches the application.
     *
     * @throws IOException if an IOException occurs, notably if there's a problem at the level of the network card
     */
    public void launch() throws IOException {
        sc.configureBlocking(false);
        var key = sc.register(selector, SelectionKey.OP_CONNECT);
        context = new ClientContext(key, this);
        key.attach(context);
        sc.connect(serverAddress);

        console.setDaemon(true);
        console.start();

        while (!Thread.interrupted()) {
            try {
                selector.select(this::treatKey);
                processCommands();
            } catch (UncheckedIOException tunneled) {
                silentlyClose();
                throw tunneled.getCause();
            }
        }
    }

    void logMessage(String msg) {
        System.out.println(msg);
    }

    void logMessage(IdentifiedMessage msg, boolean priv) {
        if (priv) System.out.print(">>> ");
        System.out.println(msg);
    }

    public void logMessage(IdentifiedFileChunk identifiedFileChunk) {
        System.out.println(">>> " + identifiedFileChunk);
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

    /**
     * Send instructions to the selector via the pipe and wakes it up
     *
     * @param cmd the command to send
     * @throws InterruptedException if the selector is not ready
     */
    private void sendCommand(String cmd) throws InterruptedException {
        if (!pipe.isEmpty()) return;
        pipe.in(cmd);
        wakeup();
    }

    /**
     * Processes the command from the pipe
     */
    private void processCommands() {
        if (pipe.isEmpty()) return;
        var cmd = pipe.out();
        if (cmd.isEmpty()) return;
        CommandParser.parse(cmd).ifPresentOrElse(
            command -> command.execute(context),
            () -> System.out.println("! Unknown command : " + cmd)
        );
    }

    private void silentlyClose() {
        try {
            if (sc != null) sc.close();
        } catch (IOException e) {
            // ignore exception
        }
    }

    private void treatKey(SelectionKey key) {
        try {
            if (key.isValid() && key.isConnectable()) {
                context.doConnect();
            }
            if (key.isValid() && key.isWritable()) {
                context.doWrite();
            }
            if (key.isValid() && key.isReadable()) {
                context.doRead();
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    public String login() {
        return login;
    }

    public Path downloadFolder() {
        return downloadFolder;
    }

    public void wakeup() {
        selector.wakeup();
    }
}
