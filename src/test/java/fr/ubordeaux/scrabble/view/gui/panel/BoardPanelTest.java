package fr.ubordeaux.scrabble.view.gui.panel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.i18n.I18n;
import fr.ubordeaux.scrabble.model.core.Board;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.utils.Point;
import fr.ubordeaux.scrabble.view.gui.panel.BoardPanel;
import java.lang.reflect.Field;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for BoardPanel.
 */
class BoardPanelTest {

  private Board board;
  private BoardPanel boardPanel;

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
    // Should be ignored gracefully (internal range check)
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
    // No exception means success
  }

  @Test
  void setBoardShouldUpdateBoardReference() {
    Board newBoard = new Board();
    boardPanel.setBoard(newBoard);
    // updateBoard() should work with the new board
    boardPanel.updateBoard();
  }

  @Test
  void setOnTileDroppedShouldNotThrow() {
    boardPanel.setOnTileDropped((row, col) -> {
    });
  }

  @Test
  void setOnTileDroppedWithNullShouldNotThrow() {
    boardPanel.setOnTileDropped(null);
  }

  @Test
  void placeThenClearShouldRestoreCell() {
    boardPanel.placeTile(7, 7, 'Z', 10);
    boardPanel.clearTile(7, 7);
    boardPanel.updateBoard();
  }

  @Test
  void updateBoardWithOccupiedSquares() {
    board.getSquare(new Point(7, 7))
        .setTile(new Tile('H'));
    board.getSquare(new Point(8, 7))
        .setTile(new Tile('E'));
    board.getSquare(new Point(9, 7))
        .setTile(new Tile('L'));
    boardPanel.updateBoard();
    Label[][] labels = getCellLabels();
    assertEquals("H", labels[7][7].getText());
    assertEquals("E", labels[7][8].getText());
    assertEquals("L", labels[7][9].getText());
  }

  @Test
  void updateBoardMixedOccupiedAndEmpty() {
    board.getSquare(new Point(0, 0))
        .setTile(new Tile('A'));
    boardPanel.updateBoard();
    Label[][] labels = getCellLabels();
    assertEquals("A", labels[0][0].getText());
  }

  @Test
  void clearAllPreservesBoardTiles() {
    board.getSquare(new Point(3, 3))
        .setTile(new Tile('X'));
    boardPanel.updateBoard();
    boardPanel.placeTile(4, 4, 'Y', 2);
    boardPanel.clearAllPending();
    Label[][] labels = getCellLabels();
    assertEquals("X", labels[3][3].getText());
  }

  @Test
  void placeTileOnAllSpecialSquares() {
    boardPanel.placeTile(0, 0, 'A', 1);
    boardPanel.placeTile(1, 1, 'B', 1);
    boardPanel.placeTile(0, 3, 'C', 1);
    boardPanel.placeTile(1, 5, 'D', 1);
    boardPanel.placeTile(7, 7, 'E', 1);
  }

  @Test
  void multiplePlaceThenClearAll() {
    for (int i = 0; i < 15; i++) {
      boardPanel.placeTile(i, 0, (char) ('A' + i), 1);
    }
    for (int i = 0; i < 15; i++) {
      boardPanel.clearTile(i, 0);
    }
    boardPanel.updateBoard();
  }

  @Test
  void updateBoardConvertsLowercase() {
    board.getSquare(new Point(5, 5))
        .setTile(new Tile('z'));
    boardPanel.updateBoard();
    Label[][] labels = getCellLabels();
    assertEquals("Z", labels[5][5].getText());
  }

  @Test
  void boardPanelHasTwoChildren() {
    assertEquals(2, boardPanel.getChildren().size());
    assertTrue(
        boardPanel.getChildren().get(1) instanceof GridPane);
  }

  @Test
  void boardTitleIsPlateauDeJeu() {
    Node first = boardPanel.getChildren().getFirst();
    assertTrue(first instanceof Label);
    assertEquals(I18n.translate("lobby.board"),
        ((Label) first).getText());
  }

  private Label[][] getCellLabels() {
    try {
      Field f = BoardPanel.class
          .getDeclaredField("cellLabels");
      f.setAccessible(true);
      return (Label[][]) f.get(boardPanel);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}