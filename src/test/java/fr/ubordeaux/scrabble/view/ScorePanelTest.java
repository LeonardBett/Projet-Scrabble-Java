package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.view.gui.panel.ScorePanel;
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
      com.sun.javafx.application.PlatformImpl.startup(() -> { });
    } catch (Exception e) {
      // Toolkit already initialized or not available in this environment
    }
  }

  @BeforeEach
  void setUp() {
    scorePanel = new ScorePanel();
  }

  @Test
  void scorePanelShouldBeInstantiable() {
    assertNotNull(scorePanel);
  }

  @Test
  void updateScoresShouldHandleTwoPlayers() {
    String[] names = {"Alice", "Bob"};
    int[] scores = {100, 200};
    // Pas d'exception levée = succès
    scorePanel.updateScores(names, scores);
  }

  @Test
  void updateScoresShouldHandleFourPlayers() {
    String[] names = {"P1", "P2", "P3", "P4"};
    int[] scores = {10, 20, 30, 40};
    scorePanel.updateScores(names, scores);
  }

  @Test
  void updateScoresShouldHandleZeroScores() {
    String[] names = {"Alice", "Bob"};
    int[] scores = {0, 0};
    scorePanel.updateScores(names, scores);
  }

  @Test
  void updateBagInfoShouldHandleFullBag() {
    scorePanel.updateBagInfo(102);
  }

  @Test
  void updateBagInfoShouldHandleEmptyBag() {
    scorePanel.updateBagInfo(0);
  }

  @Test
  void updateBagInfoShouldHandlePartialBag() {
    scorePanel.updateBagInfo(50);
  }

  @Test
  void highlightCurrentPlayerShouldNotThrow() {
    String[] names = {"Alice", "Bob"};
    int[] scores = {0, 0};
    scorePanel.updateScores(names, scores);
    scorePanel.highlightCurrentPlayer(0, "Alice");
  }

  @Test
  void highlightCurrentPlayerShouldHandleSecondPlayer() {
    String[] names = {"Alice", "Bob"};
    int[] scores = {0, 0};
    scorePanel.updateScores(names, scores);
    scorePanel.highlightCurrentPlayer(1, "Bob");
  }

  @Test
  void highlightCurrentPlayerShouldHandleNegativeIndex() {
    // Index négatif ne doit pas lever d'exception
    scorePanel.updateScores(new String[]{"Alice"}, new int[]{0});
    scorePanel.highlightCurrentPlayer(-1, "Alice");
  }

  @Test
  void updateScoresShouldHandleMismatchedArrayLengths() {
    // Plus de noms que de scores : ne doit pas planter
    String[] names = {"Alice", "Bob", "Charlie"};
    int[] scores = {10, 20};
    scorePanel.updateScores(names, scores);
  }

  @Test
  void scorePanelShouldHavePositivePreferredWidth() {
    assertTrue(scorePanel.getPrefWidth() > 0);
  }
}