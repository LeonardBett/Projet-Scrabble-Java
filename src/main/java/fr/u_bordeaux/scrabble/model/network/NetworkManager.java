package fr.u_bordeaux.scrabble.model.network;

import java.util.List;

/** Manages network operations and acts as a facade for the network layer. */
public class NetworkManager {

  // Default values use in the package
  public static final int DEFAULT_TCP_PORT = 12345;
  public static final int DEFAULT_UDP_PORT = 12346;
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
   */
  public void serverStart(int port) {
    // We check if the server is not already running
    if (gameServer != null) {
      System.out.println("User : Server is already running, can't start it");
      return;
    }
    gameServer = new GameServer();

    // We start the server in a Thread for not blocking this function with the while(true)
    new Thread(gameServer::start).start();

    // Since servers need to have a name but the command don't tell about it, I use for now the
    // System user's name
    // I will ask teachers about this next session
    String defaultName = "Server-" + System.getProperty("user.name");
    discoveryService.startBroadcasting(defaultName, port);
  }

  /** COMMAND server start : start with default port. */
  public void serverStart() {
    serverStart(DEFAULT_TCP_PORT);
  }

  /**
   * COMMAND server stop: Stops the game server. The connected client is notified
   * of the shutdown and disconnected.
   */
  public void serverStop() {
    // We check if the server is running before trying to stop it
    if (gameServer == null) {
      System.out.println("User : Server is not running, can't stop it");
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
      System.out.println("User : Client is already connected, can't connect it");
      return;
    }

    gameClient = new GameClient();
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
      System.out.println("User : Client is not connected, can't disconnect it");
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
    gameClient.sendPing();
  }

  // -----F39-----

  // TODO:

  // -----F4O-----

  // TODO:
}
