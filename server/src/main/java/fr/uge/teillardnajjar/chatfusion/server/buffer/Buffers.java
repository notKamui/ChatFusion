package fr.uge.teillardnajjar.chatfusion.server.buffer;

import fr.uge.teillardnajjar.chatfusion.core.model.parts.ServerInfo;
import fr.uge.teillardnajjar.chatfusion.core.opcode.OpCodes;
import fr.uge.teillardnajjar.chatfusion.server.logic.Server;

import java.nio.ByteBuffer;

public class Buffers {
    private Buffers() {
        throw new AssertionError("No instances");
    }

    public static ByteBuffer getFusionReqBuffer(Server server) {
        var self = server.info().toBuffer();
        var sibs = server.siblings();
        var sibBuffers = sibs.stream().map(ServerInfo::toBuffer);
        var sibsTotalSize = sibBuffers.mapToInt(ByteBuffer::remaining).sum();
        ByteBuffer buffer = ByteBuffer.allocate(1 + self.remaining() + Integer.BYTES + sibsTotalSize);
        buffer.put(OpCodes.FUSIONREQACCEPT)
            .put(self)
            .putInt(sibs.size());
        sibBuffers.forEach(buffer::put);
        buffer.flip();
        return buffer;
    }
}
