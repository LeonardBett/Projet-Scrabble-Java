package fr.ubordeaux.scrabble.model.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.core.PlayableWord;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.dictionary.Gaddag;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive unit tests for the MinimaxSolver class.
 * Evaluates core algorithm logic including Minimax and Expectiminimax branches.
 */
class MinimaxSolverTest {

  private MinimaxSolver solver;

  @BeforeEach
  void setUp() {
    solver = new MinimaxSolver(1, 5);
  }

  @Test
  void testDefaultInitialization() {
    assertFalse(solver.isUsingExpectiminimax());
  }

  @Test
  void testSetUseExpectiminimax() {
    solver.setUseExpectiminimax(true);
    assertTrue(solver.isUsingExpectiminimax());

    solver.setUseExpectiminimax(false);
    assertFalse(solver.isUsingExpectiminimax());
  }

  @Test
  void testFindBestMoveReturnsNullWhenNoMoves() {
    Game game = new Game();
    game.addPlayer(new HumanPlayer("H", PlayerColor.RED));
    game.startGame();
    game.getCurrentPlayer().getRack().getTiles().clear();

    assertNull(solver.findBestMove(game, new Gaddag()));
  }

  @Test
  void testFindBestMoveReturnsValidWordDepthOne() {
    Game game = new Game();
    game.addPlayer(new HumanPlayer("P1", PlayerColor.BLUE));
    game.startGame();

    game.getCurrentPlayer().getRack().setTiles(new ArrayList<>(List.of(
        new Tile('A'), new Tile('R'), new Tile('T')
    )));

    Gaddag dict = new Gaddag();
    dict.add("ART");

    PlayableWord bestMove = solver.findBestMove(game, dict);
    assertNotNull(bestMove);
    assertEquals("ART", bestMove.getWord());
  }

  @Test
  void testFindBestMoveTriggersMinimaxDepthTwo() {
    Game game = new Game();
    game.addPlayer(new HumanPlayer("P1", PlayerColor.BLUE));
    game.startGame();

    game.getCurrentPlayer().getRack().setTiles(new ArrayList<>(List.of(
        new Tile('B'), new Tile('O'), new Tile('Y')
    )));

    Gaddag dict = new Gaddag();
    dict.add("BOY");

    // Declaration moved right before its usage to satisfy Checkstyle
    MinimaxSolver deepSolver = new MinimaxSolver(2, 5);
    PlayableWord bestMove = deepSolver.findBestMove(game, dict);

    assertNotNull(bestMove);
    assertEquals("BOY", bestMove.getWord());
  }

  @Test
  void testFindBestMoveTriggersExpectiminimaxDepthTwo() {
    Game game = new Game();
    game.addPlayer(new HumanPlayer("P1", PlayerColor.BLUE));
    game.startGame();

    game.getCurrentPlayer().getRack().setTiles(new ArrayList<>(List.of(
        new Tile('B'), new Tile('O'), new Tile('Y')
    )));

    Gaddag dict = new Gaddag();
    dict.add("BOY");

    // Declaration moved right before its usage to satisfy Checkstyle
    MinimaxSolver deepSolver = new MinimaxSolver(2, 5);
    deepSolver.setUseExpectiminimax(true);
    PlayableWord bestMove = deepSolver.findBestMove(game, dict);

    assertNotNull(bestMove);
    assertEquals("BOY", bestMove.getWord());
  }

  @Test
  void testDrawRandomRackNormal() throws Exception {
    Method drawMethod =
        MinimaxSolver.class.getDeclaredMethod("drawRandomRack", List.class, int.class);
    drawMethod.setAccessible(true);

    List<Character> unseen = Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I');
    Character[] rack = (Character[]) drawMethod.invoke(solver, unseen, 7);

    assertEquals(7, rack.length);
  }

  @Test
  void testDrawRandomRackNotEnoughTiles() throws Exception {
    Method drawMethod =
        MinimaxSolver.class.getDeclaredMethod("drawRandomRack", List.class, int.class);
    drawMethod.setAccessible(true);

    List<Character> unseen = Arrays.asList('X', 'Y', 'Z');
    Character[] rack = (Character[]) drawMethod.invoke(solver, unseen, 7);

    assertEquals(3, rack.length);
  }

  @Test
  void testFindBestMoveWithTimeLimitReached() {
    // Sets up the game environment first.
    Game game = new Game();
    game.addPlayer(new HumanPlayer("P1", PlayerColor.BLUE));
    game.startGame();

    game.getCurrentPlayer().getRack().setTiles(new ArrayList<>(List.of(
        new Tile('A'), new Tile('R'), new Tile('T')
    )));

    Gaddag dict = new Gaddag();
    dict.add("ART");

    // Sets an impossibly short time limit (0 seconds) right before usage
    // to satisfy the Checkstyle distance rule.
    MinimaxSolver timeoutSolver = new MinimaxSolver(2, 0);

    // The solver should break out of loops early due to the time constraint.
    PlayableWord bestMove = timeoutSolver.findBestMove(game, dict);

    // Asserts that the bestMove is null because the 0ms timeout correctly
    // prevents any move evaluation.
    assertNull(bestMove);
  }

  @Test
  void testUnseenTilesWithBoardTiles() {
    Game game = new Game();
    game.addPlayer(new HumanPlayer("P1", PlayerColor.BLUE));
    game.startGame();

    // Places a 'B' on the board to allow connecting a word.
    game.getBoard().getSquare(new fr.ubordeaux.scrabble.model.utils.Point(7, 7))
        .setTile(new Tile('B'));

    // Provides the remaining letters for "BOY" in the rack.
    game.getCurrentPlayer().getRack().setTiles(new ArrayList<>(List.of(
        new Tile('O'), new Tile('Y')
    )));

    Gaddag dict = new Gaddag();
    dict.add("BOY");

    // Requires depth 2 to trigger the getUnseenTiles and expectiminimax/minimax logic.
    MinimaxSolver deepSolver = new MinimaxSolver(2, 5);
    PlayableWord bestMove = deepSolver.findBestMove(game, dict);

    // Now the MoveGenerator can find "BOY" by connecting the rack to the board.
    assertNotNull(bestMove);
    assertEquals("BOY", bestMove.getWord());
  }

  @Test
  void testEvaluateRackLeaveCoverage() {
    Game game = new Game();
    game.addPlayer(new HumanPlayer("P1", PlayerColor.BLUE));
    game.startGame();

    // Provides a rack designed to trigger specific rack leave heuristic branches:
    // 'S' (bonus), ' ' (huge bonus), 'Z' (penalty), 'V' (penalty), 'A', 'E' (vowels).
    game.getCurrentPlayer().getRack().setTiles(new ArrayList<>(List.of(
        new Tile('S'), new Tile(' ', true), new Tile('Z'),
        new Tile('V'), new Tile('A'), new Tile('E'), new Tile('T')
    )));

    Gaddag dict = new Gaddag();
    dict.add("TEA"); // Will leave S, Blank, Z, V in the rack.

    // Uses depth 1 to trigger evaluateRackLeave immediately.
    MinimaxSolver heuristicSolver = new MinimaxSolver(1, 5);
    PlayableWord bestMove = heuristicSolver.findBestMove(game, dict);

    // Verifies a move was found despite the complex heuristic calculations.
    assertNotNull(bestMove);
    assertEquals("TEA", bestMove.getWord());
  }
}