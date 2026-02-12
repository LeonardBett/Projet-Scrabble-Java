package fr.u_bordeaux.scrabble.model.network;

import static fr.u_bordeaux.scrabble.model.network.NetworkManager.DEFAULT_TCP_PORT;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Game server for multiplayer mode. Manages client connections and network games. */
public class GameServer {

  // Volatile flag used to maintain the loop active and allow a graceful shutdown of the thread.
  private volatile boolean isRunning = false;

  // Thread-safe list of running clients
  private final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

  // Server socket use to accept connexion, need to store it to be able to close it
  private ServerSocket serverSocket;

  /** Start the server on the default port. */
  public void start() {
    start(DEFAULT_TCP_PORT);
  }

  /**
   * Start a server on the specified port.
   *
   * @param port the port
   */
  public void start(int port) {
    System.out.println("Server : Server Starting...");
    isRunning = true;
    try {
      serverSocket = new ServerSocket(port);

      // Infinite loop for accepting connexion
      while (isRunning) {
        Socket clientSocket = serverSocket.accept();
        System.out.println("Server : Client connected: " + clientSocket.getInetAddress());

        // We are going to give this socket to a thread
        ClientHandler handler = new ClientHandler(clientSocket, this);
        addClient(handler);
        new Thread(handler).start();
      }
    } catch (SocketException e) {
      if (isRunning) {
        e.printStackTrace();
      }
      // If isRunning is false, it means we called stop(), so we just exit the loop
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      // Only call stop if it's still running to avoid double call message
      if (isRunning) {
        stop();
      }
    }
  }

  /** Stop the server. */
  public void stop() {
    if (!isRunning) {
      System.out.println("Server : Server is not running, can't stop it");
      return;
    }
    isRunning = false;

    System.out.println("Server : Server stopping, disconnecting all clients...");

    // Create a copy of the list to iterate over, to avoid ConcurrentModificationException
    // because client.quit() calls removeClient() which modifies the original list
    List<ClientHandler> clientsCopy;
    synchronized (clients) {
      clientsCopy = new ArrayList<>(clients);
    }
    for (ClientHandler client : clientsCopy) {
      client.sendMessage("Server is shutting down");
      client.quit();
    }

    try {
      if (serverSocket != null && !serverSocket.isClosed()) {
        serverSocket.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void addClient(ClientHandler client) {
    clients.add(client);
    System.out.println("Server : There is now " + clients.size() + " client(s) connected");
  }

  /**
   * Remove client.
   *
   * @param client the client
   */
  public void removeClient(ClientHandler client) {
    clients.remove(client);
    System.out.println("Server : There is now " + clients.size() + " client(s) connected");
  }
}
