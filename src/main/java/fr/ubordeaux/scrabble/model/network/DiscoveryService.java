package fr.ubordeaux.scrabble.model.network;

import static fr.ubordeaux.scrabble.model.network.NetworkManager.DEFAULT_TCP_PORT;
import static fr.ubordeaux.scrabble.model.network.NetworkManager.DEFAULT_UDP_PORT;

import fr.ubordeaux.scrabble.model.network.server.ServerInfo;
import fr.ubordeaux.scrabble.model.utils.GameLogger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** The type Discovery service. */
public class DiscoveryService {

  /** Default constructor for DiscoveryService. */
  public DiscoveryService() {}

  // List of observers
  private final List<NetworkObserver> observers = new ArrayList<>();

  // Default value for the discovery service
  private static final String DEFAULT_BROADCAST = "255.255.255.255";
  private static final int BROADCAST_INTERVAL = 10000;

  // Broadcast variable
  // For broadcast, we keep a reference of the Thread (and not of Socket like
  // other)
  // because broadcast will be stuck in a sleep most of the time.
  // So when stopBroadcasting() is called, stopping with a Socket like other will
  // cause lag
  // (need to wait for the timer to expire before raising Socket Exception)
  private volatile boolean isBroadcasting = false;
  private Thread broadCastThread;

  // Listening variable
  private volatile boolean isListening = false;
  private DatagramSocket listenSocket;

  // Needed for stocking discovered server while broadcasting
  // ConcurrentHashMap because it can be access simultaneously
  private final Map<String, ServerInfo> discoveredServer = new ConcurrentHashMap<>();

  // =========================================================================
  // BROADCASTING
  // =========================================================================
  // We the user start a server, the Broadcast part will send UDP message
  // periodically,
  // to signal himself to others online players

  /**
   * Start broadcasting. Message format: "SCRABBLE_SERVER;serverName;PortTCP"
   *
   * @param serverName the server name
   * @param tcpPort the tcp port
   * @param localIp the local ip to broadcast on
   */
  public void startBroadcasting(String serverName, int tcpPort, String localIp) {
    if (isBroadcasting) {
      GameLogger.logVerbose("Broadcasting is already running");
      return;
    }
    isBroadcasting = true;
    GameLogger.logVerbose("Broadcast : Broadcasting started");

    // Lambda use to shortcut of the run method of Runnable
    // Can be put in an external methode
    broadCastThread =
        new Thread(
            () -> {
              try (DatagramSocket broadcastSocket =
                  new DatagramSocket(new java.net.InetSocketAddress(localIp, 0))) {
                // We have to set the broadcast to true to be able to send broadcast message
                broadcastSocket.setBroadcast(true);

                // We build the message and get byte buffer of it (needed for the socket)
                String message = "SCRABBLE_SERVER;" + serverName + ";" + tcpPort;
                byte[] byteMessage = message.getBytes();

                // Needed for translating the String destination address into bit
                InetAddress destinationAddress = InetAddress.getByName(DEFAULT_BROADCAST);

                // We create the packet here, since it will not change during the Thread life
                DatagramPacket packet =
                    new DatagramPacket(
                        byteMessage, byteMessage.length, destinationAddress, DEFAULT_UDP_PORT);

                // Infinite loop of sending periodic broadcast message
                while (isBroadcasting) {
                  GameLogger.logDebug("Broadcast : Sending a broadcast message");

                  // We send the UDP packet, then sleep
                  broadcastSocket.send(packet);
                  Thread.sleep(BROADCAST_INTERVAL);
                }

              } catch (InterruptedException e) {
                // Normal stop for this thread, call by stopBroadcasting()
                if (isBroadcasting) {
                  Thread.currentThread().interrupt();
                }
              } catch (Exception e) {
                GameLogger.logError("Broadcast : Exception caught", e);
              } finally {
                if (isBroadcasting) {
                  stopBroadcasting();
                }
              }
            });
    // We start the broadcasting Thread
    broadCastThread.start();
  }

  /**
   * Start broadcasting with default port.
   *
   * @param serverName the server name
   * @param localIp the local ip
   */
  public void startBroadcasting(String serverName, String localIp) {
    startBroadcasting(serverName, DEFAULT_TCP_PORT, localIp);
  }

