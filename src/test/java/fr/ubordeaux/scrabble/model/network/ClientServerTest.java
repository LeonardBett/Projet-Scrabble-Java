package fr.ubordeaux.scrabble.model.network;

import fr.ubordeaux.scrabble.model.network.client.GameClient;
import fr.ubordeaux.scrabble.model.network.server.GameServer;
import fr.ubordeaux.scrabble.model.network.server.ServerInfo;
import fr.ubordeaux.scrabble.model.utils.GameLogger;
import java.io.IOException;
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
  void setUp() throws InterruptedException, IOException {
    // 1. Define a new port for this specific test
    currentTestPort = portCounter++;

    // 2. Start the server
    server = new GameServer();
    server.start(currentTestPort);

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

    GameLogger.setVerbose(false);
    GameLogger.setDebug(false);
  }

  @AfterEach
  void tearDown() {
    // Always disconnect the client before stopping the server to ensure clean
    // socket closure
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
    Assertions.assertEquals("0", scoreboard.getFirst().get("WINS"));
  }

  @Test
  void testSpecificPlayerDetailsRequest() throws InterruptedException {
    client.sendPlayersPlayerId(1); // Demande les infos du joueur 1

    // Attend la réponse asynchrone
    for (int i = 0; i < 50 && spyObserver.lastPlayerDetails == null; i++) {
      Thread.sleep(50);
    }

    Map<String, String> details = spyObserver.lastPlayerDetails;
    Assertions.assertNotNull(details, "Client did not receive the detailed player info");
    Assertions.assertEquals("1", details.get("ID"));
    Assertions.assertEquals("Player-1", details.get("NAME"));
    Assertions.assertEquals("IDLE", details.get("STATUS"));
    Assertions.assertTrue(details.containsKey("WINS"));
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

    // Wait for Client 2 to receive the invitation
    for (int i = 0; i < 50 && observer2.invitationFrom == null; i++) {
      Thread.sleep(50);
    }

    Assertions.assertNotNull(
        observer2.invitationFrom, "Client 2 should have received an invitation");
    Assertions.assertEquals(
        "Player-1", observer2.invitationFrom, "Invitation should be from Player-1");

    // Client 2 accepts the invitation
    client2.sendAccept();

    // Wait for GAME_START for both clients
    for (int i = 0;
        i < 50 && (!spyObserver.localModelUpdated || !observer2.localModelUpdated);
        i++) {
      Thread.sleep(50);
    }

    Assertions.assertTrue(
        spyObserver.localModelUpdated,
        "Client 1 should have received GAME_START and updated model");
    Assertions.assertTrue(
        observer2.localModelUpdated, "Client 2 should have received GAME_START and updated model");

    // Wait a bit for SET_RACK to be processed for both clients
    Thread.sleep(200);

    // Client 1 passes their turn
    client.sendPassMove();

    // Wait for the turn update on client 2's model
    boolean turnAdvanced = false;
    for (int i = 0; i < 50; i++) {
      if (client2.getLocalGame() != null
          && "Player-2".equals(client2.getLocalGame().getCurrentPlayer().getName())) {
        turnAdvanced = true;
        break;
      }
      Thread.sleep(200);
    }

    Assertions.assertTrue(
        turnAdvanced, "Client 2's local game should have advanced the turn to Player-2");

    client2.quit();
  }

  @Test
  void testMultiplayerInvitationWithMixedResponses() throws InterruptedException {
    // 1. Setup: Connect three more clients (total of 4)
    GameClient client2 = new GameClient();
    TestObserver observer2 = new TestObserver();
    client2.addObserver(observer2);
    client2.connect("127.0.0.1", currentTestPort);
    Thread.sleep(50); // Ensure sequential connection for predictable IDs

    GameClient client3 = new GameClient();
    TestObserver observer3 = new TestObserver();
    client3.addObserver(observer3);
    client3.connect("127.0.0.1", currentTestPort);
    Thread.sleep(50);

    GameClient client4 = new GameClient();
    TestObserver observer4 = new TestObserver();
    client4.addObserver(observer4);
    client4.connect("127.0.0.1", currentTestPort);

    // Wait for all clients to connect completely
    for (int i = 0; i < 50; i++) {
      if (observer2.lastMessage.contains("Connected")
          && observer3.lastMessage.contains("Connected")
          && observer4.lastMessage.contains("Connected")) {
        break;
      }
      Thread.sleep(50);
    }

    // Wait for server to process connections internally
    Thread.sleep(200);

    // 2. Client 1 (host) invites clients 2, 3, and 4
    client.sendNew(2, 3, 4);

    // 3. Wait for all clients to receive the invitation and update their status
    for (int i = 0;
        i < 50
            && (spyObserver.lastPlayerStatus == null
                || observer2.lastPlayerStatus == null
                || observer3.lastPlayerStatus == null
                || observer4.lastPlayerStatus == null);
        i++) {
      Thread.sleep(50);
    }
    Assertions.assertEquals(
        "WAITGAME", spyObserver.lastPlayerStatus, "Host status should be WAITGAME");
    Assertions.assertEquals(
        "WAITGAME", observer2.lastPlayerStatus, "Client 2 status should be WAITGAME");
    Assertions.assertEquals(
        "WAITGAME", observer3.lastPlayerStatus, "Client 3 status should be WAITGAME");
    Assertions.assertEquals(
        "WAITGAME", observer4.lastPlayerStatus, "Client 4 status should be WAITGAME");

    // 4. Process responses
    client2.sendAccept();
    client3.sendDecline();
    client4.sendAccept();

    // 5. Wait for the game to start for the host and accepting players
    for (int i = 0;
        i < 50
            && (!spyObserver.localModelUpdated
                || !observer2.localModelUpdated
                || !observer4.localModelUpdated);
        i++) {
      Thread.sleep(50);
    }

    // 6. Assertions
    Assertions.assertTrue(
        spyObserver.localModelUpdated, "Host (Client 1) should have started the game");
    Assertions.assertTrue(
        observer2.localModelUpdated, "Client 2 (accepted) should have started the game");
    Assertions.assertFalse(
        observer3.localModelUpdated, "Client 3 (declined) should NOT have started the game");
    Assertions.assertTrue(
        observer4.localModelUpdated, "Client 4 (accepted) should have started the game");

    // Verify that the host received the decline message from client 3
    Assertions.assertTrue(
        spyObserver.invitationDeclinedBy.contains("Player-3"),
        "Host should be notified that Player-3 declined");

    // Verify that client 3's status returned to IDLE
    Assertions.assertEquals(
        "IDLE", observer3.lastPlayerStatus, "Client 3 status should be IDLE after declining");

    // Cleanup
    client2.quit();
    client3.quit();
    client4.quit();
  }

  @Test
  void testInvitationLifecycleAndErrors() throws InterruptedException {
    // 1. Setup: Connect two more clients
    GameClient client2 = new GameClient();
    TestObserver observer2 = new TestObserver();
    client2.addObserver(observer2);
    client2.connect("127.0.0.1", currentTestPort);

    GameClient client3 = new GameClient();
    TestObserver observer3 = new TestObserver();
    client3.addObserver(observer3);
    client3.connect("127.0.0.1", currentTestPort);

    for (int i = 0; i < 50; i++) {
      if (observer2.lastMessage.contains("Connected")
          && observer3.lastMessage.contains("Connected")) {
        break;
      }
      Thread.sleep(50);
    }
    Thread.sleep(200);

    // --- SCENARIO A: Host cancels the invitation ---
    // Client 1 invites Client 2
    client.sendNew(2);

    // Wait for invitation to be received
    for (int i = 0; i < 50 && observer2.invitationFrom == null; i++) {
      Thread.sleep(50);
    }

    // Client 1 cancels
    client.sendCancel();

    // Wait for cancellation to be processed
    for (int i = 0; i < 50 && observer2.invitationCancelledReason == null; i++) {
      Thread.sleep(50);
    }

    // Assertions
    Assertions.assertTrue(
        spyObserver.lastMessage.contains("Invitation successfully cancelled."),
        "Host should receive cancellation confirmation");
    Assertions.assertEquals(
        "Host cancelled",
        observer2.invitationCancelledReason,
        "Client 2 should be notified that host cancelled");
    Assertions.assertEquals(
        "IDLE", spyObserver.lastPlayerStatus, "Host status should return to IDLE");
    Assertions.assertEquals(
        "IDLE", observer2.lastPlayerStatus, "Client 2 status should return to IDLE");

    // --- SCENARIO B: Player tries to accept without a pending invitation ---
    client2.sendAccept();

    // Wait a bit for the error message
    Thread.sleep(200);
    Assertions.assertTrue(
        observer2.lastMessage.contains("ERROR: No pending invitation"),
        "Client 2 should receive an error for accepting without invitation");

    // --- SCENARIO C: Player tries to invite a busy player ---
    // Start a game between Client 1 and Client 2 to make them busy
    observer2.reset(); // RESET DE L'ÉTAT POUR ÉVITER LA RACE CONDITION !
    spyObserver.reset();

    client.sendNew(2);
    for (int i = 0; i < 50 && observer2.invitationFrom == null; i++) {
      Thread.sleep(50); // wait for re-invitation
    }
    client2.sendAccept();

    // Wait for game to start
    for (int i = 0; i < 50 && !spyObserver.localModelUpdated; i++) {
      Thread.sleep(50);
    }

    // Client 3 tries to invite the now-busy Client 2
    client3.sendNew(2);

    // Wait for error message
    Thread.sleep(200);
    Assertions.assertTrue(
        observer3.lastMessage.contains("ERROR: Player Player-2 is busy"),
        "Client 3 should receive an error for inviting a busy player");

    // Cleanup
    client2.quit();
    client3.quit();
  }

  @Test
  void testAwayStatusAndInviteRejection() throws InterruptedException {
    // Setup Client 2
    GameClient client2 = new GameClient();
    TestObserver observer2 = new TestObserver();
    client2.addObserver(observer2);
    client2.connect("127.0.0.1", currentTestPort);

    for (int i = 0; i < 50 && !observer2.lastMessage.contains("Connected"); i++) {
      Thread.sleep(50);
    }

    // 1. Client 2 se met en mode AWAY
    client2.sendAway();
    for (int i = 0; i < 50 && !"AWAY".equals(observer2.lastPlayerStatus); i++) {
      Thread.sleep(50);
    }
    Assertions.assertEquals("AWAY", observer2.lastPlayerStatus);

    // 2. Client 1 essaie d'inviter Client 2 (qui est AWAY)
    client.sendNew(2);
    for (int i = 0; i < 50 && !spyObserver.lastMessage.contains("Player Player-2 is busy"); i++) {
      Thread.sleep(50);
    }
    Assertions.assertTrue(
        spyObserver.lastMessage.contains("ERROR: Player Player-2 is busy"),
        "Client 1 should not be able to invite an AWAY player");

    // 3. Client 2 revient en IDLE
    client2.sendBack();
    for (int i = 0; i < 50 && !"IDLE".equals(observer2.lastPlayerStatus); i++) {
      Thread.sleep(50);
    }
    Assertions.assertEquals("IDLE", observer2.lastPlayerStatus);

    // 4. Client 1 réessaie, ça doit marcher (Waitgame)
    spyObserver.reset(); // On clean l'historique de l'observer
    client.sendNew(2);
    for (int i = 0; i < 50 && !"WAITGAME".equals(spyObserver.lastPlayerStatus); i++) {
      Thread.sleep(50);
    }
    Assertions.assertEquals(
        "WAITGAME", spyObserver.lastPlayerStatus, "Client 1 should now be able to invite Client 2");

    client2.quit();
  }

  @Test
  void testHostDisconnectionDuringInvitation() throws InterruptedException {
    // 1. Setup: On connecte le Client 2 proprement
    GameClient client2 = new GameClient();
    TestObserver observer2 = new TestObserver();
    client2.addObserver(observer2);
    client2.connect("127.0.0.1", currentTestPort);

    // On attend qu'il soit bien connecté
    for (int i = 0; i < 50 && !observer2.lastMessage.contains("Connected"); i++) {
      Thread.sleep(50);
    }
    Thread.sleep(100); // Laisse le temps au serveur d'enregistrer l'ID

    // 2. Le Client 1 (Hôte) invite le Client 2
    client.sendNew(2);

    // On attend que le Client 2 reçoive l'invitation
    for (int i = 0; i < 50 && observer2.invitationFrom == null; i++) {
      Thread.sleep(50);
    }
    Assertions.assertEquals("Player-1", observer2.invitationFrom, "L'invitation doit bien arriver");

    // 3. CRASH : Le Client 1 (Hôte) se déconnecte brutalement avant la réponse
    client.quit();

    // 4. On attend que le serveur s'en rende compte, nettoie, et prévienne le Client 2
    for (int i = 0; i < 50 && observer2.invitationCancelledReason == null; i++) {
      Thread.sleep(50);
    }

    // 5. Assertions : Vérification du rattrapage d'erreur
    Assertions.assertEquals(
        "Host disconnected",
        observer2.invitationCancelledReason,
        "Client 2 doit être notifié que l'hôte a crashé");
    Assertions.assertEquals(
        "IDLE", observer2.lastPlayerStatus, "Client 2 doit être libéré et repasser en IDLE");

    // Nettoyage final
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
    volatile String invitationFrom = null;
    volatile String invitationDeclinedBy = "";
    volatile String invitationCancelledReason = null;
    volatile String lastPlayerStatus = null;
    volatile Map<String, String> lastPlayerDetails = null;

    /**
     * Réinitialise l'état de l'observer pour éviter les pollutions (Race Conditions) entre
     * différents scénarios d'un même test.
     */
    public void reset() {
      lastMessage = "";
      lastStatus = null;
      lastPlayers = null;
      lastScoreboard = null;
      localModelUpdated = false;
      invitationFrom = null;
      invitationDeclinedBy = "";
      invitationCancelledReason = null;
      lastPlayerStatus = null;
      lastPlayerDetails = null;
    }

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
    public void gameEndedUpdate(List<Map<String, String>> players) {}

    @Override
    public void serverWelcomeUpdate(int myId) {

    }

    @Override
    public void serverListUpdate(List<ServerInfo> activeServers) {}

    @Override
    public void invitationReceivedUpdate(String from) {
      this.invitationFrom = from;
    }

    @Override
    public void invitationAcceptedUpdate(String playerAccepted) {}

    @Override
    public void invitationDeclinedUpdate(String playerDeclined) {
      this.invitationDeclinedBy = playerDeclined;
    }

    @Override
    public void invitationCancelledUpdate(String reason) {
      this.invitationCancelledReason = reason;
    }

    @Override
    public void playersPlayerIdUpdate(Map<String, String> playerInfo) {
      this.lastPlayerDetails = playerInfo;
    }

    @Override
    public void playerStatusUpdate(String status) {
      this.lastPlayerStatus = status;
    }

    @Override
    public void clientDisconnectedUpdate(String reason) {}

    @Override
    public void gameInterruptedUpdate(String reason) {}

    @Override
    public void connectionFailedUpdate(String reason) {}

    @Override
    public void invitationFailedUpdate(String reason) {

    }
  }
}
