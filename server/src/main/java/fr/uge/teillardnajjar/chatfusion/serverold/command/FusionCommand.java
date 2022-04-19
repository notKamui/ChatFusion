package fr.uge.teillardnajjar.chatfusion.serverold.command;

import fr.uge.teillardnajjar.chatfusion.serverold.logic.Server;

public record FusionCommand(String address, short port) implements Command {

    @Override
    public void execute(Server server) {
        server.fusion(address, port);
    }
}
