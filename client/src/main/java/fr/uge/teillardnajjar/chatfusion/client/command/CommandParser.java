package fr.uge.teillardnajjar.chatfusion.client.command;

import fr.uge.teillardnajjar.chatfusion.core.command.Command;
import fr.uge.teillardnajjar.chatfusion.core.util.Triple;

import java.util.Optional;

public class CommandParser {

    public static Optional<Command> parse(String command) {
        return switch (command.charAt(0)) {
            case ':' -> {
                if (!command.equals(":quit")) yield Optional.empty();
                yield Optional.of(new QuitCommand());
            }
            case '@' -> {
                var members = parseMembers(command);
                if (members != null) yield Optional.empty();
                yield Optional.of(new PrivateMessageCommand(
                    members.first(),
                    members.second(),
                    members.third()
                ));
            }
            case '/' -> {
                var members = parseMembers(command);
                if (members != null) yield Optional.empty();
                yield Optional.of(new PrivateFileCommand(
                    members.first(),
                    members.second(),
                    members.third()
                ));
            }
            default -> Optional.of(new PublicMessageCommand(command));
        };
    }

    private static Triple<String, String, String> parseMembers(String command) {
        var posColon = command.indexOf(':');
        var posSpace = command.indexOf(' ');
        if (posColon == -1 || posSpace == -1) return null;
        var username = command.substring(1, posColon);
        var servername = command.substring(posColon + 1, posSpace);
        if (servername.length() != 5) return null;
        var message = command.substring(posSpace + 1);
        return Triple.of(username, servername, message);
    }
}
