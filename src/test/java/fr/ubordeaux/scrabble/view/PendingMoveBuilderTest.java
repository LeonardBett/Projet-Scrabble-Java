package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import fr.ubordeaux.scrabble.controller.builders.PendingMoveBuilderController;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.enums.Direction;
import fr.ubordeaux.scrabble.model.enums.MoveType;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.model.utils.Point;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PendingMoveBuilderTest {

  private Player player;
  private Game game;

  @BeforeEach
  void setUp() {
    player = new HumanPlayer("Bob", PlayerColor.BLUE);
    game = new Game();
  }

  @Test
  void buildShouldReturnNullForEmptyMap() {
    assertNull(PendingMoveBuilderController.build(new HashMap<>(), player, game));
  }

  @Test
  void buildShouldReturnHorizontalMoveForSingleTileWithNoNeighbours() {
    // No tiles on the board around (7,7) → should default to HORIZONTAL
    Map<Point, Tile> pending = new HashMap<>();
    pending.put(new Point(7, 7), new Tile('A'));

    Move move = PendingMoveBuilderController.build(pending, player, game);
    assertNotNull(move);
    assertEquals(Direction.HORIZONTAL, move.getDirection());
    assertEquals(MoveType.PLAY, move.getType());
  }

  @Test
  void buildShouldReturnVerticalMoveForSingleTileWithVerticalNeighbours() {
    // Place tiles above and below (7,7) to simulate a vertical word context
    game.getBoard().getSquare(new Point(7, 6)).setTile(new Tile('J'));
    game.getBoard().getSquare(new Point(7, 8)).setTile(new Tile('G'));

    Map<Point, Tile> pending = new HashMap<>();
    pending.put(new Point(7, 7), new Tile('I'));

    Move move = PendingMoveBuilderController.build(pending, player, game);
    assertNotNull(move);
    assertEquals(Direction.VERTICAL, move.getDirection());
  }

  @Test
  void buildShouldReturnVerticalMoveForSingleTileAppendedToVerticalWord() {
    // Simulate appending S after JIG (vertical word at column 7, rows 6-8)
    game.getBoard().getSquare(new Point(7, 6)).setTile(new Tile('J'));
    game.getBoard().getSquare(new Point(7, 7)).setTile(new Tile('I'));
    game.getBoard().getSquare(new Point(7, 8)).setTile(new Tile('G'));

    Map<Point, Tile> pending = new HashMap<>();
    pending.put(new Point(7, 9), new Tile('S'));

    Move move = PendingMoveBuilderController.build(pending, player, game);
    assertNotNull(move);
    assertEquals(Direction.VERTICAL, move.getDirection());
  }

  @Test
  void buildShouldReturnHorizontalMoveWhenTilesOnSameRow() {
    Map<Point, Tile> pending = new HashMap<>();
    pending.put(new Point(5, 7), new Tile('H'));
    pending.put(new Point(6, 7), new Tile('I'));
    pending.put(new Point(7, 7), new Tile('S'));

    Move move = PendingMoveBuilderController.build(pending, player, game);
    assertNotNull(move);
    assertEquals(Direction.HORIZONTAL, move.getDirection());
    assertEquals(3, move.getTiles().size());
  }

  @Test
  void buildShouldReturnVerticalMoveWhenTilesOnSameColumn() {
    Map<Point, Tile> pending = new HashMap<>();
    pending.put(new Point(7, 5), new Tile('H'));
    pending.put(new Point(7, 6), new Tile('I'));
    pending.put(new Point(7, 7), new Tile('S'));

    Move move = PendingMoveBuilderController.build(pending, player, game);
    assertNotNull(move);
    assertEquals(Direction.VERTICAL, move.getDirection());
    assertEquals(3, move.getTiles().size());
  }

  @Test
  void buildShouldReturnNullWhenTilesNotAligned() {
    Map<Point, Tile> pending = new HashMap<>();
    pending.put(new Point(5, 5), new Tile('A'));
    pending.put(new Point(6, 6), new Tile('B')); // diagonal → invalid

    Move move = PendingMoveBuilderController.build(pending, player, game);
    assertNull(move);
  }

  @Test
  void buildShouldSetCorrectStartPosition() {
    Map<Point, Tile> pending = new HashMap<>();
    pending.put(new Point(3, 7), new Tile('A'));
    pending.put(new Point(4, 7), new Tile('B'));

    Move move = PendingMoveBuilderController.build(pending, player, game);
    assertNotNull(move);
    assertEquals(3, move.getStartPosition().getX());
    assertEquals(7, move.getStartPosition().getY());
  }
}