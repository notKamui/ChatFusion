package fr.uge.teillardnajjar.chatfusion.serverold.command;

import fr.uge.teillardnajjar.chatfusion.serverold.logic.Server;

public interface Command {
    void execute(Server server);
}
