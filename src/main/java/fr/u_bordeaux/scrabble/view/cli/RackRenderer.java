package fr.u_bordeaux.scrabble.view.cli;

import java.util.List;

import fr.u_bordeaux.scrabble.model.core.Rack;
import fr.u_bordeaux.scrabble.model.core.Tile;
import fr.u_bordeaux.scrabble.model.interfaces.Player;

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