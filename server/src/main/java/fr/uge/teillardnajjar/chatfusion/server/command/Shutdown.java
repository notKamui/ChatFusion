package fr.uge.teillardnajjar.chatfusion.server.command;

import fr.uge.teillardnajjar.chatfusion.server.logic.Server;

public record Shutdown() implements Command {
    @Override
    public void execute(Server server) {
        server.shutdown();
    }
}
