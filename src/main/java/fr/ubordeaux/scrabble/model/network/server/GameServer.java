package fr.ubordeaux.scrabble.model.network.server;

import static fr.ubordeaux.scrabble.model.network.NetworkManager.DEFAULT_TCP_PORT;

import fr.ubordeaux.scrabble.model.network.PlayerStatus;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Game server for multiplayer mode. Manages client connections and network games. */
public class GameServer {

  /*
   * THREAD-SAFETY NOTE:
   * Collections.synchronizedList only provides thread-safety for individual
   * atomic operations like add(), remove(), or get().
   * It's just an extension that use synchronize on these operations
   *
   * It does NOT protect compound operations such as iterations (for-loops, removeIf).
   * To prevent ConcurrentModificationException, we must explicitly synchronize
   * on the list instance during iteration. This ensures the list's structure
   * isn't modified by another thread while we are traversing it.
   */

  // Volatile flag used to maintain the loop active and allow a graceful shutdown
  // of the thread.
  private volatile boolean isRunning = false;

  // Thread-safe list of running clients
  private final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

  // Thread-safe list of outgoing online games
  private final List<OnlineGame> onlineGames = Collections.synchronizedList(new ArrayList<>());

  // Server socket use to accept connexion, need to store it to be able to close
  // it
  private ServerSocket serverSocket;

  // Server info, created when starting the server and needed for commands
  private ServerInfo serverInfo;

  // Is used to give a unique id to each player
  private int idCounter = 1;

  private final List<PendingInvitation> activeInvitations =
      Collections.synchronizedList(new ArrayList<>());

  /** Start the server on the default port. */
  public void start() throws IOException {
    start(DEFAULT_TCP_PORT);
  }

