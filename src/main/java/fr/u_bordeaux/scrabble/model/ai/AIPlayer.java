package fr.u_bordeaux.scrabble.model.ai;

import fr.u_bordeaux.scrabble.model.interfaces.Player;

/**
 * Represents an artificial player (AI).
 */
public class AIPlayer extends  Player {
    /**
     * Base constructor for any player.
     *
     * @param name The name of the player.
     */
    public AIPlayer(String name) {
        super(name);
    }
}
