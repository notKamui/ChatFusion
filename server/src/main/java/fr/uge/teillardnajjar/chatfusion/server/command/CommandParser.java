package fr.uge.teillardnajjar.chatfusion.server.command;

import java.util.Optional;

public class CommandParser {

    public static Optional<Command> parse(String command) {
        return switch (command.toUpperCase()) {
            case "TOGGLE DEBUG" -> Optional.of(new ToggleDebugCommand());
            case "SHUTDOWN" -> Optional.of(new Shutdown());
            case "SHUTDOWN NOW" -> Optional.of(new ShutdownNow());
            case "INFO" -> Optional.of(new InfoCommand());
            default -> {
                if (command.toUpperCase().startsWith("FUSION")) {
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
            port = Short.parseShort(parts[2]);
        } catch (NumberFormatException e) {
            return null;
        }
        return new FusionCommand(parts[1], port);
    }
}
