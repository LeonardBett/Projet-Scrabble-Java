package fr.u_bordeaux.scrabble.view.gui;

import java.util.ArrayList;
import java.util.List;

import fr.u_bordeaux.scrabble.model.core.Move;
import fr.u_bordeaux.scrabble.model.core.Tile;
import fr.u_bordeaux.scrabble.model.interfaces.Player;

/**
 * Builds an EXCHANGE Move from a string typed by the user in the dialog.
 *
 * ✅ MVC: Converts raw user input (String) → Move object.
 * Belongs in the view package because it depends on user input format.
 */
public class ExchangeMoveBuilder {

    private ExchangeMoveBuilder() {}

    /**
     * Builds an EXCHANGE Move from a letter string (e.g. "ABC").
     * Returns null if a letter is not found in the player's rack.
     */
    public static Move build(String letters, Player player) {
        List<Tile> rack       = player.getRack().getTiles();
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
            if (!found) return null; // caller will show the error
        }

        return toExchange.isEmpty() ? null : Move.createExchange(player, toExchange);
    }
}