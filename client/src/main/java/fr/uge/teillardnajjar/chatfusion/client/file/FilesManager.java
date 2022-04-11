package fr.uge.teillardnajjar.chatfusion.client.file;

import fr.uge.teillardnajjar.chatfusion.client.logic.Client;
import fr.uge.teillardnajjar.chatfusion.core.model.part.IdentifiedFileChunk;

import java.io.FileNotFoundException;
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

    public void feedChunk(IdentifiedFileChunk chunk) {
        var fileId = chunk.fileId();
        long size;
        synchronized (files) {
            var chunks = files.computeIfAbsent(fileId, __ -> new ArrayList<>());
            chunks.add(chunk);
            size = chunks.stream().mapToLong(c -> c.chunk().capacity()).sum();
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
            } catch (FileNotFoundException e) {
                LOGGER.warning("File not found: " + fname);
            } catch (IOException e) {
                LOGGER.warning("Error while writing file : " + fname);
            } finally {
                files.remove(fileId);
            }
        }
    }
}
