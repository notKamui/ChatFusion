package fr.uge.teillardnajjar.chatfusion.server.command;

import java.util.Optional;

public class CommandParser {

    public static Optional<Command> parse(String command) {
        return switch (command) {
            case "SHUTDOWN" -> Optional.of(new Shutdown());
            case "SHUTDOWNNOW" -> Optional.of(new ShutdownNow());
            default -> {
                if (command.startsWith("FUSION")) {
                    yield Optional.ofNullable(parseFusionCommand(command));
                } else {
                    yield Optional.empty();
                }
            }
        };
    }

    private static FusionCommand parseFusionCommand(String command) {
        var parts = command.split(" ");
        if (parts.length != 3) return null;
        short port;
        try {
            port = Short.parseShort(parts[1]);
        } catch (NumberFormatException e) {
            return null;
        }
        return new FusionCommand(parts[1], port);
    }
}
