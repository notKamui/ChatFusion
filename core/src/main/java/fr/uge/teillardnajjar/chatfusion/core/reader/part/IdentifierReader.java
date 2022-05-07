package fr.uge.teillardnajjar.chatfusion.core.reader.part;

import fr.uge.teillardnajjar.chatfusion.core.model.part.Identifier;
import fr.uge.teillardnajjar.chatfusion.core.reader.ComposedReader;
import fr.uge.teillardnajjar.chatfusion.core.reader.Reader;
import fr.uge.teillardnajjar.chatfusion.core.reader.primitive.ServernameReader;
import fr.uge.teillardnajjar.chatfusion.core.reader.sized.StringReader;

import static fr.uge.teillardnajjar.chatfusion.core.reader.ComposedReader.inner;

public class IdentifierReader extends PartReader<Identifier> implements Reader<Identifier> {
    private String username;
    private String servername;

    @Override
    protected Reader<Identifier> provide() {
        return ComposedReader.with(
            () -> new Identifier(username, servername),
            inner(StringReader.ascii(), v -> username = v),
            inner(new ServernameReader(), v -> servername = v)
        );
    }
}
