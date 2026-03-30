package fr.ubordeaux.scrabble.model.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class BagTest {

  /**
   * Test that the default Bag constructor initializes with 100 tiles (English distribution).
   */
  @Test
  void constructorShouldInitializeStandardEnglishDistributionSize() {
    Bag bag = new Bag();

    assertEquals(100, bag.size());
    assertFalse(bag.isEmpty());
  }

  @Test
  void constructorShouldInitializeFrenchDistributionSizeWhenRequested() {
    Bag bag = new Bag("fr");

    assertEquals(102, bag.size());
    assertFalse(bag.isEmpty());
  }

  /**
   * Test that drawing a tile decreases the bag size and returns null when attempting to draw from
   * an empty bag.
   */
  @Test
  void drawTileShouldDecreaseSizeAndReturnNullWhenEmpty() {
    Bag bag = new Bag();
    int initialSize = bag.size();

    Tile firstDraw = bag.drawTile();
    assertNotNull(firstDraw);
    assertEquals(initialSize - 1, bag.size());

    while (!bag.isEmpty()) {
      bag.drawTile();
    }

    assertNull(bag.drawTile());
  }

  /**
   * Test that putting tiles back into the bag correctly increases the bag size by the number of
   * tiles returned.
   */
  @Test
  void putBackShouldReinsertTilesInBag() {
    Bag bag = new Bag();
    bag.drawTile();
    int sizeAfterOneDraw = bag.size();

    List<Tile> returned = List.of(new Tile('A'), new Tile('B'));
    bag.putBack(returned);

    assertEquals(sizeAfterOneDraw + 2, bag.size());
  }

  /**
   * Test that removing a specific tile from the bag returns true when the tile exists and false
   * when the tile is not in the bag.
   */
  @Test
  void removeTileShouldReturnTrueWhenMatchingTileExists() {
    Bag bag = new Bag();

    assertTrue(bag.removeTile(new Tile('Z')));
    assertFalse(bag.removeTile(new Tile('#')));
  }

  @Test
  void constructorShouldCreateTwoJokerTiles() {
    Bag bag = new Bag();
    List<Tile> allTiles = bagToList(bag);

    List<Tile> jokers = allTiles.stream().filter(Tile::isJoker).collect(Collectors.toList());
    assertEquals(2, jokers.size());
    assertTrue(jokers.stream().allMatch(t -> t.getCharacter() == ' '));
    assertTrue(jokers.stream().allMatch(t -> t.getValue() == 0));
  }

  private List<Tile> bagToList(Bag bag) {
    List<Tile> allTiles = new java.util.ArrayList<>();
    while (!bag.isEmpty()) {
      allTiles.add(bag.drawTile());
    }
    return allTiles;
  }
}
