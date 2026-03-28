package fr.ubordeaux.scrabble.model.network;

import fr.ubordeaux.scrabble.model.network.server.ServerInfo;
import java.util.List;
import java.util.Map;

/** Observer interface that is use in CLI/GUI. We use a different methods for each type of event */
public interface NetworkObserver {

  // -----Local model update-----

  /** Call when the local model of the client is modified. */
  void localModelUpdate();

  /**
   * Call when the game is finished.
   *
   * @param finalScore the final score of the finished game
   */
  void gameEndedUpdate(List<Map<String, String>> finalScore);

  // -----Server commands update-----

  /**
   * Call when the server send hello to this client.
   *
   * @param myId map containing server information (PORT, CLIENTS, GAMES)
   */
  void serverWelcomeUpdate(int myId);

  /**
   * Call when the server status command response is received.
   *
   * @param info map containing server information (PORT, CLIENTS, GAMES)
   */
  void serverStatusUpdate(Map<String, String> info);

  /**
   * Call when the players command response is received.
   *
   * @param players list of maps containing players information
   */
  void playersUpdate(List<Map<String, String>> players);

  /**
   * Call when the scoreboard command response is received.
   *
   * @param scoreboard list of maps containing players statistics
   */
  void scoreboardUpdate(List<Map<String, String>> scoreboard);

  /**
   * Call when a PONG response is received from the server.
   *
   * @param latencyMs the latency in milliseconds
   */
  void pongUpdate(long latencyMs);

  // -----Discovery service update-----

  /**
   * Call when the discovery service update the server list.
   *
   * @param activeServers list of servers founds
   */
  void serverListUpdate(List<ServerInfo> activeServers);

  // -----Other update-----

  /**
   * Call when we receive a generic message from the server.
   *
   * @param message the message
   */
  void messageUpdate(String message);

  /**
   * Call when a game invitation is received from another player.
   *
   * @param from the name of the player who sent the invitation
   */
  void invitationReceivedUpdate(String from);

  /**
   * Call when a player accepts an invitation sent by this client.
   *
   * @param playerAccepted the name of the player who accepted
   */
  void invitationAcceptedUpdate(String playerAccepted);

  /**
   * Call when a player declines an invitation sent by this client.
   *
   * @param playerDeclined the name of the player who declined
   */
  void invitationDeclinedUpdate(String playerDeclined);

  /**
   * Call when a pending invitation is cancelled (by host, timeout, or disconnect).
   *
   * @param reason the reason of the cancellation
   */
  void invitationCancelledUpdate(String reason);

  /**
   * Call when the players playerId command response is received.
   *
   * @param playerInfo map containing this player information
   */
  void playersPlayerIdUpdate(Map<String, String> playerInfo);

  /**
   * Call when the server confirms a status change for this client.
   *
   * @param status the new status
   */
  void playerStatusUpdate(String status);

  // -----Connexion / disconnection update-----

  /**
   * Call when the client is disconnected from the server.
   *
   * @param reason the reason of the disconnection
   */
  void clientDisconnectedUpdate(String reason);

  /**
   * Call when the game is interrupted.
   *
   * @param reason the reason of the interruption
   */
  void gameInterruptedUpdate(String reason);

  /**
   * Call when a connection attempt to a server fails.
   *
   * @param reason the error message
   */
  void connectionFailedUpdate(String reason);

  /**
   * Call when an invitation attempt fails.
   *
   * @param reason the error message
   */
  void invitationFailedUpdate(String reason);
}
