package fr.uge.teillardnajjar.chatfusion.core.reader.part;

import fr.uge.teillardnajjar.chatfusion.core.model.part.ForwardedIdentifiedFileChunk;
import fr.uge.teillardnajjar.chatfusion.core.model.part.IdentifiedFileChunk;
import fr.uge.teillardnajjar.chatfusion.core.reader.ComposedReader;
import fr.uge.teillardnajjar.chatfusion.core.reader.Reader;
import fr.uge.teillardnajjar.chatfusion.core.reader.sized.StringReader;

import static fr.uge.teillardnajjar.chatfusion.core.reader.ComposedReader.inner;

public class ForwardedIdentifiedFileChunkReader extends PartReader<ForwardedIdentifiedFileChunk> implements Reader<ForwardedIdentifiedFileChunk> {
    private String username;
    private IdentifiedFileChunk chunk;


    @Override
    protected Reader<ForwardedIdentifiedFileChunk> provide() {
        return ComposedReader.with(
            () -> new ForwardedIdentifiedFileChunk(username, chunk),
            inner(StringReader.ascii(), v -> username = v),
            inner(new IdentifiedFileChunkReader(), v -> chunk = v)
        );
    }
}
