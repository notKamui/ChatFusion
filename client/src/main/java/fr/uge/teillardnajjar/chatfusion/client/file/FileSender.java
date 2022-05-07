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

    private final String username;
    private final String filename;
    private final String servername;

    private FileSender(String username, String servername, String filename) {
        this.username = username;
        this.filename = filename;
        this.servername = servername;
    }

    public static FileSender from(PrivateFileCommand cmd) throws IOException {
        return new FileSender(cmd.targetUsername(), cmd.targetServername(), cmd.path());
    }

    public void sendFile(ClientContext ctx) throws IOException {
        var unameBuffer = ASCII.encode(username);
        var snameBuffer = ASCII.encode(servername);
        var fnameBuffer = UTF8.encode(Path.of(filename).getFileName().toString());
        var fileId = Math.abs(random.nextInt());
        try (var os = new FileInputStream(filename)) {
            var file = os.getChannel();
            var fileSize = file.size();
            var header = buildHeader(unameBuffer, fnameBuffer, snameBuffer, fileId, fileSize);
            queueChunks(ctx, file, header);
        }
    }

    private ByteBuffer buildHeader(ByteBuffer uname, ByteBuffer fname, ByteBuffer sname, int fileId, long fileSize) {
        return ByteBuffer.allocate(
                1
                    + Integer.BYTES * 3
                    + Long.BYTES
                    + uname.remaining() + 5 + fname.remaining()
            ).put(OpCodes.PRIVFILE)
            .putInt(uname.remaining())
            .put(uname)
            .put(sname)
            .putInt(fname.remaining())
            .put(fname)
            .putLong(fileSize)
            .putInt(fileId);
    }

    private void queueChunks(ClientContext ctx, FileChannel file, ByteBuffer header) throws IOException {
        var chunk = ByteBuffer.allocate(BUFFER_SIZE);
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
            ctx.queueFileChunk(buffer);
        }
    }
}
