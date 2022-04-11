package fr.uge.teillardnajjar.chatfusion.core.reader.part;

import fr.uge.teillardnajjar.chatfusion.core.model.part.Inet;
import fr.uge.teillardnajjar.chatfusion.core.reader.ComposedReader;
import fr.uge.teillardnajjar.chatfusion.core.reader.Reader;
import fr.uge.teillardnajjar.chatfusion.core.reader.primitive.ShortReader;
import fr.uge.teillardnajjar.chatfusion.core.reader.sized.StringReader;

import static fr.uge.teillardnajjar.chatfusion.core.reader.ComposedReader.inner;

public class InetReader extends PartReader<Inet> implements Reader<Inet> {
    private String hostname;
    private short port;

    @Override
    protected Reader<Inet> provide() {
        return ComposedReader.with(
            () -> new Inet(hostname, port),
            inner(StringReader.ascii(), v -> hostname = v),
            inner(new ShortReader(), v -> port = v)
        );
    }
}
