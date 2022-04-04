package fr.uge.teillardnajjar.chatfusion.core.command;

import fr.uge.teillardnajjar.chatfusion.core.context.Context;

public interface Command {
    void execute(Context context);
}
