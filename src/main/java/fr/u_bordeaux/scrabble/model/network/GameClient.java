package fr.u_bordeaux.scrabble.model.network;

import static fr.u_bordeaux.scrabble.model.network.NetworkManager.DEFAULT_ADDRESS;
import static fr.u_bordeaux.scrabble.model.network.NetworkManager.DEFAULT_TCP_PORT;

import fr.u_bordeaux.scrabble.model.core.Game;
import fr.u_bordeaux.scrabble.model.core.HumanPlayer;
import fr.u_bordeaux.scrabble.model.core.Tile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Network client to connect to a game server. */
public class GameClient {

  // Volatile because it can be read/write in the same time by two thread (main and
  // listenServerLoop)
  // Volatile flag used to maintain the loop active and allow a graceful shutdown of the thread.
  private volatile boolean isRunning = false;

  // Socket use to talk with the server
  private Socket socket;

  // Simplify receiving data from the server (get a string easily)
  private BufferedReader in;
  // Simplify sending data to the server (no need to use an array of bit)
  private PrintWriter out;

  // Variable needed for the PING command
  private long pingStartTime;

  // Need to keep the reference of the Thread running startHeartbeat,
  // because we have to stop it manually when quit() is called to avoid blocking for 30sec
  private Thread heartbeatThread;

  private Game localGame; // Local model for CLI/GUI

  private int myId; // My private ID on the server

  /**
   * Connect to a server on a specific address and port.
   *
   * @param address the address
   * @param port the port
   */
  public void connect(String address, int port) {
    try {
      // Try to connect to a server
      socket = new Socket(address, port);

      System.out.println("Client : connected to " + socket.getInetAddress().getHostName());

      // Set IO for ASCII communication
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new PrintWriter(socket.getOutputStream(), true);

      isRunning = true;

      // We use a Thread for listening to the server
      new Thread(this::listenServerLoop).start();

      // We use a Thread for sending a regular PING command to the server (to avoid disconnecting
      // with 60sec timeout)
      heartbeatThread = new Thread(this::startHeartbeat);
      heartbeatThread.start();

    } catch (IOException e) {
      System.err.println("Client Error: Could no connect to server " + e.getMessage());
      System.out.println("Client : Cant connect to the server");
    }
  }

  /** Connect to a server on the default address and port. */
  public void connect() {
    connect(DEFAULT_ADDRESS, DEFAULT_TCP_PORT);
  }

  // Infinite loop for listening to the server incoming messages
  // This method will only be call in a Thread
  // This loop print receive messages, but they will later be notified with observer for CLI/GUI
  private void listenServerLoop() {
    try {
      // Infinite loop for listening to the server
      String serverMessage;
      while (isRunning && (serverMessage = in.readLine()) != null) {
        Packet packet = new Packet(serverMessage);

        switch (packet.getCommand()) {
          case "WELCOME":
            // The server send us our ID when connecting for the first time
            if (!packet.getEntries().isEmpty()) {
              this.myId = Integer.parseInt(packet.getEntries().getFirst().get("ID"));
              System.out.println("Client : My ID is " + myId);
            }
            break;
          case "PONG":
            long pingEndTime = System.currentTimeMillis();
            System.out.println("Client : PONG TIME=" + (pingEndTime - pingStartTime) + "ms");
            break;

          case "SERVER_STATUS":
            if (packet.getEntries().isEmpty()) {
              break;
            }
            System.out.println("\n--- Remote Server Status ---");
            Map<String, String> info = packet.getEntries().getFirst();
            System.out.println("Port : " + info.get("PORT"));
            System.out.println("Clients connected : " + info.get("CLIENTS"));
            System.out.println("Games in progress : " + info.get("GAMES"));
            break;

          case "PLAYERS":
            System.out.println("\n--- Connected Players ---");
            for (Map<String, String> player : packet.getEntries()) {
              System.out.println(
                  "ID: "
                      + player.get("ID")
                      + " | Name: "
                      + player.get("NAME")
                      + " | Status: "
                      + player.get("STATUS"));
            }
            break;

          case "SCOREBOARD":
            System.out.println("\n--- Server Scoreboard ---");
            // Iterate through the scoreboard and display stats (F39)
            for (Map<String, String> stats : packet.getEntries()) {
              System.out.println(
                  stats.get("NAME")
                      + " -> Wins: "
                      + stats.get("WINS")
                      + " | Losses: "
                      + stats.get("LOSSES")
                      + " | Total: "
                      + stats.get("TOTAL"));
            }
            break;

          case "GAME_START":
            System.out.println("\n--- Game Started ---");
            localGame = new Game();

            for (Map<String, String> playerData : packet.getEntries()) {
              String name = playerData.get("NAME");
              this.localGame.addPlayer(new HumanPlayer(name));
            }
            break;

          case "SET_RACK":
            if (localGame != null && !packet.getEntries().isEmpty()) {
              String tilesStr = packet.getEntries().getFirst().get("TILES");
              if (tilesStr != null) {
                // We need our name in the model
                String myName = "Player-" + myId;

                // We build the list of Tile from the server message
                List<Tile> receivedTiles = new ArrayList<>();
                if (!tilesStr.trim().isEmpty()) {
                  for (String letter : tilesStr.split(",")) {
                    receivedTiles.add(new Tile(letter.charAt(0)));
                  }
                }

                // We update our local model with this Tile list
                //localGame.forceTilesToPlayer(myName, receivedTiles);
                System.out.println("Local rack updated: " + tilesStr);
              }
            }
            break;

          default:
            System.out.println("Client : Received: " + serverMessage);
            break;
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
      if (isRunning) {
        quit();
      }
    }
  }

  /** Close the connexion with the server. */
  public void quit() {
    if (!isRunning) {
      System.out.println("Client : This client is already disconnected");
      return;
    }
    isRunning = false;

    System.out.println("Client : connection closed");
    // Try closing the socket, if success will stop listenServerLoop() Thread in consequence
    try {
      if (socket != null && !socket.isClosed()) {
        socket.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Stop the heartbeat Thread
    if (heartbeatThread != null) {
      heartbeatThread.interrupt();
    }
  }

  /**
   * Send a message to the server. Use for all command
   *
   * @param message the message
   */
  public void sendMessage(String message) {
    if (isRunning && out != null) {
      out.println(message);
    } else {
      System.out.println("Client : Client is not running/connected");
    }
  }

  /** Send ping command to the server. */
  public void sendPing() {
    pingStartTime = System.currentTimeMillis();
    sendMessage("PING");
  }

  /** Send server status command to the server. */
  public void sendServerStatus() {
    sendMessage("SERVER_STATUS");
  }

  /** Send players command to the server. */
  public void sendPlayers() {
    sendMessage("PLAYERS");
  }

  /** Send scoreboard command to the server. */
  public void sendScoreboard() {
    sendMessage("SCOREBOARD");
  }

  /**
   * Send new PLAYER_ID command to the server.
   *
   * @param playerId the target id
   */
  public void sendNew(int playerId) {
    sendMessage("NEW_" + playerId);
  }

  // Method use in a Thread
  // Needed since the server timeout is 60sec, we ping it every 30sec to avoid disconnecting
  private void startHeartbeat() {
    try {
      while (isRunning) {
        Thread.sleep(30000);
        if (isRunning && !socket.isClosed()) {
          this.sendPing();
        }
      }
    } catch (InterruptedException e) {
      // Normal stop for this thread, call by stop()
      Thread.currentThread().interrupt();
    }
  }
}
