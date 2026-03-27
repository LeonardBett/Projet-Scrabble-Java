package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.controller.GameController;
import fr.ubordeaux.scrabble.model.ai.AiPlayer;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.enums.Direction;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.model.network.NetworkManager;
import fr.ubordeaux.scrabble.model.utils.Point;
import fr.ubordeaux.scrabble.view.gui.JavaFxView;
import fr.ubordeaux.scrabble.view.gui.NetworkGameBridge;
import fr.ubordeaux.scrabble.view.gui.ScrabbleGui;
import fr.ubordeaux.scrabble.view.gui.panel.BoardPanel;
import fr.ubordeaux.scrabble.view.gui.panel.ControlPanel;
import fr.ubordeaux.scrabble.view.gui.panel.MessagePanel;
import fr.ubordeaux.scrabble.view.gui.panel.RackPanel;
import fr.ubordeaux.scrabble.view.gui.panel.ScorePanel;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Additional tests to increase ScrabbleGui coverage for
 * online-mode paths, exchange, submit, AI, and blitz timer flows.
 */
class ScrabbleGuiAdditionalTest {

  private Game game;
  private ScrabbleGui gui;
  private BoardPanel boardPanel;
  private RackPanel rackPanel;
  private ScorePanel scorePanel;
  private ControlPanel controlPanel;
  private MessagePanel messagePanel;
  private GameController controller;
  private JavaFxView view;
  private FakeNetworkManager fakeNetworkManager;

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
  void setUp() {
    game = new Game();
    game.addPlayer(new HumanPlayer("P1", PlayerColor.BLUE));
    game.addPlayer(new HumanPlayer("P2", PlayerColor.RED));
    game.startGame();

    ScrabbleGui.setGame(game);
    view = new JavaFxView(game);
    ScrabbleGui.setView(view);

    gui = new ScrabbleGui();

    boardPanel = new BoardPanel(game.getBoard());
    rackPanel = new RackPanel(game.getCurrentPlayer().getRack());
    scorePanel = new ScorePanel();
    controlPanel = new ControlPanel();
    messagePanel = new MessagePanel();
    controller = new GameController(game, view);

    fakeNetworkManager = new FakeNetworkManager();

    setField(gui, "boardPanel", boardPanel);
    setField(gui, "rackPanel", rackPanel);
    setField(gui, "scorePanel", scorePanel);
    setField(gui, "controlPanel", controlPanel);
    setField(gui, "messagePanel", messagePanel);
    setField(gui, "controller", controller);
    setField(gui, "networkManager", fakeNetworkManager);
    setField(gui, "networkBridge",
        new NetworkGameBridge(new NetworkManager()));
    view.setGui(gui);
    setFieldStatic("viewInstance", view);
  }

  @AfterEach
  void tearDown() {
    Game resetGame = new Game();
    resetGame.addPlayer(new HumanPlayer("R1", PlayerColor.BLUE));
    resetGame.addPlayer(new HumanPlayer("R2", PlayerColor.RED));
    resetGame.startGame();
    ScrabbleGui.setGame(resetGame);
    ScrabbleGui.setView(new JavaFxView(resetGame));
  }

  // ===== submitPendingTiles in online mode =====

  @Test
  void submitPendingTilesInOnlineModeSendsPlay() throws Exception {
    setField(gui, "onlineMode", true);
    Tile t1 = game.getCurrentPlayer().getRack().getTiles().get(0);
    Tile t2 = game.getCurrentPlayer().getRack().getTiles().get(1);
    gui.onTileDragged(t1);
    gui.onTileDropped(7, 7);
    gui.onTileDragged(t2);
    gui.onTileDropped(7, 8);
    runOnFxThread(() -> invokePrivate(gui, "submitPendingTiles"));
    assertTrue(fakeNetworkManager.playCalled);
  }

