package fr.ubordeaux.scrabble.view.gui.main;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.controller.GameController;
import fr.ubordeaux.scrabble.controller.config.ControllerConfigSnapshot;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.model.network.NetworkManager;
import fr.ubordeaux.scrabble.model.utils.Point;
import fr.ubordeaux.scrabble.view.gui.JavaFxView;
import fr.ubordeaux.scrabble.view.gui.ScrabbleGui;
import fr.ubordeaux.scrabble.view.gui.network.NetworkGameBridge;
import fr.ubordeaux.scrabble.view.gui.panel.BoardPanel;
import fr.ubordeaux.scrabble.view.gui.panel.ControlPanel;
import fr.ubordeaux.scrabble.view.gui.panel.MessagePanel;
import fr.ubordeaux.scrabble.view.gui.panel.RackPanel;
import fr.ubordeaux.scrabble.view.gui.panel.ScorePanel;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests that exercise ScrabbleGui instance methods by injecting
 * panels via reflection.
 */
class ScrabbleGuiInstanceTest {

  private Game game;
  private ScrabbleGui gui;
  private BoardPanel boardPanel;
  private RackPanel rackPanel;
  private ScorePanel scorePanel;
  private ControlPanel controlPanel;
  private MessagePanel messagePanel;
  private GameController controller;
  private JavaFxView view;

  @BeforeAll
  static void initToolkit() {
    try {
      com.sun.javafx.application.PlatformImpl.startup(() -> {
      });
    } catch (Exception e) {
      // Already initialized
    }
    Platform.setImplicitExit(false);
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
    rackPanel = new RackPanel(
        game.getCurrentPlayer().getRack());
    scorePanel = new ScorePanel();
    controlPanel = new ControlPanel();
    messagePanel = new MessagePanel();
    controller = new GameController(game, view);

    setField(gui, "boardPanel", boardPanel);
    setField(gui, "rackPanel", rackPanel);
    setField(gui, "scorePanel", scorePanel);
    setField(gui, "controlPanel", controlPanel);
    setField(gui, "messagePanel", messagePanel);
    setField(gui, "controller", controller);
    setField(gui, "networkManager", new NetworkManager());
    setField(gui, "networkBridge",
        new NetworkGameBridge(new NetworkManager()));
    setField(gui, "saveMenuItem", new javafx.scene.control.MenuItem());
    setField(gui, "loadMenuItem", new javafx.scene.control.MenuItem());
    setField(gui, "configurationMenuItem", new javafx.scene.control.MenuItem());
    view.setGui(gui);
    setFieldStatic("viewInstance", view);
  }

  @AfterEach
  void tearDown() {
    Game resetGame = new Game();
    resetGame.addPlayer(
        new HumanPlayer("R1", PlayerColor.BLUE));
    resetGame.addPlayer(
        new HumanPlayer("R2", PlayerColor.RED));
    resetGame.startGame();
    ScrabbleGui.setGame(resetGame);
    ScrabbleGui.setView(new JavaFxView(resetGame));
  }

  @Test
  void refreshAllShouldNotThrow() {
    assertDoesNotThrow(() -> gui.refreshAll());
  }

  @Test
  void refreshBoardShouldNotThrow() {
    assertDoesNotThrow(() -> gui.refreshBoard());
  }

  @Test
  void refreshRackShouldNotThrow() {
    assertDoesNotThrow(() -> gui.refreshRack());
  }

  @Test
  void refreshScoresShouldNotThrow() {
    assertDoesNotThrow(() -> gui.refreshScores());
  }

  @Test
  void showInfoShouldNotThrow() throws Exception {
    runOnFxThread(() -> gui.showInfo("Title", "Message"));
  }

  @Test
  void showErrorShouldNotThrow() throws Exception {
    runOnFxThread(() -> gui.showError("Error message"));
  }

  @Test
  void isOnlineModeDefaultsFalse() {
    assertFalse(gui.isOnlineMode());
  }

