package fr.ubordeaux.scrabble.model.savefiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.dictionary.core.Game;
import fr.ubordeaux.scrabble.model.utils.Point;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for the GameLoader class.
 * Validates file parsing and error handling with line numbers[cite: 209, 231].
 */
class GameLoaderTest {

  private GameLoader loader;

  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() {
    loader = new GameLoader();
  }

  /**
   * Tests that a valid file correctly restores board and player states[cite:
   * 207].
   */
  @Test
  void loadGameShouldRestoreStateCorrectly() throws Exception {
    // Row 8, Col 8 (h8) has tile 'A'
    String content = """
        [settings]
        blitz true

        [game]
        1
        ---------------
        ---------------
        ---------------
        ---------------
        ---------------
        ---------------
        ---------------
        -------A-------
        ---------------
        ---------------
        ---------------
        ---------------
        ---------------
        ---------------
        ---------------
        rack-1: XYZ
        score-1: 10

        [history]
        1 h8h A""";

    Path path = tempDir.resolve("valid.scrabble");
    Files.writeString(path, content);

    Game game = loader.loadGame(path.toString());

    assertTrue(game.isBlitzModeEnabled());
    assertEquals('A', game.getBoard().getSquare(new Point(7, 7)).getTile().getCharacter());
    assertEquals(10, game.getPlayers().getFirst().getScore());
  }

  /**
   * Verifies that single-line and multi-line comments are ignored[cite: 211,
   * 212].
   */
  @Test
  void loadGameShouldHandleCommentsCorrectly() throws Exception {
    String content = """
        { Block\s
         Comment }
        [settings] # Line comment
        blitz true
        [game]
        ---------------
        ---------------
        ---------------
        ---------------
        ---------------
        ---------------
        ---------------
        ---------------
        ---------------
        ---------------
        ---------------
        ---------------
        ---------------
        ---------------
        ---------------
        """;

    Path path = tempDir.resolve("comments.scrabble");
    Files.writeString(path, content);

    Game game = loader.loadGame(path.toString());
    assertTrue(game.isBlitzModeEnabled());
  }

  /**
   * Tests error reporting with the specific line number[cite: 231].
   */
  @Test
  void loadGameShouldThrowExceptionWithLineNumberOnFormatError() throws Exception {
    // Error expected on line 2
    String content = """
        [settings]
        [invalid_section]
        data""";

    Path path = tempDir.resolve("error.scrabble");
    Files.writeString(path, content);

    Exception exception = assertThrows(Exception.class, () -> loader.loadGame(path.toString()));
    assertTrue(exception.getMessage().contains("line 2"));
  }
}