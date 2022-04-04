package fr.uge.teillardnajjar.chatfusion.client.command;

import fr.uge.teillardnajjar.chatfusion.client.ClientContext;
import fr.uge.teillardnajjar.chatfusion.core.command.Command;
import fr.uge.teillardnajjar.chatfusion.core.context.Context;

public record QuitCommand() implements Command {
    @Override
    public void execute(Context context) {
        ((ClientContext) context).exit();
    }
}
