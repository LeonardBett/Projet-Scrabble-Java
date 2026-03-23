package fr.ubordeaux.scrabble.model.network.server;

import java.util.ArrayList;
import java.util.List;

public class PendingInvitation {

  private final ClientHandler host;
  private final List<ClientHandler> acceptedPlayers;
  private final List<ClientHandler> pendingPlayers;
  private final long expirationTime;

  public PendingInvitation(ClientHandler host, List<ClientHandler> targets) {
    this.host = host;
    this.acceptedPlayers = new ArrayList<>();
    this.acceptedPlayers.add(host);
    this.pendingPlayers = new ArrayList<>(targets);
    this.expirationTime = System.currentTimeMillis() + 300000; // 5 minutes (300 000 ms)
  }

  public ClientHandler getHost() {
    return host;
  }

  public List<ClientHandler> getAcceptedPlayers() {
    return acceptedPlayers;
  }

  public List<ClientHandler> getPendingPlayers() {
    return pendingPlayers;
  }

  public boolean isExpired(long currentTime) {
    return currentTime > expirationTime;
  }

  public boolean containsPendingPlayer(ClientHandler player) {
    return pendingPlayers.contains(player);
  }

  public void acceptPlayer(ClientHandler player) {
    pendingPlayers.remove(player);
    acceptedPlayers.add(player);
  }

  public void declinePlayer(ClientHandler player) {
    pendingPlayers.remove(player);
  }

  public boolean isComplete() {
    return pendingPlayers.isEmpty();
  }

  public boolean hasEnoughPlayers() {
    return acceptedPlayers.size() >= 2;
  }
}