  @Test
  void switchToOnlineGameSetsOnlineMode() throws Exception {
    Game onlineGame = createOnlineGame();
    runOnFxThread(() -> gui.switchToOnlineGame(onlineGame));
    assertTrue(gui.isOnlineMode());
  }

  @Test
  void exitOnlineModeClearsOnlineMode() throws Exception {
    Game onlineGame = createOnlineGame();
    runOnFxThread(() -> gui.switchToOnlineGame(onlineGame));
    assertTrue(gui.isOnlineMode());
    runOnFxThread(() -> gui.exitOnlineMode());
    assertFalse(gui.isOnlineMode());
  }

  @Test
  void onTileDraggedSetsCurrentTile() {
    Tile tile = new Tile('A');
    gui.onTileDragged(tile);
    assertEquals(tile,
        getField(gui, "currentlyDraggedTile"));
  }

  @Test
  void onTileDroppedWithNullDraggedTileIgnores() {
    gui.onTileDragged(null);
    gui.onTileDropped(7, 7);
    @SuppressWarnings("unchecked")
    Map<Point, Tile> pending = (Map<Point, Tile>) getField(gui, "pendingTiles");
    assertTrue(pending.isEmpty());
  }

  @Test
  void onTileDroppedWithValidTilePlacesPending() {
    Tile tile = game.getCurrentPlayer().getRack().getTiles().stream()
          .filter(t -> !t.isJoker() && t.getCharacter() != ' ')
          .findFirst()
          .orElseThrow();
    gui.onTileDragged(tile);
    gui.onTileDropped(7, 7);
    @SuppressWarnings("unchecked")
    Map<Point, Tile> pending = (Map<Point, Tile>) getField(gui, "pendingTiles");
    assertTrue(pending.containsKey(new Point(7, 7)));
  }

  @Test
  void onTileDroppedOnOccupiedPendingRejects()
      throws Exception {
    java.util.List<Tile> playableTiles = game.getCurrentPlayer().getRack().getTiles().stream()
        .filter(t -> !t.isJoker() && t.getCharacter() != ' ')
        .limit(2)
        .toList();
    assertEquals(2, playableTiles.size());
    Tile t1 = playableTiles.get(0);
    Tile t2 = playableTiles.get(1);
    gui.onTileDragged(t1);
    gui.onTileDropped(5, 5);
    gui.onTileDragged(t2);
    runOnFxThread(() -> gui.onTileDropped(5, 5));
    @SuppressWarnings("unchecked")
    Map<Point, Tile> pending = (Map<Point, Tile>) getField(gui, "pendingTiles");
    assertEquals(t1, pending.get(new Point(5, 5)));
  }

  @Test
  void cancelPendingTilesWhenEmptyNotThrow() {
    invokePrivate(gui, "cancelPendingTiles");
  }

  @Test
  void cancelPendingTilesClearsPending() {
    Tile tile = game.getCurrentPlayer().getRack()
        .getTiles().getFirst();
    gui.onTileDragged(tile);
    gui.onTileDropped(3, 3);
    @SuppressWarnings("unchecked")
    Map<Point, Tile> pending = (Map<Point, Tile>) getField(gui, "pendingTiles");
    assertFalse(pending.isEmpty());
    invokePrivate(gui, "cancelPendingTiles");
    assertTrue(pending.isEmpty());
  }

  @Test
  void submitPendingTilesWhenEmpty() throws Exception {
    runOnFxThread(
        () -> invokePrivate(gui, "submitPendingTiles"));
  }

  @Test
  void submitPendingTilesWithTilesLocal() throws Exception {
    Tile tile = game.getCurrentPlayer().getRack()
        .getTiles().getFirst();
    gui.onTileDragged(tile);
    gui.onTileDropped(7, 7);
    runOnFxThread(
        () -> invokePrivate(gui, "submitPendingTiles"));
  }

