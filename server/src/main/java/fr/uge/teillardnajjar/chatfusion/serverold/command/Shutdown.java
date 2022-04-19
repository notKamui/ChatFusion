package fr.uge.teillardnajjar.chatfusion.serverold.command;

import fr.uge.teillardnajjar.chatfusion.serverold.logic.Server;

public record Shutdown() implements Command {
    @Override
    public void execute(Server server) {
        server.shutdown();
    }
}
