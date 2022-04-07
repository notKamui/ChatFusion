package fr.uge.teillardnajjar.chatfusion.server.command;

import fr.uge.teillardnajjar.chatfusion.server.logic.Server;

public record FusionCommand(String address, short port) implements Command {

    @Override
    public void execute(Server server) {
        server.fusion(address, port);
    }
}