  @Test
  void setGameplayControlsDisabledToggles() {
    invokePrivate(gui, "setGameplayControlsDisabled",
        boolean.class, true);
    assertTrue(controlPanel.getPlayButton().isDisable());
    invokePrivate(gui, "setGameplayControlsDisabled",
        boolean.class, false);
    assertFalse(controlPanel.getPlayButton().isDisable());
  }

  @Test
  void refreshScoresUpdatesPanel() {
    gui.refreshScores();
    assertNotNull(scorePanel);
  }

  @Test
  void switchToOnlineThenRefreshAll() throws Exception {
    Game onlineGame = createOnlineGame();
    runOnFxThread(() -> {
      gui.switchToOnlineGame(onlineGame);
      gui.refreshAll();
    });
  }

  @Test
  void getCurrentRackReturnsRack() {
    Object rack = invokePrivate(gui, "getCurrentRack");
    assertNotNull(rack);
  }

  // loadDictionary removed: dictionary loading now handled by GameController.getOrLoadGaddag()

  @Test
  void buildLeftMenuReturnsVbox() {
    setUpMenuItemFields();
    Object menu = invokePrivate(gui, "buildLeftMenu");
    assertNotNull(menu);
    assertTrue(menu instanceof VBox);
  }

  @Test
  void checkAiTurnWithHumanDoesNothing() {
    invokePrivate(gui, "checkAiTurn");
  }

  @Test
  void checkAiTurnWhenBoardDisabledReturns() {
    boardPanel.setDisable(true);
    invokePrivate(gui, "checkAiTurn");
  }

  @Test
  void onBlitzTimeExpiredSetsGameOver() throws Exception {
    HumanPlayer p1 = (HumanPlayer) game.getPlayers().get(0);
    p1.enableBlitzClock(Duration.ofNanos(1));
    p1.startTurnTimer();
    p1.pauseTurnTimer();
    runOnFxThread(
        () -> invokePrivate(gui, "onBlitzTimeExpired"));
    assertTrue(game.isGameOver());
  }

  @Test
  void onTileDroppedOnBoardOccupiedSquare()
      throws Exception {
    game.getBoard().getSquare(new Point(0, 0))
        .setTile(new Tile('Z'));
    Tile tile = game.getCurrentPlayer().getRack()
        .getTiles().getFirst();
    gui.onTileDragged(tile);
    runOnFxThread(() -> gui.onTileDropped(0, 0));
    @SuppressWarnings("unchecked")
    Map<Point, Tile> pending = (Map<Point, Tile>) getField(gui, "pendingTiles");
    assertFalse(pending.containsKey(new Point(0, 0)));
  }

  @Test
  void onTileDroppedWhenGameOverIgnores() {
    game.setGameOver(true);
    Tile tile = game.getCurrentPlayer().getRack()
        .getTiles().getFirst();
    gui.onTileDragged(tile);
    gui.onTileDropped(7, 7);
    @SuppressWarnings("unchecked")
    Map<Point, Tile> pending = (Map<Point, Tile>) getField(gui, "pendingTiles");
    assertTrue(pending.isEmpty());
  }

  @Test
  void multipleTilesDroppedInSequence() {
    Player currentPlayer = game.getCurrentPlayer();
    game.forceTilesToPlayer(currentPlayer.getName(),
        java.util.List.of(new Tile('A'), new Tile('B')));

    Tile t1 = currentPlayer.getRack().getTiles().get(0);
    Tile t2 = currentPlayer.getRack().getTiles().get(1);

    gui.onTileDragged(t1);
    gui.onTileDropped(7, 7);

    gui.onTileDragged(t2);
    gui.onTileDropped(7, 8);

    @SuppressWarnings("unchecked")
    Map<Point, Tile> pending = (Map<Point, Tile>) getField(gui, "pendingTiles");
    assertEquals(2, pending.size(), "There should be exactly 2 pending tiles on the board.");
  }

