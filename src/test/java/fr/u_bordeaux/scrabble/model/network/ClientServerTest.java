package fr.u_bordeaux.scrabble.model.network;

import fr.u_bordeaux.scrabble.model.network.client.GameClient;
import fr.u_bordeaux.scrabble.model.network.server.GameServer;
import fr.u_bordeaux.scrabble.model.network.server.ServerInfo;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Deep integration test for GameClient and GameServer. Validates the complete network protocol,
 * data serialization, and thread management.
 */
class ClientServerTest {

  // Static counter to use a different TCP port for EACH test.
  // Prevents "Address already in use" (TIME_WAIT) crashes on Linux and CI environments.
  private static int portCounter = 12350;

  private int currentTestPort;
  private GameServer server;
  private GameClient client;
  private TestObserver spyObserver;

  @BeforeEach
  void setUp() throws InterruptedException {
    // 1. Define a new port for this specific test
    currentTestPort = portCounter++;

    // 2. Start the server in a separate background thread
    server = new GameServer();
    new Thread(() -> server.start(currentTestPort)).start();

    // Slightly increased delay to allow the OS to bind the socket
    Thread.sleep(1500);

    // 3. Initialize the client and attach the spy observer
    client = new GameClient();
    spyObserver = new TestObserver();
    client.addObserver(spyObserver);

    // 4. Connect the client to the local server
    client.connect("127.0.0.1", currentTestPort);

    // Polling: wait (max 1 second) for the client to confirm its connection
    for (int i = 0; i < 50; i++) {
      if (spyObserver.lastMessage.contains("Connected to server")) {
        break;
      }
      Thread.sleep(50);
    }
  }

  @AfterEach
  void tearDown() {
    // Always disconnect the client before stopping the server to ensure clean socket closure
    if (client != null) {
      client.quit();
    }
    if (server != null) {
      server.stop();
    }
  }

  // --- 1. CONNECTION & BASIC PROTOCOL TESTS ---

  @Test
  void testConnectionAndWelcomeMessage() {
    Assertions.assertTrue(
        spyObserver.lastMessage.contains("Connected to server"),
        "The WELCOME message was not received");
  }

  @Test
  void testPingProtocol() throws InterruptedException {
    client.sendPing();

    // Polling to wait for the PONG response
    for (int i = 0; i < 50; i++) {
      if (spyObserver.lastMessage.contains("PONG TIME=")) {
        break;
      }
      Thread.sleep(50);
    }
    Assertions.assertTrue(spyObserver.lastMessage.contains("PONG TIME="));
    Assertions.assertDoesNotThrow(() -> client.sendPingSilent());
  }

  // --- 2. DATA SYNCHRONIZATION TESTS ---

  @Test
  void testServerStatusRequest() throws InterruptedException {
    client.sendServerStatus();

    // Wait for the asynchronous response
    for (int i = 0; i < 50 && spyObserver.lastStatus == null; i++) {
      Thread.sleep(50);
    }

    Map<String, String> status = spyObserver.lastStatus;
    Assertions.assertNotNull(status, "Client did not receive the SERVER_STATUS response");
    Assertions.assertEquals(String.valueOf(currentTestPort), status.get("PORT"));
    Assertions.assertEquals("1", status.get("CLIENTS"));
  }

  @Test
  void testPlayersListRequest() throws InterruptedException {
    client.sendPlayers();

    // Wait for the asynchronous response
    for (int i = 0; i < 50 && spyObserver.lastPlayers == null; i++) {
      Thread.sleep(50);
    }

    List<Map<String, String>> players = spyObserver.lastPlayers;
    Assertions.assertNotNull(players, "Client did not receive the PLAYERS list");
    Assertions.assertEquals(1, players.size());
    Assertions.assertTrue(players.getFirst().containsKey("NAME"));
  }

  @Test
  void testScoreboardRequest() throws InterruptedException {
    client.sendScoreboard();

    // Wait for the asynchronous response
    for (int i = 0; i < 50 && spyObserver.lastScoreboard == null; i++) {
      Thread.sleep(50);
    }

    List<Map<String, String>> scoreboard = spyObserver.lastScoreboard;
    Assertions.assertNotNull(scoreboard, "Client did not receive the SCOREBOARD response");
    Assertions.assertEquals(1, scoreboard.size());
    Assertions.assertEquals("0", scoreboard.get(0).get("WINS"));
  }

  // --- 3. MULTIPLAYER & GAMEPLAY TESTS ---

  @Test
  void testRealMultiplayerInteraction() throws InterruptedException {
    // Setup: Connect a second client to the server
    GameClient client2 = new GameClient();
    TestObserver observer2 = new TestObserver();
    client2.addObserver(observer2);
    client2.connect("127.0.0.1", currentTestPort);

    // Wait for the second client to connect
    for (int i = 0; i < 50 && !observer2.lastMessage.contains("Connected to server"); i++) {
      Thread.sleep(50);
    }
    // Client 1 invites Client 2
    client.sendNew(2);

    // Wait for GAME_START for both clients
    for (int i = 0;
        i < 50 && (!spyObserver.localModelUpdated || !observer2.localModelUpdated);
        i++) {
      Thread.sleep(50);
    }

    Assertions.assertTrue(
        spyObserver.localModelUpdated, "Client 1 should have received GAME_START");
    Assertions.assertTrue(observer2.localModelUpdated, "Client 2 should have received GAME_START");

    // Client 1 passes their turn
    client.sendPassMove();

    // Wait for the turn update on client 2's model
    for (int i = 0; i < 50; i++) {
      if (client2.getLocalGame() != null
          && "Player-2".equals(client2.getLocalGame().getCurrentPlayer().getName())) {
        break;
      }
      Thread.sleep(200);
    }

    Assertions.assertEquals(
        "Player-2",
        client2.getLocalGame().getCurrentPlayer().getName(),
        "Client 2's local game should have advanced the turn to Player-2");

    client2.quit();
  }

  // --- 4. LIFECYCLE & ERROR HANDLING TESTS ---

  @Test
  void testObserverRemoval() {
    Assertions.assertDoesNotThrow(() -> client.removeObserver(spyObserver));
    Assertions.assertDoesNotThrow(() -> client.removeObserver(spyObserver));
  }

  @Test
  void testDisconnectionSafety() {
    client.quit();
    Assertions.assertDoesNotThrow(() -> client.sendPing());
    Assertions.assertDoesNotThrow(() -> client.quit());
  }

  // =================================================================================
  // UTILITY CLASS: Spy Observer
  // =================================================================================
  private static class TestObserver implements NetworkObserver {

    volatile String lastMessage = "";
    volatile Map<String, String> lastStatus = null;
    volatile List<Map<String, String>> lastPlayers = null;
    volatile List<Map<String, String>> lastScoreboard = null;
    volatile boolean localModelUpdated = false;

    @Override
    public void messageUpdate(String message) {
      this.lastMessage = message;
    }

    @Override
    public void serverStatusUpdate(Map<String, String> info) {
      this.lastStatus = info;
    }

    @Override
    public void playersUpdate(List<Map<String, String>> players) {
      this.lastPlayers = players;
    }

    @Override
    public void scoreboardUpdate(List<Map<String, String>> scoreboard) {
      this.lastScoreboard = scoreboard;
    }

    @Override
    public void localModelUpdate() {
      this.localModelUpdated = true;
    }

    @Override
    public void gameEndedUpdate(String reason) {}

    @Override
    public void serverListUpdate(List<ServerInfo> activeServers) {}
  }
}
