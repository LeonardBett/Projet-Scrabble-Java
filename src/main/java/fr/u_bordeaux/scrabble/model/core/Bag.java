package fr.u_bordeaux.scrabble.model.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Represents the bag of tiles in the game.
 * Manages the stock of letters and random distribution.
 */
public class Bag {
    private final List<Tile> tiles;
    private final Random random;

    /**
     * Constructor: initializes the bag with the standard distribution of tiles.
     */
    public Bag() {
        this.tiles = new ArrayList<>();
        this.random = new Random();
        initializeBag();
        shuffle();
    }

    private void initializeBag() {
        // French Scrabble Distribution (102 tiles)
        addTiles('A', 9);
        addTiles('B', 2);
        addTiles('C', 2);
        addTiles('D', 3);
        addTiles('E', 15);
        addTiles('F', 2);
        addTiles('G', 2);
        addTiles('H', 2);
        addTiles('I', 8);
        addTiles('J', 1);
        addTiles('K', 1);
        addTiles('L', 5);
        addTiles('M', 3);
        addTiles('N', 6);
        addTiles('O', 6);
        addTiles('P', 2);
        addTiles('Q', 1);
        addTiles('R', 6);
        addTiles('S', 6);
        addTiles('T', 6);
        addTiles('U', 6);
        addTiles('V', 2);
        addTiles('W', 1);
        addTiles('X', 1);
        addTiles('Y', 1);
        addTiles('Z', 1);

        // Jokers (Blanks) - represented by a space ' '
        addTiles(' ', 2);
    }

    private void addTiles(char letter, int count) {
        for (int i = 0; i < count; i++) {
            tiles.add(new Tile(letter));
        }
    }

    /**
     * Shuffles the content of the bag.
     */
    public void shuffle() {
        Collections.shuffle(tiles, random);
    }

    /**
     * Draws a single tile from the bag.
     *
     * @return The drawn Tile, or null if the bag is empty.
     */
    public Tile drawTile() {
        if (tiles.isEmpty()) {
            return null;
        }
        return tiles.removeLast();
    }

    /**
     * Puts a list of tiles back into the bag and shuffles it.
     * Used for the "Exchange" move.
     *
     * @param tilesToReturn The tiles to put back.
     */
    public void putBack(List<Tile> tilesToReturn) {
        tiles.addAll(tilesToReturn);
        shuffle();
    }

    /**
     * Removes a specific tile from the bag.
     * Used for undoing an exchange move.
     *
     * @param tile The tile to remove.
     * @return true if the tile was found and removed, false otherwise.
     */
    public boolean removeTile(Tile tile) {
        return tiles.remove(tile);
    }

    public boolean isEmpty() {
        return tiles.isEmpty();
    }

    public int size() {
        return tiles.size();
    }
}