package fr.uge.teillardnajjar.chatfusion.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;

public class Application {
    public static void main(String[] args) throws IOException {
        if (args.length != 4) {
            System.out.println("Usage: java -jar chatfusionclient.jar <server_address> <server_port> <download_folder> <login>");
            System.exit(1);
        }

        new Client(
            new InetSocketAddress(args[0], Integer.parseInt(args[1])),
            Path.of(args[2]),
            args[3]
        ).launch();
    }
}
