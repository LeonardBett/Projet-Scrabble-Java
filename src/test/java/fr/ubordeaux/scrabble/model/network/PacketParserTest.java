package fr.ubordeaux.scrabble.model.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the PacketParser class. Verifies command extraction and payload data splitting.
 */
class PacketParserTest {

  @Test
  void testSimpleCommand() {
    // Test a command without any data (e.g., PING) [cite: 17, 315]
    PacketParser parser = new PacketParser("PING");

    assertEquals("PING", parser.getCommand(), "Command should be PING");
    assertTrue(parser.getEntries().isEmpty(), "Entries list should be empty");
  }

  @Test
  void testCommandWithSingleEntry() {
    // Test a command with one entity and one attribute [cite: 17]
    PacketParser parser = new PacketParser("WELCOME:ID=1");

    assertEquals("WELCOME", parser.getCommand());
    assertEquals(1, parser.getEntries().size());
    assertEquals("1", parser.getEntries().getFirst().get("ID"));
  }

  @Test
  void testCommandWithMultipleAttributes() {
    // Test an entity with multiple attributes separated by ';' [cite: 17, 324]
    PacketParser parser = new PacketParser("SERVER_STATUS:PORT=12345;CLIENTS=2;GAMES=1");

    assertEquals("SERVER_STATUS", parser.getCommand());
    Map<String, String> data = parser.getEntries().get(0);
    assertEquals("12345", data.get("PORT"));
    assertEquals("2", data.get("CLIENTS"));
    assertEquals("1", data.get("GAMES"));
  }

  @Test
  void testCommandWithMultipleEntities() {
    // Test multiple entities separated by '|' (e.g., players list) [cite: 17, 325]
    PacketParser parser =
        new PacketParser("PLAYERS:ID=1;NAME=Alice;STATUS=IDLE|ID=2;NAME=Bob;STATUS=INGAME");

    assertEquals("PLAYERS", parser.getCommand());
    List<Map<String, String>> entries = parser.getEntries();
    assertEquals(2, entries.size(), "There should be 2 players");

    // Check first player data
    assertEquals("1", entries.get(0).get("ID"));
    assertEquals("Alice", entries.get(0).get("NAME"));

    // Check second player data
    assertEquals("2", entries.get(1).get("ID"));
    assertEquals("Bob", entries.get(1).get("NAME"));
  }

  @Test
  void testRobustnessWithMalformedData() {
    // Verify the parser does not crash with malformed formats [cite: 17]
    PacketParser parser = new PacketParser("CMD:");
    assertEquals("CMD", parser.getCommand());
    assertTrue(parser.getEntries().isEmpty());

    parser = new PacketParser("CMD:||");
    assertEquals("CMD", parser.getCommand());
    assertTrue(parser.getEntries().isEmpty());
  }

  @Test
  void testNullInput() {
    // Verify behavior when receiving a null message [cite: 17]
    PacketParser parser = new PacketParser(null);
    assertNull(parser.getCommand(), "Command should be null for null input");
  }
}