  @Test
  void submitPendingTilesWithMultipleTiles()
      throws Exception {
    Tile t1 = game.getCurrentPlayer().getRack()
        .getTiles().get(0);
    Tile t2 = game.getCurrentPlayer().getRack()
        .getTiles().get(1);
    gui.onTileDragged(t1);
    gui.onTileDropped(7, 7);
    gui.onTileDragged(t2);
    gui.onTileDropped(7, 8);
    runOnFxThread(
        () -> invokePrivate(gui, "submitPendingTiles"));
  }

  @Test
  void exitOnlineModeStopsBlitzTimers() throws Exception {
    Game onlineGame = createOnlineGame();
    runOnFxThread(() -> gui.switchToOnlineGame(onlineGame));
    runOnFxThread(() -> gui.exitOnlineMode());
    assertFalse(gui.isOnlineMode());
  }

  @Test
  void refreshAllAfterGameOver() {
    game.setGameOver(true);
    assertDoesNotThrow(() -> gui.refreshAll());
  }

  @Test
  void refreshRackAfterPlayerChanges() {
    assertDoesNotThrow(() -> gui.refreshRack());
  }

  @Test
  void applyConfigurationAssignmentsHandlesInvalidAndValidData() throws Exception {
    runOnFxThread(() -> invokePrivate(gui, "applyConfigurationAssignments",
        String.class, "invalid-entry"));

    runOnFxThread(() -> invokePrivate(gui, "applyConfigurationAssignments",
        String.class,
        "language=fr; players=2; blitz=true; timeout=1; ai-time=2; "
            + "ai-exptiminimax=false; ai-ml=false; debug=false; verbose=false"));
  }

  @Test
  void refreshLocalizedTextsWithBoundControls() {
    setField(gui, "newGameMenuItem", new javafx.scene.control.MenuItem());
    setField(gui, "onlineMenuItem", new javafx.scene.control.MenuItem());
    setField(gui, "saveMenuItem", new javafx.scene.control.MenuItem());
    setField(gui, "loadMenuItem", new javafx.scene.control.MenuItem());
    setField(gui, "configurationMenuItem", new javafx.scene.control.MenuItem());
    setField(gui, "infoMenuItem", new javafx.scene.control.MenuItem());
    setField(gui, "quitMenuItem", new javafx.scene.control.MenuItem());
    setField(gui, "newGameButton", new javafx.scene.control.Button());
    setField(gui, "onlineButton", new javafx.scene.control.Button());
    setField(gui, "saveButton", new javafx.scene.control.Button());
    setField(gui, "loadButton", new javafx.scene.control.Button());
    setField(gui, "configurationButton", new javafx.scene.control.Button());
    setField(gui, "infoButton", new javafx.scene.control.Button());
    setField(gui, "quitButton", new javafx.scene.control.Button());
    setField(gui, "appMenuButton", new javafx.scene.control.MenuButton());

    assertDoesNotThrow(() -> invokePrivate(gui, "refreshLocalizedTexts"));
  }

  @Test
  void controllerConfigSnapshotCaptureAndApplyToController() {
    assertDoesNotThrow(() -> {
      final ControllerConfigSnapshot snapshot = ControllerConfigSnapshot.capture(controller);
      assertNotNull(snapshot);
      snapshot.applyTo(controller);
    });
  }

  @Test
  void startUiShouldInitializeCoreComponents() throws Exception {
    Game localGame = new Game();
    localGame.addPlayer(new HumanPlayer("S1", PlayerColor.BLUE));
    localGame.addPlayer(new HumanPlayer("S2", PlayerColor.RED));
    localGame.startGame();

    ScrabbleGui.setGame(localGame);
    ScrabbleGui.setView(new JavaFxView(localGame));

    ScrabbleGui realGui = new ScrabbleGui();

    runOnFxThread(() -> {
      Platform.setImplicitExit(false);
      Stage stage = new Stage();
      realGui.startUi(stage);
      stage.hide();
    });

    assertNotNull(getField(realGui, "controller"));
    assertNotNull(getField(realGui, "boardPanel"));
    assertNotNull(getField(realGui, "controlPanel"));
    assertNotNull(getField(realGui, "networkBridge"));

    Object bridge = getField(realGui, "networkBridge");
    if (bridge instanceof NetworkGameBridge networkGameBridge) {
      networkGameBridge.dispose();
    }
  }

