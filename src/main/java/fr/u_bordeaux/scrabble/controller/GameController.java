package fr.u_bordeaux.scrabble.controller;

import fr.u_bordeaux.scrabble.model.core.Game;
import fr.u_bordeaux.scrabble.model.core.Move;
import fr.u_bordeaux.scrabble.model.interfaces.Player;
import fr.u_bordeaux.scrabble.view.UserInterface;
import fr.u_bordeaux.scrabble.view.cli.CLIInputHandler;
import fr.u_bordeaux.scrabble.view.cli.CLIView;
import fr.u_bordeaux.scrabble.model.core.HumanPlayer;

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
        view.refresh();
    }

    /**
     * Runs a CLI game loop if the provided view is a CLIView.
     * This will prompt for players (if missing), start the game and process
     * player actions until the game ends or the user quits.
     */
    public void runCli() {
        if (!(view instanceof CLIView)) {
            throw new IllegalStateException("CLI loop requires a CLIView instance as view.");
        }

        CLIInputHandler input = new CLIInputHandler();
        CLIView cliView = (CLIView) view;

        cliView.displayWelcome();

        // If not enough players, ask to create them
        if (game.getPlayers().size() < 2) {
            int num = input.askNumberOfPlayers();
            for (int i = 1; i <= num; i++) {
                String name = input.askPlayerName(i);
                Player p = new HumanPlayer(name);
                addPlayer(p);
            }
        }

        startGame();

        boolean running = true;
        while (running && !game.isGameOver()) {
            view.refresh();
            Player current = game.getCurrentPlayer();

            String action = input.askAction();
            switch (action) {
                case "1": // Play a word
                {
                    Move move = input.askPlayMove(current);
                    if (move != null) {
                        try {
                            handlePlayerMove(move);
                            view.displaySuccess("Coup joué.");
                        } catch (RuntimeException e) {
                            view.displayError(e.getMessage());
                        }
                    }
                    break;
                }
                case "2": // Exchange
                {
                    Move move = input.askExchangeMove(current);
                    if (move != null) {
                        try {
                            handlePlayerMove(move);
                            view.displaySuccess("Lettres échangées.");
                        } catch (RuntimeException e) {
                            view.displayError(e.getMessage());
                        }
                    }
                    break;
                }
                case "3": // Pass
                {
                    try {
                        handlePlayerMove(Move.createPass(current));
                        view.displayMessage(current.getName() + " a passé son tour.");
                    } catch (RuntimeException e) {
                        view.displayError(e.getMessage());
                    }
                    break;
                }
                case "4": // Undo
                {
                    undo();
                    break;
                }
                case "5": // Redo
                {
                    redo();
                    break;
                }
                case "6": // Quit
                {
                    if (input.askConfirmation("Voulez-vous vraiment quitter ?")) {
                        running = false;
                    }
                    break;
                }
                default:
                    view.displayError("Choix invalide.");
            }
        }

        // Game ended or user quit
        Player winner = game.determineWinner();
        if (winner != null) {
            view.displaySuccess("Partie terminée. Vainqueur: " + winner.getName());
        }

        input.close();
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
            view.refresh();

            
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
        view.refresh();
    }
    
    /**
     * Redoes the undone move.
     */
    public void redo() {
        game.redo();
        view.refresh();
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
