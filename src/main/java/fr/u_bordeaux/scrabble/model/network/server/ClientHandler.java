package fr.u_bordeaux.scrabble.model.network.server;

import fr.u_bordeaux.scrabble.model.network.PacketParser;
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

  // Useful player information
  private ClientInfo clientInfo;

  // Active game for this client
  // null if no current game
  private OnlineGame onlineGame;

  /**
   * Instantiates a new Client handler.
   *
   * @param socket the socket
   * @param server the server
   * @param playerId the player id
   */
  public ClientHandler(Socket socket, GameServer server, int playerId) {
    this.socket = socket;
    this.server = server;

    this.clientInfo = new ClientInfo(playerId);

    this.isRunning = true;
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

      sendMessage("WELCOME:ID=" + clientInfo.getId()); // Sending the server id of this client to it

      // Infinite loop for listening to the client
      String clientMessage;
      while (isRunning && (clientMessage = in.readLine()) != null) {
        // Check for command with parameters
        if (clientMessage.startsWith("NEW_")) {
          handleNewGameRequest(clientMessage);
        } else if (clientMessage.startsWith("MOVE:")) {
          handleMoveRequest(clientMessage);

        } else {
          // Fixed command with no parameters
          switch (clientMessage) {
            case "PING" -> sendMessage("PONG");
            case "PINGS" -> sendMessage("PONGS");
            case "SERVER_STATUS" -> sendMessage(server.getStatusResponse());
            case "PLAYERS" -> sendMessage(server.getPlayerResponse());
            case "SCOREBOARD" -> sendMessage(server.getScoreboardResponse());

            default -> System.err.println("Server : Received unknow command: " + clientMessage);
          }
        }
      }
    } catch (SocketTimeoutException e) {
      System.err.println("Server : Socket Timeout Exception");
      this.quit();
    } catch (SocketException e) {
      // If isRunning is false, it means we called stop(), so we just exit the loop
      if (isRunning) {
        System.err.println(
            "ClientHandler run() : Unintended socket Exception with message: " + e.getMessage());
      }
    } catch (IOException e) {
      System.err.println("ClientHandler run() : IOException with message: " + e.getMessage());
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
      System.err.println("Client is not connected");
    }
  }

  /** Close the connexion with the client. */
  public void quit() {
    if (!isRunning) {
      System.err.println("Client is already disconnected");
      return;
    }
    isRunning = false;

    if (onlineGame != null) {
      onlineGame.terminateGame(clientInfo.getName() + " disconnected");
    }

    // We need to remove this ClientHandler from the list of clients
    server.removeClient(this);

    try {
      if (socket != null && !socket.isClosed()) {
        socket.close();
      }
    } catch (IOException e) {
      System.err.println("ClientHandler quit() : IOException with message: " + e.getMessage());
    }
  }

  /**
   * Gets client info.
   *
   * @return the client info
   */
  public ClientInfo getClientInfo() {
    return clientInfo;
  }

  /**
   * Parses the 'new' command and requests game creation from the server.
   *
   * @param message the NEW command
   */
  private void handleNewGameRequest(String message) {
    try {
      // Extract the ID from "new ID"
      int targetId = Integer.parseInt(message.substring(4).trim());
      String response = server.createNewGame(this, targetId);

      // If there's an error, we notify the requester
      if (response.startsWith("ERROR")) {
        sendMessage(response);
      }
    } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
      sendMessage("ERROR: Invalid format. Use 'new PLAYER_ID'");
    }
  }

  private void handleMoveRequest(String message) {
    if (onlineGame == null) {
      sendMessage("ERROR: You are not currently in a game");
    } else {
      onlineGame.processMove(this, new PacketParser(message));
    }
  }

  /**
   * Sets online game.
   *
   * @param onlineGame the online game
   */
  public void setOnlineGame(OnlineGame onlineGame) {
    this.onlineGame = onlineGame;
  }

  /**
   * Gets server.
   *
   * @return the server
   */
  public GameServer getServer() {
    return server;
  }
}