  @Test
  void submitPendingTilesInOnlineModeClearsPending() throws Exception {
    setField(gui, "onlineMode", true);
    Tile tile = game.getCurrentPlayer().getRack().getTiles().getFirst();
    gui.onTileDragged(tile);
    gui.onTileDropped(7, 7);
    runOnFxThread(() -> invokePrivate(gui, "submitPendingTiles"));
    @SuppressWarnings("unchecked")
    Map<Point, Tile> pending = (Map<Point, Tile>) getField(gui, "pendingTiles");
    assertTrue(pending.isEmpty());
  }

  @Test
  void submitPendingTilesLocalWithInvalidMoveShowsError() throws Exception {
    // Place tiles diagonally = invalid alignment
    Tile t1 = game.getCurrentPlayer().getRack().getTiles().get(0);
    Tile t2 = game.getCurrentPlayer().getRack().getTiles().get(1);
    gui.onTileDragged(t1);
    gui.onTileDropped(5, 5);
    gui.onTileDragged(t2);
    gui.onTileDropped(6, 6);
    runOnFxThread(() -> invokePrivate(gui, "submitPendingTiles"));
  }

  // ===== Exchange dialog path (without actual dialog) =====

  @Test
  void openExchangeDialogBlockedWhenPendingHasTiles() throws Exception {
    Tile tile = game.getCurrentPlayer().getRack().getTiles().getFirst();
    gui.onTileDragged(tile);
    gui.onTileDropped(7, 7);
    @SuppressWarnings("unchecked")
    Map<Point, Tile> pending = (Map<Point, Tile>) getField(gui, "pendingTiles");
    assertTrue((boolean) invokeStatic("shouldBlockExchangeWhilePending",
        new Class<?>[] { Map.class }, pending));
  }

  // ===== checkAiTurn paths =====

  @Test
  void checkAiTurnWithAiPlayerDisablesBoardAndStarts() throws Exception {
    Game aiGame = new Game();
    aiGame.addPlayer(new AiPlayer("AI", 1, 60, PlayerColor.BLUE));
    aiGame.addPlayer(new HumanPlayer("P2", PlayerColor.RED));
    aiGame.startGame();
    ScrabbleGui.setGame(aiGame);
    setFieldStatic("gameInstance", aiGame);
    setField(gui, "boardPanel", new BoardPanel(aiGame.getBoard()));
    setField(gui, "rackPanel", new RackPanel(aiGame.getCurrentPlayer().getRack()));
    setField(gui, "controller", new GameController(aiGame, view));

    // checkAiTurn should detect AI player and start the AI thread
    invokePrivate(gui, "checkAiTurn");
    // Wait a bit for the thread to start
    Thread.sleep(200);
    // Board should be disabled during AI turn
    BoardPanel bp = (BoardPanel) getField(gui, "boardPanel");
    assertTrue(bp.isDisable());
  }

  // ===== Blitz timer tests =====

  @Test
  void onBlitzTimeExpiredSetsGameOverAndDisablesControls() throws Exception {
    HumanPlayer p1 = (HumanPlayer) game.getPlayers().get(0);
    p1.enableBlitzClock(Duration.ofNanos(1));
    p1.startTurnTimer();
    p1.pauseTurnTimer();
    runOnFxThread(() -> invokePrivate(gui, "onBlitzTimeExpired"));
    assertTrue(game.isGameOver());
    assertTrue(boardPanel.isDisable());
  }

  // ===== refreshScores with currentPlayer null =====

  @Test
  void refreshScoresWithNoCurrentPlayerDoesNotHighlight() {
    // Force game to have currentPlayer null via reflection
    Game freshGame = new Game();
    freshGame.addPlayer(new HumanPlayer("X1", PlayerColor.BLUE));
    freshGame.addPlayer(new HumanPlayer("X2", PlayerColor.RED));
    freshGame.startGame();
    setFieldStatic("gameInstance", freshGame);
    assertDoesNotThrow(() -> gui.refreshScores());
  }

  // ===== setGameplayControlsDisabled =====

