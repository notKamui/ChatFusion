package fr.uge.teillardnajjar.chatfusion.core.model.frame;

import fr.uge.teillardnajjar.chatfusion.core.model.IdentifiedMessage;

public record PrivMsgFwd(IdentifiedMessage message) implements Frame {
}