  @Test
  void startUiShouldInitializeWhenViewIsNullAndBlitzEnabled() throws Exception {
    Game blitzGame = new Game();
    blitzGame.addPlayer(new HumanPlayer("B1", PlayerColor.BLUE));
    blitzGame.addPlayer(new HumanPlayer("B2", PlayerColor.RED));
    blitzGame.startGame();
    blitzGame.enableBlitzMode(Duration.ofMinutes(1));

    ScrabbleGui.setGame(blitzGame);
    ScrabbleGui.setView(null);

    ScrabbleGui realGui = new ScrabbleGui();

    runOnFxThread(() -> {
      Platform.setImplicitExit(false);
      Stage stage = new Stage();
      realGui.startUi(stage);
      stage.hide();
    });

    assertNotNull(getField(realGui, "scorePanel"));
    assertNotNull(getField(realGui, "networkBridge"));

    Object bridge = getField(realGui, "networkBridge");
    if (bridge instanceof NetworkGameBridge networkGameBridge) {
      networkGameBridge.dispose();
    }
  }

  @Test
  void openExchangeDialogShouldReturnEarlyWhenPendingTilesExist() throws Exception {
    Tile tile = game.getCurrentPlayer().getRack().getTiles().getFirst();
    gui.onTileDragged(tile);
    gui.onTileDropped(7, 7);

    runOnFxThread(() -> invokePrivate(gui, "openExchangeDialog"));
  }

  @Test
  void addMenuShortcutShouldIgnoreInvalidShortcut() throws Exception {
    runOnFxThread(() -> {
      Scene scene = new Scene(new VBox());
      MenuItem item = new MenuItem("X");
      invokePrivate(gui, "addMenuShortcut",
          new Class<?>[] { Scene.class, String.class, MenuItem.class },
          scene, "bad++shortcut", item);
    });
  }

  @Test
  void addActionShortcutShouldIgnoreInvalidShortcut() throws Exception {
    runOnFxThread(() -> {
      Scene scene = new Scene(new VBox());
      invokePrivate(gui, "addActionShortcut",
          new Class<?>[] { Scene.class, String.class, Runnable.class },
          scene, "bad++shortcut", (Runnable) () -> {
          });
    });
  }

  @Test
  void startShouldDelegateToStartUi() {
    final boolean[] called = { false };
    ScrabbleGui delegatingGui = new ScrabbleGui() {
      @Override
      public void startUi(Stage stage) {
        called[0] = true;
      }
    };

    assertDoesNotThrow(() -> delegatingGui.start(null));
    assertTrue(called[0]);
  }

  @Test
  void connectButtonsShouldInstallHandlersAndSupportSafeFiresWhenGameOver() throws Exception {
    game.setGameOver(true);
    runOnFxThread(() -> {
      invokePrivate(gui, "buildLeftMenu");
      invokePrivate(gui, "connectButtons");
      controlPanel.getPlayButton().fire();
      controlPanel.getPassButton().fire();
      controlPanel.getExchangeButton().fire();
      controlPanel.getCancelPlacementButton().fire();
      controlPanel.getUndoButton().fire();
      controlPanel.getRedoButton().fire();
      controlPanel.getHintButton().fire();
      controlPanel.getPauseButton().fire();
    });
  }

  @Test
  void setupShortcutsShouldPopulateSceneAccelerators() {
    Object menu = invokePrivate(gui, "buildLeftMenu");
    assertTrue(menu instanceof VBox);
    Scene scene = new Scene(new VBox());

    invokePrivate(gui, "setupShortcuts", Scene.class, scene);

    assertFalse(scene.getAccelerators().isEmpty());
  }

