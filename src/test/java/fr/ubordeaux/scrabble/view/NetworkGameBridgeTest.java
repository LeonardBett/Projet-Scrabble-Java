package fr.ubordeaux.scrabble.view;

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
import fr.ubordeaux.scrabble.view.gui.NetworkGameBridge;
import fr.ubordeaux.scrabble.view.gui.ScrabbleGui;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NetworkGameBridgeTest {

  @BeforeAll
  static void initToolkit() {
    try {
      com.sun.javafx.application.PlatformImpl.startup(() -> {
      });
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
    assertTrue((boolean) invokeStatic("shouldDispatchGameStart",
        new Class<?>[] { boolean.class, int.class }, true, 2));
    assertFalse((boolean) invokeStatic("shouldDispatchGameStart",
        new Class<?>[] { boolean.class, int.class }, true, 1));
    assertFalse((boolean) invokeStatic("shouldDispatchGameStart",
        new Class<?>[] { boolean.class, int.class }, false, 4));

    assertEquals(7, invokeStatic("parsePlayerId",
        new Class<?>[] { Map.class }, Map.of("ID", "7")));
    assertEquals(0, invokeStatic("parsePlayerId",
        new Class<?>[] { Map.class }, Map.of("ID", "abc")));
    assertEquals(0, invokeStatic("parsePlayerId",
        new Class<?>[] { Map.class }, Map.of()));

    int[] ids = (int[]) invokeStatic("extractPositivePlayerIds",
        new Class<?>[] { List.class },
        List.of(Map.of("ID", "1"), Map.of("ID", "x"), Map.of("ID", "3"), Map.of()));
    assertEquals(2, ids.length);
    assertEquals(1, ids[0]);
    assertEquals(3, ids[1]);
  }

  @Test
  void dispatchHelperShouldChooseCorrectNewPlayerOverload() {
    FakeNetworkManager manager = new FakeNetworkManager();
    assertEquals(0, invokeStatic("dispatchNewGame",
        new Class<?>[] { NetworkManager.class, int[].class }, manager, new int[] { 1 }));
    assertEquals(1, invokeStatic("dispatchNewGame",
        new Class<?>[] { NetworkManager.class, int[].class }, manager, new int[] { 1, 2 }));
    assertEquals(2, invokeStatic("dispatchNewGame",
        new Class<?>[] { NetworkManager.class, int[].class }, manager,
        new int[] { 1, 2, 3 }));
    assertEquals(3, invokeStatic("dispatchNewGame",
        new Class<?>[] { NetworkManager.class, int[].class }, manager,
        new int[] { 1, 2, 3, 4 }));
    assertEquals(1, manager.new1Calls);
    assertEquals(1, manager.new2Calls);
    assertEquals(1, manager.new3Calls);
  }

  @Test
  void dispatchNewGameReturnsZeroForEmptyIds() {
    FakeNetworkManager manager = new FakeNetworkManager();
    assertEquals(0, invokeStatic("dispatchNewGame",
        new Class<?>[] { NetworkManager.class, int[].class }, manager, new int[] {}));
  }

  @Test
  void dispatchNewGameHandlesFiveIds() {
    FakeNetworkManager manager = new FakeNetworkManager();
    assertEquals(3, invokeStatic("dispatchNewGame",
        new Class<?>[] { NetworkManager.class, int[].class }, manager,
        new int[] { 1, 2, 3, 4, 5 }));
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
    assertFalse((boolean) invokeStatic("shouldDispatchGameStart",
        new Class<?>[] { boolean.class, int.class }, false, 2));
    assertTrue((boolean) invokeStatic("shouldDispatchGameStart",
        new Class<?>[] { boolean.class, int.class }, true, 3));
    assertTrue((boolean) invokeStatic("shouldDispatchGameStart",
        new Class<?>[] { boolean.class, int.class }, true, 4));
  }

  @Test
  void parsePlayerIdVariousInputs() {
    assertEquals(0, invokeStatic("parsePlayerId",
        new Class<?>[] { Map.class }, Map.of("ID", "0")));
    assertEquals(99, invokeStatic("parsePlayerId",
        new Class<?>[] { Map.class }, Map.of("ID", "99")));
  }

  @Test
  void extractPositivePlayerIdsAllZeros() {
    int[] ids = (int[]) invokeStatic("extractPositivePlayerIds",
        new Class<?>[] { List.class },
        List.of(Map.of("ID", "0"), Map.of("ID", "0")));
    assertEquals(0, ids.length);
  }

  @Test
  void extractPositivePlayerIdsAllPositive() {
    int[] ids = (int[]) invokeStatic("extractPositivePlayerIds",
        new Class<?>[] { List.class },
        List.of(Map.of("ID", "1"), Map.of("ID", "2"), Map.of("ID", "3")));
    assertEquals(3, ids.length);
  }

  private static Object invokeStatic(String methodName, Class<?>[] argTypes,
      Object... args) {
    try {
      Method method = NetworkGameBridge.class.getDeclaredMethod(methodName, argTypes);
      method.setAccessible(true);
      return method.invoke(null, args);
    } catch (NoSuchMethodException | IllegalAccessException
        | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  private static void waitFx() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(latch::countDown);
    assertTrue(latch.await(2, TimeUnit.SECONDS),
        "Timeout waiting for JavaFX event queue");
  }

  private static class FakeNetworkManager extends NetworkManager {
    int playersCalls;
    int new1Calls;
    int new2Calls;
    int new3Calls;
    boolean removeObserverCalled;
    boolean stopOnlineCalled;
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
    public void removeObserver(
        fr.ubordeaux.scrabble.model.network.NetworkObserver observer) {
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
  }
}
