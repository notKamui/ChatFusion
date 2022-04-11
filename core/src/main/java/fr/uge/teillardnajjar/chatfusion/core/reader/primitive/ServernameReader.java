package fr.uge.teillardnajjar.chatfusion.core.reader.primitive;

import fr.uge.teillardnajjar.chatfusion.core.reader.Reader;

import java.nio.charset.StandardCharsets;

public class ServernameReader extends PrimitiveReader<String> implements Reader<String> {
    public ServernameReader() {
        super(5, buffer -> StandardCharsets.US_ASCII.decode(buffer).toString());
    }
}
