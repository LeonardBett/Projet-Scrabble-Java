package fr.ubordeaux.scrabble.model.enums;

/**
 * Represents the distinct colors assigned to players in the game.
 * Includes the corresponding ANSI escape codes for CLI formatting.
 */
public enum PlayerColor {
  /** Blue color for player 1. */
  BLUE("\u001B[34m"),
  /** Red color for player 2. */
  RED("\u001B[31m"),
  /** Yellow color for player 3. */
  YELLOW("\u001B[33m"),
  /** Green color for player 4. */
  GREEN("\u001B[32m"),
  /** Reset ANSI code to default terminal color. */
  RESET("\u001B[0m");

  private final String ansiCode;

  /**
   * Constructs a PlayerColor with its associated ANSI escape sequence.
   *
   * @param ansiCode The ANSI escape sequence for the color.
   */
  PlayerColor(String ansiCode) {
    this.ansiCode = ansiCode;
  }

  /**
   * Retrieves the ANSI escape code to apply the color in a standard terminal.
   *
   * @return The string representing the ANSI code.
   */
  public String getAnsiCode() {
    return ansiCode;
  }

  /**
   * Utility method to assign a predefined color based on the player's index.
   *
   * @param index The order index of the player (0 to 3).
   * @return The corresponding PlayerColor.
   */
  public static PlayerColor fromIndex(int index) {
    return switch (index) {
      case 0 -> BLUE;
      case 1 -> RED;
      case 2 -> YELLOW;
      case 3 -> GREEN;
      default -> RESET;
    };
  }
}