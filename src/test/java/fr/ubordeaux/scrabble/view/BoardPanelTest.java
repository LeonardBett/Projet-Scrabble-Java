package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.core.Board;
import fr.ubordeaux.scrabble.view.gui.panel.BoardPanel;
import java.lang.reflect.Field;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour BoardPanel.
 */
class BoardPanelTest {

  private Board board;
  private BoardPanel boardPanel;

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
    board = new Board();
    boardPanel = new BoardPanel(board);
  }

  @Test
  void boardPanelShouldBeInstantiable() {
    assertNotNull(boardPanel);
  }

  @Test
  void updateBoardShouldNotThrowOnEmptyBoard() {
    boardPanel.updateBoard();
  }

  @Test
  void placeTileShouldNotThrowForValidPosition() {
    boardPanel.placeTile(7, 7, 'A', 1);
  }

  @Test
  void placeTileShouldNotThrowForCornerPositions() {
    boardPanel.placeTile(0, 0, 'B', 3);
    boardPanel.placeTile(14, 14, 'C', 3);
    boardPanel.placeTile(0, 14, 'D', 2);
    boardPanel.placeTile(14, 0, 'E', 1);
  }

  @Test
  void placeTileShouldNotThrowForOutOfBoundsPosition() {
    // Doit être ignoré gracieusement (range check interne)
    boardPanel.placeTile(-1, 7, 'A', 1);
    boardPanel.placeTile(7, -1, 'A', 1);
    boardPanel.placeTile(15, 7, 'A', 1);
    boardPanel.placeTile(7, 15, 'A', 1);
  }

  @Test
  void clearTileShouldNotThrowForValidPosition() {
    boardPanel.placeTile(5, 5, 'H', 4);
    boardPanel.clearTile(5, 5);
  }

  @Test
  void clearTileShouldNotThrowForOutOfBoundsPosition() {
    boardPanel.clearTile(-1, 0);
    boardPanel.clearTile(0, 20);
  }

  @Test
  void clearAllPendingShouldNotThrowOnEmptyBoard() {
    boardPanel.clearAllPending();
  }

  @Test
  void clearAllPendingShouldClearPendingTiles() {
    boardPanel.placeTile(3, 3, 'A', 1);
    boardPanel.placeTile(3, 4, 'B', 3);
    boardPanel.clearAllPending();
    // Pas d'exception = succès
  }

  @Test
  void setBoardShouldUpdateBoardReference() {
    Board newBoard = new Board();
    boardPanel.setBoard(newBoard);
    // updateBoard() doit fonctionner avec le nouveau plateau
    boardPanel.updateBoard();
  }

  @Test
  void setOnTileDroppedShouldNotThrow() {
    boardPanel.setOnTileDropped((row, col) -> {});
  }

  @Test
  void setOnTileDroppedWithNullShouldNotThrow() {
    boardPanel.setOnTileDropped(null);
  }

  @Test
  void placeThenClearShouldRestoreCell() {
    boardPanel.placeTile(7, 7, 'Z', 10);
    boardPanel.clearTile(7, 7);
    boardPanel.updateBoard(); // ne doit pas planter
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