package fr.u_bordeaux.scrabble.model.network;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    // Socket use to talk with the client
    private Socket socket;

    // Simplify receiving data from the client (get a string easily)
    private BufferedReader in;
    // Simplify sending data to the client (no need to use an array of bit)
    private PrintWriter out;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // Set IO for ASCII communication
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Infinite loop for listening to the client
            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                System.out.println("Server received: " + clientMessage);

                // PING implementation
                if (clientMessage.equals("PING")) {
                    out.println("PONG");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}