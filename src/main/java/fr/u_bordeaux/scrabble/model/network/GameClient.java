package fr.u_bordeaux.scrabble.model.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

/**
 * Client réseau pour se connecter à un serveur de jeu.
 */
public class GameClient {

    //default address and port
    private static final String DEFAULT_ADDRESS = "localhost";
    private static final int DEFAULT_PORT = 12345;

    // Socket use to talk with the server
    private Socket socket;

    // Simplify receiving data from the server (get a string easily)
    private BufferedReader in;
    // Simplify sending data to the server (no need to use an array of bit)
    private PrintWriter out;

    // Volatile because it can be read/write in the same time by two thread (main and listenServerLoop)
    private volatile boolean isRunning = false;

    // Variable needed for the PING command
    private long pingStartTime;

    // Connect to a server on the default address and port
    public void connect() {
        connect(DEFAULT_ADDRESS, DEFAULT_PORT);
    }

    // Connect to a server on a specific address and port
    public void connect(String address, int port){
        try {
            // Try to connect to a server
            socket = new Socket(address, port);

            System.out.println("Connected to " + socket.getInetAddress().getHostName());

            // Set IO for ASCII communication
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            isRunning = true;

            // We use a Thread for listening to the server
            new Thread(this::listenServerLoop).start();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Cant connect to the server");
        }
    }

    //Infinite loop for listening to the server incoming messages
    private void listenServerLoop() {
        try {
            // Infinite loop for listening to the server
            String serverMessage;
            while (isRunning && (serverMessage = in.readLine()) != null) {
                if ("PONG".equals(serverMessage)) {
                    long pingEndTime = System.currentTimeMillis();
                    System.out.println("PONG TIME=" + (pingEndTime - pingStartTime) + "ms");
                } else {
                    System.out.println(serverMessage);
                }
            }
        } catch (SocketException e) {
            // Socket closed, normal behavior if we called close()
            if (isRunning) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            quit();
        }
    }

    //Close the connexion with the server
    public void quit() {
        isRunning = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("Connection closed");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Send a message to the server (temporary methode)
    public void sendMessage(String message) {
        if (isRunning && out != null) {
            out.println(message);
        } else {
            System.out.println("Client is not running/connected");
        }
    }

    // Send a PING command to the server
    public void sendPing() {
        if (isRunning && out != null) {
            pingStartTime = System.currentTimeMillis();
            out.println("PING");
        } else {
            System.out.println("Client is not running/connected");
        }
    }
}