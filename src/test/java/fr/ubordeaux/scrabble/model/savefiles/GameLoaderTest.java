package fr.ubordeaux.scrabble.model.savefiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.ai.AiPlayer;
import fr.ubordeaux.scrabble.model.core.Game;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GameLoaderTest {
  private GameLoader loader;

  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() {
    loader = new GameLoader();
  }

  @Test
  void testLoadSuperScrabbleDetection() throws Exception {
    String content = "[settings]\nsuper-scrabble=true\n[game]\n"
        + "---------------------\n".repeat(21);
    Path path = tempDir.resolve("super.scrabble");
    Files.writeString(path, content);

    Game game = loader.loadGame(path.toString());
    assertEquals(21, game.getBoard().getSize(),
      "Board should have size 21 (F9) [cite: 118]");
  }

  @Test
  void testLoadAiPlayerSettings() throws Exception {
    String content = "[settings]\n"
        + "player-1-type=ai\n"
        + "player-1-ai-mode=Expectiminimax\n"
        + "player-1-name=DeepBlue\n"
        + "[game]\n1\n" + "---------------\n".repeat(15)
        + "score-1: 50";

    Path path = tempDir.resolve("ai_load.scrabble");
    Files.writeString(path, content);

    Game game = loader.loadGame(path.toString());

    assertFalse(game.getPlayers().isEmpty());
    assertTrue(game.getPlayers().get(0) instanceof AiPlayer,
      "Player should be an AI (F8) [cite: 109]");
    AiPlayer ai = (AiPlayer) game.getPlayers().get(0);
    assertEquals("DeepBlue", ai.getName());
    assertTrue(ai.isExpectiminimaxMode(),
      "Expectiminimax mode should be restored (F36) [cite: 257]");
  }

  @Test
  void testLoadHistoryWithExchange() throws Exception {
    String content = "[settings]\n[game]\n1\n" + "---------------\n".repeat(15)
        + "[history]\n1 exchange AZERTY";

    Path path = tempDir.resolve("history_exchange.scrabble");
    Files.writeString(path, content);

    Game game = loader.loadGame(path.toString());
    assertEquals(1, game.getUndoRedo().getHistory().size());
    assertEquals("AZERTY", game.getUndoRedo().getHistory().get(0).getTiles().stream()
        .map(t -> String.valueOf(t.getCharacter())).reduce("", String::concat));
  }
}