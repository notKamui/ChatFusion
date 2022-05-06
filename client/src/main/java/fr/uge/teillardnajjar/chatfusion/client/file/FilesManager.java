package fr.uge.teillardnajjar.chatfusion.client.file;

import fr.uge.teillardnajjar.chatfusion.client.logic.Client;
import fr.uge.teillardnajjar.chatfusion.core.model.part.IdentifiedFileChunk;
import fr.uge.teillardnajjar.chatfusion.core.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

public class FilesManager {
    private final static Logger LOGGER = Logger.getLogger(FilesManager.class.getName());

    private final Client client;

    // id -> (stream, totalWritten)
    private final Map<Integer, Pair<FileOutputStream, Long>> files = new HashMap<>();

    public FilesManager(Client client) {
        Objects.requireNonNull(client);
        this.client = client;
    }

    public void feedChunk(IdentifiedFileChunk chunk) {
        var fileId = chunk.fileId();
        var fname = client.downloadFolder().toString() + "/" + chunk.filename() + ".tmp";
        try {
            var file = files.computeIfAbsent(fileId, __ -> {
                try {
                    return Pair.of(new FileOutputStream(fname), 0L);
                } catch (FileNotFoundException e) {
                    throw new UncheckedIOException(e);
                }
            });
            var os = file.first();
            var written = os.getChannel().write(chunk.chunk().flip());
            var totalWritten = file.second() + written;
            files.put(fileId, Pair.of(os, totalWritten));
            if (totalWritten == chunk.fileSize()) {
                client.logMessage(chunk);
                os.close();
                files.remove(fileId);
                if (!new File(fname).renameTo(new File(fname.substring(0, fname.length() - 4)))) {
                    throw new IOException("Unable to rename file");
                }
            }
        } catch (UncheckedIOException | IOException e) {
            LOGGER.warning("Error while writing file : " + fname + " : " + e.getMessage());
        }
    }
}
