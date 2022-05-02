package fr.uge.teillardnajjar.chatfusion.server.command;

import fr.uge.teillardnajjar.chatfusion.server.logic.Server;

public record ToggleDebugCommand() implements Command {
    @Override
    public void execute(Server server) {
        server.toggleDebug();
    }
}
