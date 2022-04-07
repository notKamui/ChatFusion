package fr.uge.teillardnajjar.chatfusion.client.command;

import fr.uge.teillardnajjar.chatfusion.client.logic.ClientContext;

public record PrivateMessageCommand(
    String targetUsername,
    String targetServername,
    String message
) implements Command {
    @Override
    public void execute(ClientContext context) {
        context.queuePrivateMessage(this);
    }
}
