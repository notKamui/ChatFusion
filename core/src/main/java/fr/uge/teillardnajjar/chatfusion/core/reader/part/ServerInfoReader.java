package fr.uge.teillardnajjar.chatfusion.core.reader.part;

import fr.uge.teillardnajjar.chatfusion.core.model.part.ServerInfo;
import fr.uge.teillardnajjar.chatfusion.core.reader.ComposedReader;
import fr.uge.teillardnajjar.chatfusion.core.reader.InetAddressReader;
import fr.uge.teillardnajjar.chatfusion.core.reader.Reader;
import fr.uge.teillardnajjar.chatfusion.core.reader.primitive.ServernameReader;
import fr.uge.teillardnajjar.chatfusion.core.reader.primitive.ShortReader;

import java.net.InetAddress;

import static fr.uge.teillardnajjar.chatfusion.core.reader.ComposedReader.inner;

public class ServerInfoReader extends PartReader<ServerInfo> implements Reader<ServerInfo> {
    private String servername;
    private InetAddress ip;
    private short port;

    @Override
    protected Reader<ServerInfo> provide() {
        return ComposedReader.with(
            () -> new ServerInfo(servername, ip, port),
            inner(new ServernameReader(), v -> servername = v),
            inner(new InetAddressReader(), v -> ip = v),
            inner(new ShortReader(), v -> port = v)
        );
    }
}
