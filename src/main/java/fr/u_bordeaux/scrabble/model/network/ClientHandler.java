package fr.u_bordeaux.scrabble.model.network;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * Handles communication with a single connected client.
 * Runs in its own thread.
 */
public class ClientHandler implements Runnable {

    private volatile  boolean isRunning = false;

    // Socket use to talk with the client
    private Socket socket;
    // Reference to the main server
    private GameServer server;

    // Simplify receiving data from the client (get a string easily)
    private BufferedReader in;
    // Simplify sending data to the client (no need to use an array of bit)
    private PrintWriter out;

    public ClientHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;

        isRunning = true;
    }

    @Override
    public void run() {
        try {
            // Set IO for ASCII communication
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Infinite loop for listening to the client
            String clientMessage;
            while (isRunning && (clientMessage = in.readLine()) != null) {
                System.out.println("Server received: " + clientMessage);

                // PING implementation
                if (clientMessage.equals("PING")) {
                    sendMessage("PONG");
                }
            }
        } catch (SocketException e) {
            if (isRunning) {
                e.printStackTrace();
            }
            // If isRunning is false, it means we called stop(), so we just exit the loop
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(isRunning){
                this.quit();
            }
        }
    }

    public void sendMessage(String message){
        if(out != null){
            out.println(message);
        } else {
            System.out.println("Client is not connected");
        }
    }

    public void quit(){
        if(!isRunning){
            System.out.println("Client is already disconnected");
            return;
        }
        isRunning = false;

        server.removeClient(this);

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}