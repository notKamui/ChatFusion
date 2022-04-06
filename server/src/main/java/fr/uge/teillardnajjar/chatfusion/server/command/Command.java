package fr.uge.teillardnajjar.chatfusion.server.command;

import fr.uge.teillardnajjar.chatfusion.server.logic.Server;

public interface Command {
    void execute(Server server);
}
