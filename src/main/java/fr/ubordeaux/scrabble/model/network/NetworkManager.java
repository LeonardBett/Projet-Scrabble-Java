package fr.ubordeaux.scrabble.model.network;

import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.network.client.GameClient;
import fr.ubordeaux.scrabble.model.network.server.GameServer;
import fr.ubordeaux.scrabble.model.network.server.ServerInfo;
import fr.ubordeaux.scrabble.model.utils.GameLogger;
import java.util.ArrayList;
import java.util.List;

/** Manages network operations and acts as a facade for the network layer. */
public class NetworkManager {

  // List of observers for the network manager
  // Since the gameClient instance will be deleted when we stop online play,
  // we have to keep here the real list of Observer (CLI/GUI)
  private final List<NetworkObserver> observers = new ArrayList<>();

  /** The constant DEFAULT_TCP_PORT. */
  // Default values use in the package
  public static final int DEFAULT_TCP_PORT = 12345;

  /** The constant DEFAULT_UDP_PORT. */
  public static final int DEFAULT_UDP_PORT = 12346;

  /** The constant DEFAULT_ADDRESS. */
  public static final String DEFAULT_ADDRESS = "localhost";

  // Reference to server/client instances
  // Reinstantiated on each start/join to ensure a clean state (fresh sockets and new threads, no
  // reuse that cause bug)
  private GameServer gameServer;
  private GameClient gameClient;

  // Reference to the discovery service, no need to be reuse so no null check
  private final DiscoveryService discoveryService;

  /** Instantiates a new Network manager. */
  public NetworkManager() {
    discoveryService = new DiscoveryService();
  }

  /** Start online play. */
  public void startOnlinePlay() {
    discoveryService.startListening();
  }

  /** Stop online play. */
  public void stopOnlinePlay() {
    discoveryService.stopListening();
    discoveryService.stopBroadcasting();

    // We stop running server/client (if not already stopped)
    if (gameServer != null) {
      gameServer.stop();
      gameServer = null;
    }
    if (gameClient != null) {
      gameClient.quit();
      gameClient = null;
    }
  }

  // =========================================================================
  // COMMANDS METHODS (these will be called when the user click on a button (GUI) / run a command
  // (CLI))
  // =========================================================================

  // -----F38-----

  /**
   * COMMAND server list: Displays the list of game servers available on the local network. Servers
   * periodically broadcast (every 10 seconds) their presence via a UDP message on port 12346
   * containing the server name and TCP port. A server is removed from the list if no message is
   * received for 30 seconds.
   *
   * @return the list
   */
  public List<ServerInfo> serverList() {
    return discoveryService.getActiveServer();
  }

  /**
   * COMMAND server start [PORT]: Starts a simple game server on specified TCP port default 12345.
   * The server must be able to accept one client connection at a time. Once started, the server
   * broadcasts its presence on the network via UDP broadcast. If the port is already in use,
   * display an error message.
   *
   * @param port the port
   * @return if the server start successfully or not
   */
  public boolean serverStart(int port) {
    // We check if the server is not already running
    if (gameServer != null) {
      GameLogger.logError("User : Server is already running, can't start it", null);
      return false;
    }

    // We check if the port is valid
    if (port < 0 || port > 65535) {
      return false;
    }

    try {
      gameServer = new GameServer();

      // We start the server in a Thread for not blocking this function with the while(true)
      gameServer.start(port);

      // Since servers need to have a name but the command don't tell about it, I use for now the
      // System user's name
      // I will ask teachers about this next session
      String defaultName = "Server-" + System.getProperty("user.name");
      discoveryService.startBroadcasting(defaultName, port, gameServer.getLocalNetworkIp());
      return true;
    } catch (Exception e) {
      gameServer = null;
      return false;
    }
  }

  /**
   * COMMAND server start : start with default port.
   *
   * @return if the server start successfully or not
   */
  public boolean serverStart() {
    return serverStart(DEFAULT_TCP_PORT);
  }

  /**
   * COMMAND server stop: Stops the game server. The connected client is notified of the shutdown
   * and disconnected.
   */
  public void serverStop() {
    // We check if the server is running before trying to stop it
    if (gameServer == null) {
      GameLogger.logError("User : Server is not running, can't stop it", null);
      return;
    }

    discoveryService.stopBroadcasting();
    gameServer.stop();
    gameServer = null;
  }

