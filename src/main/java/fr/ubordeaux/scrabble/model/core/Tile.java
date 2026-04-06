package fr.ubordeaux.scrabble.model.core;

import java.util.Objects;

/**
 * Represents a game tile (letter) with its point value.
 */
public class Tile {
  private static final String DEFAULT_LANGUAGE = "en";
  private static volatile String activeLanguage = DEFAULT_LANGUAGE;

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
    this.isJoker = (character == ' ');
  }

  /**
   * Standard constructor with explicit language.
   *
   * @param character the character represented by this tile
   * @param language language code ("en" or "fr")
   */
  public Tile(char character, String language) {
    this.character = character;
    this.value = getStandardValue(character, language);
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
   * Joker-aware constructor with explicit language.
   *
   * @param character the character represented by this tile
   * @param isJoker true if this tile is a joker; false otherwise
   * @param language language code ("en" or "fr")
   */
  public Tile(char character, boolean isJoker, String language) {
    this.character = character;
    this.value = isJoker ? 0 : getStandardValue(character, language);
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
    return getStandardValue(character, activeLanguage);
  }

  /**
   * Returns the standard score for a letter according to the provided language.
   *
   * @param character letter to score.
   * @param language language code ("en" or "fr")
   * @return standard score value.
   */
  public static int getStandardValue(char character, String language) {
    char c = Character.toUpperCase(character);
    String normalizedLanguage = normalizeLanguage(language);

    if ("fr".equals(normalizedLanguage)) {
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

    return switch (c) {
      case 'A', 'E', 'I', 'L', 'N', 'O', 'R', 'S', 'T', 'U' -> 1;
      case 'D', 'G' -> 2;
      case 'B', 'C', 'M', 'P' -> 3;
      case 'F', 'H', 'V', 'W', 'Y' -> 4;
      case 'K' -> 5;
      case 'J', 'X' -> 8;
      case 'Q', 'Z' -> 10;
      default -> 0;
    };
  }

  /**
   * Sets the active language used by constructors and default score lookups.
   *
   * @param language language code ("en" or "fr")
   */
  public static void setActiveLanguage(String language) {
    activeLanguage = normalizeLanguage(language);
  }

  /**
   * Returns the active language used by tile defaults.
   *
   * @return active language code.
   */
  public static String getActiveLanguage() {
    return activeLanguage;
  }

  /**
   * Normalizes language to supported values.
   *
   * @param language language candidate.
   * @return "fr" when French is requested, otherwise "en".
   */
  public static String normalizeLanguage(String language) {
    return "fr".equalsIgnoreCase(language) ? "fr" : DEFAULT_LANGUAGE;
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
    return character == tile.character && value == tile.value && isJoker == tile.isJoker;
  }

  @Override
  public int hashCode() {
    return Objects.hash(character, value, isJoker);
  }
}
