package fr.ubordeaux.scrabble.view.gui.main;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.model.utils.Point;
import fr.ubordeaux.scrabble.view.gui.ScrabbleGui;
import fr.ubordeaux.scrabble.view.gui.panel.BoardPanel;
import fr.ubordeaux.scrabble.view.gui.panel.RackPanel;
import fr.ubordeaux.scrabble.view.gui.panel.ScorePanel;
import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ScrabbleGuiRefreshTest {

  @BeforeAll
  static void initToolkit() {
    try {
      com.sun.javafx.application.PlatformImpl.startup(() -> {
      });
    } catch (Exception e) {
      // Toolkit already initialized.
    }
  }

  @Test
  void refreshBoardShouldReflectPlacedTile() throws Exception {
    Game game = createStartedGame();
    game.getBoard().getSquare(new Point(0, 0)).setTile(new Tile('a'));
    ScrabbleGui gui = buildGui(game);
    ScrabbleGuiRefresh refresh = new ScrabbleGuiRefresh();

    runOnFxThread(() -> refresh.refreshBoard(gui));

    Label[][] cells = (Label[][]) getField(gui.getBoardPanel(), "cellLabels");
    assertEquals("A", cells[0][0].getText());
  }

  @Test
  void refreshRackShouldUseEmptyRackWhenNoCurrentPlayer() throws Exception {
    Game game = new Game();
    ScrabbleGui gui = buildGui(game);
    ScrabbleGuiRefresh refresh = new ScrabbleGuiRefresh();

    runOnFxThread(() -> refresh.refreshRack(gui));

    assertTrue(gui.getRackPanel().getRack().getTiles().isEmpty());
  }

  @Test
  void refreshScoresShouldKeepListEmptyWhenNoPlayers() throws Exception {
    Game game = new Game();
    ScrabbleGui gui = buildGui(game);
    ScrabbleGuiRefresh refresh = new ScrabbleGuiRefresh();

    runOnFxThread(() -> refresh.refreshScores(gui));

    @SuppressWarnings("unchecked")
    ListView<String> playerList = (ListView<String>) getField(gui.getScorePanel(), "playerList");
    assertTrue(playerList.getItems().isEmpty());
  }

  @Test
  void refreshAllShouldRunWhenBoardDisabled() throws Exception {
    Game game = createStartedGame();
    ScrabbleGui gui = buildGui(game);
    gui.getBoardPanel().setDisable(true);
    ScrabbleGuiRefresh refresh = new ScrabbleGuiRefresh();

    runOnFxThread(() -> assertDoesNotThrow(() -> refresh.refreshAll(gui)));

    @SuppressWarnings("unchecked")
    ListView<String> playerList = (ListView<String>) getField(gui.getScorePanel(), "playerList");
    assertEquals(2, playerList.getItems().size());
  }

  private static Game createStartedGame() {
    Game game = new Game();
    game.addPlayer(new HumanPlayer("P1", PlayerColor.BLUE));
    game.addPlayer(new HumanPlayer("P2", PlayerColor.RED));
    game.startGame();
    return game;
  }

  private static ScrabbleGui buildGui(Game game) throws Exception {
    ScrabbleGui.setGame(game);
    ScrabbleGui gui = new ScrabbleGui();
    BoardPanel boardPanel = new BoardPanel(game.getBoard());
    RackPanel rackPanel = game.getCurrentPlayer() == null
        ? new RackPanel()
        : new RackPanel(game.getCurrentPlayer().getRack());
    ScorePanel scorePanel = new ScorePanel();
    setField(gui, "boardPanel", boardPanel);
    setField(gui, "rackPanel", rackPanel);
    setField(gui, "scorePanel", scorePanel);
    return gui;
  }

  private static void runOnFxThread(ThrowingRunnable runnable) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Throwable[] error = new Throwable[1];
    Platform.runLater(() -> {
      try {
        runnable.run();
      } catch (Throwable t) {
        error[0] = t;
      } finally {
        latch.countDown();
      }
    });
    boolean completed = latch.await(3, TimeUnit.SECONDS);
    assertTrue(completed, "JavaFX action timed out");
    if (error[0] != null) {
      if (error[0] instanceof Exception e) {
        throw e;
      }
      throw new RuntimeException(error[0]);
    }
  }

  private static void setField(Object target, String name, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(name);
    field.setAccessible(true);
    field.set(target, value);
  }

  private static Object getField(Object target, String name) throws Exception {
    Field field = target.getClass().getDeclaredField(name);
    field.setAccessible(true);
    return field.get(target);
  }

  @FunctionalInterface
  private interface ThrowingRunnable {
    void run() throws Exception;
  }
}
