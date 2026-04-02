package fr.ubordeaux.scrabble.model.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.enums.Direction;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.model.utils.Point;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class MoveHandlerTest {

  @Test
  void handlePlayMoveShouldRejectFirstMoveNotCoveringCenter() {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    game.addPlayer(alice);
    final MoveHandler handler = new MoveHandler(game);

    alice.getRack().setTiles(new ArrayList<>(List.of(new Tile('a'))));
    Move move =
        Move.createPlay(alice, List.of(new Tile('a')), new Point(0, 0), Direction.HORIZONTAL);

    assertThrows(IllegalArgumentException.class, () -> handler.handlePlayMove(move));
  }

  @Test
  void handlePlayMoveShouldAcceptMoveTouchingExistingTileByAdjacency() {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    game.addPlayer(alice);
    final MoveHandler handler = new MoveHandler(game);

    game.getBoard().getSquare(new Point(7, 7)).setTile(new Tile('a'));
    game.setFirstMoveDone(true);

    alice.getRack().setTiles(new ArrayList<>(List.of(new Tile('t'))));
    Move move =
        Move.createPlay(alice, List.of(new Tile('t')), new Point(8, 7), Direction.HORIZONTAL);

    assertDoesNotThrow(() -> handler.handlePlayMove(move));
    assertNotNull(game.getBoard().getSquare(new Point(8, 7)).getTile());
  }

  @Test
  void getCompleteWordShouldIncludeExistingPrefixAndSuffix() {
    Game game = new Game();
    MoveHandler handler = new MoveHandler(game);

    game.getBoard().getSquare(new Point(6, 7)).setTile(new Tile('c'));
    game.getBoard().getSquare(new Point(8, 7)).setTile(new Tile('t'));

    String word =
        handler.getCompleteWord(new Point(7, 7), Direction.HORIZONTAL, List.of(new Tile('a')));

    assertEquals("cat", word);
  }

  @Test
  void getCompleteWordShouldIncludeNewlyPlacedTilesOnEmptyBoard() {
    Game game = new Game();
    MoveHandler handler = new MoveHandler(game);

    String word = handler.getCompleteWord(new Point(7, 7), Direction.HORIZONTAL,
        List.of(new Tile('r'), new Tile('u'), new Tile('e'), new Tile('s')));

    assertEquals("rues", word);
  }

  @Test
  void handlePlayMoveShouldPlaceTilesScoreAndRefill() {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    game.addPlayer(alice);
    MoveHandler handler = new MoveHandler(game);

    alice.getRack().setTiles(new ArrayList<>(List.of(new Tile('a'), new Tile('n'))));
    Move move = Move.createPlay(alice, List.of(new Tile('a'), new Tile('n')), new Point(7, 7),
        Direction.HORIZONTAL);

    handler.handlePlayMove(move);

    assertNotNull(game.getBoard().getSquare(new Point(7, 7)).getTile());
    assertNotNull(game.getBoard().getSquare(new Point(8, 7)).getTile());
    assertTrue(alice.getScore() > 0);
    assertEquals(2, move.getPlacedPositions().size());
    assertEquals(2, move.getPlacedTiles().size());
    assertNotNull(move.getDrawnTiles());
  }

  @Test
  void handlePlayMoveShouldRejectTileNotInRack() {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    game.addPlayer(alice);
    MoveHandler handler = new MoveHandler(game);

    alice.getRack().setTiles(new ArrayList<>());
    Move move =
        Move.createPlay(alice, List.of(new Tile('a')), new Point(7, 7), Direction.HORIZONTAL);

    assertThrows(IllegalArgumentException.class, () -> handler.handlePlayMove(move));
  }

  @Test
  void handlePlayMoveShouldUseJokerFromRackWithZeroPoints() {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    game.addPlayer(alice);
    MoveHandler handler = new MoveHandler(game);

    alice.getRack().setTiles(new ArrayList<>(List.of(new Tile('A'), new Tile(' ', true))));

    Move move = Move.createPlay(alice, List.of(new Tile('A'), new Tile('N', true)),
        new Point(7, 7), Direction.HORIZONTAL);

    handler.handlePlayMove(move);

    Tile placedA = game.getBoard().getSquare(new Point(7, 7)).getTile();
    Tile placedN = game.getBoard().getSquare(new Point(8, 7)).getTile();

    assertNotNull(placedA);
    assertNotNull(placedN);
    assertEquals('A', placedA.getCharacter());
    assertEquals(1, placedA.getValue());
    assertEquals('N', placedN.getCharacter());
    assertEquals(0, placedN.getValue());
    assertTrue(placedN.isJoker());
  }

  @Test
  void handlePlayMoveShouldRejectDisconnectedWordAfterFirstMove() {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    game.addPlayer(alice);
    final MoveHandler handler = new MoveHandler(game);

    game.setFirstMoveDone(true);
    game.getBoard().getSquare(new Point(7, 7)).setTile(new Tile('a'));
    alice.getRack().setTiles(new ArrayList<>(List.of(new Tile('b'))));

    Move move =
        Move.createPlay(alice, List.of(new Tile('b')), new Point(0, 0), Direction.HORIZONTAL);

    assertThrows(IllegalArgumentException.class, () -> handler.handlePlayMove(move));
  }

  @Test
  void handlePlayMoveShouldRejectOutOfBoundsWord() {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    game.addPlayer(alice);
    MoveHandler handler = new MoveHandler(game);

    alice.getRack().setTiles(new ArrayList<>(List.of(new Tile('a'), new Tile('b'))));
    Move move = Move.createPlay(alice, List.of(new Tile('a'), new Tile('b')), new Point(14, 14),
        Direction.HORIZONTAL);

    assertThrows(IllegalArgumentException.class, () -> handler.handlePlayMove(move));
  }

  @Test
  void handlePlayMoveShouldUseCustomDictionaryPathFromGame() throws Exception {
    Path customDictionary = Files.createTempFile("scrabble-custom-dict", ".txt");
    Files.writeString(customDictionary, "AA\n");

    Game game = new Game();
    game.setDictionaryPathOverride(customDictionary.toString());

    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    game.addPlayer(alice);
    MoveHandler handler = new MoveHandler(game);

    alice.getRack().setTiles(new ArrayList<>(List.of(new Tile('a'), new Tile('a'))));
    Move move = Move.createPlay(alice, List.of(new Tile('a'), new Tile('a')), new Point(7, 7),
        Direction.HORIZONTAL);

    assertDoesNotThrow(() -> handler.handlePlayMove(move));
  }

  @Test
  void exchangeMoveShouldValidateBagAndRackAndSucceed() {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    game.addPlayer(alice);
    MoveHandler handler = new MoveHandler(game);

    alice.getRack().setTiles(new ArrayList<>(List.of(new Tile('a'), new Tile('b'), new Tile('c'),
        new Tile('d'), new Tile('e'), new Tile('f'), new Tile('g'))));

    Move invalid = Move.createExchange(alice, List.of(new Tile('z')));
    assertThrows(IllegalArgumentException.class, () -> handler.handleExchangeMove(invalid));

    Move valid = Move.createExchange(alice, List.of(new Tile('a'), new Tile('b')));
    handler.handleExchangeMove(valid);
    assertEquals(Rack.MAX_SIZE, alice.getRack().getTiles().size());
    assertEquals(2, valid.getDrawnTiles().size());

    while (game.getBag().size() >= 7) {
      game.getBag().drawTile();
    }
    Move blockedByBag = Move.createExchange(alice, List.of(new Tile('c')));
    assertThrows(IllegalStateException.class, () -> handler.handleExchangeMove(blockedByBag));
  }

  @Test
  void revertMoveShouldHandlePassAndRestorePlayState() {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    game.addPlayer(alice);
    MoveHandler handler = new MoveHandler(game);

    assertDoesNotThrow(() -> handler.revertMove(Move.createPass(alice)));

    alice.getRack().setTiles(new ArrayList<>(List.of(new Tile('a'), new Tile('n'))));
    Move play = Move.createPlay(alice, List.of(new Tile('a'), new Tile('n')), new Point(7, 7),
        Direction.HORIZONTAL);
    handler.handlePlayMove(play);
    int scoreAfterPlay = alice.getScore();

    handler.revertMove(play);

    assertTrue(scoreAfterPlay > 0);
    assertEquals(0, alice.getScore());
    assertEquals(2, alice.getRack().getTiles().size());
    assertNull(game.getBoard().getSquare(new Point(7, 7)).getTile());
    assertNull(game.getBoard().getSquare(new Point(8, 7)).getTile());
  }

  @Test
  void computePositionsShouldReturnPositionsAndRejectOutOfBounds() throws Exception {
    Game game = new Game();
    MoveHandler handler = new MoveHandler(game);

    Method computePositions = MoveHandler.class.getDeclaredMethod("computePositions", Point.class,
        Direction.class, int.class);
    computePositions.setAccessible(true);

    @SuppressWarnings("unchecked")
    List<Point> positions =
        (List<Point>) computePositions.invoke(handler, new Point(0, 0), Direction.HORIZONTAL, 3);
    assertEquals(3, positions.size());
    assertEquals(new Point(2, 0), positions.get(2));

    Exception ex = assertThrows(Exception.class,
        () -> computePositions.invoke(handler, new Point(14, 14), Direction.HORIZONTAL, 2));
    assertTrue(ex.getCause() instanceof IllegalArgumentException);
  }

  @Test
  void validatePlacementShouldCoverCenterTouchAndConflictRules() throws Exception {
    Game game = new Game();
    MoveHandler handler = new MoveHandler(game);

    Method validatePlacement =
        MoveHandler.class.getDeclaredMethod("validatePlacement", List.class, List.class);
    validatePlacement.setAccessible(true);

    List<Point> offCenter = List.of(new Point(0, 0));
    List<Tile> offCenterTiles = List.of(new Tile('a'));
    Exception firstMoveEx = assertThrows(Exception.class,
        () -> validatePlacement.invoke(handler, offCenter, offCenterTiles));
    assertTrue(firstMoveEx.getCause() instanceof IllegalArgumentException);

    assertDoesNotThrow(
        () -> validatePlacement.invoke(handler, List.of(new Point(7, 7)), List.of(new Tile('a'))));

    game.setFirstMoveDone(true);
    game.getBoard().getSquare(new Point(7, 7)).setTile(new Tile('x'));
    Exception conflictEx = assertThrows(Exception.class,
        () -> validatePlacement.invoke(handler, List.of(new Point(7, 7)), List.of(new Tile('a'))));
    assertTrue(conflictEx.getCause() instanceof IllegalArgumentException);

    assertDoesNotThrow(
        () -> validatePlacement.invoke(handler, List.of(new Point(7, 8)), List.of(new Tile('b'))));

    Exception noTouchEx = assertThrows(Exception.class,
        () -> validatePlacement.invoke(handler, List.of(new Point(0, 0)), List.of(new Tile('c'))));
    assertTrue(noTouchEx.getCause() instanceof IllegalArgumentException);
  }
}
