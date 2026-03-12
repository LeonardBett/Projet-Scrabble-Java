package fr.u_bordeaux.scrabble.model.ai;

import java.util.ArrayList;
import java.util.List;

import fr.u_bordeaux.scrabble.model.core.Game;
import fr.u_bordeaux.scrabble.model.core.Move;
import fr.u_bordeaux.scrabble.model.core.PlayableWord;
import fr.u_bordeaux.scrabble.model.core.Tile;
import fr.u_bordeaux.scrabble.model.dictionary.GADDAG;
import fr.u_bordeaux.scrabble.model.interfaces.Player;
import fr.u_bordeaux.scrabble.model.utils.Point;

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
     * @param game The current game instance
     * @param gaddag The GADDAG dictionary used to generate moves
     */
    public void playTurn(Game game, GADDAG gaddag) {
        System.out.println("AI " + getName() + " is thinking using the " 
            + (isExpectiminimaxMode() ? "Expectiminimax" : "Minimax") + " algorithm...");
        
        PlayableWord bestPlay = solver.findBestMove(game, gaddag);
        
        Move moveToExecute;
        if (bestPlay != null) {
            System.out.println("The AI decided to play: " + bestPlay.getWord() + " for " + bestPlay.getScore() + " points.");
            
            int hookIndex = bestPlay.getGaddagRepresentation().indexOf('>') - 1;
            
            int startX = bestPlay.getDirection() == fr.u_bordeaux.scrabble.model.enums.Direction.HORIZONTAL ? 
                         bestPlay.getHookX() - hookIndex : bestPlay.getHookX();
            int startY = bestPlay.getDirection() == fr.u_bordeaux.scrabble.model.enums.Direction.VERTICAL ? 
                         bestPlay.getHookY() - hookIndex : bestPlay.getHookY();
            
            Point startPos = new Point(startX, startY);
            
            // 1. Copy the AI rack to identify the Joker
            java.util.List<Character> myRack = new java.util.ArrayList<>();
            for (Tile t : getRack().getTiles()) {
                myRack.add(t.getCharacter());
            }

            // 2. Take the letters that are NOT already on the board
            List<Tile> wordTiles = new ArrayList<>();
            String word = bestPlay.getWord();
            
            for (int i = 0; i < word.length(); i++) {
                int currentX = startX + (bestPlay.getDirection() == fr.u_bordeaux.scrabble.model.enums.Direction.HORIZONTAL ? i : 0);
                int currentY = startY + (bestPlay.getDirection() == fr.u_bordeaux.scrabble.model.enums.Direction.VERTICAL ? i : 0);
                
                fr.u_bordeaux.scrabble.model.core.Square sq = game.getBoard().getSquare(new Point(currentX, currentY));
                
                // If the square is empty, the letter comes from our rack
                if (sq != null && sq.isEmpty()) {
                    char neededChar = word.charAt(i);
                    
                    if (myRack.contains(neededChar)) {
                        // We have the actual letter
                        myRack.remove((Character) neededChar);
                        wordTiles.add(new Tile(neededChar)); 
                    } else {
                        // We DON'T have the letter, so it MUST be the Joker!
                        myRack.remove((Character) ' ');
                        wordTiles.add(new Tile(neededChar, true)); // Using the new Joker constructor
                    }
                }
            }
            
            // Remove when fixed
            System.out.println("\n=== DÉBOGAGE IA ===");
            System.out.println("Mot trouvé par le GADDAG et validé : [" + word + "]");
            System.out.println("Position de départ : " + startPos + " | Direction : " + bestPlay.getDirection());
            System.out.print("Tuiles physiques extraites du chevalet de l'IA pour ce coup : [ ");
            for (Tile t : wordTiles) {
                System.out.print(t.getCharacter() + " ");
            }
            System.out.println("]");
            System.out.println("===================\n");
            // remove when fixed
            
            moveToExecute = Move.createPlay(this, wordTiles, startPos, bestPlay.getDirection());            
        } else {
            System.out.println("The AI could not find any word and passes its turn.");
            moveToExecute = Move.createPass(this);
        }
        
        game.executeMove(moveToExecute);
    }
}