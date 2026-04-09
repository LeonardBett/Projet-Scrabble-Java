package fr.ubordeaux.scrabble.view.network;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.model.network.NetworkManager;
import fr.ubordeaux.scrabble.model.network.server.ServerInfo;
import fr.ubordeaux.scrabble.view.gui.ScrabbleGui;
import fr.ubordeaux.scrabble.view.gui.network.NetworkGameBridge;
import fr.ubordeaux.scrabble.view.gui.network.NetworkLobbyView;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NetworkGameBridgeTest {

  @BeforeAll
  static void initToolkit() {
    try {
      com.sun.javafx.application.PlatformImpl.startup(() -> {});
    } catch (Exception e) {
      // Toolkit already initialized
    }
  }

  /*
    @Test
    void requestGameStartShouldAskPlayers() {
      FakeNetworkManager manager = new FakeNetworkManager();
      NetworkGameBridge bridge = new NetworkGameBridge(manager);

      bridge.requestGameStart();

      assertEquals(1, manager.playersCalls);
    }
  */

  @Test
  void disposeShouldRemoveObserverAndStopOnlinePlay() {
    FakeNetworkManager manager = new FakeNetworkManager();
    NetworkGameBridge bridge = new NetworkGameBridge(manager);
    bridge.dispose();
    assertTrue(manager.removeObserverCalled);
    assertTrue(manager.stopOnlineCalled);
  }

  @Test
  void helpersShouldParseAndDispatchIdsAcrossBranches() {
    assertTrue(
        (boolean)
            invokeStatic(
                "shouldDispatchGameStart", new Class<?>[] {boolean.class, int.class}, true, 2));
    assertFalse(
        (boolean)
            invokeStatic(
                "shouldDispatchGameStart", new Class<?>[] {boolean.class, int.class}, true, 1));
    assertFalse(
        (boolean)
            invokeStatic(
                "shouldDispatchGameStart", new Class<?>[] {boolean.class, int.class}, false, 4));

    assertEquals(7, invokeStatic("parsePlayerId", new Class<?>[] {Map.class}, Map.of("ID", "7")));
    assertEquals(0, invokeStatic("parsePlayerId", new Class<?>[] {Map.class}, Map.of("ID", "abc")));
    assertEquals(0, invokeStatic("parsePlayerId", new Class<?>[] {Map.class}, Map.of()));

    int[] ids =
        (int[])
            invokeStatic(
                "extractPositivePlayerIds",
                new Class<?>[] {List.class},
                List.of(Map.of("ID", "1"), Map.of("ID", "x"), Map.of("ID", "3"), Map.of()));
    assertEquals(2, ids.length);
    assertEquals(1, ids[0]);
    assertEquals(3, ids[1]);
  }

  @Test
  void dispatchHelperShouldChooseCorrectNewPlayerOverload() {
    FakeNetworkManager manager = new FakeNetworkManager();
    assertEquals(
        0,
        invokeStatic(
            "dispatchNewGame",
            new Class<?>[] {NetworkManager.class, int[].class},
            manager,
            new int[] {1}));
    assertEquals(
        1,
        invokeStatic(
            "dispatchNewGame",
            new Class<?>[] {NetworkManager.class, int[].class},
            manager,
            new int[] {1, 2}));
    assertEquals(
        2,
        invokeStatic(
            "dispatchNewGame",
            new Class<?>[] {NetworkManager.class, int[].class},
            manager,
            new int[] {1, 2, 3}));
    assertEquals(
        3,
        invokeStatic(
            "dispatchNewGame",
            new Class<?>[] {NetworkManager.class, int[].class},
            manager,
            new int[] {1, 2, 3, 4}));
    assertEquals(1, manager.new1Calls);
    assertEquals(1, manager.new2Calls);
    assertEquals(1, manager.new3Calls);
  }

  @Test
  void dispatchNewGameReturnsZeroForEmptyIds() {
    FakeNetworkManager manager = new FakeNetworkManager();
    assertEquals(
        0,
        invokeStatic(
            "dispatchNewGame",
            new Class<?>[] {NetworkManager.class, int[].class},
            manager,
            new int[] {}));
  }

  @Test
  void dispatchNewGameHandlesFiveIds() {
    FakeNetworkManager manager = new FakeNetworkManager();
    assertEquals(
        3,
        invokeStatic(
            "dispatchNewGame",
            new Class<?>[] {NetworkManager.class, int[].class},
            manager,
            new int[] {1, 2, 3, 4, 5}));
  }

  @Test
  void getNetworkManagerShouldReturnSameInstance() {
    FakeNetworkManager manager = new FakeNetworkManager();
    NetworkGameBridge bridge = new NetworkGameBridge(manager);
    assertSame(manager, bridge.getNetworkManager());
  }

  @Test
  void setGuiShouldNotThrow() {
    FakeNetworkManager manager = new FakeNetworkManager();
    NetworkGameBridge bridge = new NetworkGameBridge(manager);
    assertDoesNotThrow(() -> bridge.setGui(null));
  }

  @Test
  void setLobbyViewShouldNotThrow() {
    FakeNetworkManager manager = new FakeNetworkManager();
    NetworkGameBridge bridge = new NetworkGameBridge(manager);
    assertDoesNotThrow(() -> bridge.setLobbyView(null));
  }

  @Test
  void localModelUpdateWithNullGuiShouldNotThrow() throws InterruptedException {
    FakeNetworkManager manager = new FakeNetworkManager();
    NetworkGameBridge bridge = new NetworkGameBridge(manager);
    bridge.setGui(null);
    bridge.localModelUpdate();
    waitFx();
  }

  @Test
  void localModelUpdateWithNullLocalGameShouldNotThrow() throws InterruptedException {
    FakeNetworkManager manager = new FakeNetworkManager();
    manager.localGame = null;
    NetworkGameBridge bridge = new NetworkGameBridge(manager);
    FakeScrabbleGui fakeGui = new FakeScrabbleGui();
    bridge.setGui(fakeGui);
    bridge.localModelUpdate();
    waitFx();
    assertFalse(fakeGui.switchCalled);
    assertFalse(fakeGui.refreshCalled);
  }

  @Test
  void localModelUpdateSwitchesWhenNotOnline() throws InterruptedException {
    FakeNetworkManager manager = new FakeNetworkManager();
    Game game = new Game();
    game.addPlayer(new HumanPlayer("P1", PlayerColor.BLUE));
    game.addPlayer(new HumanPlayer("P2", PlayerColor.RED));
    manager.localGame = game;
    NetworkGameBridge bridge = new NetworkGameBridge(manager);
    FakeScrabbleGui fakeGui = new FakeScrabbleGui();
    fakeGui.onlineMode = false;
    bridge.setGui(fakeGui);
    bridge.localModelUpdate();
    waitFx();
    assertTrue(fakeGui.switchCalled);
  }

  @Test
  void localModelUpdateRefreshesWhenOnline() throws InterruptedException {
    FakeNetworkManager manager = new FakeNetworkManager();
    Game game = new Game();
    game.addPlayer(new HumanPlayer("P1", PlayerColor.BLUE));
    game.addPlayer(new HumanPlayer("P2", PlayerColor.RED));
    manager.localGame = game;
    NetworkGameBridge bridge = new NetworkGameBridge(manager);
    FakeScrabbleGui fakeGui = new FakeScrabbleGui();
    fakeGui.onlineMode = true;
    bridge.setGui(fakeGui);
    bridge.localModelUpdate();
    waitFx();
    assertFalse(fakeGui.switchCalled);
    assertTrue(fakeGui.refreshCalled);
  }

  @Test
  void gameEndedUpdateCallsExitAndShowsInfo() throws InterruptedException {
    FakeNetworkManager manager = new FakeNetworkManager();
    NetworkGameBridge bridge = new NetworkGameBridge(manager);
    FakeScrabbleGui fakeGui = new FakeScrabbleGui();
    bridge.setGui(fakeGui);
    bridge.gameEndedUpdate("Player disconnected");
    waitFx();
    assertTrue(fakeGui.exitOnlineCalled);
    assertNotNull(fakeGui.lastInfoTitle);
    assertTrue(fakeGui.lastInfoMessage.contains("Player disconnected"));
  }

  @Test
  void gameEndedUpdateWithNullGuiShouldNotThrow() throws InterruptedException {
    FakeNetworkManager manager = new FakeNetworkManager();
    NetworkGameBridge bridge = new NetworkGameBridge(manager);
    bridge.setGui(null);
    bridge.gameEndedUpdate("reason");
    waitFx();
  }

  @Test
  void serverStatusUpdateNullLobbyNotThrow() throws InterruptedException {
    FakeNetworkManager manager = new FakeNetworkManager();
    NetworkGameBridge bridge = new NetworkGameBridge(manager);
    bridge.setLobbyView(null);
    bridge.serverStatusUpdate(Map.of("PORT", "1234"));
    waitFx();
  }

  @Test
  void playersUpdateNullLobbyNotThrow() throws InterruptedException {
    FakeNetworkManager manager = new FakeNetworkManager();
    NetworkGameBridge bridge = new NetworkGameBridge(manager);
    bridge.setLobbyView(null);
    bridge.playersUpdate(List.of(Map.of("ID", "1")));
    waitFx();
  }

  @Test
  void scoreboardUpdateNullLobbyNotThrow() throws InterruptedException {
    FakeNetworkManager manager = new FakeNetworkManager();
    NetworkGameBridge bridge = new NetworkGameBridge(manager);
    bridge.setLobbyView(null);
    bridge.scoreboardUpdate(List.of(Map.of("NAME", "X")));
    waitFx();
  }

  @Test
  void serverListUpdateNullLobbyNotThrow() throws InterruptedException {
    FakeNetworkManager manager = new FakeNetworkManager();
    NetworkGameBridge bridge = new NetworkGameBridge(manager);
    bridge.setLobbyView(null);
    bridge.serverListUpdate(List.of());
    waitFx();
  }

  @Test
  void messageUpdateNullLobbyNotThrow() throws InterruptedException {
    FakeNetworkManager manager = new FakeNetworkManager();
    NetworkGameBridge bridge = new NetworkGameBridge(manager);
    bridge.setLobbyView(null);
    bridge.messageUpdate("hello");
    waitFx();
  }

  @Test
  void shouldDispatchGameStartEdgeCases() {
    assertFalse(
        (boolean)
            invokeStatic(
                "shouldDispatchGameStart", new Class<?>[] {boolean.class, int.class}, false, 2));
    assertTrue(
        (boolean)
            invokeStatic(
                "shouldDispatchGameStart", new Class<?>[] {boolean.class, int.class}, true, 3));
    assertTrue(
        (boolean)
            invokeStatic(
                "shouldDispatchGameStart", new Class<?>[] {boolean.class, int.class}, true, 4));
  }

  @Test
  void parsePlayerIdVariousInputs() {
    assertEquals(0, invokeStatic("parsePlayerId", new Class<?>[] {Map.class}, Map.of("ID", "0")));
    assertEquals(99, invokeStatic("parsePlayerId", new Class<?>[] {Map.class}, Map.of("ID", "99")));
  }

  @Test
  void extractPositivePlayerIdsAllZeros() {
    int[] ids =
        (int[])
            invokeStatic(
                "extractPositivePlayerIds",
                new Class<?>[] {List.class},
                List.of(Map.of("ID", "0"), Map.of("ID", "0")));
    assertEquals(0, ids.length);
  }

  @Test
  void extractPositivePlayerIdsAllPositive() {
    int[] ids =
        (int[])
            invokeStatic(
                "extractPositivePlayerIds",
                new Class<?>[] {List.class},
                List.of(Map.of("ID", "1"), Map.of("ID", "2"), Map.of("ID", "3")));
    assertEquals(3, ids.length);
  }

  @Test
  void serverWelcomeUpdateNullLobbyNotThrow() throws InterruptedException {
    FakeNetworkManager manager = new FakeNetworkManager();
    NetworkGameBridge bridge = new NetworkGameBridge(manager);
    bridge.serverWelcomeUpdate(1);
    waitFx();
  }

  @Test
  void pongUpdateNullLobbyNotThrow() throws InterruptedException {
    FakeNetworkManager manager = new FakeNetworkManager();
    NetworkGameBridge bridge = new NetworkGameBridge(manager);
    bridge.pongUpdate(50L);
    waitFx();
  }

  @Test
  void invitationReceivedUpdateNullLobbyNotThrow() throws InterruptedException {
    FakeNetworkManager manager = new FakeNetworkManager();
    NetworkGameBridge bridge = new NetworkGameBridge(manager);
    bridge.invitationReceivedUpdate("Player2");
    waitFx();
  }

  @Test
  void invitationAcceptedUpdateNullLobbyNotThrow() throws InterruptedException {
    FakeNetworkManager manager = new FakeNetworkManager();
    NetworkGameBridge bridge = new NetworkGameBridge(manager);
    bridge.invitationAcceptedUpdate("Player2");
    waitFx();
  }

  @Test
  void invitationDeclinedUpdateNullLobbyNotThrow() throws InterruptedException {
    FakeNetworkManager manager = new FakeNetworkManager();
    NetworkGameBridge bridge = new NetworkGameBridge(manager);
    bridge.invitationDeclinedUpdate("Player2");
    waitFx();
  }

  @Test
  void invitationCancelledUpdateNullLobbyNotThrow() throws InterruptedException {
    FakeNetworkManager manager = new FakeNetworkManager();
    NetworkGameBridge bridge = new NetworkGameBridge(manager);
    bridge.invitationCancelledUpdate("reason");
    waitFx();
  }

  @Test
  void playersPlayerIdUpdateNullLobbyNotThrow() throws InterruptedException {
    FakeNetworkManager manager = new FakeNetworkManager();
    NetworkGameBridge bridge = new NetworkGameBridge(manager);
    bridge.playersPlayerIdUpdate(Map.of("ID", "1"));
    waitFx();
  }

  @Test
  void playerStatusUpdateNullLobbyNotThrow() throws InterruptedException {
    FakeNetworkManager manager = new FakeNetworkManager();
    NetworkGameBridge bridge = new NetworkGameBridge(manager);
    bridge.playerStatusUpdate("AWAY");
    waitFx();
  }

  @Test
  void clientDisconnectedUpdateNullLobbyNotThrow() throws InterruptedException {
    FakeNetworkManager manager = new FakeNetworkManager();
    NetworkGameBridge bridge = new NetworkGameBridge(manager);
    bridge.clientDisconnectedUpdate("reason");
    waitFx();
  }

  @Test
  void connectionFailedUpdateNullLobbyNotThrow() throws InterruptedException {
    FakeNetworkManager manager = new FakeNetworkManager();
    NetworkGameBridge bridge = new NetworkGameBridge(manager);
    bridge.connectionFailedUpdate("reason");
    waitFx();
  }

  @Test
  void invitationFailedUpdateNullLobbyNotThrow() throws InterruptedException {
    FakeNetworkManager manager = new FakeNetworkManager();
    NetworkGameBridge bridge = new NetworkGameBridge(manager);
    bridge.invitationFailedUpdate("reason");
    waitFx();
  }

  @Test
  void gameInterruptedUpdateNullLobbyNotThrow() throws InterruptedException {
    FakeNetworkManager manager = new FakeNetworkManager();
    NetworkGameBridge bridge = new NetworkGameBridge(manager);
    bridge.gameInterruptedUpdate("reason");
    waitFx();
  }

  @Test
  void bridgeShouldDelegateToGuiAndLobbyWhenAttached() throws Exception {
    FakeNetworkManager manager = new FakeNetworkManager();
    manager.localGame = new Game();
    manager.localGame.addPlayer(new HumanPlayer("P1", PlayerColor.BLUE));
    manager.localGame.addPlayer(new HumanPlayer("P2", PlayerColor.RED));

    NetworkGameBridge bridge = new NetworkGameBridge(manager);
    FakeScrabbleGui fakeGui = new FakeScrabbleGui();
    bridge.setGui(fakeGui);

    AtomicReference<TestLobbyView> lobbyRef = new AtomicReference<>();
    CountDownLatch createLatch = new CountDownLatch(1);
    Platform.runLater(() -> {
      try {
        TestLobbyView lobby = new TestLobbyView(bridge);
        lobby.hide();
        lobbyRef.set(lobby);
      } finally {
        createLatch.countDown();
      }
    });
    assertTrue(createLatch.await(5, TimeUnit.SECONDS));
    TestLobbyView lobby = lobbyRef.get();
    assertNotNull(lobby);

    bridge.requestGameStart();
    assertEquals(1, manager.playersCalls);

    bridge.localModelUpdate();
    waitFx();
    assertTrue(fakeGui.switchCalled);
    assertTrue(lobby.invitationCancelCalls >= 1);

    fakeGui.onlineMode = true;
    bridge.localModelUpdate();
    waitFx();
    assertTrue(fakeGui.refreshCalled);

    bridge.gameEndedUpdate(List.of(Map.of("NAME", "P1", "SCORE", "42")));
    waitFx();
    assertTrue(lobby.gameEndedListCalls >= 1);

    bridge.serverWelcomeUpdate(7);
    bridge.serverStatusUpdate(Map.of("PORT", "1111"));
    bridge.pongUpdate(22L);
    bridge.playersUpdate(List.of(Map.of("ID", "1", "NAME", "A", "STATUS", "IDLE")));
    bridge.scoreboardUpdate(List.of(Map.of("NAME", "A", "WINS", "1", "LOSSES", "0", "TOTAL", "1")));
    bridge.serverListUpdate(List.of());
    bridge.messageUpdate("lobby.disconnected");
    bridge.invitationReceivedUpdate("A");
    bridge.invitationAcceptedUpdate("A");
    bridge.invitationDeclinedUpdate("A");
    bridge.invitationCancelledUpdate("lobby.disconnected");
    bridge.playersPlayerIdUpdate(Map.of("ID", "1", "NAME", "A"));
    bridge.playerStatusUpdate("AWAY");
    bridge.clientDisconnectedUpdate("lobby.disconnected");
    bridge.connectionFailedUpdate("lobby.disconnected");
    bridge.invitationFailedUpdate("lobby.disconnected");
    bridge.gameInterruptedUpdate("lobby.disconnected");
    bridge.moveRefusedUpdate("scrabble.invalidMove");
    waitFx();

    assertTrue(lobby.welcomeCalls >= 1);
    assertTrue(lobby.serverStatusCalls >= 1);
    assertTrue(lobby.playersCalls >= 1);
    assertTrue(lobby.scoreboardCalls >= 1);
    assertTrue(lobby.serverListCalls >= 1);
    assertTrue(lobby.messageCalls >= 1);
    assertTrue(lobby.invitationReceivedCalls >= 1);
    assertTrue(lobby.invitationCancelledCalls >= 1);
    assertTrue(lobby.playerDetailsCalls >= 1);
    assertTrue(lobby.playerStatusCalls >= 1);
    assertTrue(lobby.clientDisconnectedCalls >= 1);
    assertTrue(lobby.connectionFailedCalls >= 1);
    assertTrue(lobby.gameInterruptedCalls >= 1);
    assertTrue(lobby.pongCalls >= 1);
    assertTrue(manager.quitCalls >= 2);
    assertTrue(fakeGui.moveRefusedCalled);

    CountDownLatch closeLatch = new CountDownLatch(1);
    Platform.runLater(() -> {
      try {
        lobby.close();
      } finally {
        closeLatch.countDown();
      }
    });
    assertTrue(closeLatch.await(5, TimeUnit.SECONDS));
  }

  private static Object invokeStatic(String methodName, Class<?>[] argTypes, Object... args) {
    try {
      Method method = NetworkGameBridge.class.getDeclaredMethod(methodName, argTypes);
      method.setAccessible(true);
      return method.invoke(null, args);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  private static void waitFx() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(latch::countDown);
    assertTrue(latch.await(2, TimeUnit.SECONDS), "Timeout waiting for JavaFX event queue");
  }

  private static class FakeNetworkManager extends NetworkManager {
    int playersCalls;
    int new1Calls;
    int new2Calls;
    int new3Calls;
    boolean removeObserverCalled;
    boolean stopOnlineCalled;
    int quitCalls;
    Game localGame;

    @Override
    public void players() {
      playersCalls++;
    }

    @Override
    public void newPlayerId(int targetId) {
      new1Calls++;
    }

    @Override
    public void newPlayerId(int targetId1, int targetId2) {
      new2Calls++;
    }

    @Override
    public void newPlayerId(int targetId1, int targetId2, int targetId3) {
      new3Calls++;
    }

    @Override
    public Game getLocalGame() {
      return localGame;
    }

    @Override
    public void quit() {
      quitCalls++;
    }

    @Override
    public void removeObserver(fr.ubordeaux.scrabble.model.network.NetworkObserver observer) {
      removeObserverCalled = true;
    }

    @Override
    public void stopOnlinePlay() {
      stopOnlineCalled = true;
    }
  }

  private static class FakeScrabbleGui extends ScrabbleGui {
    boolean onlineMode;
    boolean switchCalled;
    boolean refreshCalled;
    boolean exitOnlineCalled;
    boolean moveRefusedCalled;
    String lastInfoTitle;
    String lastInfoMessage;

    @Override
    public boolean isOnlineMode() {
      return onlineMode;
    }

    @Override
    public void switchToOnlineGame(Game onlineGame) {
      switchCalled = true;
      onlineMode = true;
    }

    @Override
    public void refreshAll() {
      refreshCalled = true;
    }

    @Override
    public void exitOnlineMode() {
      exitOnlineCalled = true;
      onlineMode = false;
    }

    @Override
    public void showInfo(String title, String message) {
      lastInfoTitle = title;
      lastInfoMessage = message;
    }

    @Override
    public void handleMoveRefused(String reason) {
      moveRefusedCalled = true;
    }
  }

  private static class TestLobbyView extends NetworkLobbyView {
    int welcomeCalls;
    int serverStatusCalls;
    int playersCalls;
    int scoreboardCalls;
    int serverListCalls;
    int messageCalls;
    int invitationReceivedCalls;
    int invitationCancelledCalls;
    int playerDetailsCalls;
    int playerStatusCalls;
    int clientDisconnectedCalls;
    int connectionFailedCalls;
    int gameInterruptedCalls;
    int invitationCancelCalls;
    int pongCalls;
    int gameEndedListCalls;

    private TestLobbyView(NetworkGameBridge bridge) {
      super(bridge);
    }

    @Override
    public void onWelcomeReceived(int id) {
      welcomeCalls++;
    }

    @Override
    public void onServerStatusReceived(Map<String, String> info) {
      serverStatusCalls++;
    }

    @Override
    public void onPongReceived(long latencyMs) {
      pongCalls++;
    }

    @Override
    public void onPlayersReceived(List<Map<String, String>> players) {
      playersCalls++;
    }

    @Override
    public void onScoreboardReceived(List<Map<String, String>> scoreboard) {
      scoreboardCalls++;
    }

    @Override
    public void onServerListUpdated(List<ServerInfo> servers) {
      serverListCalls++;
    }

    @Override
    public void onMessageReceived(String message) {
      messageCalls++;
    }

    @Override
    public void onInvitationReceived(String from) {
      invitationReceivedCalls++;
    }

    @Override
    public void onInvitationCancelled(String reason) {
      invitationCancelledCalls++;
    }

    @Override
    public void onPlayerDetailsReceived(Map<String, String> info) {
      playerDetailsCalls++;
    }

    @Override
    public void onPlayerStatusChanged(String status) {
      playerStatusCalls++;
    }

    @Override
    public void onClientDisconnected(String reason) {
      clientDisconnectedCalls++;
    }

    @Override
    public void onConnectionFailed(String reason) {
      connectionFailedCalls++;
    }

    @Override
    public void onInvitationCancel() {
      invitationCancelCalls++;
    }

    @Override
    public void onGameInterrupted(String reason) {
      gameInterruptedCalls++;
    }

    @Override
    public void onGameEnded(List<Map<String, String>> finalScores) {
      gameEndedListCalls++;
    }
  }
}
