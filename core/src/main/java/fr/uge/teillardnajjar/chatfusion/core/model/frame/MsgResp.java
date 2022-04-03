package fr.uge.teillardnajjar.chatfusion.core.model.frame;

import fr.uge.teillardnajjar.chatfusion.core.model.IdentifiedMessage;

public record MsgResp(IdentifiedMessage message) implements Frame {
}
