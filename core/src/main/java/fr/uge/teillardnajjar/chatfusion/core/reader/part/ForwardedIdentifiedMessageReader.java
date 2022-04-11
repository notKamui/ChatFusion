package fr.uge.teillardnajjar.chatfusion.core.reader.part;

import fr.uge.teillardnajjar.chatfusion.core.model.part.ForwardedIdentifiedMessage;
import fr.uge.teillardnajjar.chatfusion.core.model.part.IdentifiedMessage;
import fr.uge.teillardnajjar.chatfusion.core.reader.ComposedReader;
import fr.uge.teillardnajjar.chatfusion.core.reader.Reader;
import fr.uge.teillardnajjar.chatfusion.core.reader.sized.StringReader;

import static fr.uge.teillardnajjar.chatfusion.core.reader.ComposedReader.inner;

public class ForwardedIdentifiedMessageReader extends PartReader<ForwardedIdentifiedMessage> implements Reader<ForwardedIdentifiedMessage> {
    private String username;
    private IdentifiedMessage message;


    @Override
    protected Reader<ForwardedIdentifiedMessage> provide() {
        return ComposedReader.with(
            () -> new ForwardedIdentifiedMessage(username, message),
            inner(StringReader.ascii(), v -> username = v),
            inner(new IdentifiedMessageReader(), v -> message = v)
        );
    }
}
