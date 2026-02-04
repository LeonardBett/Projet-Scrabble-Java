package fr.u_bordeaux.scrabble.model.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Serveur de jeu pour le mode multijoueur.
 * Gère les connexions clients et les parties en réseau.
 */
public class GameServer {

    private static final int DEFAULT_PORT = 12345;
    private volatile boolean isRunning = false;

    // Server socket use to accept connexion, need to store it to be able to close it
    private ServerSocket serverSocket;

    // Start a server on the default port
    public void start() {
        start(DEFAULT_PORT);
    }

    // Start a server on the specified port
    public void start(int port) {
        System.out.println("Server starting...");
        isRunning = true;
        try {
            serverSocket = new ServerSocket(port);

            //Infinite loop for accepting connexion
            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected: " + clientSocket.getInetAddress());

                    //We are going to give this socket to a thread
                    ClientHandler handler = new ClientHandler(clientSocket);
                    new Thread(handler).start();
                } catch (SocketException e) {
                    if (isRunning) {
                        e.printStackTrace();
                    }
                    // If isRunning is false, it means we called stop(), so we just exit the loop
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Only call stop if it's still running to avoid double call message
            if (isRunning) {
                stop();
            }
        }
    }

    // Stop the server
    public void stop(){
        if (!isRunning){
            System.out.println("Server is not running, can't stop it");
            return;
        };
        
        System.out.println("Server stopping...");
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}