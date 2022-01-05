package ga.epicpix.javadebugger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Start {

    public static void main(String[] args) throws IOException {
        if(args.length >= 1) {
            String host = args[0];

            String address = host.split(":")[0];
            int port = Integer.parseInt(host.split(":", 2)[1]);

            Socket socket = new Socket(address, port);
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            DataInputStream input = new DataInputStream(socket.getInputStream());
            Debugger debugger = new Debugger(output, input);

            if(debugger.PerformHandshake()) {
                System.out.println("Handshake succeeded");
                Scanner scanner = new Scanner(System.in);
                while(true) {
                    if(scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        String[] split = line.split(" ");
                        if(split[0].equals("caps") || split[0].equals("capabilities")) {
                            System.out.println("Capabilities:");
                            debugger.Capabilities().Print();
                        }if(split[0].equals("version") || split[0].equals("ver")) {
                            debugger.Version().Print();
                        }else if(split[0].equals("quit") || split[0].equals("q")) {
                            System.exit(0);
                        }else if(split[0].equals("kill")) {
                            int code = split.length >= 2 ? Integer.parseInt(split[1]) : 0;
                            debugger.Exit(code);
                            return;
                        }else {
                            System.out.println("Unknown command");
                        }
                    }
                }
            }else throw new RuntimeException("Handshake failed");
        }else {
            throw new IllegalArgumentException("Missing required argument 'host'");
        }
    }

}
