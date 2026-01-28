package fr.u_bordeaux.scrabble.model.core;

import java.util.Objects;

/**
 * Represents a game tile (letter) with its point value.
 */
public class Tile {
    /** The character displayed on the tile (e.g., 'A'). */
    private final char character;

    /** The point value of the tile (e.g., 1 for 'A'). */
    private final int value;

    /**
     * Constructor that automatically assigns the standard Scrabble value based on the letter.
     *
     * @param character The letter character.
     */
    public Tile(char character) {
        this.character = character;
        this.value = getStandardValue(character);
    }

    public char getCharacter() {
        return character;
    }

    public int getValue() {
        return value;
    }

    /**
     * Returns the standard point value for a given letter (French Scrabble rules).
     *
     * @param character The letter.
     * @return The point value (0 for Joker/Blank).
     */
    public static int getStandardValue(char character) {
        char c = Character.toUpperCase(character);
        return switch (c) {
            case 'A', 'E', 'I', 'L', 'N', 'O', 'R', 'S', 'T', 'U' -> 1;
            case 'D', 'G', 'M' -> 2;
            case 'B', 'C', 'P' -> 3;
            case 'F', 'H', 'V' -> 4;
            case 'J', 'Q' -> 8;
            case 'K', 'W', 'X', 'Y', 'Z' -> 10;
            default -> 0; // Joker (Blank) or invalid characters
        };
    }

    @Override
    public String toString() {
        return "" + character;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Tile tile = (Tile) o;
        return character == tile.character && value == tile.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(character, value);
    }
}