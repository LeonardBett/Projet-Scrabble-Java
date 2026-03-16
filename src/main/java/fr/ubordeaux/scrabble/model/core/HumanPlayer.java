package fr.ubordeaux.scrabble.model.core;

import fr.ubordeaux.scrabble.model.interfaces.Player;

/**
 * Represents a standard human player.
 */
public class HumanPlayer extends Player {
  /**
   * Creates a new human player.
   *
   * @param name The name of the player.
   */
  public HumanPlayer(String name) {
    super(name);
  }
}
