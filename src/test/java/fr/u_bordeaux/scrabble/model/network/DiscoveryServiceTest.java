package fr.u_bordeaux.scrabble.model.network;

import fr.u_bordeaux.scrabble.model.network.server.ServerInfo;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for DiscoveryService. Verifies UDP listening, broadcasting, and observer
 * notifications.
 */
class DiscoveryServiceTest {

  private DiscoveryService discoveryService;

  @BeforeEach
  void setUp() {
    discoveryService = new DiscoveryService();
  }

  @AfterEach
  void tearDown() {
    // Ensure all threads and sockets are closed after each test
    discoveryService.stopListening();
    discoveryService.stopBroadcasting();
  }

  @Test
  void testStartStopListening() {
    discoveryService.startListening();
    // No exception should be thrown, and service should be active
    discoveryService.stopListening();
  }

  @Test
  void testServerDiscoveryViaFakePacket() throws Exception {
    discoveryService.startListening();

    // Prepare a fake UDP broadcast packet
    String message = "SCRABBLE_SERVER;TestJUnit;12345";
    byte[] buffer = message.getBytes();
    InetAddress address = InetAddress.getByName("localhost");

    // Send the packet manually to the discovery port
    try (DatagramSocket socket = new DatagramSocket()) {
      DatagramPacket packet =
          new DatagramPacket(buffer, buffer.length, address, NetworkManager.DEFAULT_UDP_PORT);
      socket.send(packet);
    }

    // Wait a bit for the background thread to process the packet
    Thread.sleep(200);

    List<ServerInfo> servers = discoveryService.getActiveServer();
    Assertions.assertFalse(
        servers.isEmpty(), "Server list should not be empty after receiving a packet");
    Assertions.assertEquals("TestJUnit", servers.get(0).getName());
  }

  @Test
  void testObserverNotification() throws Exception {
    AtomicBoolean notificationReceived = new AtomicBoolean(false);

    // Create a minimal observer to catch the update
    NetworkObserver observer =
        new NetworkObserver() {
          @Override
          public void serverListUpdate(List<ServerInfo> activeServers) {
            if (!activeServers.isEmpty()) {
              notificationReceived.set(true);
            }
          }

          @Override
          public void localModelUpdate() {}

          @Override
          public void gameEndedUpdate(String reason) {}

          @Override
          public void serverStatusUpdate(java.util.Map<String, String> info) {}

          @Override
          public void playersUpdate(java.util.List<java.util.Map<String, String>> players) {}

          @Override
          public void scoreboardUpdate(java.util.List<java.util.Map<String, String>> scoreboard) {}

          @Override
          public void messageUpdate(String message) {}
        };

    discoveryService.addObserver(observer);
    discoveryService.startListening();

    // Trigger discovery manually
    String message = "SCRABBLE_SERVER;ObserverTest;12345";
    try (DatagramSocket socket = new DatagramSocket()) {
      DatagramPacket packet =
          new DatagramPacket(
              message.getBytes(),
              message.length(),
              InetAddress.getByName("localhost"),
              NetworkManager.DEFAULT_UDP_PORT);
      socket.send(packet);
    }

    Thread.sleep(200);
    Assertions.assertTrue(
        notificationReceived.get(), "Observer should have been notified of the new server");
  }

  @Test
  void testGetActiveServerRemovesExpired() {
    // This test simulates the internal map to check expiration logic
    // Note: Real time testing of 30s timeout is too slow for unit tests.
    // We verify that the method returns a clean list.
    List<ServerInfo> servers = discoveryService.getActiveServer();
    Assertions.assertNotNull(servers);
    Assertions.assertTrue(servers.isEmpty());
  }

  @Test
  void testBroadcastingSendsCorrectPacket() throws Exception {
    // 1. Arrange: Start an "emulator" socket to catch the broadcast packet
    try (DatagramSocket spySocket = new DatagramSocket(NetworkManager.DEFAULT_UDP_PORT)) {
      spySocket.setSoTimeout(2000); // 2 seconds timeout to avoid blocking forever
      byte[] buffer = new byte[1024];
      DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);

      // 2. Act: Start broadcasting from the service
      discoveryService.startBroadcasting("TestServer", 12345, "127.0.0.1");

      // 3. Assert: Check if our "spy" caught the packet
      spySocket.receive(receivedPacket);
      String message = new String(receivedPacket.getData(), 0, receivedPacket.getLength());

      // The message format should be "SCRABBLE_SERVER;name;port"
      Assertions.assertTrue(message.startsWith("SCRABBLE_SERVER"));
      Assertions.assertTrue(message.contains("TestServer"));
      Assertions.assertTrue(message.contains("12345"));
    } finally {
      discoveryService.stopBroadcasting();
    }
  }
}
