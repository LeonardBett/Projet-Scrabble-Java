package fr.ubordeaux.scrabble.controller.network;

import fr.ubordeaux.scrabble.model.network.NetworkManager;
import fr.ubordeaux.scrabble.model.network.server.ServerInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

/**
 * Facade for lobby network commands.
 */
public class NetworkLobbyController {

  private final NetworkManager networkManager;
  private boolean pendingGameStart;

  /**
   * Creates a lobby controller for the given network manager.
   *
   * @param networkManager network backend
   */
  public NetworkLobbyController(NetworkManager networkManager) {
    this.networkManager = networkManager;
  }

  /**
   * Starts discovery/listening for online play.
   */
  public void startOnlinePlay() {
    networkManager.startOnlinePlay();
  }

  /**
   * Stops every online network activity.
   */
  public void stopOnlinePlay() {
    networkManager.stopOnlinePlay();
  }

  /**
   * Starts a server.
   *
   * @param port tcp port
   * @return true when started
   */
  public boolean serverStart(int port) {
    return networkManager.serverStart(port);
  }

  /** Stops the server. */
  public void serverStop() {
    networkManager.serverStop();
  }

  /** Connects to a server. */
  public void join(String ip, int port) {
    networkManager.join(ip, port);
  }

  /** Leaves client mode. */
  public void quit() {
    networkManager.quit();
  }

  /** Sends a ping. */
  public void ping() {
    networkManager.ping();
  }

  /** Requests server status. */
  public void serverStatus() {
    networkManager.serverStatus();
  }

  /** Requests player list. */
  public void players() {
    networkManager.players();
  }

  /**
   * Requests a game start from the host side by fetching the player list first.
   */
  public void requestGameStart() {
    pendingGameStart = true;
    players();
  }

  /**
   * Handles the player list update used when starting a game from the lobby.
   *
   * @param players player data received from the server
   */
  public void handlePlayersUpdate(List<Map<String, String>> players) {
    if (!pendingGameStart || players == null) {
      return;
    }

    List<Integer> ids = collectPositivePlayerIds(players);
    if (!shouldDispatchGameStart(true, ids.size())) {
      return;
    }

    pendingGameStart = false;
    dispatchHostNewGame(ids);
  }

  /** Requests scoreboard. */
  public void scoreboard() {
    networkManager.scoreboard();
  }

  /** Starts a game with one target. */
  public void newPlayerId(int targetId) {
    networkManager.newPlayerId(targetId);
  }

  /** Starts a game with two targets. */
  public void newPlayerId(int targetId1, int targetId2) {
    networkManager.newPlayerId(targetId1, targetId2);
  }

  /** Starts a game with three targets. */
  public void newPlayerId(int targetId1, int targetId2, int targetId3) {
    networkManager.newPlayerId(targetId1, targetId2, targetId3);
  }

  /**
   * Starts a new game with the selected players. The first three valid ids are used.
   *
   * @param selectedEntries lobby entries selected in the UI
   */
  public void newPlayerIds(List<String> selectedEntries) {
    List<Integer> targetIds = parseLobbyPlayerIds(selectedEntries);
    if (targetIds.isEmpty()) {
      return;
    }

    if (targetIds.size() == 1) {
      newPlayerId(targetIds.getFirst());
    } else if (targetIds.size() == 2) {
      newPlayerId(targetIds.get(0), targetIds.get(1));
    } else {
      newPlayerId(targetIds.get(0), targetIds.get(1), targetIds.get(2));
    }
  }

  /** Sends a play command. */
  public void play(int x, int y, String direction, String tile) {
    networkManager.play(x, y, direction, tile);
  }

  /** Sends an exchange command. */
  public void exchange(String tiles) {
    networkManager.exchange(tiles);
  }

  /** Sends a pass command. */
  public void pass() {
    networkManager.pass();
  }

  /** Accepts an invitation. */
  public void accept() {
    networkManager.accept();
  }

  /** Declines an invitation. */
  public void decline() {
    networkManager.decline();
  }

  /** Requests details for one player. */
  public void playersPlayerId(int playerId) {
    networkManager.playersPlayerId(playerId);
  }

  /** Marks away status. */
  public void away() {
    networkManager.away();
  }

  /** Marks back status. */
  public void back() {
    networkManager.back();
  }

  /** Cancels the current invitation. */
  public void cancel() {
    networkManager.cancel();
  }

