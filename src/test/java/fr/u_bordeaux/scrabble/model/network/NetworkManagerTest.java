package fr.u_bordeaux.scrabble.model.network;

import fr.u_bordeaux.scrabble.model.network.server.ServerInfo;
import java.util.List;
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
    networkManager = new NetworkManager();
    mockObserver =
        new NetworkObserver() {
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
          public void serverListUpdate(List<ServerInfo> activeServers) {}

          @Override
          public void messageUpdate(String message) {}
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
  void testJoinAndQuitLifecycle() {
    networkManager.startOnlinePlay();

    // Connect to a local address
    networkManager.join("localhost");

    // Attempting to join while already connected should be blocked
    networkManager.join("127.0.0.1", 12345);

    networkManager.quit();
    // Quitting while disconnected should be handled gracefully
    networkManager.quit();
  }

  // --- OBSERVER DELEGATION TESTS ---

  @Test
  void testObserverPropagation() {
    // Add observer before client exists
    networkManager.addObserver(mockObserver);

    // When joining, the observer must be added to the new GameClient
    networkManager.join("localhost");

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
    networkManager.play(7, 7, "H", "TEST");
    networkManager.exchange("ABC");
    networkManager.pass();

    // No exceptions should be thrown despite being disconnected
  }
}
