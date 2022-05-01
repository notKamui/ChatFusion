package fr.uge.teillardnajjar.chatfusion.core.model.part;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;

public record FusionLockInfo(
    ServerInfo self,
    List<ServerInfo> siblings
) implements Part {
    public FusionLockInfo {
        Objects.requireNonNull(self);
        Objects.requireNonNull(siblings);
    }

    @Override
    public ByteBuffer toBuffer() {
        var self = this.self.toBuffer().flip();
        var sibBuffers = siblings.stream().map(si -> si.toBuffer().flip()).toList();
        var sibsTotalSize = sibBuffers.stream().mapToInt(ByteBuffer::remaining).sum();
        ByteBuffer buffer = ByteBuffer.allocate(self.remaining() + Integer.BYTES + sibsTotalSize);
        buffer.put(self).putInt(siblings.size());
        sibBuffers.forEach(buffer::put);
        return buffer;
    }
}
