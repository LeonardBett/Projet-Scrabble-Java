package fr.ubordeaux.scrabble.model.enums;

/**
 * Enum representing the different types of squares on the board (multipliers).
 */
public enum SquareType {
  /** Normal square with no letter or word bonus. */
  NORMAL(1, 1),
  /** Double letter value. */
  DOUBLE_LETTER(2, 1),
  /** Triple letter value. */
  TRIPLE_LETTER(3, 1),
  /** Double word value. */
  DOUBLE_WORD(1, 2),
  /** Triple word value. */
  TRIPLE_WORD(1, 3);

  private final int letterMultiplier;
  private final int wordMultiplier;

  SquareType(int letterMultiplier, int wordMultiplier) {
    this.letterMultiplier = letterMultiplier;
    this.wordMultiplier = wordMultiplier;
  }

  /**
   * Returns the letter multiplier for this square type.
   *
   * @return the letter multiplier value
   */
  public int getLetterMultiplier() {
    return letterMultiplier;
  }

  /**
   * Returns the word multiplier for this square type.
   *
   * @return the word multiplier value
   */
  public int getWordMultiplier() {
    return wordMultiplier;
  }
}
