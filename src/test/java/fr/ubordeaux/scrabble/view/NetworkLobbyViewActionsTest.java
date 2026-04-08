package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.network.NetworkManager;
import fr.ubordeaux.scrabble.model.network.server.ServerInfo;
import fr.ubordeaux.scrabble.view.gui.network.NetworkGameBridge;
import fr.ubordeaux.scrabble.view.gui.network.NetworkLobbyView;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NetworkLobbyViewActionsTest {

  private FakeNetworkManager networkManager;
  private NetworkLobbyView lobby;

  @BeforeAll
  static void initToolkit() {
    try {
      com.sun.javafx.application.PlatformImpl.startup(() -> {
      });
    } catch (Exception e) {
      // Already initialized
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    networkManager = new FakeNetworkManager();
    NetworkGameBridge bridge = new NetworkGameBridge(networkManager);

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<NetworkLobbyView> ref = new AtomicReference<>();
    Platform.runLater(() -> {
      try {
        ref.set(new NetworkLobbyView(bridge));
      } finally {
        latch.countDown();
      }
    });

    assertTrue(latch.await(5, TimeUnit.SECONDS));
    lobby = ref.get();
  }

  @AfterEach
  void tearDown() throws Exception {
    runOnFx(() -> {
      lobby.hide();
      lobby.close();
    });
  }

  @Test
  void privateLobbyActionsShouldTriggerControllerCommands() throws Exception {
    runOnFx(() -> {
      TextField portField = (TextField) getField(lobby, "portField");
      portField.setText("bad-port");
      invokePrivate(lobby, "onStartServer");

      portField.setText("12345");
      invokePrivate(lobby, "onStartServer");
      invokePrivate(lobby, "onStopServer");

      TextField ipField = (TextField) getField(lobby, "ipField");
      TextField joinPortField = (TextField) getField(lobby, "joinPortField");

      joinPortField.setText("oops");
      invokePrivate(lobby, "onConnect");

      ipField.setText("127.0.0.1");
      joinPortField.setText("23456");
      invokePrivate(lobby, "onConnect");

      invokePrivate(lobby, "onRefreshPlayers");
      invokePrivate(lobby, "onRefreshScoreboard");

      lobby.onServerListUpdated(List.of(new ServerInfo("127.0.0.1", 34567, "srv")));
      @SuppressWarnings("unchecked")
      ListView<String> serverList = (ListView<String>) getField(lobby, "serverListView");
      serverList.getSelectionModel().select(0);
      invokePrivate(lobby, "onJoinSelected");

      lobby.onPlayersReceived(List.of(
          Map.of("ID", "1", "NAME", "Alice", "STATUS", "IDLE"),
          Map.of("ID", "2", "NAME", "Bob", "STATUS", "AWAY")));

      @SuppressWarnings("unchecked")
      ListView<String> playersList = (ListView<String>) getField(lobby, "playersListView");
      playersList.getSelectionModel().selectIndices(0, 1);
      invokePrivate(lobby, "onInvitePlayers");
      invokePrivate(lobby, "onCancelInvitation");

      playersList.getSelectionModel().clearAndSelect(0);
      invokePrivate(lobby, "onViewPlayerDetails");

      setField(lobby, "isAway", false);
      invokePrivate(lobby, "onToggleStatus");
      setField(lobby, "isAway", true);
      invokePrivate(lobby, "onToggleStatus");

      invokePrivate(lobby, "onDisconnect");
      invokePrivate(lobby, "onLobbyClose");
    });

    assertTrue(networkManager.startOnlinePlayCalls >= 1);
    assertTrue(networkManager.stopOnlinePlayCalls >= 1);
    assertTrue(networkManager.serverStartCalls >= 1);
    assertTrue(networkManager.serverStopCalls >= 1);
    assertTrue(networkManager.joinCalls >= 2);
    assertTrue(networkManager.playersCalls >= 1);
    assertTrue(networkManager.scoreboardCalls >= 1);
    assertTrue(networkManager.newPlayerId2Calls >= 1);
    assertTrue(networkManager.cancelCalls >= 1);
    assertTrue(networkManager.playersPlayerIdCalls >= 1);
    assertTrue(networkManager.awayCalls >= 1);
    assertTrue(networkManager.backCalls >= 1);
    assertTrue(networkManager.quitCalls >= 1);
  }

  private static void runOnFx(Runnable action) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<Throwable> error = new AtomicReference<>();
    Platform.runLater(() -> {
      try {
        action.run();
      } catch (Throwable t) {
        error.set(t);
      } finally {
        latch.countDown();
      }
    });

    assertTrue(latch.await(5, TimeUnit.SECONDS));
    if (error.get() != null) {
      throw new RuntimeException(error.get());
    }
  }

  private static Object getField(Object target, String name) {
    try {
      Field field = target.getClass().getDeclaredField(name);
      field.setAccessible(true);
      return field.get(target);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private static void setField(Object target, String name, Object value) {
    try {
      Field field = target.getClass().getDeclaredField(name);
      field.setAccessible(true);
      field.set(target, value);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private static Object invokePrivate(Object target, String methodName) {
    try {
      Method method = target.getClass().getDeclaredMethod(methodName);
      method.setAccessible(true);
      return method.invoke(target);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  private static final class FakeNetworkManager extends NetworkManager {

    private int startOnlinePlayCalls;
    private int stopOnlinePlayCalls;
    private int serverStartCalls;
    private int serverStopCalls;
    private int joinCalls;
    private int playersCalls;
    private int scoreboardCalls;
    private int newPlayerId2Calls;
    private int cancelCalls;
    private int playersPlayerIdCalls;
    private int awayCalls;
    private int backCalls;
    private int quitCalls;

    @Override
    public void startOnlinePlay() {
      startOnlinePlayCalls++;
    }

    @Override
    public void stopOnlinePlay() {
      stopOnlinePlayCalls++;
    }

    @Override
    public boolean serverStart(int port) {
      serverStartCalls++;
      return true;
    }

    @Override
    public void serverStop() {
      serverStopCalls++;
    }

    @Override
    public void join(String address, int port) {
      joinCalls++;
    }

    @Override
    public void players() {
      playersCalls++;
    }

    @Override
    public void scoreboard() {
      scoreboardCalls++;
    }

    @Override
    public void newPlayerId(int playerId1, int playerId2) {
      newPlayerId2Calls++;
    }

    @Override
    public void cancel() {
      cancelCalls++;
    }

    @Override
    public void playersPlayerId(int playerId) {
      playersPlayerIdCalls++;
    }

    @Override
    public void away() {
      awayCalls++;
    }

    @Override
    public void back() {
      backCalls++;
    }

    @Override
    public void quit() {
      quitCalls++;
    }
  }
}
