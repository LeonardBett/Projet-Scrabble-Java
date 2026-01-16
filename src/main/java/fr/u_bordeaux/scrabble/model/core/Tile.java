package fr.u_bordeaux.scrabble.model.core;

/**
 * Represents a game tile (letter) with its point value.
 */
public class Tile {
    /** The character displayed on the tile (e.g., 'A'). */
    private final char character;
    
    /** The point value of the tile (e.g., 1 for 'A'). */
    private final int value;

    public Tile(char character, int value) {
        this.character = character;
        this.value = value;
    }

    public char getCharacter() {
        return character;
    }

    public int getValue() {
        return value;
    }
}
