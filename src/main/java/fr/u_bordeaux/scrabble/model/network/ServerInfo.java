package fr.u_bordeaux.scrabble.model.network;

/** The type Server info. */
// Store data of server, use for the Map of active server of DiscoveryService
public class ServerInfo {
  private final String ip;
  private final int port;
  private final String name;

  /** The constant SERVER_TIMEOUT. */
  // We will be changed for 30000 in final version
  public static final int SERVER_TIMEOUT = 15000;

  // Store the last time this server was seen
  private long lastSeen;

  /**
   * Instantiates a new Server info.
   *
   * @param ip the ip
   * @param port the port
   * @param name the name
   */
  public ServerInfo(String ip, int port, String name) {
    this.ip = ip;
    this.port = port;
    this.name = name;
    this.lastSeen = System.currentTimeMillis();
  }

  /** Update last seen. */
  // Update the last time this server was seen, when we receive a signal
  public void updateLastSeen() {
    this.lastSeen = System.currentTimeMillis();
  }

  /**
   * Is expired boolean.
   *
   * @return the boolean
   */
  // Return true if this server is expired, false otherwise
  public boolean isExpired() {
    return (System.currentTimeMillis() - lastSeen) > SERVER_TIMEOUT;
  }

  /**
   * Gets ip.
   *
   * @return the ip
   */
  // Getter
  public String getIp() {
    return ip;
  }

  /**
   * Gets port.
   *
   * @return the port
   */
  public int getPort() {
    return port;
  }

  /**
   * Gets name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "(name=" + name + ", ip=" + ip + ", port=" + port + ")";
  }
}
