package fr.ubordeaux.scrabble.controller.network;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.network.NetworkManager;
import fr.ubordeaux.scrabble.model.network.server.ServerInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NetworkLobbyControllerTest {

  private RecordingNetworkManager networkManager;
  private NetworkLobbyController controller;

  @BeforeEach
  void setUp() {
    networkManager = new RecordingNetworkManager();
    controller = new NetworkLobbyController(networkManager);
  }

  @Test
  void parsePortShouldHandleValidAndInvalidValues() {
    assertEquals(OptionalInt.of(12345), controller.parsePort("12345"));
    assertEquals(OptionalInt.empty(), controller.parsePort(null));
    assertEquals(OptionalInt.empty(), controller.parsePort("abc"));
    assertEquals(OptionalInt.empty(), controller.parsePort("70000"));
  }

  @Test
  void parseLobbyPlayerIdShouldExtractIds() {
    assertEquals(OptionalInt.of(12), controller.parseLobbyPlayerId("#12 Alice [IDLE]"));
    assertEquals(OptionalInt.empty(), controller.parseLobbyPlayerId(null));
    assertEquals(OptionalInt.empty(), controller.parseLobbyPlayerId("   "));
    assertEquals(OptionalInt.empty(), controller.parseLobbyPlayerId("Alice"));
  }

  @Test
  void parseLobbyPlayerIdsShouldFilterInvalidEntries() {
    List<Integer> ids = controller.parseLobbyPlayerIds(List.of("#3 Alice", "bad", "#7 Bob"));

    assertEquals(List.of(3, 7), ids);
  }

  @Test
  void shouldDispatchGameStartShouldRequirePendingStateAndEnoughPlayers() {
    assertTrue(NetworkLobbyController.shouldDispatchGameStart(true, 2));
    assertFalse(NetworkLobbyController.shouldDispatchGameStart(false, 2));
    assertFalse(NetworkLobbyController.shouldDispatchGameStart(true, 1));
  }

  @Test
  void parsePlayerIdShouldReturnZeroForInvalidIds() {
    Map<String, String> valid = Map.of("ID", "42");
    Map<String, String> invalid = Map.of("ID", "abc");

    assertEquals(42, NetworkLobbyController.parsePlayerId(valid));
    assertEquals(0, NetworkLobbyController.parsePlayerId(invalid));
  }

  @Test
  void extractPositivePlayerIdsShouldSortAndFilterValues() {
    List<Map<String, String>> players = List.of(
        Map.of("ID", "5"),
        Map.of("ID", "-1"),
        Map.of("ID", "3"),
        Map.of("ID", "abc"),
        Map.of("ID", "8"));

    assertArrayEquals(new int[] {3, 5, 8},
        NetworkLobbyController.extractPositivePlayerIds(players));
  }

  @Test
  void dispatchNewGameShouldCallCorrectNetworkManagerOverload() {
    assertEquals(0, NetworkLobbyController.dispatchNewGame(networkManager, new int[] {1}));
    assertEquals(1, NetworkLobbyController.dispatchNewGame(networkManager, new int[] {1, 2}));
    assertEquals(2, NetworkLobbyController.dispatchNewGame(networkManager, new int[] {1, 2, 3}));
    assertEquals(3, NetworkLobbyController.dispatchNewGame(networkManager, new int[] {1, 2, 3, 4}));

    assertEquals(List.of(2), networkManager.calls);
    assertEquals(1, networkManager.calls2.size());
    assertEquals(List.of(2, 3), networkManager.calls2.getFirst());
    assertEquals(1, networkManager.calls3.size());
    assertEquals(List.of(2, 3, 4), networkManager.calls3.getFirst());
  }

  @Test
  void requestGameStartShouldTriggerPlayersRequest() {
    controller.requestGameStart();

    assertEquals(1, networkManager.playersCalls);
  }

  @Test
  void handlePlayersUpdateShouldIgnoreMissingPendingRequest() {
    controller.handlePlayersUpdate(List.of(Map.of("ID", "1"), Map.of("ID", "2")));

    assertEquals(0, networkManager.calls.size());
  }

  @Test
  void handlePlayersUpdateShouldDispatchHostGameStart() {
    controller.requestGameStart();
    controller.handlePlayersUpdate(List.of(
        Map.of("ID", "1"),
        Map.of("ID", "2"),
        Map.of("ID", "3")));

    assertEquals(List.of(2, 3), networkManager.calls2.getFirst());
  }

  @Test
  void directDelegationMethodsShouldReachNetworkManager() {
    controller.startOnlinePlay();
    controller.stopOnlinePlay();
    assertTrue(controller.serverStart(12345));
    controller.serverStop();
    controller.join("127.0.0.1", 12345);
    controller.quit();
    controller.ping();
    controller.serverStatus();
    controller.players();
    controller.scoreboard();
    controller.newPlayerId(7);
    controller.newPlayerId(7, 8);
    controller.newPlayerId(7, 8, 9);
    controller.play(1, 2, "H", "TEST");
    controller.exchange("ABC");
    controller.pass();
    controller.accept();
    controller.decline();
    controller.playersPlayerId(4);
    controller.away();
    controller.back();
    controller.cancel();

    assertEquals(1, networkManager.startOnlinePlayCalls);
    assertEquals(1, networkManager.stopOnlinePlayCalls);
    assertEquals(1, networkManager.serverStartCalls);
    assertEquals(1, networkManager.serverStopCalls);
    assertEquals(1, networkManager.joinCalls);
    assertEquals(1, networkManager.quitCalls);
    assertEquals(1, networkManager.pingCalls);
    assertEquals(1, networkManager.serverStatusCalls);
    assertEquals(1, networkManager.playersCalls);
    assertEquals(1, networkManager.scoreboardCalls);
    assertEquals(List.of(7), networkManager.calls);
    assertEquals(List.of(7, 8), networkManager.calls2.getFirst());
    assertEquals(List.of(7, 8, 9), networkManager.calls3.getFirst());
    assertEquals(1, networkManager.playCalls);
    assertEquals(1, networkManager.exchangeCalls);
    assertEquals(1, networkManager.passCalls);
    assertEquals(1, networkManager.acceptCalls);
    assertEquals(1, networkManager.declineCalls);
    assertEquals(1, networkManager.playersPlayerIdCalls);
    assertEquals(1, networkManager.awayCalls);
    assertEquals(1, networkManager.backCalls);
    assertEquals(1, networkManager.cancelCalls);
  }

  private static final class RecordingNetworkManager extends NetworkManager {

    int startOnlinePlayCalls;
    int stopOnlinePlayCalls;
    int serverStartCalls;
    int serverStopCalls;
    int joinCalls;
    int quitCalls;
    int pingCalls;
    int serverStatusCalls;
    int playersCalls;
    int scoreboardCalls;
    int playCalls;
    int exchangeCalls;
    int passCalls;
    int acceptCalls;
    int declineCalls;
    int playersPlayerIdCalls;
    int awayCalls;
    int backCalls;
    int cancelCalls;
    final List<Integer> calls = new ArrayList<>();
    final List<List<Integer>> calls2 = new ArrayList<>();
    final List<List<Integer>> calls3 = new ArrayList<>();

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
    public void quit() {
      quitCalls++;
    }

    @Override
    public void ping() {
      pingCalls++;
    }

    @Override
    public void serverStatus() {
      serverStatusCalls++;
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
    public void newPlayerId(int targetId) {
      calls.add(targetId);
    }

    @Override
    public void newPlayerId(int targetId1, int targetId2) {
      calls2.add(List.of(targetId1, targetId2));
    }

    @Override
    public void newPlayerId(int targetId1, int targetId2, int targetId3) {
      calls3.add(List.of(targetId1, targetId2, targetId3));
    }

    @Override
    public void play(int x, int y, String direction, String tile) {
      playCalls++;
    }

    @Override
    public void exchange(String tiles) {
      exchangeCalls++;
    }

    @Override
    public void pass() {
      passCalls++;
    }

    @Override
    public void accept() {
      acceptCalls++;
    }

    @Override
    public void decline() {
      declineCalls++;
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
    public void cancel() {
      cancelCalls++;
    }
  }
}