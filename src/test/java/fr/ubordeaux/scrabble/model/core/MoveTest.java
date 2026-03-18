package fr.ubordeaux.scrabble.model.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.enums.Direction;
import fr.ubordeaux.scrabble.model.enums.MoveType;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.model.utils.Point;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class MoveTest {

  /**
   * Test that the createPass factory method correctly creates a PASS move with the specified player
   * and empty tiles list.
   */
  @Test
  void createPassShouldBuildPassMove() {
    HumanPlayer player = new HumanPlayer("Alice", PlayerColor.BLUE);
    Move move = Move.createPass(player);

    assertEquals(player, move.getPlayer());
    assertEquals(MoveType.PASS, move.getType());
    assertTrue(move.getTiles().isEmpty());
  }

  /**
   * Test that the createExchange factory method rejects invalid input (null or empty tile list) by
   * throwing IllegalArgumentException.
   */
  @Test
  void createExchangeShouldRejectEmptyTiles() {
    HumanPlayer player = new HumanPlayer("Bob", PlayerColor.BLUE);

    assertThrows(IllegalArgumentException.class, () -> Move.createExchange(player, List.of()));
    assertThrows(IllegalArgumentException.class, () -> Move.createExchange(player, null));
  }

  /**
   * Test that the createPlay factory method validates all required fields (tiles, position,
   * direction) and throws exceptions for invalid inputs.
   */
  @Test
  void createPlayShouldValidateRequiredFields() {
    HumanPlayer player = new HumanPlayer("Carol", PlayerColor.BLUE);
    List<Tile> word = List.of(new Tile('C'));

    assertThrows(IllegalArgumentException.class,
        () -> Move.createPlay(player, List.of(), new Point(7, 7), Direction.HORIZONTAL));
    assertThrows(IllegalArgumentException.class,
        () -> Move.createPlay(player, word, null, Direction.HORIZONTAL));
    assertThrows(IllegalArgumentException.class,
        () -> Move.createPlay(player, word, new Point(7, 7), null));
  }

  /**
   * Test that Move provides defensive copies to prevent external modification of internal
   * collections (tiles, placed positions, placed tiles).
   */
  @Test
  void moveShouldExposeUnmodifiableTilesAndDefensiveCopiesForPlacedData() {
    HumanPlayer player = new HumanPlayer("Dan", PlayerColor.BLUE);
    Move move =
        Move.createPlay(player, List.of(new Tile('D')), new Point(7, 7), Direction.HORIZONTAL);

    assertThrows(UnsupportedOperationException.class, () -> move.getTiles().add(new Tile('E')));

    List<Point> placedPositions = new ArrayList<>();
    placedPositions.add(new Point(7, 7));
    move.setPlacedPositions(placedPositions);
    placedPositions.add(new Point(7, 8));
    assertEquals(1, move.getPlacedPositions().size());

    List<Tile> placedTiles = new ArrayList<>();
    placedTiles.add(new Tile('D'));
    move.setPlacedTiles(placedTiles);
    placedTiles.add(new Tile('E'));
    assertEquals(1, move.getPlacedTiles().size());
  }

  /**
   * Test that the score gained from a move can be set and retrieved correctly.
   */
  @Test
  void scoreGainedShouldBeMutable() {
    HumanPlayer player = new HumanPlayer("Eve", PlayerColor.BLUE);
    Move move = Move.createPass(player);

    move.setScoreGained(42);
    assertEquals(42, move.getScoreGained());
  }
}
