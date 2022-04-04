package fr.uge.teillardnajjar.chatfusion.client;

import fr.uge.teillardnajjar.chatfusion.core.model.parts.IdentifiedFileChunk;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

public class FilesManager {
    private final static Logger LOGGER = Logger.getLogger(FilesManager.class.getName());

    private final Client client;
    private final Map<Integer, List<IdentifiedFileChunk>> files = new HashMap<>();

    public FilesManager(Client client) {
        Objects.requireNonNull(client);
        this.client = client;
    }

    void feedChunk(IdentifiedFileChunk chunk) {
        var fileId = chunk.fileId();
        int size;
        synchronized (files) {
            var chunks = files.computeIfAbsent(fileId, __ -> new ArrayList<>());
            chunks.add(chunk);
            size = chunks.stream().mapToInt(c -> c.chunk().capacity()).sum();
        }
        if (size == chunk.fileSize()) {
            var writer = new Thread(() -> writeFile(fileId));
            writer.setDaemon(true);
            writer.start();
        }
    }

    private void writeFile(int fileId) {
        synchronized (files) {
            var file = files.get(fileId);
            var fname = client.downloadFolder().toString() + "/" + file.get(0).filename();
            try (var channel = new FileOutputStream(fname).getChannel()) {
                for (var chunk : file) {
                    channel.write(chunk.chunk().flip());
                }
                client.logMessage(file.get(0));
            } catch (IOException e) {
                LOGGER.warning("Error while writing file : " + fname);
            } finally {
                files.remove(fileId);
            }
        }
    }
}