  /**
   * Parses a TCP port string and validates the range.
   *
   * @param portText text entered by the user
   * @return parsed port, or empty when invalid
   */
  public OptionalInt parsePort(String portText) {
    if (portText == null) {
      return OptionalInt.empty();
    }
    try {
      int port = Integer.parseInt(portText.trim());
      if (port < 0 || port > 65535) {
        return OptionalInt.empty();
      }
      return OptionalInt.of(port);
    } catch (NumberFormatException e) {
      return OptionalInt.empty();
    }
  }

  /**
   * Extracts a player identifier from a lobby entry such as "#12 Name [STATUS]".
   *
   * @param entry lobby list entry
   * @return parsed player id, or empty when invalid
   */
  public OptionalInt parseLobbyPlayerId(String entry) {
    if (entry == null || entry.isBlank()) {
      return OptionalInt.empty();
    }
    try {
      String idStr = entry.trim().split("\\s+")[0].replace("#", "");
      return OptionalInt.of(Integer.parseInt(idStr));
    } catch (RuntimeException e) {
      return OptionalInt.empty();
    }
  }

  /**
   * Extracts multiple player ids from lobby selections.
   *
   * @param selectedEntries selected player entries
   * @return parsed ids in selection order
   */
  public List<Integer> parseLobbyPlayerIds(List<String> selectedEntries) {
    List<Integer> ids = new ArrayList<>();
    if (selectedEntries == null) {
      return ids;
    }

    for (String entry : selectedEntries) {
      parseLobbyPlayerId(entry).ifPresent(ids::add);
    }
    return ids;
  }

  private void dispatchHostNewGame(List<Integer> ids) {
    if (ids.size() == 2) {
      newPlayerId(ids.get(1));
      return;
    }
    if (ids.size() == 3) {
      newPlayerId(ids.get(1), ids.get(2));
      return;
    }
    newPlayerId(ids.get(1), ids.get(2), ids.get(3));
  }

  /**
   * Indicates whether a pending host-start request can be dispatched.
   *
   * @param pending whether a start request is pending
   * @param playersCount number of parsed players
   * @return true when the game can be started
   */
  public static boolean shouldDispatchGameStart(boolean pending, int playersCount) {
    return pending && playersCount >= 2;
  }

  /**
   * Parses a player id from the server player map.
   *
   * @param player player information map
   * @return parsed positive id, or zero when invalid
   */
  public static int parsePlayerId(Map<String, String> player) {
    String rawId = player.getOrDefault("ID", "0");
    try {
      return Integer.parseInt(rawId);
    } catch (NumberFormatException ex) {
      return 0;
    }
  }

  /**
   * Extracts the positive player ids from server data.
   *
   * @param players player information list
   * @return sorted positive ids
   */
  public static int[] extractPositivePlayerIds(List<Map<String, String>> players) {
    return players.stream()
        .mapToInt(NetworkLobbyController::parsePlayerId)
        .filter(id -> id > 0)
        .sorted()
        .toArray();
  }

  /**
   * Dispatches a NEW command according to the number of parsed ids.
   *
   * @param manager network backend
   * @param ids parsed player ids
   * @return number of ids actually used by the command
   */
  public static int dispatchNewGame(NetworkManager manager, int[] ids) {
    if (ids.length < 2) {
      return 0;
    }
    if (ids.length == 2) {
      manager.newPlayerId(ids[1]);
      return 1;
    }
    if (ids.length == 3) {
      manager.newPlayerId(ids[1], ids[2]);
      return 2;
    }
    manager.newPlayerId(ids[1], ids[2], ids[3]);
    return 3;
  }

  private static List<Integer> collectPositivePlayerIds(List<Map<String, String>> players) {
    List<Integer> ids = new ArrayList<>();
    for (Map<String, String> player : players) {
      String rawId = player.getOrDefault("ID", "0");
      try {
        int id = Integer.parseInt(rawId);
        if (id > 0) {
          ids.add(id);
        }
      } catch (NumberFormatException ignored) {
        // Ignore malformed ids.
      }
    }
    ids.sort(Integer::compareTo);
    return ids;
  }

  /** Returns the active discovered servers. */
  public List<ServerInfo> serverList() {
    return networkManager.serverList();
  }

  /** Returns the underlying network manager. */
  public NetworkManager getNetworkManager() {
    return networkManager;
  }
}