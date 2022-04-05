package fr.uge.teillardnajjar.chafusion.server;

import fr.uge.teillardnajjar.chafusion.server.logic.Server;

import java.io.IOException;

public class Application {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: java -jar ChatFusion-server.jar <port> <name>");
            System.exit(1);
        }
        new Server(Integer.parseInt(args[0]), args[1]).launch();
    }
}
