package fr.uge.teillardnajjar.chatfusion.client.command;

import fr.uge.teillardnajjar.chatfusion.client.logic.ClientContext;

public interface Command {
    void execute(ClientContext context);
}
