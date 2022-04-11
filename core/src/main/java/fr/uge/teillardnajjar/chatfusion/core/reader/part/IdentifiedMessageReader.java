package fr.uge.teillardnajjar.chatfusion.core.reader.part;

import fr.uge.teillardnajjar.chatfusion.core.model.part.IdentifiedMessage;
import fr.uge.teillardnajjar.chatfusion.core.model.part.Identifier;
import fr.uge.teillardnajjar.chatfusion.core.reader.ComposedReader;
import fr.uge.teillardnajjar.chatfusion.core.reader.Reader;
import fr.uge.teillardnajjar.chatfusion.core.reader.sized.StringReader;

import static fr.uge.teillardnajjar.chatfusion.core.reader.ComposedReader.inner;

public class IdentifiedMessageReader extends PartReader<IdentifiedMessage> implements Reader<IdentifiedMessage> {
    private Identifier identifier;
    private String message;

    @Override
    protected Reader<IdentifiedMessage> provide() {
        return ComposedReader.with(
            () -> new IdentifiedMessage(identifier, message),
            inner(new IdentifierReader(), v -> identifier = v),
            inner(StringReader.utf8(), v -> message = v)
        );
    }
}