  /**
   * Start a server on the specified port.
   *
   * @param port the port
   */
  public void start(int port) throws java.io.IOException {
    // System.out.println("Server : Server Starting...");
    isRunning = true;
    serverSocket = new ServerSocket(port);

    String ipAddress = getLocalNetworkIp();
    // Replace because we use ";" for parsing, so a player name can't contain this
    // character
    String serverName = "Server-" + System.getProperty("user.name").replace(";", "_");
    serverInfo = new ServerInfo(ipAddress, port, serverName);

    // Thread to clean expired invitations
    new Thread(
            () -> {
              while (isRunning) {
                try {
                  Thread.sleep(10000);
                  long now = System.currentTimeMillis();
                  synchronized (activeInvitations) {
                    activeInvitations.removeIf(
                        inv -> {
                          if (inv.isExpired(now)) {
                            for (ClientHandler p : inv.getAcceptedPlayers()) {
                              p.getClientInfo().setStatus(PlayerStatus.IDLE);
                              p.sendMessage("STATUS_UPDATE:STATUS=IDLE");
                              p.sendMessage("INVITATION_CANCELLED:REASON=Timeout (5 minutes)");
                            }

                            for (ClientHandler p : inv.getPendingPlayers()) {
                              p.getClientInfo().setStatus(PlayerStatus.IDLE);
                              p.sendMessage("STATUS_UPDATE:STATUS=IDLE");
                              p.sendMessage("INVITATION_CANCELLED:REASON=Timeout (5 minutes)");
                            }
                            return true;
                          }
                          return false;
                        });
                  }
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                }
              }
            })
        .start();

    // Thread to accept client
    new Thread(
            () -> {
              try {
                // Infinite loop for accepting connexion
                while (isRunning) {
                  Socket clientSocket = serverSocket.accept();
                  // System.out.println("Server : Client connected: " +
                  // clientSocket.getInetAddress());

                  // We are going to give this socket to a thread
                  ClientHandler handler = new ClientHandler(clientSocket, this, idCounter++);
                  addClient(handler);
                  new Thread(handler).start();
                }
              } catch (java.net.BindException e) {
                // System.err.println("Server Error: Port " + port + " is already in use.");
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
            })
        .start();
  }

  /** Stop the server. */
  public void stop() {
    if (!isRunning) {
      // System.err.println("Server : Server is not running, can't stop it");
      return;
    }
    isRunning = false;

    // System.out.println("Server : Server stopping, disconnecting all clients...");

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
    // System.out.println("Server : There is now " + clients.size() + " client(s) connected");
  }

  /**
   * Remove client.
   *
   * @param client the client
   */
  public void removeClient(ClientHandler client) {
    clients.remove(client);
    // System.out.println("Server : There is now " + clients.size() + " client(s) connected");
  }

  /**
   * Gets local network ip of this server. Needed for getting server infos
   *
   * @return the local network ip
   */
  public String getLocalNetworkIp() {
    try {
      var interfaces = java.net.NetworkInterface.getNetworkInterfaces();
      while (interfaces.hasMoreElements()) {
        var iface = interfaces.nextElement();
        // We ignore loopback and virtual interfaces
        if (iface.isLoopback() || !iface.isUp() || iface.isVirtual()) {
          continue;
        }

        var addresses = iface.getInetAddresses();
        while (addresses.hasMoreElements()) {
          var addr = addresses.nextElement();

          // We look for an ipv4 address which is not link local
          if (addr instanceof java.net.Inet4Address && !addr.isLinkLocalAddress()) {
            return addr.getHostAddress();
          }
        }
      }
    } catch (java.net.SocketException e) {
      System.err.println("Server Error: Unable to scan network interfaces.");
    }
    return "127.0.0.1";
  }

  /**
   * Create the STATUS command response with server information.
   *
   * @return the server STATUS command response
   */
  public String getStatusResponse() {
    int port = serverInfo.getPort();
    int clientCount = clients.size();
    int gameCount = onlineGames.size();

    return String.format("SERVER_STATUS:PORT=%d;CLIENTS=%d;GAMES=%d", port, clientCount, gameCount);
  }

  /**
   * Create the string with players infos needed for PLAYER command.
   *
   * @return the string with PLAYERS command response infos
   */
  public String getPlayersResponse() {
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
   * Starts a new game invitation process between the requester and target players. Checks if the
   * initiator and all targets are available before creating a pending invitation.
   *
   * @param initiator The client who sent the "new" command
   * @param targetIds The list of IDs of the players to invite
   * @return A status message for the initiator (e.g., "INVITATION_SENT" or an error message)
   */
  public synchronized String createNewGame(ClientHandler initiator, List<Integer> targetIds) {
    if (initiator.getClientInfo().getStatus() != PlayerStatus.IDLE) {
      initiator.sendMessage("INVITATION_FAILED:REASON=You are not available");
      return "ERROR: You are not available";
    }

    List<ClientHandler> targets = new ArrayList<>();
    synchronized (clients) {
      for (int id : targetIds) {
        ClientHandler target = null;
        for (ClientHandler c : clients) {
          if (c.getClientInfo().getId() == id) {
            target = c;
            break;
          }
        }
        if (target == null) {
          initiator.sendMessage("INVITATION_FAILED:REASON=Player " + id + " not found");
          return "ERROR: Player " + id + " not found";
        }
        if (target == initiator) {
          initiator.sendMessage("INVITATION_FAILED:REASON=You cannot play against yourself");
          return "ERROR: You cannot play against yourself";
        }
        if (target.getClientInfo().getStatus() != PlayerStatus.IDLE) {
          initiator.sendMessage(
              "INVITATION_FAILED:REASON=Player " + target.getClientInfo().getName() + " is busy");
          return "ERROR: Player " + target.getClientInfo().getName() + " is busy";
        }
        targets.add(target);
      }
    }

    // We create the invitation
    PendingInvitation invitation = new PendingInvitation(initiator, targets);
    activeInvitations.add(invitation);

    // We update status of initiator and targets
    initiator.getClientInfo().setStatus(PlayerStatus.WAITGAME);
    initiator.sendMessage("STATUS_UPDATE:STATUS=WAITGAME");
    for (ClientHandler t : targets) {
      t.getClientInfo().setStatus(PlayerStatus.WAITGAME);
      t.sendMessage("STATUS_UPDATE:STATUS=WAITGAME");
      t.sendMessage("INVITATION_RECEIVED:FROM=" + initiator.getClientInfo().getName());
    }
    return "INVITATION_SENT";
  }

  /**
   * Processes a player's response (accept or decline) to a pending game invitation. If all invited
   * players have responded and enough have accepted, the game starts.
   *
   * @param player The client handler representing the player responding.
   * @param accepted true if the player accepted the invitation, false if they declined.
   */
  public synchronized void processInvitationResponse(ClientHandler player, boolean accepted) {
    PendingInvitation currentInv = null;

    synchronized (activeInvitations) {
      for (PendingInvitation inv : activeInvitations) {
        if (inv.containsPendingPlayer(player)) {
          currentInv = inv;
          break;
        }
      }
    }

    if (currentInv == null) {
      player.sendMessage("ERROR: No pending invitation.");
      return;
    }

    if (accepted) {
      currentInv.acceptPlayer(player);
      currentInv
          .getHost()
          .sendMessage("INVITATION_ACCEPTED:PLAYER=" + player.getClientInfo().getName());
    } else {
      currentInv.declinePlayer(player);
      player.getClientInfo().setStatus(PlayerStatus.IDLE);
      player.sendMessage("STATUS_UPDATE:STATUS=IDLE");
      currentInv
          .getHost()
          .sendMessage("INVITATION_DECLINED:PLAYER=" + player.getClientInfo().getName());
    }

    if (currentInv.isComplete()) {
      activeInvitations.remove(currentInv);

      if (currentInv.hasEnoughPlayers()) {
        OnlineGame session = new OnlineGame(currentInv.getAcceptedPlayers());
        onlineGames.add(session);
        for (ClientHandler p : currentInv.getAcceptedPlayers()) {
          p.getClientInfo().setStatus(PlayerStatus.INGAME);
          p.sendMessage("STATUS_UPDATE:STATUS=INGAME");
        }
      } else {
        for (ClientHandler p : currentInv.getAcceptedPlayers()) {
          p.getClientInfo().setStatus(PlayerStatus.IDLE);
          p.sendMessage("STATUS_UPDATE:STATUS=IDLE");
        }
        currentInv.getHost().sendMessage("INVITATION_FAILED:REASON=Not enough players accepted");
        currentInv.getHost().sendMessage("ERROR: Not enough players accepted to start the game.");
      }
    }
  }

  /**
   * Handles the AWAY command by changing the player's status, if allowed.
   *
   * @param player the client requesting to go away
   */
  public synchronized void processAway(ClientHandler player) {
    PlayerStatus currentStatus = player.getClientInfo().getStatus();

    if (currentStatus == PlayerStatus.WAITGAME) {
      player.sendMessage("ERROR: You cannot go away while having a pending invitation.");
      return;
    }
    if (currentStatus == PlayerStatus.INGAME) {
      player.sendMessage("ERROR: You cannot go away while in a game.");
      return;
    }

    player.getClientInfo().setStatus(PlayerStatus.AWAY);
    player.sendMessage("STATUS_UPDATE:STATUS=AWAY");
  }

  /**
   * Handles the BACK command by returning an AWAY player to the IDLE state.
   *
   * @param player the client requesting to go back to idle
   */
  public void processBack(ClientHandler player) {
    if (player.getClientInfo().getStatus() == PlayerStatus.AWAY) {
      player.getClientInfo().setStatus(PlayerStatus.IDLE);
      player.sendMessage("STATUS_UPDATE:STATUS=IDLE");
    } else {
      player.sendMessage("ERROR: You are not currently away.");
    }
  }

  /**
   * Processes the CANCEL command. Cancels a pending invitation initiated by the requesting player.
   *
   * @param player the client requesting to cancel their own invitation
   */
  public void processCancel(ClientHandler player) {
    PendingInvitation currentInv = null;

    // We look for the current invitation
    synchronized (activeInvitations) {
      for (PendingInvitation inv : activeInvitations) {
        if (inv.getHost() == player) {
          currentInv = inv;
          break;
        }
      }
    }

    if (currentInv == null) {
      player.sendMessage("ERROR: You have no pending invitation to cancel.");
      return;
    }

    // We delete her
    synchronized (activeInvitations) {
      activeInvitations.remove(currentInv);
    }

    // We update all player from this invitation
    for (ClientHandler p : currentInv.getAcceptedPlayers()) {
      p.getClientInfo().setStatus(PlayerStatus.IDLE);
      p.sendMessage("STATUS_UPDATE:STATUS=IDLE");

      if (p == player) {
        p.sendMessage("Invitation successfully cancelled.");
      } else {
        p.sendMessage("INVITATION_CANCELLED:REASON=Host cancelled");
      }
    }

    // On libère les joueurs qui étaient encore en train de réfléchir (pending)
    for (ClientHandler p : currentInv.getPendingPlayers()) {
      p.getClientInfo().setStatus(PlayerStatus.IDLE);
      p.sendMessage("STATUS_UPDATE:STATUS=IDLE");
      p.sendMessage("INVITATION_CANCELLED:REASON=Host cancelled");
    }
  }

  /**
   * Cleans up any pending invitations involving a disconnecting player.
   *
   * @param player the client that is disconnecting.
   */
  public synchronized void removePlayerFromInvitations(ClientHandler player) {
    synchronized (activeInvitations) {
      activeInvitations.removeIf(
          inv -> {
            if (inv.getHost() == player
                || inv.containsPendingPlayer(player)
                || inv.getAcceptedPlayers().contains(player)) {

              // We update all player from this invitation
              for (ClientHandler p : inv.getAcceptedPlayers()) {
                if (p != player) {
                  p.getClientInfo().setStatus(PlayerStatus.IDLE);
                  p.sendMessage("STATUS_UPDATE:STATUS=IDLE");
                  p.sendMessage("INVITATION_CANCELLED:REASON=Host disconnected");
                }
              }
              for (ClientHandler p : inv.getPendingPlayers()) {
                if (p != player) {
                  p.getClientInfo().setStatus(PlayerStatus.IDLE);
                  p.sendMessage("STATUS_UPDATE:STATUS=IDLE");
                  p.sendMessage("INVITATION_CANCELLED:REASON=Host disconnected");
                }
              }
              return true;
            }
            return false;
          });
    }
  }

  /**
   * Generates a detailed response for a specific player ID (Requirement F40). Ensures every
   * attribute follows the KEY=VALUE format for the PacketParser.
   *
   * @param targetId the ID of the player to look for.
   * @return a formatted string compatible with PacketParser, or an error message.
   */
  public String getSpecificPlayerResponse(int targetId) {
    synchronized (clients) {
      for (ClientHandler client : clients) {
        if (client.getClientInfo().getId() == targetId) {
          ClientInfo info = client.getClientInfo();
          return "PLAYERS_PLAYER_ID:" + info.getPlayerInfo() + ";" + info.getScoreboardLine();
        }
      }
    }
    return "ERROR: Player " + targetId + " not found";
  }

  /**
   * Remove an online game from the list of active online game.
   *
   * @param game the game to remove
   */
  public void removeOnlineGame(OnlineGame game) {
    onlineGames.remove(game);
  }
}
