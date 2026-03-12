package fr.u_bordeaux.scrabble.model.network;

import fr.u_bordeaux.scrabble.model.network.server.ServerInfo;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DiscoveryServiceTest {

  private DiscoveryService discoveryService;

  @BeforeEach
  void setUp() throws InterruptedException {
    // OS needs time to release the UDP port between tests
    Thread.sleep(150);
    discoveryService = new DiscoveryService();
  }

  @AfterEach
  void tearDown() {
    discoveryService.stopListening();
    discoveryService.stopBroadcasting();
  }

  @Test
  void testServerDiscoveryViaFakePacket() throws Exception {
    discoveryService.startListening();
    Thread.sleep(200); // Wait for the listening thread to bind

    String message = "SCRABBLE_SERVER;TestJUnit;12345";
    byte[] buffer = message.getBytes();

    // Use the same broadcast address as the service
    InetAddress broadcastAddr = InetAddress.getByName("255.255.255.255");

    try (DatagramSocket socket = new DatagramSocket()) {
      socket.setBroadcast(true); // REQUIRED to send broadcast packets
      DatagramPacket packet =
          new DatagramPacket(buffer, buffer.length, broadcastAddr, NetworkManager.DEFAULT_UDP_PORT);
      socket.send(packet);
    }

    // Polling loop: check every 50ms for up to 1 second
    boolean found = false;
    for (int i = 0; i < 20; i++) {
      if (!discoveryService.getActiveServer().isEmpty()) {
        found = true;
        break;
      }
      Thread.sleep(50);
    }

    List<ServerInfo> servers = discoveryService.getActiveServer();
    Assertions.assertTrue(
        found, "Server list remained empty. UDP packet was likely blocked or dropped.");
    Assertions.assertEquals("TestJUnit", servers.getFirst().getName());
  }

  @Test
  void testObserverNotification() throws Exception {
    AtomicBoolean notificationReceived = new AtomicBoolean(false);

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
          public void gameEndedUpdate(String r) {}

          @Override
          public void serverStatusUpdate(java.util.Map<String, String> i) {}

          @Override
          public void playersUpdate(java.util.List<java.util.Map<String, String>> p) {}

          @Override
          public void scoreboardUpdate(java.util.List<java.util.Map<String, String>> s) {}

          @Override
          public void messageUpdate(String m) {}
        };

    discoveryService.addObserver(observer);
    discoveryService.startListening();
    Thread.sleep(200);

    String message = "SCRABBLE_SERVER;ObserverTest;12345";
    InetAddress broadcastAddr = InetAddress.getByName("255.255.255.255");

    try (DatagramSocket socket = new DatagramSocket()) {
      socket.setBroadcast(true);
      DatagramPacket packet =
          new DatagramPacket(
              message.getBytes(), message.length(), broadcastAddr, NetworkManager.DEFAULT_UDP_PORT);
      socket.send(packet);
    }

    // Polling loop for notification
    for (int i = 0; i < 20; i++) {
      if (notificationReceived.get()) {
        break;
      }
      Thread.sleep(50);
    }

    Assertions.assertTrue(notificationReceived.get(), "Observer was not notified after broadcast.");
  }

  @Test
  void testBroadcastingSendsCorrectPacket() throws Exception {
    // Bind the spy socket with REUSE_ADDRESS
    DatagramSocket spySocket = new DatagramSocket(null);
    spySocket.setReuseAddress(true);
    spySocket.bind(
        new InetSocketAddress(InetAddress.getByName("0.0.0.0"), NetworkManager.DEFAULT_UDP_PORT));

    try (spySocket) {
      spySocket.setSoTimeout(3000);
      byte[] buffer = new byte[1024];
      DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);

      // We broadcast on all interfaces so our spy on 0.0.0.0 catches it
      discoveryService.startBroadcasting("TestServer", 12345, "0.0.0.0");

      spySocket.receive(receivedPacket);
      String message = new String(receivedPacket.getData(), 0, receivedPacket.getLength());

      Assertions.assertTrue(message.startsWith("SCRABBLE_SERVER"));
      Assertions.assertTrue(message.contains("TestServer"));
    } finally {
      discoveryService.stopBroadcasting();
    }
  }
}
