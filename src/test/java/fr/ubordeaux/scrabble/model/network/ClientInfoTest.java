package fr.ubordeaux.scrabble.model.network;

import fr.ubordeaux.scrabble.model.network.server.ClientInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ClientInfo class. Verifies player stats and network string formatting.
 */
class ClientInfoTest {

  @Test
  void testClientInitialization() {
    // Initialize a client with ID 5
    ClientInfo client = new ClientInfo(5);

    Assertions.assertEquals(5, client.getId());
    Assertions.assertEquals("Player-5", client.getName());
    Assertions.assertEquals(PlayerStatus.IDLE, client.getStatus());
  }

  @Test
  void testStatusChange() {
    ClientInfo client = new ClientInfo(1);

    // Change status to INGAME
    client.setStatus(PlayerStatus.INGAME);
    Assertions.assertEquals(PlayerStatus.INGAME, client.getStatus());
  }

  @Test
  void testGetPlayerInfoFormatting() {
    ClientInfo client = new ClientInfo(1);
    // Default format: ID=1;NAME=Player-1;STATUS=IDLE
    String expected = "ID=1;NAME=Player-1;STATUS=IDLE";

    Assertions.assertEquals(expected, client.getPlayerInfo());
  }

  @Test
  void testGetScoreboardLineFormatting() {
    ClientInfo client = new ClientInfo(2);
    // Default stats are 0
    String expected = "ID=2;NAME=Player-2;WINS=0;LOSSES=0;TOTAL=0";

    Assertions.assertEquals(expected, client.getScoreboardLine());
  }

  @Test
  void testWinLoose() {
    ClientInfo client = new ClientInfo(2);
    client.addWin();
    Assertions.assertEquals(1, client.getWins());
    Assertions.assertEquals(1, client.getGamesPlayed());

    client.addLoose();
    Assertions.assertEquals(1, client.getLosses());
    Assertions.assertEquals(2, client.getGamesPlayed());
  }
}