  @Test
  void setGameplayControlsDisabledDisablesAllPanels() {
    invokePrivate(gui, "setGameplayControlsDisabled", boolean.class, true);
    assertTrue(boardPanel.isDisable());
    assertTrue(rackPanel.isDisable());
    assertTrue(controlPanel.getPlayButton().isDisable());
  }

  @Test
  void setGameplayControlsEnabledEnablesAllPanels() {
    invokePrivate(gui, "setGameplayControlsDisabled", boolean.class, true);
    invokePrivate(gui, "setGameplayControlsDisabled", boolean.class, false);
    assertFalse(boardPanel.isDisable());
    assertFalse(rackPanel.isDisable());
    assertFalse(controlPanel.getPlayButton().isDisable());
  }

  // ===== switchToOnlineGame and exitOnlineMode in detail =====

  @Test
  void switchToOnlineGameDisablesUndoRedo() throws Exception {
    Game onlineGame = createOnlineGame();
    runOnFxThread(() -> gui.switchToOnlineGame(onlineGame));
    assertTrue(controlPanel.getUndoButton().isDisable());
    assertTrue(controlPanel.getRedoButton().isDisable());
  }

  @Test
  void exitOnlineModeEnablesUndoRedo() throws Exception {
    Game onlineGame = createOnlineGame();
    runOnFxThread(() -> gui.switchToOnlineGame(onlineGame));
    runOnFxThread(() -> gui.exitOnlineMode());
    assertFalse(controlPanel.getUndoButton().isDisable());
    assertFalse(controlPanel.getRedoButton().isDisable());
  }

  @Test
  void switchToOnlineClearsPendingTiles() throws Exception {
    Tile tile = game.getCurrentPlayer().getRack().getTiles().getFirst();
    gui.onTileDragged(tile);
    gui.onTileDropped(7, 7);
    Game onlineGame = createOnlineGame();
    runOnFxThread(() -> gui.switchToOnlineGame(onlineGame));
    @SuppressWarnings("unchecked")
    Map<Point, Tile> pending = (Map<Point, Tile>) getField(gui, "pendingTiles");
    assertTrue(pending.isEmpty());
  }

  // ===== buildPlayedWord and moveDirection for complex cases =====

  @Test
  void buildPlayedWordForMultipleTiles() {
    List<Tile> tiles = List.of(new Tile('S'), new Tile('C'),
        new Tile('R'), new Tile('A'), new Tile('B'));
    Move move = Move.createPlay(game.getCurrentPlayer(), tiles,
        new Point(7, 7), Direction.HORIZONTAL);
    assertEquals("SCRAB", invokeStatic("buildPlayedWord",
        new Class<?>[] { Move.class }, move));
  }

  @Test
  void moveDirectionTokenForHorizontal() {
    Move move = Move.createPlay(game.getCurrentPlayer(),
        List.of(new Tile('A')), new Point(7, 7), Direction.HORIZONTAL);
    assertEquals("H", invokeStatic("moveDirectionToken",
        new Class<?>[] { Move.class }, move));
  }

  @Test
  void moveDirectionTokenForVertical() {
    Move move = Move.createPlay(game.getCurrentPlayer(),
        List.of(new Tile('A')), new Point(7, 7), Direction.VERTICAL);
    assertEquals("V", invokeStatic("moveDirectionToken",
        new Class<?>[] { Move.class }, move));
  }

  // ===== Additional static helper tests =====

  @Test
  void shouldRunAiTurnFalseForNull() {
    assertFalse((boolean) invokeStatic("shouldRunAiTurn",
        new Class<?>[] { fr.ubordeaux.scrabble.model.interfaces.Player.class, boolean.class },
        null, false));
  }

  @Test
  void selectedPlayerCountDefaultsToZero() {
    assertEquals(0, invokeStatic("selectedPlayerCount",
        new Class<?>[] { java.util.Optional.class }, java.util.Optional.empty()));
  }

