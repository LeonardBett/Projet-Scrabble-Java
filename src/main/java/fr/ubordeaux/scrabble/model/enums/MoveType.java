package fr.ubordeaux.scrabble.model.enums;

/**
 * Enumeration of possible move types.
 */
public enum MoveType {
  /** Place a word on the board. */
  PLAY,
  /** Exchange tiles with the bag. */
  EXCHANGE,
  /** Pass the turn without playing. */
  PASS
}
