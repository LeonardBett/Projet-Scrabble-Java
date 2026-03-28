package fr.ubordeaux.scrabble.model.network;

import fr.ubordeaux.scrabble.model.network.server.ServerInfo;
import fr.ubordeaux.scrabble.model.utils.GameLogger;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Deep integration tests for NetworkManager. Covers lifecycle, server/client management, and
 * command delegation. Since it's only a facade to other methods, we don't go deep into commands
 * testing
 */
class NetworkManagerTest {

  private NetworkManager networkManager;
  private NetworkObserver mockObserver;

  @BeforeEach
  void setUp() {
    GameLogger.setVerbose(false);
    GameLogger.setDebug(false);
    networkManager = new NetworkManager();
    mockObserver =
        new NetworkObserver() {
          @Override
          public void localModelUpdate() {}

          @Override
          public void gameEndedUpdate(List<Map<String, String>> players) {}

          @Override
          public void serverWelcomeUpdate(int myId) {}

          @Override
          public void serverStatusUpdate(java.util.Map<String, String> info) {}

          @Override
          public void playersUpdate(java.util.List<java.util.Map<String, String>> players) {}

          @Override
          public void scoreboardUpdate(java.util.List<java.util.Map<String, String>> scoreboard) {}

            @Override
            public void pongUpdate(long latencyMs) {

            }

            @Override
          public void serverListUpdate(List<ServerInfo> activeServers) {}

          @Override
          public void messageUpdate(String message) {}

          @Override
          public void invitationReceivedUpdate(String from) {}

          @Override
          public void invitationAcceptedUpdate(String playerAccepted) {}

          @Override
          public void invitationDeclinedUpdate(String playerDeclined) {}

          @Override
          public void invitationCancelledUpdate(String reason) {}

          @Override
          public void playersPlayerIdUpdate(Map<String, String> playerInfo) {}

          @Override
          public void playerStatusUpdate(String status) {}

          @Override
          public void clientDisconnectedUpdate(String reason) {}

          @Override
          public void gameInterruptedUpdate(String reason) {}

          @Override
          public void connectionFailedUpdate(String reason) {}

          @Override
          public void invitationFailedUpdate(String reason) {}
        };
  }

  @AfterEach
  void tearDown() {
    // Shutdown everything to clean up ports and threads
    networkManager.stopOnlinePlay();
  }

  // --- LIFECYCLE TESTS ---

  @Test
  void testDiscoveryServiceLifecycle() {
    // Verify list is accessible and empty at start
    Assertions.assertTrue(networkManager.serverList().isEmpty());

    networkManager.startOnlinePlay();
    // stopOnlinePlay should clean up everything
    networkManager.stopOnlinePlay();
  }

  // --- SERVER MANAGEMENT TESTS ---

  @Test
  void testServerStartAndStop() {
    networkManager.startOnlinePlay();

    // Start server on default port
    networkManager.serverStart();

    // Attempting to start a second server should be blocked internally
    networkManager.serverStart(12345);

    networkManager.serverStop();
    // Stopping again should be handled gracefully
    networkManager.serverStop();
  }

  // --- CLIENT MANAGEMENT TESTS ---

  @Test
  void testJoinAndQuitLifecycle() throws InterruptedException {
    networkManager.startOnlinePlay();

    // Start server on default port
    networkManager.serverStart();
    Thread.sleep(1500);

    // Connect to a local address
    networkManager.join("127.0.0.1");

    networkManager.quit();
    // Quitting while disconnected should be handled gracefully
    networkManager.quit();
  }

  // --- OBSERVER DELEGATION TESTS ---

  @Test
  void testObserverPropagation() {
    // Add observer before client exists
    networkManager.addObserver(mockObserver);

    // Remove observer should update discovery and client
    networkManager.removeObserver(mockObserver);

    // Removing non-existent observer should print error but not crash
    networkManager.removeObserver(mockObserver);
  }

  // --- COMMAND DELEGATION (DEEP COVERAGE) ---
  // These tests ensure all command methods handle the "disconnected" state

  @Test
  void testCommandsWhileDisconnected() {
    // These calls should all hit the "Client is not connected" error branch
    networkManager.ping();
    networkManager.serverStatus();
    networkManager.players();
    networkManager.scoreboard();
    networkManager.newPlayerId(1);
    networkManager.newPlayerId(1, 2);
    networkManager.newPlayerId(1, 2, 3);
    networkManager.play(7, 7, "H", "TEST");
    networkManager.exchange("ABC");
    networkManager.pass();

    // Testing the new invitation-related commands
    networkManager.accept();
    networkManager.decline();
    networkManager.cancel();
    networkManager.away();
    networkManager.back();
    networkManager.playersPlayerId(1);

    // No exceptions should be thrown despite being disconnected
  }

  @Test
  void testCommandsWhileConnected() throws InterruptedException {
    int port = 12350;

    // Start server and allow time for the thread to bind the port on slow machines
    Assertions.assertTrue(networkManager.serverStart(port));
    Thread.sleep(1000);

    // Join using localhost for cross-platform compatibility
    networkManager.join("localhost", port);
    Thread.sleep(1000); // Allow time for the socket connection to establish

    // Verify that calling methods while connected doesn't crash the client
    Assertions.assertDoesNotThrow(
        () -> {
          networkManager.ping();
          networkManager.serverStatus();
          networkManager.players();
          networkManager.scoreboard();
          networkManager.newPlayerId(1);
          networkManager.newPlayerId(1, 2);
          networkManager.newPlayerId(1, 2, 3);
          networkManager.play(7, 7, "H", "TEST");
          networkManager.exchange("ABC");
          networkManager.pass();
          networkManager.accept();
          networkManager.decline();
          networkManager.cancel();
          networkManager.playersPlayerId(1);
          networkManager.away();
          networkManager.back();
        });

    networkManager.quit();
    networkManager.serverStop();
  }

  @Test
  void testGetLocalGame() throws InterruptedException {
    // Should return null when completely disconnected
    Assertions.assertNull(networkManager.getLocalGame());

    int port = 12352;
    networkManager.serverStart(port);
    Thread.sleep(1000); // Delay for slow machines

    networkManager.join("localhost:" + port);
    Thread.sleep(1000); // Delay for socket setup

    // Should still return null because the server hasn't sent a GAME_START packet yet
    Assertions.assertNull(networkManager.getLocalGame());

    networkManager.quit();
    networkManager.serverStop();
  }
}
