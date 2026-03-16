package fr.ubordeaux.scrabble.view.cli.renderer;

import fr.ubordeaux.scrabble.model.core.Rack;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import java.util.List;

/**
 * Responsible for rendering a player's rack.
 */
public class RackRenderer {

  public void render(Player player) {
    Rack rack = player.getRack();
    List<Tile> tiles = rack.getTiles();

    System.out.print("Rack : [ ");
    for (Tile tile : tiles) {
      renderTile(tile);
    }
    System.out.println("]");
  }

  private void renderTile(Tile tile) {
    System.out.printf("%c(%d) ", tile.getCharacter(), tile.getValue());
  }

}
