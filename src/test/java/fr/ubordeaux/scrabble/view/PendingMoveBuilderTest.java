package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.enums.Direction;
import fr.ubordeaux.scrabble.model.enums.MoveType;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.model.utils.Point;
import fr.ubordeaux.scrabble.view.gui.PendingMoveBuilder;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PendingMoveBuilderTest {

  private Player player;

  @BeforeEach
  void setUp() {
    player = new HumanPlayer("Bob");
  }

  @Test
  void buildShouldReturnNullForEmptyMap() {
    assertNull(PendingMoveBuilder.build(new HashMap<>(), player));
  }

  @Test
  void buildShouldReturnHorizontalMoveForSingleTile() {
    Map<Point, Tile> pending = new HashMap<>();
    pending.put(new Point(7, 7), new Tile('A'));

    Move move = PendingMoveBuilder.build(pending, player);
    assertNotNull(move);
    assertEquals(Direction.HORIZONTAL, move.getDirection());
    assertEquals(MoveType.PLAY, move.getType());
  }

  @Test
  void buildShouldReturnHorizontalMoveWhenTilesOnSameRow() {
    Map<Point, Tile> pending = new HashMap<>();
    pending.put(new Point(5, 7), new Tile('H'));
    pending.put(new Point(6, 7), new Tile('I'));
    pending.put(new Point(7, 7), new Tile('S'));

    Move move = PendingMoveBuilder.build(pending, player);
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

    Move move = PendingMoveBuilder.build(pending, player);
    assertNotNull(move);
    assertEquals(Direction.VERTICAL, move.getDirection());
    assertEquals(3, move.getTiles().size());
  }

  @Test
  void buildShouldReturnNullWhenTilesNotAligned() {
    Map<Point, Tile> pending = new HashMap<>();
    pending.put(new Point(5, 5), new Tile('A'));
    pending.put(new Point(6, 6), new Tile('B')); // diagonal → invalid

    Move move = PendingMoveBuilder.build(pending, player);
    assertNull(move);
  }

  @Test
  void buildShouldSetCorrectStartPosition() {
    Map<Point, Tile> pending = new HashMap<>();
    pending.put(new Point(3, 7), new Tile('A'));
    pending.put(new Point(4, 7), new Tile('B'));

    Move move = PendingMoveBuilder.build(pending, player);
    assertNotNull(move);
    assertEquals(3, move.getStartPosition().getX());
    assertEquals(7, move.getStartPosition().getY());
  }
}
