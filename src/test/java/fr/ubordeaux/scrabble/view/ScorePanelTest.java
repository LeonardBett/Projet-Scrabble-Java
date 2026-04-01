package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.i18n.I18n;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.view.gui.panel.ScorePanel;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.scene.control.Label;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour ScorePanel.
 */
class ScorePanelTest {

  private ScorePanel scorePanel;

  @BeforeAll
  static void initToolkit() {
    try {
      com.sun.javafx.application.PlatformImpl.startup(() -> {
      });
    } catch (Exception e) {
      // Toolkit already initialized or not available in this environment
    }
  }

  @BeforeEach
  void setUp() {
    I18n.setLanguage("fr");
    scorePanel = new ScorePanel();
  }

  @AfterEach
  void tearDown() {
    I18n.setLanguage("en");
  }

  @Test
  void scorePanelShouldBeInstantiable() {
    assertNotNull(scorePanel);
  }

  @Test
  void updateScoresShouldHandleTwoPlayers() {
    scorePanel.updateScores(new String[] { "Alice", "Bob" }, new int[] { 100, 200 });
  }

  @Test
  void updateScoresShouldHandleFourPlayers() {
    scorePanel.updateScores(
        new String[] { "P1", "P2", "P3", "P4" }, new int[] { 10, 20, 30, 40 });
  }

  @Test
  void updateScoresShouldHandleZeroScores() {
    scorePanel.updateScores(new String[] { "Alice", "Bob" }, new int[] { 0, 0 });
  }

  @Test
  void updateBagInfoShouldHandleValues() {
    scorePanel.updateBagInfo(102);
    scorePanel.updateBagInfo(0);
    scorePanel.updateBagInfo(50);
  }

  @Test
  void highlightCurrentPlayerShouldNotThrow() {
    scorePanel.updateScores(new String[] { "Alice", "Bob" }, new int[] { 0, 0 });
    scorePanel.highlightCurrentPlayer(0, "Alice");
    scorePanel.highlightCurrentPlayer(1, "Bob");
  }

  @Test
  void highlightCurrentPlayerShouldHandleNegativeIndex() {
    scorePanel.updateScores(new String[] { "Alice" }, new int[] { 0 });
    scorePanel.highlightCurrentPlayer(-1, "Alice");
  }

  @Test
  void highlightCurrentPlayerShouldHandleOutOfRangeIndex() {
    scorePanel.updateScores(new String[] { "Alice" }, new int[] { 0 });
    scorePanel.highlightCurrentPlayer(10, "Alice");
  }

  @Test
  void updateScoresShouldHandleMismatchedArrayLengths() {
    scorePanel.updateScores(
        new String[] { "Alice", "Bob", "Charlie" }, new int[] { 10, 20 });
  }

  @Test
  void scorePanelShouldHavePositivePreferredWidth() {
    assertTrue(scorePanel.getPrefWidth() > 0);
  }

  @Test
  void scorePanelShouldExposeExpectedDefaultTexts() {
    Label title = (Label) scorePanel.getChildren().getFirst();
    assertEquals("SCORES", title.getText());
    Label bagInfo = (Label) getPrivateField(scorePanel, "bagInfoLabel");
    Label currentPlayer = (Label) getPrivateField(scorePanel, "currentPlayerLabel");
    assertEquals("Lettres restantes : 102", bagInfo.getText());
    // assertEquals("🎯 Tour de : —", currentPlayer.getText());
  }

  @Test
  void blitzLabelShouldBeHiddenByDefault() {
    Label blitzLabel = (Label) getPrivateField(scorePanel, "blitzLabel");
    assertFalse(blitzLabel.isVisible());
  }

  @Test
  void stopBlitzTimersWhenNoTimelineRunning() {
    scorePanel.stopBlitzTimers();
    Label blitzLabel = (Label) getPrivateField(scorePanel, "blitzLabel");
    assertFalse(blitzLabel.isVisible());
  }

