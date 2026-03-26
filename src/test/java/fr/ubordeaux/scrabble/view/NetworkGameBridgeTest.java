package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.network.NetworkManager;
import fr.ubordeaux.scrabble.view.gui.NetworkGameBridge;
import fr.ubordeaux.scrabble.view.gui.ScrabbleGui;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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

  private static void waitFx() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(latch::countDown);
    latch.await(2, TimeUnit.SECONDS);
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
