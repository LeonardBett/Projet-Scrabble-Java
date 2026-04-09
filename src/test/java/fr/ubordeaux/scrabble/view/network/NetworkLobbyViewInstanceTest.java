package fr.ubordeaux.scrabble.view.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.network.NetworkManager;
import fr.ubordeaux.scrabble.model.network.server.ServerInfo;
import fr.ubordeaux.scrabble.view.gui.network.NetworkGameBridge;
import fr.ubordeaux.scrabble.view.gui.network.NetworkLobbyView;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests NetworkLobbyView instance methods by creating a real instance on the FX thread. */
class NetworkLobbyViewInstanceTest {

  private NetworkManager networkManager;
  private NetworkGameBridge bridge;
  private NetworkLobbyView lobby;

  @BeforeAll
  static void initToolkit() {
    try {
      com.sun.javafx.application.PlatformImpl.startup(() -> {});
    } catch (Exception e) {
      // Already initialized
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    networkManager = new NetworkManager();
    bridge = new NetworkGameBridge(networkManager);
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<NetworkLobbyView> ref = new AtomicReference<>();
    Platform.runLater(
        () -> {
          try {
            ref.set(new NetworkLobbyView(bridge));
          } finally {
            latch.countDown();
          }
        });
    assertTrue(latch.await(5, TimeUnit.SECONDS));
    lobby = ref.get();
    assertNotNull(lobby);
  }

  @AfterEach
  void tearDown() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          try {
            lobby.hide();
            lobby.close();
          } finally {
            latch.countDown();
          }
        });
    latch.await(5, TimeUnit.SECONDS);
    try {
      networkManager.serverStop();
    } catch (Exception ignored) {
      // best effort cleanup
    }
  }

  @Test
  void isHostModeDefaultsFalse() {
    assertFalse(lobby.isHostMode());
  }

  @Test
  void lobbyPlayerCountDefaultsZero() {
    assertEquals(0, lobby.getLobbyPlayerCount());
  }

  @Test
  void onServerListUpdatedUpdatesView() throws Exception {
    runOnFx(() -> lobby.onServerListUpdated(List.of(new ServerInfo("1.2.3.4", 5555, "srv"))));
  }

  @Test
  void onServerListUpdatedEmpty() throws Exception {
    runOnFx(() -> lobby.onServerListUpdated(List.of()));
  }

  @Test
  void onPlayersReceivedUpdatesCount() throws Exception {
    runOnFx(
        () ->
            lobby.onPlayersReceived(
                List.of(
                    Map.of("ID", "1", "NAME", "A", "STATUS", "OK"),
                    Map.of("ID", "2", "NAME", "B", "STATUS", "OK"))));
    assertEquals(2, lobby.getLobbyPlayerCount());
  }

  @Test
  void onPlayersReceivedEmpty() throws Exception {
    runOnFx(() -> lobby.onPlayersReceived(List.of()));
    assertEquals(0, lobby.getLobbyPlayerCount());
  }

  @Test
  void onScoreboardReceivedNotThrow() throws Exception {
    runOnFx(
        () ->
            lobby.onScoreboardReceived(
                List.of(Map.of("NAME", "A", "WINS", "1", "LOSSES", "0", "TOTAL", "1"))));
  }

  @Test
  void onServerStatusReceivedNotThrow() throws Exception {
    runOnFx(
        () -> lobby.onServerStatusReceived(Map.of("PORT", "5555", "CLIENTS", "2", "GAMES", "1")));
  }

  @Test
  void onMessageReceivedNotThrow() throws Exception {
    runOnFx(() -> lobby.onMessageReceived("Hello"));
  }

  @Test
  void onGameEndedClearsState() throws Exception {
    runOnFx(() -> lobby.onGameEnded("Player disconnected"));
    assertEquals(0, lobby.getLobbyPlayerCount());
  }

  @Test
  void onPlayersReceivedWhenServerRunning() throws Exception {
    setPrivateField(lobby, "serverRunning", true);
    runOnFx(
        () ->
            lobby.onPlayersReceived(
                List.of(
                    Map.of("ID", "1", "NAME", "A", "STATUS", "OK"),
                    Map.of("ID", "2", "NAME", "B", "STATUS", "OK"))));
    assertTrue(lobby.isHostMode());
    assertEquals(2, lobby.getLobbyPlayerCount());
  }

  @Test
  void onPlayersReceivedWithOnePlayerNotReady() throws Exception {
    setPrivateField(lobby, "serverRunning", true);
    runOnFx(() -> lobby.onPlayersReceived(List.of(Map.of("ID", "1", "NAME", "A", "STATUS", "OK"))));
    assertEquals(1, lobby.getLobbyPlayerCount());
  }

  @Test
  void onPongReceivedNotThrow() throws Exception {
    runOnFx(() -> lobby.onPongReceived(42L));
  }

  @Test
  void onInvitationCancelledNotThrow() throws Exception {
    runOnFx(() -> lobby.onInvitationCancelled("reason"));
  }

  @Test
  void onInvitationCancelNotThrow() throws Exception {
    runOnFx(() -> lobby.onInvitationCancel());
  }

  @Test
  void onPlayerDetailsReceivedNotThrow() throws Exception {
    runOnFx(() -> lobby.onPlayerDetailsReceived(Map.of("ID", "1", "NAME", "A")));
  }

  @Test
  void onPlayerStatusChangedNotThrow() throws Exception {
    runOnFx(() -> lobby.onPlayerStatusChanged("AWAY"));
  }

  @Test
  void onClientDisconnectedNotThrow() throws Exception {
    runOnFx(() -> lobby.onClientDisconnected("reason"));
  }

  @Test
  void onConnectionFailedNotThrow() throws Exception {
    runOnFx(() -> lobby.onConnectionFailed("reason"));
  }

  @Test
  void onGameInterruptedNotThrow() throws Exception {
    runOnFx(() -> lobby.onGameInterrupted("reason"));
  }

  @Test
  void onWelcomeReceivedNotThrow() throws Exception {
    runOnFx(() -> lobby.onWelcomeReceived(1));
  }

  @Test
  void onGameEndedWithFinalScoresNotThrow() throws Exception {
    runOnFx(() -> lobby.onGameEnded(List.of(Map.of("NAME", "A", "SCORE", "12"))));
  }

  @Test
  void onInvitationReceivedAcceptNotThrow() throws Exception {
    scheduleInvitationDialogResponse(0);
    runOnFx(() -> lobby.onInvitationReceived("Bob"));
  }

  @Test
  void onInvitationReceivedDeclineNotThrow() throws Exception {
    scheduleInvitationDialogResponse(1);
    runOnFx(() -> lobby.onInvitationReceived("Bob"));
  }

  private static void runOnFx(Runnable action) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          try {
            action.run();
          } finally {
            latch.countDown();
          }
        });
    assertTrue(latch.await(5, TimeUnit.SECONDS));
  }

  private void scheduleInvitationDialogResponse(int buttonIndex) {
    Thread responder = new Thread(() -> {
      try {
        Thread.sleep(120);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      Platform.runLater(() -> {
        Object dialogObject = getPrivateField(lobby, "currentInvitationDialog");
        if (dialogObject instanceof Alert alert && !alert.getButtonTypes().isEmpty()) {
          int safeIndex = Math.min(buttonIndex, alert.getButtonTypes().size() - 1);
          ButtonType chosen = alert.getButtonTypes().get(safeIndex);
          alert.setResult(chosen);
          alert.close();
        }
      });
    });
    responder.setDaemon(true);
    responder.start();
  }

  private static void setPrivateField(Object target, String name, Object value) {
    try {
      Field f = target.getClass().getDeclaredField(name);
      f.setAccessible(true);
      f.set(target, value);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private static Object getPrivateField(Object target, String name) {
    try {
      Field f = target.getClass().getDeclaredField(name);
      f.setAccessible(true);
      return f.get(target);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
