package fr.ubordeaux.scrabble.model.dictionary.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.enums.SquareType;
import fr.ubordeaux.scrabble.model.utils.Point;
import org.junit.jupiter.api.Test;

class SquareTest {

  /**
   * Test that creating a Square without a tile results in an empty square with
   * the correct position
   * and type.
   */
  @Test
  void constructorWithoutTileShouldCreateEmptySquare() {
    Point point = new Point(2, 3);
    Square square = new Square(point, SquareType.DOUBLE_LETTER);

    assertTrue(square.isEmpty());
    assertEquals(point, square.getPosition());
    assertEquals(SquareType.DOUBLE_LETTER, square.getSquareType());
  }

  /**
   * Test that setting and removing tiles on a square correctly updates the
   * square's occupancy
   * status.
   */
  @Test
  void setTileShouldUpdateSquareOccupancy() {
    Square square = new Square(new Point(0, 0), SquareType.NORMAL);
    Tile tile = new Tile('H');

    square.setTile(tile);
    assertEquals(tile, square.getTile());

    square.setTile(null);
    assertNull(square.getTile());
    assertTrue(square.isEmpty());
  }
}
