package fr.ubordeaux.scrabble.model.network.server;


/**
 * Store data of server, use for the Map of active server of DiscoveryService.
 */
public class ServerInfo {
  private final String ip;
  private final int port;
  private final String name;

  /** The constant SERVER_TIMEOUT. */
  // The timeout of this server in ms
  public static final int SERVER_TIMEOUT = 30000;

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

  /** Update last seen of this server. */
  public void updateLastSeen() {
    this.lastSeen = System.currentTimeMillis();
  }

  /**
   * Return if this server is expired (lastseen > SERVER_TIMEOUT).
   *
   * @return the boolean
   */
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
