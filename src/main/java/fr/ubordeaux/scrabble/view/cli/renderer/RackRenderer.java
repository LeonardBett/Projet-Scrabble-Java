package fr.ubordeaux.scrabble.view.cli.renderer;

import fr.ubordeaux.scrabble.i18n.I18n;
import fr.ubordeaux.scrabble.model.core.Rack;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import java.util.List;

/**
 * Responsible for rendering a player's rack.
 */
public class RackRenderer {

  /**
   * Default constructor for RackRenderer.
   */
  public RackRenderer() {
  }

  /**
   * Renders the player's rack in the CLI console.
   *
   * @param player player whose rack must be rendered
   */
  public void render(Player player) {
    Rack rack = player.getRack();
    List<Tile> tiles = rack.getTiles();

    System.out.print(I18n.translate("cli.rack.label"));
    for (Tile tile : tiles) {
      renderTile(tile);
    }
    System.out.println("]");
  }

  private void renderTile(Tile tile) {
    System.out.printf("%c(%d) ", tile.getCharacter(), tile.getValue());
  }
}