  /**
   * COMMAND join [IP[:PORT]]: Connects to the server at the specified IP address and port (default
   * localhost:12345). Once connected, the program switches to client mode. If the connection fails,
   * display an explicit error message.
   *
   * @param address the address
   * @param port the port
   */
  public void join(String address, int port) {
    // We check if the client isn't already connected
    if (gameClient != null) {
      GameLogger.logError("User : Client is already connected, can't connect it", null);
      return;
    }

    gameClient = new GameClient();
    for (NetworkObserver o : observers) {
      gameClient.addObserver(o);
    }

    gameClient.connect(address, port);
  }

  /**
   * COMMAND join [IP[:PORT]]: Connects to the server at the specified IP address and port (default
   * localhost:12345). Once connected, the program switches to client mode. If the connection fails,
   * display an explicit error message.
   *
   * @param address the address
   */
  public void join(String address) {
    this.join(address, DEFAULT_TCP_PORT);
  }

  /** COMMAND quit: Leaves the server and returns to local mode. */
  public void quit() {
    // We check if the client is connected before trying to disconnect it
    if (gameClient == null) {
      GameLogger.logError("User : Client is not connected, can't disconnect it", null);
      return;
    }

    gameClient.quit();
    gameClient = null;
  }

  /**
   * COMMAND ping: Sends a ping message to the server. The server responds with a pong and the
   * response time is displayed. This command allows testing the connection.
   */
  public void ping() {
    if (gameClient == null) {
      GameLogger.logError("User : Client is not connected, can't send a ping", null);
      return;
    }
    gameClient.sendPing();
  }

  // -----F39-----

  /**
   * COMMAND server status: Displays the server status. Listening port, number of connected clients,
   * number of ongoing games
   */
  public void serverStatus() {
    if (gameClient == null) {
      GameLogger.logError("User : Client is not connected, can't show server status", null);
      return;
    }
    gameClient.sendServerStatus();
  }

  /**
   * COMMAND players : Show server connected players with their id, name and status (idle, ingame).
   */
  public void players() {
    if (gameClient == null) {
      GameLogger.logError("User : Client is not connected, can't show players", null);
      return;
    }
    gameClient.sendPlayers();
  }

  /**
   * COMMAND scoreboard : Show player scoreboard for this server, with number of win, loose and
   * games played.
   */
  public void scoreboard() {
    if (gameClient == null) {
      GameLogger.logError("User : Client is not connected, can't show scoreboard", null);
      return;
    }
    gameClient.sendScoreboard();
  }

  /**
   * COMMAND new PLAYER_ID : Starts a new game with the specified player if they are IDLE. If the
   * player does not exist or is unavailable, display an error message. If the game supports more
   * than two players, simply provide multiple player IDs as arguments to the command.
   *
   * @param targetId the target id
   */
  public void newPlayerId(int targetId) {
    if (gameClient == null) {
      GameLogger.logError("User : Client is not connected, can't start a new game", null);
      return;
    }
    gameClient.sendNew(targetId);
  }

  /**
   * COMMAND new PLAYER_ID : Starts a new game with the specified player if they are IDLE. If the
   * player does not exist or is unavailable, display an error message. If the game supports more
   * than two players, simply provide multiple player IDs as arguments to the command.
   *
   * @param targetId1 the target id of the player 1
   * @param targetId2 the target id of the player 2
   */
  public void newPlayerId(int targetId1, int targetId2) {
    if (gameClient == null) {
      GameLogger.logError("User : Client is not connected, can't start a new game", null);
      return;
    }
    gameClient.sendNew(targetId1, targetId2);
  }

  /**
   * COMMAND new PLAYER_ID : Starts a new game with the specified player if they are IDLE. If the
   * player does not exist or is unavailable, display an error message. If the game supports more
   * than two players, simply provide multiple player IDs as arguments to the command.
   *
   * @param targetId1 the target id of the player 1
   * @param targetId2 the target id of the player 2
   * @param targetId3 the target id of the player 3
   */
  public void newPlayerId(int targetId1, int targetId2, int targetId3) {
    if (gameClient == null) {
      GameLogger.logError("User : Client is not connected, can't start a new game", null);
      return;
    }
    gameClient.sendNew(targetId1, targetId2, targetId3);
  }

