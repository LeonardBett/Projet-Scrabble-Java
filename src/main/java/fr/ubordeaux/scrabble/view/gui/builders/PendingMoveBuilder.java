package fr.ubordeaux.scrabble.view.gui.builders;

import fr.ubordeaux.scrabble.controller.builders.PendingMoveBuilderController;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.model.utils.Point;
import java.util.Map;

/**
 * Builds a PLAY move from the pending tiles map.
 */
public class PendingMoveBuilder {

  private PendingMoveBuilder() {
  }

  /**
   * Builds a Move from the pending tiles, or returns null if the tiles are not aligned (which the
   * view will show as an error).
   *
   * @param pendingTiles the tiles that have been placed pending validation
   * @param player the player placing the tiles
   * @param game the current game instance, used to infer direction when only one tile is placed
   * @return the constructed Move, or null if tiles are not properly aligned
   */
  public static Move build(Map<Point, Tile> pendingTiles, Player player, Game game) {
    return PendingMoveBuilderController.build(pendingTiles, player, game);
  }
}