  @Test
  void openNetworkLobbyShouldCreateLobbyView() throws Exception {
    runOnFxThread(() -> invokePrivate(gui, "openNetworkLobby"));

    Object lobbyView = getField(gui, "lobbyView");
    assertNotNull(lobbyView);
  }

  @Test
  void handleMoveRefusedShouldClearPendingTiles() {
    Tile tile = game.getCurrentPlayer().getRack().getTiles().getFirst();
    gui.onTileDragged(tile);
    gui.onTileDropped(7, 7);

    @SuppressWarnings("unchecked")
    Map<Point, Tile> pendingBefore = (Map<Point, Tile>) getField(gui, "pendingTiles");
    assertFalse(pendingBefore.isEmpty());

    assertDoesNotThrow(() -> runOnFxThread(() -> gui.handleMoveRefused("scrabble.invalidMove")));

    @SuppressWarnings("unchecked")
    Map<Point, Tile> pendingAfter = (Map<Point, Tile>) getField(gui, "pendingTiles");
    assertTrue(pendingAfter.isEmpty());
  }

  private Game createOnlineGame() {
    Game g = new Game();
    g.addPlayer(new HumanPlayer("O1", PlayerColor.BLUE));
    g.addPlayer(new HumanPlayer("O2", PlayerColor.RED));
    g.startGame();
    return g;
  }

  private void setUpMenuItemFields() {
    setField(gui, "newGameMenuItem",
        new javafx.scene.control.MenuItem("New"));
    setField(gui, "onlineMenuItem",
        new javafx.scene.control.MenuItem("Online"));
    setField(gui, "saveMenuItem",
        new javafx.scene.control.MenuItem("Save"));
    setField(gui, "loadMenuItem",
        new javafx.scene.control.MenuItem("Load"));
    setField(gui, "quitMenuItem",
        new javafx.scene.control.MenuItem("Quit"));
  }

  private static void runOnFxThread(Runnable action)
      throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<Throwable> thrown = new AtomicReference<>();
    Platform.runLater(() -> {
      try {
        action.run();
      } catch (Throwable t) {
        thrown.set(t);
      } finally {
        latch.countDown();
      }
    });
    assertTrue(latch.await(15, TimeUnit.SECONDS));
    if (thrown.get() != null) {
      throw new RuntimeException(thrown.get());
    }
  }

  private static void setField(
      Object target, String name, Object value) {
    try {
      Field f = target.getClass().getDeclaredField(name);
      f.setAccessible(true);
      f.set(target, value);
    } catch (NoSuchFieldException
        | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private static void setFieldStatic(
      String name, Object value) {
    try {
      Field f = ScrabbleGui.class.getDeclaredField(name);
      f.setAccessible(true);
      f.set(null, value);
    } catch (NoSuchFieldException
        | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private static Object getField(
      Object target, String name) {
    try {
      Field f = target.getClass().getDeclaredField(name);
      f.setAccessible(true);
      return f.get(target);
    } catch (NoSuchFieldException
        | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private static Object invokePrivate(
      Object target, String methodName) {
    try {
      Method m = target.getClass()
          .getDeclaredMethod(methodName);
      m.setAccessible(true);
      return m.invoke(target);
    } catch (NoSuchMethodException
        | IllegalAccessException
        | InvocationTargetException e) {
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
    } catch (NoSuchMethodException
        | IllegalAccessException
        | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  private static Object invokePrivate(Object target,
      String methodName, Class<?>[] argTypes, Object... args) {
    try {
      Method m = target.getClass().getDeclaredMethod(methodName, argTypes);
      m.setAccessible(true);
      return m.invoke(target, args);
    } catch (NoSuchMethodException
        | IllegalAccessException
        | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }
}
