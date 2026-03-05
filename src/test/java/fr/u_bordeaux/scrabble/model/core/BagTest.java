package fr.u_bordeaux.scrabble.model.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BagTest {

    /**
     * Test that the Bag constructor initializes with 102 tiles,
     * which is the standard French Scrabble tile distribution.
     */
    @Test
    void constructorShouldInitializeStandardFrenchDistributionSize() {
        Bag bag = new Bag();

        assertEquals(102, bag.size());
        assertFalse(bag.isEmpty());
    }

    /**
     * Test that drawing a tile decreases the bag size and returns null
     * when attempting to draw from an empty bag.
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
     * Test that putting tiles back into the bag correctly increases
     * the bag size by the number of tiles returned.
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
     * Test that removing a specific tile from the bag returns true when
     * the tile exists and false when the tile is not in the bag.
     */
    @Test
    void removeTileShouldReturnTrueWhenMatchingTileExists() {
        Bag bag = new Bag();

        assertTrue(bag.removeTile(new Tile('Z')));
        assertFalse(bag.removeTile(new Tile('#')));
    }
}
