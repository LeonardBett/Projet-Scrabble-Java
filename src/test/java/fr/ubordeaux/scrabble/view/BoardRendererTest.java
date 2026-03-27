package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.core.Board;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.utils.Point;
import fr.ubordeaux.scrabble.view.cli.renderer.BoardRenderer;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.Test;

class BoardRendererTest {

  @Test
  void renderShouldPrintGridAndPlacedTile() {
    Board board = new Board();
    board.getSquare(new Point(7, 7)).setTile(new Tile('a'));

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream original = System.out;
    System.setOut(new PrintStream(out));

    new BoardRenderer().render(board, true);

    System.setOut(original);
    String output = out.toString();
    assertTrue(output.contains("  1  2  3"));
    assertTrue(output.contains("a "));
    assertTrue(output.contains(" A "));
  }

  @Test
  void renderWithoutBonusShouldStillPrintBoard() {
    Board board = new Board();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream original = System.out;
    System.setOut(new PrintStream(out));

    new BoardRenderer().render(board, false);

    System.setOut(original);
    String output = out.toString();
    assertTrue(output.contains("_"));
    assertTrue(output.contains("o "));
  }
}
