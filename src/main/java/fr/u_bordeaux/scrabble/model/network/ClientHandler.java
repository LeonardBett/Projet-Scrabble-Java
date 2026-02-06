package fr.u_bordeaux.scrabble.model.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/** Handles communication with a single connected client. Runs in its own thread. */
public class ClientHandler implements Runnable {

  // Volatile flag used to maintain the loop active and allow a graceful shutdown of the thread.
  private volatile boolean isRunning = false;

  // Socket use to talk with the client
  private final Socket socket;
  // Reference to the main server
  private final GameServer server;

  // Simplify receiving data from the client (get a string easily)
  private BufferedReader in;
  // Simplify sending data to the client (no need to use an array of bit)
  private PrintWriter out;

  /**
   * Instantiates a new Client handler.
   *
   * @param socket the socket
   * @param server the server
   */
  public ClientHandler(Socket socket, GameServer server) {
    this.socket = socket;
    this.server = server;
    isRunning = true;
  }

  // Needed since this class will be called in a Thread
  @Override
  public void run() {
    try {
      // Set the timeout for the socket to 60 seconds, so the client will
      // have to ping the server regularly to avoid disconnecting
      socket.setSoTimeout(60000);

      // Set IO for ASCII communication
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new PrintWriter(socket.getOutputStream(), true);

      // Infinite loop for listening to the client
      String clientMessage;
      while (isRunning && (clientMessage = in.readLine()) != null) {
        System.out.println("Server : Received: " + clientMessage);

        // PING implementation
        if (clientMessage.equals("PING")) {
          sendMessage("PONG");
        }
      }
    } catch (SocketTimeoutException e) {
      System.out.println("Server : Socket Timeout Exception");
      this.quit();
    } catch (SocketException e) {
      if (isRunning) {
        e.printStackTrace();
      }
      // If isRunning is false, it means we called stop(), so we just exit the loop
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (isRunning) {
        this.quit();
      }
    }
  }

  /**
   * Send a message to the client.
   *
   * @param message the message
   */
  public void sendMessage(String message) {
    if (out != null) {
      out.println(message);
    } else {
      System.out.println("Client is not connected");
    }
  }

  /** Close the connexion with the client. */
  public void quit() {
    if (!isRunning) {
      System.out.println("Client is already disconnected");
      return;
    }
    isRunning = false;

    // We need to remove this ClientHandler from the list of clients
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
