package fr.uge.teillardnajjar.chatfusion.core.model.frame;

import fr.uge.teillardnajjar.chatfusion.core.model.IdentifiedMessage;

public record MsgFwd(IdentifiedMessage message) implements Frame {
}
