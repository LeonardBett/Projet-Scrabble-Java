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

  // Thread-safe list of outgoing online games
  private final List<ActiveGame> activeGames = Collections.synchronizedList(new ArrayList<>());

  // Server socket use to accept connexion, need to store it to be able to close it
  private ServerSocket serverSocket;

  // Server info, created when starting the server and needed for commands
  private ServerInfo serverInfo;

  // Is used to give a unique id to each player
  private int idCounter = 1;

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

      String ipAddress = getLocalNetworkIp();
      // Replace because we use ";" for parsing, so a player name can't contain this character
      String serverName = "Server-" + System.getProperty("user.name").replace(";", "_");
      serverInfo = new ServerInfo(ipAddress, port, serverName);

      // Infinite loop for accepting connexion
      while (isRunning) {
        Socket clientSocket = serverSocket.accept();
        System.out.println("Server : Client connected: " + clientSocket.getInetAddress());

        // We are going to give this socket to a thread
        ClientHandler handler = new ClientHandler(clientSocket, this, idCounter++);
        addClient(handler);
        new Thread(handler).start();
      }
    } catch (java.net.BindException e) {
      System.err.println("Server Error: Port " + port + " is already in use.");
      isRunning = false;
    } catch (SocketException e) {
      if (isRunning) {
        System.err.println("Server Error: Socket exception - " + e.getMessage());
      }
      // If isRunning is false, it means we called stop(), so we just exit the loop
    } catch (IOException e) {
      System.err.println("Server Error: IO exception - " + e.getMessage());
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
      System.err.println("Server Error: IO exception - " + e.getMessage());
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

  /**
   * Gets local network ip of this server.
   * Needed for getting server infos
   *
   * @return the local network ip
   */
  private String getLocalNetworkIp() {
    try {
      var interfaces = java.net.NetworkInterface.getNetworkInterfaces();
      while (interfaces.hasMoreElements()) {
        var iface = interfaces.nextElement();
        // We ignore inactive, virtual and loopback interfaces
        if (iface.isLoopback() || !iface.isUp() || iface.isVirtual()) continue;

        var addresses = iface.getInetAddresses();
        while (addresses.hasMoreElements()) {
          var addr = addresses.nextElement();
          // We ignore IPv6
          if (addr instanceof java.net.Inet4Address) {
            return addr.getHostAddress();
          }
        }
      }
    } catch (java.net.SocketException e) {
      System.err.println("Server Error: Unable to scan network interfaces.");
    }
    return "127.0.0.1"; // Default value if no IP is found
  }

  /**
   * Create the STATUS command response with server information.
   *
   * @return the server STATUS command response
   */
  public String getStatusResponse() {
    int port = serverInfo.getPort();
    int clientCount = clients.size();
    int gameCount = activeGames.size();

    return String.format("SERVER_STATUS:PORT=%d;CLIENTS=%d;GAMES=%d",
            port, clientCount, gameCount);
  }

  /**
   * Create the string with players infos needed for PLAYER command.
   *
   * @return the string with PLAYERS command response infos
   */
  public String getPlayerResponse() {
    StringBuilder sb = new StringBuilder("PLAYERS:");
    synchronized (clients) {
      for (ClientHandler client : clients) {
        sb.append(client.getClientInfo().getPlayerInfo()).append("|");
      }
    }
    return sb.toString();
  }

  /**
   * Create the string with players infos needed for SCOREBOARD command.
   *
   * @return the string with SCOREBOARD command response infos
   */
  public String getScoreboardResponse() {
    StringBuilder sb = new StringBuilder("SCOREBOARD:");
    synchronized (clients) {
      for (ClientHandler client : clients) {
        sb.append(client.getClientInfo().getScoreboardLine()).append("|");
      }
    }
    return sb.toString();
  }

  /**
   * Starts a new game between the requester and a target player.
   * Will support multiple target player in the future.
   * @param initiator The client who sent the "new" command
   * @param targetId The ID of the player to invite
   * @return String response for the initiator
   */
  public synchronized String createNewGame(ClientHandler initiator, int targetId) {
    // 1. Find the target client
    ClientHandler target = null;
    synchronized (clients) {
      for (ClientHandler c : clients) {
        if (c.getClientInfo().getId() == targetId) {
          target = c;
          break;
        }
      }
    }

    // 2. Check if  the target player is valid and not busy
    if (target == null) {
      return "ERROR: Player not found";
    }
    if (target == initiator) {
      return "ERROR: You cannot play against yourself";
    }
    if (target.getClientInfo().getStatus() != PlayerStatus.IDLE) {
      return "ERROR: Player is busy";
    }
    if (initiator.getClientInfo().getStatus() != PlayerStatus.IDLE) {
      return "ERROR: You are already in game";
    }

    ActiveGame session = new ActiveGame(List.of(initiator, target));
    activeGames.add(session);

    // 4. Update status
    initiator.getClientInfo().setStatus(PlayerStatus.INGAME);
    target.getClientInfo().setStatus(PlayerStatus.INGAME);

    // 5. Notify both players
    session.broadcast("GAME_START: ");

    return "SUCCESS: Game started";
  }
}