  /** Stop broadcasting. */
  public void stopBroadcasting() {
    if (!isBroadcasting) {
      return;
    }
    isBroadcasting = false;
    GameLogger.logVerbose("Broadcast : Broadcasting stopped");

    // Stop the broadcasting Thread, this will stop the broadcast loop
    if (broadCastThread != null) {
      broadCastThread.interrupt();
    }
  }

  // =========================================================================
  // LISTENING
  // =========================================================================

  /** Start listening to broadcast message. */
  public void startListening() {
    if (isListening) {
      GameLogger.logVerbose("Listening is already running");
      return;
    }
    isListening = true;
    GameLogger.logVerbose("Listening : Listening started");

    // Anonym Thread because we will stop it with his socket
    new Thread(
            () -> {
              try {
                listenSocket = new DatagramSocket(null);

                // We allow reuse of the port to allow multiple client on the same computer
                listenSocket.setReuseAddress(true);

                // We specify 0.0.0.0 to be able to listen on all network interfaces
                listenSocket.bind(
                    new java.net.InetSocketAddress(
                        InetAddress.getByName("0.0.0.0"), DEFAULT_UDP_PORT));

                // We have to set the broadcast to true to be able to receive broadcast message
                listenSocket.setBroadcast(true);

                // Buffer use for stocking the received data
                byte[] buffer = new byte[1024];

                // Listening loop
                while (isListening) {
                  DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                  // Block here => waiting a message
                  // Will deblock if the socket is closed, and raise a SocketException
                  listenSocket.receive(packet);
                  GameLogger.logDebug("Listening : Received a broadcast message");

                  // Extract and send the data to processing
                  String message = new String(packet.getData(), 0, packet.getLength());
                  processPacket(message, packet.getAddress());
                }
              } catch (SocketException e) {
                // Normal behavior if we closed the socket with stopListening()
                if (isListening) {
                  GameLogger.logError("Listening : Socket Exception caught", e);
                }
              } catch (Exception e) {
                GameLogger.logError("Listening : Exception caught", e);
              } finally {
                if (isListening) {
                  stopListening();
                }
              }
            })
        .start(); // We start this thread
  }

  // Process a packet's message, by adding it to the servers Map if not present,
  // or else updating
  // this server timeout
  private void processPacket(String message, InetAddress senderAddress) {
    // We need a try catch because the parsing and the port conversion can fail
    // This failure will stop the listening loop if not handled
    try {
      // Parsing the message
      String[] parts = message.split(";");

      // Verifying if the message is correctly formed
      if (parts.length == 3 && parts[0].equals("SCRABBLE_SERVER")) {
        // We get server name, TCP port and address
        String name = parts[1];
        int port = Integer.parseInt(parts[2]);
        String ip = senderAddress.getHostAddress();

        // We build the key for the map
        String key = ip + ":" + port;

        // If we know this server, we update his timer
        // Else, we add it to the map
        ServerInfo existing = discoveredServer.putIfAbsent(key, new ServerInfo(ip, port, name));
        if (existing != null) {
          existing.updateLastSeen();
        }
        notifyListeners();
      }
    } catch (NumberFormatException e) {
      GameLogger.logError("Malformed packet received (Invalid port)", e);
    } catch (Exception e) {
      GameLogger.logError("Error while processing packet", e);
    }
  }

  /** Stop listening to broadcast message. */
  public void stopListening() {
    if (!isListening) {
      return;
    }
    isListening = false;
    GameLogger.logVerbose("Listening : Listening stopped");

    // Close the socket if opened, this will stop the listening loop
    if (listenSocket != null && !listenSocket.isClosed()) {
      listenSocket.close();
    }
  }

  // =========================================================================
  // DISCOVERYSERVICE UTILS METHODS

  /**
   * Get the list of known server (and remove expired one).
   *
   * @return the active server
   */
  public List<ServerInfo> getActiveServer() {
    // We remove expired server from the map
    discoveredServer.values().removeIf(ServerInfo::isExpired);

    // We return a new List to avoid concurrency
    return new ArrayList<>(discoveredServer.values());
  }

  // =========================================================================
  // OBSERVERS METHODS

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
      GameLogger.logError("User : Observer not found, can't remove it from the list", null);
      return;
    }
  }

  private void notifyListeners() {
    List<ServerInfo> servers = getActiveServer();
    for (NetworkObserver o : observers) {
      o.serverListUpdate(servers);
    }
  }
}
