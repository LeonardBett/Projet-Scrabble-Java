package fr.ubordeaux.scrabble.model.savefiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.enums.Direction;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.model.utils.Point;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for the SaveManager class.
 * Ensures correct serialization of the board, racks, scores, and history[cite: 205, 210].
 */
class SaveManagerTest {

  private SaveManager saveManager;
  private Game game;

  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() {
    saveManager = new SaveManager();
    game = new Game();
  }

  /**
   * Tests a complete save including multiple players, words on the board,
   * scores, filled racks, and mixed history (PLAY/PASS)[cite: 210, 233].
   */
  @Test
  void testSaveGameFullComplexState() throws IOException {
    // 1. Setup players with colors
    HumanPlayer p1 = new HumanPlayer("Alice", PlayerColor.RED);
    HumanPlayer p2 = new HumanPlayer("Bob", PlayerColor.BLUE);
    game.addPlayer(p1);
    game.addPlayer(p2);

    // 2. Configure Blitz mode and scores
    game.enableBlitzMode();
    p1.addScore(120);
    p2.addScore(85);

    // 3. Fill Racks
    p1.getRack().addTile(new Tile('A'));
    p1.getRack().addTile(new Tile('B'));

    // 4. Place tiles on the board [cite: 219]
    // Place "TEST" horizontally at h8 (center 7,7)

    game.getBoard().getSquare(new Point(7, 7)).setTile(new Tile('T'));
    game.getBoard().getSquare(new Point(8, 7)).setTile(new Tile('E'));
    game.getBoard().getSquare(new Point(9, 7)).setTile(new Tile('S'));
    game.getBoard().getSquare(new Point(10, 7)).setTile(new Tile('T'));
    game.setFirstMoveDone(true);

    // Place "TEST" horizontally at h8 (center 7,7)
    Point h8 = new Point(7, 7);
    // 5. Create history [cite: 236]
    Move playMove = Move.createPlay(p1,
        List.of(new Tile('T'), new Tile('E'), new Tile('S'), new Tile('T')),
        h8, Direction.HORIZONTAL);
    game.getUndoRedo().addMove(playMove);

    Move passMove = Move.createPass(p2);
    game.getUndoRedo().addMove(passMove);

    // 6. Action: Save to file
    Path savePath = tempDir.resolve("complex_save.scrabble");
    saveManager.saveGame(game, savePath.toString());

    // 7. Verify ASCII content [cite: 206]
    List<String> content = Files.readAllLines(savePath);

    // Verify [settings]
    assertTrue(content.contains("[settings] # Global game parameters"));
    assertTrue(content.contains("blitz=true"));

    // Verify [game] current player
    assertTrue(content.contains("[game]"));
    assertTrue(content.contains("1 # current player index"));

    // Verify board line (h8 is row 8) [cite: 219]
    boolean foundTestOnBoard = content.stream().anyMatch(line -> line.contains("-------TEST----"));
    assertTrue(foundTestOnBoard);

    // Verify Racks and Scores [cite: 227, 228]
    assertTrue(content.contains("rack-1: AB"));
    assertTrue(content.contains("score-1: 120"));

    // Verify [history] format [cite: 236]
    assertTrue(content.contains("1 h8h TEST"));
    assertTrue(content.contains("2 pass"));
  }

  /**
   * Verifies that the coordinate conversion follows Scrabble notation[cite: 201].
   */
  @Test
  void testCoordinateConversionFormat() throws IOException {
    HumanPlayer p1 = new HumanPlayer("Tester", PlayerColor.GREEN);
    game.addPlayer(p1);

    // Position a1 (0,0) Vertical
    Move move = Move.createPlay(p1, List.of(new Tile('Z')), new Point(0, 0), Direction.VERTICAL);
    game.getUndoRedo().addMove(move);

    Path savePath = tempDir.resolve("coord_test.scrabble");
    saveManager.saveGame(game, savePath.toString());

    List<String> content = Files.readAllLines(savePath);
    // Expected: 1 a1v Z
    assertTrue(content.contains("1 a1v Z"));
  }

  /**
   * Test the behavior of an empty game.
   */
  @Test
  void testSaveEmptyGame() throws IOException {
    Path savePath = tempDir.resolve("empty.scrabble");
    saveManager.saveGame(game, savePath.toString());

    List<String> content = Files.readAllLines(savePath);
    assertTrue(content.contains("players-count=0"));

    long dashLinesCount = content.stream().filter(line -> line.equals("---------------")).count();
    assertEquals(15, dashLinesCount, "Empty board must contain 15 dashed lines");
  }
}