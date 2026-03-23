package fr.ubordeaux.scrabble.model.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a parsed network packet for command and data extraction.
 */
public class PacketParser {

  // The command name (ex: PLAYERS, PING, ...)
  private final String command; // The command name (ex: PLAYERS, PING, ...)

  // List containing all the data from the payload of the packet
  // Each element of this List is a Map of data for a specific entity
  // (ex : [{ "ID": "1", "NAME": "Alice", "STATUS": "IDLE" },
  // { "ID": "2", "NAME": "Bob", "STATUS": "INGAME" }]
  private final List<Map<String, String>> entries = new ArrayList<>();

  /**
   * Create an instance of the PacketParser class It will be parsed automatically, no need to call a
   * method for that.
   *
   * @param rawMessage the raw message receive by the client form the server
   */
  public PacketParser(String rawMessage) {
    // We check if this raw message is a simple world (ex: PONG for the command
    // PING)
    if (rawMessage == null || !rawMessage.contains(":")) {
      this.command = rawMessage;
      return;
    }

    // We parse the command name and the data payload
    String[] parts = rawMessage.split(":", 2);
    this.command = parts[0];
    parsePayload(parts[1]);
  }

  // Intern method for parsing the payload of the packet
  private void parsePayload(String payload) {
    // We split different entities separated by | (ex: Alice|Bob)
    // We need \\ because | is a special character in regex
    String[] entities = payload.split("\\|");

    for (String entity : entities) {
      if (entity.isEmpty()) {
        continue;
      }

      // Map of attribute of an entity (ex: {ID=1, NAME=Alice})
      Map<String, String> entityData = new HashMap<>();
      // We split different attributes of a specific entity with ; (ex:
      // {ID=1;NAME=Alice})
      String[] pairs = entity.split(";");

      for (String pair : pairs) {
        // We split each attribute with = (ex: ID=1) and add them to the map
        String[] kv = pair.split("=", 2);
        if (kv.length == 2) {
          entityData.put(kv[0], kv[1]);
        }
      }
      // We add the formed map to the list
      entries.add(entityData);
    }
  }

  /**
   * Return the command name of this packet.
   *
   * @return the command name (ex: PLAYERS, PING, ...)
   */
  public String getCommand() {
    return command;
  }

  /**
   * Returns all entries (needed for lists like PLAYERS or SCOREBOARD).
   *
   * @return the list of all entries
   */
  public List<Map<String, String>> getEntries() {
    return entries;
  }

  @Override
  public String toString() {
    return "PacketParser [command=" + command + ", entries=" + entries + "]";
  }
}
