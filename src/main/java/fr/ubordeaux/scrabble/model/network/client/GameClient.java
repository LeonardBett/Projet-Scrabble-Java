package fr.ubordeaux.scrabble.model.network.client;

import static fr.ubordeaux.scrabble.model.network.NetworkManager.DEFAULT_ADDRESS;
import static fr.ubordeaux.scrabble.model.network.NetworkManager.DEFAULT_TCP_PORT;

import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.model.network.NetworkObserver;
import fr.ubordeaux.scrabble.model.network.PacketParser;
import fr.ubordeaux.scrabble.model.utils.GameLogger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Network client to connect to a game server. Manages the TCP connection, message parsing, and
 * updating the local game state. Implements the Observer pattern to notify the UI/CLI of network
 * events.
 */
public class GameClient {

  /** Default constructor for GameClient. */
  public GameClient() {}

  // List of observers
  private final List<NetworkObserver> observers = new ArrayList<>();

  // Volatile because it can be read/write in the same time by two thread (main
  // and
  // listenServerLoop)
  // Volatile flag used to maintain the loop active and allow a graceful shutdown
  // of the thread.
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
  // because we have to stop it manually when quit() is called to avoid blocking
  // for 30sec
  private Thread heartbeatThread;

  // Local model for CLI/GUI
  private Game localGame;

  // My private ID on the server
  private int myId;

  // =========================================================================
  // CLIENT LIFECYCLE METHODS

  /**
   * Connects to a game server at the specified IP address and TCP port. Initializes the socket, I/O
   * streams, and starts the listening and heartbeat threads.
   *
   * @param address the IP address or hostname of the server
   * @param port the TCP port of the server
   */
  public void connect(String address, int port) {
    try {
      // Check if the port is valid
      if (port < 0 || port > 65535) {
        throw new IllegalArgumentException("Port out of range: " + port);
      }

      // Try to connect to a server
      socket = new Socket(address, port);

      GameLogger.logVerbose("Client : connected to " + socket.getInetAddress().getHostName());

      // Set IO for ASCII communication
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new PrintWriter(socket.getOutputStream(), true);

      isRunning = true;

      // We use a Thread for listening to the server
      new Thread(this::listenServerLoop).start();

      // We use a Thread for sending a regular PING command to the server (to avoid
      // disconnecting
      // with 60sec timeout)
      heartbeatThread = new Thread(this::startHeartbeat);
      heartbeatThread.start();

    } catch (IOException e) {
      GameLogger.logError("Client Error: Could not connect to server ", e);
      for (NetworkObserver obs : new java.util.ArrayList<>(observers)) {
        obs.connectionFailedUpdate("err_connection_failed");
      }
    }
  }