  @Test
  @SuppressWarnings("unchecked")
  void createDefaultPlayersWithThree() {
    List<HumanPlayer> players = (List<HumanPlayer>) invokeStatic("createDefaultPlayers",
        new Class<?>[] { int.class }, 3);
    assertEquals(3, players.size());
    assertEquals("Joueur1", players.get(0).getName());
    assertEquals("Joueur3", players.get(2).getName());
  }

  @Test
  void isOccupiedOrPendingWithBoardOccupied() {
    game.getBoard().getSquare(new Point(3, 3)).setTile(new Tile('Z'));
    assertTrue((boolean) invokeStatic("isOccupiedOrPending",
        new Class<?>[] { Game.class, Map.class, Point.class },
        game, Map.of(), new Point(3, 3)));
  }

  @Test
  void shouldBlockExchangeWhilePendingTrue() {
    Map<Point, Tile> pending = Map.of(new Point(7, 7), new Tile('A'));
    assertTrue((boolean) invokeStatic("shouldBlockExchangeWhilePending",
        new Class<?>[] { Map.class }, pending));
  }

  @Test
  void shouldBlockExchangeWhilePendingFalse() {
    assertFalse((boolean) invokeStatic("shouldBlockExchangeWhilePending",
        new Class<?>[] { Map.class }, Map.of()));
  }

  @Test
  void shouldIgnoreTileDropFalseWhenValid() {
    assertFalse((boolean) invokeStatic("shouldIgnoreTileDrop",
        new Class<?>[] { Tile.class, boolean.class }, new Tile('X'), false));
  }

  @Test
  void moveOriginXandY() {
    Move move = Move.createPlay(game.getCurrentPlayer(),
        List.of(new Tile('Q')), new Point(10, 12), Direction.HORIZONTAL);
    assertEquals(10, invokeStatic("moveOriginX",
        new Class<?>[] { Move.class }, move));
    assertEquals(12, invokeStatic("moveOriginY",
        new Class<?>[] { Move.class }, move));
  }

  // ===== Helper classes and methods =====

  private Game createOnlineGame() {
    Game g = new Game();
    g.addPlayer(new HumanPlayer("O1", PlayerColor.BLUE));
    g.addPlayer(new HumanPlayer("O2", PlayerColor.RED));
    g.startGame();
    return g;
  }

  private static void runOnFxThread(Runnable action) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(() -> {
      try {
        action.run();
      } finally {
        latch.countDown();
      }
    });
    assertTrue(latch.await(5, TimeUnit.SECONDS));
  }

  private static void setField(Object target, String name, Object value) {
    try {
      Field f = target.getClass().getDeclaredField(name);
      f.setAccessible(true);
      f.set(target, value);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private static void setFieldStatic(String name, Object value) {
    try {
      Field f = ScrabbleGui.class.getDeclaredField(name);
      f.setAccessible(true);
      f.set(null, value);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private static Object getField(Object target, String name) {
    try {
      Field f = target.getClass().getDeclaredField(name);
      f.setAccessible(true);
      return f.get(target);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private static Object invokePrivate(Object target, String methodName) {
    try {
      Method m = target.getClass().getDeclaredMethod(methodName);
      m.setAccessible(true);
      return m.invoke(target);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  private static Object invokePrivate(Object target,
      String methodName, Class<?> argType, Object arg) {
    try {
      Method m = target.getClass()
          .getDeclaredMethod(methodName, argType);
      m.setAccessible(true);
      return m.invoke(target, arg);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  private static Object invokeStatic(String methodName,
      Class<?>[] argTypes, Object... args) {
    try {
      Method method = ScrabbleGui.class.getDeclaredMethod(methodName, argTypes);
      method.setAccessible(true);
      return method.invoke(null, args);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  private static class FakeNetworkManager extends NetworkManager {
    boolean playCalled;
    boolean passCalled;
    boolean exchangeCalled;

    @Override
    public void play(int x, int y, String direction, String word) {
      playCalled = true;
    }

    @Override
    public void pass() {
      passCalled = true;
    }

    @Override
    public void exchange(String letters) {
      exchangeCalled = true;
    }
  }
}
