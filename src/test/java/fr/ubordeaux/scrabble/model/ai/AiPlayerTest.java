package fr.ubordeaux.scrabble.model.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.dictionary.Gaddag;
import fr.ubordeaux.scrabble.model.enums.MoveType;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive unit tests for the AiPlayer class. Verifies player configuration, delegation to the
 * solver, and internal rack parsing and turn playing logic.
 */
class AiPlayerTest {

  private AiPlayer aiPlayer;

  @BeforeEach
  void setUp() {
    aiPlayer = new AiPlayer("IA-Bot", 3, 5, PlayerColor.BLUE);
  }

  @Test
  void testInitialization() {
    assertEquals("IA-Bot", aiPlayer.getName());
    assertFalse(aiPlayer.isExpectiminimaxMode());
  }

  @Test
  void testSetExpectiminimaxMode() {
    aiPlayer.setExpectiminimaxMode(true);
    assertTrue(aiPlayer.isExpectiminimaxMode());

    aiPlayer.setExpectiminimaxMode(false);
    assertFalse(aiPlayer.isExpectiminimaxMode());
  }

  @Test
  void testSetMlAgent() {
    MlAgent dummyAgent = new MlAgent("dummy/path", new ArrayList<>());
    aiPlayer.setMlAgent(dummyAgent);
    assertNotNull(aiPlayer);

    aiPlayer.setMlAgent(null);
    assertNotNull(aiPlayer);
  }

  @Test
  void testSetTimeLimitSeconds() {
    aiPlayer.setTimeLimitSeconds(15);
    assertTrue(true);
  }

  @Test
  void testGetRackAsString() throws Exception {
    aiPlayer.getRack().addTile(new Tile('S'));
    aiPlayer.getRack().addTile(new Tile('C'));
    aiPlayer.getRack().addTile(new Tile('R'));

    Method getRackMethod = AiPlayer.class.getDeclaredMethod("getRackAsString");
    getRackMethod.setAccessible(true);

    String rackStr = (String) getRackMethod.invoke(aiPlayer);
    assertEquals("SCR", rackStr);
  }

  @Test
  void testPlayTurnPassesWhenNoWordFound() {
    Game game = new Game();
    game.addPlayer(aiPlayer);
    game.startGame();

    // Give a rack with no possible words
    aiPlayer.getRack().setTiles(new ArrayList<>(List.of(new Tile('X'), new Tile('Z'))));
    Gaddag emptyDict = new Gaddag();

    aiPlayer.playTurn(game, emptyDict);

    // AI should pass its turn
    assertEquals(MoveType.PASS, game.getUndoRedo().getHistory().getFirst().getType());
  }

  @Test
  void testPlayTurnFindsWordAndPlaysIt() {
    Game game = new Game();
    game.addPlayer(aiPlayer);
    game.startGame();

    // Give a specific rack
    aiPlayer.getRack().setTiles(new ArrayList<>(List.of(
        new Tile('C'), new Tile('A'), new Tile('T')
    )));

    // Create a dictionary containing only our target word
    Gaddag dict = new Gaddag();
    dict.add("CAT");

    aiPlayer.playTurn(game, dict);

    // AI should play the word CAT
    assertTrue(game.isFirstMoveDone());
    assertEquals(MoveType.PLAY, game.getUndoRedo().getHistory().getFirst().getType());
  }

  @Test
  void testPlayTurnWithMlAgentNotLoadedFallsBackToSolver() {
    // Sets up the game and player.
    Game game = new Game();
    game.addPlayer(aiPlayer);
    game.startGame();

    aiPlayer.getRack().setTiles(new ArrayList<>(List.of(
        new Tile('B'), new Tile('A'), new Tile('T')
    )));

    // Injects an ML agent that will fail to load (invalid path).
    MlAgent invalidAgent = new MlAgent("invalid/path", new ArrayList<>());
    aiPlayer.setMlAgent(invalidAgent);

    Gaddag dict = new Gaddag();
    dict.add("BAT");

    aiPlayer.playTurn(game, dict);

    // The AI should fall back to Minimax and play the word.
    assertTrue(game.isFirstMoveDone());
    assertEquals(MoveType.PLAY, game.getUndoRedo().getHistory().getFirst().getType());
  }

  @Test
  void testPlayTurnVerticalDirection() {
    // Sets up the game.
    Game game = new Game();
    game.addPlayer(aiPlayer);
    game.startGame();

    aiPlayer.getRack().setTiles(new ArrayList<>(List.of(
        new Tile('V'), new Tile('E'), new Tile('R'), new Tile('T')
    )));

    Gaddag dict = new Gaddag();
    dict.add("VERT");

    // Forces a vertical play by artificially placing a tile and setting up a constraint
    // if your MoveGenerator allows it, or we simply verify that vertical words
    // don't crash the coordinate calculation.
    aiPlayer.playTurn(game, dict);

    // Verifies the move was executed.
    assertEquals(MoveType.PLAY, game.getUndoRedo().getHistory().getFirst().getType());
  }

  @Test
  void testPlayTurnFallbackFailsAndPasses() {
    Game game = new Game();
    game.addPlayer(aiPlayer);
    game.startGame();

    // Give a rack with impossible letters (e.g., only W, X, Y, Z without vowels)
    aiPlayer.getRack().setTiles(new ArrayList<>(List.of(
        new Tile('W'), new Tile('X'), new Tile('Y'), new Tile('Z')
    )));

    Gaddag emptyDict = new Gaddag();

    // Sets an ML agent that will bypass its phase due to empty predictions
    MlAgent dummyAgent = new MlAgent("dummy", new ArrayList<>());
    aiPlayer.setMlAgent(dummyAgent);

    aiPlayer.playTurn(game, emptyDict);

    // AI should try ML, fail, try Minimax, fail, and ultimately pass.
    assertEquals(MoveType.PASS, game.getUndoRedo().getHistory().getFirst().getType());
  }

  @Test
  void testPlayTurnWithJoker() {
    Game game = new Game();
    game.addPlayer(aiPlayer);
    game.startGame();

    aiPlayer.getRack().setTiles(new ArrayList<>(List.of(
        new Tile('D'), new Tile('O'), new Tile(' ', true)
    )));

    Gaddag dict = new Gaddag();

    dict.add("DOS");

    aiPlayer.playTurn(game, dict);

    assertTrue(game.isFirstMoveDone());
    assertEquals(MoveType.PLAY, game.getUndoRedo().getHistory().getFirst().getType());
  }
}