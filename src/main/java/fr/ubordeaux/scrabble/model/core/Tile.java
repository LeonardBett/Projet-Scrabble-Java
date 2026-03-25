package fr.ubordeaux.scrabble.model.core;

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
   *
   * @param character the character represented by this tile
   */
  public Tile(char character) {
    this.character = character;
    this.value = getStandardValue(character);
    this.isJoker = false;
  }

  /**
   * NEW CONSTRUCTOR: Creates a tile acting as a Joker. It displays the chosen letter, but is worth
   * 0 points.
   *
   * @param character the character represented by this tile
   * @param isJoker true if this tile is a joker; false otherwise
   */
  public Tile(char character, boolean isJoker) {
    this.character = character;
    this.value = isJoker ? 0 : getStandardValue(character);
    this.isJoker = isJoker;
  }

  /**
   * Returns the displayed letter for this tile.
   *
   * @return tile character.
   */
  public char getCharacter() {
    return character;
  }

  /**
   * Returns the score value of this tile.
   *
   * @return tile value.
   */
  public int getValue() {
    return value;
  }

  /**
   * Indicates whether this tile is used as a joker (blank).
   *
   * @return true when joker behavior is enabled.
   */
  public boolean isJoker() {
    return isJoker;
  }

  /**
   * Returns the standard score for a letter in French Scrabble.
   *
   * @param character letter to score.
   * @return standard score value.
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
      default -> 0;
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