  /** Connects to a server using the default address (localhost) and port (12345). */
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
              try {
                this.myId = Integer.parseInt(packetParser.getEntries().getFirst().get("ID"));
                GameLogger.logVerbose("Client : My ID is " + myId);
                for (NetworkObserver obs : observers) {
                  obs.serverWelcomeUpdate(myId);
                  obs.messageUpdate("info_connected");
                }
              } catch (NumberFormatException e) {
                GameLogger.logError("Client : Invalid ID format", e);
              }
            }
            break;

          case "PONG":
            long duration = System.currentTimeMillis() - pingStartTime;
            GameLogger.logVerbose("Client : PONG TIME=" + duration + "ms");
            for (NetworkObserver obs : observers) {
              obs.pongUpdate(duration);
            }
            break;

          case "PONGS":
            break;

          case "SERVER_STATUS":
            if (packetParser.getEntries().isEmpty()) {
              break;
            }
            Map<String, String> info = packetParser.getEntries().getFirst();
            GameLogger.logVerbose("\n--- Remote Server Status ---");
            GameLogger.logVerbose("Port : " + info.get("PORT"));
            GameLogger.logVerbose("Clients connected : " + info.get("CLIENTS"));
            GameLogger.logVerbose("Games in progress : " + info.get("GAMES"));
            for (NetworkObserver obs : observers) {
              obs.serverStatusUpdate(info);
            }
            break;

          case "PLAYERS":
            GameLogger.logVerbose("\n--- Connected Players ---");
            for (Map<String, String> player : packetParser.getEntries()) {
              GameLogger.logVerbose(
                  "ID: "
                      + player.get("ID")
                      + " | Name: "
                      + player.get("NAME")
                      + " | Status: "
                      + player.get("STATUS"));
            }

            List<Map<String, String>> playersEntries = packetParser.getEntries();
            if (playersEntries.isEmpty()) {
              break;
            }

            for (NetworkObserver obs : observers) {
              obs.playersUpdate(playersEntries);
            }
            break;

          case "SCOREBOARD":
            GameLogger.logVerbose("\n--- Server Scoreboard ---");
            // Iterate through the scoreboard and display stats (F39)
            for (Map<String, String> stats : packetParser.getEntries()) {
              GameLogger.logVerbose(
                  stats.get("NAME")
                      + " -> Wins: "
                      + stats.get("WINS")
                      + " | Losses: "
                      + stats.get("LOSSES")
                      + " | Total: "
                      + stats.get("TOTAL"));
            }
            for (NetworkObserver obs : observers) {
              obs.scoreboardUpdate(packetParser.getEntries());
            }
            break;

          case "GAME_START":
            if (packetParser.getEntries().isEmpty()) {
              break;
            }
            GameLogger.logVerbose("\n--- Game Started ---");

            // We create a local model, which will only be updated with server data
            localGame = new Game();

            // Extracting bag size and updating the local model
            try {
              int bagSize = Integer.parseInt(packetParser.getEntries().getFirst().get("BAG"));
              localGame.getBag().setOnlineSize(bagSize);
            } catch (NumberFormatException e) {
              GameLogger.logError("Client : Invalid bag size format", e);
            }

            // Extracting player info and adding them to the local model
            int playerIndex = 0;
            for (Map<String, String> playerData : packetParser.getEntries()) {
              String name = playerData.get("NAME");
              if (name != null) {
                this.localGame.addPlayer(new HumanPlayer(name, PlayerColor.fromIndex(playerIndex)));
                playerIndex++;
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
                // Find our player in the local model by matching ID position
                // The server sends SET_RACK only to the player it concerns,
                // so we identify ourselves by our position in the player list
                // (players are added in connection order, our ID = 1-based index)
                Player myPlayer = null;
                List<Player> playerList = localGame.getPlayers();
                for (Player pp : playerList) {
                  // Match by "Player-N" fallback or by finding the player whose
                  // index matches our ID
                  if (pp.getName().equals("Player-" + myId)) {
                    myPlayer = pp;
                    break;
                  }
                }
                // Fallback: use ID as 1-based index into the player list
                if (myPlayer == null && myId > 0 && myId <= playerList.size()) {
                  myPlayer = playerList.get(myId - 1);
                }

                if (myPlayer != null) {
                  List<Tile> receivedTiles = new ArrayList<>();
                  if (!tilesStr.trim().isEmpty()) {
                    for (String letter : tilesStr.split(",")) {
                      receivedTiles.add(new Tile(letter.charAt(0)));
                    }
                  }
                  localGame.forceTilesToPlayer(myPlayer.getName(), receivedTiles);
                }

                GameLogger.logVerbose("Local rack updated: " + tilesStr);

                for (NetworkObserver obs : observers) {
                  obs.localModelUpdate();
                }
              }
            }
            break;

          case "OPPONENT_MOVE":
            if (localGame == null || packetParser.getEntries().isEmpty()) {
              break;
            }

            Map<String, String> move = packetParser.getEntries().getFirst();
            String type = move.get("TYPE");

            // We extract and get a Player objet from the move
            String playerName = move.get("PLAYER");
            Player player = localGame.getPlayerFromName(playerName);
            if (player == null) {
              GameLogger.logError("Player " + playerName + " not found", null);
              break;
            }

            try {
              if ("PLAY".equals(type)) {
                // We extract and sync the new board to the local model
                String boardData = move.get("BOARD");
                if (boardData != null) {
                  localGame.syncBoard(boardData);
                }

                // We extract and sync new score to the local model
                int score = Integer.parseInt(move.get("SCORE"));
                player.setScore(score);

                // We extract and sync new bag size to the local model
                int bagSizes = Integer.parseInt(move.get("BAG"));
                localGame.getBag().setOnlineSize(bagSizes);
              } else if ("EXCHANGE".equals(type)) {
                int bagSizes = Integer.parseInt(move.get("BAG"));
                localGame.getBag().setOnlineSize(bagSizes);
              }
            } catch (NumberFormatException e) {
              GameLogger.logError("Client : Invalid move format", e);
              break;
            }

            // Change the turn of the local model
            localGame.nextTurn();

            // Debug: print the board client side if it was not our play move
            if (!(playerName.equals("Player-" + myId) && ("PLAY".equals(type)))) {
              if (GameLogger.isVerbose()) {
                localGame.printDebugState(false, true);
              }
            }

            for (NetworkObserver obs : observers) {
              obs.localModelUpdate();
            }

            break;

          case "INVITATION_RECEIVED":
            if (packetParser.getEntries().isEmpty()) {
              break;
            }
            String from = packetParser.getEntries().getFirst().get("FROM");
            for (NetworkObserver obs : observers) {
              obs.invitationReceivedUpdate(from);
            }
            break;

          case "INVITATION_ACCEPTED":
            if (packetParser.getEntries().isEmpty()) {
              break;
            }
            String playerAccepted = packetParser.getEntries().getFirst().get("PLAYER");
            for (NetworkObserver obs : observers) {
              obs.invitationAcceptedUpdate(playerAccepted);
            }
            break;

          case "INVITATION_DECLINED":
            if (packetParser.getEntries().isEmpty()) {
              break;
            }
            String playerDeclined = packetParser.getEntries().getFirst().get("PLAYER");
            for (NetworkObserver obs : observers) {
              obs.invitationDeclinedUpdate(playerDeclined);
            }
            break;

          case "PLAYERS_PLAYER_ID":
            List<Map<String, String>> playerEntries = packetParser.getEntries();
            if (playerEntries.isEmpty()) {
              break;
            }

            if (playerEntries.size() == 1 && playerEntries.getFirst().containsKey("WINS")) {
              Map<String, String> playerDetails = playerEntries.getFirst();
              for (NetworkObserver obs : observers) {
                obs.playersPlayerIdUpdate(playerDetails);
              }
            }
            break;

          case "STATUS_UPDATE":
            if (!packetParser.getEntries().isEmpty()) {
              String newStatus = packetParser.getEntries().getFirst().get("STATUS");
              for (NetworkObserver obs : observers) {
                obs.playerStatusUpdate(newStatus);
              }
            }
            break;

          case "INVITATION_CANCELLED":
            if (!packetParser.getEntries().isEmpty()) {
              String reason = packetParser.getEntries().getFirst().get("REASON");
              for (NetworkObserver obs : observers) {
                obs.invitationCancelledUpdate(reason);
              }
            }
            break;

          case "GAME_INTERRUPTED":
            if (packetParser.getEntries().isEmpty()) {
              break;
            }
            String reason = packetParser.getEntries().getFirst().get("REASON");
            for (NetworkObserver obs : new ArrayList<>(observers)) {
              obs.gameInterruptedUpdate(reason);
            }
            break;

          case "GAME_ENDED":
            List<Map<String, String>> finalScore = packetParser.getEntries();
            for (NetworkObserver obs : new ArrayList<>(observers)) {
              obs.gameEndedUpdate(finalScore);
            }
            break;

          case "INVITATION_FAILED":
            if (packetParser.getEntries().isEmpty()) {
              break;
            }
            String invitationFailedReason = packetParser.getEntries().getFirst().get("REASON");
            for (NetworkObserver obs : observers) {
              obs.invitationFailedUpdate(invitationFailedReason);
            }
            break;

          case "MOVE_ERROR":
            if (!packetParser.getEntries().isEmpty()) {
              String errorKey = packetParser.getEntries().getFirst().get("REASON");

              if (errorKey != null) {
                for (NetworkObserver obs : observers) {
                  obs.moveRefusedUpdate(errorKey);
                }
              }
            }
            break;

          case "ERROR":
            if (!packetParser.getEntries().isEmpty()) {
              // We get the error code for translating it later in GUI/CLI
              String errorKey = packetParser.getEntries().getFirst().get("REASON");

              if (errorKey != null) {
                for (NetworkObserver obs : observers) {
                  obs.messageUpdate(errorKey);
                }
              }
            }
            break;

          default:
            GameLogger.logVerbose("Client : Received: " + serverMessage);
            for (NetworkObserver obs : observers) {
              obs.messageUpdate(serverMessage);
            }
            break;
        }
      }
    } catch (SocketException e) {
      // Socket closed, normal behavior if raised when called close()
      if (isRunning) {
        GameLogger.logError("Client Error: Socket error while listening to server ", e);
      }
    } catch (IOException e) {
      GameLogger.logError("Client Error: IO Error while listening to server ", e);
    } finally {
      // If the exception was not intended, we stop the connexion
      if (isRunning) {
        quit();
      }
    }
  }

  /**
   * Closes the connection with the server and stops all background threads. If the client is
   * already disconnected, this method does nothing.
   */
  public void quit() {
    if (!isRunning) {
      GameLogger.logError("Client : This client is already disconnected", null);
      return;
    }
    isRunning = false;

    GameLogger.logVerbose("Client : connection closed");
    // Try closing the socket, if success will stop listenServerLoop() Thread in
    // consequence
    try {
      if (socket != null && !socket.isClosed()) {
        socket.close();
      }
    } catch (IOException e) {
      GameLogger.logError("Client Error: Could not close socket ", e);
    }

    // Stop the heartbeat Thread
    if (heartbeatThread != null) {
      heartbeatThread.interrupt();
    }

    List<NetworkObserver> observersCopy = new ArrayList<>(observers);
    for (NetworkObserver obs : observersCopy) {
      obs.clientDisconnectedUpdate("info_connection_closed");
    }
  }

  // =========================================================================
  // COMMANDS METHODS

  /**
   * Sends a raw string message to the server. Use specific methods like sendPing() or
   * sendPlayMove() instead when possible.
   *
   * @param message the raw string message to send
   */
  public void sendMessage(String message) {
    if (isRunning && out != null) {
      out.println(message);
    } else {
      GameLogger.logError("Client : Client is not running/connected", null);
      return;
    }
  }

  /**
   * Sends a PING command to the server to test latency. The response time will be calculated when
   * the PONG is received.
   */
  public void sendPing() {
    pingStartTime = System.currentTimeMillis();
    sendMessage("PING");
  }

  /**
   * Sends a silent PING command (PINGS) to the server. Used internally by the heartbeat thread to
   * prevent connection timeouts.
   */
  public void sendPingSilent() {
    sendMessage("PINGS");
  }

  /** Requests the server status (port, connected clients, games in progress). */
  public void sendServerStatus() {
    sendMessage("SERVER_STATUS");
  }

  /** Requests the list of connected players and their statuses. */
  public void sendPlayers() {
    sendMessage("PLAYERS");
  }

  /** Requests the global scoreboard from the server. */
  public void sendScoreboard() {
    sendMessage("SCOREBOARD");
  }

  /**
   * Sends an invitation to start a new game with a specific player.
   *
   * @param playerId the ID of the target player to invite
   */
  public void sendNew(int playerId) {
    String message = String.format("NEW:PLAYER1=%d", playerId);
    sendMessage(message);
  }

  /**
   * Sends an invitation to start a new game with two specific players.
   *
   * @param playerId1 the ID of the first target player
   * @param playerId2 the ID of the second target player
   */
  public void sendNew(int playerId1, int playerId2) {
    String message = String.format("NEW:PLAYER1=%d;PLAYER2=%d", playerId1, playerId2);
    sendMessage(message);
  }

  /**
   * Sends an invitation to start a new game with three specific players.
   *
   * @param playerId1 the ID of the first target player
   * @param playerId2 the ID of the second target player
   * @param playerId3 the ID of the third target player
   */
  public void sendNew(int playerId1, int playerId2, int playerId3) {
    String message =
        String.format("NEW:PLAYER1=%d;PLAYER2=%d;PLAYER3=%d", playerId1, playerId2, playerId3);
    sendMessage(message);
  }

  /**
   * Sends a PLAY move to the server.
   *
   * @param x the X coordinate on the board
   * @param y the Y coordinate on the board
   * @param direction the direction of the word (e.g., "H" or "V")
   * @param tile the letters forming the word being played
   */
  public void sendPlayMove(int x, int y, String direction, String tile) {
    // Format: MOVE:TYPE=PLAY;X=7;Y=7;DIR=H;WORD=CHAT
    String message =
        String.format("MOVE:TYPE=PLAY;X=%d;Y=%d;DIR=%s;TILES=%s", x, y, direction, tile);
    sendMessage(message);
  }

  /**
   * Sends an EXCHANGE move to the server.
   *
   * @param tiles a comma-separated string of tiles to exchange (e.g., "A,B,C")
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

  /** Accepts a pending game invitation. */
  public void sendAccept() {
    sendMessage("ACCEPT");
  }

  /** Declines a pending game invitation. */
  public void sendDecline() {
    sendMessage("DECLINE");
  }

  /**
   * Requests detailed information and statistics for a specific player.
   *
   * @param playerId the ID of the player to inspect
   */
  public void sendPlayersPlayerId(int playerId) {
    sendMessage("PLAYERS_PLAYER_ID:PLAYER=" + playerId);
  }

  /**
   * Changes the player's status on the server to AWAY. Players who are AWAY cannot receive game
   * invitations.
   */
  public void sendAway() {
    sendMessage("AWAY");
  }

  /** Changes the player's status on the server back to IDLE. */
  public void sendBack() {
    sendMessage("BACK");
  }

  /** Cancels an invitation that was previously sent by this client. */
  public void sendCancel() {
    sendMessage("CANCEL");
  }

  // =========================================================================
  // THREAD METHODS

  // Method use in a Thread
  // Needed since the server timeout is 60sec, we ping it every 30sec to avoid
  // disconnecting
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

  // =========================================================================
  // OBSERVER METHODS

  /**
   * Adds an observer to listen for network events.
   *
   * @param observer the observer to add
   */
  public void addObserver(NetworkObserver observer) {
    observers.add(observer);
  }

  /**
   * Removes an observer from the list of listeners.
   *
   * @param observer the observer to remove
   */
  public void removeObserver(NetworkObserver observer) {
    if (!observers.remove(observer)) {
      GameLogger.logError("User : Observer not found, can't remove it from the list", null);
      return;
    }
  }

  // =========================================================================
  // GETTER

  /**
   * Gets the local representation of the game model. This model is synchronized with the server
   * state.
   *
   * @return the local Game instance
   */
  public Game getLocalGame() {
    return localGame;
  }

  /**
   * Get this client Id, given by the server welcome message.
   *
   * @return the local client Id
   */
  public int getMyId() {
    return myId;
  }
}
