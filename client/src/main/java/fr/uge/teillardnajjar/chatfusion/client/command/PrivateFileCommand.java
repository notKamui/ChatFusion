package fr.uge.teillardnajjar.chatfusion.client.command;

import fr.uge.teillardnajjar.chatfusion.client.logic.ClientContext;

public record PrivateFileCommand(
    String targetUsername,
    String targetServername,
    String path
) implements Command {
    @Override
    public void execute(ClientContext context) {
        context.queuePrivateFile(this);
    }
}
