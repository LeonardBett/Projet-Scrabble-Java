package fr.u_bordeaux.scrabble.controller;

import fr.u_bordeaux.scrabble.model.core.Game;
import fr.u_bordeaux.scrabble.model.core.Move;
import fr.u_bordeaux.scrabble.model.interfaces.Player;
import fr.u_bordeaux.scrabble.view.UserInterface;

/**
 * Main controller (application logic).
 * Handles user input, updates the model and the view.
 * 
 * Responsibilities:
 * - Orchestrate communication between the view and the model
 * - Manage application logic (turns, validations)
 * - Notify the view of model changes
 */
public class GameController {
    private Game game;
    private UserInterface view;
    
    public GameController(Game game, UserInterface view) {
        this.game = game;
        this.view = view;
    }
    
    /**
     * Starts the game.
     */
    public void startGame() {
        if (game == null || view == null) {
            throw new IllegalStateException("Game and view must be initialized before starting.");
        }
        
        // Validate that at least 2 players are present
        if (game.getPlayers().size() < 2) {
            throw new IllegalStateException("At least 2 players must be present to start.");
        }
        
        // Initialize the game
        game.startGame();
        //view.refresh();
    }
    
    /**
     * Executes a player's move.
     * @param move The move to execute
     */
    public void handlePlayerMove(Move move) {
        try {
            if (move == null) {
                return;
            }
            
            // Execute the move in the model
            game.executeMove(move);
            
            // Notify the view
            //view.refresh();
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new RuntimeException("Invalid move: " + e.getMessage(), e);
        }
    }
    
    /**
     * Adds a player to the game.
     * @param player The player to add
     */
    public void addPlayer(Player player) {
        game.addPlayer(player);
    }
    
    /**
     * Undoes the last move.
     */
    public void undo() {
        game.undo();
        //view.refresh();
    }
    
    /**
     * Redoes the undone move.
     */
    public void redo() {
        game.redo();
        //view.refresh();
    }
    
    /**
     * Gets the game.
     * @return The Game model
     */
    public Game getGame() {
        return game;
    }
    
    /**
     * Gets the view.
     * @return The user interface
     */
    public UserInterface getView() {
        return view;
    }
}
