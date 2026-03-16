package fr.ubordeaux.scrabble.view.gui;

import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds an EXCHANGE Move from a string typed by the user in the dialog.
 *
 * <p>
 * MVC: Converts raw user input (String) into a Move object. Belongs in the view package because it
 * depends on user input format.
 */
public class ExchangeMoveBuilder {

  private ExchangeMoveBuilder() {}

  /**
   * Builds an EXCHANGE Move from a letter string (e.g. "ABC").
   *
   * @param letters the letters to exchange, as a string (e.g. "ABC")
   * @param player the player performing the exchange
   * @return the constructed Move, or null if a letter is not found in the rack
   */
  public static Move build(String letters, Player player) {
    List<Tile> rack = player.getRack().getTiles();
    List<Tile> toExchange = new ArrayList<>();

    for (char c : letters.toCharArray()) {
      boolean found = false;
      for (Tile tile : rack) {
        if (tile.getCharacter() == c && !toExchange.contains(tile)) {
          toExchange.add(tile);
          found = true;
          break;
        }
      }
      if (!found)
        return null;
    }

    return toExchange.isEmpty() ? null : Move.createExchange(player, toExchange);
  }
}
