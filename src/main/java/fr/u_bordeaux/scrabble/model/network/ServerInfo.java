package fr.u_bordeaux.scrabble.model.network;

// Store data of server, use for the Map of active server of DiscoveryService
public class ServerInfo {
    private String ip;
    private int port;
    private String name;

    // We will be changed for 30000 in final version
    public static final int SERVER_TIMEOUT = 15000;

    // Store the last time this server was seen
    private long lastSeen;

    public ServerInfo(String ip, int port, String name) {
        this.ip = ip;
        this.port = port;
        this.name = name;
        this.lastSeen = System.currentTimeMillis();
    }

    // Update the last time this server was seen, when we receive a signal
    public void updateLastSeen(){
        this.lastSeen = System.currentTimeMillis();
    }

    // Return true if this server is expired, false otherwise
    public boolean isExpired(){
        return (System.currentTimeMillis() - lastSeen) > SERVER_TIMEOUT;
    }

    // Getter
    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString(){
        return "(name=" + name + ", ip=" + ip + ", port=" + port + ")";
    }
}
