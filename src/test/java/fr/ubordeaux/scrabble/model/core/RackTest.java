package fr.ubordeaux.scrabble.model.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class RackTest {

  /**
   * Test that tiles can be added to a rack until it reaches maximum capacity, after which
   * additional tiles cannot be added.
   */
  @Test
  void addTileShouldSucceedUntilRackIsFull() {
    Rack rack = new Rack();

    for (int i = 0; i < Rack.MAX_SIZE; i++) {
      assertTrue(rack.addTile(new Tile('A')));
    }

    assertTrue(rack.isFull());
    assertFalse(rack.addTile(new Tile('B')));
    assertEquals(Rack.MAX_SIZE, rack.getTiles().size());
  }

  /**
   * Test that removing a tile returns true when the tile exists in the rack and false when
   * attempting to remove a non-existent tile.
   */
  @Test
  void removeTileShouldReturnTrueOnlyWhenTileExists() {
    Rack rack = new Rack();
    Tile tile = new Tile('C');
    rack.addTile(tile);

    assertTrue(rack.removeTile(tile));
    assertFalse(rack.removeTile(tile));
    assertTrue(rack.isEmpty());
  }

  /**
   * Test that getTiles returns a defensive copy of the tiles list, preventing external modification
   * of the rack's internal state.
   */
  @Test
  void getTilesShouldReturnDefensiveCopy() {
    Rack rack = new Rack();
    rack.addTile(new Tile('D'));

    List<Tile> snapshot = rack.getTiles();
    snapshot.clear();

    assertEquals(1, rack.getTiles().size());
  }
}
