package fr.uge.teillardnajjar.chatfusion.core.reader.part;

import fr.uge.teillardnajjar.chatfusion.core.model.part.FusionLockInfo;
import fr.uge.teillardnajjar.chatfusion.core.model.part.ServerInfo;
import fr.uge.teillardnajjar.chatfusion.core.reader.ComposedReader;
import fr.uge.teillardnajjar.chatfusion.core.reader.Reader;
import fr.uge.teillardnajjar.chatfusion.core.reader.list.ServerInfoListReader;

import java.util.List;

import static fr.uge.teillardnajjar.chatfusion.core.reader.ComposedReader.inner;

public class FusionLockInfoReader extends PartReader<FusionLockInfo> implements Reader<FusionLockInfo> {
    private ServerInfo self;
    private List<ServerInfo> siblings;

    @Override
    protected Reader<FusionLockInfo> provide() {
        return ComposedReader.with(
            () -> new FusionLockInfo(self, siblings),
            inner(new ServerInfoReader(), v -> self = v),
            inner(new ServerInfoListReader(), v -> siblings = v)
        );
    }
}
