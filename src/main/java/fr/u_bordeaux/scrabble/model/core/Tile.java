package fr.u_bordeaux.scrabble.model.core;

import java.util.Objects;

/**
 * Represents a game tile (letter) with its point value.
 */
public class Tile {
    private final char character;
    private final int value;
    private final boolean isJoker;

    /**
     * Standard constructor.
     */
    public Tile(char character) {
        this.character = character;
        this.value = getStandardValue(character);
        this.isJoker = false;
    }

    /**
     * NEW CONSTRUCTOR: Creates a tile acting as a Joker.
     * It displays the chosen letter, but is worth 0 points.
     */
    public Tile(char character, boolean isJoker) {
        this.character = character;
        this.value = isJoker ? 0 : getStandardValue(character);
        this.isJoker = isJoker;
    }

    public char getCharacter() {
        return character;
    }

    public int getValue() {
        return value;
    }

    public boolean isJoker() {
        return isJoker;
    }

    public static int getStandardValue(char character) {
        char c = Character.toUpperCase(character);
        return switch (c) {
            case 'A', 'E', 'I', 'L', 'N', 'O', 'R', 'S', 'T', 'U' -> 1;
            case 'D', 'G', 'M' -> 2;
            case 'B', 'C', 'P' -> 3;
            case 'F', 'H', 'V' -> 4;
            case 'J', 'Q' -> 8;
            case 'K', 'W', 'X', 'Y', 'Z' -> 10;
            default -> 0; 
        };
    }

    @Override
    public String toString() {
        return "" + character;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tile tile = (Tile) o;
        return character == tile.character && value == tile.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(character, value);
    }
}