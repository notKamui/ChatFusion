package fr.uge.teillardnajjar.chatfusion.server;

import fr.uge.teillardnajjar.chatfusion.server.logic.Server;

import java.io.IOException;

public class Application {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: java -jar ChatFusion-server.jar <port> <name>");
            System.exit(1);
        }
        if (args[1].length() != 5) {
            System.out.println("The server name must be exactly 5 ascii characters long");
            System.exit(1);
        }
        new Server(Integer.parseInt(args[0]), args[1]).launch();
    }
}
