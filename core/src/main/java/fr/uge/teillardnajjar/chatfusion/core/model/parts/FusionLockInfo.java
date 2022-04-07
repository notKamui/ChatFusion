package fr.uge.teillardnajjar.chatfusion.core.model.parts;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;

public record FusionLockInfo(
    ServerInfo self,
    List<ServerInfo> siblings
) {
    public FusionLockInfo {
        Objects.requireNonNull(self);
        Objects.requireNonNull(siblings);
    }

    public ByteBuffer toBuffer() {
        var self = this.self.toBuffer();
        var sibBuffers = siblings.stream().map(ServerInfo::toBuffer).toList();
        var sibsTotalSize = sibBuffers.stream().mapToInt(ByteBuffer::remaining).sum();
        ByteBuffer buffer = ByteBuffer.allocate(self.remaining() + Integer.BYTES + sibsTotalSize);
        buffer.put(self).putInt(siblings.size());
        sibBuffers.forEach(buffer::put);
        sibBuffers.forEach(buffer::put);
        buffer.flip();
        return buffer;
    }
}
