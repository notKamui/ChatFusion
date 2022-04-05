package fr.uge.teillardnajjar.chatfusion.client.command;

import fr.uge.teillardnajjar.chatfusion.client.logic.ClientContext;
import fr.uge.teillardnajjar.chatfusion.core.command.Command;
import fr.uge.teillardnajjar.chatfusion.core.context.Context;

public record PrivateMessageCommand(
    String targetUsername,
    String targetServername,
    String message
) implements Command {
    @Override
    public void execute(Context context) {
        ((ClientContext) context).queuePrivateMessage(this);
    }
}
