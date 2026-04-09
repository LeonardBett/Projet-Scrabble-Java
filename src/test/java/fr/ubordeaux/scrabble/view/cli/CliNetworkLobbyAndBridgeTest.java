package fr.ubordeaux.scrabble.view.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.i18n.I18n;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.enums.Direction;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.model.network.NetworkManager;
import fr.ubordeaux.scrabble.model.network.server.ServerInfo;
import fr.ubordeaux.scrabble.model.utils.Point;
import fr.ubordeaux.scrabble.view.cli.input.CliInputHandler;
import fr.ubordeaux.scrabble.view.cli.network.CliNetworkBridge;
import fr.ubordeaux.scrabble.view.cli.network.CliNetworkLobby;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CliNetworkLobbyAndBridgeTest {

  @Test
  void cliNetworkBridgeHandlesAllUpdateMethods() {
    FakeNetworkManager nm = new FakeNetworkManager();
    RecordingCliView view = new RecordingCliView();
    CliNetworkBridge bridge = new CliNetworkBridge(nm, view);

    bridge.localModelUpdate();
    bridge.gameEndedUpdate(List.of(Map.of("name", "p1")));
    bridge.gameEndedUpdate(List.of());
    bridge.serverWelcomeUpdate(7);
    bridge.serverStatusUpdate(Map.of("PORT", "12345"));
    bridge.playersUpdate(List.of(Map.of("NAME", "Alice")));
    bridge.playersUpdate(List.of());
    bridge.scoreboardUpdate(List.of(Map.of("WINS", "1")));
    bridge.scoreboardUpdate(List.of());
    bridge.pongUpdate(12L);
    bridge.serverListUpdate(List.of());
    bridge.messageUpdate("hello");
    bridge.invitationReceivedUpdate("Bob");
    bridge.invitationAcceptedUpdate("Bob");
    bridge.invitationDeclinedUpdate("Bob");
    bridge.invitationCancelledUpdate("cancel");
    bridge.playersPlayerIdUpdate(Map.of("ID", "1"));
    bridge.playerStatusUpdate("AWAY");
    bridge.clientDisconnectedUpdate("bye");
    bridge.gameInterruptedUpdate("stop");
    bridge.connectionFailedUpdate("fail");
    bridge.invitationFailedUpdate("bad");

    assertTrue(view.messages.size() >= 10);
    assertTrue(view.errors.size() >= 3);
  }

  @Test
  void lobbyParsersAndCommandsTriggerExpectedNetworkCalls() throws Exception {
    FakeNetworkManager nm = new FakeNetworkManager();
    RecordingCliView view = new RecordingCliView();
    final CliNetworkLobby lobby = new CliNetworkLobby(nm, view, new CliInputHandler());

    assertEquals(42, invokePrivate(lobby, "parseTrailingInt", "host 42"));
    assertEquals(null, invokePrivate(lobby, "parseTrailingInt", "host xx"));

    assertTrue((Boolean) invokePrivate(lobby, "tryDirectJoin", "join 127.0.0.1 12345"));
    assertEquals("127.0.0.1", nm.lastJoinIp);
    assertEquals(12345, nm.lastJoinPort);

    assertTrue((Boolean) invokePrivate(lobby, "tryDirectJoin", "join 127.0.0.1 bad"));
    assertFalse((Boolean) invokePrivate(lobby, "tryDirectJoin", "join 127.0.0.1"));

    invokePrivate(lobby, "handlePlayerInfo", "players 3");
    assertEquals(3, nm.lastPlayerInfoId);

    invokePrivate(lobby, "handleNewInvitation", "new 1");
    assertEquals(List.of(1), nm.lastInvitationIds);

    invokePrivate(lobby, "handleNewInvitation", "new 1 2");
    assertEquals(List.of(1, 2), nm.lastInvitationIds);

    invokePrivate(lobby, "handleNewInvitation", "new 1 2 3");
    assertEquals(List.of(1, 2, 3), nm.lastInvitationIds);

    invokePrivate(lobby, "handlePlay", "play a1h mot");
    assertEquals(1, nm.lastPlayX);
    assertEquals(1, nm.lastPlayY);
    assertEquals("H", nm.lastPlayDirection);
    assertEquals("MOT", nm.lastPlayWord);

    assertTrue((Boolean) invokePrivate(lobby, "tryCliPlayNotation", "a1v test"));
    assertFalse((Boolean) invokePrivate(lobby, "tryCliPlayNotation", "??"));

    invokePrivate(lobby, "handleShowCommand", "show board");
    invokePrivate(lobby, "handleShowCommand", "show history");
    invokePrivate(lobby, "handleShowCommand", "show time");
    invokePrivate(lobby, "handleShowCommand", "show configuration");
    invokePrivate(lobby, "handleShowCommand", "show unknown");

    assertFalse(view.messages.isEmpty());
  }

  @Test
  void lobbyFormatMoveCoversMoveKinds() throws Exception {
    FakeNetworkManager nm = new FakeNetworkManager();
    RecordingCliView view = new RecordingCliView();
    final CliNetworkLobby lobby = new CliNetworkLobby(nm, view, new CliInputHandler());

    HumanPlayer p = new HumanPlayer("Alice", PlayerColor.RED);
    Move pass = Move.createPass(p);
    Move exchange = Move.createExchange(p, List.of(new Tile('A')));
    Move play = Move.createPlay(p, List.of(new Tile('M'), new Tile('O'), new Tile('T')),
      new Point(1, 2), Direction.HORIZONTAL);

    String passText = (String) invokePrivate(lobby, "formatMove", pass);
    String exchangeText = (String) invokePrivate(lobby, "formatMove", exchange);
    String playText = (String) invokePrivate(lobby, "formatMove", play);

    assertNotNull(passText);
    assertNotNull(exchangeText);
    assertNotNull(playText);
  }

  @Test
  void lobbyHandlePlayAndNotationCoverMoreBranches() throws Exception {
    FakeNetworkManager nm = new FakeNetworkManager();
    RecordingCliView view = new RecordingCliView();
    CliNetworkLobby lobby = new CliNetworkLobby(nm, view, new CliInputHandler());

    invokePrivate(lobby, "handlePlay", "play 12av abc");
    assertEquals(12, nm.lastPlayX);
    assertEquals(1, nm.lastPlayY);
    assertEquals("V", nm.lastPlayDirection);
    assertEquals("ABC", nm.lastPlayWord);

    invokePrivate(lobby, "handlePlay", "play bad");
    invokePrivate(lobby, "handlePlay", "play zz1h word");
    assertTrue(view.errors.size() >= 2);

    assertTrue((Boolean) invokePrivate(lobby, "tryCliPlayNotation", "a10g mot"));
    assertEquals("H", nm.lastPlayDirection);

    assertFalse((Boolean) invokePrivate(lobby, "tryCliPlayNotation", "a1x mot"));
    assertFalse((Boolean) invokePrivate(lobby, "tryCliPlayNotation", "a1h"));
  }

  @Test
  void lobbyHandleShowAndInvitationsCoverErrorBranches() throws Exception {
    FakeNetworkManager nm = new FakeNetworkManager();
    RecordingCliView view = new RecordingCliView();
    CliNetworkLobby lobby = new CliNetworkLobby(nm, view, new CliInputHandler());

    invokePrivate(lobby, "handleShowCommand", "show");
    nm.localGameOverride = null;
    invokePrivate(lobby, "handleShowCommand", "show board");
    nm.localGameOverride = RecordingCliView.minimalGame();

    invokePrivate(lobby, "handlePlayerInfo", "player 0");
    invokePrivate(lobby, "handlePlayerInfo", "player nope");

    invokePrivate(lobby, "handleNewInvitation", "new");
    invokePrivate(lobby, "handleNewInvitation", "new a b");
    invokePrivate(lobby, "handleNewInvitation", "new 1 2 3 4");

    assertTrue(view.errors.size() >= 5);
  }

  @Test
  void lobbyShowHistoryAndTimeCoverDataBranches() throws Exception {
    FakeNetworkManager nm = new FakeNetworkManager();
    RecordingCliView view = new RecordingCliView();
    final CliNetworkLobby lobby = new CliNetworkLobby(nm, view, new CliInputHandler());

    Game gameWithHistory = RecordingCliView.minimalGame();
    HumanPlayer p = new HumanPlayer("Hist", PlayerColor.YELLOW);
    gameWithHistory.addPlayer(p);
    gameWithHistory.getUndoRedo().addMove(Move.createPass(p));
    nm.localGameOverride = gameWithHistory;

    invokePrivate(lobby, "handleShowCommand", "show history");
    invokePrivate(lobby, "handleShowCommand", "show time");
    invokePrivate(lobby, "handleShowCommand", "show configuration");

    assertTrue(view.messages.stream().anyMatch(m -> m.contains("Hist")));
  }

  @Test
  void showMenuScriptShouldCoverMainCommandBranches() {
    FakeNetworkManager nm = new FakeNetworkManager();
    RecordingCliView view = new RecordingCliView();
    ScriptedInputHandler input = new ScriptedInputHandler(
        "",
        "host", "abc",
        "host 70000",
        "host 12345",
        "server start",
        "server start 12346",
        "server list",
        "join", "1",
        "join 127.0.0.1 23456",
        "join 127.0.0.1 bad",
        "players",
        "players 3",
        "scoreboard",
        "server status",
        "ping",
        "new 1 2",
        "accept",
        "decline",
        "cancel",
        "away",
        "back",
        "pass",
        "exchange",
        "exchange AZ",
        "play a1h mot",
        "server stop",
        "show board",
        "show history",
        "show time",
        "show configuration",
        "show unknown",
        "hint",
        "a1v test",
        "disconnect",
        "helpnetwork",
        "help",
        "quit",
        "menu");

    CliNetworkLobby lobby = new CliNetworkLobby(nm, view, input);

    lobby.showMenu();

    assertTrue(nm.startOnlinePlayCalls >= 1);
    assertTrue(nm.stopOnlinePlayCalls >= 1);
    assertTrue(nm.serverStartCalls >= 1);
    assertTrue(nm.serverStartPorts.contains(NetworkManager.DEFAULT_TCP_PORT));
    assertTrue(nm.serverStartPorts.contains(12346));
    assertTrue(nm.serverStopCalls >= 1);
    assertTrue(nm.joinCalls >= 2);
    assertTrue(nm.playersCalls >= 1);
    assertTrue(nm.scoreboardCalls >= 1);
    assertTrue(nm.serverStatusCalls >= 1);
    assertTrue(nm.pingCalls >= 1);
    assertTrue(nm.backCalls >= 1);
    assertTrue(nm.passCalls >= 1);
    assertTrue(nm.exchangeCalls >= 1);
    assertTrue(nm.playCalls >= 1);
    assertTrue(nm.quitCalls >= 1);
    assertTrue(nm.stopOnlinePlayCalls >= 1);
    String leftLobbyMessage = I18n.translate("cli.network.leftLobby");
    String fullHelpMessage = I18n.translate("cli.network.help");
    String shortHelpMessage = I18n.translate("cli.network.helpNetworkHint");
    assertTrue(view.messages.stream().anyMatch(m -> m.contains(leftLobbyMessage)));
    assertTrue(view.messages.stream().anyMatch(m -> m.contains(fullHelpMessage)
            || m.contains(shortHelpMessage)));
    assertTrue(view.errors.size() >= 3);
  }

  private static Object invokePrivate(Object target, String methodName, Object... args)
      throws Exception {
    Method m = resolveMethod(target.getClass(), methodName, args.length);
    m.setAccessible(true);
    return m.invoke(target, args);
  }

  private static Method resolveMethod(Class<?> type, String name, int arity) {
    for (Method m : type.getDeclaredMethods()) {
      if (m.getName().equals(name) && m.getParameterCount() == arity) {
        return m;
      }
    }
    throw new IllegalArgumentException("Method not found: " + name + "/" + arity);
  }

  private static final class RecordingCliView extends CliView {
    private final List<String> messages = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    private RecordingCliView() {
      super(minimalGame());
    }

    @Override
    public void displayMessage(String message) {
      messages.add(message);
    }

    @Override
    public void displayError(String error) {
      errors.add(error);
    }

    private static Game minimalGame() {
      Game g = new Game();
      g.addPlayer(new HumanPlayer("A", PlayerColor.RED));
      g.addPlayer(new HumanPlayer("B", PlayerColor.BLUE));
      return g;
    }
  }

  private static final class FakeNetworkManager extends NetworkManager {
    private String lastJoinIp;
    private int lastJoinPort;
    private int lastPlayerInfoId;
    private List<Integer> lastInvitationIds = List.of();
    private int lastPlayX;
    private int lastPlayY;
    private String lastPlayDirection;
    private String lastPlayWord;
    private final Game local = RecordingCliView.minimalGame();
    private Game localGameOverride;
    private int startOnlinePlayCalls;
    private int stopOnlinePlayCalls;
    private int joinCalls;
    private int serverStartCalls;
    private final List<Integer> serverStartPorts = new ArrayList<>();
    private int serverStopCalls;
    private int playersCalls;
    private int scoreboardCalls;
    private int serverStatusCalls;
    private int pingCalls;
    private int backCalls;
    private int passCalls;
    private int exchangeCalls;
    private int playCalls;
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
      serverStartPorts.add(port);
      return true;
    }

    @Override
    public void serverStop() {
      serverStopCalls++;
    }

    @Override
    public boolean join(String address, int port) {
      joinCalls++;
      this.lastJoinIp = address;
      this.lastJoinPort = port;
      return true;
    }

    @Override
    public void quit() {
      quitCalls++;
    }

    @Override
    public void playersPlayerId(int playerId) {
      this.lastPlayerInfoId = playerId;
    }

    @Override
    public void newPlayerId(int playerId1) {
      this.lastInvitationIds = List.of(playerId1);
    }

    @Override
    public void newPlayerId(int playerId1, int playerId2) {
      this.lastInvitationIds = List.of(playerId1, playerId2);
    }

    @Override
    public void newPlayerId(int playerId1, int playerId2, int playerId3) {
      this.lastInvitationIds = List.of(playerId1, playerId2, playerId3);
    }

    @Override
    public void play(int x, int y, String direction, String word) {
      playCalls++;
      this.lastPlayX = x;
      this.lastPlayY = y;
      this.lastPlayDirection = direction;
      this.lastPlayWord = word;
    }

    @Override
    public void exchange(String letters) {
      exchangeCalls++;
    }

    @Override
    public void pass() {
      passCalls++;
    }

    @Override
    public void ping() {
      pingCalls++;
    }

    @Override
    public void back() {
      backCalls++;
    }

    @Override
    public Game getLocalGame() {
      return localGameOverride != null ? localGameOverride : local;
    }

    @Override
    public List<ServerInfo> serverList() {
      return List.of(new ServerInfo("127.0.0.1", 12345, "S"));
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
  }

  private static final class ScriptedInputHandler extends CliInputHandler {
    private final Deque<String> inputs;

    private ScriptedInputHandler(String... scriptedInputs) {
      this.inputs = new ArrayDeque<>(List.of(scriptedInputs));
    }

    @Override
    public String askAction() {
      if (inputs.isEmpty()) {
        return "quit";
      }
      return inputs.removeFirst();
    }
  }
}
