package fr.ubordeaux.scrabble.view.gui.panel;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ScorePanel update behavior.
 */
class ScorePanelUnitTest {
  private Game game;

  @BeforeEach
  void setUp() {
    game = new Game();
    game.addPlayer(new HumanPlayer("P1", PlayerColor.BLUE));
    game.addPlayer(new HumanPlayer("P2", PlayerColor.RED));
    game.startGame();
  }

  @Test
  void playerScoresInitiallyZero() {
    int score1 = game.getPlayers().get(0).getScore();
    int score2 = game.getPlayers().get(1).getScore();
    assertEquals(0, score1, "initial score should be 0");
    assertEquals(0, score2, "initial score should be 0");
  }

  @Test
  void scorePanelUpdateScoresMethodExists() {
    assertDoesNotThrow(() -> {
      var updateScores = ScorePanel.class.getMethod("updateScores",
          String[].class, int[].class);
      assertNotNull(updateScores);
    });
  }

  @Test
  void scorePanelUpdateBagMethodExists() {
    assertDoesNotThrow(() -> {
      var updateBag = ScorePanel.class.getMethod("updateBagInfo", int.class);
      assertNotNull(updateBag);
    });
  }

  @Test
  void scorePanelHighlightPlayerMethodExists() {
    assertDoesNotThrow(() -> {
      var highlight = ScorePanel.class.getMethod("highlightCurrentPlayer",
          int.class, String.class);
      assertNotNull(highlight);
    });
  }

  @Test
  void gameHasTwoPlayers() {
    assertEquals(2, game.getPlayers().size(), "game should have 2 players");
  }

  @Test
  void playerNamesRetrievable() {
    String p1Name = game.getPlayers().get(0).getName();
    String p2Name = game.getPlayers().get(1).getName();
    assertEquals("P1", p1Name);
    assertEquals("P2", p2Name);
  }
}
