package fr.ubordeaux.scrabble.model.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.ai.AiPlayer;
import fr.ubordeaux.scrabble.model.enums.Direction;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.model.utils.Point;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class GameAdvancedTest {

  @Test
  void executeMoveShouldFailWhenGameIsOver() {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    game.addPlayer(alice);
    game.setGameOver(true);

    assertThrows(IllegalStateException.class, () -> game.executeMove(Move.createPass(alice)));
  }

  @Test
  void undoRedoShouldWorkForHumanPlayAndResetFirstMoveFlag() {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    game.addPlayer(alice);

    alice.getRack().setTiles(new ArrayList<>(List.of(new Tile('a'))));
    Move play =
        Move.createPlay(alice, List.of(new Tile('a')), new Point(7, 7), Direction.HORIZONTAL);

    game.executeMove(play);
    assertNotNull(game.getBoard().getSquare(new Point(7, 7)).getTile());
    assertTrue(game.isFirstMoveDone());

    game.undo();
    assertNull(game.getBoard().getSquare(new Point(7, 7)).getTile());
    assertEquals(0, alice.getScore());
    assertTrue(!game.isFirstMoveDone());

    game.redo();
    assertNotNull(game.getBoard().getSquare(new Point(7, 7)).getTile());
    assertTrue(game.isFirstMoveDone());
  }

  @Test
  void undoRedoShouldDoNothingWhenCurrentPlayerIsNotHuman() {
    Game game = new Game();
    AiPlayer bot = new AiPlayer("Bot", 1, 5, PlayerColor.BLUE);
    game.addPlayer(bot);

    assertDoesNotThrow(game::undo);
    assertDoesNotThrow(game::redo);
    assertEquals(0, game.getUndoRedo().getHistory().size());
  }

  @Test
  void networkHelpersShouldFindPlayerForceTilesAndSyncBoard() {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    HumanPlayer bob = new HumanPlayer("Bob", PlayerColor.RED);
    game.addPlayer(alice);
    game.addPlayer(bob);

    assertEquals(alice, game.getPlayerFromName("Alice"));
    assertNull(game.getPlayerFromName("Unknown"));

    List<Tile> forcedTiles = List.of(new Tile('x'), new Tile('y'));
    game.forceTilesToPlayer("Alice", forcedTiles);
    assertEquals(2, alice.getRack().getTiles().size());

    assertDoesNotThrow(() -> game.syncBoard(null));
    assertDoesNotThrow(() -> game.syncBoard("short"));

    StringBuilder boardData = new StringBuilder(".".repeat(225));
    boardData.setCharAt(0, 'z');
    game.syncBoard(boardData.toString());

    assertEquals('z', game.getBoard().getSquare(new Point(0, 0)).getTile().getCharacter());
  }

  @Test
  void printDebugStateShouldRunInBothModes() {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    game.addPlayer(alice);

    assertDoesNotThrow(() -> game.printDebugState(true, false));
    game.getBag().setOnlineSize(42);
    assertDoesNotThrow(() -> game.printDebugState(false, true));
  }
}
