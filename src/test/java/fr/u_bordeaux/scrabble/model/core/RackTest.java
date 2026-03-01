package fr.u_bordeaux.scrabble.model.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RackTest {

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

    @Test
    void removeTileShouldReturnTrueOnlyWhenTileExists() {
        Rack rack = new Rack();
        Tile tile = new Tile('C');
        rack.addTile(tile);

        assertTrue(rack.removeTile(tile));
        assertFalse(rack.removeTile(tile));
        assertTrue(rack.isEmpty());
    }

    @Test
    void getTilesShouldReturnDefensiveCopy() {
        Rack rack = new Rack();
        rack.addTile(new Tile('D'));

        List<Tile> snapshot = rack.getTiles();
        snapshot.clear();

        assertEquals(1, rack.getTiles().size());
    }
}
