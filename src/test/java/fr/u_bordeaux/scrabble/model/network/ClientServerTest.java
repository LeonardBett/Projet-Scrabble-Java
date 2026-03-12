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

  private static final int TEST_PORT = 12350;
  private GameServer server;
  private GameClient client;
  private TestObserver spyObserver;

  @BeforeEach
  void setUp() throws InterruptedException {
    // 1. Start the server in a separate background thread
    server = new GameServer();
    new Thread(() -> server.start(TEST_PORT)).start();

    // Allow time for the server socket to bind
    Thread.sleep(100);

    // 2. Initialize the client and attach the spy observer
    client = new GameClient();
    spyObserver = new TestObserver();
    client.addObserver(spyObserver);

    // 3. Connect the client to the local server
    client.connect("127.0.0.1", TEST_PORT);

    // Allow time for the TCP handshake and the WELCOME message to be processed
    Thread.sleep(150);
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
    // Upon connection, the server sends "WELCOME:ID=...".
    // Verify the client parses it and notifies the observer.
    Assertions.assertTrue(spyObserver.lastMessage.contains("Connected to server"));
  }

  @Test
  void testPingProtocol() throws InterruptedException {
    // Test standard PING command
    client.sendPing();
    Thread.sleep(100); // Wait for round-trip

    Assertions.assertTrue(spyObserver.lastMessage.contains("PONG TIME="));

    // Test silent PING used for heartbeats (should not trigger UI updates)
    Assertions.assertDoesNotThrow(() -> client.sendPingSilent());
  }

  // --- 2. DATA SYNCHRONIZATION TESTS ---

  @Test
  void testServerStatusRequest() throws InterruptedException {
    // Request server status and wait for the asynchronous response
    client.sendServerStatus();
    Thread.sleep(100);

    Map<String, String> status = spyObserver.lastStatus;
    Assertions.assertNotNull(status, "Client did not receive the SERVER_STATUS response");
    Assertions.assertEquals(String.valueOf(TEST_PORT), status.get("PORT"));
    Assertions.assertEquals("1", status.get("CLIENTS")); // Only our test client is connected
  }

  @Test
  void testPlayersListRequest() throws InterruptedException {
    // Request connected players list
    client.sendPlayers();
    Thread.sleep(100);

    List<Map<String, String>> players = spyObserver.lastPlayers;
    Assertions.assertNotNull(players, "Client did not receive the PLAYERS list");
    Assertions.assertEquals(1, players.size());
    Assertions.assertTrue(players.getFirst().containsKey("NAME"));
  }

  @Test
  void testScoreboardRequest() throws InterruptedException {
    // Request scoreboard statistics
    client.sendScoreboard();
    Thread.sleep(100);

    List<Map<String, String>> scoreboard = spyObserver.lastScoreboard;
    Assertions.assertNotNull(scoreboard, "Client did not receive the SCOREBOARD response");
    Assertions.assertEquals(1, scoreboard.size());
    Assertions.assertEquals("0", scoreboard.get(0).get("WINS"));
  }

  // --- 3. MULTIPLAYER & GAMEPLAY TESTS ---

  @Test
  void testRealMultiplayerInteraction() throws InterruptedException {
    // 1. Setup: Connect a second client to the server
    GameClient client2 = new GameClient();
    TestObserver observer2 = new TestObserver();
    client2.addObserver(observer2);
    client2.connect("127.0.0.1", TEST_PORT);

    // Wait for the TCP handshake to complete
    Thread.sleep(200);

    // 2. Action: Client 1 invites Client 2 (assuming Client 2 has ID 2)
    client.sendNew(2);

    // Allow time for the server to create the OnlineGame session and broadcast GAME_START
    Thread.sleep(500);

    // 3. Verification: Ensure both clients received the GAME_START command and initialized their
    // local models
    Assertions.assertTrue(
        spyObserver.localModelUpdated,
        "Client 1 should have received GAME_START and updated its local model");
    Assertions.assertTrue(
        observer2.localModelUpdated,
        "Client 2 should have received GAME_START and updated its local model");

    // 4. Action: Client 1 performs a "PASS" move
    client.sendPassMove();

    // Allow time for the server to process the move and broadcast the update to all participants
    Thread.sleep(200);

    // 5. Final Verification: Check if Client 2's local model successfully updated the turn order
    // In a 2-player game, after Player-1 passes, the current player must be Player-2
    Assertions.assertEquals(
        "Player-2",
        client2.getLocalGame().getCurrentPlayer().getName(),
        "Client 2's local game should have advanced the turn to Player-2");

    // Cleanup: Disconnect the second client
    client2.quit();
  }

  // --- 4. LIFECYCLE & ERROR HANDLING TESTS ---

  @Test
  void testObserverRemoval() {
    // Removing an existing observer
    Assertions.assertDoesNotThrow(() -> client.removeObserver(spyObserver));

    // Removing an observer not in the list should handle gracefully (prints to stderr)
    Assertions.assertDoesNotThrow(() -> client.removeObserver(spyObserver));
  }

  @Test
  void testDisconnectionSafety() {
    client.quit();

    // Attempting to send messages after disconnection should not crash the application
    Assertions.assertDoesNotThrow(() -> client.sendPing());

    // Double quit should be safe
    Assertions.assertDoesNotThrow(() -> client.quit());
  }

  // =================================================================================
  // UTILITY CLASS: Spy Observer to capture asynchronous server responses
  // =================================================================================
  private static class TestObserver implements NetworkObserver {

    // Volatile because they will be accessed by 2 different threads
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
