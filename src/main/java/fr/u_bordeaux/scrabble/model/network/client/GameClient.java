package fr.u_bordeaux.scrabble.model.network.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.u_bordeaux.scrabble.model.core.Game;
import fr.u_bordeaux.scrabble.model.core.HumanPlayer;
import fr.u_bordeaux.scrabble.model.core.Tile;
import fr.u_bordeaux.scrabble.model.interfaces.Player;
import static fr.u_bordeaux.scrabble.model.network.NetworkManager.DEFAULT_ADDRESS;
import static fr.u_bordeaux.scrabble.model.network.NetworkManager.DEFAULT_TCP_PORT;
import fr.u_bordeaux.scrabble.model.network.NetworkObserver;
import fr.u_bordeaux.scrabble.model.network.PacketParser;

/** Network client to connect to a game server. */
public class GameClient {

  // List of observers
  private final List<NetworkObserver> observers = new ArrayList<>();

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

  // Local model for CLI/GUI
  private Game localGame;

  // My private ID on the server
  private int myId;

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

      // System.out.println("Client : connected to " + socket.getInetAddress().getHostName());

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
    }
  }

  /** Connect to a server on the default address and port. */
  public void connect() {
    connect(DEFAULT_ADDRESS, DEFAULT_TCP_PORT);
  }

  // Infinite loop for listening to the server incoming messages
  // This method will only be call in a Thread
  // This loop will later notify with observer for CLI/GUI
  private void listenServerLoop() {
    try {
      // Infinite loop for listening to the server
      String serverMessage;
      while (isRunning && (serverMessage = in.readLine()) != null) {
        PacketParser packetParser = new PacketParser(serverMessage);

        switch (packetParser.getCommand()) {
          case "WELCOME":
            // The server send us our ID when connecting for the first time
            if (!packetParser.getEntries().isEmpty()) {
              this.myId = Integer.parseInt(packetParser.getEntries().getFirst().get("ID"));
              // System.out.println("Client : My ID is " + myId);
              for (NetworkObserver obs : observers) {
                obs.messageUpdate("Client : Connected to server, my ID on it is " + myId);
              }
            }
            break;

          case "PONG":
            long pingEndTime = System.currentTimeMillis();
            // System.out.println("Client : PONG TIME=" + (pingEndTime - pingStartTime) + "ms");
            for (NetworkObserver obs : observers) {
              obs.messageUpdate("Client : PONG TIME=" + (pingEndTime - pingStartTime) + "ms");
            }
            break;

          case "PONGS":
            break;

          case "SERVER_STATUS":
            if (packetParser.getEntries().isEmpty()) {
              break;
            }
            Map<String, String> info = packetParser.getEntries().getFirst();
            // System.out.println("\n--- Remote Server Status ---");
            // System.out.println("Port : " + info.get("PORT"));
            // System.out.println("Clients connected : " + info.get("CLIENTS"));
            // System.out.println("Games in progress : " + info.get("GAMES"));
            for (NetworkObserver obs : observers) {
              obs.serverStatusUpdate(info);
            }
            break;

          case "PLAYERS":
            //            System.out.println("\n--- Connected Players ---");
            //            for (Map<String, String> player : packetParser.getEntries()) {
            //              System.out.println(
            //                  "ID: "
            //                      + player.get("ID")
            //                      + " | Name: "
            //                      + player.get("NAME")
            //                      + " | Status: "
            //                      + player.get("STATUS"));
            //            }

            for (NetworkObserver obs : observers) {
              obs.playersUpdate(packetParser.getEntries());
            }
            break;

          case "SCOREBOARD":
            // System.out.println("\n--- Server Scoreboard ---");
            // Iterate through the scoreboard and display stats (F39)
            //            for (Map<String, String> stats : packetParser.getEntries()) {
            //              System.out.println(
            //                  stats.get("NAME")
            //                      + " -> Wins: "
            //                      + stats.get("WINS")
            //                      + " | Losses: "
            //                      + stats.get("LOSSES")
            //                      + " | Total: "
            //                      + stats.get("TOTAL"));
            //            }
            for (NetworkObserver obs : observers) {
              obs.scoreboardUpdate(packetParser.getEntries());
            }
            break;

          case "GAME_START":
            // System.out.println("\n--- Game Started ---");
            // We create a local model, which will only be updated with server data
            localGame = new Game();

            // Extracting bag size and updating the local model
            int bagSize = Integer.parseInt(packetParser.getEntries().getFirst().get("BAG"));
            localGame.getBag().setOnlineSize(bagSize);

            // Extracting player info and adding them to the local model
            for (Map<String, String> playerData : packetParser.getEntries()) {
              String name = playerData.get("NAME");
              if (name != null) {
                this.localGame.addPlayer(new HumanPlayer(name));
              }
            }

            for (NetworkObserver obs : observers) {
              obs.localModelUpdate();
            }

            break;

          case "SET_RACK":
            if (localGame != null && !packetParser.getEntries().isEmpty()) {
              String tilesStr = packetParser.getEntries().getFirst().get("TILES");
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
                localGame.forceTilesToPlayer(myName, receivedTiles);
                // System.out.println("Local rack updated: " + tilesStr);
                // localGame.printDebugState(false, true);

                for (NetworkObserver obs : observers) {
                  obs.localModelUpdate();
                }
              }
            }
            break;

          case "OPPONENT_MOVE":
            Map<String, String> move = packetParser.getEntries().getFirst();
            String type = move.get("TYPE");

            // We extract and get a Player objet from the move
            String playerName = move.get("PLAYER");
            Player player = localGame.getPlayerFromName(playerName);
            if (player == null) {
              System.err.println("Player " + playerName + " not found");
              break;
            }

            if ("PLAY".equals(type)) {
              // We extract and sync the new board to the local model
              String boardData = move.get("BOARD");
              if (boardData != null) {
                localGame.syncBoard(boardData);
              }

              // We extract and sync new score to the local model
              int score = Integer.parseInt(move.get("SCORE"));
              player.addScore(score);

              // We extract and sync new bag size to the local model
              int bagSizes = Integer.parseInt(move.get("BAG"));
              localGame.getBag().setOnlineSize(bagSizes);
            }

            // Change the turn of the local model
            localGame.nextTurn();

            // Debug: print the board client side if it was not our play move
            // if (!(playerName.equals("Player-" + myId) && ("PLAY".equals(type)))) {
            //  localGame.printDebugState(false, true);
            // }

            for (NetworkObserver obs : observers) {
              obs.localModelUpdate();
            }

            break;

          default:
            // System.out.println("Client : Received: " + serverMessage);
            for (NetworkObserver obs : observers) {
              obs.messageUpdate(serverMessage);
            }
            break;
        }
      }
    } catch (SocketException e) {
      // Socket closed, normal behavior if raised when called close()
      if (isRunning) {
        System.err.println(
            "Client Error: Socket error while listening to server " + e.getMessage());
      }
    } catch (IOException e) {
      System.err.println("Client Error: IO Error while listening to server " + e.getMessage());
    } finally {
      // If the exception was not intended, we stop the connexion
      if (isRunning) {
        quit();
      }
    }
  }

  /** Close the connexion with the server. */
  public void quit() {
    if (!isRunning) {
      System.err.println("Client : This client is already disconnected");
      return;
    }
    isRunning = false;

    // System.out.println("Client : connection closed");
    // Try closing the socket, if success will stop listenServerLoop() Thread in consequence
    try {
      if (socket != null && !socket.isClosed()) {
        socket.close();
      }
    } catch (IOException e) {
      System.err.println("Client Error: Could not close socket " + e.getMessage());
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
      System.err.println("Client : Client is not running/connected");
    }
  }

  /** Send ping command to the server. */
  public void sendPing() {
    pingStartTime = System.currentTimeMillis();
    sendMessage("PING");
  }

  /** Send ping command to the server only for timeout management. */
  public void sendPingSilent() {
    sendMessage("PINGS");
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

  /**
   * Send PLAY move command to the server.
   *
   * @param x the x coordinate on the board
   * @param y the y coordinate on the board
   * @param direction the direction of the move
   * @param tile the word to play
   */
  public void sendPlayMove(int x, int y, String direction, String tile) {
    // Format: MOVE:TYPE=PLAY;X=7;Y=7;DIR=H;WORD=CHAT
    String message =
        String.format("MOVE:TYPE=PLAY;X=%d;Y=%d;DIR=%s;TILES=%s", x, y, direction, tile);
    sendMessage(message);
  }

  /**
   * Send EXCHANGE move command to the server.
   *
   * @param tiles the tiles to exchange (ex: "A,B,C")
   */
  public void sendExchangeMove(String tiles) {
    // Format: MOVE:TYPE=EXCHANGE;TILES=A,B,C
    String message = String.format("MOVE:TYPE=EXCHANGE;TILES=%s", tiles);
    sendMessage(message);
  }

  /** Send PASS move command to the server. */
  public void sendPassMove() {
    // Format: MOVE:TYPE=PASS
    sendMessage("MOVE:TYPE=PASS");
  }

  // Method use in a Thread
  // Needed since the server timeout is 60sec, we ping it every 30sec to avoid disconnecting
  private void startHeartbeat() {
    try {
      while (isRunning) {
        Thread.sleep(30000);
        if (isRunning && !socket.isClosed()) {
          this.sendPingSilent();
        }
      }
    } catch (InterruptedException e) {
      // Normal stop for this thread, call by stop()
      Thread.currentThread().interrupt();
    }
  }

    /**
   * Returns the local game model synchronized with the server.
   * Used by the GUI to display the online game state.
   *
   * @return the local game, or null if no game is in progress
   */
  public Game getLocalGame() {
    return localGame;
  }

  /**
   * Returns this client's ID on the server.
   *
   * @return the player ID
   */
  public int getMyId() {
    return myId;
  }

  /**
   * Add an observer to the list.
   *
   * @param observer the new observer to add
   */
  public void addObserver(NetworkObserver observer) {
    observers.add(observer);
  }

  /**
   * Remove an observer to the list.
   *
   * @param observer the new observer to remove
   */
  public void removeObserver(NetworkObserver observer) {
    if (!observers.remove(observer)) {
      System.err.println("User : Observer not found, can't remove it from the list");
    }
  }
}
