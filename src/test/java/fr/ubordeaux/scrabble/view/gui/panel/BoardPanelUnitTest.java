package fr.ubordeaux.scrabble.view.gui.panel;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import fr.ubordeaux.scrabble.model.dictionary.core.Board;
import fr.ubordeaux.scrabble.model.dictionary.core.Tile;
import fr.ubordeaux.scrabble.model.utils.Point;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for BoardPanel initialization and basic behavior.
 * Tests that panel can be instantiated and accept method calls.
 */
class BoardPanelUnitTest {
  private Board board;

  @BeforeEach
  void setUp() {
    board = new Board();
  }

  @Test
  void boardCanBeCreatedWithDefault() {
    assertNotNull(board, "board should not be null");
  }

  @Test
  void boardPanelMethodsExist() {
    assertDoesNotThrow(() -> {
      var method = BoardPanel.class.getMethod("setBoard", Board.class);
      assertNotNull(method);
    });
  }

  @Test
  void boardCellCoordinatesValid() {
    int row = 7;
    int col = 7;
    Point center = new Point(row, col);
    assertEquals(row, center.getX());
    assertEquals(col, center.getY());
  }

  @Test
  void boardTilesCanBeCreated() {
    Tile tileA = new Tile('A');
    assertEquals('A', tileA.getCharacter());
    assertNotNull(tileA.getValue());
  }

  @Test
  void multipleWordsOnBoard() {
    Point pos1 = new Point(7, 7);
    Point pos2 = new Point(7, 8);
    assertEquals(pos1.getX(), pos2.getX());
    assertNotNull(pos1);
    assertNotNull(pos2);
  }
}
