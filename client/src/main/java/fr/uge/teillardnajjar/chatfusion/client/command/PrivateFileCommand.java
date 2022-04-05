package fr.uge.teillardnajjar.chatfusion.client.command;

import fr.uge.teillardnajjar.chatfusion.client.logic.ClientContext;
import fr.uge.teillardnajjar.chatfusion.core.command.Command;
import fr.uge.teillardnajjar.chatfusion.core.context.Context;

public record PrivateFileCommand(
    String targetUsername,
    String targetServername,
    String path
) implements Command {
    @Override
    public void execute(Context context) {
        ((ClientContext) context).queuePrivateFile(this);
    }
}
