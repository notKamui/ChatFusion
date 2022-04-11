package fr.uge.teillardnajjar.chatfusion.core.reader.part;

import fr.uge.teillardnajjar.chatfusion.core.model.part.IdentifiedFileChunk;
import fr.uge.teillardnajjar.chatfusion.core.model.part.Identifier;
import fr.uge.teillardnajjar.chatfusion.core.reader.ComposedReader;
import fr.uge.teillardnajjar.chatfusion.core.reader.Reader;
import fr.uge.teillardnajjar.chatfusion.core.reader.primitive.IntReader;
import fr.uge.teillardnajjar.chatfusion.core.reader.primitive.LongReader;
import fr.uge.teillardnajjar.chatfusion.core.reader.sized.ByteChunkReader;
import fr.uge.teillardnajjar.chatfusion.core.reader.sized.StringReader;

import java.nio.ByteBuffer;

import static fr.uge.teillardnajjar.chatfusion.core.reader.ComposedReader.inner;

public class IdentifiedFileChunkReader extends PartReader<IdentifiedFileChunk> implements Reader<IdentifiedFileChunk> {
    private Identifier identifier;
    private String filename;
    private long fileSize;
    private int fileId;
    private ByteBuffer chunk;

    @Override
    protected Reader<IdentifiedFileChunk> provide() {
        return ComposedReader.with(
            () -> new IdentifiedFileChunk(identifier, filename, fileSize, fileId, chunk),
            inner(new IdentifierReader(), v -> identifier = v),
            inner(StringReader.utf8(), v -> filename = v),
            inner(new LongReader(), v -> fileSize = v),
            inner(new IntReader(), v -> fileId = v),
            inner(new ByteChunkReader(), v -> chunk = v)
        );
    }
}
