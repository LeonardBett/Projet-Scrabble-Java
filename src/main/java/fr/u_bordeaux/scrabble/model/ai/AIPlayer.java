package fr.u_bordeaux.scrabble.model.ai;

import fr.u_bordeaux.scrabble.model.core.Game;
import fr.u_bordeaux.scrabble.model.core.Move;
import fr.u_bordeaux.scrabble.model.core.PlayableWord;
import fr.u_bordeaux.scrabble.model.core.Tile;
import fr.u_bordeaux.scrabble.model.dictionary.GADDAG;
import fr.u_bordeaux.scrabble.model.interfaces.Player;
import fr.u_bordeaux.scrabble.model.utils.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an artificial player (AI).
 */
public class AIPlayer extends Player {
    
    private final MinimaxSolver solver;

    /**
     * Base constructor for any player.
     *
     * @param name The name of the player.
     * @param difficultyLevel Defines the search depth (e.g., 1 = basic, 2 = medium)
     */
    public AIPlayer(String name, int difficultyLevel) {
        super(name);
        this.solver = new MinimaxSolver(difficultyLevel);
    }

    /**
     * Allows the user to toggle the AI into Expectiminimax mode.
     * If false, the AI uses classic Minimax (default).
     * * @param enable true to enable Expectiminimax, false for Minimax
     */
    public void setExpectiminimaxMode(boolean enable) {
        solver.setUseExpectiminimax(enable);
        System.out.println("AI [" + getName() + "] mode changed to: " 
            + (enable ? "EXPECTIMINIMAX" : "Classic MINIMAX"));
    }

    public boolean isExpectiminimaxMode() {
        return solver.isUsingExpectiminimax();
    }

    /**
     * Makes the AI play its turn. It analyzes the game state and executes the best action.
     * * @param game The current game instance
     * @param gaddag The GADDAG dictionary used to generate moves
     */
    public void playTurn(Game game, GADDAG gaddag) {
        System.out.println("AI " + getName() + " is thinking using the " 
            + (isExpectiminimaxMode() ? "Expectiminimax" : "Minimax") + " algorithm...");
        
        PlayableWord bestPlay = solver.findBestMove(game, gaddag);
        
        Move moveToExecute;
        if (bestPlay != null) {
            System.out.println("The AI decided to play: " + bestPlay.getWord() + " for " + bestPlay.getScore() + " points.");
            
            List<Tile> wordTiles = new ArrayList<>();
            for (char c : bestPlay.getWord().toCharArray()) {
                wordTiles.add(new Tile(c)); 
            }
            
            Point startPos = new Point(bestPlay.getHookX(), bestPlay.getHookY());
            moveToExecute = Move.createPlay(this, wordTiles, startPos, bestPlay.getDirection());
            
        } else {
            System.out.println("The AI could not find any word and passes its turn.");
            moveToExecute = Move.createPass(this);
        }
        
        game.executeMove(moveToExecute);
    }
}