package fr.ubordeaux.scrabble.model.core;

import fr.ubordeaux.scrabble.model.enums.Direction;

/**
 * Represents a candidate word playable from a given hook square.
 */
public class PlayableWord {
  private final int hookX;
  private final int hookY;
  private final String word;
  private final Direction direction;
  private int score; // à ajouter avec la methode de leonard
  private final String gaddagRepresentation;

  /**
   * Creates a playable word candidate.
   *
   * @param hookX                hook x coordinate.
   * @param hookY                hook y coordinate.
   * @param word                 candidate word text.
   * @param direction            candidate placement direction.
   * @param gaddagRepresentation internal Gaddag representation used to build the word.
   */
  public PlayableWord(int hookX, int hookY, String word, Direction direction,
                      String gaddagRepresentation) {
    this.hookX = hookX;
    this.hookY = hookY;
    this.word = word;
    this.direction = direction;
    this.score = 0; // à ajouter avec la methode de leonard
    this.gaddagRepresentation = gaddagRepresentation;
  }

  /**
   * Returns the hook x coordinate.
   *
   * @return hook x coordinate.
   */
  public int getHookX() {
    return hookX;
  }

  /**
   * Returns the hook y coordinate.
   *
   * @return hook y coordinate.
   */
  public int getHookY() {
    return hookY;
  }

  /**
   * Returns the candidate word.
   *
   * @return candidate word text.
   */
  public String getWord() {
    return word;
  }

  /**
   * Returns the candidate placement direction.
   *
   * @return placement direction.
   */
  public Direction getDirection() {
    return direction;
  }

  /**
   * Returns the computed score for this candidate.
   *
   * @return candidate score.
   */
  public int getScore() {
    return score;
  }

  /**
   * Returns the internal Gaddag path representation.
   *
   * @return Gaddag path.
   */
  public String getGaddagRepresentation() {
    return gaddagRepresentation;
  }

  /**
   * Sets the score associated with this candidate.
   *
   * @param score candidate score.
   */
  public void setScore(int score) {
    this.score = score;
  } // à ajouter avec la methode de leonard

  @Override
  public String toString() {
    return String.format("PlayableWord[word=%s, x=%d, y=%d, dir=%s, gaddag=%s, score=%d]", word,
        hookX, hookY, direction, gaddagRepresentation, score);
  }
}
