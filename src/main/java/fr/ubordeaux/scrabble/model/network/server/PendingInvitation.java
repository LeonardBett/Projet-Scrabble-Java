package fr.ubordeaux.scrabble.model.network.server;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a game invitation that has been sent by a host to one or more target players, and is
 * waiting for their responses. It manages the players who have accepted, those who are still
 * pending, and an expiration time.
 */
public class PendingInvitation {

  private final ClientHandler host;
  private final List<ClientHandler> acceptedPlayers;
  private final List<ClientHandler> pendingPlayers;
  private final long expirationTime;

  /**
   * Constructs a new PendingInvitation. The host is automatically added to the list of accepted
   * players. The invitation is set to expire in 5 minutes (300,000 milliseconds).
   *
   * @param host The client who initiated the game invitation.
   * @param targets The list of clients being invited to the game.
   */
  public PendingInvitation(ClientHandler host, List<ClientHandler> targets) {
    this.host = host;
    this.acceptedPlayers = new ArrayList<>();
    this.acceptedPlayers.add(host);
    this.pendingPlayers = new ArrayList<>(targets);
    this.expirationTime = System.currentTimeMillis() + 300000; // 5 minutes (300 000 ms)
  }

  /**
   * Gets the client who hosted (initiated) this invitation.
   *
   * @return The host's ClientHandler.
   */
  public ClientHandler getHost() {
    return host;
  }

  /**
   * Gets the list of clients who have accepted the invitation (including the host).
   *
   * @return A list of ClientHandlers representing the accepted players.
   */
  public List<ClientHandler> getAcceptedPlayers() {
    return acceptedPlayers;
  }

  /**
   * Gets the list of clients who have not yet responded to the invitation.
   *
   * @return A list of ClientHandlers representing the pending players.
   */
  public List<ClientHandler> getPendingPlayers() {
    return pendingPlayers;
  }

  /**
   * Checks if the invitation has expired based on the provided current time.
   *
   * @param currentTime The current system time in milliseconds.
   * @return true if the invitation is expired, false otherwise.
   */
  public boolean isExpired(long currentTime) {
    return currentTime > expirationTime;
  }

  /**
   * Checks if a specific player is still pending a response for this invitation.
   *
   * @param player The client handler to check.
   * @return true if the player is in the pending list, false otherwise.
   */
  public boolean containsPendingPlayer(ClientHandler player) {
    return pendingPlayers.contains(player);
  }

  /**
   * Processes an acceptance from a pending player. The player is removed from the pending list and
   * added to the accepted list.
   *
   * @param player The client handler representing the player who accepted.
   */
  public void acceptPlayer(ClientHandler player) {
    pendingPlayers.remove(player);
    acceptedPlayers.add(player);
  }

  /**
   * Processes a decline from a pending player. The player is removed from the pending list.
   *
   * @param player The client handler representing the player who declined.
   */
  public void declinePlayer(ClientHandler player) {
    pendingPlayers.remove(player);
  }

  /**
   * Checks if all invited players have responded (either accepted or declined).
   *
   * @return true if the pending list is empty, false otherwise.
   */
  public boolean isComplete() {
    return pendingPlayers.isEmpty();
  }

  /**
   * Checks if there are enough players to start a game. Currently, this requires at least 2 players
   * (the host and at least one other).
   *
   * @return true if there are 2 or more accepted players, false otherwise.
   */
  public boolean hasEnoughPlayers() {
    return acceptedPlayers.size() >= 2;
  }
}