  /**
   * COMMAND move PLAY: Plays a word on the board at the specified coordinates and direction.
   *
   * @param x the x coordinate (column)
   * @param y the y coordinate (row)
   * @param direction the direction (H for horizontal, V for vertical)
   * @param tile the word to place on the board
   */
  public void play(int x, int y, String direction, String tile) {
    if (gameClient == null) {
      GameLogger.logError("User : Client is not connected, can't play a move", null);
      return;
    }
    gameClient.sendPlayMove(x, y, direction, tile);
  }

  /**
   * COMMAND move EXCHANGE: Exchanges specified tiles from the player's rack with new ones from bag.
   *
   * @param tiles the tiles to exchange (ex: "A,B,C")
   */
  public void exchange(String tiles) {
    if (gameClient == null) {
      GameLogger.logError("User : Client is not connected, can't exchange tiles", null);
      return;
    }
    gameClient.sendExchangeMove(tiles);
  }

  /** COMMAND move PASS: Skips the current player's turn. */
  public void pass() {
    if (gameClient == null) {
      GameLogger.logError("User : Client is not connected, can't skip a turn", null);
      return;
    }
    gameClient.sendPassMove();
  }

  // -----F4O-----

  /** COMMAND accept : Accept the current invitation. */
  public void accept() {
    if (gameClient == null) {
      GameLogger.logError("User : Client is not connected, can't accept an invitation", null);
      return;
    }
    gameClient.sendAccept();
  }

  /** COMMAND accept : Decline the current invitation. */
  public void decline() {
    if (gameClient == null) {
      GameLogger.logError("User : Client is not connected, can't decline an invitation", null);
      return;
    }
    gameClient.sendDecline();
  }

  /**
   * COMMAND players [PLAYER_ID]: If an ID is provided, displays detailed information for the player
   * (status, score, statistics). The possible statuses are now: idle, away, waitgame (waiting for
   * an invitation response), and ingame.
   *
   * @param playerId the player id to get information about
   */
  public void playersPlayerId(int playerId) {
    if (gameClient == null) {
      GameLogger.logError("User : Client is not connected, can't show player information's", null);
      return;
    }
    gameClient.sendPlayersPlayerId(playerId);
  }

  /** COMMAND away: Change the player statue to away. */
  public void away() {
    if (gameClient == null) {
      return;
    }
    gameClient.sendAway();
  }

  /** COMMAND away: Change the player statue to idle. */
  public void back() {
    if (gameClient == null) {
      return;
    }
    gameClient.sendBack();
  }

  /** COMMAND cancel: Cancel current invitation sent). */
  public void cancel() {
    if (gameClient == null) {
      return;
    }
    gameClient.sendCancel();
  }

  // =========================================================================
  // UTILS METHODS

  /**
   * Get the local game client side.
   *
   * @return the local game client side, or null if not in online game
   */
  public Game getLocalGame() {
    if (gameClient == null) {
      return null;
    }
    return gameClient.getLocalGame();
  }

  // =========================================================================
  // OBSERVER METHODS

  /**
   * Add an observer to the list. Since NetworkManager observer list is the one who rules, we apply
   * change on gameClient and discoveryService
   *
   * @param observer the new observer to add
   */
  public void addObserver(NetworkObserver observer) {
    observers.add(observer);
    discoveryService.addObserver(observer);
    if (gameClient != null) {
      gameClient.addObserver(observer);
    }
  }

  /**
   * Remove an observer to the list. Since NetworkManager observer list is the one who rules, we
   * apply change on gameClient and discoveryService
   *
   * @param observer the new observer to remove
   */
  public void removeObserver(NetworkObserver observer) {
    if (!observers.remove(observer)) {
      GameLogger.logError("User : Observer not found, can't remove it from the list", null);
      return;
    } else {
      discoveryService.removeObserver(observer);
      if (gameClient != null) {
        gameClient.removeObserver(observer);
      }
    }
  }
}
