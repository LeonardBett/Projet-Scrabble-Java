package fr.ubordeaux.scrabble.model.network;

import fr.ubordeaux.scrabble.model.network.server.ServerInfo;
import java.util.List;
import java.util.Map;

/**
 * Observer interface that is use in CLI/GUI. We use a different methods for each type of event
 */
public interface NetworkObserver {

  // -----Local model update-----
  /** Call when the local model of the client is modified. */
  void localModelUpdate();

  /**
   * Call when the game is ended or interrupted.
   *
   * @param reason the reason of the game ending
   */
  void gameEndedUpdate(String reason);

  // -----Server commands update-----
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
}
