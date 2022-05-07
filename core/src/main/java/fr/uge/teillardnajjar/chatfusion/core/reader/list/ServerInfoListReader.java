package fr.uge.teillardnajjar.chatfusion.core.reader.list;

import fr.uge.teillardnajjar.chatfusion.core.model.part.ServerInfo;
import fr.uge.teillardnajjar.chatfusion.core.reader.Reader;
import fr.uge.teillardnajjar.chatfusion.core.reader.part.ServerInfoReader;

import java.util.List;

public class ServerInfoListReader extends ListReader<ServerInfo> implements Reader<List<ServerInfo>> {

    public ServerInfoListReader() {
        super(new ServerInfoReader());
    }
}
