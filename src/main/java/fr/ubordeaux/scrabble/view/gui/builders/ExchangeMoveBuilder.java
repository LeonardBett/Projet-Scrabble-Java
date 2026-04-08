package fr.ubordeaux.scrabble.view.gui.builders;

import fr.ubordeaux.scrabble.controller.builders.ExchangeMoveBuilderController;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.interfaces.Player;

/**
 * Builds an exchange move from a string typed by the user in the dialog.
 */
public class ExchangeMoveBuilder {

  private ExchangeMoveBuilder() {
  }

  /**
   * Builds an EXCHANGE Move from a letter string (e.g. "ABC").
   *
   * @param letters the letters to exchange, as a string (e.g. "ABC")
   * @param player  the player performing the exchange
   * @return the constructed Move, or null if a letter is not found in the rack
   */
  public static Move build(String letters, Player player) {
    return ExchangeMoveBuilderController.build(letters, player);
  }
}
