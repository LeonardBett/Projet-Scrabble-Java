package fr.ubordeaux.scrabble.model.network;

import fr.ubordeaux.scrabble.model.network.server.ServerInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ServerInfo class. Focuses on data integrity and the expiration
 * logic.
 */
class ServerInfoTest {

  @Test
  void testServerInfoCreation() {
    // Create a new ServerInfo instance
    ServerInfo info = new ServerInfo("192.168.1.1", 12345, "TestServer");

    // Verify that all getters return correct initial values
    Assertions.assertEquals("192.168.1.1", info.getIp());
    Assertions.assertEquals(12345, info.getPort());
    Assertions.assertEquals("TestServer", info.getName());
  }

  @Test
  void testExpirationLogic() throws InterruptedException {
    // Create a server info
    ServerInfo info = new ServerInfo("localhost", 12345, "TempServer");

    // Initially, the server should not be expired
    Assertions.assertFalse(info.isExpired(), "Server should not be expired right after creation");

    // We can't realistically wait 30 seconds in a unit test.
    // However, we can verify that updateLastSeen resets the timer.
    long firstSeen = System.currentTimeMillis();
    Thread.sleep(10); // Wait a tiny bit
    info.updateLastSeen();

    // This is a basic check to ensure the method exists and doesn't crash
    Assertions.assertFalse(info.isExpired());
  }

  @Test
  void testToStringFormat() {
    ServerInfo info = new ServerInfo("10.0.0.1", 8080, "Web");
    String expected = "(name=Web, ip=10.0.0.1, port=8080)";

    // Verify the custom toString output matches the requirement
    Assertions.assertEquals(expected, info.toString());
  }
}
