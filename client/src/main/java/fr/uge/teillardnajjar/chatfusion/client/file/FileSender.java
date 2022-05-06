package fr.uge.teillardnajjar.chatfusion.client.file;

import fr.uge.teillardnajjar.chatfusion.client.command.PrivateFileCommand;
import fr.uge.teillardnajjar.chatfusion.client.logic.ClientContext;
import fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Random;

public class FileSender {
    private final static int BUFFER_SIZE = 1024;
    private final static Charset ASCII = StandardCharsets.US_ASCII;
    private final static Charset UTF8 = StandardCharsets.UTF_8;
    private final static Random random = new Random();

    private final ByteBuffer chunk = ByteBuffer.allocate(BUFFER_SIZE);
    private final ByteBuffer header;
    private final FileChannel file;

    private Thread sender;

    private FileSender(String username, String servername, String filename) throws IOException {
        var unameBuffer = ASCII.encode(username);
        var snameBuffer = ASCII.encode(servername);
        var fnameBuffer = UTF8.encode(Path.of(filename).getFileName().toString());
        var fileId = Math.abs(random.nextInt());
        file = new FileInputStream(filename).getChannel();
        var fileSize = file.size();
        header = ByteBuffer.allocate(
            1
                + Integer.BYTES * 3
                + Long.BYTES
                + unameBuffer.remaining() + 5 + fnameBuffer.remaining()
        );
        header.put(OpCodes.PRIVFILE)
            .putInt(unameBuffer.remaining())
            .put(unameBuffer)
            .put(snameBuffer)
            .putInt(fnameBuffer.remaining())
            .put(fnameBuffer)
            .putLong(fileSize)
            .putInt(fileId);
    }

    public static FileSender from(PrivateFileCommand cmd) throws IOException {
        return new FileSender(cmd.targetUsername(), cmd.targetServername(), cmd.path());
    }

    /*public void sendAsync(ClientContext ctx) {
        sender = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    chunk.clear();
                    var read = file.read(chunk);
                    if (read == -1) break;
                    header.flip();
                    chunk.flip();
                    if (read < BUFFER_SIZE) chunk.limit(read);
                    var buffer = ByteBuffer.allocate(header.remaining() + Integer.BYTES + chunk.remaining());
                    buffer.put(header).putInt(read).put(chunk).flip();
                    ctx.queuePacket(buffer);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            ctx.finishSender(this);
       n });
        sender.setDaemo(true);
        sender.start();
    }*/

    public void sendFile(ClientContext ctx) throws IOException {
        while (!Thread.currentThread().isInterrupted()) {
            chunk.clear();
            var read = file.read(chunk);
            if (read == -1) {
                break;
            }
            header.flip();
            chunk.flip();
            if (read < BUFFER_SIZE) {
                chunk.limit(read);
            }
            var buffer = ByteBuffer.allocate(header.remaining() + Integer.BYTES + chunk.remaining());
            buffer.put(header).putInt(read).put(chunk).flip();
            ctx.fillFileQueue(buffer);
        }
    }

    public void cancel() {
        sender.interrupt();
    }
}