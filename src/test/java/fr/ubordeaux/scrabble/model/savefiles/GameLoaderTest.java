package fr.ubordeaux.scrabble.model.savefiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.core.Game;
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
   * Verifies that single-line and multi-line comments are ignored[cite: 211, 212].
   */
  @Test
  void loadGameShouldHandleCommentsCorrectly() throws Exception {
    String content = "{ Block \n Comment }\n"
        + "[settings] # Line comment\n"
        + "blitz true\n"
        + "[game]\n"
        + "---------------\n".repeat(15);

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
    String content = "[settings]\n"
        + "[invalid_section]\n" // Error on line 2
        + "data";

    Path path = tempDir.resolve("error.scrabble");
    Files.writeString(path, content);

    Exception exception = assertThrows(Exception.class, () -> loader.loadGame(path.toString()));
    assertTrue(exception.getMessage().contains("line 2"));
  }
}