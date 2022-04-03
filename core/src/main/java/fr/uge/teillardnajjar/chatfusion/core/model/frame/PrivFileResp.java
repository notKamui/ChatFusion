package fr.uge.teillardnajjar.chatfusion.core.model.frame;

import fr.uge.teillardnajjar.chatfusion.core.model.IdentifiedFileChunk;

public record PrivFileResp(IdentifiedFileChunk identifiedFileChunk) implements Frame {
}
