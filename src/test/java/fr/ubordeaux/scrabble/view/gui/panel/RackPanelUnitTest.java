package fr.ubordeaux.scrabble.view.gui.panel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import fr.ubordeaux.scrabble.model.core.Rack;
import fr.ubordeaux.scrabble.model.core.Tile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for RackPanel and Rack model integration behavior.
 */
class RackPanelUnitTest {
  private Rack rack;

  @BeforeEach
  void setUp() {
    rack = new Rack();
  }

  @Test
  void rackCanHoldTiles() {
    Tile tile = new Tile('A');
    boolean added = rack.addTile(tile);
    assertEquals(true, added, "should add tile to empty rack");
  }

  @Test
  void rackInitiallyEmpty() {
    assertEquals(0, rack.getTiles().size(), "rack should be empty");
  }

  @Test
  void rackCanBeFilledToCapacity() {
    int maxSize = Rack.MAX_SIZE;
    for (int i = 0; i < maxSize; i++) {
      Tile tile = new Tile((char) ('A' + i));
      rack.addTile(tile);
    }
    assertEquals(maxSize, rack.getTiles().size(), "rack should be full");
  }

  @Test
  void rackRejectsTilesWhenFull() {
    for (int i = 0; i < Rack.MAX_SIZE; i++) {
      rack.addTile(new Tile((char) ('A' + i)));
    }
    boolean added = rack.addTile(new Tile('Z'));
    assertEquals(false, added, "should not add tile when full");
  }

  @Test
  void rackCanRemoveTile() {
    Tile tile = new Tile('X');
    rack.addTile(tile);
    assertEquals(1, rack.getTiles().size(), "rack should have 1 tile");
    boolean removed = rack.removeTile(tile);
    assertEquals(true, removed, "should remove tile");
    assertEquals(0, rack.getTiles().size(), "rack should be empty after removal");
  }

  @Test
  void rackRemoveNonexistentTileFails() {
    Tile tile1 = new Tile('A');
    Tile tile2 = new Tile('B');
    rack.addTile(tile1);
    boolean removed = rack.removeTile(tile2);
    assertFalse(removed, "should not remove tile not in rack");
  }
}
