package fr.ubordeaux.scrabble.controller.network;

import fr.ubordeaux.scrabble.model.network.NetworkManager;
import java.util.Locale;
import java.util.OptionalInt;

/**
 * Handles parsing and dispatching of CLI network commands.
 */
public class CliNetworkCommandController {

  /** Result for direct join commands (join [ip] [port]). */
  public enum DirectJoinResult {
    /** Input was not in direct-join format. */
    NOT_A_DIRECT_JOIN,
    /** Direct join command was parsed and sent. */
    JOINED,
    /** Port token was invalid. */
    INVALID_PORT
  }

  /** Result for invitation commands (new [id] [id] [id]). */
  public enum InvitationResult {
    /** Invitation command was parsed and sent. */
    SENT,
    /** Invitation command payload was invalid. */
    INVALID
  }

  /** Result for play command dispatch (play [pos][dir] [word]). */
  public enum PlayResult {
    /** Play command was parsed and sent. */
    SENT,
    /** Play command payload was invalid. */
    INVALID
  }

  private final NetworkManager networkManager;

  /**
   * Creates a command controller for CLI network parsing/dispatch.
   *
   * @param networkManager network backend used to dispatch parsed commands
   */
  public CliNetworkCommandController(NetworkManager networkManager) {
    this.networkManager = networkManager;
  }

  /**
   * Parses the trailing integer token from a command string.
   *
   * @param value command text
   * @return parsed integer when valid, empty otherwise
   */
  public OptionalInt parseTrailingInt(String value) {
    String[] tokens = value.trim().split("\\s+");
    if (tokens.length == 0) {
      return OptionalInt.empty();
    }

    String maybeNumber = tokens[tokens.length - 1];
    try {
      return OptionalInt.of(Integer.parseInt(maybeNumber));
    } catch (NumberFormatException e) {
      return OptionalInt.empty();
    }
  }

  /**
   * Parses and dispatches a direct join command.
   *
   * @param raw command text
   * @return dispatch result
   */
  public DirectJoinResult tryDirectJoin(String raw) {
    String[] tokens = raw.trim().split("\\s+");
    if (tokens.length < 3) {
      return DirectJoinResult.NOT_A_DIRECT_JOIN;
    }

    String address = tokens[1];
    try {
      int port = Integer.parseInt(tokens[2]);
      networkManager.join(address, port);
      return DirectJoinResult.JOINED;
    } catch (NumberFormatException e) {
      return DirectJoinResult.INVALID_PORT;
    }
  }

  /**
   * Parses and dispatches player info request command.
   *
   * @param raw command text
   * @return true when valid and dispatched
   */
  public boolean handlePlayerInfo(String raw) {
    OptionalInt id = parseTrailingInt(raw);
    if (id.isEmpty() || id.getAsInt() <= 0) {
      return false;
    }
    networkManager.playersPlayerId(id.getAsInt());
    return true;
  }

  /**
   * Parses and dispatches invitation command.
   *
   * @param raw command text
   * @return invitation dispatch result
   */
  public InvitationResult handleNewInvitation(String raw) {
    String[] tokens = raw.trim().split("\\s+");
    if (tokens.length < 2 || tokens.length > 4) {
      return InvitationResult.INVALID;
    }

    try {
      int id1 = Integer.parseInt(tokens[1]);
      if (tokens.length == 2) {
        networkManager.newPlayerId(id1);
        return InvitationResult.SENT;
      }

      int id2 = Integer.parseInt(tokens[2]);
      if (tokens.length == 3) {
        networkManager.newPlayerId(id1, id2);
        return InvitationResult.SENT;
      }

      int id3 = Integer.parseInt(tokens[3]);
      networkManager.newPlayerId(id1, id2, id3);
      return InvitationResult.SENT;
    } catch (NumberFormatException e) {
      return InvitationResult.INVALID;
    }
  }

  /**
   * Parses and dispatches an explicit "play" command.
   *
   * @param raw command text
   * @return play dispatch result
   */
  public PlayResult handlePlay(String raw) {
    String payload = raw.substring("play".length()).trim();
    String[] parts = payload.split("\\s+");
    if (parts.length < 2) {
      return PlayResult.INVALID;
    }

    String posDir = parts[0];
    if (posDir.length() < 3) {
      return PlayResult.INVALID;
    }

    char dirChar = Character.toUpperCase(posDir.charAt(posDir.length() - 1));
    String direction = dirChar == 'V' ? "V" : "H";
    String pos = posDir.substring(0, posDir.length() - 1).toLowerCase(Locale.ROOT);

    int x;
    int y;
    try {
      if (pos.matches("[a-o]\\d+")) {
        y = pos.charAt(0) - 'a' + 1;
        x = Integer.parseInt(pos.substring(1));
      } else if (pos.matches("\\d+[a-o]")) {
        y = pos.charAt(pos.length() - 1) - 'a' + 1;
        x = Integer.parseInt(pos.substring(0, pos.length() - 1));
      } else {
        return PlayResult.INVALID;
      }
    } catch (NumberFormatException e) {
      return PlayResult.INVALID;
    }

    String word = String.join("", java.util.Arrays.copyOfRange(parts, 1, parts.length))
        .toUpperCase(Locale.ROOT);
    networkManager.play(x, y, direction, word);
    return PlayResult.SENT;
  }

  /**
   * Parses and dispatches short CLI notation command (for example: a1v test).
   *
   * @param raw command text
   * @return true when parsed and dispatched
   */
  public boolean tryCliPlayNotation(String raw) {
    String[] parts = raw.trim().split("\\s+");
    if (parts.length < 2) {
      return false;
    }

    String posDir = parts[0];
    if (posDir.length() < 3) {
      return false;
    }

    char dirChar = Character.toUpperCase(posDir.charAt(posDir.length() - 1));
    if (dirChar != 'H' && dirChar != 'G' && dirChar != 'V') {
      return false;
    }

    String pos = posDir.substring(0, posDir.length() - 1).toLowerCase(Locale.ROOT);
    int x;
    int y;

    try {
      if (pos.matches("[a-o]\\d+")) {
        y = pos.charAt(0) - 'a' + 1;
        x = Integer.parseInt(pos.substring(1));
      } else if (pos.matches("\\d+[a-o]")) {
        y = pos.charAt(pos.length() - 1) - 'a' + 1;
        x = Integer.parseInt(pos.substring(0, pos.length() - 1));
      } else {
        return false;
      }
    } catch (NumberFormatException e) {
      return false;
    }

    String direction = dirChar == 'V' ? "V" : "H";
    String word = String.join("", java.util.Arrays.copyOfRange(parts, 1, parts.length))
        .toUpperCase(Locale.ROOT);
    networkManager.play(x, y, direction, word);
    return true;
  }
}