package fr.ubordeaux.scrabble.controller.builders;

import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds an exchange move from letters typed by the user.
 */
public final class ExchangeMoveBuilderController {

  private ExchangeMoveBuilderController() {
  }

  /**
   * Builds an EXCHANGE move from letters.
   *
   * @param letters letters to exchange
   * @param player player performing the exchange
   * @return exchange move or null if invalid
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
      if (!found) {
        return null;
      }
    }

    return toExchange.isEmpty() ? null : Move.createExchange(player, toExchange);
  }
}