  @Test
  void startBlitzTimersShouldMakeBlitzLabelVisible() {
    HumanPlayer p1 = new HumanPlayer("Alice", PlayerColor.BLUE);
    p1.enableBlitzClock(Duration.ofMinutes(5));
    HumanPlayer p2 = new HumanPlayer("Bob", PlayerColor.RED);
    p2.enableBlitzClock(Duration.ofMinutes(5));

    scorePanel.startBlitzTimers(List.of(p1, p2), () -> {
    });

    Label blitzLabel = (Label) getPrivateField(scorePanel, "blitzLabel");
    assertTrue(blitzLabel.isVisible());
    assertFalse(blitzLabel.getText().isEmpty());
    assertTrue(blitzLabel.getText().contains("Alice"));
    assertTrue(blitzLabel.getText().contains("Bob"));

    scorePanel.stopBlitzTimers();
    assertFalse(blitzLabel.isVisible());
  }

  @Test
  void startBlitzTimersShouldCallCallbackWhenExpired() {
    HumanPlayer p = new HumanPlayer("Timeout", PlayerColor.BLUE);
    p.enableBlitzClock(Duration.ofNanos(1));
    p.startTurnTimer();
    p.pauseTurnTimer();

    AtomicBoolean callbackCalled = new AtomicBoolean(false);
    scorePanel.startBlitzTimers(List.of(p), () -> callbackCalled.set(true));

    assertTrue(callbackCalled.get());
  }

  @Test
  void startBlitzTimersWithNonBlitzPlayersStillWorks() {
    HumanPlayer p = new HumanPlayer("NoBlitz", PlayerColor.BLUE);

    scorePanel.startBlitzTimers(List.of(p), () -> {
    });

    Label blitzLabel = (Label) getPrivateField(scorePanel, "blitzLabel");
    assertTrue(blitzLabel.isVisible());

    scorePanel.stopBlitzTimers();
  }

  @Test
  void startBlitzTimersCalledTwiceStopsPrevious() {
    HumanPlayer p1 = new HumanPlayer("A", PlayerColor.BLUE);
    p1.enableBlitzClock(Duration.ofMinutes(5));

    scorePanel.startBlitzTimers(List.of(p1), () -> {
    });
    scorePanel.startBlitzTimers(List.of(p1), () -> {
    });

    Label blitzLabel = (Label) getPrivateField(scorePanel, "blitzLabel");
    assertTrue(blitzLabel.isVisible());
    scorePanel.stopBlitzTimers();
  }

  @Test
  void startBlitzTimersWithEmptyListShouldNotCrash() {
    scorePanel.startBlitzTimers(List.of(), () -> {
    });
    Label blitzLabel = (Label) getPrivateField(scorePanel, "blitzLabel");
    assertTrue(blitzLabel.isVisible());
    scorePanel.stopBlitzTimers();
  }

  @Test
  void updateBagInfoChangesLabelText() {
    Label bagInfo = (Label) getPrivateField(scorePanel, "bagInfoLabel");
    scorePanel.updateBagInfo(42);
    assertEquals("Lettres restantes : 42", bagInfo.getText());
  }

  @Test
  void highlightCurrentPlayerUpdatesLabel() {
    scorePanel.updateScores(new String[] { "Alice", "Bob" }, new int[] { 10, 20 });
    scorePanel.highlightCurrentPlayer(0, "Alice");
    Label currentPlayer = (Label) getPrivateField(scorePanel, "currentPlayerLabel");
    assertTrue(currentPlayer.getText().contains("Alice"));
  }

  @Test
  void updateScoresClearsOldEntries() {
    scorePanel.updateScores(new String[] { "A", "B" }, new int[] { 1, 2 });
    scorePanel.updateScores(new String[] { "X" }, new int[] { 99 });
    // Should have just 1 entry now (not 3)
    javafx.scene.control.ListView<?> playerList =
        (javafx.scene.control.ListView<?>) getPrivateField(
            scorePanel, "playerList");
    assertEquals(1, playerList.getItems().size());
  }

  @Test
  void blitzTimerWhenPlayerHasLessThanSixtySeconds() {
    HumanPlayer p = new HumanPlayer("Urgent", PlayerColor.BLUE);
    p.enableBlitzClock(Duration.ofSeconds(30));

    scorePanel.startBlitzTimers(List.of(p), () -> {
    });

    Label blitzLabel = (Label) getPrivateField(scorePanel, "blitzLabel");
    assertTrue(blitzLabel.isVisible());
    assertTrue(blitzLabel.getText().contains("Urgent"));

    scorePanel.stopBlitzTimers();
  }

  private static Object getPrivateField(Object target, String fieldName) {
    try {
      Field field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      return field.get(target);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
