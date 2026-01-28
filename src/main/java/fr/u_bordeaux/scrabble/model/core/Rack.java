package fr.u_bordeaux.scrabble.model.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player's rack containing their tiles.
 * Holds up to 7 tiles.
 */
public class Rack {
    /** Maximum number of tiles in a rack. */
    public static final int MAX_SIZE = 7;

    private final List<Tile> tiles;

    /**
     * Default constructor: initializes an empty rack.
     */
    public Rack() {
        this.tiles = new ArrayList<>();
    }

    /**
     * Adds a tile to the rack if it is not full.
     *
     * @param tile The tile to add.
     * @return true if the tile was added, false if the rack is full.
     */
    public boolean addTile(Tile tile) {
        if (tiles.size() < MAX_SIZE) {
            return tiles.add(tile);
        }
        return false;
    }

    /**
     * Removes a specific tile from the rack (used when playing a word).
     *
     * @param tile The tile object to remove.
     * @return true if the tile was present and removed.
     */
    public boolean removeTile(Tile tile) {
        return tiles.remove(tile);
    }

    /**
     * Returns a copy of the list of tiles to prevent external modification.
     *
     * @return A new list containing the tiles.
     */
    public List<Tile> getTiles() {
        return new ArrayList<>(tiles);
    }

    /**
     * Checks if the rack is full.
     *
     * @return true if the rack contains MAX_SIZE tiles.
     */
    public boolean isFull() {
        return tiles.size() == MAX_SIZE;
    }

    /**
     * Checks if the rack is empty.
     *
     * @return true if the rack contains no tiles.
     */
    public boolean isEmpty() {
        return tiles.isEmpty();
    }

    @Override
    public String toString() {
        return "Rack{" + "tiles=" + tiles + '}';
    }
}